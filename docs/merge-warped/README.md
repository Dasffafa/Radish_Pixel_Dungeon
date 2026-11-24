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

## 环境音 + 火焰反应（已并入 ronghe）

**环境音**
- `levels/ambience/AmbientSound.java`：仓库里实际存在的 amb_*.mp3（splash/lap/frog/rat/fly/skitter/lava/drip）
- `levels/ambience/AmbientPlayer.java`：用 `Sample` 播放，支持距离衰减、左右声像、随机变调
- `levels/ambience/AmbientSounds.java`：同时最多 4 个环境音
- `effects/fx/water/WaterVoices.java`：视野里有水时每 2.5~6 秒一次拍岸声
- 小动物的 `voice()` 现在真的会响（青蛙叫、鼠妇窜、水滴等）

**火焰反应**
- `AmbientCritters`：每 0.3 秒刷新一次 `Fire` blob 的格子，1 格内有火的小动物会 `scare` 并 1.2 秒内淡出

**说明**：Warped 原版用 Android SoundPool + 自己的声像/变调实现；这里用引擎自带的 `Sample`，
声像用左右声道音量近似，变调用 playback rate，效果接近但不完全相同。

## 环境音床 + 互动音效（补充）

- `levels/ambience/LevelAmbience.java`：按关卡类型的环境音床
  - 下水道：滴水 / 拍岸 / 老鼠 / 飞虫
  - 监狱：金属滴水 / 滴水 / 爬动 / 老鼠
  - 洞窟：滴水 / 爬动   城市：金属滴水 / 老鼠
  - 殿堂：岩浆 / 滴水   冰层：落叶滴水
  - 每 3~18 秒一次，音量 0.18~0.40，随机声像与 ±6% 变调
- 互动音效：踩水三步都有声音（入水 0.55 / 水中走 0.28 / 出水 0.32 / 跳落 0.70），只有主角触发
- 与 `WaterVoices`（拍岸声）共用 `AmbientSounds` 的 4 声道上限，不会互相淹没

## 其它环境互动

| 互动 | 状态 |
|---|---|
| 踩水（水线/涟漪/水花/涉水声） | 已搬（WaterWalk + WaterFX） |
| 踩草飞草屑 + 草声 | 已有（Warped WaterWalk.footstep；`Z_FxGrass` 验过粒子 10→13） |
| 出水湿脚印 | 新加（`FxDecal.wet` 落在出水落脚处） |
| 物品落水（水花 + 落水声） | 新加（`Level.drop` 里判水格） |
| 火堆噼啪声 | 新加（`LevelAmbience`：主角 6 格内有火时每 2.5~6 秒一声） |
| 水下鱼影 / 屋顶滴水 / 岸边水帘 | 已搬 |
| 小动物（生成/吓跑/怕火/叫声） | 已搬 |
| 雪地踩雪 | 不适用（本仓库没有雪地形） |
| 天气（雨雪雾） | 未搬 |

## 火光水面倒影 + 洞穴飞尘

- `effects/fx/water/FireReflections.java`：火格附近（下/左/右 3 格内）的水面画一条暖色叠加光带，
  随波动横向晃 ±1.2px、明暗呼吸；最多 4 条，挂在 waterSheen 层（水面上、瓦片下）
- `effects/AmbientMotes.java`：洞窟/殿堂里每 0.45~1.35 秒飘起一粒 `CaveMoteParticle`，
  在主角周围 11x9 格内随机、避开墙
- 两者都由 GameScene 每帧驱动；WaterLive 里也调一次 FireReflections 以保证顺序

实测：在水边点一堆火 → 倒影 shown=1；进 11 层洞窟 400 帧 → 飞尘 30 粒

## 分区域环境音/特效

| 区域 | 环境音床 | 视觉 |
|---|---|---|
| 下水道 1-5 | 滴水 / 拍岸 / 细流 / 水咕噜 / 老鼠 / 飞虫 | 水系统全套、小动物 |
| 监狱 6-10 | 金属滴水 / 铁链吱呀 / 滴水 / 爬动 / 老鼠 / 远处呻吟 | 浮尘（少量） |
| 矿洞 11-15 | 滴水 / 碎石 / 洞风 / 蝙蝠 / 爬动 | **洞穴飞尘** |
| 城镇 16-20 | 铁砧 / 钟鸣 / 吱呀 / 金属滴水 / 低鸣 | — |
| 地狱 21+ | 岩浆 / 轰隆 / 低语 / 呻吟 / 噼啪 | **飘火星** |

- 音床按关卡类名优先判定，其次按 `Dungeon.depth` 分段兜底
- 换层时 `AmbientSounds.reset()`，避免声道占用跨层累计导致"永远算满"
- 城镇：本仓库是 Dwarven Metropolis（无独立城镇关卡），Warped 的暴风城/城镇属于主世界，没有搬

## 附魔特效（自建，按 Warped 的视觉语言）

Warped 的附魔特效体系（`effects/fx/enchant/*`，约 30 个文件、上万行，且和它自己的
enchant/rarity/quality 类强绑定）没有整搬，而是在现有 fx 工具上做了本分支版本：

- `effects/fx/enchant/EnchantFx.java`
  - `colorOf(Enchantment)`：本分支 40+ 种附魔按类名归到 9 个属性族取色
    （火/冰/雷/毒/暗/血/光/水/物理），无附魔 → 无粒子
  - `trail(...)`：飞行拖尾，每 0.035s 一粒 MOTE，带反向速度与淡出
  - `burst(...)`：命中时 5 粒 STAR/SPARK 向外炸开
- `MissileSprite`：`update()` 里按附魔颜色撒拖尾，`onComplete()` 里放命中爆点；
  弓箭取主角弓的附魔，其它投掷物取自身附魔

**没做的**：物品在地上/装备时的光环（Warped `ItemAura`）、每种附魔的专属形状、
附魔触发时的元素特效（火焰/冰霜/闪电本体）。

## 附魔特效（三项补齐）

1. **物品光环** `effects/fx/enchant/ItemAura.java`
   - 地上已发现的附魔武器：每 0.45s 冒一颗同色星点（最多同时 6 件）
   - 角色手持附魔武器：脚边持续冒小点
2. **按族分形状** `EnchantFx.Family`
   - 火→火星(SPARK_3)/冰→霜晶(STAR4_5)/雷→电弧(SPARK_2)/毒→绿点(MOTE_2)/
     暗→紫星(STAR4_7)/血→红点/光→金星/水→青点/物理→白刃
   - 拖尾与爆点各用各的帧，爆点数量也按族变（火/雷 7 粒，其它 5 粒）
3. **附魔触发** `Weapon.proc()` → `EnchantFx.proc()`
   - 命中并触发附魔时，在目标身上放该族的爆点 + 特征粒子
   - 火：向上飘的火星；冰：霜晶；其它：同族星点
   - 只在敌我阵营不同触发，治疗/增益类不会乱放

