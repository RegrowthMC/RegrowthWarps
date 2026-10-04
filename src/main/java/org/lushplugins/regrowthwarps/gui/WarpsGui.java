package org.lushplugins.regrowthwarps.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.guihandler.annotation.CustomGui;
import org.lushplugins.guihandler.annotation.GuiActionHandler;
import org.lushplugins.guihandler.annotation.LabelledSlots;
import org.lushplugins.guihandler.annotation.Provided;
import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.guihandler.config.gui.ConfiguredPagedGui;
import org.lushplugins.guihandler.config.slot.IconConfig;
import org.lushplugins.guihandler.config.slot.SlotConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.gui.GuiAction;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.guihandler.slot.Slot;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.guihandler.slot.SlotIcon;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.StringUtil;
import org.lushplugins.regrowthwarps.util.WarpUtil;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Add filter button
@SuppressWarnings("unused")
@CustomGui(title = "Warps")
public abstract class WarpsGui implements ConfiguredPagedGui<Warp> {

    public abstract WarpTypeConfig getWarpTypeConfig();

    @Override
    public GuiConfig getGuiConfig() {
        return getWarpTypeConfig().guiConfig();
    }

    public void warpAction(SlotContext context, InventoryClickEvent event, Warp warp) {
        context.gui().close();
        warp.teleport(context.gui().actor().player());
        // TODO: Message
    }

    @GuiActionHandler(GuiAction.REFRESH)
    public void warps(Gui gui, @LabelledSlots('w') List<Slot> slots, @Provided @Nullable Warp.Filter filter) {
        GuiActor actor = gui.actor();
        WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(actor.uuid());
        if (user == null) {
            return;
        }

        SlotConfig warpsSlotConfig = getGuiConfig().slots().get('w');
        IconConfig warpsIconBase = warpsSlotConfig != null ? warpsSlotConfig.icon() : new IconConfig(DisplayItemStack.builder()
            .setType(Material.BAMBOO_DOOR)
            .setDisplayName("%warp_display_name%")
            .build());

        ArrayDeque<Warp> warps = this.getPageWarpContent(gui, gui.page(), slots.size(), filter);
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

            if (iconBuilder.hasLore()) {
                List<String> lore = new ArrayList<>(iconBuilder.getLore());

                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.contains("%warp_description%")) {
                        lore.remove(i);

                        if (warp.description() != null) {
                            lore.addAll(i, StringUtil.splitByCount(warp.description(), 50).stream()
                                .map(str -> line.replace("%warp_description%", str))
                                .toList());
                        } else {
                            lore.add(i, line.replace("%warp_description%", "<i>No Description</i>"));
                        }
                    }
                }

                iconBuilder.setLore(lore);
            }

            slot.icon(iconBuilder
                .replace(str -> WarpUtil.parsePlaceholders(warp, str))
                .build()
                .asItemStack(actor.player()));
            slot.action((context, event) -> warpAction(context, event, warp));
        }
    }

    @Override
    public char getContentLabel() {
        return 'w';
    }

    @Override
    public Comparator<Warp> getContentSortMethod(Gui gui) {
        Warp.SortingMethod sortMethod = gui.provided(Warp.SortingMethod.class);
        return sortMethod != null ? sortMethod.comparator() : Warp.SortingMethod.A_TO_Z.comparator();
    }

    public ArrayDeque<Warp> getPageWarpContent(Gui gui, int page, int pageSize, @Nullable Warp.Filter filter) {
        Stream<Warp> stream = getContentStream(gui);

        if (filter != null) {
            stream = stream.filter(filter);
        }

        return stream
            .sorted(this.getContentSortMethod(gui))
            .skip((long) (page - 1) * pageSize)
            .limit(pageSize)
            .collect(Collectors.toCollection(ArrayDeque::new));
    }
}
