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

## 奥术第四批：ChargeGlow（蓄力光，已并入 feat/fx-modules）

- `effects/fx/arcane/ChargeGlow`：蓄力时在目标中间聚起一小团光（7px），0.8s 升到 `PEAK 0.6` 后呼吸式明灭，
  蓄力结束时 0.15s 内熄灭；会跟着目标移动
- 接到 `EyeSprite`：眼睛的 beam 在蓄力（`Eye.beamCharged`）且可见时聚光，死亡时熄灭
  （本仓库只有 EyeSprite 存在；SentrySprite/TempleSentrySprite 不在本分支）
- `ChargeGlow` 加了验收计数 `glows`

**实测**：临时场景 `Z_Glow`（跑完即删）在英雄旁边放一只 Eye 并让它蓄力，跑 60 帧，PASS 2/2（`glows>0`、英雄没死）；
回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3 全过。

## 火第五批：Explosion + BurstWatch（炸弹爆炸，已并入 feat/fx-modules）

- `BurstWatch`：把老工厂「一次 burst 出 n 颗粒」读成**一件事**——第 0 颗开一个 watch，同一 emitter 后续颗粒累加数量与中心，
  watch 第一帧时整件事一次播完；最多 16 个同时开，满了让最老的先播（碎片炸弹不会丢爆炸）
- `Explosion`：按 burst 大小分 枪火/小爆/炸弹/大爆 四档——核心闪光、地面光环、光照、火舌、碎屑、
  烟（按大小分 puff）、余烬、焦痕；还有 `hint`（落点预警）与 `meteor`
- `BlastParticle` 换成 Warped 版：它的 `FACTORY` 不再撒方块，而是 `Explosion.burst(...)`，
  于是**所有现有 `burst(BlastParticle.FACTORY, n)` 的调用点自动变成爆炸**（炸弹、DM-300 等）
- `SmokeParticle` 一并换成 Warped 的 FxParticle 版（`puff/RAMP/ALPHA`，FACTORY/SPEW 名字不变）
- 适配：删掉 `Explosion`/`Ash` 里的 `FireNet` 联机广播；`Explosion` 叠在 LIGHTS 层

**实测**：临时场景 `Z_Bomb`（跑完即删）在英雄旁边直接引爆一颗炸弹，PASS 2/2（`Explosion.bursts>0`）；
截图能看到烟团与地面光环。**注意**：在这块很暗的测试场里，核心闪光/火舌不明显，可能需要在真实关卡里再看一次、
必要时调 `Explosion` 的亮度参数。回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3、`A1_BlastWaveMobIntoChasm` 3/3 全过。

## 奥术第五批：剩余 ItemFx 事件点（已并入 feat/fx-modules）

- **传送卷轴**：`ScrollOfTeleportation.appear()` 里，角色真正挪过去之后调
  `ItemFx.teleport( from, pos, seenFrom, seenTo )`——起点/落点之间一道光，两头的光斑按可见性决定
- **充能卷轴**：`ScrollOfRecharging.doRead` → `ItemFx.recharge( curUser )`
- **暴怒/恐惧卷轴**：`ItemFx.shock( curUser, 0xFF4040/0xFF3030, false/true )`（红色冲击，恐惧那份更大）
- `ItemFx` 又加了 `recharges / teleports / shocks` 三个验收计数
- 没接的：`PotionOfBeacon` / `PotionOfHoly` 的 `bloom`（本分支没有这两种药水）、`ScrollOfAntiMagic`/`Dread`（同样没有）

**实测**：临时场景 `Z_Items2`（跑完即删）连读充能/暴怒/恐惧/传送四张卷轴，PASS 5/5
（`reads=4`、`recharges>0`、`shocks=2`、`teleports>0`、英雄没死）。

## 战斗第二批：HitFx 命中星芒（已并入 feat/fx-modules）

- `effects/fx/combat/HitFx`：一刀命中的「星芒」——中心亮星 + 一圈 bloom（按血量颜色）、按武器音效分
  HIT / SLASH / STAB / CRUSH / ARROW / MAGIC / PARRY / STRONG 八种形态（挥砍的月牙、突刺的细芒、钝击的尘土、
  箭的木屑、法术的亮点、格挡的环），重伤/暴击时更大更亮，英雄参与时还会轻微震屏；另有 `dodged`（闪避的气流）
  与 `killPuff`（击杀的一团血雾 + 地面环）
- `effects/fx/combat/Piece` 与 `LightFlash` 一并搬来（星芒作为 blow 的零件、命中瞬间的一道光）
- 接线：`Char.damage()` 里打完 `bloodBurstA`/`flash` 之后，若 `visibleFight` 且攻击者可见 →
  `CharSprite.hitFx( ... )` → `HitFx.hit(...)`；武器形态用 `HitFx.styleOf(attacker)`，
  `KindOfWeapon` 补了 `hitSoundAsset()` 取值
- **去掉的 4.x 耦合**：`EnchantLook`/`EnchantState`/`TypeFx`（附魔 look 框架）——星芒固定用目标自身血色，
  刀锋用白色；木屑用固定色而不是 `TypeFx.WOOD`

## 场景互动第二批：岩浆活起来（WaterLava 真身）

之前搬 water 套件时，`WaterLava` 只留了个空壳（5 个空方法）。这次把 Warped 的正文整段搬了进来（316 行），
按我们 `WaterAmbience` 的接口原样接通：

- **壳（crust）**：熔岩表面的暗色浮皮，缓慢漂移，最多每格 0.5 片
- **气泡**：鼓起 → 亮泡 → 破裂（`POP_5` + 2~3 颗火星 + 一小团光晕），平均每格 3 秒一个
- **热气**：每 1.5 秒一道摇曳的热浪（三分之一格，持续 1.5 秒，最多 8 道）
- **岸光**：熔岩旁最近的 6 格干地各有一盏橙色池光（POOL_23，随英雄走开熄灭）
- **喷溅**：`spit(x, y)`（连 `HallsLevel.Stream` 那种场景用）

**`WaterRain` 与 `WaterVoices`**：
- `WaterVoices` 本仓库早已有一版可用实现（拍岸/细流/咕噜/倒水，靠近水面时随机播），这次验证它确实在跑
- `WaterRain`（雨点涟漪）**没有搬**：本仓库没有天气系统，没有降水来源；同类视觉由 `WaterLive` 的屋顶滴水与
  `WaterWalk` 的落水涟漪承担

**实测**：临时场景 `Z_Lava`（跑完即删）在英雄旁边铺 9 格水、把液体设为熔岩，跑 150 帧，PASS 3/3
（`bubbles=3`、`waterVoices=1 last=LAP`、英雄没死）；截图里能看到熔岩池的流动纹理。回归：`Smoke` 9/9、`F1` 3/3。

## 场景互动第三批：风里的草（WindSway 的替身版）

Warped 的 `tiles/WindSway` 是把树/灌木/高草的**贴图上半截**按风向整像素侧移，因此要求它的 tilemap
（`OverworldDress.Layer`、`TerrainFeaturesTilemap`、`RaisedTerrainTilemap`）把会摆的格子从 chunk 里排除、
再用单独的 Batch 跟在后面画；风本身来自 `ClimateManager`（风速、风向、阵风、雷暴）。

本仓库的 tilemap 没有这个钩子，所以这里走另一条路：**在见过的草格上放几十片可摆动的小草叶**
（`FxFrames.BLADE_3/4/5`，2 倍缩放，恒微风 1.15 周期/秒，最多左右各 2 像素，整像素取整保持锐利）。
贴图本身不动，属于"视觉近似"而不是原版效果。

- 只放在 `heroFOV || visited` 且是 `GRASS/HIGH_GRASS/FURROWED_GRASS` 的格子上
- 每 2.5 秒重新挑一次位置（不够半个屏时更勤）
- 接在 `GameScene.update` 每帧推进，`FxModules.sceneCreated` 时重建

**实测**：临时场景 `Z_Wind`（跑完即删）在英雄周围铺 7×7 草地，PASS 3/3
（站着 28 片草、`swaySum` 在采样中变化多次、英雄没死）。回归：`Smoke` 9/9、`F1` 3/3。

## 场景氛围第五批：草与踩草（已定的最终形态）

- **风里的草叶**：`effects/fx/WindSway` 在视野内的草格上放小草叶，按 `Weather.windX` 摆动（风大摆得远、顺风偏）
- **踩草反馈**：踩进草格时压弯附近草叶，在**刚离开的那格**爆一簇 `Speck.LIGHT`（传送卷轴同款），并从角色 sprite emitter
  喷绿色 `GrassParticle` 草屑——实机确认可见（玩家脚下绿色粒子＋闪光，怪物踩草也有闪光）

