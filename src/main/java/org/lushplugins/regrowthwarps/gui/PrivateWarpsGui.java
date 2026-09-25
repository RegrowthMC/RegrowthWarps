package org.lushplugins.regrowthwarps.gui;

import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.stream.Stream;

public class PrivateWarpsGui extends WarpsGui {

    @Override
    public GuiConfig getGuiConfig() {
        return RegrowthWarps.getInstance().getConfigManager().getPrivateWarpsGui();
    }

    @Override
    public Stream<Warp> getContentStream(Gui gui) {
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(gui.actor().uuid());
        return user != null ? user.getWarps().stream() : Stream.empty();
    }
}
