package org.lushplugins.regrowthwarps.gui.action;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.slot.SlotAction;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.regrowthwarps.warp.Warp;

public class SortingMethodSlotAction implements SlotAction {

    @Override
    public void click(SlotContext context, InventoryClickEvent event) {
        Gui gui = context.gui();

        Warp.SortingMethod currSortMethod = gui.provided(Warp.SortingMethod.class);
        if (currSortMethod == null) {
            currSortMethod = Warp.SortingMethod.A_TO_Z;
        }

        Warp.SortingMethod newSortMethod = findAdjacentValue(currSortMethod, event.isLeftClick());
        gui.provide(Warp.SortingMethod.class, newSortMethod);

        gui.page(1);
        gui.refresh();
    }

    public Warp.SortingMethod findAdjacentValue(Warp.SortingMethod value, boolean forward) {
        Warp.SortingMethod[] values = Warp.SortingMethod.values();
        for (int i = 0; i < values.length; i++) {
            Warp.SortingMethod current = values[i];
            if (value == current) {
                return values[i + (forward ? 1 : -1)];
            }
        }

        return value;
    }
}