**已放弃**：草格**贴图本身**的摆动（Warped `tiles/WindSway` 那种）。两次尝试都会带来可见的异常：
把草格从 chunk 排除后逐格补画会出现黑块或每格 1px 的红缝（独立 quad 的边缘采样问题），
要做对必须改底层渲染（自己组装 quad set / batch），已判断不值得。

## 场景氛围第四批：岸边的浪与冰缘

Warped 有 `IceFringeTilemap`（整块冰缘图块）与 `TideSurf`（滚上沙滩的浪带）。本仓库两者都没有，所以做成两个轻量件：

- **`IceFringe`**：冻结天气下，在**视野内贴岸的水格**往岸边铺一层淡冰壳（`FxFrames.RIME`，alpha 0.55，
  4 秒停留 + 3 秒淡出），只有边缘有，中间归 `IceCrust` 的白霜
- **`ShoreSurf`**：非冻结天气下每 0.8~2.2 秒在**视野内的岸边**打一道白色浪圈（`WaterFX.ring` M 号），
  六成概率再补一个小圈；采样方式是在镜头内的岸格里做蓄水池抽样，所以不会像随机撒点那样漏掉小水塘

两者都只是画面，不改水位、不淋湿人。

**实测**：临时场景 `Z_SurfIce`（跑完即删）先晴天后暴雪，PASS 4/4（`waves=6 fringes=3`、**HP 20→20**）。

## 场景氛围第三批：水上的低雾

Warped 的 `SwampMist`（303 行）是覆盖整片湿地的雾层，读气候与 overworld。本仓库没有湿地，所以做了
`effects/fx/water/LowMist`：每秒约 1.4 缕，从**视野可见的水面**升起（贴水面 5px 内），随风漂、6~8 秒消散，
alpha 只有 0.16 左右。

和天气的地雾（`WeatherOverlay.mist`，下雨才起）区分：这只是"静水自己冒的雾"，晴天也有。

**实测**：临时场景 `Z_LowMist`（跑完即删）在英雄旁铺 7×5 水面跑 3 秒，PASS 2/2（`mists=4`）。

## 场景氛围第二批：空气里的浮尘

Warped 用 `DustParticle`/`SunMoteParticle` 配合天气与光柱画浮尘；本仓库没有这些，所以直接做了个轻量版
`effects/fx/AmbientDust`：每秒 1.6 颗、在视野内可见的非实心格子里随机出现，随风轻飘（`Weather.windX`）、
`sway` 小幅摆动、5~7 秒后淡出；颜色多数冷白、三成偏暖。

刻意压得很淡（alpha 0.14~0.24），只是让空气"有东西"，不挡视线。

**实测**：临时场景 `Z_Dust`（跑完即删）在 arena 里跑 5 秒，PASS 2/2（`motes=4`）；截图里能看到淡淡的浮尘点。

## 场景氛围第一批：天上一道雷（ThunderBolt）

Warped 的 `effects/ThunderBolt` 是"从天而降砸在某个人身上的一道雷"（它用两条带分叉的 Arc、三声声音与火花拼出来，
接在 ShockingBrew / 雷霆药水上）。本仓库的 `Lightning` 自己从两点长出分叉，所以这里是：

- `ThunderBolt.thunderEffect(CharSprite)`：从视野顶端（随风倾斜，`WeatherOverlay.skyLean()`）往目标砸一道
  **major 闪电**，播一声 `Assets.Sounds.LIGHTNING`
- 接线：法杖 `WandOfLightning` 每次 zap，对**英雄看得见的每个目标**补这一道天雷（火花与闪光原本就有，不重复）
- 只是画面与声音：伤害仍是法杖自己的，天雷不加一点伤害

**实测**：临时场景 `Z_ThunderBolt`（跑完即删）用闪电法杖打老鼠，PASS 4/4（`bolts=1`、老鼠 8→1、
**英雄 HP 20→20**）；截图里能看到从天而降的白色分叉电弧落在老鼠身上。

**这批其余四件（CaveGlow/StarStreak/FestivalDecor/WobblyBanner）的情况**：它们都是 overworld/城镇专用——
CaveGlow 要 `MUSHROOM_PATCH/MINE_CRYSTAL/GEM` 这些本仓库没有的地形，StarStreak 是夜空的流星（地牢没有天空），
FestivalDecor 绑在 overworld 的节日事件上，WobblyBanner 是个通用`Group`但地牢里没有可挂的旗帜内容。
所以没有硬搬，等确认后再选地牢里用得上的替代品（沼泽雾、冰缘、水面浪花等）。

## 天气第十五批：水面会冻结（视觉 + 行为，不改变地形）

Warped 的做法是把格子本身换成 `Terrain.FROZEN_WATER`（水⇄冰，冰可行走、有自己的贴图）。
本仓库的 `Terrain.FROZEN_WATER` 只是 `WATER` 的别名，tilemap 里也没有冰的贴图，所以这里换了个做法：

- **状态**：`Weather.freezing()`（有降水且气温 ≤ 0℃）为真时，`WaterFX.liquid()` 返回 `Element.Liquid.FROZEN`
  —— 水面静止、不波光；同时 `WaterWalk` 不再溅水花、`WaterRain` 不在冰上打涟漪、`Thermal` 也不把冰当成湿身
- **视觉**：`IceCrust` 每帧在视野内的水面上铺白霜（`FxFrames.RIME`，0.5 透明度、2.5 秒停留 + 2 秒淡出），
  天冷时冰壳越积越明显，天暖后停止、旧霜自然化掉
- 天气一停，液体切回关卡原本的（`WaterLive.liquid()` 回落到关卡自己的水）

**没有做的**：把格子真的变成可行走的冰（`Terrain.FROZEN_WATER` 的独立地形与贴图）。那需要新的 tile 美术，
而且会改变寻路/滑动等玩法。

**实测**：临时场景 `Z_Freeze`（跑完即删）在英雄旁边铺水面后拉暴雪，PASS 7/7：
`liquid=FROZEN rings=0 wet=false rime=2`，天气停后 `liquid` 回到关卡原本的液体；截图里水面上能看到白霜。

## 天气第十四批：火的烟柱

Warped 用 `ChimneySmokeParticle`（依赖它的营火 blob 与天气图集）做一条直上的烟。这里用本仓库等价的实现：
`FireField.smoke()` 每 0.9 秒有 55% 概率从**燃烧区域顶端**放一蓬烟（`SmokeParticle.puff`，3.2~4.8 秒寿命、
向上 18~32 px/s、带风的横向漂移），比火堆上原本那层贴近火焰的烟**升得更高**，读起来就是烟柱。

**实测**：临时场景 `Z_CampSmoke`（跑完即删）在英雄两格外点火，PASS 2/2（`smoke puffs=1`）；
截图里火堆上方能看到烟。回归：`Smoke` 9/9、`F1` 3/3。

## 天气第十三批：火的热浪（HeatRay 终于有得看）

之前 `FireStyle.shimmers()` 只给 `INFERNO`/`FORGE`（Warped 里"普通火不闪"），所以营火上方没有热浪。
这轮放开：**任何真实的火焰都冒热浪**（`FireField.shimmer()` 每 0.3 秒在火堆上方放一道 `HeatRay`）。

**实测**：临时场景 `Z_HeatRay`（跑完即删）在英雄两格外点火，PASS 2/2（`heat rays=7`、火在视野内）；
截图里火堆上方能看到淡淡的热浪。

## 天气第十二批：岩浆也给人回暖

`Thermal.heat()` 之前只认焦土和火，现在再认**熔岩**：脚下或邻格是水面、且关卡液体是 `Element.Liquid.LAVA`
（即 water 套件画成岩浆的那种池子）时，按火的热源算（+15℃）。

- 暖的是"空气"，不会点燃角色、不扣血（本仓库没有 Warped 的岩浆伤害逻辑）
- 用途：Hall 类关卡里岩浆边上的干地不会"看着火却越待越冷"

**实测**：临时场景 `Z_LavaWarm`（跑完即删）在英雄旁边铺 6 格水并把液体设为熔岩，PASS 5/5
（`heat=15.0 air=24.85`，**HP 20→20、buff 3→3**）。

**热浪（HeatRay）**：搬火的时候就已经接在 `FireBlobFx → FireField.shimmer()` 上了，这次想用 `HeatRay.risen`
做个小验收没成功（暴风雪的 arena 里火没铺开、`risen` 一直是 0），所以**没有单独的验证证据**，只保留了代码路径；
要在真实关卡里看火堆上方的热浪是否有，需要你实机确认一下。

## 天气第十一批：闪电终于有人接（补回天气层缺失时丢掉的那半）

之前搬 `Lightning` 时，Warped 把「闪屏 / 雷声 / 震屏 / 载体」都交给天气层，而本仓库没有天气层，
只能留一个 `LightningSky` 空壳（`carry()` 返回 null、`struck()` 什么都不做）。现在天气层有了，空壳换成真的：

