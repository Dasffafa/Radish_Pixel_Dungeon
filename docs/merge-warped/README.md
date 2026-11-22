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
| 2026-11-22 | `77afb7a47` | 六张区域地块贴图换为 4.x 版（sewers/prison/city/halls/caves_crystal/caves_gnoll） | 6 文件 |
| 2026-11-22 | `b91a9582a` | 换图后把自绘的 (5,0) 格还原（六张图仅此一格与官方 2.4.2 不同） | 6 文件 |

除最后一项外，其余资源目前**没有 `Assets` 常量或代码引用**，属于为后续系统移植预置的内容；注意 AGP 不会剔除 assets，打包体积会相应增加。

地块替换的安全性依据：两版 `DungeonTileSheet` 的 109 个格子索引常量逐一比对完全一致（0 处偏移），4.x 新增内容只落在第 16 行之后的追加行，`TextureFilm` 按实际尺寸切格，因此替换后原有索引取到的仍是同类地块。另外本仓库高台/垂饰逻辑用到的索引 257~271（第 17 行）原先在 256 高的图集里取不到，换成 288 高后补齐。

**运行时验证**：临时场景 `Z_EnvVerify`（跑完即删）用真实引擎依次渲染第 1/6/11/16/21 层并截图，结论 PASS（5/5 检查通过，无异常）；矿洞层未换图，作为对照组前后截图一致。

**视觉影响需要知晓**：这是把既有区域换成 4.x 画风，不是叠加素材。最明显的是城市层地面由红棕金点变成深灰石板（详见对照图）。若希望保留 2.4.2 原画风，只需回退 `77afb7a47` 与 `b91a9582a` 两个提交，新增的环境素材（`fd339d1a2`）不受影响。

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
| `tiles_caves.png`（256×256 → 256×1728） | 4.x 把洞穴拆成多套叠图，取图逻辑不同，需单独移植 |
| `terrain_features.png`（256×128 → 256×368） | 4.x 按阶段扩充到 23 行且前 8 行内容大改，直接覆盖会让植物/陷阱贴图错位 |
| `custom_tiles/{caves_boss,city_boss,caves_quest,prison_exit,prison_quest,weak_floor}.png` | 内容或格位含义不同（`caves_quest` 宽高对调），属各房间专用贴图，需逐个核对 |
| `assets/fonts/pixel_font.ttf`（59,828 → 67,260） | 字体版本不同，字宽/行高会影响现有 UI 排版，需实测后再替换 |
| `assets/gdx/{textfield.atlas,textfield.json,cursor_mouse.png}` | 来自 libGDX 1.14（仓库为 1.11），属框架资源版本差异，无移植价值 |
| `assets/music/` | APK 中 31 首曲目仓库已全部具备 |
| 代码内写死的中文 UI（约 90 条） | 对应 Warped 的多人联机/天象盘/天气面板，需连带移植这些系统 |
| 天气系统、新怪物 | 按需求明确不移植 |

## 环境表现（轻量版，非 Warped 原实现）

Warped 的环境表现是一整套 `effects/fx` 框架（`FxModule` 钩子 + `WaterWalk.afterDraw` + `WaterLive` 调色板循环 + `CritterSprite`/`DungeonCritterSprite`，20+ 文件、3000+ 行，且与天气/昼夜耦合）。本仓库改用一个不依赖天气系统的最小实现：

| 文件 | 作用 | 关键参数 |
|---|---|---|
| `effects/WaterCycle.java` | 把 `environment/waterN_cycle.png`（8 帧）切成 8 张 32×32 单帧，按时间轮换给水面 `SkinnedBlock` | `FRAME_TIME = 0.18s` |
| `effects/WaterWade.java` | 在 `mobs` 之后新建 `waterFront` 层，为水格中的角色盖一条水线；取样条带跟随水面滚动；入水/移动时在离开的格子放两圈自绘水波 | 英雄水线 `7px`、小怪 `5px`、透明度 `0.86`、滚动 `5px/s` |
| `effects/WaterRipple.java` | 自绘水波：Pixmap 生成 32×32 三圈水波（真实 3px 厚），放大 + 淡出 | `1.0→2.1` 倍、`0.85s`、第二圈延迟 `0.12s` |
| `effects/AmbientCritters.java` | 地面小虫（蚱蜢/甲虫/蜘蛛，`dungeon_critters.png`）跳 2~4 格后淡出；萤火虫（`critters.png` 光点帧，暖黄 hardlight）飘动呼吸 | 小虫每 `1.8~4.4s`、同屏 ≤2；萤火虫每 `3~7s`、同屏 ≤3；均不进 `mobs` |
| `scenes/GameScene.java` | 记录水面路径、创建 `waterFront` 层、每帧 tick 上述三者 | — |

与 Warped 原版的差距（可后续补）：

- 涟漪已改为自绘（`WaterRipple`：运行时 Pixmap 生成三圈水波，1.0→2.1 倍、0.85s 淡出，并延迟 0.12s 追加一圈小波），单独放在 `waterRipples` 层（地块之上、人物之下）；约 2 格宽，水面上清楚可见
- 没有 `sheen`/`subsurface` 双层与鱼影
- 小生物不会区分浅水/深水，也没有 Warped 的鱼、萤火虫、蝴蝶等完整图集
- 水面循环是整张纹理换帧，不是原版的调色板循环

运行时验证：临时场景 `Z_WaterFx`（跑完即删）在真实引擎里把英雄放进水格并截图，PASS 3/3，`waterlines drawn=5`、`critters spawned=9`、无异常。
