package org.lushplugins.regrowthwarps.gui.action;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.slot.SlotAction;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.regrowthwarps.util.Lists;
import org.lushplugins.regrowthwarps.warp.Warp;

public class SortingMethodSlotAction implements SlotAction {

    @Override
    public void click(SlotContext context, InventoryClickEvent event) {
        Gui gui = context.gui();

        Warp.SortingMethod currSortMethod = gui.provided(Warp.SortingMethod.class);
        if (currSortMethod == null) {
            currSortMethod = Warp.SortingMethod.A_TO_Z;
        }

        Warp.SortingMethod newSortMethod = Lists.findAdjacentValue(Warp.SortingMethod.values(), currSortMethod, event.isLeftClick());
        gui.provide(Warp.SortingMethod.class, newSortMethod);

        gui.page(1);
        gui.refresh();
    }
}
