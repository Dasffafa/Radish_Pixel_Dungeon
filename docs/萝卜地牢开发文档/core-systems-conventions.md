# 核心系统约定摘要（给开发者 / AI Agent）

> 用途：进入本仓库前的最小共识。替代「凭 SPD 原版经验写代码」。
> 生成方式：逐篇精读 `docs/萝卜地牢开发文档/` 下 7 份核心文档 **并逐条对照源码核实**。
> 凡与文档不一致处以源码为准，差异集中列在 §6「文档与代码已漂移的地方」。

---

## 0. 三条总纲

1. **原版 SPD 的行为不是基线。** TheCatist 重构过伤害、楼层、事件、附魔四条主干，数值与流程都已偏离原版（见各节）。
2. **一切伤害只有一个入口**：`new DamageInfo(...)` → `target.damage(info)` → `DamagePipeline` → `Char.applyDamage`。没有第二种写法。
3. **楼层由 `branchId + depth` 唯一确定**，楼梯由**稳定 `linkId` 严格双向配对**，任何"就近匹配 / 第一个匹配 / 动态配对"都是被明令禁止的旧模式。

---

## 1. 伤害类型系统（damage）

### 1.1 解决的问题

原版把「暴击」「无视护甲」当成特殊 source 类和 `instanceof` 长链来判，导致：伤害类型无法携带、天赋/Buff 无法插队改伤、跳字图标靠类名猜、抗性无法按类型分层。重构后伤害变成**自包含的计算单元 + 单一结算管线**。

### 1.2 核心类型（全限定名，均已核对存在）

| 类型 | 全限定名 | 职责 |
|---|---|---|
| 类型枚举 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamageType` | 25 个类型；自带 `floatingTextIcon` |
| 计算单元 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo` | 基础值 + 6 类 modifier + 暴击 + 类型 + 来源链 + 混合成分 |
| 修正项 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamageModifier`（内部 `enum Type`） | 构建期数值项 |
| 管线 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamagePipeline` | `final` 类，私有构造；`apply(Char, DamageInfo)` |
| 结果 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamageResult` | `final` 类，7 个 `public final` 字段 |
| 抗性表 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamageResistance` | 按类型的抗性/免疫容器（**当前全库无任何引用，属死代码**；语义见 §6.2 第 4 条） |
| 混合伤害 | `com.shatteredpixel.shatteredpixeldungeon.damage.MixedDamage` | 成分 + 占比，`validate()` 强制和为 1 |
| 普通攻击构建器 | `com.shatteredpixel.shatteredpixeldungeon.damage.OrdinaryAttackDamage` | `build(...)` / `rollBaseDamage` / `rollDefenseReduction` |
| 来源链格式化 | `com.shatteredpixel.shatteredpixeldungeon.damage.DamageCauseFormatter` | `nameOf` / `describeChain` |
| 承伤修正钩子 | `com.shatteredpixel.shatteredpixeldungeon.damage.IDamageModifier` | 6 个 default 方法，作用于**已算出的承伤值** |
| 角色入口 | `com.shatteredpixel.shatteredpixeldungeon.actors.Char#damage(DamageInfo)` / `#applyDamage(DamageInfo)` | 前者是公开入口，后者是唯一权威实现 |

### 1.3 六阶段公式（源码逐行确认：`DamageInfo#calculateFinalDamage`）

```
max(0, round(
  (((基础 + Σflat) × (1 + Σ(stack-1)) × Πdirect) × crit + ΣpreFinalAdd)
  × ΠfinalMult + ΣfinalAdd
))
```

- 阶段2 `STACK_MULTIPLICATIVE`：**累加百分比后一次乘**。两个 `1.5f` → `×2.0`
- 阶段3 `DIRECT_MULTIPLICATIVE`：**逐项累乘**。两个 `1.5f` → `×2.25`
- 暴击独立于 modifier 列表，位于 direct 之后、preFinalAdd 之前
- `PRE_FINAL_ADDITIVE` 是正式阶段，**禁止**并入 `FINAL_ADDITIVE`

链式 API（均返回 `this`，都有 `(value, source)` 与 `(value, source, sourceObject)` 两个重载）：
`addFlatModifier` / `addStackMultModifier` / `addDirectMultModifier` / `addPreFinalAddModifier` / `addFinalMultModifier` / `addFinalAddModifier` / `setCritical(boolean[, float])`。

