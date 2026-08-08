# 伤害类型系统设计文档（v3.0）

## 一、概述

本文档描述伤害类型系统的完整设计，包括核心类、伤害计算流程以及使用指南。

### 设计目标

1. **伤害信息完整封装** - DamageInfo包含伤害值、类型、来源、modifier等所有信息
2. **暴击是属性而非类型** - 保留原始伤害类型，暴击作为独立属性
3. **六阶段Modifier系统** - 支持多层伤害修正（加算、乘算），便于天赋/Buff系统扩展
4. **调试友好** - 可追踪从基础伤害到最终伤害的完整计算过程

---

## 二、核心架构

### 2.1 DamageInfo = 伤害计算单元

DamageInfo不再只是数据容器，而是完整的伤害计算单元：

```
基础伤害 → 应用Modifiers → 最终伤害 → 传递给目标
```

**核心职责**：
- 存储**基础伤害值**（baseDamage）
- 管理**伤害Modifier列表**
- 提供`getDamage()`计算并返回最终伤害
- 包装伤害类型、来源、暴击等元信息

### 2.2 Modifier计算顺序（六阶段）

采用六阶段计算公式：

```
最终伤害 = floor(((((基础 + Σ直接加算) × Σ直接乘算) × 暴击倍率 + Σ最终前加算) × Σ最终乘算 + Σ最终加算))
```

**计算阶段说明**：

| 阶段 | Modifier类型 | 示例 | 叠加方式 |
|------|-------------|------|----------|
| 1 | 直接加算 (FLAT_ADDITIVE) | 天赋+10伤害、武器附魔+5 | 累加 |
| 2 | 直接乘算 (DIRECT_MULTIPLICATIVE) | 暴击×1.5、弱点攻击×2 | **累乘** |
| 3 | 暴击倍率 | 独立于modifier列表，便于UI显示 | 单独处理 |
| 4 | 最终前加算 (PRE_FINAL_ADDITIVE) | 固定追加值（在负向乘区前） | 累加 |
| 5 | 最终乘算 (FINAL_MULTIPLICATIVE) | 最终伤害×1.2 | 累乘 |
| 6 | 最终加算 (FINAL_ADDITIVE) | 固定追加+50 | 累加 |

**注意**：
- 乘算采用**累乘**而非累加
- 累乘：`×1.5 × ×1.2 × ×1.1 = ×1.98`
- 优点：多个乘算效果叠加更合理，避免无限膨胀
- 暴击独立于modifier列表，便于UI和旧source兼容
- `PRE_FINAL_ADDITIVE` 是正式的第五个阶段，不得删除或并入 `FINAL_ADDITIVE`

---

## 三、核心类设计

### 3.1 DamageType 枚举

**文件路径**: `damage/DamageType.java`

```java
public enum DamageType {
    // 物理伤害
    PHYSICAL("physical", FloatingText.PHYS_DMG, false, false),
    PHYSICAL_NO_ARMOR("physical_no_armor", FloatingText.PHYS_DMG_NO_BLOCK, false, true),

    // 魔法伤害
    MAGICAL("magical", FloatingText.MAGIC_DMG, true, false),

    // 元素伤害
    FIRE("fire", FloatingText.BURNING, true, false),
    FROST("frost", FloatingText.FROST, true, false),
    LIGHTNING("lightning", FloatingText.SHOCKING, true, false),
    TOXIC("toxic", FloatingText.TOXIC, true, false),
    CORROSIVE("corrosive", FloatingText.CORROSION, true, false),

    // 状态伤害（DoT）
    BLEEDING("bleeding", FloatingText.BLEEDING, false, false),
    POISON("poison", FloatingText.POISON, true, false),
    OOZE("ooze", FloatingText.OOZE, true, false),
    BURNING_STATUS("burning_status", FloatingText.BURNING, true, false),
    CHILL("chill", FloatingText.FROST, true, false),

    // 特殊伤害
    HUNGER("hunger", FloatingText.HUNGER, true, true),
    FALL("fall", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
    CHASM("chasm", FloatingText.PHYS_DMG_NO_BLOCK, false, true),
    DEFERRED("deferred", FloatingText.DEFERRED, false, false),
    CORRUPTION("corruption", FloatingText.CORRUPTION, true, true),
    PICK("pick", FloatingText.PICK_DMG, false, false),
    WATER("water", FloatingText.WATER, true, false),
    AMULET("amulet", FloatingText.AMULET, true, true),

    // 真实伤害（无视一切减免）
    TRUE("true", FloatingText.PHYS_DMG, true, true),

    UNKNOWN("unknown", FloatingText.PHYS_DMG, false, false);

    // 属性方法
    public boolean isMagical();
    public boolean ignoresArmor();
    public boolean ignoresShields();  // TRUE 和 HUNGER 无视护盾
    public boolean isTrueDamage();
    public boolean isPhysical();
    public boolean isElemental();
    public boolean isDoT();
    
    // 兼容方法（迁移完成后删除）
    public static DamageType fromSource(Object source);
}
```

