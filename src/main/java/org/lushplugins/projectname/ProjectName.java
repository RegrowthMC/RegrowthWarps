package org.lushplugins.projectname;

import org.bukkit.plugin.java.JavaPlugin;

public final class ProjectName extends JavaPlugin {
    private static ProjectName plugin;

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

    public static ProjectName getInstance() {
        return plugin;
    }
}