工厂：`DamageInfo.physical(int, Char[, Item])`、`physicalNoArmor`、`magical`、`holy`、`fire`、`lightning`、`frost`、`poison`、`corrosive`、`bleeding`、`ooze`、`burningStatus`、`trueDamage`、`hunger`、`chasm`、`of(int, DamageType, [Char,] Object)`。
> 注意：**没有** `DamageType.fromSource(...)`（已被删除）。

### 1.4 结算顺序（`Char#applyDamage`，行号见 `Char.java`）

```
919  applyDamage(DamageInfo)
939  ① emit CharUnprocedDamageEvent      ← 减免前原始 dmg（不可修改）
943  ② 护甲 DR：仅当 attacker != null && type == PHYSICAL && !ignoresDefenseRoll(attacker)
        dr = OrdinaryAttackDamage.rollDefenseReduction(attacker, this, true)
957  ③ applyRawDamageModifiers：(dmg - dr + ΣrawAddFlat) × (1+ΣrawAddPercent) × ΠrawMultiply
        ⚠ 取整用截断 (int)，不是四舍五入
963  ④ LifeLink 分摊
968  ⑤ 抗性/免疫：isImmuneTo(type) || isImmune(src.getClass()) → 免疫则 dmg=0 且不弹跳字
        resistanceMultiplier = resistanceTo(type) * resist(src.getClass())   ← 双轨
1008 ⑥ applyFinalDamageModifiers：(dmg + Σflat) × (1+Σpct) × Πmult × resistanceMultiplier
        ⚠ 取整用 Math.round
1011 ⑦ Buff.onPreShieldDamage(dmg, hpBefore, info)
1014 ⑧ 护盾 ShieldBuff.absorbDamage（type.ignoresShields() 时跳过）→ 破裂发 ShieldBreakEvent
     ⑨ VitaeBuff 临时生命
     ⑩ HP 扣减 → Grim 处决判定
1064 ⑪ emit CharFinalDamageEvent（dealt > 0 才发）
1073 ⑫ Buff.onDamageTaken(dealt, info)（快照迭代，允许 detach）
1078 ⑬ 跳字：info.getFloatingTextIcon() / getFloatingTextIcons() + DamagePalette 取色
1091 ⑭ lastDamageType / lastAttacker / lastDamageCauseChain
```

### 1.5 抗性 / 免疫（双轨制，**未合并**）

- 新层：`Char.typeResistances` / `Char.typeImmunities`（`HashMap<DamageType,Float>` / `HashSet<DamageType>`，`protected final`）
  查询：`resistanceTo(DamageType)`、`isImmuneTo(DamageType)`
- 旧层：`Char.immunities` / `resistances`（基于 `Class`）
- Buff 层：`Buff.typeResistances` / `Buff.typeImmunities`（`protected` 字段，子类构造里 `add`/`put`）
- 结算同时乘两条：`resistanceTo(type) * resist(src.getClass())`
- **抗性值被 `clampEffectiveness` 夹到 `[0,1]`** —— 无法用 `1.5f` 表达"易伤"

### 1.6 混合伤害

`MixedDamage.add(type, pct)`（禁 MIXED/UNKNOWN/TRUE，pct ∈ [0,1]）→ `validate()`（和必须 =1，否则抛 `IllegalArgumentException`）→ `DamageInfo.setMixedDamage(md)` 自动 validate 并置 `type = MIXED`。
结算按成分加权：`Σ pct × resistanceTo(comp) × resist(srcClass)`，全免 → `immuneHit = true`。

### 1.7 扩展点（新代码应优先用这些，不要到处 `instanceof`）

| 需求 | 用哪个 |
|---|---|
| 攻击方改伤害 | `Buff#modifyOutgoingAttackDamage` / `modifyPreFinalOutgoingAttackDamage` / `modifyFinalOutgoingAttackDamage` |
| 受击方改伤害 | `Buff#modifyIncomingAttackDamage` |
| 承伤值级修正（抗性前/后） | 实现 `IDamageModifier`，被 `Char#collectDamageModifiers` 收集（Buff 与护甲刻印自动纳入） |
| 受击后副作用 | `Buff#onDamageTaken(dealt, info)` |
| 护盾前反应 | `Buff#onPreShieldDamage(dmg, hpBefore, info)` |
| 武器改写来源/类型 | `Weapon#modifyDamageInfo(attacker, defender, info)` |
| 无视防御掷骰 | `Weapon#ignoresDefenseRoll()` 返回 true（或攻击时走 `PHYSICAL_NO_ARMOR`） |

---

## 2. 事件系统（events）

### 2.1 解决的问题

模块间解耦通信；订阅者**零手工注册**——编译期注解处理器扫 `@SubscribeEvent`，生成 `EventSubscriberIndex`。

