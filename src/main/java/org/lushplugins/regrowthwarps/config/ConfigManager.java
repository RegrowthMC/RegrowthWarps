package org.lushplugins.regrowthwarps.config;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.regrowthwarps.RegrowthWarps;

public class ConfigManager {
    private GuiConfig warpsGui;
    private GuiConfig privateWarpsGui;
    private GuiConfig publicWarpsGui;

    public ConfigManager() {
        RegrowthWarps.getInstance().saveDefaultConfig();
    }

    public void reload() {
        RegrowthWarps.getInstance().reloadConfig();
        ConfigurationSection config = RegrowthWarps.getInstance().getConfig();

        this.warpsGui = new GuiConfig(config.getConfigurationSection("admin-warps.gui"));
        this.privateWarpsGui = new GuiConfig(config.getConfigurationSection("private-warps.gui"));
        this.publicWarpsGui = new GuiConfig(config.getConfigurationSection("public-warps.gui"));
    }

    public GuiConfig getAdminWarpsGui() {
        return warpsGui;
    }

    public GuiConfig getPrivateWarpsGui() {
        return privateWarpsGui;
    }

    public GuiConfig getPublicWarpsGui() {
        return publicWarpsGui;
    }
}
