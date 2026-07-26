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
- 摄像头显示为原版灵魂灯笼模型/贴图
- 接收器始终显示为铁块模型
- 接收器触发时输出 15 级红石信号 2 秒
- 接收器支持自定义名称，`/gbs bind` 会显示修改后的接收器名称
- 接收器支持切换极性：触发时充能，或未触发时充能
- 授权玩家触发摄像头时播放提示音
- 右键摄像头打开配置 GUI
- 右键接收器打开配置 GUI
- `/gbs` 命令配置摄像头
- 摄像头配置保存在方块实体 NBT 中
- 授权玩家进入监控格时触发接收器并公屏提示
- 未授权玩家、怪物、动物等生物进入监控格时公屏警告

## 命令

右键摄像头会打开配置 GUI。命令仍然保留为备用：先右键一个摄像头，然后执行：

```text
/gbs info
/gbs toggle
/gbs name 基地入口
/gbs volume 100
/gbs trust 玩家名
/gbs untrust 玩家名
/gbs area addall
/gbs area clear
/gbs bind
```

`/gbs area addall` 会把摄像头下方 `5x5x5` 范围内的空气格加入监控区域。

`/gbs bind` 会绑定摄像头下方 `5x5x5` 范围内找到的第一个接收器。

`/gbs volume 0-200` 会设置授权玩家触发摄像头时的提示音音量，`0` 为静音，`100` 为正常音量，`200` 为两倍音量。

## GUI

- 右键高别师的摄像头：打开摄像头配置界面
- 摄像头 GUI：修改名称、开关监控、设置提示音音量、授权/移除玩家、虚拟编辑/清空监控区域、绑定/解绑下方接收器
- 右键高别师的接收器：打开接收器配置界面
- 接收器 GUI：修改接收器名称、切换极性
- 摄像头和接收器只有拥有权限的玩家可以右键配置
- 放置方块的玩家默认拥有配置权限
- GUI 会列出服务器在线玩家，可直接允许/移除配置权限
- 摄像头 GUI 可以进入监控区域编辑模式：会获得“监控区域配置”绿色玻璃和“退出编辑模式”望远镜
- 编辑模式中只能在摄像头下方 `5x5x5` 范围内放置“监控区域配置”玻璃；放置位置加入监控区域
- 破坏编辑模式中放置的绿色玻璃会移除对应监控区域
- 右键“退出编辑模式”望远镜会退出编辑，并清除本次编辑放置的临时绿色玻璃；监控区域配置仍然保留

## 构建

需要 Java 17 和网络下载 ForgeGradle 依赖：

```bash
cd /Users/gargantua/GitHub_repo/Minecraft-security-mod
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home ./gradlew build
```

成功后 jar 在：

```text
build/libs/gaobieshi-security-0.1.0.jar
```

把这个 jar 放到客户端和服务端的 `mods` 目录。

项目已经包含 Gradle Wrapper，不需要本机安装 Gradle。也可以先尝试：

```bash
./build-local.sh
```

这个脚本会在 `/tmp` 下载 Gradle 8.8 后构建。
