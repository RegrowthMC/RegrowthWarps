package org.lushplugins.regrowthwarps.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.lamp.annotation.OwnedPublicWarps;
import org.lushplugins.regrowthwarps.util.lamp.annotation.PublicWarps;
import org.lushplugins.regrowthwarps.warp.Warp;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.UUID;

@SuppressWarnings("unused")
@Command("pw")
public class PublicWarpsCommand {

    @Command("pw")
    @CommandPermission(value = "warps.warp.public.list", defaultAccess = PermissionDefault.TRUE)
    public void pw(BukkitCommandActor actor) {
        RegrowthWarps.getInstance().getPublicWarpsConfig().categoriesGui()
            .open(actor.requirePlayer());
    }

    @Command({"pw teleport", "pw"})
    @CommandPermission(value = "warps.warp.public.teleport", defaultAccess = PermissionDefault.TRUE)
    public void teleport(BukkitCommandActor actor, @PublicWarps String name) {
        Player player = actor.requirePlayer();
        Warp warp = RegrowthWarps.getInstance().getPublicWarpCache().getWarp(name.toLowerCase());
        if (warp == null) {
            // TODO: Message
            return;
        }

        player.teleportAsync(warp.location());
        // TODO: Message
    }

    @Command("pw set")
    @CommandPermission(value = "warps.warp.public.set", defaultAccess = PermissionDefault.TRUE)
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
            if (warp.visibility() != Warp.Visibility.PUBLIC) {
                // TODO: Message
                return;
            }

            warp.location(player.getLocation());
        } else {
            warp = new Warp(
                name,
                null,
                displayName,
                null,
                player.getLocation(),
                uuid,
                Warp.Visibility.PUBLIC,
                null,
                null
            );

            warp.cache();
        }

        warp.save();
        // TODO: Message
    }

    @Command("pw delete")
    @CommandPermission(value = "warps.warp.public.delete", defaultAccess = PermissionDefault.TRUE)
    public void delete(BukkitCommandActor actor, @OwnedPublicWarps String name) {
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
