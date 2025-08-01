# EasyBot Velocity 支持

这是EasyBot插件的Velocity代理服务器版本，提供与Bukkit版本相同的功能。

## 功能特性

- ✅ 聊天消息同步
- ✅ 玩家加入/离开消息同步
- ✅ 玩家绑定功能
- ✅ 强制绑定功能
- ✅ 命令执行支持
- ✅ 配置热重载
- ✅ @功能支持

## 安装方法

1. 将编译好的 `EasyBot-Velocity.jar` 文件放入Velocity服务器的 `plugins` 文件夹
2. 启动Velocity服务器
3. 编辑生成的配置文件 `plugins/easybot/config.toml`
4. 使用 `/easybot reload` 命令重新加载配置

## 配置说明

### 基础配置

```toml
# 服务器名称
server_name = "velocity_server"

# 服务URL (WebSocket连接地址)
service_url = "ws://127.0.0.1:8080/bridge"

# 认证令牌
token = "your_token_here"
```

### 同步设置

```toml
# 是否同步聊天消息
sync_chat = true

# 是否同步玩家加入/离开消息
sync_join_leave = true

# 是否开启调试模式
debug = false
```

### 消息格式

```toml
# 聊天消息格式
chat_format = "&b[MC] &e{player_name}&r: {message}"

# 加入消息格式
join_format = "&e{player_name} &a加入了服务器"

# 离开消息格式
leave_format = "&e{player_name} &c离开了服务器"
```

### 绑定功能

```toml
# 绑定成功消息
bind_success_message = "§f[§a!§f] 绑定§f §a#account §f(§a#name§f) 成功!"

# 是否启用绑定成功事件
enable_success_event = false

# 绑定成功后执行的命令列表
bind_success_commands = [
    "give {player_name} diamond 1",
    "broadcast {player_name} 完成了绑定！"
]
```

### 强制绑定功能

```toml
# 是否启用强制绑定（未绑定玩家无法进入服务器）
force_bind_enabled = false

# 强制绑定踢出消息
force_bind_kick_message = "§c您需要先绑定QQ账号才能进入服务器！"

# 绑定检查失败时是否拒绝登录
deny_on_bind_check_fail = true
```

### @功能设置

```toml
# 是否启用@查找功能
enable_at_find = true

# 是否启用@事件功能
enable_at_event = true
```

## 命令说明

### `/easybot` 或 `/eb`

- `/easybot` - 显示插件信息
- `/easybot reload` - 重新加载配置（需要 `easybot.admin` 权限）
- `/easybot status` - 查看Bridge连接状态

## 权限说明

- `easybot.command` - 使用基础命令的权限
- `easybot.admin` - 管理员权限（重载配置等）

## 与Bukkit版本的差异

1. **配置格式**：Velocity版本使用TOML格式配置文件，而Bukkit版本使用YAML格式
2. **事件系统**：使用Velocity的事件系统，但功能保持一致
3. **消息系统**：使用Adventure API处理文本消息
4. **依赖注入**：使用Google Guice进行依赖注入

## 故障排除

### 连接问题

1. 检查 `service_url` 配置是否正确
2. 确认EasyBot主程序正在运行
3. 检查网络连接和防火墙设置

### 强制绑定问题

1. 确认 `force_bind_enabled = true`
2. 检查EasyBot主程序的绑定功能是否正常
3. 查看控制台日志获取详细错误信息

### 权限问题

1. 确认玩家拥有相应的权限节点
2. 检查Velocity的权限插件配置

## 开发信息

- **API版本**：Velocity 3.2.0
- **Java版本**：Java 11+
- **依赖**：easybot_bridge模块

## 更新日志

### v1.0.0
- 初始版本发布
- 支持所有基础功能
- 添加强制绑定功能
- 支持配置热重载