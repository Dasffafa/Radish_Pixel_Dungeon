# 杂散职业与变身机制（Jumble Hero & Shift System）

> 状态：**已实现**。

## 概述

杂散（Jumble）是独立英雄职业，使用 `jumble.png` 及周期性变身 Buff `JumbleChangeBuff`。开局随机选择其他基础职业之一，直接使用其完整基础初始装备；不采用该职业的可选皮肤。随后从所有基础职业的同阶天赋池抽取4个T1、5个T2与2个通用T3天赋。转职时抽取两个不同的已实现转职作为固定被动身份。

T3 从全部基础职业池抽取两个通用天赋，以及从两个随机转职的专属池抽取三个天赋。T4 恶魔之力抽取两个基础职业通用天赋及两个转职专属天赋；只从有效专属池足以提供至少两个天赋的转职组合中抽取。转职身份在一局内不变，周期变身会重新抽取杂散天赋。

周期性变身：每隔180-220回合，杂散会在6组外观间切换，先跨职业替换专属物品，再变换其余已装备的神器、戒指、武器、护甲，最后按新物品重抽天赋。
---

## 1. 贴图与帧布局（`jumble.png`）

贴图尺寸 **336x192**，按 **12x16** 帧格划分，共 **28 列 × 12 行**。

### 分组结构

共 **6 组**贴图，每组占据 **2 行**，与职业无关（变身随机切到任意一组）：

| 组 | 行号（0 基） |
|----|-------------|
| 第 1 组 | 0-1 |
| 第 2 组 | 2-3 |
| 第 3 组 | 4-5 |
| 第 4 组 | 6-7 |
| 第 5 组 | 8-9 |
| 第 6 组 | 10-11 |

每组行首索引 = `行号 × 28`（行内列偏移 0-27）。

### 第一行（基础动画，列 0 起）

| 动画 | 前 4 组（组 0-3） | 第 5 组（组 4） | 第 6 组（组 5） |
|------|------------------|----------------|----------------|
| 站立 idle | 0-1 | 0-1 | 0-1 |
| 行走 run | 2-7 | 2-7 | 2-7 |
| 死亡 die | 8-11 | 8-11 | 8-11 |
| 攻击 attack | 12-16 | 12-20 | 12-21 |
| 使用 operate | 17-18 | 21-22 | 22-23 |
| 阅读 read | 19-21 | 23-25 | 24-26 |

> 第 5/6 组攻击帧更多，导致 使用/阅读 帧号整体后移（由 `JumbleSprite` 内根据 `group` 动态计算）。

### 第二行（消失/出现动画）

- 列 0-9：**消失（进入隐身 / 变身）**动画
- 列 10-19：**出现（退出隐身 / 变身完成）**动画

---

## 2. 精灵类 `JumbleSprite`

继承 `HeroSprite`，与赌徒/流浪者一样采用自足怪物贴图模板，通过覆盖 `updateArmor()` 定义全部动画。

关键方法：

```java
public void setGroup(int g);              // 切换到指定组（0-5），重建动画并回待机
public int group();                       // 当前组索引

public void playChange(Callback cb);      // 播放当前组的"消失"动画，完成回调
public void playAppear(int newGroup, Callback cb); // 切到新组并播放"出现"动画，完成回调
```

### 倒计时 Buff 的附加

`updateArmor()` 末尾会调用：

```java
if (ch == Dungeon.hero) {
    JumbleChangeBuff.resetCountdownIfMissing();
}
```

- 由于 `HeroSprite` 构造时 `link(hero)` 设置 `ch`，而 `GameScene` 在 `place()` 后会再次调用 `updateArmor()`，此时 `ch == Dungeon.hero` 成立，Buff 被可靠附加（`Buff.affect` 幂等，不重复）。

---

## 3. 变身机制 `JumbleChangeBuff`

位于 `actors/buffs/JumbleChangeBuff.java`，继承 `Buff`。

### Buff 属性

| 属性 | 实现 |
|------|------|
| 类型 | `buffType.POSITIVE`（正面） |
| 永久 | `act()` 中 `spend(TICK)` 每回合保持激活 |
| 不可驱散 | 重写 `detach()` 为 **no-op**（净化药水只清 NEGATIVE，天然免疫；任何显式移除也无效） |
| 持久化 | 存档剩余倒计时与外观组；动画中的临时状态不持久化 |

### 触发流程

`act()` 每回合递减 `turnsRemaining`，归零时：

1. 中断英雄当前行动，并提交变身消耗的一回合；动画锁使用不额外消耗时间的动画接口。
2. 在 `STATE_LOCK` 内先替换职业专属物品，再变换普通装备，最后依据变化后的物品重抽天赋；确定目标外观组并重置180-220回合倒计时。
3. `sprite.playChange(cb)` 播放当前组消失动画，再以 `sprite.playAppear(newGroup, cb)` 展示已提交的新形态。
4. 出现动画完成后解除动画锁，英雄恢复行动；超时直接解锁，读档不重播未完成动画。

> 守卫：`changing` 标志防止动画播放期间重入；变身期间 `act()` 不递减倒计时。

### 天赋随机化（`doTalentMetamorph`）

