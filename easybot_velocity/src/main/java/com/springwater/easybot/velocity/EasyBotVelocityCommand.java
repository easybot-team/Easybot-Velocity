package com.springwater.easybot.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * EasyBot Velocity 命令处理器
 */
public class EasyBotVelocityCommand implements SimpleCommand {
    
    private final EasyBotVelocity plugin;
    
    public EasyBotVelocityCommand(EasyBotVelocity plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            invocation.source().sendMessage(Component.text("EasyBot Velocity 插件 v1.1.0").color(NamedTextColor.GREEN));
            invocation.source().sendMessage(Component.text("使用 /easybot help 查看帮助").color(NamedTextColor.GRAY));
            return;
        }
        
        String subCommand = invocation.arguments()[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(invocation);
                break;
            case "reload":
                if (invocation.source().hasPermission("easybot.admin")) {
                    plugin.reload();
                    invocation.source().sendMessage(Component.text("EasyBot 配置已重新加载").color(NamedTextColor.GREEN));
                } else {
                    invocation.source().sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED));
                }
                break;
            case "status":
                showStatus(invocation);
                break;
            case "info":
                showInfo(invocation);
                break;
            default:
                invocation.source().sendMessage(Component.text("未知命令。使用 /easybot help 查看帮助").color(NamedTextColor.RED));
                break;
        }
    }
    
    private void showHelp(Invocation invocation) {
        invocation.source().sendMessage(Component.text("=== EasyBot Velocity 帮助 ===").color(NamedTextColor.GOLD));
        invocation.source().sendMessage(Component.text("/easybot - 显示插件信息").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/easybot help - 显示此帮助").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/easybot status - 显示连接状态").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/easybot info - 显示详细信息").color(NamedTextColor.YELLOW));
        if (invocation.source().hasPermission("easybot.admin")) {
            invocation.source().sendMessage(Component.text("/easybot reload - 重新加载配置").color(NamedTextColor.YELLOW));
        }
    }
    
    private void showStatus(Invocation invocation) {
        boolean connected = plugin.getBridgeClient() != null && plugin.getBridgeClient().isReady();
        invocation.source().sendMessage(Component.text("Bridge 连接状态: " + (connected ? "已连接" : "未连接"))
                .color(connected ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        if (connected) {
            invocation.source().sendMessage(Component.text("服务器名称: " + plugin.getConfigManager().getServerName()).color(NamedTextColor.GRAY));
            invocation.source().sendMessage(Component.text("在线玩家: " + plugin.getServer().getPlayerCount()).color(NamedTextColor.GRAY));
        }
    }
    
    private void showInfo(Invocation invocation) {
        invocation.source().sendMessage(Component.text("=== EasyBot Velocity 信息 ===").color(NamedTextColor.GOLD));
        invocation.source().sendMessage(Component.text("插件版本: 1.1.0").color(NamedTextColor.GRAY));
        invocation.source().sendMessage(Component.text("服务器名称: " + plugin.getConfigManager().getServerName()).color(NamedTextColor.GRAY));
        invocation.source().sendMessage(Component.text("服务URL: " + plugin.getConfigManager().getServiceUrl()).color(NamedTextColor.GRAY));
        invocation.source().sendMessage(Component.text("聊天同步: " + (plugin.getConfigManager().isSyncChat() ? "启用" : "禁用")).color(NamedTextColor.GRAY));
        invocation.source().sendMessage(Component.text("进出同步: " + (plugin.getConfigManager().isSyncJoinLeave() ? "启用" : "禁用")).color(NamedTextColor.GRAY));
        invocation.source().sendMessage(Component.text("调试模式: " + (plugin.getConfigManager().isDebug() ? "启用" : "禁用")).color(NamedTextColor.GRAY));
        
        boolean connected = plugin.getBridgeClient() != null && plugin.getBridgeClient().isReady();
        invocation.source().sendMessage(Component.text("连接状态: " + (connected ? "已连接" : "未连接"))
                .color(connected ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        boolean updateCheckerRunning = false; // 暂时禁用更新检查器状态
        invocation.source().sendMessage(Component.text("更新检查器: " + (updateCheckerRunning ? "运行中" : "已停止"))
                .color(updateCheckerRunning ? NamedTextColor.GREEN : NamedTextColor.RED));
    }
    
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("easybot.command");
    }
}