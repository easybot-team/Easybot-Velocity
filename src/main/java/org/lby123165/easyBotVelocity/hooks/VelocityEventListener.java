package org.lby123165.easyBotVelocity.hooks;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.lby123165.easyBotVelocity.config.Configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VelocityEventListener {
    private final ProxyServer server;
    private final BridgeClient client;
    private final Configuration config;
    // 保持使用 '&' 格式
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    private final Set<String> kickList = Collections.synchronizedSet(new HashSet<>());

    public VelocityEventListener(ProxyServer server, BridgeClient client, Configuration config) {
        this.server = server;
        this.client = client;
        this.config = config;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (config.skipOptions.skipJoin) return;

        new Thread(() -> {
            try {
                Player player = event.getPlayer();
                String name = player.getUsername();
                String uuid = player.getUniqueId().toString();
                String ip = "127.0.0.1";
                if (player.getRemoteAddress() != null) {
                    ip = player.getRemoteAddress().getAddress().getHostAddress();
                }

                client.reportPlayer(name, uuid, ip);

                var loginResult = client.login(name, uuid);

                if (loginResult != null && Boolean.TRUE.equals(loginResult.getKick())) {
                    String rawKickReason = loginResult.getKickMessage();
                    if (rawKickReason == null) rawKickReason = "&c验证失败";

                    // [关键修复] 将后端发来的 '§' 替换为 '&'，以适配我们的 serializer
                    String kickReason = rawKickReason.replace('§', '&');

                    kickList.add(name);

                    // [关键修复] 这里必须用 '&c' 而不是 '§c'
                    player.sendMessage(serializer.deserialize("&c[EasyBot] 验证未通过: " + kickReason));
                    player.sendMessage(serializer.deserialize("&c[EasyBot] 您将在 3 秒后被移出服务器..."));

                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

                    // [关键修复] 同样替换 '§' 为 '&'
                    player.disconnect(serializer.deserialize("&c" + kickReason));

                    return;
                }

                PlayerInfoWithRaw info = new PlayerInfoWithRaw();
                info.setName(name);
                info.setUuid(uuid);
                info.setNameRaw(name);
                info.setIp(ip);

                client.syncEnterExit(info, true);

                try {
                    client.serverState(String.valueOf(server.getPlayerCount()));
                } catch (Exception ignored) {
                }

            } catch (Exception e) {
                if (!config.ignoreError) e.printStackTrace();
            }
        }).start();
    }

    // onDisconnect 和 onChat 保持不变...
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (config.skipOptions.skipQuit) return;
        Player player = event.getPlayer();
        String name = player.getUsername();

        if (kickList.contains(name)) {
            kickList.remove(name);
            return;
        }

        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(name);
        info.setUuid(player.getUniqueId().toString());
        info.setNameRaw(name);

        client.syncEnterExit(info, false);

        try {
            client.serverState(String.valueOf(server.getPlayerCount()));
        } catch (Exception ignored) {
        }
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (config.skipOptions.skipChat) return;
        Player player = event.getPlayer();
        String msg = event.getMessage();
        String serverName = player.getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("?");
        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(player.getUsername());
        info.setNameRaw(player.getUsername() + " [" + serverName + "]");
        client.syncMessage(info, msg, false); // useCommand = false 表示当前用户是从chat触发的消息同步
    }
}
