# AstroImageJ 汉化(本地化)说明

本仓库已加入一套轻量级 i18n 框架,可在不影响原英文界面的前提下,逐对话框翻译成中文(或其他任何语言)。

## 总览

| 文件 | 角色 |
|---|---|
| `ij/src/main/java/ij/I18n.java` | 静态 ResourceBundle 加载器 + `I18n.t(key)`(包名 `ij`) |
| `ij/src/main/resources/messages.properties` | 英文默认值(所有 key 都要有) |
| `ij/src/main/resources/messages_zh_CN.properties` | 简体中文翻译 |
| `messages_zh_TW.properties`(可建) | 繁体中文 |

## 切换语言

启动 AIJ 时加 JVM 参数:

```
-Daij.lang=zh_CN     # 简体中文
-Daij.lang=zh_TW     # 繁体中文
                     # (不加该参数 = 英文,与未汉化前一致)
```

### 在哪里加

- **开发(`./gradlew aijRun`)**:在仓库根目录的 `devLaunchOptions.txt` 写一行 `-Daij.lang=zh_CN`(该文件被 git 忽略)
- **正式版**:编辑安装目录下的 `AstroImageJ.cfg`,加同样一行到 JVM 参数
- **IntelliJ Idea 调试**:Run Configurations → VM options 加 `-Daij.lang=zh_CN`

## 如何翻一个新对话框

只需 3 步,**不需要碰 Java 编译,只改 .java + .properties 两个文件**。

### 1. 找到要翻的字符串

在 `*.java` 里搜形如:
```java
gd.addCheckbox("Use RA/Dec to locate aperture positions", useWCS, ...);
gd.setTitle("Multi-Aperture Measurements");
IJ.showMessage("No image is open");
```

### 2. 给字符串一个 key,加进两个 .properties

`messages.properties`(英文,主索引):
```properties
multiap.use_radec = Use RA/Dec to locate aperture positions
```

`messages_zh_CN.properties`(中文):
```properties
multiap.use_radec = 使用 RA/Dec 定位孔径
```

**key 命名约定**:`<模块>.<用途>`,模块前缀:
- `multiap.*` — Multi-Aperture
- `apset.*` — Aperture Settings
- `mp.*` — Multi-Plot
- `dp.*` — Data Processor
- `cc.*` — Coordinate Converter
- `error.*` — 通用错误
- `menu.*` — 菜单(**慎用** — 菜单名是 macro 命令 ID)

### 3. 把 .java 里的字面量替换为 `I18n.t(key)`

```java
// 之前
gd.addCheckbox("Use RA/Dec to locate aperture positions", useWCS, ...);

// 之后
gd.addCheckbox(I18n.t("multiap.use_radec"), useWCS, ...);
```

如果 `import ij.I18n;` 不在文件顶部,自己加一行。`I18n` 在 `ij` 模块;`Astronomy_` 依赖 `ij`,所以两个模块的 `.java` 都能直接 `import ij.I18n;`(`ij` 包内的文件若已 `import ij.*;` 则无需再加)。

### 带参数的字符串

```properties
status.found_n_stars = Found {0} comparison stars (median {1} ADU)
```

```properties
status.found_n_stars = 找到 {0} 颗参考星(中位流量 {1} ADU)
```

```java
IJ.log(I18n.t("status.found_n_stars", n, median));
```

## 已翻译进度

| 对话框 / 模块 | 状态 | 备注 |
|---|---|---|
| **Multi-Aperture 主对话框** | ✅ 完成(~40 串) | 包含孔径形状、半径、循序帧、单步模式、checkbox 组 |
| Aperture Settings 偏好 | ⬜ 待翻 | 字符串少(~20),建议下一步做 |
| Multi-Plot 主窗口 | ⬜ 待翻 | 大量字符串(~200),建议分两批 |
| Multi-Plot Y-data 选择器 | ⬜ 待翻 | ~40 |
| Detrending 参数面板 | ⬜ 待翻 | ~30 |
| Transit Fit 面板 | ⬜ 待翻 | ~50 |
| Data Processor | ⬜ 待翻 | 只翻 bias/dark/flat tab ~80 |
| Coordinate Converter | ⬜ 待翻 | ~30 |
| 高频 error / status | ⬜ 待翻 | top 30 条 |

## 验证流程

1. 改完 .java 和 .properties 后,跑 `./gradlew :ij:compileJava :Astronomy_:compileJava` 验证语法
2. 跑 `./gradlew aijRun` 启动 AIJ,检查中文是否正确显示
3. 不加 `-Daij.lang=zh_CN` 再跑一遍,确认英文界面没变(向后兼容)

## 几个注意事项

### 字符宽度
中文每字宽 ≈ 2 个英文字符。原对话框列宽是按英文设的,
中文有可能 **截断或被压窄**。如果某个 label 显示不全,
可以在 `messages_zh_CN.properties` 里写更短一点(比如 "Fixed/Base radius of photometric aperture" → "测光孔径半径" 而不是 "固定/基础测光孔径半径")。

### 不能翻的地方
- 菜单项名(用作 macro 命令 ID)→ 翻了 macro 都会炸
- FITS header 字段、CSV 列名 → 下游脚本依赖
- 数值格式分隔符 → `IJU.locale = Locale.US` 必须留着,保证 "3.14" 而不是 "3,14"

### Tooltip 里的 HTML
```java
.setToolTipText("<html>If enabled, ...<br>...</html>")
```
HTML 标签照搬,只翻里面的文本。

## 发布流程

1. `./gradlew packageAijForMacOS`(或 Windows)生成中文化版本
2. 默认仍是英文行为(用户不加 `-Daij.lang=zh_CN` 看不到中文)
3. 给国内用户的发行包:
   - 修改 `AstroImageJ.cfg`,默认加上 `-Daij.lang=zh_CN`
   - 或者打两个包:`AIJ-x.x.x-zh.dmg` 默认中文,`AIJ-x.x.x.dmg` 默认英文
4. 配套出一份中文工作流 PDF:打开 → 校准 → 多孔径 → 出 CSV → 凌星拟合

## 长期维护

- 上游(`keastrid`)频繁提交,定期 `git pull` + rebase 即可
- 因为 i18n 改动局限于:
  - 新建文件 `I18n.java` + 2 个 .properties(不冲突)
  - `MultiAperture_.java` 里的字符串替换(可能小冲突,但容易解决)
- 上游若主动加 i18n,可以无缝迁移