- `LightningSky.struck(cell)` / `stormStruck(...)` → `LightningFlash.game()`（走 FlashGate 的"游戏自己的闪光"闸门，
  2 秒一次、10 秒内最多 3 次）+ 一声雷（`WeatherSounds.thunder()`）
- `crackOnly/wandThunder` → 只响雷不闪屏（小怪的电击不该闪屏，照 Warped 的规矩）
- `spark(x, y, n)` → `FxRecipes.sparks(..., Element.ELECTRIC, n)`
- `Lightning.strike()` 里恢复：**major 级闪电**（法杖/药水/法术/风暴的）落地时调 `LightningSky.struck()`，
  原来那句「本仓库没有天气层：不闪屏、不响雷」的注释可以删了

**不落雷伤人**：这套只闪屏、响雷、放火花，不产生伤害或点燃。

**实测**：临时场景 `Z_LightningSky`（跑完即删）在英雄身旁放一道真实 `Lightning(...).major()`，PASS 5/5
（`flashes=1 thunders=1 skyThunders=1`，**HP 20→20、buff 3→3**）；截图能看到白色分叉电弧与整屏微亮。

## 天气第十批：温度看得见（并修正了状态判定的口径）

上一批把阈值套在**体温**上，导致"正常体温 36.5℃ > 30℃"永远算过热——这轮改成 Warped 的口径：

- **状态看气温（体感）**：`air ≤ -5℃` 严寒、`≤ 0℃` 冷、`≥ 30℃` 热，否则舒适（用 `NaN` 表示舒适，避免和 0℃ 撞上）
- **体温**只是一个平滑副本（0.06℃/秒趋近气温），用来驱动画面，不再参与判定
- 气温里加入**热源**：脚下焦土 +8℃、邻格有火 +15℃；风寒仍按 |风向|×9℃ 扣
- **看得见**：冷时角色被染成冷蓝并"呵白气"、热时染成暖橙并升热浪；进入/离开冷热状态时日志各说一句
  （`You feel a little cold` / `You are shivering with cold` / `You feel a little warm` / `You feel comfortable again`）
- 仍然**完全不改 HP、不加任何 buff**

**实测**：临时场景 `Z_ThermalShow`（跑完即删）把气温依次压到 -8℃、抬到 35℃、再放回常态，PASS 7/7
（`tints cold=-1 hot=1 plain=0`、`words=1`、`airNow=9.69 state=NaN`，**HP 20→20、buff 3→3**）。

## 天气第九批：回暖与湿身（同样不伤人）

- **暖源**：脚下是焦土（`Terrain.EMBERS`，0.7）或 8 邻格里有火（`blobs.Fire`，1.0）时，体温的目标值变成
  `气温 + (36.5 − 气温) × 暖度`——即火边会往"自己的体温"回升。**火只是暖，不会点着人，也不扣血**
- **湿身**：站在水里，或外面正在下雨/雨夹雪/暴雪时，体温升降速度 ×1.6（淋湿更快失温、火边也回得更快）
- 用途：岩浆旁的干地、篝火边这些地方，玩家不会因为天气降温而"越待越冷"，符合"气候不是威胁"的定位

**实测**：临时场景 `Z_Warmth`（跑完即删）在暴雪里先让体温从 20℃ 掉到 19.71，再站到焦土上从 20℃ 回升到 20.30，
PASS 6/6（`warmth=0.7`，**HP 20→20、buff 3→3**）。回归：`Smoke` 9/9。

## 天气第八批：温度（有无伤害的那一半——去掉伤害）

Warped 的温度是 `ClimateManager` 里：气候算出气温 → 体感温度（含风寒）→ 体温缓慢趋近 → 低于 -5℃ 挂
`Hypothermia`（每 20 回合扣血、减速）、高于 30℃ 挂 `Heatstroke`（扣血、加速饥饿）。

这次搬的是**前半段**，后半段（伤害与减益）明确丢掉：

| 新增 | 作用 |
|---|---|
| `actors/Thermal` | `ambient()`：按层数的静风气温（13→8→12℃ 一条曲线）+ 天气修正（雨 -3 / 雪 -7 / 冰霰 -6 / 暴雪 -12 / 雨夹雪 -5）+ 风寒（每 0.1 风向 -0.9℃）；`body()`：体温以 0.06℃/秒趋近气温；`stress()`：≤0℃ 冷、≤-5℃ 严寒、≥30℃ 热 |
| 视觉 | 体温 ≤12℃ 时角色嘴边每隔 1~2 秒呵一口白气；≥30℃ 时身上升起热浪 |
| **明确没有** | 不改 HP、不加 `Hypothermia`/`Heatstroke`、不减速、不加速饥饿、不加任何 buff |

阈值用 Warped 的原值（0 / -5 / 30℃），气温模型是本仓库的简化版（不模拟季节、锋面、湿度）。

**实测**：临时场景 `Z_Thermal`（跑完即删）在暴雪里跑，PASS 7/7：
气温 **-6.2℃**、体温 36.29→36.05（在往气温走）、体温压到 -8℃ 时进入严寒并呵出 3 口白气，
**HP 20→20、buff 数 3→3**。回归：`Smoke` 9/9、`F1` 3/3。

**还没做的**：火把/岩浆/火堆给附近的角色回暖、体温的 UI 显示（目前只有呵气/热浪这类画面暗示）、
湿身（淋雨/踩水）加速失温。

## 天气第七批：天气的声音

本仓库的 `assets/sounds` 里其实早就有整套天气环境音（rain / rain_light / rain_heavy / rain_water / rain_leaves /
rain_roof / snow / hail / sleet / blizzard / gale / gust / thunder_far / thunder_near / sandstorm），只是
`AmbientSound` 枚举里没有登记它们。这批：

- **`AmbientSound` 补 15 个条目**（名字、段数、时长、增益照 Warped 的值），文件本来就在
- **`levels/ambience/WeatherSounds`**（本仓库简化版，Warped 的 `WeatherScape` 703 行按地表/屋顶/树叶分声道、还按雷的距离算延迟）：
  - 有天气时每隔"一段声音长度 × 0.6~1.1"播一段，按降水类型挑（雨有四种音色随机、暴雪在 blizzard/gale 之间、雪/冰霰/雨夹雪各自一条）
  - 音量随天气强度 0.5~1.0 倍，音高/声像带随机
  - 雷暴闪光时补一记雷（35% 近雷、否则远雷）
- 接在 `WeatherOverlay.update` 每帧，换场景时 `reset`

**实测**：临时场景 `Z_WeatherSound`（跑完即删）拉起暴雪 10 秒，PASS 5/5
（`beds=1 thunders=2 flashes=2 last=GALE`，**血量 20→20、buff 3→3**）。

**没搬的**：`GameCalendar`/`DayNightCycle`（昼夜循环）、`WeatherScape` 的分声道天气声景、天空/云层绘制
（`SkyPaint`/`CloudSea`/`CloudShadows` 这一套和 Warped 的 overworld 天空绑死）。天气对玩家的所有效果也都没有。

## 天气第六批：雨天的地雾

下雨/下雪时，地面会慢慢升起很淡的一缕缕雾（`FxFrames.Light.GLOW_31`，透明度 0.10~0.16、随风轻轻漂、4~7 秒消散），
每 0.45~1.35 秒起一缕，强度低于 0.25 的天气不起雾。

刻意压得很淡：雾只是氛围，**不能挡住怪物**，更不碰任何角色。

**实测**：临时场景 `Z_Mist`（跑完即删）下雨 3 秒，PASS 4/4（`mists=2`、**血量 20→20、buff 3→3**）。
回归：`Smoke` 9/9、`F1` 3/3。

## 天气第五批：雨点砸在地上

雨雪落到**干燥地面**上会留一下小印子：雨是 `RING_5` 的小圈、雪是 `MOTE_1` 的小点，0.03 秒亮相、
0.22 秒淡掉，落点取镜头内的可见地面（水面交给 `WaterRain` 的涟漪，跳过）。频率：雨约 16/秒、
雪约 4/秒，乘以天气强度。

**实测**：临时场景 `Z_Splash`（跑完即删）下雨 3 秒，PASS 4/4（`splashes=15 drops=188`，
**血量 20→20、buff 3→3**）。回归：`Smoke` 9/9。

## 天气第四批：雷暴的天闪（只有光，不落雷）

- `effects/fx/FlashGate` 照搬：全屏闪光的总闸（风暴 0.7 秒一次、游戏自己的 2 秒一次、10 秒内最多 3 次），
  用真实时间计，所以不会闪成癫痫
- `effects/LightningFlash` 照搬：整屏一瞬的冷白闪光（瞬间起亮→快速衰减→回击的第二道小脉冲），
  跟着镜头走；震屏幅度取 `SPDSettings.screenShake()`，最多 2 像素
