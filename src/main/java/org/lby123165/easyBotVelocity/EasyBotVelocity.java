package org.lby123165.easyBotVelocity;

import com.google.inject.Inject;
import com.springwater.easybot.bridge.BridgeClient;
import com.springwater.easybot.bridge.ClientProfile;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "easybot-velocity",
        name = "EasyBot-Velocity",
        version = "0.1",
        authors = {"lby123165"}
)
public class EasyBotVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private BridgeClient bridgeClient;

    @Inject
    public EasyBotVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("EasyBot-Velocity 正在加载...");

        // 1. 加载 Config.json
        Configuration config = Configuration.load(dataDirectory);

        ClientProfile.setPluginVersion(BuildConstants.VERSION);
        ClientProfile.setServerDescription("Velocity");
        ClientProfile.setDebugMode(config.debug);

        logger.info("连接目标: " + config.ws);

        // 3. 初始化 Client
        VelocityBridgeBehavior behavior = new VelocityBridgeBehavior(server, logger);
        bridgeClient = new BridgeClient(config.ws, behavior);
        bridgeClient.setToken(config.token);

        // 4. 注册监听器 (将 config 传进去，用于判断 skipOptions)
        server.getEventManager().register(this, new VelocityEventListener(bridgeClient, config));
        EasyBotCommand cmd = new EasyBotCommand(bridgeClient, config, dataDirectory);

        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("ez")
                        .aliases("easybot")
                        .plugin(this)
                        .build(),
                cmd
        );

        logger.info("EasyBot-Velocity 加载完成!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (bridgeClient != null) {
            bridgeClient.stop();
        }
    }
}
