/*
 * Radish Pixel Dungeon
 *
 * 典狱长 Boss 层：一个较长的长方形牢房，与天狗（PrisonBossLevel）在第 10 层随机二选一。
 * 原型机数量、控制器位置等见类内常量。
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.DOOR;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_DOOR;
import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.WardenPrototype;
import com.shatteredpixel.shatteredpixeldungeon.effects.SnDBGM;
import com.shatteredpixel.shatteredpixeldungeon.items.SimpleController;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.ArrayList;

public class WardenBossLevel extends Level {

	{
		color1 = 0x3f3f46;
		color2 = 0x6a6a75;
	}

	//严格按用户给的地形片段：整层 13 宽 × 18 高，四周留一圈墙。
	//片段坐标（left=0, top=0）：墙 y=12 两段、竖墙 x=3/x=9（y=13..16）、入口 (6,16)、门 (6,12)
	private static final int WIDTH  = 13;
	private static final int HEIGHT = 18;

	//下方初始房间：只挖 x=4..8, y=13..16（left/top 是闭区间、right/bottom 是开区间），
	//外圈 x=3、x=9、y=12、y=17 保留为墙，门只开在 (6,12) 这一格
	private static final Rect entranceRoom = new Rect( 4, 13, 9, 17 );
	//房间顶墙那格：这里放门，开门直接进战斗区
	private static final Point doorCell = new Point( 6, 12 );
	//战斗区（上方大长方形）：x=1..11, y=1..11
	private static final Rect arena = new Rect( 1, 1, 12, 12 );

	//两种“图画”随机：A=旧石地（零散碎石），B=破损地砖（裂痕 + 墙饰）
	private boolean variantB = false;

	private static final String BOSS_DEAD = "boss_dead";

	private boolean bossDead = false;
	private int exitCell = -1;

	@Override
	public void playLevelMusic() {
		if (SnDBGM.playLevelMusic()) return;
		if (bossDead) {
			Music.INSTANCE.playTracks( PrisonLevel.PRISON_TRACK_LIST, PrisonLevel.PRISON_TRACK_CHANCES, false );
		} else {
			Music.INSTANCE.play( Assets.Music.PRISON_BOSS, true );
		}
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_PRISON;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_PRISON;
	}

	@Override
	protected boolean build() {

		setSize( WIDTH, HEIGHT );

		Painter.fill( this, 0, 0, WIDTH, HEIGHT, Terrain.WALL );

		//下方初始房间：玩家从上一层下来先落在这里，看不到也打不到 Boss
		//注意：Painter.fill(level, rect, m, value) 的 m 是“四边内缩”，不是地形值！
		//之前写成 fill(arena, 1, EMPTY) 实际只挖了内缩一圈的 9x9，导致门(6,12)正上方(6,11)还是墙，
		//门后面是实心的，玩家怎么都进不去战斗区。这里改用显式 xywh 版本。
		Painter.fill( this, 1, 1, WIDTH - 2, doorCell.y - 1, Terrain.EMPTY );        //战斗区 x1..11, y1..11
		Painter.fill( this, 4, doorCell.y + 1, 5, 4, Terrain.EMPTY );                //入口房 x4..8, y13..16

		//房间顶墙上的门：普通门，不用钥匙，开门直接进战斗区
		Level.set( pointToCell( doorCell ), Terrain.DOOR, this );

		//点亮一些装饰，让长条牢房不那么空（两种图画二选一）
		variantB = Random.Int(2) == 1;
		if (variantB) {
			//B：破损地砖 —— 战斗区里散布裂痕地面，墙脚加墙饰
			for (int y = arena.top + 1; y < arena.bottom - 1; y++) {
				for (int x = arena.left + 1; x < arena.right - 1; x++) {
					int cell = pointToCell( new Point( x, y ) );
					if (map[cell] == Terrain.EMPTY && Random.Int(5) == 0) {
						Level.set( cell, Terrain.EMPTY_SP, this );
					}
				}
			}
			for (int i = 0; i < 4; i++) {
				int x = Random.IntRange( arena.left + 1, arena.right - 2 );
				//战斗区内部从 arena.top 就开始了，墙在更上面一行（y=0）
				int y = Math.max( 0, arena.top - 1 );
				int cell = pointToCell( new Point( x, y ) );
				if (map[cell] == Terrain.WALL) {
					Level.set( cell, Terrain.WALL_DECO, this );
				}
			}
		} else {
			//A：旧石地 —— 零散碎石装饰
			for (int i = 0; i < 6; i++) {
				int x = Random.IntRange( arena.left + 1, arena.right - 2 );
				int y = Random.IntRange( arena.top + 1, arena.bottom - 2 );
				int cell = pointToCell( new Point( x, y ) );
				if (map[cell] == Terrain.EMPTY) {
					//注意：build() 期间 Dungeon.level 还没指向本关，2 参 set() 会拿到 null 抛 NPE，
					//一律显式传 this 走 3 参版本
					Level.set( cell, Terrain.EMPTY_DECO, this );
				}
			}
		}

		//入口放在初始房间底部正中（与用户给的地形片段一致）
		int entrance = pointToCell( new Point( doorCell.x, entranceRoom.bottom - 1 ) );
		Level.set( entrance, Terrain.ENTRANCE, this );
		transitions.add( new LevelTransition( this, entrance, LevelTransition.Type.REGULAR_ENTRANCE ) );

		//出口在战斗区最上端（打完典狱长后从这里下去），击败典狱长前锁住
		exitCell = pointToCell( new Point( WIDTH / 2, arena.top + 1 ) );
		Level.set( exitCell, Terrain.LOCKED_EXIT, this );
		transitions.add( new LevelTransition( this, exitCell, LevelTransition.Type.REGULAR_EXIT ) );

		return true;
	}

	//关卡构建中：此时只能登记到 mobs，精灵与 Actor 都交给 GameScene.create() 统一挂，
	//否则会和 GameScene.create() 的遍历重复挂精灵（旧精灵滞留在已清空的层里，表现为怪物整个消失）
	private boolean building = false;

	@Override
	protected void createMobs() {
		building = true;
		Warden warden = new Warden();
		//Boss 待在战斗区靠中间的位置，不会一进关就贴脸
		//站在门内侧不远处：玩家一开门就能看见 Boss（放在房间正中会被 8 格视野挡住）
		warden.pos = pointToCell( new Point( WIDTH / 2, arena.bottom - 2 ) );
		mobs.add( warden );
		//成阶段 Boss 也还没进 Actor，这里占位，避免原型机刷到 Boss 身上
		spawnTaken.add( warden.pos );

		for (int i = 0; i < 8; i++) {
			spawnPrototype( 100 );
		}
		for (int i = 0; i < 3; i++) {
			spawnPrototype( 175 );
		}
		building = false;
	}

	@Override
	protected void createItems() {

		//控制器与补给都放在上方初始房间里，玩家进战斗区之前就能拿到
		SimpleController controller = new SimpleController();
		controller.identify();
		drop( controller, pointToCell( new Point( entranceRoom.left + 1, entranceRoom.top + 2 ) ) );

		int healing = pointToCell( new Point( entranceRoom.right - 2, entranceRoom.top + 2 ) );
		drop( new PotionOfHealing(), healing );

		//bottom 是开区间，-1 会落在房间外圈的墙上，这里退到内圈
		int shock = pointToCell( new Point( WIDTH / 2, entranceRoom.bottom - 2 ) );
		drop( new StoneOfShock(), shock );
	}

	//本轮生成里已经占掉的格子：createMobs 阶段怪物还没进 Actor，Actor.findChar 查不到，
	//不自己记一份的话 11 台原型机会互相重叠在同一格上
	private final ArrayList<Integer> spawnTaken = new ArrayList<>();

	/** 生成一台原型机（100 或 175），只落在战斗区里 */
	public WardenPrototype spawnPrototype( int model ) {
		WardenPrototype p = new WardenPrototype( model );
		int cell = randomRespawnCell( null );
		if (cell == -1) {
			//极端情况：找不到空位就放到战斗区中心
			cell = pointToCell( new Point( WIDTH / 2, arena.top + (arena.height() / 2) ) );
		}
		p.pos = cell;
		spawnTaken.add( cell );
		if (building) {
			//关卡构建阶段：只登记，精灵由 GameScene.create() 统一挂
			mobs.add( p );
		} else if (Dungeon.level == this) {
			//Boss 战中途补生成：场景已就绪，直接挂上去
			GameScene.add( p );
		} else {
			mobs.add( p );
		}
		return p;
	}

	@Override
	public void seal() {
		super.seal();
		int door = sealDoorCell();
		//英雄可能正站在门格上追击：此时把脚下改成实心格会让人嵌在墙里，
		//而整层已 locked（LockedFloor）又封掉了所有 transition，等于永久卡关。
		//封锁本身由 Level.seal() 的 locked + LockedFloor 负责，门格只是视觉/寻路补充，可跳过。
		if ( Actor.findChar( door ) == null ) {
			Level.set( door, Terrain.LOCKED_DOOR, this );
			GameScene.updateMap( door );
		}
	}

	@Override
	public void unseal() {
		super.unseal();
		int door = sealDoorCell();
		if ( map[door] == Terrain.LOCKED_DOOR ) {
			Level.set( door, Terrain.DOOR, this );
			GameScene.updateMap( door );
		}
	}

	/** 战斗区与初始房间之间的那扇门（构建期与运行期都用它，避免魔法数 162 漂移）。 */
	private int sealDoorCell() {
		return doorCell.x + doorCell.y * width();
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		ArrayList<Integer> candidates = new ArrayList<>();

		//只在战斗区里随机：不能刷到下方初始房间，否则玩家一落地就撞上一排原型机
		//结束行是 arena.bottom-2：最下面那一行是隔开初始房间的墙+门，机体站上去会和门重叠
		for (int y = arena.top; y < arena.bottom - 1; y++) {
			for (int x = arena.left; x < arena.right; x++) {
				int i = y * WIDTH + x;
				if (!passable[i] || map[i] == Terrain.ENTRANCE || map[i] == Terrain.EXIT || map[i] == Terrain.LOCKED_EXIT){
					continue;
				}
				//门口一格内不生成：否则一开门就有一堆机体挤在门后，精灵互相压住
				if (Math.abs(x - doorCell.x) <= 1 && Math.abs(y - doorCell.y) <= 1){
					continue;
				}
				if (Actor.findChar( i ) != null || spawnTaken.contains( i )){
					continue;
				}
				//已生成的机体周围一格也不再生成，避免两台机体的精灵叠在一起
				boolean crowded = false;
				for (int t : spawnTaken){
					if (Math.abs(t % WIDTH - x) <= 1 && Math.abs(t / WIDTH - y) <= 1){
						crowded = true;
						break;
					}
				}
				if (crowded){
					continue;
				}
				if (Char.hasProp( ch, Char.Property.LARGE ) && !openSpace[i]){
					continue;
				}
				candidates.add( i );
			}
		}
		if (candidates.isEmpty()) {
			return -1;
		}
		return Random.element( candidates );
	}

	public void onBossDeath() {
		bossDead = true;
		for (Mob mob : new ArrayList<>(mobs)) {
			if (mob instanceof WardenPrototype) {
				mob.alignment = Char.Alignment.NEUTRAL;
				mob.destroy();
				if (mob.sprite != null) {
					mob.sprite.killAndErase();
				}
			}
		}
		unseal();
		if (exitCell != -1) {
			Level.set( exitCell, Terrain.EXIT, this );
			GameScene.updateMap( exitCell );
		}
		GLog.w( Messages.get( this, "boss_dead" ) );
	}

	public boolean heroInBossArena(int cell) {
		return cell >= 0 && cell < length() && cell / width() < doorCell.y;
	}

	public boolean bossDead() {
		return bossDead;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( BOSS_DEAD, bossDead );
		bundle.put( "exit_cell", exitCell );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		bossDead = bundle.getBoolean( BOSS_DEAD );
		//旧档/异常档里可能没有 exit_cell：Bundle.getInt 缺键会返回 0，直接用会把地图左上角挖成 EXIT。
		//只在键存在且落在合法范围时才采用，否则标记为 -1（onBossDeath 里有 -1 守卫）。
		if ( bundle.contains( "exit_cell" ) ) {
			int cell = bundle.getInt( "exit_cell" );
			exitCell = ( cell >= 0 && cell < length() ) ? cell : -1;
		} else {
			exitCell = -1;
		}
		//Boss 已死却没有出口记录（异常档）：按 build() 的规则补算，避免打完却无路可走
		if ( bossDead && exitCell == -1 ) {
			exitCell = pointToCell( new Point( WIDTH / 2, arena.top + 1 ) );
		}
	}
}