- 触发：`WeatherOverlay` 在**雷暴**时每 4~11 秒放一次 `LightningFlash.flash()`，加进 EFFECTS 层
- **不落雷**：Warped 的 `StormStrikes`（会掉血、会点燃）完全没有搬，闪光纯粹是画面

**实测**：临时场景 `Z_Storm`（跑完即删）拉起暴雪，PASS 5/5（`flashes=1 drops=847`，
**血量 20→20、buff 3→3**、英雄没死）；截图正好拍到闪光那一帧，整屏被照亮。

## 天气第三批：草跟着天气的风摆（WindSway 接风）

`WindSway` 之前是"恒微风"（固定 1.15 周期、±2 像素）。现在它读 `Weather.windX`：

- 风越大摆得越远（幅度 1 + 2.2×|风|）、也越快（周期 ×(1 + 0.9×|风|)）
- 整体**顺风偏**（偏置 1.6×风向），所以刮右风时草叶平均往右倒，而不是原地对称晃
- 没天气时风为默认 0.35（一阵轻风），和之前观感一致

**实测**：临时场景 `Z_WindWeather`（跑完即删）在草地里刮 0.9 的右风，PASS 4/4
（12 次采样的平均倾斜 `avg=55.6`、`min=40 max=65`——**顺风且一直在摆**，英雄没死）。回归：`Smoke` 9/9。

## 天气第二批：雨点打在水面（WaterRain 终于有用）

之前搬 water 套件时 `WaterRain` 只是空壳，理由是"没有天气就没有降水"。现在 `Weather` 有了，它就干回本职：

- 每帧按天气类型和强度，在**镜头能看到的水平格**上落下一圈圈涟漪
- 雨 26 圈/秒、冰霰 34（还额外多溅 1~2 圈）、雨夹雪 16、雪 5（落地即化，只轻轻一圈）
- 圈的大小/颜色取液体自己的泡沫色，接在 `WaterLive.update` 里（`FireReflections` 之后）
- 依旧只是画面：水位不动、不淋到任何角色

**实测**：临时场景 `Z_RainWater`（跑完即删）在英雄旁边铺 5×4 水面再下雨，PASS 4/4
（`ripples=8`、**血量 20→20、buff 数 3→3**、英雄没死）。回归：`Smoke` 9/9、`F1` 3/3。

## 天气第一批：只有布景的天气（无伤害版）

Warped 的天气是 `ClimateManager`（1614 行：温度、湿度、锋面、昼夜、雷暴、还会生成毒气团/冰风暴）加上
`WeatherOverlay`（1111 行：雨雪冰霰暴雪、雾、云、烟、萤火、阳光尘埃、落叶、屋檐滴水、雷闪）。
它的天气**有牙齿**：暴雪冻伤、热浪灼伤、雷击掉血。

这一批只搬"看得见"的部分，并且**明确去掉一切对玩家的效果**：

| 新增 | 作用 |
|---|---|
| `actors/PrecipType` | 降水类型枚举（NONE/RAIN/SNOW/HAIL/SLEET/BLIZZARD），照搬 |
| `actors/Weather` | 本仓库的天气状态：降水类型、强度、风向、是否雷暴；`rollForLevel()` 每次进关自己掷（约三成有天气，21 层以下永远晴），**不碰角色、buff、毒气、伤害** |
| `effects/WeatherOverlay` | 把雨/雪/冰霰画出来：用本仓库 fx 套件的粒子，按天气和风向落（雨快、雪慢飘、冰霰又快又硬），接在 `GameScene.update` 每帧推进 |

**实测**：临时场景 `Z_Weather`（跑完即删）先下雨再下雪，PASS 5/5：
`rain=69 total=138` 颗落下来，**英雄血量 20→20、buff 数 3→3（没有任何增减）**，截图里能看到满屏细雨点。
回归：`Smoke` 9/9、`E0` 3/3。

**还没搬的**（后续批次）：雨点的地面溅射（`WeatherSprites`）、雾/云/雷闪、天气音效、
以及把它接到 `WaterRain`（雨点让水面起涟漪）和 `WindSway`（草随风力强弱摆）。

## 场景互动第一批：怪物足迹（RoamerTracks）

Warped 的 `effects/RoamerTracks` 是「漫游野兽留下的脚印」：它只负责把上游（overworld 的漫游事件）算好的
`Print` 列表画到地上，贴图是构建时生成的 `effects/roamer_tracks.png`——这两样本仓库都没有。

所以这里做的是它的**本仓库版**：脚印直接来自每一步（`CharSprite.move` → `FxModules.stepped` → `RoamerTracks.stepped`），
用本仓库的 `FxDecal` 画在 FLOOR 层，`WET_9` 当脚印形状，颜色取扬起的尘土色（浅底/深底都能看见）。

- 只有**怪物**留脚印（英雄自己的会糊满脚下，看着乱）
- 只留在**已经见过**的地面上（没见过的地看不到脚印）
- 空中、水里的不留；**不是每一步都留**（55%）
- 存活 8 秒、再用 6 秒淡出

**实测**：临时场景 `Z_Tracks`（跑完即删）让老鼠来回走几步，PASS 2/2（`RoamerTracks.prints>0`），
截图里老鼠脚下能看到浅色尘土印。回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3。

## 战斗第四批：状态外观层的前置件（进行中）

`Looks`/`StateLooks`（武器与角色状态外观）连到 Warped 的「角色状态外观」层上，这轮先把本仓库缺的 API/替身补上：

| 新增 | 说明 |
|---|---|
| `CharSprite.State` 扩充 | 补 `GLOWING/AURA/ELECTRIC/BUTTER/HEATSTROKE/HYPOTHERMIA/TORCHLIT/ROOTED/BARKED/VERDANT/HERB/POISONED/CORRODED/OOZED/SEEDED/SURGING/BOAT_*`（只追加，不改原有序号） |
| `actors/buffs/Stunned` | 替身，`Stunned.HOLD` |
| `HeldLight` / `Quad` | 外观层的光与贴片基元 |
| `effects/fx/enchant/EnchantState` | 替身（`of/get/stunAt`） |
| `Group.membersView()` | Warped 的 Group 有它（外观给已有 quad 染色） |
| `CharSprite` | `isFlashing/tintFree/frameNow/shiver/lift/auraColor/stunStars` |

**第五批已落地**：`Looks` + `StateLooks` + `BoatLook` 搬进来了（`RiddenHorse` 因本仓库没有 `HorseSprite` 舍弃）：

- `StateLooks` 是**角色状态外观**的注册表：`BURNING/CHILLED/FROZEN/DARKENED/MARKED/HEALING/GLOWING/TORCHLIT/AURA/ELECTRIC/BUTTER/POISONED/...` 各自一档外观
- 接到 `CharSprite`：`add(State)`/`remove(State)` 通知它、`update()` 每帧推进、`stunStars()` 交由它回答
- 为它做的替身：`Surging`（只保留内部 `SurgeTracker.stacks`）、`FiendBurning`、`EnchantState`
- `Blow` 里晕眩等待恢复为 `Looks.stunning(ch)`

**注意**：`Looks` 这一层只有**状态外观**（火/冰/暗/光/藤/泡泡…），没有武器刀光——Warped 的「武器外观」其实由 `HitFx` 的八种形态承担，那部分我们已经在上一批接好了。

`StateLooks` 的 `switch (CharSprite.State)` 与 `Look` 基类（`pour/once` 等）需要成组搬，且 `RiddenHorse` 依赖本仓库没有的 `HorseSprite`。

- **没搬的**：`Blow`/`BlowPresenter`（打击演示管线，负责把一次攻击编排成多拍）、`Looks`/`StateLooks`（武器外观表，

## 战斗第三批：打击演示管线的骨架（进行中）

Warped 的「打击演示」（`Blow` + `BlowPresenter` + `Looks` + `StateLooks`）不是一个孤立特效，它长在四套子系统上。
这一批先把**缺失的依赖骨架**补上，让后面几批能逐块搬：

| 新增 | 作用 |
|---|---|
| `effects/fx/FxOrbit` | 打击光环的轨道粒子（341 行，照搬，和本仓库 fx kit 兼容） |
| `effects/HitCard` | **替身**：Warped 的 798 行伤害数字卡；这里先用现有 `FloatingText` 顶上，数字照常浮出，没有 chips/stamps/hop |
| `net/NetManager` | **替身**：本仓库没有联机，恒为 host、非 client |
| `effects/fx/enchant/EnchantNet` | **替身**：联机广播出口，全部空实现 |

**为什么不能一次搬完**：`Blow`/`BlowPresenter` 直接引用 Warped 的
`EnchantFx.SwingPiece/ProcPiece`（附魔 look 体系内部类）、`EnchantLook`/`EnchantState`/`TypeFx`、
以及 Warped 版的 `FloatingText` 数字图标码（`MANA`/`HIT_WEP`/`MISS_DODGE`…）和 `Buff.icon()` 返回 int 的约定
（本仓库是 String）。这些要逐个做替身或改写，属多轮工作。