实测：火附魔剑 → 光环 29 粒；proc 一次触发 1 次

## 附魔特效：偷来的配方（EnchantRecipes）

从 Warped 的 `EmberFx / RimeFx / StormFx / VenomFx / DreadFx` 里提取**帧、颜色、速度曲线**，
去掉它的 tier/echo/curse/tally 框架，重写成 `effects/fx/enchant/EnchantRecipes.java`：

| 族 | 偷来的美术（同一张 fx_sheet） |
|---|---|
| 火 | 火舌 `TONGUE_S/TONGUE_M`（12fps，ramp `FxFactories.EMBER`，vel -12/-16）、火星 `EMBER_1/2`（polar 26、drag 3、gravity -10）、地面焦痕 `SCORCH_M`（`FxRecipes.SCORCH_COLOR/HOLD/FADE`） |
| 冰 | 白闪 `PLUS_5`、霜雾 `PUFF_S`+`Element.FROST.smoke`、冰碴 `SHARD_ICE/SHARD_GLASS`（polar 30-60、gravity 160、spin、弹跳一次）、地面霜迹 `RIME[1]`（hold 2s / fade 1s） |
| 雷 | 电弧 `SPARK_2`（orient + slow MOTE_1、ramp `Element.ELECTRIC.ember`、gravity 120、drag 1.2、弹跳）、中心闪 `SPARK_3`、地面焦刻 `ETCH`（0x2A2A40） |
| 毒 | 毒滴 `DROP_3`（gravity 240）、绿雾 `PUFF_S`+`Element.SMOKE.smoke`、地面蚀痕 `ETCH`（0x2A3A1E） |
| 暗 | 墨雾 `INK_WISP`（3fps、smooth）、暗核 `INK_CORE`（1.2s） |

**自己设计的部分**：血/光/水/物理四族仍用通用爆点；物品光环（ItemAura）、
飞行拖尾、按族取色表都是自建。

**没搬的**：tier（WEAK/MID/STRONG）分级、echo/curse 变体、tally 预算、
每种附魔的独立配色（Warped 是逐附魔定义 look，我们按族归纳）。
实测：五族各触发一次，无异常（Z_FxEnch5）

## 逐附魔独立配色

`EnchantFx.COLORS`：46 个附魔类逐个配色（不再是"同族同色"）：

- 火：Blazing FF7A1E / BlazingFury FF3A10 / Meteor FFB040 / Rainstorm FF9A5A ...
- 冰：Chilling 9FE4FF / Crystal D6F4FF / Flawless E8FBFF ...
- 雷：Shocking FFF25A / Resonance FFE07A / Pulse FFD24A ...
- 毒：Corrupting 9CE87A / Blooming 7AE86A / Lamprey 8AD06A ...
- 暗：Grim B48AFF / VoidRift 8A5AE0 / Eclipse 6A3AC0 / Unstable E070FF ...
- 血：Vampiric FF5A6A / SoulRend FF3A5A / Reaping E04060
- 光：Lucky FFD86A / Midas FFE890 / Lamp FFF4B0 / FlowingLight FFF0C8 ...
- 水：WetEnchantment 7AD6D6 / Reversal 9AE0D0 / Survival 8AD0C0
- 物理：Blocking E8E8F0 / Clash F0F0F8 / Kinetic D0D0E0 ...

查表优先，未收录的类名回落到属性族颜色。拖尾/光环/爆点都用逐附魔色；
命中配方仍是族级（火就是火舌/火星，只是主色按附魔微调留给后续）。

实测：46/46 全部有独立配色，46 种颜色互不相同。

## 命中配方主色按附魔微调

`EnchantRecipes.fire/ice/storm/venom/dark` 全部改成接收 `tint`（逐附魔色）：

- 火舌用附魔本色，火星用提亮 35% 的同色；焦痕仍用 `FxRecipes.SCORCH_*`
- 霜雾/冰碴用附魔本色（玻璃碴再提亮 60%），霜迹用压暗 35%
- 电弧/中心闪用附魔本色（闪提亮 50%）
- 毒滴用本色，绿雾压暗 35%
- 墨雾用本色，暗核压暗 45%
- 新增 `light(color,k)` / `dark(color,k)` 两个比例混色工具

效果：Blazing 与 BlazingFury 的火、Chilling 与 Crystal 的霜，现在连命中时的火焰/冰碴
主色都不同（之前只有拖尾/光环/爆点不同）。

## 附魔框架：tier / echo / curse

`EnchantFx.Ctx` 把 Warped 的框架级差异补上（它原版靠 tier/tally，我们按本分支能拿到的信号推断）：

- **tier**（0 弱 / 1 中 / 2 强）：由 `Weapon.proc` 收到的伤害判定（<8 弱、8–15 中、≥16 强）
  - 火舌 1/2/3 条、火星 2/4/6 粒；冰碴 2/4/6、霜雾 1/2/3；电弧 2/3/4 条；毒滴 2/3/4
  - 地面痕迹（焦痕/霜迹/焦刻/蚀痕）只在**中/强**出现
- **echo**（回响）：同一目标 **0.4 秒内**再次触发 → alpha ×0.5、预算减半、tier 降一档、不留痕迹
- **curse**（诅咒武器）：`Weapon.cursed` → 颜色走 `curseTint()`（去饱和 + 偏暗紫）
- **预算**：每次触发 18 粒子（回响 9），超了自动停

计数：`EnchantFx.weaks/mids/strongs/echoes/curses` 便于验收。
实测：弱1/中1/强3/回响4/诅咒1，四条断言全过。

## 萝卜特色附魔的专属特效（自设计）

`effects/fx/enchant/SpecialFx.java`：8 个本分支独有附魔的专属表现，
按类名精确匹配，命中时优先于族配方：

| 附魔 | 表现 |
|---|---|
| FlowingLight 流光 | CORE_FLASH 中心闪 + 递增光环 + 向上收束的光点 |
| Crystal 结晶 | PLUS_5 白闪 + 方形冰晶 SQ_4 呈放射状崩开（自旋） |
| Pulse 脉冲 | 三层 RING 递进扩散环（按 tier 叠 1~3 层） |
| VoidRift 虚空裂隙 | INK_CORE 暗核 + 墨雾向内吸附（drag 减速） |
| SoulRend 裂魂 | CROSS_5 十字裂口 + 魂点上飘（drag） |
| Meteor 陨星 | 从上方 26px 落下的 STREAK_FLAME 火痕 + CORE_FLASH 落地 + SPARK 溅射 + 焦痕 |
| Rainstorm 骤雨 | 多个 DROP_3 斜向斜落 + 落点小环 |
| Sweeping 横扫 | 递增宽环模拟横向斩击弧 + 中心十字闪 |

