package org.lushplugins.regrowthwarps.gui;

import org.lushplugins.guihandler.annotation.CustomGui;
import org.lushplugins.guihandler.annotation.SlotActionProvider;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.regrowthwarps.RegrowthWarps;

@SuppressWarnings("unused")
@CustomGui(title = "Public Warps")
public class PublicWarpsCategoriesGui {

    @SlotActionProvider('y')
    public void yourPlayerWarps(GuiActor actor) {
        RegrowthWarps.getInstance().getPrivateWarpsConfig().gui()
            .open(actor.player());
    }
}
