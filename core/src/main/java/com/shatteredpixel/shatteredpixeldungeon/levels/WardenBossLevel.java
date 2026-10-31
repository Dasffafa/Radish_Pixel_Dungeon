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

	//竖放的长条牢房：入口小房间在最下端，推开门向上进入上方的战斗区
	private static final int WIDTH  = 12;
	private static final int HEIGHT = 26;

	//入口初始房间（最下方），Rect 的 right/bottom 是开区间
	private static final Rect entranceRoom = new Rect( 3, 21, 9, 25 );
	//连接初始房间与战斗区的走廊（2 格宽，向上）
	private static final Rect corridor = new Rect( 5, 18, 7, 21 );
	//走廊顶端通往战斗区的门（向上开）
	private static final Point doorCell = new Point( 6, 18 );
	//战斗区（上方大长方形）
	private static final Rect arena = new Rect( 1, 1, 11, 18 );

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
		//走廊：从初始房间向上通到战斗区
		Painter.fill( this, corridor, 1, Terrain.EMPTY );
		//战斗区
		Painter.fill( this, arena, 1, Terrain.EMPTY );

		//走廊顶端通往战斗区的门（玩家开门向上进入 Boss 战）
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

		//入口在上方初始房间里
		int entrance = pointToCell( new Point( WIDTH / 2, entranceRoom.top + 1 ) );
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
		warden.pos = pointToCell( new Point( WIDTH / 2, arena.top + (arena.height() / 2) ) );
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
		drop( controller, pointToCell( new Point( entranceRoom.left + 1, entranceRoom.top + 1 ) ) );

		int healing = pointToCell( new Point( entranceRoom.right - 2, entranceRoom.top + 1 ) );
		drop( new PotionOfHealing(), healing );

		int shock = pointToCell( new Point( WIDTH / 2, entranceRoom.bottom - 1 ) );
		drop( new StoneOfShock(), shock );
	}

	/** 生成一台原型机（100 或 175），只落在战斗区里 */
	public WardenPrototype spawnPrototype( int model ) {
		WardenPrototype p = new WardenPrototype( model );
		int cell = randomRespawnCell( null );
		if (cell == -1) {
			//极端情况：找不到空位就放到战斗区中心
			cell = pointToCell( new Point( WIDTH / 2, arena.top + (arena.height() / 2) ) );
		}
		p.pos = cell;
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
