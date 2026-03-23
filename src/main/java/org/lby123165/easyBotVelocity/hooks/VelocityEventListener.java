package org.lby123165.easyBotVelocity.hooks;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import org.lby123165.easyBotVelocity.EasyBotVelocity;
import org.lby123165.easyBotVelocity.config.Configuration;
import org.lby123165.easyBotVelocity.utils.LegacyTextUtils;
import org.lby123165.easyBotVelocity.utils.PlayerInfoBuilder;

public class VelocityEventListener {
    private final BridgeClient client;
    private final Configuration config;

    public VelocityEventListener(BridgeClient client, Configuration config) {
        this.client = client;
        this.config = config;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (config.skipOptions.skipJoin) return;
        Player player = event.getPlayer();
        PlayerInfoWithRaw playerInfo = PlayerInfoBuilder.build(player);
        try {
            client.reportPlayer(playerInfo.getName(), playerInfo.getUuid(), playerInfo.getIp());
            var loginResult = client.login(playerInfo.getName(), playerInfo.getUuid());
            if (loginResult.getKick()) {
                player.disconnect(LegacyTextUtils.toComponent(loginResult.getKickMessage()));
                return;
            }
            client.syncEnterExit(playerInfo, true);
        } catch (Exception e) {
            EasyBotVelocity.getInstance().getLogger().error("[EasyBot] 处理玩家登陆失败", e);
            if (!config.ignoreError) {
                player.disconnect(LegacyTextUtils.toComponent("§c[VC] 服务器内部异常,请稍后再试"));
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (config.skipOptions.skipQuit) return;
        Player player = event.getPlayer();
        PlayerInfoWithRaw playerInfo = PlayerInfoBuilder.build(player);
        client.syncEnterExit(playerInfo, false);
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
