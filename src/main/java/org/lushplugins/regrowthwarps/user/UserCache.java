package org.lushplugins.regrowthwarps.user;

import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.Record;
import org.jooq.Result;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;
import org.lushplugins.regrowthwarps.util.jooq.BinaryUUIDBinding;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UserCache extends org.lushplugins.lushlib.utils.cache.UserCache<WarpUser> {

    public UserCache(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    protected CompletableFuture<WarpUser> load(UUID uuid) {
        return RegrowthWarps.getInstance().getStorageHandler().query((context) -> {
            Result<Record> result = context
                .select(
                    UsersTable.USER_ID,
                    UsersTable.DEFAULT_WARP,
                    WarpsTable.TABLE.asterisk()
                )
                .from(UsersTable.TABLE)
                .leftJoin(WarpsTable.TABLE)
                .on(WarpsTable.OWNER_ID.eq(UsersTable.USER_ID))
                .where(UsersTable.UUID.eq(BinaryUUIDBinding.to(uuid)))
                .fetch();
            if (result.isEmpty()) {
                return new WarpUser(uuid);
            }

            Map<String, Warp> warps = result.stream()
                .map(Warp::findOrRead)
                .collect(Collectors.toMap(
                    Warp::name,
                    warp -> warp
                ));

            Record record = result.getFirst();
            return new WarpUser(uuid, warps, record.get(UsersTable.DEFAULT_WARP));
        });
    }
}
