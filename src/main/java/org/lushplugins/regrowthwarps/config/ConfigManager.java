package org.lushplugins.regrowthwarps.config;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.regrowthwarps.RegrowthWarps;

public class ConfigManager {

    public ConfigManager() {
        RegrowthWarps.getInstance().saveDefaultConfig();
    }

    public void reload() {
        RegrowthWarps.getInstance().reloadConfig();
        ConfigurationSection config = RegrowthWarps.getInstance().getConfig();
    }
}
