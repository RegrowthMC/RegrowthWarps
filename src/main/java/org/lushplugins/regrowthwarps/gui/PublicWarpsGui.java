package org.lushplugins.regrowthwarps.gui;

import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.stream.Stream;

public class PublicWarpsGui extends WarpsGui {

    @Override
    public GuiConfig getGuiConfig() {
        return RegrowthWarps.getInstance().getConfigManager().getPublicWarpsGui();
    }

    @Override
    public Stream<Warp> getContentStream(Gui gui) {
        return RegrowthWarps.getInstance().getWarpManager().getAllWarps().stream()
            .filter(warp -> warp.visibility() == Warp.Visibility.PUBLIC);
    }
}
