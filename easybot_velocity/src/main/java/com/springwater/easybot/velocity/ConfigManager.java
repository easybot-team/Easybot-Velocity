package com.springwater.easybot.velocity;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 */
public class ConfigManager {
    
    private final Path dataDirectory;
    private File configFile;
    private Map<String, Object> config;
    
    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configFile = dataDirectory.resolve("config.toml").toFile();
        this.config = new HashMap<>();
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * 加载配置
     */
    public void loadConfig() {
        try {
            if (!configFile.exists()) {
                if (!configFile.getParentFile().exists()) {
                    configFile.getParentFile().mkdirs();
                }
                
                // 创建默认配置
                Map<String, Object> defaultConfig = new HashMap<>();
                defaultConfig.put("server_name", "velocity_server");
                defaultConfig.put("service_url", "ws://127.0.0.1:8080/bridge");
                defaultConfig.put("token", "");
                defaultConfig.put("sync_chat", true);
                defaultConfig.put("sync_join_leave", true);
                defaultConfig.put("debug", false);
                defaultConfig.put("chat_format", "&b[MC] &e{player_name}&r: {message}");
                defaultConfig.put("join_format", "&e{player_name} &a加入了服务器");
                defaultConfig.put("leave_format", "&e{player_name} &c离开了服务器");
                defaultConfig.put("bind_success_message", "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!");
                defaultConfig.put("enable_success_event", false);
                defaultConfig.put("bind_success_commands", new java.util.ArrayList<String>());
                defaultConfig.put("enable_at_find", true);
                defaultConfig.put("enable_at_event", true);
                defaultConfig.put("force_bind_enabled", false);
                defaultConfig.put("force_bind_kick_message", "§c您需要先绑定QQ账号才能进入服务器！");
                defaultConfig.put("deny_on_bind_check_fail", true);
                
                // 保存默认配置
                new TomlWriter().write(defaultConfig, configFile);
                config = defaultConfig;
            } else {
                // 加载现有配置
                config = new Toml().read(configFile).toMap();
                
                // 检查是否有新的配置项需要添加
                boolean needUpdate = false;
                
                if (!config.containsKey("service_url") && config.containsKey("server_address") && config.containsKey("server_port")) {
                    // 从旧配置迁移
                    String address = (String) config.get("server_address");
                    Number port = (Number) config.get("server_port");
                    config.put("service_url", "ws://" + address + ":" + port + "/bridge");
                    needUpdate = true;
                } else if (!config.containsKey("service_url")) {
                    config.put("service_url", "ws://127.0.0.1:8080/bridge");
                    needUpdate = true;
                }
                
                if (!config.containsKey("token")) {
                    config.put("token", "");
                    needUpdate = true;
                }
                
                if (!config.containsKey("debug")) {
                    config.put("debug", false);
                    needUpdate = true;
                }
                
                if (!config.containsKey("chat_format")) {
                    config.put("chat_format", "&b[MC] &e{player_name}&r: {message}");
                    needUpdate = true;
                }
                
                if (!config.containsKey("join_format")) {
                    config.put("join_format", "&e{player_name} &a加入了服务器");
                    needUpdate = true;
                }
                
                if (!config.containsKey("leave_format")) {
                    config.put("leave_format", "&e{player_name} &c离开了服务器");
                    needUpdate = true;
                }
                
                if (!config.containsKey("bind_success_message")) {
                    config.put("bind_success_message", "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!");
                    needUpdate = true;
                }
                
                if (!config.containsKey("enable_success_event")) {
                    config.put("enable_success_event", false);
                    needUpdate = true;
                }
                
                if (!config.containsKey("bind_success_commands")) {
                    config.put("bind_success_commands", new java.util.ArrayList<String>());
                    needUpdate = true;
                }
                
                if (!config.containsKey("enable_at_find")) {
                    config.put("enable_at_find", true);
                    needUpdate = true;
                }
                
                if (!config.containsKey("enable_at_event")) {
                    config.put("enable_at_event", true);
                    needUpdate = true;
                }
                
                // 强制绑定相关配置
                if (!config.containsKey("force_bind_enabled")) {
                    config.put("force_bind_enabled", false);
                    needUpdate = true;
                }
                
                if (!config.containsKey("force_bind_kick_message")) {
                    config.put("force_bind_kick_message", "§c您需要先绑定QQ账号才能进入服务器！");
                    needUpdate = true;
                }
                
                if (!config.containsKey("deny_on_bind_check_fail")) {
                    config.put("deny_on_bind_check_fail", true);
                    needUpdate = true;
                }
                
                // 如果有新配置项，保存更新后的配置
                if (needUpdate) {
                    new TomlWriter().write(config, configFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取服务器名称
     */
    public String getServerName() {
        return (String) config.getOrDefault("server_name", "velocity_server");
    }
    
    /**
     * 获取服务URL
     */
    public String getServiceUrl() {
        return (String) config.getOrDefault("service_url", "ws://127.0.0.1:8080/bridge");
    }
    
    /**
     * 获取Token
     */
    public String getToken() {
        return (String) config.getOrDefault("token", "");
    }
    
    /**
     * 是否同步聊天
     */
    public boolean isSyncChat() {
        return (boolean) config.getOrDefault("sync_chat", true);
    }
    
    /**
     * 是否同步加入/离开消息
     */
    public boolean isSyncJoinLeave() {
        return (boolean) config.getOrDefault("sync_join_leave", true);
    }
    
    /**
     * 是否开启调试模式
     */
    public boolean isDebug() {
        return (boolean) config.getOrDefault("debug", false);
    }
    
    /**
     * 获取聊天格式
     */
    public String getChatFormat() {
        return (String) config.getOrDefault("chat_format", "&b[MC] &e{player_name}&r: {message}");
    }
    
    /**
     * 获取加入消息格式
     */
    public String getJoinFormat() {
        return (String) config.getOrDefault("join_format", "&e{player_name} &a加入了服务器");
    }
    
    /**
     * 获取离开消息格式
     */
    public String getLeaveFormat() {
        return (String) config.getOrDefault("leave_format", "&e{player_name} &c离开了服务器");
    }
    
    /**
     * 获取绑定成功消息
     */
    public String getBindSuccessMessage() {
        return (String) config.getOrDefault("bind_success_message", "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!");
    }
    
    /**
     * 是否启用绑定成功事件
     */
    public boolean isEnableSuccessEvent() {
        return (boolean) config.getOrDefault("enable_success_event", false);
    }
    
    /**
     * 获取绑定成功命令列表
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getBindSuccessCommands() {
        return (java.util.List<String>) config.getOrDefault("bind_success_commands", new java.util.ArrayList<>());
    }
    
    /**
     * 是否启用@查找
     */
    public boolean isEnableAtFind() {
        return (boolean) config.getOrDefault("enable_at_find", true);
    }
    
    /**
     * 是否启用@事件
     */
    public boolean isEnableAtEvent() {
        return (boolean) config.getOrDefault("enable_at_event", true);
    }
    
    /**
     * 是否为调试模式（兼容方法）
     */
    public boolean isDebugMode() {
        return isDebug();
    }
    
    /**
     * 是否启用强制绑定
     */
    public boolean isForceBindEnabled() {
        return (boolean) config.getOrDefault("force_bind_enabled", false);
    }
    
    /**
     * 获取强制绑定踢出消息
     */
    public String getForceBindKickMessage() {
        return (String) config.getOrDefault("force_bind_kick_message", "§c您需要先绑定QQ账号才能进入服务器！");
    }
    
    /**
     * 绑定检查失败时是否拒绝登录
     */
    public boolean isDenyOnBindCheckFail() {
        return (boolean) config.getOrDefault("deny_on_bind_check_fail", true);
    }
}