### 2.2 核心类型

| 类型 | 全限定名 | 所在模块 |
|---|---|---|
| 事件基类 | `com.shatteredpixel.shatteredpixeldungeon.events.GameEvent` | **`event-annotations`**（独立 gradle 模块，不是 core） |
| 注解 | `com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent` | `event-annotations`；`@Retention(SOURCE)`、`@Target(METHOD)` |
| 总线 | `com.shatteredpixel.shatteredpixeldungeon.events.EventManager` | core |
| 订阅者描述 | `com.shatteredpixel.shatteredpixeldungeon.events.Subscriber` | core |
| 生成索引 | `com.shatteredpixel.shatteredpixeldungeon.events.EventSubscriberIndex` | **编译期生成**，勿手改 |
| 处理器 | `com.shatteredpixel.shatteredpixeldungeon.events.processor.EventSubscriberProcessor` | `event-processor` |

### 2.3 用法（三步）

1. **定义事件**：`extends GameEvent`，放 `core/.../events/` 包，字段私有 + getter。
2. **订阅**：`public static void` + 单参数（类型必须等于 `event()`）+ 注解
   ```java
   @SubscribeEvent(event = HeroMoveEvent.class, priority = 0)
   public static void onHeroMove(HeroMoveEvent event) { ... }
   ```
   处理器只校验 **static / 单参数 / 参数类型可赋值**，不校验 `public`（但请写 `public static`，与全库 29 个现有订阅点一致）。
3. **发布**：`EventManager.emit(new XxxEvent(...))`；需要"能否决"时先 `emit` 再查 `event.isCancelled()`。

### 2.4 语义要点

- 优先级**数字大者先执行**；`emit` 内是"升序排序 + 倒序遍历"。
- 任一订阅者 `cancel()` 后，后续订阅者**全部跳过**（`emit` 循环里 `break`）。
- 首次调用在 `Dungeon.init()`（`Dungeon.java:247`）与 `Dungeon.loadGame()`（`:815`）自动完成；`EventManager.init()` 用反射加载索引类。
- **分发按 `event.getClass()` 精确匹配，无多态派发**：订阅父类事件收不到子类事件。

### 2.5 已有的伤害事件

| 事件 | 时机 | 携带 |
|---|---|---|
| `CharUnprocedDamageEvent` | 减免前 | `target/attacker/source/damage/type`，**不可取消**、无修改入口 |
| `CharFinalDamageEvent` | 减免后 | 额外 `sourceItem/dealt/hpBefore/hpAfter`，`dealt>0` 才发布 |

---

## 3. 分支楼层 + 楼梯配对

### 3.1 解决的问题

原版 `Level.getTransition(Type)` 只返回**第一个**匹配楼梯 → 多层多入口时会"串位"，且错误随存档永久固化。现在改为 `branchId + depth` 定位楼层 + `linkId` 严格双向配对。

### 3.2 核心类型

| 类型 | 全限定名 |
|---|---|
| 分支配置 | `com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branch` |
| 分支注册表 | `com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches`（`MAIN/MOSS/MINING/CONVICT`） |
| 楼梯数据 | `com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition`（内部 `enum Type`、`enum Direction`） |
| 层 | `com.shatteredpixel.shatteredpixeldungeon.levels.Level`（`transitions` 列表 + `requireTransition(String)`） |
| 过渡场景 | `com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene`（静态字段 `curTransition`） |
| 全局状态 | `com.shatteredpixel.shatteredpixeldungeon.Dungeon`（`branchId`、`depth`、`loadOrCreateLevel(int,String,int)`） |

`LevelTransition` 的字段只有这些：`linkId`、`direction`、`destBranch`、`destDepth`、`centerCell`、`type`。

### 3.3 注册分支（`Branches.init()`）

```java
public static final String GARDEN = "garden";          // 稳定 ID，勿改
// init() 里 register(createGardenBranch());
Class<? extends Level>[] levels = new Class[3];        // 索引 = 支线内部深度，0 号位占位
levels[1] = GardenLevel.class; levels[2] = GardenBossLevel.class;
return new Branch(GARDEN, 2, "branch_garden", levels); // maxDepth 必须 = 最大有效索引
```

### 3.4 建楼梯（4 个工厂，方向/类型/地形必须自洽）