**当前状态（第二批已落地）**：`Blow` 与 `BlowPresenter` 已搬进来并接上：

- `Char.attack(...)` 外面包了 `Blow.begin( this, enemy )` / `try { attackInBlow(...) } finally { Blow.end(); }`
- `CharSprite.hitFx(...)` 里先 `Blow.offer( new HitFx.StarPiece(...) )`，open 的 blow 就把它当一拍演，没有 open 就自己画
- 去掉的部分：`EnchantFx.SwingPiece`/`ProcPiece`（附魔 look 体系的拍子）、`Looks.stunning`、`DamageLedger.card`
  （伤害数字卡的数据来源）、Warped 的 `FloatingText` 图标码（缺失的都用 `NO_ICON`，数字照常显示）
- **伤害数字**仍由本仓库 `Char.damage` 的 `showStatusWithIcons` 显示；`HitCard` 替身目前只做兜底

下一批：`Looks`/`StateLooks`（武器外观表）——它们依赖 `EnchantState` 与 `Stunned`（本仓库没有），仍要逐个替身。

  依赖 rarity）；`CharSprite.nudge`（受击抖动）本分支没有，先省掉

**实测**：临时场景 `Z_Hit`（跑完即删）砍一只老鼠三刀，PASS 3/3（`HitFx.hits>0`、老鼠掉血、英雄没死）；
截图里能清楚看到目标身上一团亮黄星芒 + 一道红色月牙。回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3 全过。

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

## 奥术第三批：ItemFx 接到物品上（已并入 feat/fx-modules）

- 卷轴：`Scroll.readAnimation` 每次读卷轴都 `ItemFx.read( curUser, SCROLL )`（翻页的光、上升的光点）
- 魔法地图卷轴：`ItemFx.mapping( curUser )` + 每发现一处密门 `ItemFx.secret( i )`（地面上一圈扩散的光）
- 力量药水：`ItemFx.strength( hero )`（金色迸发）
- 移除诅咒卷轴：`ItemFx.uncurse( curUser )`（**注意**：本分支的移除诅咒卷轴会先弹背包窗，所以自动化场景里读不了它，
  这个接线只做了代码接入，没有场景断言）
- `ItemFx` 加了验收计数：`reads / mappings / curses / strengths`

**实测**：临时场景 `Z_Items`（跑完即删）连读三张魔法地图卷轴 + 喝一瓶力量药水，PASS 4/4
（`reads=3`、`mappings=3`、`strengths=1`、英雄没死）；回归：`Smoke` 9/9、`SelfTest_Potions` 5/5、
`B0_TeleportScrollControl` 3/3、`E0` 3/3 全过。

## 火第四批：Embers / Ash（余烬与飞灰，已并入 feat/fx-modules）

- `effects/fx/fire/Embers`：一个格子烧成 `EMBERS` 时铺下余烬——地面留一小片焦痕（1.5s 后开始 6s 内淡出）、
  3 粒余烬在地上明灭、每隔 0.6s 冒一小股烟
- `effects/fx/fire/Ash`：东西烧掉时飞灰——`n` 片灰屑（`6A6468`/`3E3A3C` 两色）从格子往上飘、打旋、慢慢落
- 接线：`FxModules.terrainChanged` 里判断新地形是 `EMBERS` 且旧的不是 → `Embers.lay(cell)`；
  `Heap.burnFX` 里调 `Ash.flakes( pos, 3 )`（东西烧掉时先绿光一闪，再飞灰卷起）
- 适配：`Ash.FireNet.send(...)`（联机广播）删掉，本仓库没有联机层

**实测**：临时场景 `Z_Embers`（跑完即删）把一片地板烧成 EMBERS、再烧一个堆，PASS 3/3（`Embers.laid>0`、`Ash.flakes>0`、英雄没死）；
回归：`Smoke` 9/9、`E0` 3/3、`F1` 3/3、`D4_WardenSaveReload` 14/14 全过。

## 附魔外观体系第一批：词汇表落位（命名空间）

Warped 的 `effects/fx/enchant`（43 个文件）与本仓库**已有**的自定义 `effects/fx/enchant`
（`EnchantFx`/`EnchantRecipes`/`ItemAura`/`SpecialFx`，被 `Weapon.proc`、`ItemSlot`、`MissileSprite`、
`GameScene`、`BlowPresenter` 调用）**同名冲突**，所以不能直接替换。决定：Warped 的整套
放进 **`effects/fx/enchant/warped/`**，两套并存。

本批先把不依赖 `EnchantFx` 的底层词汇表搬过来并编译通过：

- `warped/Family`（附魔家族，63 行）
- `warped/Motif`（固定图案，71 行）
- `warped/Profile`（配置档位，172 行）
- `warped/EnchantLook`（一条附魔外观，116 行）
- `effects/fx/ItemGhost`（占位：本仓库物品美术是运行时图集，没有残影帧，`of()` 返回 null）

**依赖图**（后续批次用）：
- 可直接搬的 21 个特效文件（`CutFx`、`DreadFx`、`EmberFx`……）都依赖 `EnchantFx.Beat`/`Flash`
- `EnchantFx`（1603 行，核心）还差 `EnchantNet`、`EnchantState`、`Sting`，而 `EnchantState` 差
  `EnchantLooks`（附魔行表）与 `items.rarity.Quality`
- `EnchantLooks`/`ItemAura`/`TypeFx`/`PieceFx` 强依赖 Warped 的 `items/rarity`、`skills`、枪械/乐器等本仓库没有的系统

下一批：带着桩过度（`Sting` 空实现、`EnchantNet` 断开、`Quality.vestigeOf` 返回 null）
把 `EnchantFx` + `EnchantState` + 精简 `EnchantLooks` 弄编译，再接 `BlowPresenter`。

## 附魔外观体系第二批：核心编排器编译通过

本批把骨架合上了，`warped` 包现在包含：

- `EnchantFx`（1603 行，核心编排器）、`EnchantState`（249 行）、`EnchantLooks`（**精简版**）
- 15 个家族特效：`EmberFx`、`RimeFx`、`StormFx`、`VenomFx`、`VerdantFx`、`DreadFx`、`ReaperFx`、`CutFx`、`ForceFx`、`WardFx`、`RadianceFx`、`FortuneFx`、`MirageFx`、`HeartFx`
- `MotifFx`、`HexOverlay`

**桩与防**（都是本仓库没有的 Warped 系统，看不到效果但不报错）：

- `EnchantNet`（联机，全空实现）、`Sting`（附魔音效，全空实现，但保留音量公式）
- `DrainFx`（吸血治疗的血珠，空实现）
- `items.rarity.Quality`（品质系统，`vestigeOf` 返回 null）
- `CharSprite` 补了 `blowLook`/`blowUntil`/`afterglow()`/`nudge()`（对应 Warped 的字段与方法）
- `FxModules` 加了 `ENCHANT` 槽位（`COUNT = 2`）

`EnchantLooks` 是**手写的精简表**：只收本仓库有的附魔——武器附魔 13 条（Blazing/Chilling/Shocking/Corrupting/Grim/
Kinetic/Elastic/Blooming/Lucky/Vampiric/Blocking/Meteor/Seeking）、符文 5 条、武器诅咒 5 条。
Warped 原表 78 行，多数对应的是它自己的附魔集合；未收录的附魔按“没有外观”处理，行为与之前一致。

下一批：把 `BlowPresenter` / `Weapon.proc` 接到这套上（先接“附魔参与打击演示”），再做物品光晕。

## 附魔外观体系第三批：接上打击演示

- `Weapon.proc` 命中时（带附魔、敌对关系、目标有 sprite）除了呼叫旧的自定义外观，
  现在还会把这次附魔交给 `enchant.warped.EnchantFx.proc(...)`（等级 MID）
- `EnchantFx` 加了 `procs` 验收计数

**自测**：临时场景 `Z_EnchantBlow`（跑完即删）——英雄手持 Blazing 剑打老鼠，
PASS：附魔表里有 Blazing（`lookKnown=1`），`procs 0 → 1`，截图里能看到“燃烧”与伤害数字。

注意：目前是“每次命中都叫一次附魔拍子”，与旧外观并存；后续可以改成“只在真正
触发 proc 时”（需要逐个附魔改接）。物品光晕（`ItemAura`）仍未接，下一批做。

## 附魔外观体系第四批：拍子确实跑了，但画面还没验证

本批给 `EnchantFx` 加了 `runs`（真正进入绘制流程）与 `earlyOut`（因 `glowAir()` 为空或 sprite 不可见而提前返回）
两个验收计数。自测（`Z_EnchantArt`，跑完即删）：直接请求 Blazing / Shocking 两次拍子，

