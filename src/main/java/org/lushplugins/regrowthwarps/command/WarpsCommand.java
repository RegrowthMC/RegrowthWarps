package org.lushplugins.regrowthwarps.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("regrowthwarps")
public class WarpsCommand {

    @Subcommand("reload")
    @CommandPermission("warps.reload")
    public void reload(CommandSender sender) {
        RegrowthWarps.getInstance().getConfigManager().reload();

        sender.sendMessage(Component.text()
            .content("RegrowthWarps reloaded!")
            .color(TextColor.fromHexString("#b7faa2"))
            .build());
    }
}
