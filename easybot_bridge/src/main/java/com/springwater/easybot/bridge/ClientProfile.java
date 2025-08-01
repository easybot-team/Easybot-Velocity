package com.springwater.easybot.bridge;

import lombok.Getter;
import lombok.Setter;

public class ClientProfile {
    @Getter
    @Setter
    private static String pluginVersion;

    @Getter
    @Setter
    private static String serverDescription;

    @Getter
    @Setter
    private static boolean isCommandSupported;

    @Getter
    @Setter
    private static boolean isPapiSupported;

    @Getter
    @Setter
    private static int syncMessageMode;

    @Getter
    @Setter
    private static int syncMessageMoney;

    @Getter
    @Setter
    private static boolean hasGeyser;

    @Getter
    @Setter
    private static boolean hasFloodgate;

    @Getter
    @Setter
    private static boolean hasBungeeChatApi;

    @Getter
    @Setter
    private static boolean isOnlineMode;

    @Getter
    @Setter
    private static boolean isDebugMode;

    @Getter
    @Setter
    private static boolean hasSkinsRestorer;

    @Getter
    @Setter
    private static boolean hasPaperSkinApi;

    @Getter
    @Setter
    private static boolean hasItemsAdder;

    @Getter
    @Setter
    private static boolean hasQFaces;

}
