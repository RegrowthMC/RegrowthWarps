package org.lushplugins.regrowthwarps.util;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public class Dialogs {

    public static Dialog textInput(String title, String inputHeader, int lines, TriConsumer<DialogResponseView, Audience, String> callback) {
        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(Component.text(title))
                .inputs(List.of(
                    DialogInput.text("input", Component.text(inputHeader))
                        .multiline(TextDialogInput.MultilineOptions.create(lines, (lines * 11) + 9))
                        .maxLength(lines * 50)
                        .build())
                )
                .build())
            .type(DialogType.notice(
                ActionButton.create(
                    Component.text("Confirm"),
                    null,
                    100,
                    DialogAction.customClick(
                        (view, audience) -> {
                            String input = view.getText("input");
                            callback.accept(view, audience, input);
                        },
                        ClickCallback.Options.builder()
                            .uses(1)
                            .lifetime(ClickCallback.DEFAULT_LIFETIME)
                            .build()
                    )
                )
            )));
    }

    public static Dialog shortTextInput(String title, String inputHeader, TriConsumer<DialogResponseView, Audience, String> callback) {
        return textInput(title, inputHeader, 1, callback);
    }

    public static Dialog longTextInput(String title, String inputHeader, TriConsumer<DialogResponseView, Audience, String> callback) {
        return textInput(title, inputHeader, 5, callback);
    }
}
