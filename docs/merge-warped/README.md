# Warped 资源融合记录

来源：`Warped Pixel Dungeon 24.5.6`（包名 `xyz.gabriwar.warpedpixeldungeoo`，Google Play 版被重打包、补译中文的 APK）

- APK SHA-256：`509B61DC7096A7795920BD48F0F0F562C1BFF01280F5E9BD2F99318FDD1CB553`
- 上游工程：`github.com/GabriWar/warpedpd`（Shattered Pixel Dungeon 的 GPLv3 分支，SPD 4.x 时代）
- 本仓库基线：`Radish Pixel Dungeon 0.8.4-base2.4.2`（Shattered PD 2.4.2）

> 两者相差一个半大版本，资源与文本只能**按需挑选**，不能整目录覆盖。

## 已融合

| 日期 | commit | 内容 | 规模 |
|---|---|---|---|
| 2026-11-22 | `fd339d1a2` | `assets/environment/` 新增地块（新区域、冰面、水面循环、roofs 等） | 47 文件 / 740 KB |
| 2026-11-22 | `128183089` | `assets/{effects,mobs,sprites,interfaces}/` 新美术 | 270 文件 / 1.0 MB |
| 2026-11-22 | `230a2509a` | `assets/splashes/` 加载喷绘（含 `title/`） | 29 文件 / 3.9 MB |
| 2026-11-22 | `ff468baeb` | `assets/sounds/` 新音效（环境音、闷响、混响等） | 254 文件 / 4.0 MB |
| 2026-11-22 | `4f614511b` | `messages/**/*_zh.properties` 补译 | 280 条 |
| 2026-11-22 | `9eacb0cc6` | `messages/{skills,warpedrooms,homestead,bounty,sea,rarity,catching,drylands,guide}/` 中英文本 | 32 文件 / 855 KB |
| 2026-11-22 | `2f947be1d` | 剩余中文条目按 2.4.2 机制改写 | 53 条 |

以上资源目前**没有 `Assets` 常量或代码引用**，属于为后续系统移植预置的内容；注意 AGP 不会剔除 assets，打包体积会相应增加。

## 补译规则

只补「仓库有英文键、中文缺失」的条目，并且**仅当 APK 的英文原文与仓库英文逐字一致**时才采用其译文——否则说明机制在 4.x 里已经改过，套用会写出与 2.4.2 行为不符的描述。

按此规则：候选 335 条 → 首轮采用 280 条 → 剩 55 条（`actors` 26、`items` 27，另有 2 条英文本身为空）需要处理。

第二轮对这 55 条逐个处理：

- **5 条**（`veterans_intuition`、`adventurers_intuition`、`focused_meal`、`weapon_recharging`、`liquid_nature`）差异仅是多出一个 `_+3:_` 阶，剔除该段后沿用原译法；
- **48 条**按 2.4.2 英文重写数值与机制（例：`liquid_willpower` 2.4.2 是「回复破损纹章护盾的 50%/75%」而非 4.x 的「最大生命值 6.5%/10%」；`combined_energy` 是「回复已消耗内力的 50%」而非 4.x 的「回复 1 点」）；
- **2 条**（`sickle` / `warscythe` 的 `stats_desc`）英文为空，保持不添加。

改写结果用脚本校验：数字集合、`_` 斜体标记数、`\n\n` 段数、`%d`/`%s` 占位符必须与英文一一对应（53 条中 52 条通过，`weapon_recharging` 的告警为中文「1点」对英文「one」的误报）。

## 未融合及原因

| 项 | 原因 |
|---|---|
| 14 个同名地块图集（`tiles_sewers/prison/city/halls/caves*`、`terrain_features`、若干 `custom_tiles`） | 图集高度 256 → 288、`tiles_caves` 由单图变叠图，需先移植 `DungeonTileSheet` 的行布局与取图逻辑 |
| `assets/fonts/pixel_font.ttf`（59,828 → 67,260） | 字体版本不同，字宽/行高会影响现有 UI 排版，需实测后再替换 |
| `assets/gdx/{textfield.atlas,textfield.json,cursor_mouse.png}` | 来自 libGDX 1.14（仓库为 1.11），属框架资源版本差异，无移植价值 |
| `assets/music/` | APK 中 31 首曲目仓库已全部具备 |
| 代码内写死的中文 UI（约 90 条） | 对应 Warped 的多人联机/天象盘/天气面板，需连带移植这些系统 |