| 工厂 | direction | type | 地形 |
|---|---|---|---|
| `LevelTransition.regularEntrance(level, cell)` | `UP` | `REGULAR_ENTRANCE` | `Terrain.ENTRANCE` |
| `LevelTransition.regularExit(level, cell)` | `DOWN` | `REGULAR_EXIT` | `Terrain.EXIT` |
| `LevelTransition.branchUp(level, cell, linkId, destBranch, destDepth)` | `UP` | `BRANCH_ENTRANCE` | `Terrain.ENTRANCE` |
| `LevelTransition.branchDown(level, cell, linkId, destBranch, destDepth)` | `DOWN` | `BRANCH_EXIT` | `Terrain.EXIT` |

- `regularEntrance/Exit` 的 `linkId` 由 `regularLinkId(branch, upperDepth)` 生成：`<branch>:<n>-<n+1>`。
- 主线第 1 层 `regularEntrance` 自动降级为 `surface()`（`destDepth = 0`，`linkId = "surface"`）。
- 支线入口**必须**手写稳定 `linkId`，推荐 `<支线>:<来源分支>-<来源层数>[:后缀]`，如 `garden:main-7`。
  禁止随机数 / `hashCode` / 生成顺序构造。
- 两端都必须 `level.transitions.add(t)` **且** `Painter.set(level, cell, Terrain.XXX)`。

### 3.5 过渡时的强制校验（`InterlevelScene#travel`）

```
682  source = curTransition                       // null → IllegalStateException
688  level = Dungeon.loadOrCreateLevel(slot, source.destBranch, source.destDepth)
689  arrival = level.requireTransition(source.linkId)      // 缺失/重复都抛
691  if (arrival.destBranch != 来源branch || arrival.destDepth != 来源depth) 抛
695  if (arrival.direction != source.direction.opposite()) 抛
699  Dungeon.switchLevel(level, arrival.cell())
```

`Level#requireTransition(linkId)`：0 命中 → `"Missing transition ..."`；>1 命中 → `"Duplicate transition ..."`。
`Dungeon#loadOrCreateLevel`：未知分支 / 越界层数 → `IllegalArgumentException`。

### 3.6 支线内部相邻层

用 `regularEntrance` / `regularExit`（自动生成 `garden:1-2` 这种 ID）。支线**第一层**回主线必须用 `branchUp` + 显式 `linkId`，不能用 regular。
最后一层不要建超出 `maxDepth` 的出口。

### 3.7 非楼梯移动

传送、测试传送器、复活、法术返回 → 走 `InterlevelScene.Mode.RETURN` / 各自专用流程（`returnTo()`），**不要伪造 `linkId` 或一对假楼梯**。坠落仍走 `FALL`。

---

## 4. 不消耗时间播放动画

### 4.1 解决的问题

需要播一段英雄演出（变身、特殊动作）但**不推进回合**，同时期间英雄不能操作，播完立刻恢复。

### 4.2 关键点（`com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero`）

| 成员 | 作用 |
|---|---|
| `public boolean animationBusy` | "正在播放不耗时阻塞动画"标志（`Hero.java:358`） |
| `public void playAnimationNoTime(Callback startAnim)` | 置 `ready=false`、`animationBusy=true`，再 `startAnim.call()`（`:1442`） |
| `public void finishAnimationNoTime()` | `animationBusy=false` + `ready()`（`:1453`） |
| `Hero.act()` 顶部 | `if (animationBusy) return false;`（`:1339`）——不行动、不 spend |

### 4.3 正确写法

```java
hero.playAnimationNoTime( () -> {
    sprite.playChange( () -> {
        try { doTalentMetamorph(hero); doEquipmentTransmute(hero); }
        catch (Exception e) { /* 必须捕获，否则收不了尾 */ }
        sprite.playAppear( Random.Int(6), () -> hero.finishAnimationNoTime() );
    });
});
```

多段动画：`finishAnimationNoTime()` 只放**最后一段**的回调里。

---

## 5. 附魔 / 诅咒 SOP

### 5.1 基类与目录

| 类型 | 基类全限定名 | 目录 |
|---|---|---|
| 武器附魔/诅咒 | `com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment` | `items/weapon/enchantments/`、`items/weapon/curses/` |
| 护甲刻印/诅咒 | `com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph` | `items/armor/glyphs/`、`items/armor/curses/` |
| 临时等级/力量 | `com.shatteredpixel.shatteredpixeldungeon.items.curses.ITempLevelCurse` | `items/curses/` |

必须实现 `proc(...)`（武器：`proc(Weapon, Char attacker, Char defender, int damage)`；护甲：`proc(Armor, Char, Char, int)`）+ `glowing()`；诅咒额外 `curse()` 返回 `true`。

### 5.2 注册（漏一个就出问题）

