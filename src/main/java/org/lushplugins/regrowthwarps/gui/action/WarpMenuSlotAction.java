package org.lushplugins.regrowthwarps.gui.action;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.guihandler.slot.SlotAction;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.regrowthwarps.RegrowthWarps;

public class WarpMenuSlotAction implements SlotAction {
    private final String menuName;

    public WarpMenuSlotAction(@Nullable ConfigurationSection config) {
        this.menuName = config != null ? config.getString("menu") : "public_categories";
    }

    @Override
    public void click(SlotContext context, InventoryClickEvent event) {
        Player player = context.gui().actor().player();
        switch (menuName) {
            case "private", "homes" -> RegrowthWarps.getInstance().getPrivateWarpsConfig().gui().open(player);
            case "admin" -> RegrowthWarps.getInstance().getAdminWarpsConfig().gui().open(player);
            case "public_categories" -> RegrowthWarps.getInstance().getPublicWarpsConfig().categoriesGui().open(player);
            default -> RegrowthWarps.getInstance().getPublicWarpsConfig().gui().open(player);
        }
    }
}
