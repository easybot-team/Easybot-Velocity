package com.springwater.easybot.velocity;

import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EasyBotVelocityBehavior implements BridgeBehavior {
    
    private final EasyBotVelocity plugin;
    
    public EasyBotVelocityBehavior(EasyBotVelocity plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String runCommand(String playerName, String command, boolean enablePapi) {
        plugin.getLogger().info("收到命令执行请求: " + command + " (玩家: " + playerName + ")");
        
        // 查找玩家
        Optional<Player> playerOpt = plugin.getServer().getPlayer(playerName);
        if (!playerOpt.isPresent()) {
            return "玩家 " + playerName + " 不在线";
        }
        
        Player player = playerOpt.get();
        
        // 处理占位符
        String processedCommand = command
                .replace("{player}", playerName)
                .replace("{player_name}", playerName);
        
        try {
            // 移除命令前缀
            if (processedCommand.startsWith("/")) {
                processedCommand = processedCommand.substring(1);
            }
            
            String[] parts = processedCommand.split(" ");
            String commandName = parts[0].toLowerCase();
            
            // 处理Velocity支持的命令
            switch (commandName) {
                case "send":
                    if (parts.length >= 2) {
                        String serverName = parts[1];
                        Optional<com.velocitypowered.api.proxy.server.RegisteredServer> server = 
                            plugin.getServer().getServer(serverName);
                        if (server.isPresent()) {
                            player.createConnectionRequest(server.get()).fireAndForget();
                            return "已将玩家 " + playerName + " 传送到服务器 " + serverName;
                        } else {
                            return "服务器 " + serverName + " 不存在";
                        }
                    }
                    break;
                    
                case "kick":
                    String reason = parts.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length)) : "被踢出服务器";
                    player.disconnect(Component.text(reason));
                    return "已踢出玩家 " + playerName + "，原因: " + reason;
                    
                case "server":
                    if (parts.length >= 2) {
                        String serverName = parts[1];
                        Optional<com.velocitypowered.api.proxy.server.RegisteredServer> server = 
                            plugin.getServer().getServer(serverName);
                        if (server.isPresent()) {
                            player.createConnectionRequest(server.get()).fireAndForget();
                            return "已将玩家 " + playerName + " 连接到服务器 " + serverName;
                        } else {
                            return "服务器 " + serverName + " 不存在";
                        }
                    }
                    break;
                    
                default:
                    // 对于其他命令，使用控制台执行
                    plugin.getServer().getCommandManager().executeAsync(
                        plugin.getServer().getConsoleCommandSource(), 
                        processedCommand
                    );
                    return "命令已通过控制台执行: " + processedCommand;
            }
            
            return "命令执行完成: " + processedCommand;
            
        } catch (Exception e) {
            plugin.getLogger().error("执行命令时出错: " + e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }
    
    @Override
    public String papiQuery(String playerName, String query) {
        // Velocity不支持PlaceholderAPI，返回原始查询
        plugin.getLogger().info("收到PAPI查询请求: " + query + " (玩家: " + playerName + ")");
        return query;
    }
    
    @Override
    public ServerInfo getInfo() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerName(plugin.getConfigManager().getServerName());
        serverInfo.setServerVersion("Velocity " + plugin.getServer().getVersion().getVersion());
        serverInfo.setPluginVersion("1.0.0");
        serverInfo.setPapiSupported(false); // Velocity不支持PlaceholderAPI
        serverInfo.setCommandSupported(true);
        serverInfo.setHasGeyser(false); // 需要检测Geyser插件
        serverInfo.setOnlineMode(plugin.getServer().getConfiguration().isOnlineMode());
        return serverInfo;
    }
    
    @Override
    public void SyncToChat(String message) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        plugin.getServer().getAllPlayers().forEach(player -> player.sendMessage(component));
        plugin.getLogger().info("同步消息到聊天: " + message);
    }
    
    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        plugin.getLogger().info("玩家绑定成功: " + playerName + " -> " + accountName + " (" + accountId + ")");
        
        // 执行绑定成功命令
        if (plugin.getConfigManager().isEnableSuccessEvent()) {
            List<String> commands = plugin.getConfigManager().getBindSuccessCommands();
            for (String command : commands) {
                String processedCommand = command
                        .replace("{player}", playerName)
                        .replace("{account}", accountId)
                        .replace("{name}", accountName);
                
                // 移除命令前缀
                if (processedCommand.startsWith("/")) {
                    processedCommand = processedCommand.substring(1);
                }
                
                // 执行Velocity命令
                plugin.getServer().getCommandManager().executeAsync(
                    plugin.getServer().getConsoleCommandSource(), 
                    processedCommand
                );
                plugin.getLogger().info("执行绑定成功命令: " + processedCommand);
            }
        }
    }
    
    @Override
    public void KickPlayer(String playerName, String kickMessage) {
        Optional<Player> playerOpt = plugin.getServer().getPlayer(playerName);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            
            // 使用自定义的解绑踢出消息
            String customKickMessage = "你已在聊群或管理后台进行解绑，如需进入请重新绑定";
            
            player.disconnect(Component.text(customKickMessage));
            plugin.getLogger().info("收到解绑通知，踢出玩家: " + playerName + "，使用自定义消息: " + customKickMessage);
            plugin.getLogger().info("原始踢出消息: " + kickMessage);
        } else {
            plugin.getLogger().warn("尝试踢出不存在的玩家: " + playerName);
        }
    }
    
    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        StringBuilder message = new StringBuilder();
        for (Segment segment : segments) {
            message.append(segment.toString());
        }
        
        // 如果有额外文本，添加到消息末尾
        if (text != null && !text.isEmpty()) {
            message.append(" ").append(text);
        }
        
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message.toString());
        plugin.getServer().getAllPlayers().forEach(player -> player.sendMessage(component));
        plugin.getLogger().info("同步扩展消息到聊天: " + message.toString());
    }
    
    @Override
    public List<PlayerInfo> getPlayerList() {
        List<PlayerInfo> players = new ArrayList<>();
        for (Player player : plugin.getServer().getAllPlayers()) {
            PlayerInfo info = new PlayerInfo();
            info.setPlayerName(player.getUsername());
            info.setPlayerUuid(player.getUniqueId().toString());
            
            // 获取玩家IP
            if (player.getRemoteAddress() != null) {
                info.setIp(player.getRemoteAddress().getAddress().getHostAddress());
            }
            
            // Velocity环境下的其他信息
            info.setSkinUrl(""); // Velocity无法直接获取皮肤URL
            info.setBedrock(false); // 需要检测Geyser或Floodgate
            
            players.add(info);
        }
        return players;
    }
}