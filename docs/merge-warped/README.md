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
| `effects/WaterWade.java` | `waterFront` 层：水格角色脚边水线（跟随水面滚动、带斜度）；入水 `Splash` 水花；移动时在离开的格子放两圈水波；水深 BFS 分三档 | 英雄 `7px` / 小怪 `5px`；岸边 `×0.5`/0.62、中段 `×0.78`/0.78、深水 满厚/0.86；斜度 `≈1.1px` 随时间摆动；滚动 `5px/s` |
| `effects/WaterRipple.java` | 自绘水波：Pixmap 生成 32×32 三圈水波（3px 厚），放大 + 淡出；**颜色取自当前楼层水面贴图的平均色**（按颜色缓存） | `1.0→2.1` 倍、`0.85s`、第二圈延迟 `0.12s` |
| `effects/AmbientCritters.java` | 地面小虫（蚱蜢/甲虫/蜘蛛，`dungeon_critters.png`）跳 2~4 格后淡出；萤火虫（`critters.png` 光点帧，暖黄 hardlight）飘动呼吸 | 小虫每 `1.8~4.4s`、同屏 ≤2；萤火虫每 `3~7s`、同屏 ≤3；均不进 `mobs` |
| `scenes/GameScene.java` | 记录水面路径、创建 `waterFront` 层、每帧 tick 上述三者 | — |

与 Warped 原版的差距（可后续补）：

- 涟漪已改为自绘（`WaterRipple`：运行时 Pixmap 生成三圈水波，1.0→2.1 倍、0.85s 淡出，并延迟 0.12s 追加一圈小波），单独放在 `waterRipples` 层（地块之上、人物之下）；约 2 格宽，水面上清楚可见
- 没有 `sheen`/`subsurface` 双层与鱼影
- 小生物不会区分浅水/深水，也没有 Warped 的鱼、萤火虫、蝴蝶等完整图集
- 水面循环是整张纹理换帧，不是原版的调色板循环

运行时验证：临时场景 `Z_WaterFx`（跑完即删）在真实引擎里把英雄放进水格并截图，PASS 3/3，`waterlines drawn=5`、`critters spawned=9`、无异常。

## 水系统（fx-port 分支，已并入 ronghe）

搬运 Warped 4.x 的水面/涉水特效框架，天气与玩法耦合部分全部退化。

**搬进来的**
- `effects/fx/`：Fx 框架（Fx/FxBudget/FxEmitter/FxParticle/FxRing/FxFrames/FxRecipes/FxLight/FxFactories/FxDecal/
  FxModule/FxModules/Element/Lit/Ramp/Steps/TerrainWatch/FxCurves/FxRandom）
- `effects/fx/water/`：WaterWalk（踩水水线 collar + 涟漪 + 水花）、WaterFX、WaterGeom、WaterDrops（滴水）、
  WaterFalls（岸边/深渊边缘下淌）、Marks、WaterTerrain、WaterLive（水面 sheen 叠加层）
- `effects/Ripple`：换成 FxRing 版
- `GameScene`：FLOOR/SURFACE/LIGHT 图层、fxEmitter/lightEmitter、waterSheen 组、水面暗罩、昼夜色调
- `CharSprite`：fxSlots、afloat()/jumping()、stepped/afterDraw/gone 挂钩
- `actors/DayNightCycle`：黎明/白天/黄昏/夜晚（整轮 360s、开局偏移 62% 约等于黄昏→夜晚）

**刻意没搬**
- 天气：ClimateManager / PrecipType / StormStrikes / WeatherSprites / HearthLight（全部退化为常量）
- 音频：WaterVoices / WaterAmbience / Source（只留 pours 桩）
- 玩法：DayNightCycle 的怪物刷新/亡灵伤害/潜行加成、季节与真实时钟
- 其它特效模块：ArcaneFx / LightningFx / FireHeatFx / GasesColdFx / NatureHolyFx / CharactersFx / SkillsFx / EnchantArea

