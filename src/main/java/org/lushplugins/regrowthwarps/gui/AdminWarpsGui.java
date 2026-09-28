package org.lushplugins.regrowthwarps.gui;

import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.stream.Stream;

public class AdminWarpsGui extends WarpsGui {

    @Override
    public WarpTypeConfig getWarpTypeConfig() {
        return RegrowthWarps.getInstance().getAdminWarpsConfig();
    }

    @Override
    public Stream<Warp> getContentStream(Gui gui) {
        return RegrowthWarps.getInstance().getPublicWarpCache().getAdminWarps().stream()
            .filter(warp -> warp.visibility() == Warp.Visibility.PUBLIC);
    }
}