| 位置 | 必须做什么 | 漏了会怎样 |
|---|---|---|
| `Weapon.Enchantment.common/uncommon/rare` | 正面附魔入对应稀有度数组 | 永不自动生成 |
| `Weapon.Enchantment.curses` | 诅咒入数组 | 永不进随机池/图鉴 |
| `Armor.Glyph.common/uncommon/rare`、`.curses` | 同上 | 同上 |
| `com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike#effectTypes` | **每个**武器附魔/诅咒加一条 `Class → MagicMissile.XXX_CONE` | 元素打击 NPE（`effectTypes.get()` 拆箱） |
| `custom.testmode.CustomWeapon#enchPrio` | 测试用 | 测试武器选不到 |
| `custom.testmode.generator.TestMelee#generateEnchant` | 可选 | 测试生成器不产出 |

> `effectTypes` 里有 `effectTypes.put(null, MAGIC_MISS_CONE)` 兜底"无附魔"，**但没有兜底"未注册的附魔"**。

### 5.3 数值成长规范

- 等级：武器 `Math.max(0, weapon.buffedLvl())`；护甲 `Math.max(0, armor.procLvl())`（含纹章 +1）
- 奥术倍率：`RingOfArcana.enchantPowerMultiplier(owner)`
- 触发概率：`procChanceMultiplier(attacker)`（武器，`protected`，内部已含 Arcana + Berserk）；护甲同名方法参数是 `defender`
- 推荐形式：`procChance = f(level) * procChanceMultiplier(...)`
- 力量需求修正放在现有计算**之后**：`req += curseStrReqMod()`
- 攻速/命中：改 `Weapon#delayFactor()` / `Weapon#accuracyFactor()`；护甲改 `Armor#speedFactor()` / `evasionFactor()` / `stealthFactor()`

### 5.4 本地化 key 规则

`core/src/main/assets/messages/items/items.properties` + `items_zh.properties`：
`items.weapon.enchantments.<小写类名>.name/desc/elestrike_desc`，诅咒用 `items.weapon.curses.*`，刻印用 `items.armor.glyphs.*`。
**内部类用 `$`**：`items.armor.curses.swamp$waterlogged.name`。

---

## 6. 易踩坑清单

### 6.1 文档与代码已经漂移（最危险的坑）

| 文档说法 | 代码事实 | 出处 |
|---|---|---|
| `damage-type-system-design.md` §5：保留 `Char.damage(int,Object)` 兼容入口，内部走 `DamageType.fromSource` | **已删除**。`Char` 只有 `damage(DamageInfo)` 与 `applyDamage(DamageInfo)`；源码注释写明"旧调用兼容入口已被移除" | `actors/Char.java:898-917`；全库 `fromSource` 0 命中 |
| 同文档 §8 / test-log §6：还剩 ~90 文件、~260 处 `damage(int,Object)` 未迁移 | 迁移已完成。全库残留调用点**全部**是 `damage(new DamageInfo(...))` 形态，`damage(int,Object)` 方法不存在 | 同上；`grep -rnE '\.damage\([^)]*,[^)]*\)'` 结果均为 `DamageInfo` 构造 |
| test-log §二：护甲在承伤倍率**之后**平坦扣减，即"先乘后减" | **相反**：DR 作为 `-armorReduction` 并入原始固定加算桶，`(dmg - dr + Σflat) × (1+pct) × Πmult`，即"先减后乘" | `actors/Char.java:1127-1135` |
| test-log §四：跳字保留 `NO_ARMOR_PHYSICAL_SOURCES` 等特例链 | 已改为 `info.getFloatingTextIcon()` / `getFloatingTextIcons()` + `DamagePalette` | `actors/Char.java:1078-1093` |
| `stair_pair_id_system.md` 全篇：`pairId` + `StairRegistry` + `Level.stairCursor` + `getTransition(Type, pairId)` | **整套已废弃**。现行为 `linkId` + `Level.requireTransition(String)`；`StairRegistry` 类不存在；`LevelTransition` 无 `pairId` 字段 | `LevelTransition.java:34`；`Level.java:635`；`InterlevelScene.java:682-700` |

> 结论：**`stair_pair_id_system.md` 视为历史文档**，楼梯只以 `branch_system.md` + 源码为准。README 里仍把它标为 ✅ 已实现，是过时标注。

### 6.2 伤害系统

1. **禁用任何"兼容入口"心态**：不要新增 `damage(int, Object)` 重载，也不要按类名猜类型。类型必须显式给。
2. **护甲只对 `type == PHYSICAL` 生效**（不是 `!type.ignoresArmor()`）。自定义类型想被护甲减伤，必须用 `PHYSICAL`。
   无攻击者（陷阱、DoT、环境）**不套护甲**。
