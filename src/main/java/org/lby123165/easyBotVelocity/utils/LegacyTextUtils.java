package org.lby123165.easyBotVelocity.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LegacyTextUtils {
    public static Component toComponent(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }
}
