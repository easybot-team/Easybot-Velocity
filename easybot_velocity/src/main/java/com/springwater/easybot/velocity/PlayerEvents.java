package com.springwater.easybot.velocity;

import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 玩家事件监听器
 */
public class PlayerEvents {
    
    private final EasyBotVelocity plugin;
    
    public PlayerEvents(EasyBotVelocity plugin) {
        this.plugin = plugin;
    }
    
    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        
        plugin.getLogger().info("玩家 " + player.getUsername() + " 正在登录，上报玩家信息并检查绑定状态...");
        
        try {
            // 始终调用login方法上报玩家信息并获取登录结果
            // EasyBot主程序会根据自己的强制绑定设置返回相应结果
            com.springwater.easybot.bridge.packet.PlayerLoginResultPacket loginResult = plugin.getBridgeClient().login(
                player.getUsername(),
                player.getUniqueId().toString(),
                player.getRemoteAddress().getAddress().getHostAddress()
            );
            
            plugin.getLogger().info("玩家 " + player.getUsername() + " 登录结果: " + 
                (loginResult.getKick() == null || !loginResult.getKick() ? "成功" : "失败 - " + loginResult.getKickMessage()));
            
            // 检查EasyBot主程序是否要求踢出玩家（基于主程序的强制绑定设置）
            if (loginResult.getKick() != null && loginResult.getKick()) {
                // EasyBot主程序要求踢出玩家，拒绝登录
                String kickMessage;
                
                // 如果EasyBot主程序返回了踢出消息，使用返回的消息
                if (loginResult.getKickMessage() != null && !loginResult.getKickMessage().isEmpty()) {
                    kickMessage = loginResult.getKickMessage();
                } else {
                    // 否则使用本地配置的消息
                    kickMessage = plugin.getConfigManager().getForceBindKickMessage();
                }
                
                // 可以在这里添加额外的消息处理逻辑
                // 例如：根据不同情况显示不同消息
                kickMessage = processKickMessage(kickMessage, player.getUsername());
                
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text(kickMessage)));
                plugin.getLogger().info("玩家 " + player.getUsername() + " 被EasyBot主程序拒绝登录（可能未绑定）");
                return;
            }
            
            plugin.getLogger().info("玩家 " + player.getUsername() + " 通过EasyBot主程序验证，允许登录");
            
        } catch (Exception e) {
            plugin.getLogger().error("检查玩家绑定状态时出错: " + e.getMessage(), e);
            
            // 如果检查失败，根据本地配置决定是否允许登录
            if (plugin.getConfigManager().isDenyOnBindCheckFail()) {
                String kickMessage = "绑定状态检查失败，请稍后重试";
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text(kickMessage)));
                plugin.getLogger().info("玩家 " + player.getUsername() + " 绑定检查异常，已拒绝登录");
                return;
            }
            
            plugin.getLogger().info("玩家 " + player.getUsername() + " 绑定检查失败但允许登录");
        }
    }
    
    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        
        plugin.getLogger().info("玩家 " + player.getUsername() + " 已成功加入服务器，确保玩家信息已上报...");
        
        // 创建玩家信息
        PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
        playerInfo.setName(player.getUsername());
        playerInfo.setUuid(player.getUniqueId().toString());
        playerInfo.setIp(player.getRemoteAddress().getAddress().getHostAddress());
        playerInfo.setNameRaw(player.getUsername()); // 设置原始名称
        
        // 再次确保玩家信息已上报到主程序（login方法中已经调用过一次）
        // 这里再次调用是为了确保主程序的在线玩家列表是最新的
        plugin.getBridgeClient().reportPlayer(
            player.getUsername(),
            player.getUniqueId().toString(),
            player.getRemoteAddress().getAddress().getHostAddress()
        );
        
        plugin.getLogger().info("玩家 " + player.getUsername() + " 在线状态已确认上报到主程序");
        
        // 如果启用了加入/离开消息同步
        if (plugin.getConfigManager().isSyncJoinLeave()) {
            plugin.getBridgeClient().syncEnterExit(playerInfo, true);
        }
    }
    
    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        Player player = event.getPlayer();
        
        // 如果启用了加入/离开消息同步
        if (plugin.getConfigManager().isSyncJoinLeave()) {
            PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
            playerInfo.setName(player.getUsername());
            playerInfo.setUuid(player.getUniqueId().toString());
            playerInfo.setIp(player.getRemoteAddress().getAddress().getHostAddress());
            playerInfo.setNameRaw(player.getUsername()); // 设置原始名称
            
            plugin.getBridgeClient().syncEnterExit(playerInfo, false);
        }
    }
    
    /**
     * 处理踢出消息，优先使用主程序传来的消息
     */
    private String processKickMessage(String originalMessage, String playerName) {
        // 优先使用EasyBot主程序传来的消息
        // 主程序会根据自己的配置返回相应的踢出消息
        if (originalMessage != null && !originalMessage.isEmpty()) {
            plugin.getLogger().info("使用主程序传来的踢出消息: " + originalMessage);
            return originalMessage;
        }
        
        // 如果主程序没有返回消息，使用本地配置的备用消息
        String fallbackMessage = plugin.getConfigManager().getForceBindKickMessage();
        plugin.getLogger().info("主程序未返回踢出消息，使用本地备用消息: " + fallbackMessage);
        return fallbackMessage;
    }
}
