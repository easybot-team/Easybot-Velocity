package org.lby123165.easyBotVelocity.utils;

import com.google.gson.*;
import com.springwater.easybot.bridge.ClientProfile;
import com.springwater.easybot.bridge.model.PlayerSkin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import net.skinsrestorer.api.PropertyUtils;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import org.jetbrains.annotations.Nullable;
import org.lby123165.easyBotVelocity.EasyBotVelocity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class SkinUtils {

    private static boolean hasSkinsRestorer() {
        try {
            Class.forName("net.skinsrestorer.api.SkinsRestorerProvider");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    private static boolean hasPaperSkinApi() {
        return true;
    }
    public static void handleSkinsRestorerCompatibility() {
        if (hasSkinsRestorer()) {
            EasyBotVelocity.getInstance().getLogger().info("\u001B[32m※ 检测到SkinsRestorer插件,玩家皮肤将通过该插件获取！\u001B[0m");
            ClientProfile.setHasSkinsRestorer(true);
        } else if (hasPaperSkinApi()) {
            EasyBotVelocity.getInstance().getLogger().info("\u001B[32m※ 检测到官方皮肤API,玩家皮肤将通过该接口获取(正版)！\u001B[0m");
            ClientProfile.setHasPaperSkinApi(true);
        } else if (!ClientProfile.isOnlineMode()) {
            EasyBotVelocity.getInstance().getLogger().info("\u001B[31m※ 当前服务器为离线服务器,且你并未安装\u001B[32mSkinsRestorer\u001B[31m插件,这会导致获取玩家皮肤不正确！\u001B[0m");
        }
    }

    public static String getSkin(Player player) {
        try {
            if (ClientProfile.isHasSkinsRestorer()) {
                SkinsRestorer api = SkinsRestorerProvider.get();
                Optional<SkinProperty> prop = api.getPlayerStorage().getSkinForPlayer(GeyserUtils.getUuid(player.getUniqueId()), GeyserUtils.getNameByPlayer(player));
                if (prop.isPresent()) {
                    return PropertyUtils.getSkinProfileData(prop.get()).getTextures().getSKIN().getUrl();
                }
            }
            if (ClientProfile.isHasPaperSkinApi()) {
                List<GameProfile.Property> props = player.getGameProfile().getProperties();
                for (GameProfile.Property it : props) {
                    if (it.getName().equalsIgnoreCase("textures")) {
                        String value = new String(Base64.getDecoder().decode(it.getValue()), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(value);
                        String skin = jsonElement.getAsJsonObject().getAsJsonObject("textures")
                                .getAsJsonObject("SKIN")
                                .getAsJsonObject("url").getAsString();
                        if (!skin.isBlank()) return skin;
                    }
                }
            }
            return "https://mc-heads.net/skin/" + player.getUniqueId();
        } catch (Exception ignored) {
            return "";
        }
    }

    public static @Nullable PlayerSkin getSkinOrNull(Player player) {
        try {
            if (ClientProfile.isHasSkinsRestorer()) {
                PlayerSkin skinWithSkinsRestorer = new PlayerSkin();
                String skin = null;
                String cape = null;
                SkinsRestorer api = SkinsRestorerProvider.get();
                Optional<SkinProperty> prop = api.getPlayerStorage().getSkinForPlayer(GeyserUtils.getUuid(player.getUniqueId()), GeyserUtils.getNameByPlayer(player));
                if (prop.isPresent()) {
                    skin = PropertyUtils.getSkinProfileData(prop.get()).getTextures().getSKIN().getUrl();
                    cape = PropertyUtils.getSkinProfileData(prop.get()).getTextures().getCAPE().getUrl();
                }
                if (skin != null && skin.isBlank()) {
                    skinWithSkinsRestorer.setSkinUrl(skin);
                }
                if (cape != null && cape.isBlank()) {
                    skinWithSkinsRestorer.setCapeUrl(cape);
                }
                if (skinWithSkinsRestorer.getSkinUrl() == null || skinWithSkinsRestorer.getSkinUrl().isBlank()) return null;
                return skinWithSkinsRestorer;
            } else if (ClientProfile.isHasPaperSkinApi()) {
                PlayerSkin skinWithPaperSkinApi = new PlayerSkin();
                List<GameProfile.Property> props = player.getGameProfile().getProperties();
                for (GameProfile.Property it : props) {
                    if (it.getName().equalsIgnoreCase("textures")) {
                        String value = new String(Base64.getDecoder().decode(it.getValue()), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(value);
                        JsonObject textures = jsonElement.getAsJsonObject().getAsJsonObject("textures");
                        String skin = textures.getAsJsonObject("SKIN").getAsJsonObject("url").getAsString();
                        String cape = textures.getAsJsonObject("CAPE").getAsJsonObject("url").getAsString();
                        if (!skin.isBlank()) skinWithPaperSkinApi.setSkinUrl(skin);
                        if (!cape.isBlank()) skinWithPaperSkinApi.setCapeUrl(cape);
                    }
                }
                if (skinWithPaperSkinApi.getSkinUrl() == null || skinWithPaperSkinApi.getSkinUrl().isBlank()) return null;
                return skinWithPaperSkinApi;
            }
        } catch (Exception ex) {
            EasyBotVelocity.getInstance().getLogger().error("处理玩家皮肤信息遇到异常! " + ex);
        }
        return null;
    }
}