### 3.2 DamageModifier 类

**文件路径**: `damage/DamageModifier.java`

用于表示单个伤害修正项：

```java
public class DamageModifier {
    public enum Type {
        FLAT_ADDITIVE,           // 直接加算
        DIRECT_MULTIPLICATIVE,   // 直接乘算
        PRE_FINAL_ADDITIVE,      // 最终乘算前加算（阶段4）
        FINAL_MULTIPLICATIVE,    // 最终乘算
        FINAL_ADDITIVE           // 最终加算
    }

    private Type type;
    private float value;
    private String source;       // 来源描述（用于调试/日志）
    private Object sourceObject; // 来源对象（可选）

    // 构造
    public DamageModifier(Type type, float value, String source);
    
    // 静态工厂
    public static DamageModifier flatAdd(float value, String source);
    public static DamageModifier directMult(float value, String source);
    public static DamageModifier preFinalAdd(float value, String source);
    public static DamageModifier finalMult(float value, String source);
    public static DamageModifier finalAdd(float value, String source);
}
```

### 3.3 DamageInfo 类（核心）

**文件路径**: `damage/DamageInfo.java`

```java
public class DamageInfo {
    
    // ========== 伤害值相关 ==========
    private int baseDamage;              // 基础伤害值

    // ========== Modifier列表（六阶段）==========
    private List<DamageModifier> flatAdditives;           // 阶段1：直接加算
    private List<DamageModifier> directMultiplicatives;   // 阶段2：直接乘算
    private List<DamageModifier> preFinalAdditives;       // 阶段4：最终乘算前加算
    private List<DamageModifier> finalMultiplicatives;    // 阶段5：最终乘算
    private List<DamageModifier> finalAdditives;          // 阶段6：最终加算

    // ========== 元信息 ==========
    private DamageType type;
    private boolean critical;
    private float criticalMultiplier = 1.5f;  // 阶段3：暴击倍率
    private Char attacker;
    private Item sourceItem;
    private Object source;

    // ========== 伤害计算 ==========
    
    /**
     * 获取最终伤害值（应用所有modifier）
     */
    public int getDamage() {
        return calculateFinalDamage();
    }

    /**
     * 核心计算方法（六阶段）
     */
    private int calculateFinalDamage() {
        float result = baseDamage;

        // 阶段1：直接加算
        for (DamageModifier m : flatAdditives) {
            result += m.getValue();
        }

        // 阶段2：直接乘算（累乘）
        for (DamageModifier m : directMultiplicatives) {
            result *= m.getValue();
        }

        // 阶段3：暴击倍率（独立于modifier列表）
        if (critical) {
            result *= criticalMultiplier;
        }

        // 阶段4：最终乘算前加算
        for (DamageModifier m : preFinalAdditives) {
            result += m.getValue();
        }

        // 阶段5：最终乘算（累乘）
        for (DamageModifier m : finalMultiplicatives) {
            result *= m.getValue();
        }

        // 阶段6：最终加算
        for (DamageModifier m : finalAdditives) {
            result += m.getValue();
        }

        // 至少为0，不会出现负伤害
        return Math.max(0, Math.round(result));
    }

    // ========== Modifier管理（链式调用）==========

    public DamageInfo addFlatModifier(float value, String source);
    public DamageInfo addDirectMultModifier(float value, String source);
    public DamageInfo addPreFinalAddModifier(float value, String source);
    public DamageInfo addFinalMultModifier(float value, String source);
    public DamageInfo addFinalAddModifier(float value, String source);
    
    /**
     * 设置暴击（独立于modifier列表）
     */
    public DamageInfo setCritical(boolean critical);
    public DamageInfo setCritical(boolean critical, float multiplier);

    // ========== 工厂方法 ==========

    public static DamageInfo physical(int baseDamage, Char attacker);
    public static DamageInfo physical(int baseDamage, Char attacker, Item weapon);
    public static DamageInfo physicalNoArmor(int baseDamage, Object source);
    public static DamageInfo magical(int baseDamage, Object source);
    public static DamageInfo fire(int baseDamage, Object source);
    public static DamageInfo lightning(int baseDamage, Object source);
    public static DamageInfo frost(int baseDamage, Object source);
    public static DamageInfo poison(int baseDamage, Object source);
    public static DamageInfo corrosive(int baseDamage, Object source);
    public static DamageInfo bleeding(int baseDamage, Object source);
    public static DamageInfo ooze(int baseDamage, Object source);
    public static DamageInfo burningStatus(int baseDamage, Object source);
    public static DamageInfo trueDamage(int baseDamage);
    public static DamageInfo trueDamage(int baseDamage, Object source);
    public static DamageInfo hunger(int baseDamage);
    public static DamageInfo fall(int baseDamage);
    public static DamageInfo chasm(int baseDamage);
    public static DamageInfo fromSource(int baseDamage, Object source);  // 兼容方法

    // ========== 类型便捷方法 ==========

    public boolean isPhysical();
    public boolean isMagical();
    public boolean isElemental();
    public boolean isDoT();
    public boolean ignoresArmor();
    public boolean ignoresShields();
    public boolean isTrueDamage();

    // ========== 调试工具 ==========

    public String getCalculationTrace();
    public DamageInfo copy();
    public DamageInfo withBaseDamage(int newBaseDamage);
    public DamageInfo withCritical(boolean isCritical);
}
```

