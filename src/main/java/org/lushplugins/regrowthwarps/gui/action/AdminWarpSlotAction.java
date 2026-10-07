package org.lushplugins.regrowthwarps.gui.action;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.guihandler.slot.SlotAction;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.regrowthwarps.RegrowthWarps;

public class AdminWarpSlotAction implements SlotAction {
    private final String warpName;

    public AdminWarpSlotAction(@Nullable ConfigurationSection config) {
        this.warpName = config != null ? config.getString("warp") : null;
    }

    @Override
    public void click(SlotContext context, InventoryClickEvent event) {
        RegrowthWarps.getInstance().getPublicWarpCache().getAdminWarp(warpName)
            .teleport(context.gui().actor().player());
    }
}
