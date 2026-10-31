/*
 * Radish Pixel Dungeon
 *
 * 典狱长 Boss 层：一个较长的长方形牢房。
 * 与天狗（PrisonBossLevel）在第 10 层随机二选一出现。
 *
 * 场上固定 8 台 DM-100 原型机 + 3 台 DM-175 原型机（全部未激活），
 * 玩家在房间入口附近能捡到一个小型控制器；典狱长每失去 30 生命会再补 1 台 DM-100。
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
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

	//竖放的长条牢房：入口小房间在最下端，开门直接进入上方的战斗区
	private static final int WIDTH  = 12;
	private static final int HEIGHT = 22;

	//入口初始房间（最下方），照抄天狗（PrisonBossLevel）第一个房间的样子：
	//一个小房间，外圈墙、内圈空，顶墙上开一扇门，直接通进战斗区（没有单独的长走廊）。
	//Rect 的 right/bottom 是开区间。
	private static final Rect entranceRoom = new Rect( 3, 13, 9, 19 );
	//房间顶墙那格：这里放门，普通门（不用钥匙），开门就进战斗区
	private static final Point doorCell = new Point( 6, 13 );
	//战斗区（上方大长方形），底墙与初始房间顶墙隔一格，门正好开在这层墙上
	private static final Rect arena = new Rect( 1, 1, 11, 14 );

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
		Painter.fill( this, entranceRoom, 1, Terrain.EMPTY );
		//战斗区
		Painter.fill( this, arena, 1, Terrain.EMPTY );

		//房间顶墙上的门：普通门，不用钥匙，开门直接进战斗区
		Level.set( pointToCell( doorCell ), Terrain.DOOR, this );

		//点亮一些装饰，让长条牢房不那么空
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

		//入口放在初始房间的左下角（不要放在房间正中的通道口）：玩家从上一层下来就站在这里，
		//放在正中时只要往回走一步就会又踩到入口、被送回上一层
		int entrance = pointToCell( new Point( entranceRoom.left + 1, entranceRoom.bottom - 2 ) );
		Level.set( entrance, Terrain.ENTRANCE, this );
		transitions.add( new LevelTransition( this, entrance, LevelTransition.Type.REGULAR_ENTRANCE ) );

		//出口在战斗区最上端（打完典狱长后从这里下去），击败典狱长前锁住
		exitCell = pointToCell( new Point( WIDTH / 2, arena.top + 1 ) );
		Level.set( exitCell, Terrain.LOCKED_EXIT, this );
		transitions.add( new LevelTransition( this, exitCell, LevelTransition.Type.REGULAR_EXIT ) );

		return true;
	}

	@Override
	protected void createMobs() {

		Warden warden = new Warden();
		//Boss 待在战斗区靠中间的位置，不会一进关就贴脸
		//站在门内侧不远处：玩家一开门就能看见 Boss（放在房间正中会被 8 格视野挡住）
		warden.pos = pointToCell( new Point( WIDTH / 2, arena.bottom - 2 ) );
		mobs.add( warden );

		for (int i = 0; i < 8; i++) {
			spawnPrototype( 100 );
		}
		for (int i = 0; i < 3; i++) {
			spawnPrototype( 175 );
		}
	}

	@Override
	protected void createItems() {

		//控制器与补给都放在上方初始房间里，玩家进战斗区之前就能拿到
		SimpleController controller = new SimpleController();
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
		if (Dungeon.level == this) {
			//Boss 战中途补生成：场景已就绪，直接挂上去
			GameScene.add( p );
		} else {
			//关卡刚创建：由 GameScene 初始化时统一挂精灵，这里只登记
			mobs.add( p );
		}
		return p;
	}

	@Override
	public int randomRespawnCell( Char ch ) {
		ArrayList<Integer> candidates = new ArrayList<>();
		//只在战斗区里随机：不能刷到上方初始房间，否则玩家一落地就撞上一排原型机
		for (int y = arena.top; y < arena.bottom; y++) {
			for (int x = arena.left; x < arena.right; x++) {
				int i = y * WIDTH + x;
				if (passable[i] && map[i] != Terrain.ENTRANCE && map[i] != Terrain.EXIT && map[i] != Terrain.LOCKED_EXIT
						&& Actor.findChar( i ) == null
						&& !spawnTaken.contains( i )
						&& (!Char.hasProp( ch, Char.Property.LARGE ) || openSpace[i])) {
					candidates.add( i );
				}
			}
		}
		if (candidates.isEmpty()) {
			return -1;
		}
		return Random.element( candidates );
	}

	public void onBossDeath() {
		bossDead = true;
		unseal();
		if (exitCell != -1) {
			Level.set( exitCell, Terrain.EXIT, this );
			GameScene.updateMap( exitCell );
		}
		GLog.w( Messages.get( this, "boss_dead" ) );
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
		exitCell = bundle.getInt( "exit_cell" );
	}
}