### 3.4 DamageResistance 类

**文件路径**: `damage/DamageResistance.java`

```java
public class DamageResistance {
    private Map<DamageType, Float> resistances;
    private Set<DamageType> immunities;

    public void setResistance(DamageType type, float value);
    public void setImmunity(DamageType type, boolean immune);
    public float getResistance(DamageType type);
    public boolean isImmune(DamageType type);

    /**
     * 计算最终伤害（应用抗性减免）
     * 真实伤害无视抗性
     */
    public int calculateDamage(int baseDamage, DamageInfo info);
    
    /**
     * 合并抗性（取最大值）
     */
    public void merge(DamageResistance other);
}
```

### 3.5 DamagePipeline 类

**文件路径**: `damage/DamagePipeline.java`

当前是兼容包装器，最终将实现完整管线：

```java
public final class DamagePipeline {
    
    /**
     * 获取当前活跃的 DamageInfo（ThreadLocal）
     */
    public static DamageInfo activeInfo();
    
    /**
     * 应用伤害（当前为兼容实现）
     */
    public static DamageResult apply(Char target, DamageInfo info);
}
```

**完整管线目标顺序**：
```
输入校验
→ DamageInfo modifier
→ 出手方效果
→ 目标承伤倍率
→ 护甲
→ DamageType 抗性/免疫
→ 护盾
→ Vitae 等额外生命层
→ HP
→ 受伤事件和死亡
→ UI / DamageResult
```

### 3.6 DamageResult 类

**文件路径**: `damage/DamageResult.java`

```java
public final class DamageResult {
    public final int baseDamage;
    public final int modifiedDamage;
    public final int shieldBlocked;
    public final int hpDamage;
    public final boolean immune;
    
    // 后续补充：
    // public final int armorBlocked;
    // public final int resistanceBlocked;
}
```

---

## 四、使用示例

### 4.1 基本物理伤害

```java
// 创建基础伤害
DamageInfo info = DamageInfo.physical(10, hero, sword);

// 添加modifier（链式调用）
info.addFlatModifier(5, "天赋：猎杀直觉")
    .addDirectMultModifier(1.2f, "弱点攻击");

// 获取最终值
int finalDamage = info.getDamage();  // (10+5) × 1.2 = 18

// 应用伤害
enemy.damage(info);
```

### 4.2 暴击伤害

```java
DamageInfo info = DamageInfo.physical(15, hero, sword);

// 设置暴击（独立处理，不作为modifier）
info.setCritical(true);

// 查看计算过程
System.out.println(info.getCalculationTrace());
// 输出:
//   伤害计算过程:
//     基础伤害: 15
//     × 暴击: ×1.5
//   = 最终伤害: 22

enemy.damage(info);
```

### 4.3 复杂modifier组合（六阶段）

```java
DamageInfo info = DamageInfo.physical(20, hero, legendarySword);

// 多阶段modifier
info.addFlatModifier(10, "天赋：砥砺锋芒")      // 阶段1：+10
    .addDirectMultModifier(1.3f, "弱点攻击")     // 阶段2：×1.3
    .setCritical(true, 2.0f)                    // 阶段3：暴击×2.0
    .addPreFinalAddModifier(15, "处决加成")      // 阶段4：+15
    .addFinalMultModifier(1.25f, "最终加成")     // 阶段5：×1.25
    .addFinalAddModifier(50, "固定追加");        // 阶段6：+50

// 计算: ((((20+10) × 1.3) × 2.0 + 15) × 1.25 + 50)
//     = (((30 × 1.3) × 2.0 + 15) × 1.25 + 50)
//     = ((39 × 2.0 + 15) × 1.25 + 50)
//     = ((78 + 15) × 1.25 + 50)
//     = (93 × 1.25 + 50)
//     = 116.25 + 50
//     = 166.25 → 166
int damage = info.getDamage();  // 166
```

