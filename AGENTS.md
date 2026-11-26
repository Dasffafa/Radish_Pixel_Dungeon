# Radish Pixel Dungeon 项目指南

## 项目概述
Radish Pixel Dungeon 是基于 Shattered Pixel Dungeon 的 roguelike 游戏。

## 编译验证
你可以进行编译验证，但不要运行:desktop:debug类会弹出游戏窗口的命令。
你可以直接进行修改，但是修改完成要给用户一个报告，告诉用户问题产生的原因和你的解决方案。
不要修改SPD-classes中的东西.

## 注释规范：
1. 本项目不使用javadoc，因此，你可以使用javadoc中一些比较好的功能（如ref，参数注解），但是不要使用html标签，这对人类来说是降低可读性。
2. 少即是多：严禁为显而易见的代码（如简单的getter/setter、标准的API调用、命名清晰的自解释逻辑）添加注释。
3. 禁止废话：严禁生成类似 // 定义一个变量、// 循环遍历数组、// 返回结果 这种复述代码字面意思的无意义注释。
4. 保留核心：只允许在以下场景编写注释：
   • 极其复杂的业务逻辑、非直观的算法或数学公式（解释“为什么这么做”，而不是“做了什么”）。
   • 关键的架构设计约束、避坑指南（Invariant）或临时的 Hack/Workaround 变通方案。
5. 语言要求：保持与项目一致的语言（除了event包之外，采用全中文），风格简洁、客观。

## Android 双版本打包
- **正式版**：`bash gradlew :android:assembleRelease`（使用 `android/keystores/release.jks`）
- **测试版（beta）**：`bash gradlew :android:assembleBeta`（使用 `android/keystores/beta.jks`，包名自动追加 `.beta`，应用名为 `Radish Pixel Dungeon Test Version`，图标带扳手角标）
- **签名密码配置**：从 `android/.env`（已被 `.gitignore`）读取 `KEYSTORE_PASSWORD`；首次缺失时可通过 `-PkeystorePassword=<密码>` 传入并自动写入 `android/.env`。
- **Firebase 配置**：`release` 版启用 Firebase，打包前会强制校验 `android/google-services.json`（已被 `.gitignore`）是否存在；`beta` 版不会引入Firebase文件。
