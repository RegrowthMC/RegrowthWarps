package org.lushplugins.regrowthwarps.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.lamp.annotation.PrivateWarps;
import org.lushplugins.regrowthwarps.warp.Warp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.UUID;

@SuppressWarnings("unused")
@Command("homes")
public class PrivateWarpsCommand {

    @Command("homes")
    @CommandPermission(value = "warps.warp.private.list", defaultAccess = PermissionDefault.TRUE)
    public void warps(BukkitCommandActor actor) {
        RegrowthWarps.getInstance().getPrivateWarpsConfig().gui()
            .open(actor.requirePlayer());
    }

    @Command({"homes teleport", "home"})
    @CommandPermission(value = "warps.warp.private.teleport", defaultAccess = PermissionDefault.TRUE)
    public void teleport(BukkitCommandActor actor, @PrivateWarps String name) {
        Player player = actor.requirePlayer();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(player.getUniqueId());
        if (user == null) {
            // TODO: Message
            return;
        }

        Warp warp = user.getWarp(name);
        if (warp == null) {
            // TODO: Message
            return;
        }

        player.teleportAsync(warp.location());
        // TODO: Message
    }

    @Command({"homes set", "sethome"})
    @CommandPermission(value = "warps.warp.private.set", defaultAccess = PermissionDefault.TRUE)
    public void set(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        UUID uuid = player.getUniqueId();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(uuid);
        if (user == null) {
            // TODO: Message
            return;
        }

        String displayName = name;
        name = name.toLowerCase();

        Warp warp;
        if (user.hasWarp(name)) {
            warp = user.getWarp(name);
            warp.location(player.getLocation());
        } else {
            warp = new Warp(
                name,
                null,
                displayName,
                null,
                player.getLocation(),
                uuid,
                Warp.Visibility.PRIVATE,
                null,
                null
            );

            warp.cache();
        }

        warp.save();
        // TODO: Message
    }

    @Command({"homes delete", "delhome"})
    @CommandPermission(value = "warps.warp.private.delete", defaultAccess = PermissionDefault.TRUE)
    public void delete(BukkitCommandActor actor, @PrivateWarps String name) {
        Player player = actor.requirePlayer();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(player.getUniqueId());
        if (user == null) {
            // TODO: Message
            return;
        }

        Warp warp = user.getWarp(name);
        if (warp == null) {
            // TODO: Message
            return;
        }

        warp.invalidateCache();
        warp.delete();
        // TODO: Message
    }
}