3. **两段取整不一致**：raw 段 `(int)` 截断，final 段 `Math.round`。跨阶段搬数值会差 1。
4. **两套抗性语义相反，别混用**：`Char.typeResistances` 存的是**乘数**（`1.0f` = 无减免，`0.5f` = 半伤）；`DamageResistance` 存的是**减免比例**（`0f` = 无减免，`1f` = 全免）。后者全库无人引用（死代码），新代码一律用 `Char.typeResistances`。
   另外 `typeResistances` 被 `clampEffectiveness` 夹到 `[0,1]`，写 `1.5f` 得到的是 `1.0f` —— **易伤必须用 `addDirectMultModifier`**。
5. **免疫会抑制跳字**（`immuneHit` 时不弹 "0"），但 `CharFinalDamageEvent` 也因 `dealt == 0` 不发 —— 依赖事件做"被免疫"反馈会收不到。
6. **双用途类（Frost/Chill/Poison/Burning）只做了"免疫 ADD、减免保留 Class"**：它们同时驱动 debuff 时长，pipeline 里是双轨相乘。给它们再加 `typeResistances` 会造成 `0.5 × 0.5` 双计数。
7. `MixedDamage.validate()` **必须调用**（`setMixedDamage` 会自动调）；手动 `new MixedDamage()` 后忘了 validate，加权结果会错但不报错。
8. 嵌套伤害用 `DamagePipeline.activeInfo()` 取上下文；`ACTIVE` 是 `ThreadLocal`，**跨线程不继承**。
9. `DamageResult` 的不变式：`modifiedDamage - armorBlocked - resistanceBlocked - shieldBlocked = hpDamage`。别用 `modifiedDamage` 当"实际伤害"。
10. `DamageInfo.getDamage()` 每次调用都重算，**不是缓存**；在 `addXxxModifier` 之后再调用。

### 6.3 事件系统

11. 订阅方法**必须 `static`**，否则注解处理器直接编译报错（`Diagnostic.Kind.ERROR`）。
12. 注解是 `RetentionPolicy.SOURCE` —— 运行时不存在，**不要试图反射扫描**；只能靠重新编译生成 `EventSubscriberIndex`。
13. **新增/改名事件后必须完整重编译**，增量编译偶尔不重跑处理器 → 运行期"订阅者一个都没注册"且只在 logcat 里有一行 `EventSubscriberIndex load failed`。
14. **按精确类分发**：`emit(new SubEvent())` 不会触发订阅 `BaseEvent.class` 的方法。
15. `cancel()` 是"终止整条链"，不是"跳过自己"；高优先级订阅者一 cancel，低优先级全部收不到。
16. 不要在订阅方法里 `emit` 同类事件（无限循环）；处理器不会拦你。
17. 订阅伤害事件时判空 `Dungeon.hero` / `Dungeon.level` / `heroFOV` / 目标 `pos` 范围。

### 6.4 分支 / 楼梯

18. **禁止恢复 `destType` / `destBranchId` / 整数 `branch`**（`branch_system.md` 明确写死）。现字段是 `type` / `destBranch` / `destDepth` / `linkId`。
19. **禁止模糊匹配**：不要用 `getTransition(Type)` 决定到达点（它只返回第一个匹配，是串位的根因），只用 `requireTransition(linkId)`。
20. `linkId` 必须**稳定**（存档后不变）。随机数 / `hashCode` / 生成顺序全部禁止。
21. 同一层重复 `linkId` 直接抛 `Duplicate transition`；两端漏建一端抛 `Missing transition`。**目标层首次生成路径必须包含那一端**，否则第一次进入就崩。
22. 两端必须**方向相反**、`destBranch/destDepth` **互相指回**；`travel()` 会逐一校验。
23. 方向/类型/地形三者要配套（`branchDown` → `DOWN` + `BRANCH_EXIT` + `Terrain.EXIT`）。`Entrance` 语义是"玩家进入本层的落点"，从那里离开是**上楼**。
24. 支线层数**从 1 开始独立计**，`levels[]` 索引 0 是占位。不要把主线层数当支线层数（进花园第一层永远是 `garden:1`）。
25. 动态来源层（Mining 模式）必须**持久化** `entranceDepth`，不能从当前支线深度反推。
26. 非法分支/层数会直接抛异常，**不再生成 `DeadEndLevel` 兜底**（`DeadEndLevel` 现在只作 `levels[0]` 占位）。

