package org.lushplugins.regrowthwarps.gui;

import org.bukkit.Material;
import org.lushplugins.guihandler.annotation.CustomGui;
import org.lushplugins.guihandler.annotation.GuiActionHandler;
import org.lushplugins.guihandler.annotation.LabelledSlots;
import org.lushplugins.guihandler.config.gui.ConfiguredPagedGui;
import org.lushplugins.guihandler.config.slot.IconConfig;
import org.lushplugins.guihandler.config.slot.SlotConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.gui.GuiAction;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.guihandler.slot.LabelledSlotProvider;
import org.lushplugins.guihandler.slot.Slot;
import org.lushplugins.guihandler.slot.SlotIcon;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.lushlib.utils.StringUtils;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.Locations;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
@CustomGui(title = "Warps")
public abstract class WarpsGui implements ConfiguredPagedGui<Warp> {

    @GuiActionHandler(GuiAction.REFRESH)
    public void warps(Gui gui, @LabelledSlots('w') List<Slot> slots) {
        GuiActor actor = gui.actor();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(actor.uuid());
        if (user == null) {
            return;
        }

        SlotConfig warpsSlotConfig = getGuiConfig().slots().get('w');
        IconConfig warpsIconBase = warpsSlotConfig != null ? warpsSlotConfig.icon() : new IconConfig(DisplayItemStack.builder()
            .setType(Material.BIRCH_DOOR)
            .setDisplayName("%warp_display_name%")
            .build());

        ArrayDeque<Warp> warps = this.getPageContent(gui, gui.page(), slots.size());
        for (Slot slot : slots) {
            if (warps.isEmpty()) {
                slot.icon((SlotIcon) null);
                continue;
            }

            Warp warp = warps.pop();
            DisplayItemStack.Builder iconBuilder;
            if (warp.icon() != null) {
                iconBuilder = warpsIconBase.overwrite(DisplayItemStack.builder(warp.icon()));
            } else {
                iconBuilder = DisplayItemStack.builder(warpsIconBase.icon());
            }

            slot.icon(iconBuilder
                .replace(str -> str
                    .replace("%warp_name%", warp.name())
                    .replace("%warp_display_name%", warp.displayName())
                    .replace("%warp_description%", warp.description() != null ? warp.description() : "<i>No Description</i>")
                    .replace("%warp_location%", Locations.serializeFriendly(warp.location()))
                    .replace("%warp_visibility%", StringUtils.makeFriendly(warp.visibility().name())))
                .build()
                .asItemStack(actor.player())
            );
            slot.action((context, event) -> {
                context.gui().close();
                actor.player().teleportAsync(warp.location());
                // TODO: Message
            });
        }
    }

    @Override
    public char getContentLabel() {
        return 'w';
    }

    @Override
    public Comparator<Warp> getContentSortMethod() {
        return Comparator.comparing(Warp::name);
    }
}
