# 地牢开发注意事项-生物（Mob.java）

> 编写时间：2026-11-08
> 编写作者：JDSALing
> 适用范围：All Ai agent

### 1、生物开发原则（Mob.java）

所有生物必须遵守**Mob.java**这个父类的接口，
严禁因为实现一些效果直接破坏**Mob.java**来获得效果，**Mob.java**应该作为所有游戏中生物的父类[除了Hero.java]，您的所有开发都应该基于**Mob.java**进行，并基于现有接口进行实现，而不是写一个效果就新建一个轮子导致**代码混乱**。

#### A、近战敌对生物开发建议
可以参考**Rat.java**，该类对于基础近战已经可以完美复刻，开发者仅需要对数值，Sprite进行基本调整，
除非我向你特殊指出，否则请不要在基于**Rat.java**代码，生成臆想的多余代码。

#### B、远程敌对生物开发建议
远程怪物基本有zap()这个方法来实现远程攻击，
另外远程攻击不能只在Mob中写了一个doAttack()就完事，
因为远程攻击是一个完整的回调结果，
应该严格按照：
> 远程怪物Mob本体 + 远程怪物Sprite本体

其中，非常建议写远程怪物使用onZapComplete()来进行回调

这可以参考Warlock.java 或者 DM100.java，其中DM100还加了接口CallBack：
```java
public class DM100 extends Mob implements Callback {

  /* 
  * 中间代码已省略，
  * 这里只是供你参考Callback逻辑，
  * 请你生成代码时严禁省略 
  */

	@Override
	public void call() {
		next();
	}
	
}
```
DM100.java 这其实也是一种回调的方法
另外，建议您在写Mob时，可以看看：
##### com.watabou.utils下的：
> Callback.java
> Bundlable.java
> Bundle.java
> Random.java

其它的Utils类，在生物这里应用较少，所以不建议你使用其他接口，除非我向你特殊指出

```log
current actor:class 
com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype

trace:
java.lang.Object.wait(Native Method)
java.lang.Object.wait(Object.java:405)
java.lang.Object.wait(Object.java:543)
com.shatteredpixel.shatteredpixeldungeon.actors.Actor.process(Actor.java:314)
com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene$2.run(GameScene.java:826)
```

以上报错就是因为只有zap的攻击实现，无动画实现导致的线程卡在此生物上，因为该项目是一款回合制的核心Roguelike游戏，所以对于回合的分配至关重要，由于没有动画，游戏无法对下一回合进行判定，故此，游戏在玩家的面前表现为回合无限卡死的状态。

#### C、NPC
所有NPC应该以**无法移动，无法被击退**的标准进行，
可以参考**NPC.java**的实现方式，
一些比较特殊的NPC,可能会拒绝受到任何攻击，
这其实本质是:
```java
    @Override
    public boolean add(Buff buff ) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public int defenseSkill( Char enemy ) {
        return 1000;
    }

    @Override
    protected Char chooseEnemy() {
        return null;
    }

    @Override
    public void damage(int dmg, Object src) {
    }

    @Override
    public boolean reset() {
        return true;
    }
```
这些代码在让NPC不会受到任何攻击，可以作为一些特殊NPC，如果你需要让NPC进行互动：
则需要对**interact**方法进行处理
```java
    @Override
    public boolean interact(Char c) {
      //代码内容
      return true;
    }
```

##### α、NPC基本对话框
如果你需要让NPC进行互动且显示对话框，
你可以使用以下代码：
GLog 是游戏在左下角的浮动文本，一般用于提醒文本，关于**GLog.java**，里面包含了n,w,p(分别代表：负面提示-红，警告提示-橙，正面提示-绿)

如果你需要让NPC有弹窗对话框，例如**Wandmaker.java**委托玩家任务的弹窗，这是**WndQuest.java**的实现方式。
注意：严禁直接套用WndQuest.java，这是错误的做法，会导致游戏崩溃，例如下面的写法
```java
/* 错误写法 */
    @Override
    public boolean interact(Char c) {
        GameScene.show(new WndQuest(questgiver, text, 0));
      return true;
    }

```
该写法会导致崩溃，其报错为
```log
Text measured from the actor thread!
```
正确写法是在这里套上：
```java
Game.runOnRenderThread(new Callback() {
									   @Override
					 public void call() {
            //套在这里
							 GameScene.show(new WndQuest(questgiver, text, 0));
					 }
				}
			);
```
你发现了吗，这其实是为了保护游戏主线程，
实际上，这个报错是由Evan定义的自定义崩溃：
```java
if (Thread.currentThread().getName().equals("SHPD Actor Thread")) {
				throw new RuntimeException("Text measured from the actor thread!");
			}
```
但是，严禁你去修改底层这里来解决未套Game.runOnRenderThread的报错，这绝对是严令禁止的行为。

#### Boss
好吧，我们终于讨论到了**Boss**的开发，Boss这个东西，严格来说是一个行为逻辑，状态机巨多的一个高级怪物。
如果我们要写一个Boss，我建议你参考**Goo.java**,
然后，对于状态的保存，应该使用**Bundle.java**,
而且在这里应该直接以 storeInBundle / restoreFromBundle：
```java
  private final String PUMPEDUP = "pumpedup";
	private final String HEALINC = "healinc";

	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put( PUMPEDUP , pumpedUp );
		bundle.put( HEALINC, healInc );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {

		super.restoreFromBundle( bundle );

		pumpedUp = bundle.getInt( PUMPEDUP );
		if (state != SLEEPING) BossHealthBar.assignBoss(this);
		if ((HP*2 <= HT)) BossHealthBar.bleed(true);

		healInc = bundle.getInt(HEALINC);
	}
```
storeFromBundle 是 存储状态，是在游戏内进行的
restoreFromBundle 是 读取状态，是读取内进行的

严禁在restoreFromBundle直接进行Dungeon.level 或 Dungeon.hero等**Dungeon.java**的调用，因为在这里这些尚未实例化，所以返回为null，会崩溃游戏。

##### Boss的开发建议-面向AI
开发Boss是一个艰巨的工作，在你收到Boss的需求文档后，你应该进行以下查询：
- 检查该需求在当前整个项目有没有相同或者类似的实现
- 如果整个项目没有类似或相同的实现，实现方法优先写在自身Java文件中
- 如果该需求比较复杂，可以尝试与其他基本底层类进行修改，但严禁动：
**Mob.java**,**Char.java**,**Actor.java**
**com.watabou.XXX**的所有类
- 如果真的要动底层，必须警告你的AI主人，并建议先对修改底层备份一份或者创建Git还原点，方便如果修改错误，自动恢复

##### Boss常用高频接口

###### 直属接口
（也就是能直接在Boss本体类使用的）
- 传送技能 
- 源文件：ScrollOfTeleportation.java
- 实例：
ScrollOfTeleportation.appear(Char char,int pos)

---

- 闪电技能
- 源文件：DM100.java，DM100Sprite.java
- 实例：DM100.java 中的doAttack 与 DM100Sprite 中的zap方法结合

---

- 攻击上Buff技能
- 接口：attackProc
- 实例：FetidRat.java 中的 attackProc方法

---

- 落石技能
- 接口：dropRocks()
- 实例：DM3000 中的 dropRocks方法

###### 外部建议接口

**Level.java**

seal() 锁楼层的触发方法

unseal() 解锁楼层的触发方法

occupyCell() 实时监测楼层状态的方法


```text
上面的文本是给AI看的，
这里的文本是给人看的，AI直接忽略：
关于生物的指南暂时写到这，
后续如果有更多建议，会继续更新。
```