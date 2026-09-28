package org.lushplugins.regrowthwarps.gui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lushplugins.guihandler.annotation.CustomGui;
import org.lushplugins.guihandler.annotation.Provided;
import org.lushplugins.guihandler.annotation.SlotActionProvider;
import org.lushplugins.guihandler.annotation.SlotIconProvider;
import org.lushplugins.guihandler.gui.GuiActor;
import org.lushplugins.guihandler.slot.SlotContext;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.config.WarpTypeConfig;
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

        context.gui().actor().player().showDialog(Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text(config.name() + " Settings"))
                .inputs(List.of(
                    DialogInput.text("display_name", Component.text("Update display name:")).build())
                )
                .build())
            .type(DialogType.notice(
                ActionButton.create(
                    Component.text("Confirm"),
                    null,
                    100,
                    DialogAction.customClick(
                        (view, audience) -> {
                            String displayName = view.getText("display_name");
                            if (displayName != null && !displayName.isBlank()) {
                                warp.displayName(displayName);

                                if (audience instanceof Player player) {
                                    config.settingsGui()
                                        .prepare()
                                        .provide(warp)
                                        .open(player);
                                }
                            }
                        },
                        ClickCallback.Options.builder()
                            .uses(1)
                            .lifetime(ClickCallback.DEFAULT_LIFETIME)
                            .build()
                    )
                )
            ))));
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
            .build());

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
        warp.visibility(warp.visibility() == Warp.Visibility.PUBLIC ? Warp.Visibility.PRIVATE : Warp.Visibility.PUBLIC);

        context.gui().refresh(context.slot());
    }

    @SlotActionProvider('x')
    public void returnAction(GuiActor actor) {
        config.gui().open(actor.player());
    }
}
