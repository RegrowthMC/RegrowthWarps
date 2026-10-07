package org.lushplugins.regrowthwarps.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.guihandler.annotation.*;
import org.lushplugins.guihandler.config.GuiConfig;
import org.lushplugins.guihandler.config.gui.ConfiguredPagedGui;
import org.lushplugins.guihandler.config.slot.IconConfig;
import org.lushplugins.guihandler.config.slot.SlotConfig;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.guihandler.gui.event.GuiRefreshEvent;
import org.lushplugins.guihandler.gui.event.GuiSlotRefreshEvent;
import org.lushplugins.guihandler.slot.Slot;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.guihandler.slot.SlotIcon;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.Components;
import org.lushplugins.regrowthwarps.util.Dialogs;
import org.lushplugins.regrowthwarps.util.StringUtil;
import org.lushplugins.regrowthwarps.util.WarpUtil;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Player player = context.gui().actor().player();
        warp.teleport(player);
        RegrowthWarps.getInstance().getConfigManager().sendActionBarMessage(player, "teleported", str -> str
            .replace("%warp%", warp.displayName()));
    }

    @GuiEventListener
    public void warps(GuiRefreshEvent event, @LabelledSlots('w') List<Slot> slots, @Provided("filter") @Nullable Warp.Filter filter) {
        Gui gui = event.gui();
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
                iconBuilder.setLore(WarpUtil.parseDescriptionPlaceholder(warp, iconBuilder.getLore()));
            }

            slot.icon(iconBuilder
                .replace(str -> WarpUtil.parsePlaceholders(warp, str))
                .build()
                .asItemStack(actor.player()));
            slot.action((context, clickEvent) -> warpAction(context, clickEvent, warp));
        }
    }

    @SlotActionProvider('q')
    public void searchAction(SlotContext context) {
        WarpTypeConfig config = getWarpTypeConfig();
        Map<String, Object> providedValues = context.gui().providedValues();

        context.gui().actor().player().showDialog(Dialogs.shortTextInput(
            config.name(),
            "Search warps:",
            (response, audience, input) -> {
                if (input != null && input.isBlank()) {
                    input = null;
                }

                if (audience instanceof Player player) {
                    config.gui()
                        .prepare()
                        .provide(providedValues)
                        .provide("search_query", input)
                        .open(player);
                }
            }));
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

        String searchQuery = gui.provided("search_query", String.class);
        if (searchQuery != null) {
            stream = stream.filter((warp) -> {
                String description = warp.description();
                return warp.name().contains(searchQuery) || (description != null && description.contains(searchQuery));
            });
        }

        return stream
            .sorted(this.getContentSortMethod(gui))
            .skip((long) (page - 1) * pageSize)
            .limit(pageSize)
            .collect(Collectors.toCollection(ArrayDeque::new));
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
                Warp.SortingMethod sortingMethod = gui.provided(Warp.SortingMethod.class);
                String sortingMethodName = sortingMethod != null ? sortingMethod.friendlyName() : "A to Z";
                String searchQuery = gui.provided("search_query", String.class);
                String formattedSearchQuery = searchQuery != null ? searchQuery : "<gray><i>None</i></gray>";

                if (meta.hasDisplayName()) {
                    meta.displayName(Components.parse(meta.displayName(), str -> str
                        .replace("%sorting_method%", sortingMethodName)
                        .replace("%search_query%", formattedSearchQuery)));
                }

                if (meta.hasLore()) {
                    meta.lore(meta.lore().stream()
                        .map(line -> MiniMessage.miniMessage().serialize(line))
                        .map(line -> line
                            .replace("%sorting_method%", sortingMethodName)
                            .replace("%search_query%", formattedSearchQuery))
                        .map(line -> MiniMessage.miniMessage().deserialize(line))
                        .toList());
                }

                item.setItemMeta(meta);
            }
        }

        inventory.setItem(slot, item);
    }
}
