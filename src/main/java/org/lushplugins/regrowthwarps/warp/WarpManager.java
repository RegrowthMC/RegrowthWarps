package org.lushplugins.regrowthwarps.warp;

import org.jooq.Result;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;
import org.lushplugins.regrowthwarps.util.Locations;
import org.lushplugins.regrowthwarps.util.jooq.BinaryUUIDBinding;

import java.util.*;

public class WarpManager {
    private final Map<String, Warp> warps = new HashMap<>();
    private final Map<String, Warp> adminWarps = new HashMap<>();

    public void reloadWarps() {
        this.warps.clear();
        this.adminWarps.clear();

        RegrowthWarps.getInstance().getStorageHandler().execute((context) -> {
            Result<org.jooq.Record> result = context
                .select()
                .from(WarpsTable.TABLE)
                .join(UsersTable.TABLE)
                .on(WarpsTable.OWNER_ID.eq(UsersTable.USER_ID))
                .where(WarpsTable.VISIBILITY.eq(Warp.Visibility.PUBLIC.name()))
                .fetch();

            result.stream()
                .map(record -> {
                    byte[] rawUUID = record.get(UsersTable.UUID);
                    return new Warp(
                        record.get(WarpsTable.NAME),
                        null, // TODO: Load icon
                        record.get(WarpsTable.DISPLAY_NAME),
                        record.get(WarpsTable.DESCRIPTION),
                        Locations.deserialize(record.get(WarpsTable.LOCATION)),
                        rawUUID != null ? BinaryUUIDBinding.from(rawUUID) : null,
                        Warp.Visibility.valueOf(record.get(WarpsTable.VISIBILITY)),
                        record.get(WarpsTable.NAME_RESERVED_UNTIL),
                        record.get(WarpsTable.LAST_VISITED_DAY)
                    );
                })
                .forEach(warp -> {
                    warps.put(warp.name(), warp);

                    if (warp.owner() == null) {
                        adminWarps.put(warp.name(), warp);
                    }
                });
        });
    }

    public Collection<Warp> getAllWarps() {
        return warps.values();
    }

    public Set<String> getAllWarpNames() {
        return warps.keySet();
    }

    public List<String> getAllWarpDisplayNames() {
        return warps.values().stream()
            .map(Warp::displayName)
            .toList();
    }

    public boolean hasWarp(String id) {
        return warps.containsKey(id);
    }

    public Warp getWarp(String id) {
        return warps.get(id);
    }

    public void addWarp(Warp warp) {
        warps.put(warp.name(), warp);
    }

    public void removeWarp(String id) {
        warps.remove(id);
    }

    public Collection<Warp> getAdminWarps() {
        return adminWarps.values();
    }

    public Set<String> getAdminWarpNames() {
        return adminWarps.keySet();
    }

    public boolean hasAdminWarp(String id) {
        return adminWarps.containsKey(id);
    }

    public Warp getAdminWarp(String id) {
        return adminWarps.get(id);
    }

    public void addAdminWarp(Warp warp) {
        adminWarps.put(warp.name(), warp);
    }

    public void removeAdminWarp(String id) {
        adminWarps.remove(id);
    }
}
