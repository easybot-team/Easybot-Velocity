package com.springwater.easybot.velocity;

import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

/**
 * 消息事件监听器
 */
public class MessageEvents {
    
    private final EasyBotVelocity plugin;
    
    public MessageEvents(EasyBotVelocity plugin) {
        this.plugin = plugin;
    }
    
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        // 如果启用了聊天同步
        if (plugin.getConfigManager().isSyncChat()) {
            Player player = event.getPlayer();
            String message = event.getMessage();
            
        // 创建玩家信息
        PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
        playerInfo.setName(player.getUsername());
        playerInfo.setUuid(player.getUniqueId().toString());
        playerInfo.setIp(player.getRemoteAddress().getAddress().getHostAddress());
        playerInfo.setNameRaw(player.getUsername()); // 设置原始名称
            
            // 发送消息到Bridge
            plugin.getBridgeClient().syncMessage(playerInfo, message, false);
        }
    }
}