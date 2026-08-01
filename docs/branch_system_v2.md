# 分支系统 V2 - 字符串 ID 重构

## 概述

本次重构将分支系统从 `int branch` 整数类型改为 `String branchId` 字符串类型，使分支成为独立的地牢，拥有独立的层数计数器和配置。

## 核心变更

### 1. Dungeon.java

```java
// 旧
public static int branch;

// 新
public static String branchId = Branches.MAIN;
```

**影响的方法**：
- `init()` - 初始化为 `Branches.MAIN`
- `newLevel()` - 使用 `Branches.get(branchId).createLevel(depth)`
- `seedForDepth()` - 使用 `branchId.hashCode()` 生成种子
- `saveLevel()` / `loadLevel()` - 文件路径包含 branchId
- 存档兼容：旧存档 `branch=0/1/2` 自动转换为字符串

### 2. 新增类

#### Branch.java - 分支配置

```java
public class Branch {
    public final String id;              // 唯一标识
    public final int maxDepth;           // 最大层数
    public final String displayNameKey;  // 国际化 key
    
    public Level createLevel(int depth);  // 创建关卡实例
    public boolean hasMoreDepth(int depth); // 是否有下一层
}
```

#### Branches.java - 分支注册表

```java
public class Branches {
    // 预定义常量
    public static final String MAIN = "main";
    public static final String MOSS = "moss";
    public static final String MINING = "mining";
    
    // 注册/获取
    public static void register(Branch branch);
    public static Branch get(String branchId);
    public static boolean exists(String branchId);
    
    // 启动时调用
    public static void init();
}
```

### 3. LevelTransition.java

简化构造函数，移除 `int destBranch`：

```java
// 旧
public LevelTransition(Level level, int pos, Type type, int destDepth, int destBranch, Type destType)

// 新
public LevelTransition(Level level, int pos, Type type, int destDepth, String destBranchId, Type destType)
```

**字段变更**：
- 移除：`int destBranch`
- 保留：`String destBranchId`, `int destDepth`, `Type type`

### 4. InterlevelScene.java

```java
// 旧
public static int returnBranch;

// 新
public static String returnBranchId;
```

### 5. GamesInProgress.java

存档文件名格式变更：

```java
// 旧：depth{depth}.dat（分支 0）
//    depth{depth}_branch{branch}.dat（其他分支）

// 新：depth{depth}_{branchId}.dat
public static File depthFile(int slot, int depth, String branchId);
```

### 6. Bones.java

遗骨系统适配：

```java
private static String branchId = null;  // 旧：int branch = -1
```

存档兼容：读取旧 `branch` 字段并转换。

### 7. Chasm.java - 掉落逻辑优化

**新逻辑**：支线有下一层时正常掉落，无下一层时将 Chasm 转为普通地板。

```java
// Level.java 中的处理
protected void convertChasmOnBranchEnd() {
    if (Dungeon.branchId.equals(Branches.MAIN)) return;
    
    Branch branch = Branches.get(Dungeon.branchId);
    if (branch != null && branch.hasMoreDepth(Dungeon.depth + 1)) {
        return; // 有下一层，保留 Chasm
    }
    
    // 无下一层，Chasm → EMPTY
    for (int i = 0; i < length(); i++) {
        if (map[i] == Terrain.CHASM) {
            map[i] = Terrain.EMPTY;
        }
    }
}
```

## 使用方式

### 添加新分支

1. 在 `Branches.java` 中定义常量：

```java
public static final String NEW_BRANCH = "new_branch";
```

2. 创建分支配置：

```java
private static Branch createNewBranch() {
    Class<? extends Level>[] levels = new Class[3];
    levels[1] = NewLevel1.class;
    levels[2] = NewLevel2.class;
    return new Branch(NEW_BRANCH, 2, "branch_new", levels);
}
```

3. 在 `init()` 中注册：

```java
register(createNewBranch());
```

### 创建分支入口

在房间类中创建 LevelTransition：

```java
// 从主线进入分支
level.transitions.add(new LevelTransition(level,
    entrancePos,
    LevelTransition.Type.BRANCH_EXIT,
    Dungeon.depth,
    Branches.NEW_BRANCH,  // 目标分支 ID
    LevelTransition.Type.BRANCH_ENTRANCE));

// 从分支返回主线
level.transitions.add(new LevelTransition(level,
    exitPos,
    LevelTransition.Type.BRANCH_ENTRANCE,
    2,  // 返回主线第 2 层
    Branches.MAIN,
    LevelTransition.Type.BRANCH_EXIT));
```

### 检查当前分支

```java
// 判断是否主线
if (Dungeon.branchId.equals(Branches.MAIN)) { ... }

// 判断特定分支
if (Dungeon.branchId.equals(Branches.MINING)) { ... }

// 检查是否有下一层
Branch branch = Branches.get(Dungeon.branchId);
if (branch.hasMoreDepth(Dungeon.depth + 1)) { ... }
```

## 存档兼容性

### 旧存档迁移

`Dungeon.restoreFromBundle()` 自动处理：

```java
if (bundle.contains(BRANCH_ID)) {
    branchId = bundle.getString(BRANCH_ID);
} else if (bundle.contains("branch")) {
    int oldBranch = bundle.getInt("branch");
    // 旧映射：0→MAIN, 1→MINING, 2→MOSS
    branchId = oldBranch == 0 ? Branches.MAIN : 
               (oldBranch == 1 ? Branches.MINING : Branches.MOSS);
}
```

### 文件名变更

| 旧格式 | 新格式 |
|--------|--------|
| `depth5.dat` | `depth5_main.dat` |
| `depth2_branch1.dat` | `depth2_mining.dat` |

## 分支常量映射

| 旧值 | 新常量 | 说明 |
|------|--------|------|
| 0 | `Branches.MAIN` | 主线 |
| 1 | `Branches.MINING` | 采矿分支（11-14层） |
| 2 | `Branches.MOSS` | 苔藓分支（2层） |

## 设计原则

1. **分支独立性**：每个分支是独立地牢，有独立的层数和关卡类型
2. **字符串标识**：避免硬编码数字，提高可读性和扩展性
3. **向后兼容**：自动迁移旧存档的 `int branch` 字段
4. **统一导航**：所有楼梯使用 `destBranchId + destDepth` 导航

## 文件结构

```
core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/
├── Dungeon.java              # branchId 字段、关卡创建逻辑
├── Bones.java                # 遗骨分支适配
├── GamesInProgress.java      # 存档文件名格式
├── InterlevelScene.java      # returnBranchId 字段
├── levels/
│   ├── Level.java            # convertChasmOnBranchEnd()
│   ├── LevelTransition.java  # 简化的构造函数
│   ├── branches/
│   │   ├── Branch.java       # 分支配置类
│   │   └── Branches.java     # 分支注册表
│   └── rooms/
│       └── quest/
│           ├── BlacksmithRoom.java   # 采矿分支入口
│           └── MineEntrance.java     # 采矿分支出口
```

## Chasm 特殊处理

### 问题
旧系统在支线掉落会传送到入口，但玩家可能期望掉到下一层。

### 解决方案
在 `Level.build()` 末尾调用 `convertChasmOnBranchEnd()`：

- **有下一层**：保留 Chasm，玩家正常掉落
- **无下一层**：Chasm → EMPTY，玩家可安全行走

这样避免了 "掉落却无处可掉" 的尴尬情况。

## 总结

V2 分支系统通过字符串 ID 实现了：
- 更清晰的代码语义
- 更好的扩展性（添加新分支无需修改 switch-case）
- 统一的导航模型
- 自动存档兼容
- Chasm 智能处理