package org.lby123165.easyBotVelocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Configuration {
    // 根节点字段，赋默认值
    public String token = "YOUR_TOKEN_HERE";
    public String ws = "ws://127.0.0.1:26990/bridge";
    public boolean debug = false;
    public boolean ignoreError = false;

    // 嵌套对象
    public Message message = new Message();
    public Command command = new Command();
    public SkipOptions skipOptions = new SkipOptions();
    public Geyser geyser = new Geyser();

    // 内部类定义
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
    }


    // ================== 加载/保存逻辑 ==================

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Configuration load(Path dataDirectory) {
        Path configFile = dataDirectory.resolve("config.json");
        Configuration config = null;

        // 1. 确保目录存在
        if (Files.notExists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 2. 读取或创建
        if (Files.exists(configFile)) {
            try (Reader reader = new InputStreamReader(Files.newInputStream(configFile), StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, Configuration.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 如果文件不存在或读取失败，使用默认值
        if (config == null) {
            config = new Configuration();
        }

        // 3. 保存一次（确保新字段写入或者文件被创建）
        save(config, configFile);

        return config;
    }

    private static void save(Configuration config, Path configFile) {
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(configFile), StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
