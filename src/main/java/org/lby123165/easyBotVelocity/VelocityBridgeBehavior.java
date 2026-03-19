package org.lby123165.easyBotVelocity;

import com.springwater.easybot.bridge.BridgeBehavior;
import com.springwater.easybot.bridge.message.Segment;
import com.springwater.easybot.bridge.model.PlayerInfo;
import com.springwater.easybot.bridge.model.ServerInfo;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class VelocityBridgeBehavior implements BridgeBehavior {
    private final ProxyServer server;
    private final Logger logger;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public VelocityBridgeBehavior(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }
    @Override
    public String runCommand(String playerName, String command, boolean enablePapi) {
        try {
            logger.info("收到命令执行请求: " + command);
            server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
            return "命令已通过控制台执行: " + command;
        } catch (Exception e) {
            logger.error("执行命令时出错: " + e.getMessage(), e);
            return "命令执行失败: " + e.getMessage();
        }
    }

    @Override
    public String papiQuery(String playerName, String query) {
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
        // [修复] 替换所有 § 为 &，防止报错
        String safeMessage = message.replace('§', '&');
        server.sendMessage(serializer.deserialize("&b[Bot] &r" + safeMessage));
    }

    @Override
    public void SyncToChatExtra(List<Segment> segments, String text) {
        StringBuilder sb = new StringBuilder();
        for (Segment seg : segments) {
            // [修复] 获取文本后，立即替换 §
            String segText = seg.getText();
            if (segText != null) {
                sb.append(segText.replace('§', '&'));
            }
        }
        // 再次确保整体安全
        String finalMsg = sb.toString().replace('§', '&');
        server.sendMessage(serializer.deserialize("&b[Bot] &r" + finalMsg));
    }

    @Override
    public void BindSuccessBroadcast(String playerName, String accountId, String accountName) {
        // [修复] §a -> &a
        String msg = String.format("&a[EasyBot] 玩家 %s 成功绑定账号 %s!", playerName, accountName);
        server.sendMessage(serializer.deserialize(msg));
    }

    @Override
    public void KickPlayer(String playerName, String kickMessage) {
        server.getPlayer(playerName).ifPresent(player -> {
            // [修改] 完全忽略后端传来的 kickMessage，直接写死
            // 使用 & 格式，确保兼容性
            String reason = "&c您已在社交平台解绑账号，请重新验证。";

            player.disconnect(serializer.deserialize(reason));
        });
    }

    @Override
    public List<PlayerInfo> getPlayerList() {
        List<PlayerInfo> list = new ArrayList<>();
        for (Player p : server.getAllPlayers()) {
            PlayerInfo info = new PlayerInfo();

            // 获取玩家所在的子服务器名称
            String serverName = p.getCurrentServer()
                    .map(server -> server.getServerInfo().getName())
                    .orElse("Unknown"); // 正在连接中可能没服务器

            // [修改] 将服务器名附加在名字后面，或者只上传纯名字
            // 如果 EasyBot 后台支持解析 "Name (Server)" 这种格式最好
            // 如果不支持，可能需要修改 bridge 协议增加字段
            // 这里暂时仅上报纯名字，因为乱改名字可能会影响绑定验证等功能

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