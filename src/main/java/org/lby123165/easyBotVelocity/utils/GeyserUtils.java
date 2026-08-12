package org.lby123165.easyBotVelocity.utils;

import com.springwater.easybot.bridge.ClientProfile;
import com.velocitypowered.api.proxy.Player;
import org.geysermc.api.Geyser;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.Nullable;
import org.lby123165.easyBotVelocity.EasyBotVelocity;

import java.util.UUID;

public class GeyserUtils {
    private static boolean toggle() {
        return EasyBotVelocity.getInstance().getConfig().geyser.ignorePrefix;
    }

    public static void handleGeyserCompatibility(EasyBotVelocity server) {
        ClientProfile.setHasGeyser(hasGeyserMc());
        ClientProfile.setHasFloodgate(hasFloodgate());
        if (ClientProfile.isHasGeyser()) {
            server.getLogger().info("\u001B[32m※ 检测到GeyserMC插件\u001B[0m");
        }
        if (ClientProfile.isHasFloodgate()) {
            server.getLogger().info("\u001B[32m※ 检测到Floodgate插件\u001B[0m");

            String userNamePrefix = FloodgateApi.getInstance().getPlayerPrefix();
            if (userNamePrefix != null) {
                server.getLogger().info("\u001B[32m - 基岩版用户前缀: " + userNamePrefix + "\u001B[0m");
                if (server.getConfig().geyser.ignorePrefix) {
                    server.getLogger().info("\u001B[32m - 注意: EasyBot会在处理数据时忽略玩家前缀: " + userNamePrefix + "MiuxuE" + " -> " + "MiuxuE" + "\u001B[0m");
                } else {
                    server.getLogger().info("\u001B[32m - 您可以设置为忽略前缀, 忽略前缀后玩家将不再有前缀,例:" + userNamePrefix + "MiuxuE" + " -> " + userNamePrefix + "MiuxuE" + "\u001B[0m");
                    server.getLogger().info("\u001B[32m - 请参考配置文件: geyser.ignore_prefix\u001B[0m");
                }
            }
        }
    }

    private static boolean hasGeyserMc() {
        try {
            Class.forName("org.geysermc.api.BuildData");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasFloodgate() {
        try {
            Class.forName("org.geysermc.floodgate.api.player.FloodgatePlayer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String getNameRawByPlayer(Player player) {
        if (ClientProfile.isHasFloodgate() && toggle()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
            if (conn != null) {
                if (conn.isLinked()) {
                    return conn.getLinkedPlayer().getJavaUsername();
                }
                return conn.getJavaUsername();
            }
        }
        return player.getUsername();
    }

    public static String getNameRaw(String name, UUID player) {
        if (ClientProfile.isHasFloodgate() && toggle()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player);
            if (conn != null) {
                if (conn.isLinked()) {
                    return conn.getLinkedPlayer().getJavaUsername();
                }
                return conn.getJavaUsername();
            }
        }
        return name;
    }

    public static String getNameByPlayer(Player player) {
        if (ClientProfile.isHasFloodgate()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
            if (conn != null) {
                if (conn.isLinked()) {
                    return conn.getLinkedPlayer().getJavaUsername();
                }
                return toggle() ? conn.getUsername() : conn.getJavaUsername();
            }
        }
        return player.getUsername();
    }

    public static @Nullable String getName(String name, UUID player) {
        if (ClientProfile.isHasFloodgate()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player);
            if (conn != null) {
                if (conn.isLinked()) {
                    return conn.getLinkedPlayer().getJavaUsername();
                }
                return toggle() ? conn.getUsername() : conn.getJavaUsername();
            }
        }
        return name;
    }

    public static UUID getUuid(UUID uuid) {
        if (ClientProfile.isHasFloodgate()) {
            FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(uuid);
            if (conn != null) {
                if (conn.isLinked()) {
                    return conn.getLinkedPlayer().getJavaUniqueId();
                }
                return conn.getJavaUniqueId();
            }
        }
        return uuid;
    }

    public static boolean isBedrock(Player player) {
        if (ClientProfile.isHasGeyser()) {
            if (ClientProfile.isHasFloodgate()) {
                FloodgatePlayer conn = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
                if (conn != null) {
                    return true;
                }
            }
            return Geyser.api().isBedrockPlayer(player.getUniqueId());
        }
        return false;
    }
}