package org.lby123165.easyBotVelocity;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

public class VelocityEventListener {
    private final BridgeClient client;
    private final Configuration config; // 持有配置引用

    public VelocityEventListener(BridgeClient client, Configuration config) {
        this.client = client;
        this.config = config;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        // 判断是否跳过进服
        if (config.skipOptions.skipJoin) return;

        new Thread(() -> {
            try {
                Player player = event.getPlayer();

                // 处理 Geyser 前缀忽略 (如果需要)
                String name = player.getUsername();
                if (config.geyser.ignorePrefix && name.startsWith(".")) {
                    // 这里只是示例，具体逻辑看需求
                }

                client.login(player.getUsername(), player.getUniqueId().toString());

                PlayerInfoWithRaw info = new PlayerInfoWithRaw();
                info.setName(player.getUsername());
                info.setUuid(player.getUniqueId().toString());
                info.setNameRaw(player.getUsername());

                client.syncEnterExit(info, true);
            } catch (Exception e) {
                if (!config.ignoreError) e.printStackTrace();
            }
        }).start();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        // 判断是否跳过退服
        if (config.skipOptions.skipQuit) return;

        Player player = event.getPlayer();
        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(player.getUsername());
        info.setUuid(player.getUniqueId().toString());
        info.setNameRaw(player.getUsername());

        client.syncEnterExit(info, false);
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        // 判断是否跳过聊天
        if (config.skipOptions.skipChat) return;

        Player player = event.getPlayer();
        String msg = event.getMessage();
        boolean isCmd = msg.startsWith("/");

        PlayerInfoWithRaw info = new PlayerInfoWithRaw();
        info.setName(player.getUsername());
        info.setUuid(player.getUniqueId().toString());
        info.setNameRaw(player.getUsername());

        client.syncMessage(info, msg, isCmd);
    }
}
