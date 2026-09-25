package org.lushplugins.regrowthwarps.user;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.util.jooq.BinaryUUIDBinding;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.*;

public class WarpUser {
    private final UUID uuid;
    private final Map<String, Warp> warps;
    private String defaultWarp;

    public WarpUser(
        UUID uuid,
        Map<String, Warp> warps,
        @Nullable String defaultWarp
    ) {
        this.uuid = uuid;
        this.warps = warps;
        this.defaultWarp = defaultWarp;
    }

    public WarpUser(UUID uuid) {
        this(uuid, new HashMap<>(),null);
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    public List<String> getPublicWarpNames() {
        return warps.entrySet()
            .stream()
            .filter(entry -> entry.getValue().visibility() == Warp.Visibility.PUBLIC)
            .map(Map.Entry::getKey)
            .toList();
    }

    public boolean hasWarp(String name) {
        return warps.containsKey(name);
    }

    public Warp getWarp(String name) {
        return warps.get(name);
    }

    public void addWarp(Warp warp) {
        warps.put(warp.name(), warp);
    }

    public void removeWarp(String id) {
        warps.remove(id);
    }

    public String getDefaultWarp() {
        return defaultWarp;
    }

    public void setDefaultWarp(String defaultWarp) {
        this.defaultWarp = defaultWarp;
        save();
    }

    public void save() {
        RegrowthWarps.getInstance().getStorageHandler().execute(context -> context
            .insertInto(UsersTable.TABLE)
            .set(UsersTable.UUID, BinaryUUIDBinding.to(uuid))
            .set(UsersTable.DEFAULT_WARP, defaultWarp)
            .onConflict(UsersTable.UUID)
            .doUpdate()
            .set(UsersTable.DEFAULT_WARP, defaultWarp)
            .execute()
        );
    }
}