- 全部复用 fx_sheet 的帧，靠帧组合/运动/颜色区分
- 同样受 tier（数量）/ echo（不留痕迹、alpha 减半）/ curse（配色）影响
实测：八种各触发一次，无异常（Z_FxSpecial）

## 附魔特效可读性增强

原版帧在 16px 图块下太小，实测不明显，做了两件事：

1. **粒子放大 1.8×**：`EnchantFx.boost(p, k)` 缩放粒子并补偿左上原点偏移
2. **命中大闪光**：每次触发在目标位置闪一下 `PLUS_5`（放大 2.4×、0.16s、P0 优先级、颜色为附魔色提亮 55%）

调参入口：
- 放大倍数：`boost(..., 1.8f)`（EnchantRecipes / SpecialFx 里）
- 闪光强度：`bigFlash()` 里的 `2.4f` 与 `0.16f`
- 粒子数：各配方按 tier 的 `2/4/6` 一类数字

## 击杀特效（按附魔）

`EnchantFx.proc` 里判定「这一击是否致命」（`damage >= defender.HP && alive`），
致命时先放一套击杀特效，再放普通命中特效：

| 族 | 击杀表现 |
|---|---|
| 火 | 大号火舌 TONGUE_L + 7 粒上飘火星 + 大焦痕 |
| 冰 | 8 片冰碴/玻璃碴全身炸开（带弹跳）+ 最大霜迹 |
| 雷 | 6 道电弧乱窜 + 烟团 + 焦刻 |
| 毒 | 6 滴毒液 + 3 团绿气 |
| 暗 | 大暗核 + 向内收的环 + 5 条墨雾 |
| 其它 | 5 粒星点爆开 |

特色附魔另有专属击杀（`SpecialFx.finisher`）：

| 附魔 | 击杀表现 |
|---|---|
| 流光 | 大环 + 8 粒上升光点 |
| 结晶 | 白闪 + 8 片方形冰晶放射 + 霜迹 |
| 脉冲 | 三层大环依次扩散 |
| 虚空裂隙 | 大暗核 + 收束环 + 7 条墨雾 |
| 裂魂 | 大十字 + 8 粒魂点上升 |
| 陨星 | 大闪光 + 10 粒火花 + 大焦痕 |
| 骤雨 | 12 滴暴雨 + 大环 |
| 横扫 | 大宽环 + 十字闪 |

计数：`EnchantFx.kills`、`SpecialFx.finishers`。
实测：致命一击触发 1 次、其中特色分支 1 次，PASS。

## 附魔装备高光（UI，参数抄自 APK 反编译）

用 jadx 反编译 APK 后找到真正的实现：`effects/fx/enchant/ItemAura.streak()`：

```java
float mod = Profile.mod( t, 2.0f );          // 2 秒一个周期
if (mod >= 0.1f) return false;               // 只在前 0.1 秒出现
quad.frame( FxFrames.STREAK_TAPER );         // 帧 167,98,16,5
quad.hardlight( enchantLook.glow );          // 附魔光色
quad.angle = -45.0f;                         // 斜 45°
quad.scale.set( 0.85f, 0.6f );
quad.am = (mod < 0.05f ? 0.9f : 0.45f);      // 前 0.05 秒更亮
```

我们照抄到 `ui/ItemSlot`：同样 2 秒周期、0.1 秒可见、`STREAK_TAPER`、-45°、
缩放 (0.85, 0.6)、alpha 0.9/0.45，颜色用 `EnchantFx.uiColor(item)`。

**没搬的**：Warped 的 ItemAura 还有 ring/mote/glint 多种 motif、按稀有度决定数量、
以及 `stillRing`（RING_5 在物品脚下脉动）、`vicious`（剑身火花）等；这些依赖它的
EnchantLook/ItemLooks 框架，暂未搬。

## ItemAura 的另外三种 motif（抄自 APK 反编译）

反编译源码：`E:\work-2\radish-merge\apk-src\sources`（jadx）。照抄到 `ui/ItemSlot`：

| motif | 抄来的参数 |
|---|---|
| **streak** | 2s 周期，仅前 0.1s；`STREAK_TAPER`；-45°；scale(0.85,0.6)；alpha 0.9/0.45 |
| **stillRing** | 2s 周期，前 0.53s；`RING_5`；scale `1 + 0.8*min(1, mod/0.25)`；alpha 0.55→0.8→回落的曲线；贴在图标脚下 |
| **vicious** | 0.8s 周期，前 0.12s；`SPARK_2`；+45°；scale(0.9,0.7)；alpha 1→0 |
| **边框火花** | 15 个 1px 块（5 火花 × 3 连）；沿格子边框按 `placeOnFrame` 跑动；速度 6~16 px/s（随机方向）；拖尾 alpha 1/0.6/0.3 |

差异说明：APK 里火花数量由**稀有度**（Quality/Rarity）决定，本分支没有稀有度系统，
改用**物品升级等级**近似：`2 + min(3, level/2)` 个火花；白板且 level<=1 不显示。

**修正记录**：边框火花的子节点用的是**场景绝对坐标**，光用 `0..width` 定位会跑到别的格子里；
`placeOnFrame` 里已补上 `this.x / this.y` 偏移（与 `ItemSlot.layout()` 里 `sprite.x = x + ...` 的约定一致）。

### 静态边框（qualityFrame）已补

APK 的 `ItemSlot.layoutQuality()` 里除了跑动的火花，还有**四根 1px 的静态边框线**：

```java
frameL = x + margin.left;
frameT = y + margin.top;
frameW = width - margin.left - margin.right;
frameH = height - margin.top - margin.bottom;
frame[0].size(frameW, 1)  at (frameL, frameT);              // 上
frame[1].size(frameW, 1)  at (frameL, frameT + frameH - 1); // 下
frame[2].size(1, frameH)  at (frameL, frameT);              // 左
frame[3].size(1, frameH)  at (frameL + frameW - 1, frameT); // 右
alpha = qualityAlpha * 0.35;
sendToBack(frame); bringToFront(sparks);
```

已按同样方式实现；火花也从"格子边缘"改为沿**这个内框**跑（`placeOnFrame` 用 `frameL/frameT`）。

与原版仍不同的两点：
- 边框颜色：原版用 `quality.titleColor()`（稀有度色）；本分支没有稀有度 → 用附魔色，无附魔时用冷白
- 火花数量：原版按稀有度 2~5；我们用 `2 + min(3, level/2)`

**关掉仍显突兀的两种**：`stillRing`（脚下 RING_5）和 `vicious`（刀身 SPARK_2）在 16px 格子里
缩放后像两块糊上去的色斑，已默认关闭；需要时把 `ItemSlot.ENABLE_RING / ENABLE_VICIOUS` 改 true。

## 按附魔族的图标扫光（自设计，替代原版统一 streak）

