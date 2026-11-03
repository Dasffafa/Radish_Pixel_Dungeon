# AI 缺陷复盘（2026-11-02 ~ 11-03 人工修复提交）

> 起因（用户 2026-11-04 要求）：11-02/11-03 期间一位人工把 AI 写出的问题**几乎全部**修了一遍。
> 本文从这些提交里逐条提炼「我错在哪一步 → 下次的硬规则」，避免同类问题再犯。
> 每条都带提交号，可直接 `git show <hash>` 复核。
> 结论统一收进了 `AGENTS.md` §15 / §6 与 `mob-dev-principles.md`；本文是证据台账。

## 一、致命级：回合/线程死锁

**1. 远程攻击只写了 `zap()`，没有回调收尾**
- 证据：`WardenPrototype` actor dump（`Actor.process(Actor.java:314)`），同族已修的 `GoblinRider`（`7e5200787`）。
- 我错在：把「播动画」当成「这一回合已经打完了」，`act()`/`doAttack()` 里 `return false` 之后没人调 `next()`。
- 硬规则：每个 `return false` 必须写出「谁在哪个回调里调 `next()`」；`CharSprite.onComplete` 只认 `attack`/`operate`，
  自定义 `zap` 必须在 Sprite 侧覆写或在 `zap()` 里用 `MagicMissile.boltFromChar(...)` 的 callback。
- 详见 `mob-dev-principles.md` §3。

## 二、崩溃级

**2. 静态 UI 字段未初始化就使用** —— `2e92798d8 fix(ui): 修复游戏启动时标记清理闪退`
- `InventoryPane.crossB/crossM`、`QuickSlotButton.btn.crossB/crossM` 在首次进游戏时为 null。
- 硬规则：静态 / 延迟创建的可视组件，在任何清理路径（`cancelTargeting()` 一类）里**一律先判空**。
- 旁证：同提交补了公告条目 → 说明「玩家可见的修复必须同时补公告」。

**3. 关卡建造期拿 `Dungeon.level` 当目标** —— `33991107c`（典狱长关卡 NPE）
- `Level.set(cell, terrain)` 内部用 `Dungeon.level`；`build()` 阶段它还是 null ⇒ `Painter.set(null,...)` NPE。
- 硬规则：`Level` 的建造/装饰期一律用 `Level.set(cell, terrain, this)` 这种**显式目标**重载。

**4. 存档恢复后没有重建运行期对象** —— `d26f10ee1 fix(prison): 修复第十层返回过渡缺失`
- `LevelTransition` 不入档 ⇒ 从 `State.START` 恢复的地图永远没有返回出口。修法：`restoreFromBundle` 后
  `ensureStartExitTransition()`（幂等，先查已存在再补）。
- 硬规则：凡「运行期才创建、不进存档」的可视/交互对象（过渡、标记、发射器），读档后都要幂等重建。

## 三、存档兼容

**5. 把对象存成「类名串」而不是 Bundle** —— `77abc2950 fix(save): 药水包与投掷链改为 Bundle 集合存档，旧档仍可读`
- 旧：`"类名|类名"`（等级/鉴定/附魔全丢）。新：`bundle.put(CONTENTS_V2, ArrayList<Bundlable>)`，
  旧键 `contents` 只读不写，作为兼容分支。
- 硬规则：新存档字段一律存**对象集合**；旧键保留只读迁移分支；读不到新键时回退旧键。

## 四、UI / 输入

**6. 窗口宽度写死** —— `1e93d359e fix(npc): 修复竖屏对话框横向裁切`
- `WndDialogue.WIDTH_MAX = 220` 定死，竖屏可视宽只有 ~150-180 逻辑像素。
- 修法：`width = min(WIDTH_MAX, (int)(PixelScene.uiCamera.width * 0.92f) - 8)`，按钮加 `multiline = true`。
- 硬规则：任何 `Window` 的宽高都要用 `PixelScene.uiCamera.width/height`（或 `Camera.main`）夹取，
  且**横竖屏都要看**；`WndOptions`/`WndMessage` 的 `landscape() ? W : P` 是本仓库既定写法。

**7. 独立 camera 的窗口不避让底部栏位** —— `6a71037a4 fix(ui): 避免背包窗口遮挡底部栏位`
- 修法：`GameScene.show()` 里对 `WndBag` 计算 `windowBottom = camera.y/zoom + screenHeight/zoom`，
  超过 `scene.inventory.top()` 就整体上移。
- 硬规则：带独立 camera 的窗口（背包、选择器）要按「底部 UI 顶沿」做一次碰撞避让。

**8. 像素阈值没按缩放换算** —— `bbad58e89 fix(ui): 恢复滚动条自动显示，拖动阈值按缩放换算`
- 拖动判定原来写死 16 屏幕像素 ⇒ 高 DPI / 放大时手抖被当成点击、又或在低缩放时点不动。
- 修法：`float dragLimit = 16f * max(1f, Camera.main.zoom)`。
- 硬规则：所有「像素阈值 / 最小拖动距离」都乘 `Camera.main.zoom`；自制 `ScrollPane` 的滚动条要
  **滚动时才显示**（`thumbTimer`）并把 y 夹在视口内。

**9. 同一层输入判定被裁决两次** —— `949543e62 fix(input): 分离地图触碰与界面阻断`
- `CellSelector.select()` 里再查 `GameScene.interfaceBlockingHero()`，与 `PointerArea` 的 `blockLevel` + 窗口 blocker 重复。
- 硬规则：输入路由只在一个地方判定；`CellSelector` 只判「英雄是否 ready + 有 listener」。

## 五、机制正确性

