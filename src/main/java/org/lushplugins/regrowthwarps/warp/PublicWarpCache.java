package org.lushplugins.regrowthwarps.warp;

import org.jooq.Result;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;

import java.util.*;

public class PublicWarpCache {
    /**
     * Warps with {@link org.lushplugins.regrowthwarps.warp.Warp.Visibility#PUBLIC} (this includes admin warps)
     */
    private final Map<String, Warp> warps = new HashMap<>();
    /**
     * Warps with {@link org.lushplugins.regrowthwarps.warp.Warp.Visibility#PUBLIC} and no {@code owner}
     */
    private final Map<String, Warp> adminWarps = new HashMap<>();

    public void reloadWarps() {
        this.warps.clear();
        this.adminWarps.clear();

        RegrowthWarps.getInstance().getStorageHandler().execute((context) -> {
            Result<org.jooq.Record> result = context
                .select()
                .from(WarpsTable.TABLE)
                .leftJoin(UsersTable.TABLE)
                .on(WarpsTable.OWNER_ID.eq(UsersTable.USER_ID))
                .where(WarpsTable.VISIBILITY.eq(Warp.Visibility.PUBLIC.name()))
                .fetch();

            result.stream()
                .map(Warp::read)
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

    public void cacheWarp(Warp warp) {
        warps.put(warp.name(), warp);

        if (warp.isAdminWarp()) {
            adminWarps.put(warp.name(), warp);
        }
    }

    public void uncacheWarp(String id) {
        warps.remove(id);
        adminWarps.remove(id);
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
}