- 通过 `JumbleTalents.metamorph` 重抽所有槽位，包括未投入点数的天赋；保留槽位点数，且不超过目标天赋上限。
- T1/T2及T3前两个槽位来自全部7个基础职业的同阶通用池，T3后三个槽位来自本局两个转职的特有池。
- 恶魔之力T4按两个通用、两个特有槽位重新抽取；皇冠护甲技能T4保持不变。
- 同层不重复，通用槽避开本局特有池中的同名项，防止特有候选被提前占用。
- 每层最多保留一个因缺少职业专属物品而不可用的天赋；初始、转职、恶魔之力和周期重抽均使用此约束。
- 缺物品统计区分已发放后失去的依赖与首次加点会自动提供的依赖；附着在护甲上的纹章视为持有。特有槽先预留不可用名额，通用槽使用剩余额度。
- 专属物品替换候选同时检查固定双转职的T3/T4可行性；没有可行的跨职业替换时保留原物品，不改变转职身份或减少天赋槽位。
- 排除计数槽、未实装占位天赋、未实装的王牌转职以及当前界面隐藏的十手冠军；转职组合必须具备至少两个有效T4特有天赋。
- 完整保存天赋名称、顺序、零投入槽位、点数、两个转职身份及初始装备来源，读档不重新抽取。旧皮肤索引6的英雄存档在恢复后迁移为杂散职业，保留物品与护甲技能。

### 跨职业天赋资源

- 天赋默认没有资源需求；需要物品或 Buff 的天赋通过 `Talent.requirement()` 声明，只有投入点数后才同步依赖。
- 同步在英雄天赋初始化、加点、读档及杂散随机替换后执行。物品发放按依赖类型记录并随存档保存，避免变身或读档重复发放；丢失背包期间延迟发放。
- 依赖 Buff 在天赋失去后保留已有资源，但相关入口休眠；重新获得依赖时不重置资源。卡牌转职天赋初始化牌组，法师法杖依赖使用内嵌初始魔杖。
- 依赖天赋池为空候选时不发放资源；杂散仅对已投入点数的天赋启用依赖。
- 加点依赖统一在 `Talent.onTalentUpgraded` 入口同步，先准备资源再执行升级效果；界面不承担资源发放，特殊升级及嬗变调用同样适用。草药调配器与法术序列由依赖同步统一发放，避免升级回调重复掉落。
- `Talent.removeRequirementOnLoss()` 默认关闭；草药调配与法术序列启用。失去已投入点数的对应天赋后，同步移除背包（包括失去背包状态）及当前楼层地面上的对应物品，清除发放记录；重新投入点数时重新发放。法术序列监听同时移除，物品内部配置不保留。

### 装备变换（`doEquipmentTransmute`）

| 槽位 | 方式 | 说明 |
|------|------|------|
| 神器 | `transmuteArtifact` | 从 `Generator.Category.ARTIFACT.classes` 全池随机，**忽略唯一性 probs，允许重复**（满足"变身时可重复神器"）；保留诅咒/等级 |
| 戒指 | `ScrollOfTransmutation.changeItem` | 复用原版嬗变卷轴逻辑 |
| 武器 | `ScrollOfTransmutation.changeItem` | 复用原版嬗变卷轴逻辑 |
| 护甲 | `transmuteArmor` | **单独实现**（原版 `changeItem` 不处理护甲）：按同 tier 随机，保留强化等级/诅咒 |
| 职业专属物品 | 跨职业专属池替换 | 先于普通装备和天赋变化处理，附着纹章也参与；专属物品不走嬗变卷轴路径，新物品不在同轮重复变化 |

统一由 `replaceEquipped` 处理：卸下旧物（清诅咒）→ 装备/收集新物 → 恢复快捷栏。

独立职业头像与皮肤共用自定义帧定义，杂散使用完整12×16待机帧，不套用标准英雄的护甲行与横向裁切。

---

## 4. 关键类 / 文件

| 类 / 文件 | 作用 |
|-----------|------|
| `actors/hero/definition/heroes/Jumble.java` | 随机基础职业初始装备，初始化杂散天赋与周期变身 |
| `actors/hero/definition/subclasses/Jumble.java` | 杂散唯一转职定义，无独立固定天赋池 |
| `actors/hero/JumbleTalents.java` | 杂散初始、T3、T4与周期重抽天赋 |
| `items/ClassSpecificItems.java` | 职业专属物品归属、跨职业替换候选与状态继承 |
| `sprites/JumbleSprite.java` | 杂散精灵（6组贴图与变身动画） |
| `actors/buffs/JumbleChangeBuff.java` | 变身倒计时 Buff（触发、阻塞、天赋/装备替换） |
| `assets/sprites/RadishSnDSprite/jumble.png` | 6组×2行贴图（12×16帧格） |
| `assets/messages/actors/actors[ _zh].properties` | 职业、转职与 Buff 文案 |

### 消息键

```properties
actors.hero.heroclass.jumble/desc_short/desc/unlock
actors.hero.herosubclass.jumble/short_desc/desc
actors.buffs.jumblechangebuff.name/desc/countdown
```

---

## 5. 设计约定与坑

- **Buff 不可移除**：`detach()` 覆盖为 no-op，务必在类内自行管理状态，任何外部移除都不会生效。
- **动画与回合分离**：变身逻辑消耗一回合，动画本身不额外推进时间；超时解锁不能重新执行变身提交。
- **护甲变换**：原版 `ScrollOfTransmutation.changeItem()` 不处理 `Armor`，需单独实现按 tier 随机。
- **神器重复**：直接访问 `Generator.Category.ARTIFACT.classes` 全池随机，绕过 `probs` 唯一性，满足变身时可重复神器的需求。
- **线程安全**：物品、天赋与外观组在 `STATE_LOCK` 内同步提交，英雄存档使用同一锁；渲染回调只播放已确定结果并解除动画锁。
