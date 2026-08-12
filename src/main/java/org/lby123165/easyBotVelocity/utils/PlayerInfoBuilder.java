package org.lby123165.easyBotVelocity.utils;

import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.proxy.Player;

import java.util.UUID;

public class PlayerInfoBuilder {
    public static PlayerInfoWithRaw build(Player proxyPlayer){
        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(GeyserUtils.getNameByPlayer(proxyPlayer));
        info.setNameRaw(GeyserUtils.getNameRawByPlayer(proxyPlayer));
        info.setUuid(GeyserUtils.getUuid(proxyPlayer.getUniqueId()).toString());
        String ip = "127.0.0.1";
        if(proxyPlayer.getRemoteAddress() != null){
            ip = proxyPlayer.getRemoteAddress().getAddress().getHostAddress();
        }
        info.setIp(ip);
        return info;
    }

    public static PlayerInfoWithRaw build(String name, UUID uuid){
        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(GeyserUtils.getName(name, uuid));
        info.setNameRaw(GeyserUtils.getNameRaw(name, uuid));
        info.setUuid(GeyserUtils.getUuid(uuid).toString());
        return info;
    }
}
