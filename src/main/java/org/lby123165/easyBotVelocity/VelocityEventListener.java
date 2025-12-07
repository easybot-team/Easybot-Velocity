package org.lby123165.easyBotVelocity;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VelocityEventListener {
    private final BridgeClient client;
    private final Configuration config;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    private final Set<String> kickList = Collections.synchronizedSet(new HashSet<>());

    public VelocityEventListener(BridgeClient client, Configuration config) {
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

                    // 将后端发来的 '§' 替换为 '&'，以适配 serializer
                    String kickReason = rawKickReason.replace('§', '&');

                    kickList.add(name);

                    player.sendMessage(serializer.deserialize("&c[EasyBot] 验证未通过: " + kickReason));
                    player.sendMessage(serializer.deserialize("&c[EasyBot] 您将在 3 秒后被移出服务器..."));

                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

                    player.disconnect(serializer.deserialize("&c" + kickReason));

                    return;
                }

                PlayerInfoWithRaw info = new PlayerInfoWithRaw();
                info.setName(name);
                info.setUuid(uuid);
                info.setNameRaw(name);
                info.setIp(ip);

                client.syncEnterExit(info, true);

            } catch (Exception e) {
                if (!config.ignoreError) e.printStackTrace();
            }
        }).start();
    }

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
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (config.skipOptions.skipChat) return;
        Player player = event.getPlayer();
        String msg = event.getMessage();
        boolean isCmd = msg.startsWith("/");

        String serverName = player.getCurrentServer()
                .map(s -> s.getServerInfo().getName())
                .orElse("?");

        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(player.getUsername());
        info.setNameRaw(player.getUsername() + " [" + serverName + "]");


        client.syncMessage(info, msg, isCmd);
    }
}
