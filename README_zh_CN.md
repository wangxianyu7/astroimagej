# AstroImageJ（非官方汉化版 / Unofficial zh_CN build）

> ⚠️ **非官方汉化版** —— 本仓库是 [AstroImageJ](https://github.com/AstroImageJ/astroimagej) 的社区中文本地化 fork，
> **不是官方发布**,也**未经 AstroImageJ 原作者背书**。请勿将其误认为官方版本。
> 遵循 **GPLv3**(与上游一致)。原英文说明见 [README.md](README.md)。

AstroImageJ（简称 AIJ）是在 ImageJ 基础上、由 Karen Collins 博士等开发的天文测光与图像分析软件,
广泛用于系外行星凌星测光、孔径测光、WCS 解算等。本 fork 在**不改动原英文界面**的前提下,
增加了一套轻量 i18n 框架,把界面逐步翻译为简体中文。

## 与上游的关系

- 代码基线来自上游 `AstroImageJ/astroimagej`,本 fork 会定期合并上游更新。
- 本 fork 仅新增:i18n 框架(`ij/I18n.java`)、`messages.properties` / `messages_zh_CN.properties`、
  各对话框/菜单的 `I18n.t(...)` 调用,以及少量 fork 行为调整(如默认关闭启动更新检查,避免覆盖汉化构建)。
- 汉化技术细节见 [LOCALIZATION.md](LOCALIZATION.md)。

## 语言切换

启动时通过 JVM 参数选择语言(不加则保持英文):

```
-Daij.lang=zh_CN     # 简体中文
```

安装版可在 `AstroImageJ.cfg`(或启动脚本)的 Java 启动参数里加入上面这一行。

## 从源码构建

需要 JDK（21+）。在仓库根目录:

```bash
# 运行开发版
./gradlew aijRun

# 只编译两个核心模块
./gradlew :ij:build :Astronomy_:build

# 打包当前平台安装包（产物在 build/distributions/）
./gradlew packageAijForMac_aarch64      # macOS Apple Silicon
./gradlew packageAijForLinux_x64        # Linux
./gradlew packageAijForWindows_x64      # Windows（.msi，需在 Windows 上 + WiX Toolset）
```

### 关于 Windows MSI 安装包

- 构建脚本已内置 `jpackage --type msi`,产物是标准 MSI 安装包(带开始菜单、快捷方式、文件关联等)。
- **MSI 只能在 Windows 上生成**(jpackage 不能跨平台打包;Windows 端还需 WiX Toolset 3.x)。
- 无 Windows 机器时,可用本 fork 的 **GitHub Actions**(`windows-latest` runner)在云端构建,
  下载 `installer-windows-x64` 产物即可。首次使用需在 fork 的 **Actions 页面手动启用工作流**。
- fork 默认不包含上游的签名/公证 secrets,因此产出的是**未签名 MSI**,可正常安装,
  仅 Windows SmartScreen 会提示"未知发布者"。

## 许可 (License)

本项目遵循 **GNU GPL v3**(见 [LICENSE](LICENSE))。你可以自由使用、修改、再分发本汉化版,
但须:保持 GPLv3 开源、随二进制提供对应源码、保留原版权与许可声明、并标明这是**修改版 / 非官方汉化版**。
"AstroImageJ" 名称/品牌归原作者;本 fork 不代表官方,分发时请保留"非官方汉化版"标识。

## 致谢

- 上游 [AstroImageJ](https://github.com/AstroImageJ/astroimagej)(Karen Collins 等)与 [ImageJ](https://imagej.net/)。
- 汉化维护:本 fork 维护者。欢迎通过 Issue / PR 补充翻译。
