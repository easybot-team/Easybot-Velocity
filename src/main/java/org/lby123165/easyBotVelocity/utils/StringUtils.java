package org.lby123165.easyBotVelocity.utils;

import org.jetbrains.annotations.Nullable;

public class StringUtils {
    public static String ifEmpty(@Nullable String str, String defaultStr){
        return str == null || str.isEmpty() ? defaultStr : str;
    }
}
