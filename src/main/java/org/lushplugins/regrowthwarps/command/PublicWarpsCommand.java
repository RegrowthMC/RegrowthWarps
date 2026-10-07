package org.lushplugins.regrowthwarps.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.WarpUtil;
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
            RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "invalid-warp", str -> str
                .replace("%warp%", name));
            return;
        }

        warp.teleport(player);
        RegrowthWarps.getInstance().getConfigManager().sendActionBarMessage(player, "teleported", str -> str
            .replace("%warp%", warp.displayName()));
    }

    @Command("pw set")
    @CommandPermission(value = "warps.warp.public.set", defaultAccess = PermissionDefault.TRUE)
    public void set(BukkitCommandActor actor, String displayName) {
        Player player = actor.requirePlayer();
        UUID uuid = player.getUniqueId();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(uuid);
        if (user == null) {
            RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "try-again");
            return;
        }

        String name = displayName.toLowerCase();

        if (user.hasWarp(name)) {
            Warp warp = user.getWarp(name);
            if (warp.visibility() != Warp.Visibility.PUBLIC) {
                RegrowthWarps.getInstance().getConfigManager().sendMessage(player, "warp-not-public", str -> str
                    .replace("%warp%", displayName));
                return;
            }

            warp.location(player.getLocation());

            RegrowthWarps.getInstance().getConfigManager().sendMessage(player, "warp-name-taken", str -> str
                .replace("%warp%", name));
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
                        uuid,
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

    @Command("pw delete")
    @CommandPermission(value = "warps.warp.public.delete", defaultAccess = PermissionDefault.TRUE)
    public void delete(BukkitCommandActor actor, @OwnedPublicWarps String name) {
        Player player = actor.requirePlayer();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(player.getUniqueId());
        if (user == null) {
            RegrowthWarps.getInstance().getConfigManager().sendMessage(actor.sender(), "try-again");
            return;
        }

        Warp warp = user.getWarp(name);
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
