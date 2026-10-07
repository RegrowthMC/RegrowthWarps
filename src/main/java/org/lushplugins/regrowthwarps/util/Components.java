package org.lushplugins.regrowthwarps.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.function.UnaryOperator;

public class Components {

    public static Component parse(Component component, UnaryOperator<String> operator) {
        String rawComponent = MiniMessage.miniMessage().serialize(component);
        String parsedComponent = operator.apply(rawComponent);
        return MiniMessage.miniMessage().deserialize(parsedComponent);
    }
}