```
procs ember=1 storm=2  runs=2  earlyOut=0
```

说明拍子**确实走到了绘制步骤**（不是被提前 return 掉），但截图里**看不到火花烟尘**——
下一批要查：它们是不是落在了不可见的层（本仓库 `Fx.glowAir()` 在 `lightFx` 组）、或粒子参数（比例/颜色×暗色调）导致看不见。

**物品光晕（`ItemAura`）暂缓**：它强依赖 Warped 的 `items/rarity`（`Quality.of`、`RarityLine.WARM_GRIP/COOLING/RAINPROOF`
等）与 ~20 个本仓库没有的附魔/符文类，直接搬会变成一堆桩；先把现有的自定义光晕保留，等品质系统真正引入后再接。

## 附魔外观体系第五批：画面终于出来了（光效层顺序）

根因找到了：`GameScene.create` 里 `lightFx` 组是在 **`dayNightOverlay` 与 `FogOfWar` 之前** `add`
进场景的，于是所有“发光类”粒子（火花、闪电等走 `Fx.glowAir()` → `lightFx` 的）都被夜色遮罩与战雾
**盖在下面**，再亮也看不见。

修正：`lightFx` 改为在 `fog` 之后 `add`（光效画在遮罩与雾之上）。

**实测**（`Z_EnchantArt`，跑完即删）：

- Blazing：老鼠身上出现橙色火星与焦痕（截图 `01-ember.png`）
- Shocking：老鼠身上出现白色闪电爆裂（截图 `03-storm.png`）

全量回归：**58 通过**，仍只有 `A0_KnockbackControl`、`A4_BlastWaveHeroIntoChasm` 两个基线失败（本次
`Extra_ActivatedPrototypeAsleep` 耐性通过）。

下一批：物品光晕仍暂缓（需品质系统）；可以先把“每次命中都叫拍子”收窄到“真正 proc 才叫”。

## 附魔外观体系第六批：地上附魔物品的光晕

新增 `warped/ItemAura`（精简版）：地上堆里若有**带附魔的武器或带符文的护甲**，就按它的
`EnchantLook.motif` 周期（`periodAt(0)`）从堆上冒一颗光晕粒子（`MotifFx.heap`），只在镜头内扫描。

- `GameScene.create` 里 `reset()`，`update()` 每帧推进
- `motes` 验收计数

**与 Warped 原版的差距**（写在类注释里）：Warped 的 `ItemAura` 还画物品格子里的光晕、符文、品质线与冷热握感，
那些都需要品质系统；这里只做了“地上”这一半。

**实测**（`Z_EnchantAura`，跑完即删）：把一把 Blazing 短剑丢在英雄旁边，帧数后
`motes=1`（heap 存在、look=Blazing），截图里剑旁有橙色光点。全量回归 **58 通过**，仅 `A0`/`A4` 基线失败。

## 附魔外观体系第七批：只在真正触发时叫拍子

之前是“每次命中都叫一次附魔拍子”，会在普通命中上也冒特效。现在 `Weapon.proc` 在调用
`enchantment.proc(...)` 前后比较三个量：伤害值、目标 HP、目标 buff 数——**任一变化才认为 proc 真的发生了**，
只有那时才调 `warped.EnchantFx.proc(...)`。

**实测**（`Z_EnchantProc`，跑完即删）：

```
enchanted 0 -> 1 (burning=true)   plain 1 -> 1
```

即附魔命中叫了拍子、普通命中不叫。`Smoke` 9/9、`E0` 3/3、`F1` 3/3。

**值得注意**：这是“行为差异”启发式判断，而不是每个附魔自己声明“我 proc 了”。
对“只改状态不改伤害/HP/buff”的附魔（例如只推位、只改坡度）仍可能漏判，后续可逐个附魔加显式钩子。

## 附魔外观体系第八批：手上武器的光晕

`warped/ItemAura` 再加一半：英雄**手上武器**若带附魔，就按 motif 周期在武器侧（根据
角色是否镜像取左/右）冒一颗光晕粒子（`MotifFx.wield`），计数 `wieldMotes`。

**实测**（`Z_WieldGlow`，跑完即删）：英雄拿 Blazing 短剑，`wieldMotes=1`，截图里英雄侧有橙色光点。

至此附魔外观体系的可见部分（打击演示、地上光晕、手上光晕）均已接上；
依赖 `items/rarity` 的部分（物品格光晕、符文、品质线、冷热握感、`LineLooks`/`TypeFx`/`PieceFx` 等）
留作“品质系统引入后”的后续工作。

## 负责修正：踩草反馈只在草上生效

**现象**：玩家每走一步都冒绿烟与闪光（实机反馈）。

**原因**：`WindSway.rustle` 里“落脚格的 `Speck.LIGHT` 闪光”与“角色 sprite 的绿色草屑”两段是**无条件**的，
只有放草叶那段检查了草地。所以在石板/木板/任何地形上走路也会喷粒子。

**修正**：整个 `rustle` 先看 `from`/`to` 是否为草（`grassVisible`），两边都不是草就直接返回；
闪光只在草格上放；绿色草屑也只在真正踩到草时才喷。另加 `puffs` 验收计数。

**实测**（`Z_StepGrassOnly`，跑完即删）：`stonePuffs=0`、`grassPuffs=1`——石板上走一步不画，草里走一步才响。

## 负责修正：附魔攻击“有时候卡死”

**现象**：实机下附魔攻击偶尔卡住（游戏不再响应）。

**自测没能复现**：写了 `Z_EnchantStress`（跑完即删），英雄持 Blazing 剑连续打 ~20 次
（`hits=19~20`、`procs=3~4`），无任何卡死地跑完。所以这次是**防御性修正**，不是定位到确切原因。

针对两个最可能造成“卡死”的点：

1. **附魔家族美术异常会让打击演示永远收不了尾**：`EnchantFx.family(...)` 的分发与 `Fx.post` 里的
   回退绘制现在都包了 `try/catch (Throwable)`——任何一个家族引擎抛异常，不会再把演示状态弄坏
2. **`CharSprite.nudge()` 会启动一次零长度的移动 tween**（我之前为了“推一下”写的），
   而游戏会等待角色移动完成——这是典型的卡死；已改为安全的空实现（丢掉小幅推动，保留其他所有特效）

若仍复现，需要实机 `adb logcat` 的崩溃/无响应时刻日志，才能定位到具体家族与调用栈。

## 负责修正：烈焰附魔卡死回合——不再加入打击演示的状态机

**现象**：实机烈焰（Blazing）附魔攻击会把回合卡住。

**自测**：用假人（被动、2000 HP 的老鼠）连续砍 5 刀，每刀后等待英雄回合归来：
`attack0..4 = ok(1)`——在无头环境里**无法复现**（之前一次“复现”其实是英雄被石像打死了）。

**改动**：加了开关 `EnchantFx.joinBlow`，**默认关闭**。关闭时不再调
`Blow.offer(new ProcPiece(...))`（不往正在进行的打击演示里塞片段），而是直接
`Fx.post(runProc)` 把同一个拍子自己画出来。视觉上仍有火星/闪电（回合 5 就是这条回退路径画出来的），
但**不再与 blow 状态机交互**——那是目前最可能把回合挂住的地方。

若仍卡，下一步只能靠实机 `adb logcat`。

## 负责修正：删掉踩草与走路的特效

实机反馈：“踩草和走路特效好丑”。已整体移除：

- `FxModules.stepped` 不再调 `WindSway.rustle(...)`：**走路不再有闪光、绿色草屑、草叶被压弯**
- `WindSway` 里的 `rustle`/`placeAt` 与 `puffs`/`rustles` 计数一并删除（不留死代码）

保留：风里的草叶（环境氛围，不是走路触发）、水面涟漪、怪物脚印。

## 负责修正：删掉草地上的脚步碎叶（怪物与玩家都不再有）

上一批只删了 `WindSway.rustle`，但 **`WaterWalk.footstep` 仍在**：任何角色走在草地/耕地上都会
飞出一两片草叶（`FxFrames.BLADE_3/4`，颜色取关卡地板色）——这就是怪物走路时那些碎片。

现在：`WaterWalk.step` 对“两边都不是水”的案例**直接返回**，`footstep` 方法与 `FOOTSTEP_GAP` 一并删除。
水面/岩浆上的涟漪与火星保留（只在油/水面上发生）。

## 附魔文本样式第一批（B）：物品窗口里的附魔名

新增 `warped/ItemText`：

- `titleColor(item)`：有附魔就用它的 `EnchantLook.glow`，诅咒用减淡的 hex 色
- `line(item)` / `suffix(item)`：一行 `✦ <附魔名>`（诅咒加“（诅咒）”），追加在描述后

