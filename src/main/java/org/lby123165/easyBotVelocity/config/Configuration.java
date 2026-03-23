package org.lby123165.easyBotVelocity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.lby123165.easyBotVelocity.EasyBotVelocity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {
    public String token = "YOUR_TOKEN_HERE";
    public String ws = "ws://127.0.0.1:26990/bridge";
    public boolean debug = false;
    public boolean ignoreError = false;
    public Message message = new Message();
    public Command command = new Command();
    public SkipOptions skipOptions = new SkipOptions();
    public Geyser geyser = new Geyser();
    public Sync sync = new Sync();
    public static class Message {
        public String bindStart = "[!] 绑定开始,请加群12345678输入: \"绑定 #code\" 进行绑定, 请在#time完成绑定!";
        public String bindSuccess = "[!] 绑定 #account (#name) 成功!";
        public String bindFail = "";
        public String syncSuccess = "";
    }

    public static class Command {
        public boolean allowBind = true;
    }

    public static class SkipOptions {
        public boolean skipJoin = false;
        public boolean skipQuit = false;
        public boolean skipChat = false;
        public boolean skipDeath = false;
    }

    public static class Geyser {
        public boolean ignorePrefix = false;
    }

    public static class Sync {
        public boolean chatImageSupport = true;
    }
    public void reload(Path dataDirectory) {
        Configuration newConfig = load(dataDirectory);
        this.token = newConfig.token;
        this.ws = newConfig.ws;
        this.debug = newConfig.debug;
        this.ignoreError = newConfig.ignoreError;
        this.message = newConfig.message;
        this.command = newConfig.command;
        this.skipOptions = newConfig.skipOptions;
        this.geyser = newConfig.geyser;
        this.sync = newConfig.sync;
    }
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static Configuration load(Path dataDirectory) {
        Path configFile = dataDirectory.resolve("config.json");
        Configuration config = null;
        try {
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            EasyBotVelocity.getInstance().getLogger().error("Failed to create data directory.", e);
            return new Configuration();
        }
        if (Files.exists(configFile)) {
            try {
                String jsonContent = Files.readString(configFile, StandardCharsets.UTF_8);
                config = GSON.fromJson(jsonContent, Configuration.class);
            } catch (JsonSyntaxException e) {
                EasyBotVelocity.getInstance().getLogger().error("config.json 格式错误! 正在备份原文件并生成默认配置...", e);
                backupCorruptedFile(configFile);
            } catch (IOException e) {
                EasyBotVelocity.getInstance().getLogger().error("Failed to read config.json.", e);
            }
        }
        if (config == null) {
            config = new Configuration();
        }
        save(config, configFile);
        return config;
    }

    private static void save(Configuration config, Path configFile) {
        try {
            String jsonContent = GSON.toJson(config);
            Files.writeString(configFile, jsonContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            EasyBotVelocity.getInstance().getLogger().error("Failed to save config.json.", e);
        }
    }

    private static void backupCorruptedFile(Path configFile) {
        try {
            Path backupFile = configFile.resolveSibling("config.json.bak." + System.currentTimeMillis());
            Files.move(configFile, backupFile);
            EasyBotVelocity.getInstance().getLogger().warn("损坏的配置文件已备份至: {}", backupFile.getFileName().toString());
        } catch (IOException e) {
            EasyBotVelocity.getInstance().getLogger().error("备份损坏的配置文件失败!", e);
        }
    }
}