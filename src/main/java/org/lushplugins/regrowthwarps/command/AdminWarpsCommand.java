package org.lushplugins.regrowthwarps.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.util.WarpUtil;
import org.lushplugins.regrowthwarps.util.lamp.annotation.AdminWarps;
import org.lushplugins.regrowthwarps.warp.Warp;
import org.lushplugins.regrowthwarps.warp.PublicWarpCache;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@SuppressWarnings("unused")
@Command("warp")
public class AdminWarpsCommand {

    @Command("warps")
    @CommandPermission(value = "warps.warp.admin.list", defaultAccess = PermissionDefault.TRUE)
    public void warps(BukkitCommandActor actor) {
        RegrowthWarps.getInstance().getAdminWarpsConfig().gui()
            .open(actor.requirePlayer());
    }

    @Command({"warps teleport", "warp"})
    @CommandPermission(value = "warps.warp.admin.teleport", defaultAccess = PermissionDefault.TRUE)
    public void teleport(BukkitCommandActor actor, @AdminWarps String name) {
        Player player = actor.requirePlayer();
        Warp warp = RegrowthWarps.getInstance().getPublicWarpCache().getAdminWarp(name);
        if (warp == null) {
            RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "invalid-warp", str -> str
                .replace("%warp%", name));
            return;
        }

        warp.teleport(player);
        RegrowthWarps.getInstance().getConfigManager().sendActionBarMessage(player, "teleported", str -> str
            .replace("%warp%", warp.displayName()));
    }

    @Command({"warps set", "setwarp"})
    @CommandPermission("warps.warp.admin.set")
    public void set(BukkitCommandActor actor, String displayName) {
        Player player = actor.requirePlayer();
        PublicWarpCache warpCache = RegrowthWarps.getInstance().getPublicWarpCache();

        String name = displayName.toLowerCase();
        if (warpCache.hasAdminWarp(name)) {
            Warp warp = warpCache.getAdminWarp(name);
            warp.location(player.getLocation());
            RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "set-warp", str -> str
                .replace("%warp%", displayName));
        } else {
            WarpUtil.isWarpNameAvailable(name, player.getUniqueId()).thenAccept((available) -> {
                if (available) {
                    Warp warp = new Warp(
                        name,
                        displayName,
                        null,
                        null,
                        null,
                        player.getLocation(),
                        null,
                        Warp.Visibility.PUBLIC,
                        null,
                        0,
                        null
                    );

                    warp.cache();
                    warp.save();

                    RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "set-warp", str -> str
                        .replace("%warp%", displayName));
                } else {
                    RegrowthWarps.getInstance().getConfigManager().sendMessage(player, "warp-name-taken", str -> str
                        .replace("%warp%", name));
                }
            });
        }
    }

    @Command({"warps delete", "delwarp"})
    @CommandPermission("warps.warp.admin.delete")
    public void delete(BukkitCommandActor actor, @AdminWarps String name) {
        Player player = actor.requirePlayer();
        Warp warp = RegrowthWarps.getInstance().getPublicWarpCache().getAdminWarp(name);
        if (warp == null) {
            RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "invalid-warp", str -> str
                .replace("%warp%", name));
            return;
        }

        warp.invalidateCache();
        warp.delete();
        RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "remove-warp", str -> str
            .replace("%warp%", warp.displayName()));
    }
}
