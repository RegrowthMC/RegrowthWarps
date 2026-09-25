package org.lushplugins.regrowthwarps.command;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.gui.AdminWarpsGui;
import org.lushplugins.regrowthwarps.util.lamp.annotation.AdminWarps;
import org.lushplugins.regrowthwarps.warp.Warp;
import org.lushplugins.regrowthwarps.warp.WarpManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@SuppressWarnings("unused")
@Command("warp")
public class AdminWarpsCommand {

    @Command("warps")
    @CommandPermission(value = "warps.warp.admin.list", defaultAccess = PermissionDefault.TRUE)
    public void warps(BukkitCommandActor actor) {
        RegrowthWarps.getInstance().getConfigManager().getAdminWarpsGui()
            .applyTo(RegrowthWarps.getInstance().getGuiHandler().prepare(new AdminWarpsGui()))
            .open(actor.requirePlayer());
    }

    @Command({"warps teleport", "warp"})
    @CommandPermission(value = "warps.warp.admin.teleport", defaultAccess = PermissionDefault.TRUE)
    public void teleport(BukkitCommandActor actor, @AdminWarps String name) {
        Player player = actor.requirePlayer();
        Warp warp = RegrowthWarps.getInstance().getWarpManager().getAdminWarp(name);
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
        WarpManager warpManager = RegrowthWarps.getInstance().getWarpManager();

        String displayName = name;
        name = name.toLowerCase();

        Warp warp;
        if (warpManager.hasAdminWarp(name)) {
            warp = warpManager.getAdminWarp(name);
            warp.location(player.getLocation());
        } else {
            warp = new Warp(
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

            warpManager.addAdminWarp(warp);
        }

        warp.save();
        // TODO: Message
    }

    @Command({"warps delete", "delwarp"})
    @CommandPermission("warps.warp.admin.delete")
    public void delete(BukkitCommandActor actor, @AdminWarps String name) {
        Player player = actor.requirePlayer();
        Warp warp = RegrowthWarps.getInstance().getWarpManager().getAdminWarp(name);
        if (warp == null) {
            // TODO: Message
            return;
        }

        RegrowthWarps.getInstance().getWarpManager().removeAdminWarp(warp.name());
        warp.delete();
        // TODO: Message
    }
}
