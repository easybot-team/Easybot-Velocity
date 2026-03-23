package org.lby123165.easyBotVelocity.utils;

import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.proxy.Player;

public class PlayerInfoBuilder {
    public static PlayerInfoWithRaw build(Player proxyPlayer){
        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(proxyPlayer.getUsername());
        info.setNameRaw(proxyPlayer.getUsername());
        info.setUuid(proxyPlayer.getUniqueId().toString());
        String ip = "127.0.0.1";
        if(proxyPlayer.getRemoteAddress() != null){
            ip = proxyPlayer.getRemoteAddress().getAddress().getHostAddress();
        }
        info.setIp(ip);
        return info;
    }
}
