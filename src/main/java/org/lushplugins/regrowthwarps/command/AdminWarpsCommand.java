package org.lushplugins.regrowthwarps.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.lushplugins.regrowthwarps.RegrowthWarps;
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
            // TODO: Message
            return;
        }

        player.teleportAsync(warp.location());
        // TODO: Message
    }

    @Command({"warps set", "setwarp"})
    @CommandPermission("warps.warp.admin.set")
    public void set(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        PublicWarpCache warpCache = RegrowthWarps.getInstance().getPublicWarpCache();

        String displayName = name;
        name = name.toLowerCase();

        if (warpCache.hasAdminWarp(name)) {
            Warp warp = warpCache.getAdminWarp(name);
            warp.location(player.getLocation());
        } else {
            Warp warp = new Warp(
                name,
                null,
                displayName,
                null,
                player.getLocation(),
                null,
                Warp.Visibility.PUBLIC,
                null,
                null
            );

            warp.cache();
            warp.save();
        }

        // TODO: Message
    }

    @Command({"warps delete", "delwarp"})
    @CommandPermission("warps.warp.admin.delete")
    public void delete(BukkitCommandActor actor, @AdminWarps String name) {
        Player player = actor.requirePlayer();
        Warp warp = RegrowthWarps.getInstance().getPublicWarpCache().getAdminWarp(name);
        if (warp == null) {
            // TODO: Message
            return;
        }

        warp.invalidateCache();
        warp.delete();
        // TODO: Message
    }
}
