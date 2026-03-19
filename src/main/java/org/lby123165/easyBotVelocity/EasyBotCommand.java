package org.lby123165.easyBotVelocity;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.GetBindInfoResultPacket;
import com.springwater.easybot.bridge.packet.GetInstalledPluginResultPacket;
import com.springwater.easybot.bridge.packet.GetNewVersionResultPacket;
import com.springwater.easybot.bridge.packet.GetSocialAccountResultPacket;
import com.springwater.easybot.bridge.model.PluginInfo;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EasyBotCommand implements SimpleCommand {

    private final BridgeClient client;
    private final Configuration config;
    private final Path dataDirectory;

    public EasyBotCommand(BridgeClient client, Configuration config, Path dataDirectory) {
        this.client = client;
        this.config = config;
        this.dataDirectory = dataDirectory;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sendHelp(source);
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                handleReload(source);
                break;
            case "bind":
                handleBind(source);
                break;
            case "status":
                handleStatus(source);
                break;
            case "newversion":
                handleNewVersion(source);
                break;
            case "bindinfo":
                handleBindInfo(source, args);
                break;
            case "social":
                handleSocial(source, args);
                break;
            case "plugins":
                handlePlugins(source);
                break;
            default:
                source.sendMessage(Component.text("未知指令，请输入 /ez help 查看帮助", NamedTextColor.RED));
                break;
        }
    }

    private void handleReload(CommandSource source) {
        if (!source.hasPermission("easybot.admin")) {
            source.sendMessage(Component.text("你没有权限执行此命令!", NamedTextColor.RED));
            return;
        }

        try {
            config.reload(dataDirectory);
            // 如果 URL 或 Token 变了，可能需要重连 BridgeClient
            // 这里简单处理：重置 URL 并尝试重连
            client.resetUrl(config.ws);
            client.setToken(config.token);

            source.sendMessage(Component.text("EasyBot 配置已重载!", NamedTextColor.GREEN));
        } catch (Exception e) {
            source.sendMessage(Component.text("重载失败: " + e.getMessage(), NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void handleBind(CommandSource source) {
        if (!config.command.allowBind) {
            source.sendMessage(Component.text("绑定功能未开启。", NamedTextColor.RED));
            return;
        }

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("只有玩家才能执行绑定!", NamedTextColor.RED));
            return;
        }

        Player player = (Player) source;
        String playerName = player.getUsername();

        // 异步调用 Bridge 获取绑定状态
        CompletableFuture.runAsync(() -> {
            try {
                // 先查询是否已绑定 (假设 BridgeClient 有相关 API，如果没有可以用 startBind 尝试)
                // 这里直接调用 startBind，让后端处理逻辑
                // 注意：BridgeClient.startBind 返回的是 StartBindResultPacket
                // 由于我们现在是引用 jar，可能无法直接访问所有 Packet 类的方法
                // 但 bridge.client.startBind(playerName) 是存在的

                var result = client.startBind(playerName);

                // 处理返回结果
                // result 是 StartBindResultPacket 类型
                // 替换 config.message.bindStart 中的变量
                String msg = config.message.bindStart
                        .replace("#code", result.getCode())
                        .replace("#time", result.getTime());

                player.sendMessage(Component.text(msg, NamedTextColor.GREEN));

            } catch (Exception e) {
                player.sendMessage(Component.text("绑定请求失败: " + e.getMessage(), NamedTextColor.RED));
                if (config.debug) e.printStackTrace();
            }
        });
    }

    private void handleStatus(CommandSource source) {
        if (!source.hasPermission("easybot.admin")) return;

        boolean ready = client.isReady();

        source.sendMessage(Component.text("EasyBot 状态: ", NamedTextColor.GRAY)
                .append(Component.text(ready ? "就绪 (Ready)" : "未就绪", ready ? NamedTextColor.GREEN : NamedTextColor.RED)));
        source.sendMessage(Component.text("WebSocket URL: " + config.ws, NamedTextColor.GRAY));
    }

    private void handleNewVersion(CommandSource source) {
        if (!source.hasPermission("easybot.admin")) {
            source.sendMessage(Component.text("你没有权限执行此命令!", NamedTextColor.RED));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                GetNewVersionResultPacket result = client.getNewVersion();
                source.sendMessage(Component.text("最新版本信息:", NamedTextColor.AQUA));
                source.sendMessage(Component.text("版本: " + result.getVersionName(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("下载地址: " + result.getDownloadUrl(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("发布时间: " + result.getPublishTime(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("更新日志: " + result.getUpdateLog(), NamedTextColor.GRAY));
            } catch (Exception e) {
                source.sendMessage(Component.text("查询失败: " + e.getMessage(), NamedTextColor.RED));
                if (config.debug) e.printStackTrace();
            }
        });
    }

    private void handleBindInfo(CommandSource source, String[] args) {
        if (!source.hasPermission("easybot.admin")) {
            source.sendMessage(Component.text("你没有权限执行此命令!", NamedTextColor.RED));
            return;
        }
        if (args.length < 2) {
            source.sendMessage(Component.text("用法: /ez bindinfo <player>", NamedTextColor.RED));
            return;
        }
        String playerName = args[1];

        CompletableFuture.runAsync(() -> {
            try {
                GetBindInfoResultPacket result = client.getBindInfo(playerName);
                source.sendMessage(Component.text("绑定信息: " + playerName, NamedTextColor.AQUA));
                source.sendMessage(Component.text("平台: " + result.getPlatform(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("账号ID: " + result.getId(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("账号名: " + result.getName(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("绑定名: " + result.getBindName(), NamedTextColor.GRAY));
            } catch (Exception e) {
                source.sendMessage(Component.text("查询失败: " + e.getMessage(), NamedTextColor.RED));
                if (config.debug) e.printStackTrace();
            }
        });
    }

    private void handleSocial(CommandSource source, String[] args) {
        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("只有玩家才能执行此命令!", NamedTextColor.RED));
            return;
        }
        Player player = (Player) source;
        String playerName = player.getUsername();
        if (args.length >= 2) {
            playerName = args[1];
        }

        String finalPlayerName = playerName;
        CompletableFuture.runAsync(() -> {
            try {
                GetSocialAccountResultPacket result = client.getSocialAccount(finalPlayerName);
                source.sendMessage(Component.text("社交账号: " + finalPlayerName, NamedTextColor.AQUA));
                source.sendMessage(Component.text("平台: " + result.getPlatform(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("账号名: " + result.getName(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("UUID: " + result.getUuid(), NamedTextColor.GRAY));
                source.sendMessage(Component.text("时间: " + result.getTime(), NamedTextColor.GRAY));
            } catch (Exception e) {
                source.sendMessage(Component.text("查询失败: " + e.getMessage(), NamedTextColor.RED));
                if (config.debug) e.printStackTrace();
            }
        });
    }

    private void handlePlugins(CommandSource source) {
        if (!source.hasPermission("easybot.admin")) {
            source.sendMessage(Component.text("你没有权限执行此命令!", NamedTextColor.RED));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                GetInstalledPluginResultPacket result = client.getInstalledPlugin();
                source.sendMessage(Component.text("EasyBot 已安装插件:", NamedTextColor.AQUA));
                if (result.getPlugins() == null || result.getPlugins().isEmpty()) {
                    source.sendMessage(Component.text("(空)", NamedTextColor.GRAY));
                    return;
                }
                for (var entry : result.getPlugins().entrySet()) {
                    String key = entry.getKey();
                    PluginInfo info = entry.getValue();
                    String name = key;
                    String version = "";
                    boolean enabled = false;
                    if (info != null) {
                        enabled = info.isEnabled();
                        if (info.getManifest() != null) {
                            if (info.getManifest().getName() != null) name = info.getManifest().getName();
                            if (info.getManifest().getVersion() != null) version = info.getManifest().getVersion();
                        }
                    }
                    source.sendMessage(Component.text("- " + name + " " + version + (enabled ? " (enabled)" : " (disabled)"), NamedTextColor.GRAY));
                }
            } catch (Exception e) {
                source.sendMessage(Component.text("查询失败: " + e.getMessage(), NamedTextColor.RED));
                if (config.debug) e.printStackTrace();
            }
        });
    }


    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.text("=== EasyBot Velocity 帮助 ===", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/ez bind   - 绑定账号", NamedTextColor.WHITE));
        source.sendMessage(Component.text("/ez social [player] - 查询社交账号", NamedTextColor.WHITE));
        if (source.hasPermission("easybot.admin")) {
            source.sendMessage(Component.text("/ez reload - 重载配置", NamedTextColor.WHITE));
            source.sendMessage(Component.text("/ez status - 查看连接状态", NamedTextColor.WHITE));
            source.sendMessage(Component.text("/ez newversion - 查询 EasyBot 最新版本", NamedTextColor.WHITE));
            source.sendMessage(Component.text("/ez bindinfo <player> - 查询玩家绑定信息", NamedTextColor.WHITE));
            source.sendMessage(Component.text("/ez plugins - 查询 EasyBot 已安装插件", NamedTextColor.WHITE));
        }
    }

    // Tab 补全 (可选)
    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            if (invocation.source().hasPermission("easybot.admin")) {
                return List.of("bind", "social", "reload", "status", "newversion", "bindinfo", "plugins", "help");
            }
            return List.of("bind", "social", "help");
        }
        return List.of();
    }
}
