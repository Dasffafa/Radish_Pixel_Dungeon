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

	//参照天狗二阶段的战斗区（约 15x15），取一个略扁的长方形
	private static final int WIDTH  = 24;
	private static final int HEIGHT = 12;

	private static final Rect arena = new Rect( 1, 1, 22, 10 );

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
		Painter.fill( this, arena, 1, Terrain.EMPTY );

		//点亮一些装饰，让长条牢房不那么空
		for (int i = 0; i < 6; i++) {
			int x = Random.IntRange( 3, WIDTH - 4 );
			int y = Random.IntRange( 2, HEIGHT - 3 );
			int cell = pointToCell( new Point( x, y ) );
			if (map[cell] == Terrain.EMPTY) {
				set( cell, Terrain.EMPTY_DECO );
			}
		}

		//入口在左端
		int entrance = pointToCell( new Point( 3, HEIGHT / 2 ) );
		set( entrance, Terrain.ENTRANCE );
		transitions.add( new LevelTransition( this, entrance, LevelTransition.Type.REGULAR_ENTRANCE ) );

		//出口在右端，击败典狱长前锁住
		exitCell = pointToCell( new Point( WIDTH - 3, HEIGHT / 2 ) );
		set( exitCell, Terrain.LOCKED_EXIT );
		transitions.add( new LevelTransition( this, exitCell, LevelTransition.Type.REGULAR_EXIT ) );

		return true;
	}

	@Override
	protected void createMobs() {

		Warden warden = new Warden();
		warden.pos = pointToCell( new Point( WIDTH / 2, HEIGHT / 2 ) );
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

		SimpleController controller = new SimpleController();
		drop( controller, pointToCell( new Point( 5, HEIGHT / 2 ) ) );

		int healing = pointToCell( new Point( 6, HEIGHT / 2 - 1 ) );
		drop( new PotionOfHealing(), healing );

		int shock = pointToCell( new Point( 6, HEIGHT / 2 + 1 ) );
		drop( new StoneOfShock(), shock );
	}

	/** 生成一台原型机（100 或 175） */
	public WardenPrototype spawnPrototype( int model ) {
		WardenPrototype p = new WardenPrototype( model );
		int cell = randomRespawnCell( null );
		if (cell == -1) {
			//极端情况：找不到空位就放到场地中心附近
			cell = pointToCell( new Point( WIDTH / 2, HEIGHT / 2 ) );
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
		for (int i = 0; i < length(); i++) {
			if (passable[i] && map[i] != Terrain.ENTRANCE && map[i] != Terrain.EXIT && map[i] != Terrain.LOCKED_EXIT
					&& Actor.findChar( i ) == null
					&& (!Char.hasProp( ch, Char.Property.LARGE ) || openSpace[i])) {
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
		unseal();
		if (exitCell != -1) {
			set( exitCell, Terrain.EXIT );
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