用户要求：不要统一斜光，改成"烈焰一阵火光闪过、电击一阵电弧划过"这种按附魔区分：

| 族 | 扫光帧（fx_sheet） |
|---|---|
| 火 | `STREAK_FLAME`（火舌条） |
| 雷 | `SPARK_2`（电弧） |
| 冰 | `STAR4_5`（霜芒） |
| 毒 | `DROP_3`（毒滴） |
| 暗 | `INK_WISP`（墨雾） |
| 血 | `MOTE_2` |
| 光 | `STAR4_7` |
| 水 | `DROP_2` |
| 物理 | `STREAK_TAPER`（默认斜光） |

- 每 **2.6 秒**扫一次，单次 **0.34 秒**，从图标左上扫到右下，`angle=-35°`，sin 淡入淡出
- 后面跟 2 个 `MOTE_1` 尾点（alpha 0.55/0.40 递减）
- 颜色仍用逐附魔配色
- 原版统一的 `streak` 已替换；`stillRing`/`vicious` 仍默认关闭

**修复：空格子残留光效**。`item(null)` 分支原来没调 `updateAura()`，物品被拿走后边框/火花会留在空格子上；
现在清空即刷新，并且 `update()` 在 `item==null`、不可见或尺寸未布局时直接 `hideAll()`。

**修复：扫光溢出到相邻格子**。扫光/尾点原来按 `sprite.x + u*(w+2) - w/2` 摆放，末端会伸到图标外；
现在 `placeSweep()` 用 `clamp` 把位置严格夹在图标矩形内（`x ∈ [sprite.x, sprite.x + iw - sw]`），
尾点同理。

**再修：旋转导致溢出**。扫光原来带 `angle=-35°`，旋转是绕贴图左上角做的，实际绘制区域会超出
轴对齐边界（所以夹住 x/y 也仍会探出格子）。现在扫光**不旋转**，并把夹取范围再内缩 2px。
另外验证了格子复用链：附魔装备 → 普通卷轴 → 清空，三种状态都不会残留光效（`Z_FxReuse` 6 条断言）。

**改成每帧判定**：不再依赖"换物品时调用 updateAura"这条链路，`ItemSlot.update()` 每帧用
`wantAura()`（`uiColor != 0 || item.level() > 1`）重新决定边框/火花/扫光的可见性，
任何漏掉刷新的路径都会在这一帧被清掉。

## 弓的持续光效（已按用户要求移除）

试做过 `effects/BowAura`（环绕光点 + 自然之力绿叶 + 弓本色箭矢拖尾），但本分支里弓通常
装不上（Huntress 开局是 Gloves，SpiritBow 之外没有可装备的弓），用户选择**去掉环绕光环**。

保留下来的：

- **任何投掷物都有拖尾**：`EnchantFx.missileColor(item)` 保证非 0——
  飞镖 `0xC8D8F0`、飞刀 `0xE0C8A0`、空/默认 `0xE8E4C8`；有附魔则用附魔色
- **背包里弓/弩图标**：扫光放大到 1.45×

## 分附魔拖尾（投掷物）

`EnchantFx.trailFor(x, y, item, vx, vy)`：投掷物沿轨迹每 0.03s 留一次，**按附魔/物品种类换帧**：

| 族 | 主拖尾帧 | 余屑帧 |
|---|---|---|
| 火 | `STREAK_FLAME` | `EMBER_1` |
| 冰 | `SHARD_GLASS` | `MOTE_1` |
| 雷 | `SPARK_2` | `MOTE_1` |
| 毒 | `DROP_3` | `DROP_2` |
| 暗 | `INK_WISP` | `MOTE_2` |
| 光 | `STAR4_7` | `MOTE_1` |
| 血 | `MOTE_2` | `MOTE_1` |
| 物理/其它 | `STREAK_TAPER` | `SPARK_2` |

- 主拖尾：放大 1.2~1.5 倍、颜色提亮 25%、半衰 0.22s、有速度拖拽
- 55% 概率再撒一个余屑（带随机横向速度 + drag）
- 无附魔时用 `missileColor(item)` 给本色（飞镖冷银/飞刀暖铜/箭木白）

**拖尾对齐飞行方向**：主拖尾按速度算单位向量，贴在弹体后方 2px，`angle = atan2(vy,vx)`（帧本身是横向的），
并沿运动方向把 x 拉长 1.8 倍；余屑也按后向 + 横向散布，不再是"横着飘"的一排。

## 符号拖尾（照 Warped 的轨迹表现）

用户给了 Warped 实录截图：飞出去的投掷物后面是一串**等距符号**——亮加号、橙色圆环交替，
停在原地慢慢淡出，而不是连续拖影。已照此重做：

- `MissileSprite` 按**飞行距离**每 **6px** 放一个符号（不是按时间）
- 交替使用 `PLUS_3`（提亮 45%，白亮）与 `RING_5`（附魔本色，如烈焰=橙）
- 静止符号（不给速度），`life` 0.55s / 0.7s，alpha 从 0.95 / 0.8 淡出
- 颜色：附魔色优先，无附魔用 `missileColor(item)` 本色

**符号拖尾对齐 Warped（按第二段实录）**：录像里是"绿亮头部 + 橙圆环 + 白加号"交替、间距约 1 格、
停留约 1 秒后淡出。已调整为：

- 间距 **12px**（原来 6px 太挤）
- 圆环：`RING_5`、`boost 1.4`、**压扁成 1.5×0.9 椭圆**、停留 **1.1s**
- 加号：`PLUS_5`、`boost 1.1`、颜色提亮 80%（近白）、停留 **0.9s**
- 头部每落一个符号同时闪一次 `CORE_FLASH` 亮点（对应录像里弹体的绿色亮芒）

**符号改成固定双色**（照录像）：圆环 `RING_5` 固定暖橙 `0xFF9A3C`、压扁到 1.7×0.62；
加号 `PLUS_3` 纯白、1.2×；头部亮芒仍用物品本色。

**符号留在弹体后方**：之前符号生成在弹体当前位置，看起来像"有东西跟着箭飞"。
现在沿这一帧的线段按 12px 等间距补符号（`while (markDist>=12) markDist-=12`），
并且从弹体位置**反向偏移 7px+余量**，保证符号始终在箭后面排成一条线。

## 残影（ghost）拖尾——照 Warped 的 EnchantTrail

反编译找到真正的拖尾类：`effects/fx/enchant/EnchantTrail extends Image`，
常量是 `GHOSTS = 4`、`GHOST_ALPHA = 0.35`、`GHOST_LIFE = 0.3`、`SHED_PX = 12`、`DOT_HANG = 0.4`。
它的核心不是"撒粒子"，而是**在弹体身后留同帧的淡影**。

