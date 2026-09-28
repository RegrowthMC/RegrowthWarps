package org.lushplugins.regrowthwarps.config;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.gui.WarpSettingsGui;
import org.lushplugins.regrowthwarps.gui.WarpsGui;

public class WarpTypeConfig {
    private final String resourceName;
    private final WarpsGui guiInstance;
    private String name;
    private GuiConfig guiConfig;
    private Gui.Builder gui;
    private Gui.Builder settingsGui;

    public WarpTypeConfig(String resourceName, WarpsGui guiInstance) {
        this.resourceName = resourceName;
        this.guiInstance = guiInstance;

        RegrowthWarps.getInstance().saveDefaultResource(resourceName);
    }

    public void reload() {
        ConfigurationSection config = RegrowthWarps.getInstance().getConfigResource(this.resourceName);

        this.name = config.getString("name", "warp");
        this.guiConfig = new GuiConfig(config.getConfigurationSection("gui"));
        this.gui = this.guiConfig.applyTo(RegrowthWarps.getInstance().getGuiHandler().prepare(this.guiInstance));
        this.settingsGui = new GuiConfig(config.getConfigurationSection("settings-gui"))
            .applyTo(RegrowthWarps.getInstance().getGuiHandler().prepare(new WarpSettingsGui(this)));
    }

    public String name() {
        return name;
    }

    public GuiConfig guiConfig() {
        return guiConfig;
    }

    public Gui.Builder gui() {
        return gui;
    }

    public Gui.Builder settingsGui() {
        return settingsGui;
    }
}