**10. Boss 战开始条件挂在隐性状态上** —— `392b5cdb9 fix(warden): 受击时启动Boss战`
- 原判据 `!BossHealthBar.isAssigned() && 英雄在场地 ⇒ notice()` 不可靠（血条可能已被别的东西占用）。
- 新判据：`info != null && info.getDamage() > 0 && isAlive() && !bossDead()` ⇒ `awake = true; notice(); seal();`
- 硬规则：触发型战斗用**可观察事件**（受伤/进入某格）而不是「UI 已分配」这类间接状态。

**11. 自定义 Boss AI 绕过控制效果** —— `674d6893f fix(warden): 修复原型机战斗行为`
- 修法：`act()` 开头 `if (paralysed > 0 || buff(Amok/Terror/Dread) != null) return super.act();`
- 同提交还给 `RingOfDestruction` 加了「跳过未激活原型机」。
- 硬规则：覆写 `Mob.act()` 先放行控制状态；新增特殊 `Mob` 后 grep 一遍按 `instanceof`/`alignment` 遍历全场的代码。

**12. 消耗型交互的「取消」路径不结算** —— `ee63a9f76 fix(cards): 修复鉴定卡取消确认后重复使用`
- 修法：`WndTextNumberInput` 一类窗口覆写 `onBackPressed()` → `hide()` + `finish(..., false, cost)`。
- 硬规则：任何「先弹窗确认再消耗」的交互，**确认与取消两条路都必须结算**（取消＝退还或按文档扣费，不能什么都不做）。

**13. 效果实现选错钩子 / 半截实现**
- `e5f558a38`：`ShiningGloves` 的「对满血敌人增伤」必须落在 `Hero.attackProc` 里，
  `WebBelt` 撒网必须 `GameScene.add(Blob.seed(...))`（只 `seed` 不 `add` 不生效）。
- `ce9848449`：给「受击型」护甲加 `info.getAttacker() == Dungeon.hero` 限定。
- `6c300b1ab`（次日）又把该限定去掉 —— 因为 NPC/召唤物的闪电伤害也该吃这个加成。
- 硬规则：写「对谁生效」之前先把作用域确认清楚（读设计文档 / 问用户），
  **不要靠猜来回横跳**；加限定词与删限定词都要有依据。

**14. 状态判断用「数量」代替「性质」** —— `5fceedf1b fix(armor): 修复纯净者状态判断`
- 原 `buffs().isEmpty()` 把 NEUTRAL 类 buff 也算成「有状态」。
- 修法：`buffs().stream().noneMatch(b -> b.type != Buff.buffType.NEUTRAL)`。
- 硬规则：判「纯洁/无增益」看 buff 的 `type`，别用集合大小。

**15. 多槽 UI 默认可见导致空槽显示** —— `7b2a05758 fix(boss): 隐藏未分配的血条槽位`
- 修法：初始化时 `setComponentVisible(i, bosses[i] != null)`，分配时再置 true。
- 硬规则：槽位型 UI 在构建时按数据置可见性，不能只靠「后面会赋值」。

## 六、美术与帧网格

**16. 帧索引按想象写、没有对图算网格** —— `e5991f65d fix(sprite): 典狱长贴图裁成 120x45 整数网格，帧索引同步改为每行 10 帧`
- `warden.png` 实为 120×45（10 列 × 3 行，帧 12×15）；我按 16 列的假设写了 32..41 索引 ⇒ 整行错位。
- 硬规则：写 `TextureFilm` 帧索引前**先读 PNG 宽高**算 `cols = w/frameW, rows = h/frameH`，
  并把网格写进注释；不能被整除就先裁图。

## 七、工程与规范

**17. 英文文本侧漏改** —— `347babc13 fix(i18n): 补齐本分支新增的 12 条英文文本`
- 硬规则：新增文本键必须 zh + en **双侧**；改完逐键 `grep -c`（两侧各 1 命中）。

**18. 注释风格** —— `1cfcb9525 refactor(style): 按审查报告清理注释标签/英文注释/死常量`
- 实测要求：注释**用中文**（英文 Javadoc 逐条改中文）；类头**不要复述需求清单/数值表**（改成「数值见类内常量」，
  避免注释与代码漂移）；不用 `<p>` 之类 HTML 标签；概率注释要写「权重」而不是百分比（权重≠概率）。
- 硬规则：注释只写「为什么」和「不显然的约束」，不写「需求文档的复印件」。

**19. 调试痕迹与一次性文件进仓库** —— `8aae0344a`（清调试标记）、`6b4dc86fc`（清调试打印/构建产物）、
`3ed42f9b6`（二进制表格、replay 日志、`core/bin`）、`42c394139`（一次性补丁脚本、本机路径）
- 硬规则：`//[TEMP-DEBUG]` 提交前 `grep` 归零；不提交 `*.py` 补丁脚本、日志、构建产物、本机绝对路径。

**20. 误加的效果要整块回退** —— `9f5571d83 revert(armor): 撤销误加的护甲与雷杖效果`
- 硬规则：实现前先确认「这条需求到底要哪个效果」；不确定就一次只加一个效果并写清楚出处，便于整块 revert。

## 八、给我的通用作业流程（从上面反推）

1. 动手前：`grep` 现有实现 + `_upstream/spd` 同类实现；**先读图/数据，再写索引与尺寸**（§16）。
2. 写代码时：所有异步钩子写出「谁收尾」；所有窗口写「如何夹取」；所有新字段写「旧档怎么读」。
3. 自检：`git diff --numstat`（行尾/整文件重写）、`grep TEMP-DEBUG`、zh/en 双侧键名、构建产物不提交。
4. 提交：一功能一提交，提交信息写清「为什么」，让后来的人（和人工修复者）能快速定位。
