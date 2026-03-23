package org.lby123165.easyBotVelocity;

import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.message.TextSegment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.lby123165.easyBotVelocity.sender.EasyBotCommandSender;
import org.lby123165.easyBotVelocity.utils.LegacyTextUtils;
import org.lby123165.easyBotVelocity.utils.LibreLoginUtils;
import org.lby123165.easyBotVelocity.utils.StringUtils;
import org.lby123165.easyBotVelocity.utils.SyncSegmentConverter;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VelocityBridgeBehavior implements BridgeBehavior {
    private final ProxyServer server;

    public VelocityBridgeBehavior(ProxyServer server) {
        this.server = server;
    }

    @Override
    public String runCommand(String playerName, String command, boolean enablePapi) {
        EasyBotCommandSender sender = new EasyBotCommandSender();
        if (enablePapi) {
            command = papiQuery(playerName, command);
        }
        CompletableFuture<Boolean> task = server.getCommandManager().executeAsync(sender, command);
        Boolean isSuccess = task.join();
        String feedbacks = String.join("\n", sender.getFeedbacks());
        if (!isSuccess) {
            throw new RuntimeException(StringUtils.ifEmpty(feedbacks, "命令执行失败: 未返回错误"));
        }
        return feedbacks;
    }

    @Override
    public String papiQuery(String playerName, String query) {
        return query.replace("%player_name%", playerName);
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
        server.sendMessage(LegacyTextUtils.toComponent(message));
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        TextComponent.Builder finalMsg = Component.text();
        for (Segment segment : SyncSegmentConverter.mergeSegments(segments)) {
            finalMsg.append(SyncSegmentConverter.toComponent(segment));
        }
        server.sendMessage(finalMsg);
    }

    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        //String msg = String.format("&a[EasyBot] 玩家 %s 成功绑定账号 %s!", playerName, accountName);
        //server.sendMessage(serializer.deserialize(msg));

        // do nothing
    }

    @Override
    public void KickPlayer(String playerName, String kickMessage) {
        server.getPlayer(playerName).ifPresent(player -> {
            var reason = kickMessage;
            if (Objects.equals(kickMessage, "")) // 旧版本eb bug, 需要兼容
                reason = "§c您已在社交平台解绑账号，请重新验证。";
            player.disconnect(LegacyTextUtils.toComponent(reason));
        });
    }

    @Override
    public boolean moduleIsInstalled(String moduleName) {
        return EasyBotVelocity.getInstance().getServer().getPluginManager().isLoaded(moduleName);
    }

    @Override
    public boolean moduleIsEnabled(String moduleName) {
        return moduleIsInstalled(moduleName);// Velocity 貌似没有模块启用状态的概念??
    }

    @Override
    public boolean isAuthenticated(String playerName) {
        Optional<Player> player = server.getPlayer(playerName);
        if (player.isEmpty()) return true; // 这里返回true表示不需要进行登录
        if (LibreLoginUtils.hasLiberLogin()) {
            return LibreLoginUtils.isAuthenticated(player.get());
        }
        return true;
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