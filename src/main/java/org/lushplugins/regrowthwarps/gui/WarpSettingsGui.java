package org.lushplugins.regrowthwarps.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lushplugins.guihandler.annotation.*;
import org.lushplugins.guihandler.gui.Gui;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.guihandler.gui.event.GuiSlotRefreshEvent;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
import org.lushplugins.regrowthwarps.util.Components;
import org.lushplugins.regrowthwarps.util.Dialogs;
import org.lushplugins.regrowthwarps.util.Lists;
import org.lushplugins.regrowthwarps.util.WarpUtil;
import org.lushplugins.regrowthwarps.warp.Warp;

import java.util.List;

@SuppressWarnings("unused")
@CustomGui(title = "Settings")
public class WarpSettingsGui {
    private final WarpTypeConfig config;

    public WarpSettingsGui(WarpTypeConfig config) {
        this.config = config;
    }

    @SlotActionProvider('l')
    public void locationAction(@Provided Warp warp, SlotContext context) {
        Location location = context.gui().actor().player().getLocation();
        warp.location(location);

        context.gui().refresh(context.slot());
    }

    @SlotActionProvider('n')
    public void displayNameAction(@Provided Warp warp, SlotContext context) {
        context.gui().close();

        context.gui().actor().player().showDialog(Dialogs.shortTextInput(
            config.name() + "Settings",
            "Update display name:",
            (response, audience, input) -> {
                if (input != null && !input.isBlank()) {
                    String oldName = warp.displayName();
                    warp.displayName(input).thenAccept((success) -> {
                        if (audience instanceof Player player) {
                            if (success) {
                                RegrowthWarps.getInstance().getConfigManager().sendMessage(player, "change-name", str -> str
                                    .replace("%old_name%", oldName)
                                    .replace("%new_name%", input));
                            } else {
                                RegrowthWarps.getInstance().getConfigManager().sendMessage(player, "warp-name-taken", str -> str
                                    .replace("%warp%", input));
                            }

                            config.settingsGui()
                                .prepare()
                                .provide(warp)
                                .open(player);
                        }
                    });
                } else {
                    if (audience instanceof Player player) {
                        config.settingsGui()
                            .prepare()
                            .provide(warp)
                            .open(player);
                    }
                }
            }));
    }

    @SlotActionProvider('d')
    public void descriptionAction(@Provided Warp warp, SlotContext context) {
        context.gui().close();

        context.gui().actor().player().showDialog(Dialogs.longTextInput(
            config.name() + "Settings",
            "Update description:",
            (response, audience, input) -> {
                if (input != null && !input.isBlank()) {
                    warp.description(input
                        .replaceAll("[\\r\\n\\u2028\\u2029]+", " "));
                }

                if (audience instanceof Player player) {
                    config.settingsGui()
                        .prepare()
                        .provide(warp)
                        .open(player);
                }
            }));
    }

    @SlotIconProvider('i')
    public ItemStack iconIcon(@Provided Warp warp) {
        DisplayItemStack icon = warp.icon();
        return icon != null ? icon.asItemStack() : DisplayItemStack.builder()
            .setType(Material.ITEM_FRAME)
            .build()
            .asItemStack();
    }

    @SlotActionProvider('i')
    public void iconAction(@Provided Warp warp, SlotContext context, InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (cursor.isEmpty()) {
            return;
        }

        warp.icon(DisplayItemStack.builder(cursor)
            .setAmount(1)
            .build());

        context.gui().refresh(context.slot());
    }

    @SlotActionProvider('c')
    public void categoryAction(@Provided Warp warp, SlotContext context, InventoryClickEvent event) {
        if (event.isShiftClick()) {
            warp.category(null);
        } else {
            List<String> categories = RegrowthWarps.getInstance().getConfigManager().categories();
            String newCategory = Lists.findAdjacentValue(categories, warp.category(), event.isLeftClick());
            warp.category(newCategory);
        }

        context.gui().refresh(context.slot());
    }

    @SlotIconProvider('a')
    public ItemStack accessIcon(@Provided Warp warp) {
        return DisplayItemStack.builder()
            .setType(warp.visibility() == Warp.Visibility.PUBLIC ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE)
            .build()
            .asItemStack();
    }

    @SlotActionProvider('a')
    public void accessAction(@Provided Warp warp, SlotContext context) {
        warp.visibility(warp.visibility() == Warp.Visibility.PUBLIC ? Warp.Visibility.PRIVATE : Warp.Visibility.PUBLIC)
            .thenAccept((success) -> {
                if (success) {
                    context.gui().refresh(context.slot());
                } else {
                    RegrowthWarps.getInstance().getConfigManager().sendMessage(context.gui().actor().player(), "warp-name-taken", str -> str
                        .replace("%warp%", warp.name()));
                }
            });
    }

    @SlotActionProvider('r')
    public void returnAction(GuiActor actor) {
        config.gui().open(actor.player());
    }

    @GuiEventListener
    public void onSlotRefresh(GuiSlotRefreshEvent event, @Provided Warp warp) {
        Gui gui = event.gui();
        int slot = event.slot().rawSlot();
        Inventory inventory = gui.inventory();

        ItemStack item = inventory.getItem(slot);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    meta.displayName(Components.parse(meta.displayName(), str ->
                        WarpUtil.parsePlaceholders(warp, str)));
                }

                if (meta.hasLore()) {
                    meta.lore(WarpUtil.parseDescriptionPlaceholder(warp,  meta.lore().stream()
                        .map(line -> MiniMessage.miniMessage().serialize(line))
                        .map(line -> WarpUtil.parsePlaceholders(warp, line))
                        .toList())
                        .stream()
                        .map(line -> MiniMessage.miniMessage().deserialize(line))
                        .toList());
                }

                item.setItemMeta(meta);
            }
        }

        inventory.setItem(slot, item);
    }
}
