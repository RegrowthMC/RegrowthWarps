package org.lushplugins.regrowthwarps;

import org.jooq.impl.DSL;
import org.lushplugins.guihandler.GuiHandler;
import org.lushplugins.guihandler.slot.SlotProvider;
import org.lushplugins.lushlib.utils.plugin.SpigotPlugin;
import org.lushplugins.regrowthwarps.command.AdminWarpsCommand;
import org.lushplugins.regrowthwarps.command.PrivateWarpsCommand;
import org.lushplugins.regrowthwarps.command.PublicWarpsCommand;
import org.lushplugins.regrowthwarps.command.WarpsCommand;
import org.lushplugins.regrowthwarps.config.ConfigManager;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;
import org.lushplugins.regrowthwarps.user.UserCache;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.lamp.annotation.AdminWarps;
import org.lushplugins.regrowthwarps.util.lamp.annotation.OwnedPublicWarps;
import org.lushplugins.regrowthwarps.util.lamp.annotation.PrivateWarps;
import org.lushplugins.regrowthwarps.util.lamp.annotation.PublicWarps;
import org.lushplugins.regrowthwarps.warp.WarpManager;
import org.lushplugins.storagehandler.StorageHandler;
import revxrsal.commands.bukkit.BukkitLamp;

import java.util.Collections;

public final class RegrowthWarps extends SpigotPlugin {
    private static RegrowthWarps plugin;

    private GuiHandler guiHandler;
    private ConfigManager configManager;
    private UserCache userCache;
    private StorageHandler storageHandler;
    private WarpManager warpManager;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        this.guiHandler = GuiHandler.builder(this)
            .registerLabelProvider(' ', SlotProvider.builder().build())
            .build();

        this.configManager = new ConfigManager();
        this.configManager.reload();

        this.userCache = new UserCache(this);
        this.storageHandler = StorageHandler.builder(this).build();
        this.storageHandler.execute(context -> context
            .createTableIfNotExists(UsersTable.TABLE)
            .column(UsersTable.USER_ID)
            .column(UsersTable.UUID)
            .column(UsersTable.DEFAULT_WARP)
            .constraints(
                DSL.constraint("key_user_id").primaryKey(UsersTable.USER_ID),
                DSL.constraint("unique_uuid").unique(UsersTable.UUID)
            )
            .execute()
        );
        this.storageHandler.execute(context -> context
            .createTableIfNotExists(WarpsTable.TABLE)
            .column(WarpsTable.WARP_ID)
            .column(WarpsTable.NAME)
            .column(WarpsTable.DISPLAY_NAME)
            .column(WarpsTable.DESCRIPTION)
            .column(WarpsTable.LOCATION)
            .column(WarpsTable.OWNER_ID)
            .column(WarpsTable.VISIBILITY)
            .column(WarpsTable.NAME_RESERVED_UNTIL)
            .column(WarpsTable.LAST_VISITED_DAY)
            .constraints(
                DSL.constraint("key_warp_id").primaryKey(WarpsTable.WARP_ID),
                DSL.constraint("unique_owner_name").unique(WarpsTable.OWNER_ID, WarpsTable.NAME)
            )
            .execute()
        );

        this.warpManager = new WarpManager();
        this.warpManager.reloadWarps();


        BukkitLamp.builder(this)
            .suggestionProviders(providers -> providers
                .addProviderForAnnotation(AdminWarps.class, (annotation) -> (context) -> {
                    return RegrowthWarps.getInstance().getWarpManager().getAdminWarpNames();
                })
                .addProviderForAnnotation(PrivateWarps.class, (annotation) -> (context) -> {
                    WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(context.actor().uniqueId());
                    return user != null ? user.getWarpNames() : Collections.emptyList();
                })
                .addProviderForAnnotation(OwnedPublicWarps.class, (annotation) -> (context) -> {
                    WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(context.actor().uniqueId());
                    return user != null ? user.getPublicWarpNames() : Collections.emptyList();
                })
                .addProviderForAnnotation(PublicWarps.class, (annotation) -> (context) -> {
                    return RegrowthWarps.getInstance().getWarpManager().getAllWarpDisplayNames();
                }))
            .build()
            .register(
                new AdminWarpsCommand(),
                new PrivateWarpsCommand(),
                new PublicWarpsCommand(),
                new WarpsCommand()
            );
    }

    @Override
    public void onDisable() {
        if (storageHandler != null) {
            storageHandler.shutdown();
        }
    }

    public GuiHandler getGuiHandler() {
        return guiHandler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public UserCache getUserCache() {
        return userCache;
    }

    public StorageHandler getStorageHandler() {
        return storageHandler;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public static RegrowthWarps getInstance() {
        return plugin;
    }
}