**调过的开关（都在 WaterLive / FxBudget）**
- `WaterLive.NO_CYCLE = true` —— 调色板循环会让水面出现 32px 网格感
- `WaterLive.DRAW_POCKS/DRAW_GLOW = false` —— pocks 在贴图边界形成横条纹
- `WaterLive.DRAW_SHORE = false` —— 有拼接图块后不需要程序化岸线
- `FxBudget.GOVERNOR = false` —— 帧时调压器默关；关掉前它会把 sheenA/B 降没，水面变平色块

**踩过的坑（都已在代码注释里）**
1. `Group.recycle` 不 revive 粒子 → 粒子不销毁、预算耗尽，特效"开场有、之后全没"（FxEmitter 覆写 recycle）
2. `FxBudget.frame()` 没人调用 → spawnsLeft 只减不增，同上症状（Fx.frame 每帧刷新）
3. sheen 层被降档关掉 → 水面变成一块平的矩形色块
4. 水格若不画拼接变体 → 每格水面满覆盖，池子变成方方正正色块（岸线是靠拼接图块画出来的）
5. 水面 sheen 必须挂在 terrain 内、water 之上、tiles 之下，靠"水格不画瓦片"自然遮罩

## 小动物（Warped DungeonCritterSprite，已并入 ronghe）

**搬进来的**
- `effects/CritterSprite.java`（885 行）：精灵基类、鸟类（Crow/Gull/Finch/Owl）、野兔、蝴蝶、鱼影
- `effects/DungeonCritterSprite.java`（1762 行）：青蛙、鼠妇、老鼠、蛾子、蜘蛛、蜗牛、甲虫、长脚虫、飞虫、栖鸟
- `effects/AmbientCritters.java`：改成 Warped 精灵的生成器（视野内 6~11 秒一只、上限 5 只、靠水出青蛙、水里出飞虫）

**桩**
- `WeatherSprites.visible()` 恒 true（天气没搬）
- `FishBrain`（鱼群 AI 没搬，只留 shade/dist/disturbances）
- `AmbientSound/AmbientPlayer/AmbientSounds`：静默（没有音频管线）
- `CaveMoteParticle`：简版飞尘
- `TextureFilm(int,int,int,int)`：手工尺寸图集构造（Warped 用法）

**没搬**
- DungeonLife 的完整生态（鱼群、天气生物、营火/火焰反应、捕虫网玩法、语音）

## 水下鱼影（FishShadows / FishBrain，已并入 ronghe）

- `effects/fx/water/FishBrain.java`：鱼的判断（何时被惊动、选方向、burst 速度曲线）
- `effects/fx/water/FishShadows.java`：水面下的黑影，最多 3 条（夜里 1 条），4~8 秒来一条，被主角惊到会散开
- 挂在 `GameScene.subsurface()`（water 之上、瓦片之下），由 `WaterLive.sceneCreated()` 建立
- 去掉了 `Place.SHORE`/Overworld 判定：本仓库只要这层有水面就允许鱼影

**说明**：Warped 原本只在主世界/海岸放鱼影，这里放宽到所有水面（下水道也有），
因为本仓库没有主世界。要只在特定关卡出现，改 `FishShadows.waters()` 一个方法即可。

## 水面氛围（WaterAmbience，已并入 ronghe）

- `effects/fx/water/WaterAmbience.java`（390 行）：浪花/岸边泡沫（lapping foam）、水面闪光（glints）、
  白色浪尖（whitecaps）、气泡（bubble，会破裂成小环）、冷水的浮冰渣/雾、岩浆的岸边辉光接口
- 由 `WaterLive.update()` 每帧驱动，视图范围按水面包围盒传入
- 天气相关（云量/风/降水/主世界海岸与海洋）全部退化为常量

**`WaterScenes` 没有搬**：它其实是 Warped 的**调试画廊**（把角色传送到预设场景看特效），
不是游戏里的水面转场，对玩法没用。