已照做（`MissileSprite`）：每 0.03s 在当前位置留一张**与弹体同一帧、同角度、同缩放**的淡影，
初始 alpha 0.35，0.3 秒内线性淡出，最多 4 张循环复用。
这样轨迹看起来是"箭的残影拖出去"，而不是旁边飘着不相干的东西。

## 闪电（lightning 区，已并入 feat/fx-modules）

这次终于有真源码可读（`GabriWar/warpedpd` v24.5.6），所以不再照反编译猜，直接搬。

**搬进来的（`effects/fx/lightning/`）**

| 文件 | 作用 | 关键参数 |
|---|---|---|
| `Lines` | 线段的摆放与淡出（Warped 原样） | — |
| `StrikeLights` | 落点的地面光池；相近的两次电击点亮同一片而不是并排两片 | `NEAR 24px / FOR 0.35s / AGAIN 0.7`，最多记 24 片 |
| `StrikeGround` | 干地留焦痕；水面发亮（wash）+ 荡开一圈纹 + 鱼影惊散；电痕沿水面爬行时的青色环 | 焦痕 `0.4 / 3s`；wash `9CC8FF / 0.3 / 0.4s`，半径 1~2 格、最多 12 格；`DISTURB 96px` |
| `Crackle` | 落点上还在爬的 2~3 根 3~5px 小电枝；武器附魔触发的静电火花 | 每 `0.05s` 换一茬、`0.15s` 消失、最多 8 处 |
| `Afterglow` | 大闪电消失后，光芯在眼前再留一瞬 | `0.12s`、`PEAK 0.25`、色 `0x6A80FF`、最多 3 份 |

**接线**

- `effects/Lightning` 保留原仓库的两段折线画法，新增 `major()`：`WandOfLightning`、`ShockBomb`、`FlashBangBomb` 标记为 major，落点才有焦痕、大光池与余光
- 短于 30px 的电火花算「没打到地上」：不铺光池、不留痕（与 Warped 的 `GLOW_FROM` 一致）
- 光池每帧按光晕亮度 `halo(t)` 驱动；被别的闪电重新点亮时，驱动权交给新的那道
- `GameScene.create` 调 `Lightning.sceneCreated()` 重置 `StrikeLights / Crackle / Afterglow`

**刻意的差异（本仓库没有天气层）**

- 没有整屏闪白、雷声、落点「咔嚓」声；`stormsOwn` / `thunderous` / `WeatherOverlay` 相关的都没搬
- 仍是原来的两段折线，没有换 Warped 那套 1254 行的 `Arc`（每 16px 一个关节、叉形、树枝、光晕、leader）
- `Arc.crawl()`（电痕沿水面跑）和 `ElectricityFx`（被电的水面自身的闪烁）还没搬

**实测**：临时场景 `Z_Lightning`（跑完即删）在真实引擎里打三道电——水面上、干地上、不足一格的短电火花——PASS 7/7：
光池 `poolsLaid>0`、焦痕 `scorchesLaid>0`、余光 `afterglowsShown>0`，短电火花两项都是 0。
另做了对照：直接铺一块 63px 的暖色光池，`LIGHTS` 图层正常显示（说明不是图层问题，只是本仓库地板很暗、蓝光弱）。

## 火（fire 区第一批：火焰粒子，已并入 feat/fx-modules）

**底子：`BlobEmitter` 换成 Warped 的版本**（66 行 → 329 行）：

- 每次 tick 只看几格改成「按块扫 + 按密度抽」，一格快烧完时粒子自然变稀（`FADE_VOLUME = 5`）
- 新增 `cell()`：工厂能读到正在发粒子的那一格（体积、是不是云的边缘、往哪边漂、上一帧有没有）
- 新增 `matter()`：`Blob.emissive()` 的 blob 画在 light 层，它掉下来的烟放进气体层
- 新增 `Aftermath / Bloomable / Kickable / Held` 四个可选钩子（格子熄灭、云涌起来、有人穿过、每格留一张贴图）
- **这是所有 blob 共用的类**（毒气、烟、水汽都走它），所以改完特地跑了 `Smoke`（9 项）和一个毒气 blob 的场景，都没问题

**火本身**

| 文件 | 作用 |
|---|---|
| `effects/fx/fire/FireStyle` | 12 种火的外观（火/地狱火/魔火/卤代烷/炼狱/火风/炉火/营火…）：火苗元素、火星、烟、地面辉光、光照、光点、燃烧速率 |
| `effects/fx/fire/Flames` | 一簇火苗：TONGUE_S/M/L 按大小翻帧（12fps），0.40/0.58/0.80s；LICK 小火舌；EMBER_1/2 火星 |
| `effects/fx/fire/FireBlobFx` | 把 blob 每个燃烧格分层撒出来：小舌头/舔舐每 0.05s、按体积升高的 tall/mid/low 火苗、每 0.6s 一粒火星、顶上每 1.4s 一股烟、水面换成蒸汽 |

- `Fire` / `Inferno` / `HalomethaneFire` 的 `use()` 改成 `FireBlobFx.pour(this, emitter, FireStyle.XXX)`，并 `emissive() = true`
- `GameScene.addBlobSprite` 按 `emissive()` 决定放 light 层还是气体层，并把 `matter()` 加进气体层

**没搬的**

- 营火 / 煤床 / 火风（本仓库没有 CoalBed / CampFire / Firewind 这些 blob）
- `ChimneySmokeParticle`（依赖 ClimateManager / HearthLight / WeatherSprites，天气没搬），营火的烟柱因此没有搬

**实测**：临时场景 `Z_Fire`（跑完即删）在真实引擎里点着一块 3x3 的火，PASS 4/4：`poured=1`、`flames>0`、毒气 blob 仍正常驱动、英雄没死；`Smoke` 9/9 也过。

## 火（fire 区第二批：FireField，已并入 feat/fx-modules）

`FireBlobFx` 现在会把 `FireField` 挂到 FLOOR 层（第一次有燃烧格被看见时 `attach`），由它管整片火的外围表现：

| 文件 | 作用 |
|---|---|
| `effects/fx/fire/FireField` | 每 0.2s 扫一遍火的格子：地面上铺暖色辉光（FloorGlow，最多 24 块，按组而非按格，随火光呼吸）、火的光照（LightRef 的 glow + pool 两组，各 4 个槽，最多 12 盏，超了按最近/最亮取舍）、水边的暖色 wash（最多 8 格）、走廊里的火光摊开、格子烧尽时的烟/火星/（被冻灭则蒸汽） |
| `effects/fx/fire/FloorGlow` | 贴在地面上的一块光，2x2 格一块，用 FX_LIGHT 的软光帧，加色混合 |
| `effects/fx/fire/LightRef` | 一盏火的 light 的引用：把 FxLight 的 glow 与 pool 配对、按格子的火光亮度驱动、位置移动时跟过去 |

