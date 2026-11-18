# 萝卜自走棋（Auto Battler）· 设计说明

> 追加挑战，内部名 `auto_battler`，位掩码 `Challenges.AUTO_BATTLER = 1<<15`。
> 一句话：**买怪、摆位，然后看它们自己打。** 英雄只在场边观战。

## 一局怎么走

1. 开局进 15×15 棋盘：左半边（x 2..6）是玩家布阵区，右半边（x 9..13）是敌方出怪区，
   英雄坐在上方正中的一间封闭包间里观战（镜头跟着英雄，所以正好框住整张棋盘）。
2. **备战**：商店刷 3 个单位，花金币买；花 2 金可刷新；买到的单位进「阵容」。
3. **开战**：按阵容整队重建到棋盘上，敌方按回合数刷一波，双方 AI 自动互打。
4. **结算**：
   - 敌方全灭 → 胜利，拿 5 金（每 3 回合额外 +3），进入下一回合；
   - 己方全灭 → 战败，扣 `2 + 回合/4` 点生命，生命归零则本局结束并结算评级。
5. 回合推进由 UI 每 0.16 秒替英雄「空过」一回合驱动，观战体验接近即时。

## 数值与机制

| 机制 | 规则 |
|---|---|
| 商店费用 | 1~4 金，回合越靠后能刷到越贵的单位（`AutochessPool.costCap`） |
| 初始金币 / 生命 | 8 金 / 10 命（`START_GOLD` / `START_LIFE`） |
| 备战位 | 固定 8 格（`MAX_UNITS`），买到的单位进第一个空格；第 i 格对应棋盘第 i 个站位 |
| 升星 | 备战位里同名同星凑 3 个自动合成，升上来的棋子落在第三个棋子的格子上；2 星血量 ×1.8、3 星 ×3，伤害按 `1 + 0.8×(星-1)` |
| 敌方成长 | 数量 `min(6, 1+回合)`，血量每回合 +10%（上限 +150%） |
| 羁绊 | 兽群（攻速）/ 人形（攻击力）/ 构装（减伤）/ 恶魔（全能增伤），3/5 或 2/4 两档 |
| 存档 | 回合、金币、生命、阵容、商店都在 `AutochessRun` 的 Bundle 里 |

羁绊加成都写在 `AutochessUnitBuff` 的伤害钩子里，存活数量一变立刻生效，不用重挂 Buff。

## 代码地图

```
challenges/autochess/AutochessRun.java       总控：金币/商店/阵容/开战/胜负/存档
challenges/autochess/AutochessPool.java      单位池：费用、羁绊、按回合限制费用上限
challenges/autochess/AutochessUnitBuff.java  己方棋子的阵营、星级与羁绊伤害加成
levels/ChessBoardLevel.java                  15×15 棋盘、观战包间、双方站位
windows/WndAutochessShop.java                备战界面：购买/刷新/开战/羁绊
windows/WndAutochessResult.java              生命归零后的结算
ui/AutochessHUD.java                         顶部状态条 + 自动回合节拍器
```

接入点：

| 文件 | 改动 |
|---|---|
| `Challenges.java` | 新增 `AUTO_BATTLER` 位与名字，`MAX_VALUE` 抬到 `(1<<16)-1` |
| `Dungeon.newLevel()` | 挑战开启时把本层换成 `ChessBoardLevel` |
| `GameScene.create()` | `AutochessRun.ensureStarted()`、挂 `AutochessHUD`、备战阶段弹商店 |

## 已知取舍 / 下一步

- **拖拽换位**：在备战位网格里按住棋子拖到另一格即可换位（第 i 格 = 棋盘第 i 个站位），
  拖动时棋子精灵跟着指针走，松手落到哪格就和哪格交换。实现在
  `WndAutochessShop` 的 `PointerArea` + `AutochessRun.swapBench()`。
- **卖出**：轻点棋子选中（格子变黄框），下方出现「卖出 xxx★（+N 金）」；
  返还价 = 原价 ×（1 星 1 倍 / 2 星 3 倍 / 3 星 9 倍），实现在 `AutochessRun.sell()`。
- **棋子头像**：`ui/AutochessIcons` 临时实例化怪物取 `mob.sprite()` 当图标，备战位与商店列表都显示。
- **没有利息/连胜**：现在只有「赢 +5、每 3 回合 +3、输 +2」的简单经济。
- **阵容会死亡但会保留**：每回合整队重建，所以棋子不会永久损失，输的代价是生命。
- **羁绊只影响己方**，敌方不吃羁绊。
- 战斗中没有技能特效包装，靠现有怪物 AI 的原始表现。
