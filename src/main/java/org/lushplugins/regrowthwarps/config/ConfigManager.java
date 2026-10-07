package org.lushplugins.regrowthwarps.config;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.chatcolorhandler.paper.PaperColor;
import org.lushplugins.regrowthwarps.RegrowthWarps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigManager {
    private List<String> categories;
    private int reserveNamesFor;
    private int markInactiveAfter;
    private Map<String, String> messages;

    public ConfigManager() {
        RegrowthWarps.getInstance().saveDefaultConfig();
    }

    public void reload() {
        RegrowthWarps.getInstance().reloadConfig();
        ConfigurationSection config = RegrowthWarps.getInstance().getConfig();

        this.categories = config.getStringList("categories").stream()
            .map(String::toLowerCase)
            .toList();
        this.reserveNamesFor = config.getInt("reserve-name-for", -1);
        this.markInactiveAfter = config.getInt("mark-inactive-after", -1);
        this.messages = config.getConfigurationSection("messages").getValues(false).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> (String) entry.getValue()
            ));
    }

    public List<String> categories() {
        return categories;
    }

    public int reserveNamesFor() {
        return reserveNamesFor;
    }

    public int markInactiveAfter() {
        return markInactiveAfter;
    }

    public @Nullable String message(String key) {
        return this.messages.get(key);
    }

    public String message(String key, String def) {
        return this.messages.getOrDefault(key, def);
    }

    public String getMessageOrEmpty(String key) {
        return message(key, "");
    }

    public void sendMessage(CommandSender recipient, String key, @Nullable Function<String, String> parser) {
        String message = this.messages.get(key);
        if (message == null) {
            return;
        }

        if (parser != null) {
            message = parser.apply(message);
        }

        PaperColor.handler().sendMessage(recipient, message);
    }

    public void sendMessage(@NotNull CommandSender recipient, @NotNull String key) {
        sendMessage(recipient, key, null);
    }

    public void sendActionBarMessage(Player recipient, String key, @Nullable Function<String, String> parser) {
        String message = this.messages.get(key);
        if (message == null) {
            return;
        }

        if (parser != null) {
            message = parser.apply(message);
        }

        PaperColor.handler().sendActionBarMessage(recipient, message);
    }
}
