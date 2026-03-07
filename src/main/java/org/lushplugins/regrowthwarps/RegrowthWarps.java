package org.lushplugins.regrowthwarps;

import org.bukkit.plugin.java.JavaPlugin;

public final class RegrowthWarps extends JavaPlugin {
    private static RegrowthWarps plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        // Enable implementation
    }

    @Override
    public void onDisable() {
        // Disable implementation
    }

    public static RegrowthWarps getInstance() {
        return plugin;
    }
}
