package com.springwater.easybot.velocity;

import com.google.inject.Inject;
import com.springwater.easybot.bridge.BridgeBehavior;
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
import java.util.Objects;

@Plugin(
        id = "easybot",
        name = "EasyBot",
        version = "1.0.0",
        description = "EasyBot for Velocity",
        authors = {"Springwater"}
)
public class EasyBotVelocity {
    
    private static EasyBotVelocity instance;
    
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private BridgeClient bridgeClient;
    private BridgeBehavior bridgeBehavior;
    private ConfigManager configManager;
    private UpdateChecker updateChecker = new UpdateChecker();
    
    @Inject
    public EasyBotVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }
    
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // 加载配置
        configManager = new ConfigManager(dataDirectory);
        configManager.loadConfig();
        
        if (Objects.equals(configManager.getToken(), "")) {
            logger.error("EasyBot已禁用, 请先在配置文件中设置Token!!!");
            return;
        }
        
        instance = this;
        
        // 设置客户端配置
        ClientProfile.setPluginVersion("1.0.0");
        ClientProfile.setServerDescription("Velocity");
        ClientProfile.setDebugMode(configManager.isDebugMode());
        ClientProfile.setCommandSupported(true);
        ClientProfile.setPapiSupported(false); // Velocity不支持PlaceholderAPI
        ClientProfile.setOnlineMode(true); // Velocity通常是在线模式
        
        // 初始化Bridge行为实现
        bridgeBehavior = new EasyBotVelocityBehavior(this);
        
        // 初始化Bridge客户端
        bridgeClient = new BridgeClient(configManager.getServiceUrl(), bridgeBehavior);
        bridgeClient.setToken(configManager.getToken());
        
        // 注册事件监听器
        server.getEventManager().register(this, new PlayerEvents(this));
        server.getEventManager().register(this, new MessageEvents(this));
        
        // 注册命令
        server.getCommandManager().register("easybot", new EasyBotVelocityCommand(this), "eb");
        
        // 启动更新检查器
        updateChecker.start();
        
        logger.info("EasyBot for Velocity 已启用!");
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (bridgeClient != null) {
            bridgeClient.close();
        }
        if (updateChecker != null) {
            updateChecker.stop();
        }
        logger.info("EasyBot for Velocity 已禁用!");
    }
    
    public void reload() {
        updateChecker.stop();
        configManager.loadConfig();
        
        ClientProfile.setPluginVersion("1.0.0");
        ClientProfile.setServerDescription("Velocity代理服务器");
        ClientProfile.setDebugMode(configManager.isDebugMode());
        
        bridgeClient.setToken(configManager.getToken());
        bridgeClient.resetUrl(configManager.getServiceUrl());
        bridgeClient.stop();
        updateChecker.start();
    }
    
    public ProxyServer getServer() {
        return server;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public BridgeClient getBridgeClient() {
        return bridgeClient;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public BridgeBehavior getBridgeBehavior() {
        return bridgeBehavior;
    }
    
    public static EasyBotVelocity getInstance() {
        return instance;
    }
}