已接入本仓库的 `WndInfoItem`：物品名按附魔色着色（+0 且已鉴定时），描述末尾多一行附魔名。

**实测**（`Z_ItemText`，跑完即删）：烈焰短剑 +2 的窗口，标题绿色，底部出现
`✦ 烈焰附魔`（`line=[✦ 烈焰附魔]`、`color=16745496`）。

**还没做**：Warped 原版的多段式排版（分隔线、品质行、词条行）需要 `items/rarity`；这一批只做了“名字颜色 + 附魔行”。

## 品质系统第一阶段：Rarity 与 Quality 骨架

- 搬入 `items/rarity/Rarity`（Warped 原文件）：COMMON/UNCOMMON/RARE/LEGENDARY/EXOTIC，带颜色、品质线数、深度权重与
  `roll(depth)`、`rerollWheel(...)`
- `Quality` 换成**精简骨架**：`Family` 枚举（SWORD/AXE/SPEAR/MACE/DAGGER/STAFF/ARMOR/…，去掉 Warped 的枪械与弓）、
  `of(item)`、`family()`、`level()`、`title()`
- 保留 `Quality.vestigeOf(...)`（附魔框架需要）

**实测**：编译通过，`Smoke` 9/9。品质线（`RarityLine`）与蓝图展示留给下一阶段。

## 品质系统第二阶段：品质行与品质特质文字

- 新增 `items/rarity/RarityLine`：Warped 名字的精简版（CRIT/LIFESTEAL/WARM_GRIP/SLAYER/…/FLEET/…/SUREFOOT），
  只对“本仓库真有的装备家族”开放（剑/斧/矛/锤/匕首/法杖/护甲），没有枪械与弓
- `Quality.lines()`：按 `Rarity.lines` 与物品自身哈希选出固定的几条（同一件装备永远读到同一组）
- `ItemText.qualityText()`：“品质：普通” + 每条质量行；`Rarity.title()` 改为内置中文（本仓库没有 rarity 语言键）
- `WndInfoItem` 将其追加在附魔行之后

**实测**（`Z_RarityText`，跑完即删）：烈焰短剑 +2 窗口底部显示
`品质：普通` 与一条品质特质（本次抽到“吸血”）。编译通过、`Smoke` 9/9、`F1` 3/3。

## 品质系统第三阶段：装备光晕接上品质与附魔色

- `ItemText.auraColor(item)`：有附魔用 `EnchantLook.glow`；否则要么用 `Quality.rarity().color`（非 COMMON 才亮），否则 0
- `ItemSlot.updateAura`：优先用这个色，取不到才回退到原来的 `EnchantFx.uiColor`
  ——即物品格里的扫光/光环/粒子现在跟随附魔与品质颜色

编译通过，`Smoke` 9/9、`F1` 3/3。图标光晕的实机效果需你装 APK 看一下。

## 品质系统第四阶段：物品窗口分段（描述 / 稀有度）

- 新增 `ui/SectionHeader`：小三角 + 标题 + 细线，模仿 Warped 物品窗口的分段头
- `WndInfoItem` 改用 `layoutItem(...)`：标题 → **描述** 分段 → 正文（含附魔行）→ **稀有度：普通** 分段（按稀有度色）
  → 品质行（同色）
- `ItemText` 拆出 `isGear/rarityTitle/rarityColor/qualityLines` 供窗口使用

**实测**（`Z_RaritySections`，跑完即删）：窗口里出现“描述”与“稀有度：普通”两个分段头，
品质行按稀有度色着色。编译通过、`Smoke` 9/9、`F1` 3/3。

**与 Warped 原版的剩余差距**：稀有度不会真生成（永远 COMMON）、无词条数值效果、无品质图标、
无附魔等级与残迹附魔段落。

## 品质系统第五阶段：稀有度真的会生成了

- `Quality.of(item)` 不再固定 COMMON：装备首次被查询时**按实例确定性地 roll 一次稀有度**，
  存在 `IdentityHashMap` 里（同一件装备永远读到同一个稀有度）
- 权重用 `Rarity.weight(depth)`（深度越深越容易出高稀），种子取实例身份与类名
- 因此 `lines()` 也随之变化（普通 1 条、稀有 2 条），窗口分段按稀有度色显示

**实测**（`Z_RarityRoll`，跑完即删）：新建 60 把短剑，
`common=30 uncommon=25 rare=5 legendary=0 exotic=0`（深度 1）；拿到的非普通剑窗口显示“良好”。
编译通过、`Smoke` 9/9、`F1` 3/3。

**仍未做**：词条数值效果（暴击/吸血等）、品质图标素材、残迹附魔。

## 品质系统第六阶段：词条开始有数值了（先两条）

- `Quality.strength(line)`：每条词条按品质的 `minRoll..maxRoll` 与实例确定性地取一个 0..1 强度
- `Weapon.proc` 应用两条：
  - **CRIT**：`8% + 22%*strength` 概率使这一击伤害 ×1.5
  - **LIFESTEAL**：按伤害的 `30%*strength` 回血（至少 1）
- 其余词条仍只有名字

**实测**（`Z_RarityPerk`，跑完即删）：找到一把带吸血的短剑（轮 200 把才找到），
英雄 HP 先压到 10，一刀下去 `hp 10 -> 12`。编译通过、`Smoke` 9/9、`F1` 3/3。

**仍未做**：其余 14 条词条的数值、品质图标素材、残迹附魔。

## 品质系统第七阶段：再三条词条生效

- **EXECUTIONER**：目标低于半血时伤害 +35%×强度
- **DEFIANT**：自己低于半血时伤害 +35%×强度
- **STORMBORN**：雷暴天气下伤害 +20%×强度
- 宜家：`Quality.familyOf` 对本仓库庞大的近战武器池增加了按类名归类（axe/spear/mace/dagger/staff），
  不再全部堆在 SWORD

**实测**（`Z_RarityPerk2`，跑完即删）：一把带“不屈”的巨斧，满血平均伤害 110.0，半血时 132.9（约 +21%）。
编译通过、`Smoke` 9/9、`F1` 3/3。

**仍未做**：其余 11 条（SLAYER/HUNTER/STEADY/MOONLIT/WARM_GRIP/护甲六条）、品质图标、残迹附魔。

## 品质系统第八阶段：FLEET 生效 + 修掉“同类装备永远同一条词条”

- **FLEET**：穿着带该词条的护甲时，`Char.speed()` × `1 + 0.15×强度`
- **修 bug**：`Quality.lines()` 的选择种子原来只取**类名**，导致同一类装备永远抽到同一条（轮 400 件
  皮甲都没有 FLEET）；现在种子叠上实例身份，同一件仍稳定，但同类不同件会不同

**实测**（`Z_RarityFleet`，跑完即删）：同样皮甲，普通件 speed=0.694，FLEET 件 speed=0.766
（+10.3%，与强度 0.685 对应）。编译通过、`Smoke` 9/9、`F1` 3/3。

**剩余**：SLAYER/HUNTER/STEADY/MOONLIT/WARM_GRIP + HUSHED/RAINPROOF/COOLING/WINDBREAK/SUREFOOT。

## 品质系统第九阶段：WINDBREAK 与 SUREFOOT

- **WINDBREAK**：`Thermal.ambient()` 里的风寒（`|windX|*9`）被减弱：`chill *= 1 - 0.6*强度`
- **SUREFOOT**：`Chasm.heroLand()` 的坠落伤害乘以 `1 - 0.4*强度`（至少 1）

编译通过、`Smoke` 9/9。**这两条没有单独写场景验证**（时间关系，下一轮可补）。

**剩余**：SLAYER/HUNTER/STEADY/MOONLIT/WARM_GRIP + HUSHED/RAINPROOF/COOLING。

## 品质系统第十阶段：COOLING 与 WARM_GRIP

- **WARM_GRIP**：手持带该词条的武器时，空气温度 `+3×强度`（抵消寒冷）
- **COOLING**：穿着带该词条的护甲且温度 >25°C 时，降 `5×强度`，不低于 25°C
- 两者都在 `Thermal.ambient()` 里，与之前的 WINDBREAK 同一处

编译通过、`Smoke` 9/9、`F1` 3/3。本轮同样未写专项场景（下一轮可补测试）。

**剩余**：SLAYER/HUNTER/STEADY/MOONLIT + HUSHED/RAINPROOF。

## 品质系统第十一阶段：补上词条验证

用一个场景（`Z_PerksTest`，跑完即删）同时验证三条之前只有编译覆盖的词条：

```
exec 100.0 -> 109.0        (EXECUTIONER：目标半血时伤害上升，+9%)
wind 4.0 -> 4.79           (WINDBREAK：风大时气温降幅变小)
warm 4.79 -> 6.14          (WARM_GRIP：手持暖手武器时气温回升)
```

