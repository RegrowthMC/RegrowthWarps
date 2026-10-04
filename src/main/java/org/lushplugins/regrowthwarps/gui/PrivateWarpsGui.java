package org.lushplugins.regrowthwarps.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.stream.Stream;

public class PrivateWarpsGui extends WarpsGui {

    @Override
    public WarpTypeConfig getWarpTypeConfig() {
        return RegrowthWarps.getInstance().getPrivateWarpsConfig();
    }

    @Override
    public void warpAction(SlotContext context, InventoryClickEvent event, Warp warp) {
        context.gui().close();

        Player player = context.gui().actor().player();
        if (event.isLeftClick()) {
            warp.teleport(player);
            // TODO: Message
        } else if (event.isRightClick() && !warp.isAdminWarp() && player.getUniqueId().equals(warp.owner())) {
            if (event.isShiftClick()) {
                warp.invalidateCache();
                warp.delete();
            } else {
                getWarpTypeConfig().settingsGui()
                    .prepare()
                    .provide(warp)
                    .open(player);
            }
        }
    }

    @Override
    public Stream<Warp> getContentStream(Gui gui) {
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(gui.actor().uuid());
        return user != null ? user.getWarps().stream() : Stream.empty();
    }
}