### 6.5 动画

27. **回调里不调 `finishAnimationNoTime()` = 英雄永久卡死**（`animationBusy` 永真，`act()` 永远 `return false`）。
28. 回调链里**任何异常都会跳过后续回调**（含收尾），中间逻辑必须 try/catch。
29. 动画期间**不能 `spend` / `spendAndNext`**。误调 `doEquip/doUnequip` 会推进回合并破坏"不耗时"语义。
30. `hero.busy()` **不阻止行动**（只设 `ready=false`，本项目 `Hero.act()` 在 `!ready` 时仍会跑 `ready()`/`curAction`）—— 必须用 `animationBusy`。
31. `animationBusy` 是公开字段、全局唯一语义：同一时刻只允许一段不耗时动画。

### 6.6 附魔 / 诅咒

32. `ElementalStrike#effectTypes` 漏注册 = NPE（`get()` 返回 `null` 拆箱）。
33. `Weapon#delayFactor()` 在一次攻击中被**多次调用**（Strongman 伤害计算），里面写 `Random` 会多次 roll，最终以最后一次为准 → 概率类逻辑别放这里。
34. `buffedLvl()` 分支多（GoldRadish / RiverCrystal / GIFT / 诅咒），改等级逻辑要逐个覆盖。
35. 内部类消息 key 用 `$`，不是 `.`。
36. 诅咒 `glowing()` 惯例 `0x000000`；正面附魔不要重写 `curse()`。
37. 本项目**不考虑存档兼容**（`enchantment-curse-sop.md` §四.6 引用 `AGENTS.md`）。

---

## 7. 写新代码时的检查清单

### 7.1 任何改动的通用前置

- [ ] 这段逻辑在**原版 SPD** 里是怎么写的？如果是，先假设它在萝卜地牢里**已经不一样**，去 §1–§5 找对应系统。
- [ ] 涉及的类名/方法名在源码里 grep 过一遍（本文档所有 FQN 都可直接搜到）。
- [ ] 没有重新引入 §6.1 表格里的任何"文档旧说法"。

### 7.2 造成伤害

- [ ] 用 `new DamageInfo(base, DamageType.XXX, attacker, sourceItem, source)` 或 `DamageInfo.of/physical/magical/...` 工厂，**类型显式**。
- [ ] 攻击者存在时传 `attacker`（否则护甲阶段整个跳过）。
- [ ] 加伤选择正确的阶段：百分比增益用 `addStackMultModifier`，必须叠乘的用 `addDirectMultModifier`，固定值用 `addFlatModifier`，结算后追加用 `addFinalAddModifier`。
- [ ] 需要"无视护甲"→ 用 `DamageType.PHYSICAL_NO_ARMOR`（不要自己减 DR）。
- [ ] 需要"无视一切"（即死/献祭）→ `DamageType.TRUE`（跳过抗性与护盾）。
- [ ] 需要反应/后续效果 → 用 §1.7 的钩子，不要在 `damage()` 调用点前后手动判断。
- [ ] 调 `target.damage(info)`，**不要**直接 `target.HP -= x`（除 `Char#heal` 是唯一治疗入口这个对称约定外）。
- [ ] 需要统计/展示 → 读 `DamageResult` 字段，别用 `info.getDamage()` 当实际扣血。

### 7.3 加 Buff / 天赋改伤

- [ ] 承伤值级修正实现 `IDamageModifier`（`rawAddFlat` / `rawAddPercent` / `rawMultiply` / `finalAddFlat` / `finalAddPercent` / `finalMultiply`），放进 `Char#collectDamageModifiers` 能收集到的地方（Buff 或护甲刻印）。
- [ ] 抗性/免疫优先用类型层：`typeResistances.put(DamageType.X, 0.5f)` / `typeImmunities.add(DamageType.X)`；只有需要驱动 debuff 时长时才保留 Class 层。
- [ ] 不要同时注册 Class 层与 Type 层的**同一条减免**（会双乘）。

### 7.4 加事件

- [ ] 事件类 `extends GameEvent`，放 `core/.../events/`，只暴露 getter。
- [ ] 订阅方法 `public static`、单参数、类型严格等于 `event()`。
- [ ] 优先级留空隙（0 / 100 / 200…），避免后续插入时全库改数字。
- [ ] 需要"可否决"就在文档注释里写明，并在发布点检查 `isCancelled()`。
- [ ] 改完**全量重编译**，并在 logcat 确认 `EventSubscriberIndex loaded successfully`。

### 7.5 加楼层 / 支线

