package org.lushplugins.regrowthwarps.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lushplugins.guihandler.annotation.CustomGui;
import org.lushplugins.guihandler.annotation.GuiEventListener;
import org.lushplugins.guihandler.annotation.SlotActionProvider;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.guihandler.gui.event.GuiSlotRefreshEvent;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.util.Components;

@SuppressWarnings("unused")
@CustomGui(title = "Public Warps")
public class PublicWarpsCategoriesGui {

    @SlotActionProvider('y')
    public void yourPlayerWarps(GuiActor actor) {
        RegrowthWarps.getInstance().getPrivateWarpsConfig().gui()
            .open(actor.player());
    }

    @GuiEventListener
    public void onSlotRefresh(GuiSlotRefreshEvent event) {
        Gui gui = event.gui();
        int slot = event.slot().rawSlot();
        Inventory inventory = gui.inventory();

        ItemStack item = inventory.getItem(slot);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int totalWarps = RegrowthWarps.getInstance().getPublicWarpCache().getAllWarps().size();

                if (meta.hasDisplayName()) {
                    meta.displayName(Components.parse(meta.displayName(), str -> str
                        .replace("%total_warps%", String.valueOf(totalWarps))));
                }

                if (meta.hasLore()) {
                    meta.lore(meta.lore().stream()
                        .map(line -> MiniMessage.miniMessage().serialize(line))
                        .map(line -> line.replace("%total_warps%", String.valueOf(totalWarps)))
                        .map(line -> MiniMessage.miniMessage().deserialize(line))
                        .toList());
                }

                item.setItemMeta(meta);
            }
        }

        inventory.setItem(slot, item);
    }
}