编译通过、`Smoke` 9/9、`F1` 3/3。**SUREFOOT / COOLING / STORMBORN 仍无专项验证**。

## 品质系统第十二阶段：STORMBORN 验证 + STEADY 结论

- **STORMBORN 验证**（`Z_PerkStorm`，跑完即删）：带该词条的长枪，平静天平均伤害 100.0，
  雷暴天 105.0（+5%，与 `20%×强度 0.235` 对应）。
- **STEADY 无法实现**：它在 Warped 里是“抵消风对射击/投掷的影响”，但本仓库**没有任何风影响命中的逻辑**
  （`windX` 只用于视觉与温度）。不想凭空发明一个惩罚机制，所以这条先挂起。

**剩余**：SLAYER、HUNTER、MOONLIT、HUSHED、RAINPROOF（均需额外系统）、STEADY（需先定风影响规则）、
SUREFOOT/COOLING（实现已在，仅缺专项测试）。

## 品质系统第十三阶段：SLAYER（屠戮）生效

- 新增 `items/rarity/SlayerFamily`：按怪物类名归入 **虫鼠 / 野兽 / 亡灵 / 恶魔 / 人形** 五族
  （Warped 原表绑它自己的全怪物库，这里只按本仓库类名判断）
- `Quality.slayer()`：带 SLAYER 词条的武器按实例确定性地指定一个猎物族
- `Weapon.proc`：对同族目标伤害 `+30%×强度`

**实测**（`Z_PerkSlayer`，跑完即删）：一把“虫鼠屠戮”短剑，对老鼠 125.0，
对石像（BEAST）100.0（+25%，与 30%×0.837 对应）。编译通过、`Smoke` 9/9、`F1` 3/3。

## 品质系统第十四阶段：HUNTER（猎手）生效

- `Mob.die(...)`：若击杀者的武器带 **HUNTER**，且死者属于 **野兽/虫鼠** 族，
  按 `15% + 35%×强度` 概率在尸体位置掉一块 `ChargrilledMeat`

**实测**（`Z_PerkHunter`，跑完即删）：同一位置反复杀 40 只老鼠（每次先清掉堆），
`meatDrops=12/40`（约 30%，与 `15%+35%×0.547` 对应）。编译通过、`Smoke` 9/9、`F1` 3/3。

## 品质系统第十五阶段：RAINPROOF（防雨）

- `Thermal.update`：雨天且不在水里时，穿着带 **RAINPROOF** 的护甲会把“湿透”的温度漂移倍率
  从 `1.6` 降到 `1 + 0.6×(1-强度)`（水里不管用）

编译通过、`Smoke` 9/9、`F1` 3/3。**本轮未写专项场景**（与 SUREFOOT/COOLING 一样待补）。

## 品质系统第十六阶段：补齐 SUREFOOT / COOLING / RAINPROOF 验证

- **SUREFOOT**：把坠落伤害计算提取为 `Chasm.fallDamage(hero)` 与 `sureFoot(...)`，
  除了 SkyWalker 分支，**普通坠落路径现在也生效**（之前只在 SkyWalker 里，实际是错位）
- **COOLING**：提取为 `Thermal.coolAir(t)`，方便直接断言
- **RAINPROOF**：已在 `Thermal.update` 里

**实测**（`Z_PerksRest`，跑完即删）：

```
fall 10 -> 8              SUREFOOT：坠落伤害下降
air  32.0 -> 31.99        COOLING：高温读数被拉低（本次抽到的强度极小）
body 29.9808 -> 29.9862   RAINPROOF：淋雨时温度漂移变慢
```

编译通过、`Smoke` 9/9、`F1` 3/3。

## 品质系统第十七阶段：MOONLIT 生效；HUSHED 按要求不做

- **MOONLIT**：`Weapon.proc` 中，夜晚（`DayNightCycle.isNight()`）伤害 `+25%×强度`
- **HUSHED（潜行）按玩家要求不做**：它会改变整体潜行与敌人发现规则，影响面太大

**实测**（`Z_PerkMoonlit`，跑完即删）：一把带“月华”的短剑，白天 100.0，
夜晚 114.0（+14%，与 `25%×0.574` 对应）。编译通过、`Smoke` 9/9、`F1` 3/3。

## 时间系统第一阶段：GameCalendar（星历）搬入

- 搬入 Warped 的 `actors/GameCalendar`（521 行，年/月/日/季节与周期计算）
- 时钟源改成本仓库现有的 `Game.timeTotal`：一天 = `DayNightCycle.FULL_CYCLE`（360s）
- 去掉 Warped 的 `Dungeon.cycleTurn` / `Dungeon.calendarStartDay` / `Challenges.REAL_CLOCK`（本仓库没有），
  改为 `START_DAY = 1` 常量与常假分支

编译通过、`Smoke` 9/9。**尚未接入 UI，也尚未影响温度**。

## 时间系统第二阶段：改成回合制（Warped 的方式）

按玩家选的 A 方案，一天不再是 360 秒，而是随**英雄回合**推进：

- `DayNightCycle.FULL_CYCLE = 2500`（回合），新增 `turns`、`onHeroTurn()`、`daysElapsed()`
- `Hero.act()` 每回合调 `DayNightCycle.onHeroTurn()`
- `GameCalendar` 的日期改从 `DayNightCycle.daysElapsed()` 取，不再按 `Game.timeTotal`
- 相位/色调/亮度曲线不变，只是时间基准由秒换成回合

**实测**（`Z_TimeTurns`，跑完即删）：等待 6 回合后 `turns 1 -> 7`，时钟随英雄行动推进。
编译通过、`Smoke` 9/9、`F1` 3/3。

**未搬**：Warped DayNightCycle 里那些依赖小镇/住所/植物/睡眠的玩法效果（视野距离、刷怪率、亡灵伤害、潜行）。

## 时间系统第三阶段：夜间强化亡灵

- `DayNightCycle.undeadDamageMultiplier()`：夜晚 1.25，白天 1.0
- `Weapon.proc`：对 `SlayerFamily.UNDEAD` 目标的伤害乘上该倍率

**实测**（`Z_NightUndead`，跑完即删）：对骷髅，白天 100.0，夜晚 125.0。
编译通过、`Smoke` 9/9、`F1` 3/3。

## 时间系统第四阶段：季节进温度（ClimateManager 精简版）

- 新增 `actors/ClimateManager`：`seasonalOffset(season)` —— 夏 +6、冬 −8、春 +1、秋 0（℃）；
  `badWeatherChance(season)` —— 季节对恶劣天气概率的倍率
- `Thermal.ambient()` 在深度基线上叠加季节偏移
- `GameCalendar.debugSeason` 供测试强制季节

**实测**（`Z_SeasonClimate`，跑完即删）：同一层，夏天气温 16.2℃，冬天 2.2℃（差 14℃）。
编译通过、`Smoke` 9/9、`F1` 3/3。

**未接**：`badWeatherChance` 还没接进 `Weather.rollForLevel()`；气候水晶与季节事件没搬。

## 时间系统第五阶段：季节影响天气概率

- `Weather.rollForLevel()`：降雨/降雪的基础概率 30% 乘上
  `ClimateManager.badWeatherChance(GameCalendar.season())`（夏 0.8、秋 1.1、春 1.2、冬 1.3）

编译通过、`Smoke` 9/9、`F1` 3/3。**未写专项场景**（概率测试需要大量重复，留待后续）。

## 文本系统修复：可折叠的分段（描述 / 稀有度）

- `ui/SectionHeader` 改为**可点击的按钮**：三角 + 标题 + 细线，点一下折叠或展开它下面的文字（`open` 标记 + `onToggle` 回调）
- `WndInfoItem` 改为 `layoutSections()`：标题 → **描述** 头 → 正文 → **稀有度：X** 头 → 品质行；
  折叠时只留头部，并重算窗口高度（`resize`）

**实测**：截图确认两个分段头与正文排版正常（窗口居中、稀有度「良好」绿色、
品质行「暖手」）。编译通过、`Smoke` 9/9、`F1` 3/3。

**未验证 / 未完成**：点击折叠的交互没有在自测里模拟（无头环境难点 UI）；
三角图标尚未随展开/折叠变化方向。

## 文本系统：用透明按钮覆盖分段头（第三板，不动排版）

上一板把 `SectionHeader` 改成 `Button` 后排版错乱，已回退。这板换个思路：

- 分段头继续用原来的 `SectionHeader`（只管画，不管点击）
- 在头部同一位置叠一个**透明的 `Button`**（`setRect(0,y,width,HEIGHT)`），点击时切换
  `descOpen` / `rarityOpen` 并重跑 `layoutSections()`
- 布局代码只多了一个可见性开关，位置计算没改

编译通过、`Smoke` 9/9、`F1` 3/3。**点击仍无法在无头自测里模拟**（H 接口只有游戏格子的 tap）。