**刻意去掉的**
- `BurningTree`（树冠着火）：本仓库没有 `Terrain.TREE`，也没有树冠层
- `HeatRay`（热浪）：依赖天气图集 `WeatherSprites`，天气没搬；`FireField.shimmer()` 整段删掉
- `Blob` 的 `Kickable` 钩子（有人穿过火堆时把火苗拨一下）没接，等有需要再说

**实测**：`Z_Fire` 改成「3x3 火 + 旁边一条水」跑 80 帧，PASS 4/4：`poured>0`、`flames>0`、`FireField.attached>0`、英雄没死；
截图里火堆周围有暖光铺地、水边有一条火色的 wash；`Smoke` 9/9 同时过。

## 气体与寒冷（gas / cold 区，已并入 feat/fx-modules）

**搬进来的（`effects/fx/gas/`）**

| 文件 | 作用 |
|---|---|
| `GasStyle` | 19 种气体的外观：冠部/底部配色、最浓时不透明度、是否加色（魔法气）、细节类型与频率、辉光、地面 tint / etch、每格约几张图与 pour 间隔 |
| `GasPuff` | 一朵气团（GAS_S → GAS_L、烟用 PUFF_*），透明度按格子体积、1.5–3s、顺体积漂移、卷曲 |
| `GasFactory` | 每格的粒子：气团、云的边缘偶尔换成向外卷的 wisp、每 `every` 个粒子插一个细节、云涌起时 6–10 团、有人穿过时推两团、厚格子给地面 tint、腐蚀气蚀刻地板、辉光/静态闪光/冰晶闪光 |
| `GasGlow` | 会发光的气（麻痹、腐化、黏液…）在夜色之上的一层光 |
| `CellOverlay` | 地面上的覆盖层：冰霜（rime，从格子中间张开）、tint、蚀刻；按格子保留、化开时淡出 |
| `Shade` | 气团从冠部到底部的灰阶映射 |
| `FrostFactory` | 冰冻云：格子上的霜 0.3s 张开、雪花/冰晶下落、贴地冷雾、冰面闪光 |

**`GasDetails`（我们自己的拼装）**

Warped 把这些"细节工厂"放在 `WeatherBlobFX` 里，而那个类绑着 `WeatherSprites`（天气图集）、`DriftParticle`、雨雪雾粒子。
于是只把**细节那一段**摘出来做成 `effects/fx/gas/GasDetails`：bubbles / motes / SWIRL / FLIES / drips / acidDrips / SOOT /
FROST / LEAF_BITS / sparkles，参数照抄。只有 SOOT 和落叶的帧换了（`ASH_2`/`SPECK_1` → `MOTE_2`/`PEBBLE_2`，天气图集没有）。

**接线**
- `ToxicGas` → `TOXIC`，`ParalyticGas` → `PARALYTIC`，`ConfusionGas` → `CONFUSION`，`CorrosiveGas` → `CORROSIVE`，
  `StenchGas` → `STENCH`，`SmokeScreen` → `SMOKE`
- `Freezing` → `emitter.start( FrostFactory.FREEZING, FrostFactory.POUR, 0 )`（原来 0.05 一颗粒，现在 0.12 一张图，约十张一格的密度）

**没搬的**
- `Blizzard` 还是旧粒子：Warped 的暴雪粒子在 `WeatherBlobFX.BLIZZARD`（驱动雪、天气图集），属天气
- 本仓库没有的气体 blob（Miasma / CorruptGas / BallGas / Dirtcloud / IceStorm / CloudOfCorruption…）不接线
- 天气图集里的 wisp / puff 细节（雷云、雾、季节风）没搬

**实测**：临时场景 `Z_Gas`（跑完即删）在真实引擎里放一团毒气 + 一片冰冻，跑 90 帧，PASS 4/4：
`GasFactory.emitted>0`、`FrostFactory.emitted>0`、毒气 blob 仍在、英雄没死；截图里毒气是 Warped 的深绿气团、
冰冻区是白霜团 + 冰晶。`Smoke` 9/9 同时过。

## 自然与圣光（nature / holy 区第一批，已并入 feat/fx-modules）

**搬进来的（`effects/fx/nature/`）**

| 文件 | 作用 |
|---|---|
| `BlobFx` | 自然类 blob 的粒子工厂：`growth()`（再生/生长：绿叶、水珠、湿润闪光）、`FOLIAGE`（枝叶间的光柱与绒絮）、`WELL`（治疗水井：上升光点、光柱、十字、水滴）、`SUN`（日光）、`GLOW`、`HALLOWED`、`PORTAL` |
| `Nature` | 配色常量（HOLY/HEAL/SPELL/POLLEN/…）、叶子色阶、昼夜判断、`glow()/air()` 发射器选择 |
| `HealFx` | 回血与护盾的表现（水井喝下去、治疗、护盾） |
| `LitFrame` / `EllipseMote` / `Sprout` / `TileCrop` | light 层光片基类、绕椭圆跑的粒子、嫩芽、贴地裁剪 |

**接线**
- `Regrowth.use` → `BlobFx.growth( false )`，`Foliage.use` → `BlobFx.FOLIAGE`，`WaterOfHealth.use` → `BlobFx.WELL`
- `WaterOfHealth` 回血那一刻调 `HealFx.drink( hero, hero.pos )`

**为了编译做的适配（本仓库粒子还是老版）**
- 上游的 `ShaftParticle`/`LeafParticle` 已重写成 FxParticle 体系并绑天气图集；这里用本仓库的老粒子，
  所以 `ShaftParticle.allowed/shaft/ambientRoom` 换成 `FxBudget.keep` + `ShaftParticle.FACTORY.emit`，
  `LeafParticle.allowed/leaf` 换成设 `color1/color2` 后 `reset`
- 没有 `LightParticle`，改用 `ShaftParticle.FACTORY` 顶
- 光墙（`WallOfLight.LightWall` + `WallSheet`）整段删：`hero.spells` 是 4.x 的东西
- 季节（`GameCalendar`）→ `Nature.autumn()` 恒 false

**没搬的**
- `GrowthPulse` / `Ticker`（地形变化时的生长脉冲）：要每帧 ticker 与 `terrainChanged` 接线，留到下一批
- `HolyFx`（4.x 英雄技能的圣光/光环/星芒）、`NatureScenes`（调试画廊）、`SeasonSweep`（季节）

**实测**：临时场景 `Z_Nature`（跑完即删）放一片再生 + 一片枝叶跑 80 帧，再放一次水井回血，PASS 3/3：
`BlobFx.grown>0`、`BlobFx.foliageTicks>0`、英雄没死；截图里生长区有绿叶、枝叶区有白色光柱与绒絮。`Smoke` 9/9 同时过。

## 奥术（arcane 区第一批：药水的奥术表现，已并入 feat/fx-modules）

