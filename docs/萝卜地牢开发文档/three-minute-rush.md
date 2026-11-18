# 三分钟一把（Three-Minute Rush）· 设计说明

> 追加挑战，内部名 `three_minute`，位掩码 `Challenges.THREE_MINUTE = 1<<14`。
> 一句话：**三分钟，一场架。** 开局直接扔进竞技场，怪一波波来，清一波选一个强化，时间到结算评级。

## 玩法

- 开局不生成正常楼层，直接进 `ArenaLevel`（21×21 空场 + 四角实心掩体，没有楼梯）。
- 一局 180 回合（约等于三分钟的观感），按回合而不是真实时间计时：读弹窗、看说明不扣时间。
- 每波清空 → 弹出三选一强化 → 选完立刻刷下一波。计时在选强化时暂停。
- 时间归零 → 结算：清到第几波、击杀数、评级（S/A/B/C），可以「再来一把」或回主界面。
- **自动开火**：每 2 个回合自动朝最近的可见怪打一发，边走边射。

## 自动开火（元气骑士手感的核心）

`ArenaAutoFire.fire(hero)` 每回合被 `ArenaRun.tickAutoFire()` 试一次：

1. 在 8 格内找最近的、`hero.fieldOfView` 里看得见的敌对怪；
2. 优先用杖里嵌的法杖（`MagesStaff.wand`），其次背包里任意 `Wand`；
3. 直接走 `wand.fx(bolt, callback)` + `wand.onZap(bolt)`，**不经过 `wandUsed()`**，
   所以整局不消耗充能——这是挑战特权，正常对局完全不受影响；
4. 没有法杖就甩一发内置能量弹（`MagicMissile` + `DamageInfo.MAGICAL`），伤害是武器的一半。

射击间隔 = `max(1, 2 - 连射层数)`；打不出目标时下回合立刻重试，不空转。
`Wand.curUser` / `Wand.curItem` 在开火前后存旧值再还原，避免串到玩家手动施法。


## 爽点从哪来

| 来源 | 数值 | 在哪 |
|---|---|---|
| 开局伤害 ×3 | `START_DAMAGE = 2.0`（叠加乘区 1+2.0） | `ArenaEmpower` |
| 开局吸血 10% | `START_LIFESTEAL` | `ArenaEmpower` |
| 开局抗揍 | 受到怪物伤害 ×0.6 | `ArenaEmpower.modifyIncomingAttackDamage` |
| 开局血量 | 最大生命 ×1.5 并回满 | `ArenaRun.ensureStarted` |
| 清波回血 | 5% 最大生命 | `ArenaRun.waveCleared` |

强化池（每清一波抽三个不重复的，功能类只能拿一次）：

| id | 名字 | 效果 |
|---|---|---|
| `FURY` | 狂怒 | 伤害 +30%，可叠加 |
| `TITAN` | 巨人 | 最大生命 +15% 并回满 |
| `VAMP` | 吸血 | 吸血 +8%，可叠加 |
| `SWIFT` | 疾风 | 攻速 +12%，可叠加（上限折算 2.5 倍速） |
| `REAP` | 收割 | 击杀回 5% 最大生命，可叠加 |
| `CLEAVE` | 顺劈 | 击杀时对周围 2 格溅射 50% 伤害 |
| `SANDS` | 时之沙 | 剩余时间 +15 秒 |
| `EXECUTE` | 处决 | 对 35% 生命以下的敌人双倍伤害 |
| `RAPID` | 连射 | 自动开火间隔 -1 回合（最低 1），拿到即锁定 |

## 波次

- 数量：`min(2 + 波次, 10)`。
- 梯队：每 3 波换一档（一区 → 二区 → 三区 → 四区），见 `ArenaMobs` 的四张表。
- 血量：每波 +8%，上限 +120%。主要难度来自种类和数量，不堆血量。
- 落点：`ArenaLevel.randomRespawnCell` 会尽量把怪放在离玩家 4 格以上的地方，避免贴脸围杀。
- 兜底：一波超过 60 回合没清完，会重新 `beckon` 一次，防止怪被地形卡住导致卡波。

## 代码地图

```
core/.../challenges/ArenaRun.java       导演 Buff：计时、出怪、清波、结算、击杀/吸血事件
core/.../challenges/ArenaEmpower.java   英雄身上的数值成长（伤害/吸血/攻速/减伤）
core/.../challenges/ArenaUpgrade.java   强化池与三选一抽取
core/.../challenges/ArenaMobs.java      波次出怪表
core/.../challenges/ArenaAutoFire.java  自动索敌开火（法杖 / 保底能量弹）
core/.../levels/ArenaLevel.java         竞技场地图
core/.../windows/WndArenaReward.java    三选一窗口
core/.../windows/WndArenaResult.java    结算窗口
core/.../ui/ArenaHUD.java               顶部「时间 ｜ 波次 ｜ 击杀」条
```

接入点（都很小，方便回退）：

| 文件 | 改动 |
|---|---|
| `Challenges.java` | 新增 `THREE_MINUTE` 位与名字，`MAX_VALUE` 抬到 `(1<<15)-1` |
| `Dungeon.newLevel()` | 挑战开启时把本层换成 `ArenaLevel` |
| `GameScene.create()` | 调 `ArenaRun.ensureStarted()`，并挂 `ArenaHUD` |
| `Hero.attackDelay()` | 乘上 `ArenaEmpower.attackDelayFactor`，没有 Buff 时恒为 1 |

文本键：

- 挑战名/说明：`text.textchallenges.three_minute(_desc)`（`text_zh` / `text`）
- 其余：`challenges.arenarun.*`、`challenges.arenaupgrade.*`、`windows.wndarenareward.*`、
  `windows.wndarenaresult.*`、`ui.arenahud.*`、`levels.arenalevel.*`（`misc_zh` / `misc`）

## 已做 / 未做

已做：开局进竞技场、波次刷怪、清波三选一、计时、结算评级、再来一把、HUD、存档字段、法杖自动开火（不耗充能）+ 连射强化。

未做（下一步可以做）：

- 玩家在竞技场里死亡目前走原版死亡流程，不弹竞技场结算（想做「死亡即结算」需要拦 `HeroDeathEvent` 并接管死亡流程）。
- 每 5 波一个精英/Boss 波。
- 连杀（Combo）计数与全屏击杀特效。
- 真正的实时双摇杆：现在是「回合制 + 自动开火」，不是连续移动的实时射击。
- 竞技场主题（现在借用洞窟贴图，四角掩体是实心墙）。
