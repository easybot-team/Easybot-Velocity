package org.lby123165.easyBotVelocity.hooks;

import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.packet.PlayerInfoWithRaw;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import org.lby123165.easyBotVelocity.EasyBotVelocity;
import org.lby123165.easyBotVelocity.config.Configuration;
import org.lby123165.easyBotVelocity.utils.LegacyTextUtils;
import org.lby123165.easyBotVelocity.utils.PlayerInfoBuilder;

import java.util.HashSet;

public class VelocityEventListener {
    private final BridgeClient client;
    private final Configuration config;

    public VelocityEventListener(BridgeClient client, Configuration config) {
        this.client = client;
        this.config = config;
    }
    
    @Subscribe(priority = 100)
    public void onPreLogin(PreLoginEvent event){
        if (config.skipOptions.skipJoin) return;
        if(event.getUniqueId() ==null){
            if(!config.ignoreError) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(LegacyTextUtils.toComponent("§c[EasyBotVelocity] 登陆失败,未获取到你的UUID数据！")));
            }else{
                EasyBotVelocity.getInstance().getLogger().warn("无法处理玩家数据{},玩家加入未携带有效UUID数据!", event.getUsername());
            }
            return;
        }
        
        PlayerInfoWithRaw playerInfo = new PlayerInfoWithRaw();
        playerInfo.setName(event.getUsername());
        playerInfo.setNameRaw(event.getUsername());
        playerInfo.setUuid(event.getUniqueId().toString());
        String ip = "127.0.0.1";
        if(event.getConnection().getRemoteAddress() != null){
            ip = event.getConnection().getRemoteAddress().getAddress().getHostAddress();
        }
        playerInfo.setIp(ip);
        client.reportPlayer(playerInfo.getName(), playerInfo.getUuid(), playerInfo.getIp());
        try{
            var loginResult = client.login(playerInfo.getName(), playerInfo.getUuid());
            if (loginResult.getKick()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(LegacyTextUtils.toComponent(loginResult.getKickMessage())));
                return;
            }
            client.syncEnterExit(playerInfo, true);
        } catch (Exception e) {
            EasyBotVelocity.getInstance().getLogger().error("[EasyBot] 处理玩家登陆失败", e);
            if (!config.ignoreError) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(LegacyTextUtils.toComponent("§c[EasyBotVelocity] 登陆失败,服务器内部异常,请稍后再试")));
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