**搬进来的**

| 文件 | 作用 |
|---|---|
| `effects/fx/arcane/ItemFx` | 物品的奥术表现：`drink`（喝药的一圈光）、`shatter`（砸碎的碎片/闪光/落地粉屑）、`read`（读书）、`teleport`、`uncurse`、`upgrade`、`shock`、`mapping`、`secret`、`recharge`、`bloom`、`strength`、`flyToNear` |
| `effects/fx/arcane/Arc` | 奥术公共件：`glow`/`light`/`matter`/`glint`/`make` |
| `ArcFlash` / `ArcImage` / `ArcBitmask` | 光片、弧光图、位掩码绘制 |
| `BoltFx` / `BoltTrails` / `ChargeGlow` | 弹体形态与拖尾、蓄力光 |
| `PrismFx` / `RayFx` | 棱镜光与光线的配色/收束（给 Beam 用） |
| `effects/fx/Afterimage` | 物品残影基类 |

**接线**
- `Potion.drink()` → `ItemFx.drink( hero, splashColor() )`
- `Potion.splash()` → `ItemFx.shatter( cell, splashColor() )`。**注意**：大多数药水自己覆盖了 `shatter`，
  但每个覆盖都会先调 `splash`，所以特效挂在 `splash` 上才覆盖得到全部药水
- `MagicMissile` 补了 `velocityX/velocityY/progress/flown` 四个访问器（`BoltTrails` 读它们算拖尾）
- `GameScene.Layer` 补了 `EFFECTS` 档（映射到 `effects` 组），`ArcFlash` 需要
- `MagicMissile` 补了三个 Warped 弹种常量（`SHAMAN_ELMO/LIGHT_MISSILE/POISON_CONE` = 16/17/18），只给 `BoltFx` 配色用

**没搬/没接**
- `Beam`（死亡射线/光射线/治疗射线）还是老实现：Warped 的 `Beam` 有 271 行且绑 `RayFx`/`PrismFx`，替换留到下一批
- `BoltFx.impact`（技能弹体命中）没接：调用方是 4.x 的 `SkillBolt`
- `ItemGhost` 没搬：本仓库物品图集是运行时图集，没有 Warped 的 `ItemSheet.film`，`Afterimage` 里计数改 0
- `ArcaneFx`（画廊模块）没搬

**实测**：临时场景 `Z_Arcane`（跑完即删）喝一瓶治疗药、砸一瓶毒气，PASS 3/3：`ItemFx.drinks=1`、`shatters=1`、英雄没死；
截图里喝药有治疗色光圈、砸药处有红色碎片。`Smoke` 9/9、`SelfTest_Potions` 5/5 同时过。

## 战斗（combat 区第一批：命中飞溅，已并入 feat/fx-modules）

**搬进来的**

| 文件 | 作用 |
|---|---|
| `effects/fx/combat/Spray` | 一次飞溅的模型，按颜色分三类：LIQUID（血、药水、酸）、DUST（骨灰、尘土）、SPARK（晶光、纯净、能量）。带方向与锥角，滴落时留血渍/灰印、落水荡纹并铺一层色云；最多 24 粒 |
| `effects/fx/combat/CombatFactories` | 打击用的火舌/烟/火星工厂（`CombatFactories.make` 与按元素的 tongue/smoke 变体） |
| `effects/Splash` | 换成 Warped 版：接到 `Spray` 上，`at/around` 五个重载不变 |

**效果**：`CharSprite.bloodBurstA` 本来就调 `Splash.at(c, 角度, 锥角, blood(), n)`，所以一换全通：
命中时血朝攻击方向飞出去、落地留血渍；药水砸地、水中落水、骨头粉尘走同一条路径。
`Splash` 全仓库有 52 处调用，全都自动升级。

**没搬的**
- `Blow` / `BlowPresenter`（Warped 的「打击演示」管线：稀有度/品质、起始与收招、连击、附魔 look 参与演出）——这套要连 `items/rarity` 与本仓库没有的武器 look 系统一起搬，属大改
- `HitFx`（命中星芒）依赖 `EnchantLook` / `EnchantState` / `TypeFx`，没搬
- `Looks` / `StateLooks`（1122 + 395 行，武器/护甲外观表）、`WeaponLight` / `HeldLight` / `ColdBreath`（天气）、`Quad` / `Piece`（Blow 的零件）、`BoatLook` / `RiddenHorse`（主世界）

**实测**：临时场景 `Z_Combat`（跑完即删）打一只老鼠 5 下再砸一瓶毒气，PASS 4/4：
`Spray.spreads>0`、老鼠确实掉血、药水飞溅也走同一条路径、英雄没死。
`Smoke` 9/9、`SelfTest_Potions` 5/5、`F1_Dm175ShieldAfterBreak` 3/3、`E0_BlastInactivePrototype` 3/3 同时过。

## 奥术第二批：Beam（光束，已并入 feat/fx-modules）

把 `effects/Beam` 整个换成 Warped 的版本（87 行 → 317 行），配合上一批搬进来的 `RayFx` / `PrismFx`：

- **核心与光晕分帧**：核心粗 2px → 4px → 整条（0.04s 内），维持一半寿命后按 8/6/4/2px 逐档收细；
  光晕用 `ArcImage` 的 `BAND_21` 沿光束铺，20Hz 抖动 ±6%，核心消失后再活 `AFTERGLOW 0.2s`
- **两端的表现**：起点一盏 11px 光、终点一盏 15px 光加一片 23px 地面光池，光照按 beam 的颜色走
- **按类型分化**：死亡射线有随机 1px 抖动、光射线沿光束铺 3 片地面光池、日光射线带自己的色、治疗射线有白色珠子从起点跑到终点 + 4 个脉冲 + 光点
- **几种新形态**：`SunRay`、`SuperNovaRay`、`PrismRay`（本仓库暂时只用前三种老类，新类留给后续）
- `Beam` 现在实现 `Lit`，画在 `GameScene.carryLight` 的光照层上（本仓库该接口是空实现，所以只是不走那一层）

**为编译做的适配**
- 本仓库 `GameScene.carryLight` 返回 void（Warped 返回 Gizmo），赋值去掉
- 本仓库 `Effects.Type` 没有 `SUPERNOVA_RAY`，`SuperNovaRay` 暂用 `LIGHT_RAY` 的贴图

**实测**：临时场景 `Z_Beam`（跑完即删）连放光射线/死亡射线/治疗射线，PASS 1/1（三道光都正常画出、英雄没死）；
截图里光射线是金白一道、死亡射线是紫白一道、治疗射线是粉紫一道，末端都有光斑。`Smoke`、`F1` 等回归见下一批提交。

## 闪电第二批：ElectricityFx（被电的水面，已并入 feat/fx-modules）

