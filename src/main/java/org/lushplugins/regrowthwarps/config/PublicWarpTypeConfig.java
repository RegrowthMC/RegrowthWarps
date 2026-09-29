package org.lushplugins.regrowthwarps.config;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.gui.PublicWarpsCategoriesGui;
import org.lushplugins.regrowthwarps.gui.PublicWarpsGui;

public class PublicWarpTypeConfig extends WarpTypeConfig {
    private Gui.Builder categoriesGui;

    public PublicWarpTypeConfig() {
        super("public-warps.yml", new PublicWarpsGui());
    }

    @Override
    public void reload(ConfigurationSection config) {
        super.reload(config);

        this.categoriesGui = new GuiConfig(config.getConfigurationSection("categories-gui"))
            .applyTo(RegrowthWarps.getInstance().getGuiHandler().prepare(new PublicWarpsCategoriesGui()));
    }

    public Gui.Builder categoriesGui() {
        return categoriesGui;
    }
}
