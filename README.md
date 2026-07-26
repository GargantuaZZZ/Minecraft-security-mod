# Minecraft-security-mod

高别师安防 Forge Mod，用于 ATM9 1.1.1 / Minecraft 1.20.1。

目标环境：

- Minecraft 1.20.1
- Forge 47.4.0
- ATM9 1.1.1
- Java 17

## 当前功能

- 新方块：高别师的摄像头
- 新方块：高别师的接收器
- 摄像头只能挂在方块底面
- 接收器始终显示为铁块模型
- 接收器触发时输出 15 级红石信号 2 秒
- 右键摄像头选中它
- `/gbs` 命令配置摄像头
- 摄像头配置保存在方块实体 NBT 中
- 授权玩家进入监控格时触发接收器并公屏提示
- 未授权玩家、怪物、动物等生物进入监控格时公屏警告

## 命令

先右键一个摄像头，然后执行：

```text
/gbs info
/gbs toggle
/gbs name 基地入口
/gbs trust 玩家名
/gbs untrust 玩家名
/gbs area addall
/gbs area clear
/gbs bind
```

`/gbs area addall` 会把摄像头下方 `5x5x5` 范围内的空气格加入监控区域。

`/gbs bind` 会绑定摄像头下方 `5x5x5` 范围内找到的第一个接收器。

## 构建

需要 Gradle 和网络下载 ForgeGradle 依赖：

```bash
cd /Users/gargantua/GitHub_repo/Minecraft-security-mod
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home gradle build
```

成功后 jar 在：

```text
build/libs/gaobieshi-security-0.1.0.jar
```

把这个 jar 放到客户端和服务端的 `mods` 目录。

如果本机没有安装 Gradle，也可以先尝试：

```bash
./build-local.sh
```

这个脚本会在 `/tmp` 下载 Gradle 8.8 后构建。