### 4.4 魔法伤害

```java
// 法杖伤害
DamageInfo info = DamageInfo.lightning(25, wandOfLightning);
info.addDirectMultModifier(1.5f, "法杖等级加成");

enemy.damage(info);
```

### 4.5 持续伤害（DoT）

```java
// Burning.java
public void act() {
    int baseDamage = damageRoll();
    DamageInfo info = DamageInfo.burningStatus(baseDamage, this);
    
    // 火焰伤害可能有额外加成
    if (target.buff(Vulnerable.class) != null) {
        info.addDirectMultModifier(1.33f, "易伤");
    }
    
    target.damage(info);
}
```

---

## 五、Char.java 集成

```java
/**
 * 新的伤害方法：使用DamageInfo
 * 暴击是DamageInfo的属性，而不是特殊的伤害类型。
 * 此方法将DamageInfo转换为旧格式调用现有逻辑，保持向后兼容。
 */
public void damage(DamageInfo info) {
    DamagePipeline.apply(this, info);
}

/**
 * 兼容实现，仅由 DamagePipeline 调用
 */
public void applyDamageLegacy(DamageInfo info) {
    int dmg = info.getDamage();
    Object src = info.getSource();
    // ... 调用旧实现
}

/**
 * 兼容入口：旧式调用
 */
public void damage(int dmg, Object src) {
    DamageInfo active = DamagePipeline.activeInfo();
    if (active != null) {
        damage(dmg, src, active.getType());
    } else {
        DamageInfo info = new DamageInfo(dmg, DamageType.fromSource(src));
        DamagePipeline.apply(this, info);
    }
}
```

---

## 六、调试与日志

### 6.1 伤害计算追踪

```java
DamageInfo info = DamageInfo.physical(50, hero, sword);
info.addFlatModifier(10, "天赋A")
    .setCritical(true)
    .addPreFinalAddModifier(15, "处决")
    .addFinalAddModifier(20, "最终加成");

// 打印计算过程
GLog.i(info.getCalculationTrace());
/*
伤害计算过程:
  基础伤害: 50
  + 直接加算:
    +10.0 (天赋A)
  × 暴击: ×1.5
  + 最终乘算前加算:
    +15.0 (处决)
  + 最终加算:
    +20.0 (最终加成)
  = 最终伤害: 110
*/
```

### 6.2 Modifier来源追踪

每个modifier携带来源描述，便于：
- 调试时查看伤害为何变化
- UI显示伤害加成来源
- 日志记录战斗过程

---

## 七、文件结构

```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/
├── damage/
│   ├── DamageType.java              # 伤害类型枚举
│   ├── DamageInfo.java              # 伤害计算单元（核心）
│   ├── DamageModifier.java          # Modifier表示类（六阶段）
│   ├── DamageResistance.java        # 抗性管理
│   ├── DamagePipeline.java          # 伤害管线（兼容包装器）
│   ├── DamageResult.java            # 伤害结果
│   ├── DamageSource.java            # 兼容工具类
│   └── OrdinaryAttackDamage.java    # 普通攻击构建器
└── actors/
    └── Char.java                    # damage(DamageInfo) 方法
```

---

## 八、实现状态

| 组件 | 状态 | 备注 |
|------|------|------|
| DamageType | ✅ 已实现 | 19种类型，含 `ignoresShields()` |
| DamageInfo | ✅ 已实现 | 六阶段modifier系统 |
| DamageModifier | ✅ 已实现 | 五种类型（含PRE_FINAL_ADDITIVE） |
| DamageResistance | ✅ 已实现 | 抗性合并规则已明确 |
| DamagePipeline | ⚠️ 兼容实现 | 目前是兼容包装器 |
| DamageResult | ⚠️ 基本实现 | 缺少 armorBlocked/resistanceBlocked |
| Char.damage(DamageInfo) | ✅ 已实现 | 委托给Pipeline |
| OrdinaryAttackDamage | ✅ 已实现 | 普通攻击流程已迁移 |

---

## 九、迁移计划

详见 `docs/TODO/damage-system-migration-inventory.md`

---

## 十、设计优势

| 优势 | 说明 |
|------|------|
| **计算集中** | 所有伤害计算在DamageInfo内完成，不散落各处 |
| **调试友好** | 可追踪每一步计算过程 |
| **扩展性强** | 新增天赋/Buff只需添加modifier |
| **解耦** | Char只负责"应用伤害"，不负责"计算伤害" |
| **向后兼容** | 新方法内部调用旧方法，渐进迁移 |
| **六阶段控制** | PRE_FINAL_ADDITIVE 允许在负向乘区前添加固定值 |

---

*文档版本：3.0*  
*更新日期：2026-08-07*  
*项目：Radish Pixel Dungeon*