- [ ] `Branches.init()` 里注册了，`branchId` 常量是稳定字符串。
- [ ] `maxDepth` == `levels` 数组最大有效索引，索引 1..maxDepth 全部非 null。
- [ ] 每对楼梯 `linkId` 唯一、稳定、两端相同。
- [ ] 两端 `destBranch/destDepth` 互指、方向相反。
- [ ] `UP → Terrain.ENTRANCE`，`DOWN → Terrain.EXIT`，`level.transitions.add(...)` 与 `Painter.set(...)` 都做了。
- [ ] **目标层生成路径里创建了对应端点**（否则 `Missing transition`）。
- [ ] 同层无重复 `linkId`；多入口各自不同 ID。
- [ ] 动态来源层已持久化。
- [ ] 支线最后一层没有超出 `maxDepth` 的出口。
- [ ] 传送/复活类移动**没有**被伪造成楼梯。

### 7.6 加附魔 / 刻印

- [ ] `proc(...)` 签名正确，`glowing()` 返回颜色。
- [ ] 进了对应稀有度数组或 `curses` 数组。
- [ ] **武器附魔：`ElementalStrike#effectTypes` 加了一条映射。**
- [ ] 等级用 `buffedLvl()` / `procLvl()` 并 `Math.max(0, ...)`。
- [ ] 概率用 `procChanceMultiplier(...)`，未裸用 `Random.Float()`。
- [ ] 中英文 `name` + `desc`（+ `elestrike_desc`）齐全，内部类用 `$`。
- [ ] 诅咒 `curse()` 返回 `true`，`glowing()` 用 `0x000000`。
- [ ] 需要临时等级/力量 → 实现 `ITempLevelCurse`，护甲侧同时检查刻印与纹章。

### 7.7 加不耗时动画

- [ ] `hero.playAnimationNoTime(...)` 包裹，**最后一段**回调里 `hero.finishAnimationNoTime()`。
- [ ] 中间逻辑 try/catch。
- [ ] 全程无 `spend` / `spendAndNext`。
- [ ] 同一时刻没有第二段 `animationBusy` 动画。

### 7.8 收尾

- [ ] 静态搜索确认没有遗留旧 API：`fromSource`、`pairId`、`StairRegistry`、`damage(int,Object)`、`destType`。
- [ ] `git diff --check`。
- [ ] 编译：`JAVA_HOME=<jdk21> sh gradlew :core:compileJava`（维护者本机路径见 `enchantment-curse-sop.md` §三）。

---

## 8. 出处索引

| 结论 | 文档 | 源码 |
|---|---|---|
| 六阶段公式、链式 API、工厂 | `damage-type-system-design.md` §2–§4 | `damage/DamageInfo.java:148-213`, `:222-380`, `:627-733` |
| 结算顺序、护甲、抗性、护盾、跳字 | `damage-type-system-design.md` §5；`damage-system-refactor-test-log.md` §一–§五 | `actors/Char.java:919-1100`, `:1126-1147` |
| 混合伤害 | 同文档 §3.6、§4.4；test-log §十 | `damage/MixedDamage.java:45-100` |
| 承伤修正钩子 | test-log §二、§五 | `damage/IDamageModifier.java`；`actors/buffs/Buff.java:91-111` |
| 来源链 | test-log §九 | `damage/DamageCauseFormatter.java:53,76`；`damage/DamageInfo.java:430-467` |
| 事件三步用法、优先级、自动初始化 | `event-system-usage.md` 全篇 | `events/EventManager.java:29-133`；`event-annotations/.../GameEvent.java`；`Dungeon.java:247,815` |
| 变量处理器校验规则 | 同文档「订阅方法要求」 | `event-processor/.../EventSubscriberProcessor.java:78-97` |
| 分支注册、linkId 命名、四工厂、校验 | `branch_system.md` §1–§10 | `levels/branches/Branches.java:45-200`；`levels/features/LevelTransition.java:34-140` |
| 严格匹配与双向校验 | `branch_system.md` §4；`stair_pair_id_system.md`（仅"问题背景"一节仍成立） | `scenes/InterlevelScene.java:682-700`；`levels/Level.java:635-647` |
| 无耗时动画 | `animation-no-time-block.md` 全篇 | `actors/hero/Hero.java:358,1339,1442-1456` |
| 附魔/诅咒注册与坑 | `enchantment-curse-sop.md` §一–§五 | `items/weapon/Weapon.java:558-600,511`；`items/armor/Armor.java:1245-1300`；`.../duelist/ElementalStrike.java:113-140,193` |
