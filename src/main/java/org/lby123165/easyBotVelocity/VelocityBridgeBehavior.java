package org.lby123165.easyBotVelocity;

import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class VelocityBridgeBehavior implements BridgeBehavior {
    private final ProxyServer server;
    private final Logger logger;
    // Velocity 使用 Adventure API，需要序列化器处理 "§" 颜色代码
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

    public VelocityBridgeBehavior(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public String runCommand(String playerName, String command, boolean enablePapi) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
        return "Command executed via Velocity Console";
    }

    @Override
    public String papiQuery(String playerName, String query) {
        // Velocity 原生不支持 PAPI，通常返回原文本或空
        return query;
    }

    @Override
    public ServerInfo getInfo() {
        ServerInfo info = new ServerInfo();
        info.setServerName("Velocity");
        info.setServerVersion(server.getVersion().getVersion());
        info.setPluginVersion(BuildConstants.VERSION);
        info.setOnlineMode(server.getConfiguration().isOnlineMode());
        info.setCommandSupported(true);
        return info;
    }

    @Override
    public void SyncToChat(String message) {
        // 将文本转换为 Component 并广播
        server.sendMessage(serializer.deserialize("§b[Bot] §r" + message));
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        // 简单拼接
        StringBuilder sb = new StringBuilder();
        for (Segment seg : segments) {
            sb.append(seg.getText());
        }
        server.sendMessage(serializer.deserialize("§b[Bot] §r" + sb.toString()));
    }

    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        String msg = String.format("§a[EasyBot] 玩家 %s 成功绑定账号 %s!", playerName, accountName);
        server.sendMessage(serializer.deserialize(msg));
    }

    @Override
    public void KickPlayer(String playerName, String kickMessage) {
        server.getPlayer(playerName).ifPresent(player ->
                player.disconnect(serializer.deserialize(kickMessage))
        );
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        List<PlayerInfo> list = new ArrayList<>();
        for (Player p : server.getAllPlayers()) {
            PlayerInfo info = new PlayerInfo();
            info.setPlayerName(p.getUsername());
            info.setPlayerUuid(p.getUniqueId().toString());
            if (p.getRemoteAddress() != null) {
                info.setIp(p.getRemoteAddress().getAddress().getHostAddress());
            }
            list.add(info);
        }
        return list;
    }
}
