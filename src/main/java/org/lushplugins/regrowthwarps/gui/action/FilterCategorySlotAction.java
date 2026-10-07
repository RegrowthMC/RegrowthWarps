package org.lushplugins.regrowthwarps.gui.action;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.guihandler.slot.SlotAction;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
import org.lushplugins.regrowthwarps.warp.Warp;

public class FilterCategorySlotAction implements SlotAction {
    private final String warpType;
    private final Warp.Filter filter;

    public FilterCategorySlotAction(@Nullable ConfigurationSection config) {
        this.warpType = config != null ? config.getString("warp-type", "public") : "public";

        String category = config != null ? config.getString("category") : "undefined";
        this.filter = (warp) -> {
            if (category != null) {
                if (category.equalsIgnoreCase("undefined")) {
                    return warp.category() == null;
                } else {
                    return category.equalsIgnoreCase(warp.category());
                }
            }

            return true;
        };
    }

    @Override
    public void click(SlotContext context, InventoryClickEvent event) {
        WarpTypeConfig warpTypeConfig = switch (warpType) {
            case "private", "homes" -> RegrowthWarps.getInstance().getPrivateWarpsConfig();
            case "admin" -> RegrowthWarps.getInstance().getAdminWarpsConfig();
            default -> RegrowthWarps.getInstance().getPublicWarpsConfig();
        };

        warpTypeConfig.gui()
            .prepare()
            .provide("filter", filter)
            .open(context.gui().actor().player());
    }
}