`Electricity` blob 的 `use()` 从「每 0.05s 一颗粒」换成 Warped 的 `emitter.start( new ElectricityFx(this), 0.2f, 0 )`：

- **电火花**：每个被电的格子每次最多两粒（静止的噼啪 / 会弹一下的短跳），同屏上限 150（`ChargeSpark` 自计数）
- **水面发亮**：被电的格子在水面层点亮一块 `ChargeWash`（`9CC8FF`、0.18 透明度、2.5Hz 噪声闪烁），
  只保留离英雄最近的 24 格；格子停 0.45s 后开始淡出 0.2s
- **电痕**：相邻两格之间偶尔拉出一道短闪，最多 6 道同时
- 云体积变大时整片水会 `boost()` 一下（0.1s 内亮 30%）

**为编译做的适配**
- 本仓库的 `SparkParticle` 还是老的 `PixelParticle`（没有 `resetElectric/resetStatic/ground/polar`），
  `ChargeSpark.charge()` 简化成 `reset( x, y )`
- 电痕用本仓库的 `Lightning`（两段折线）代替 Warped 的 `Arc.visual().crawl()`；等 Arc 重写搬完再换回

**实测**：临时场景 `Z_Electric`（跑完即删）在英雄旁铺一片水并灌满电，跑 60 帧，PASS 3/3：
`sparksMade>0`、`washesMade>0`、英雄没死；截图里红水面上有白色电火花和一块块亮斑。`Smoke` 9/9、`E0` 3/3 同时过。

## 闪电第三批：Arc 重写（Warped 的闪电形态，已并入 feat/fx-modules）

`effects/Lightning` 整个换成 Warped 版（166 行 → 1254 行），这是本仓库视觉上变化最大的一次：

- **stroke 形态**：按长度分关节（每 16px 一个、最多 8 段），越长的拧得越开；40px 以上七成概率长叉（两条细腿），
  1~3 根 5~12px 的小树枝，每段中间随机鼓 3px 并逐帧抖 1px
- **两层绘制**：白亮的芯（effects.png 的闪电帧）叠在 `lightning_halo.png` 的蓝白光晕上，关节处光晕多出 3px 不缺口；
  整体按 `FLICKER` 曲线闪（打击 → 暗一下 → 再亮 → 淡出），芯色从白到 `9CC0FF` 逐渐变冷
- **富大闪电**（同时最多 3 道）：先有 0.03s 的 leader 沿通道往下跑，落点铺大光池、关节处再铺 4 片小光池，
  干地留焦痕、水面 wash/涟漪/鱼影，落点爬电痕，消失后再留 0.12s 余光
- **新的雷击配色**：`tint()` 支持技能用的 STORM/ARCANE 配色（本仓库暂无调用方）
- `ElectricityFx` 的电痕改回 Warped 的 `Lightning.Arc.visual(...).crawl(0)` + `.noGlow()`

**为编译做的适配**
- **没有天气层**：Warped 把闪屏/雷声/震屏/载体都交给 `WeatherOverlay`；这里加了一个空壳
  `effects/fx/lightning/LightningSky`，`Lightning.layer` 恒为 null ⇒ 没有闪白/雷声/震屏，其余（弧、光晕、
  光池、焦痕、wash、电痕、余光）全都保留
- `world` 的判定从「天空存在」改成「在主相机上且 LIGHTS 层可用」
- 余光挂到 `GameScene.Layer.LIGHTS`（Warped 是挂在天气层上）
- 补了 `Assets.Effects.LIGHTNING_HALO` 常量（图集文件本来就在 assets 里）
- `Afterglow.membersView()` 补上（Warped 的 Group 有，本仓库的没有）

**实测**：临时场景 `Z_Lightning`（跑完即删）向水面/干地各打一道大闪电、再打一道不足一格的小电火花，PASS 1/1；
截图里闪电是带关节和光晕的锯齿，落点有明显光斑；`Smoke` 9/9、`E0` 3/3、`F1` 3/3、
`Extra_WardenAllyLightningKill` 8/8、`E1_PsiBlastTwice` 10/10 同时过。

## 自然第二批：GrowthPulse / Ticker（地形生长脉冲，已并入 feat/fx-modules）

- `GrowthPulse`：地板（EMPTY / EMBERS / EMPTY_DECO）变成草地（GRASS）、或草地变成高地草时，播一次「生长」：
  把新地块的图裁一块从中间张开（`TileCrop.REVEAL`，0.2s 张开 + 0.3s 淡出），地面撒 2~4 株嫩芽，
  高地草有 30% 概率再冒一个花苞。法术一次铺开时 `waveFrom(cell)` 让生长以 160px/s 的波传出去，
  0.6s 窗口内的变化会排队等波到（最多 64 个）
- `Ticker`：每帧 `GrowthPulse.tick()`（`Emitter.freezeEmitters` 时停）

**接线（顺手补的一块基础设施）**
- 本仓库以前**根本没人调用 `TerrainWatch`**，所以地形变化的钩子是死的。这次接上：
  `GameScene.updateMap(cell)` → `TerrainWatch.changed(cell)`、每帧 `TerrainWatch.drain()`、换场景 `resync()`
- `FxModules.terrainChanged` → `GrowthPulse.changed(...)`；`GameScene.create` 里 `GrowthPulse.reset()` + `add(new Ticker())`
- 注意：这条链路同时也让 `WaterTerrain` 的地块变化第一次真正跑起来（之前水格拼接靠的是另一条路径）

**实测**：临时场景 `Z_Growth`（跑完即删）在英雄周围把地板改成草地，PASS 2/2（`GrowthPulse.played()>0`、英雄没死）；
截图里草地长满嫩芽。回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3、`D4_WardenSaveReload` 14/14 全过。

## 火第三批：HeatRay（热浪，已并入 feat/fx-modules）

- `effects/fx/fire/HeatRay`：一团热浪，一帧一帧往上飘，每 0.3~0.5s 左右镜像翻一次（像空气抖动，
  不是拉伸），默认 `FFE8C0` 色 0.15 透明度，1.2s 寿命
- `FireField.shimmer()` 重新接上：每 0.2s 扫一次，每 4 个燃烧格一个「源」，每源每 0.3s 起一道热浪；
  炼狱/炉火这种 `shimmers()` 的火至少一个源
- **贴图换来源**：Warped 用的是天气图集的 `HEAT_RAY`，本仓库没有天气图集，改用 `Assets.Effects.FX_LIGHT`
  里 `FxFrames.Light.SHAFT_32` 那根光柱，运动/闪烁/镜像逻辑照旧

**实测**：临时场景 `Z_Heat`（跑完即删）点一片 5x3 的大火跑 100 帧，PASS 3/3：
`FireField.attached>0`、`HeatRay.risen>0`、英雄没死；截图里火苗上方能看到一缕缕上升的热浪。
回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3 全过。
