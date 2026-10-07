package org.lushplugins.regrowthwarps.util;

import org.lushplugins.lushlib.utils.StringUtils;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;
import org.lushplugins.regrowthwarps.util.jooq.BinaryUUIDBinding;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpUtil {

    public static CompletableFuture<Boolean> isWarpNameAvailable(String name, UUID requester) {
        return RegrowthWarps.getInstance().getStorageHandler().query((context) -> context
            .select()
            .from(WarpsTable.TABLE)
            .leftJoin(UsersTable.TABLE)
            .on(WarpsTable.OWNER_ID.eq(UsersTable.USER_ID))
            .where(WarpsTable.NAME.eq(name.toLowerCase()))
            .fetch()
            .stream()
            .noneMatch((record) -> {
                // Returns true if the name is unavailable
                if (record.get(WarpsTable.VISIBILITY).equals(Warp.Visibility.PUBLIC.name())) {
                    return true;
                } else {
                    Long nameReservedSince = record.get(WarpsTable.NAME_RESERVED_SINCE);
                    long nameReserveDuration = Duration.ofDays(RegrowthWarps.getInstance().getConfigManager().reserveNamesFor()).toSeconds();

                    if (nameReservedSince != null && nameReservedSince + nameReserveDuration > Instant.now().getEpochSecond()) {
                        byte[] rawUUID = record.get(UsersTable.UUID);
                        UUID uuid = rawUUID != null ? BinaryUUIDBinding.from(rawUUID) : null;
                        return uuid == null || !uuid.equals(requester);
                    }

                    return false;
                }
            }));
    }

    public static CompletableFuture<Collection<Warp>> findInactiveWarps() {
        return RegrowthWarps.getInstance().getStorageHandler().query(context -> {
            long epochDay = LocalDate.now().toEpochDay();
            int markInactiveAfter = RegrowthWarps.getInstance().getConfigManager().markInactiveAfter();
            long inactiveCutoff = epochDay - markInactiveAfter;

            return context
                .select()
                .from(WarpsTable.TABLE)
                .leftJoin(UsersTable.TABLE)
                .on(WarpsTable.OWNER_ID.eq(UsersTable.USER_ID))
                .where(WarpsTable.LAST_VISITED_DAY.lessOrEqual(inactiveCutoff))
                .fetch()
                .stream()
                .map(Warp::findOrRead)
                .toList();
        });
    }

    public static String parsePlaceholders(Warp warp, String string) {
        return string
            .replace("%warp_name%", warp.name())
            .replace("%warp_display_name%", warp.displayName())
            .replace("%warp_category%", warp.category() != null ? warp.category() : "Undefined")
            .replace("%warp_location%", Locations.serializeFriendly(warp.location()))
            .replace("%warp_visibility%", StringUtils.makeFriendly(warp.visibility().name()))
            .replace("%warp_visits%", String.valueOf(warp.visits()));
    }

    public static List<String> parseDescriptionPlaceholder(Warp warp, List<String> list) {
        List<String> output = new ArrayList<>(list);

        for (int i = 0; i < output.size(); i++) {
            String line = output.get(i);
            if (line.contains("%warp_description%")) {
                output.remove(i);

                if (warp.description() != null) {
                    output.addAll(i, StringUtil.splitByCount(warp.description(), 50).stream()
                        .map(str -> line.replace("%warp_description%", str))
                        .toList());
                } else {
                    output.add(i, line.replace("%warp_description%", "<i>No Description</i>"));
                }
            }
        }

        return output;
    }
}
