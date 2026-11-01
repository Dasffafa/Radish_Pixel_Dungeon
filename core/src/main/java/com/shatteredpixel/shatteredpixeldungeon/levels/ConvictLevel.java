/*
 * Radish Pixel Dungeon
 * 传奇重犯副本楼层（开阔竞技场，类似天狗二阶段）
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LegendaryFelon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.noosa.audio.Music;

import java.util.Arrays;

/**
 * 传奇重犯副本：单层竞技场，从主线第 8 层进入。
 * 地形按用户给定的墙线片段生成：上方大竞技场，下方窄走廊（入口在走廊底部正中），
 * 中间那扇上锁的门在击败传奇重犯后打开。
 */
public class ConvictLevel extends Level {

	//用户给定片段：13 宽 × 18 高，left/top 取 0
	private static final int WIDTH = 13, HEIGHT = 18;

	//入口在走廊底部正中
	private static final int ENTRANCE_X = 6, ENTRANCE_Y = 16;
	//上锁的门
	private static final int DOOR_X = 6, DOOR_Y = 12;

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
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
		Arrays.fill( map, Terrain.WALL );

		//先铺内部空地（外圈留一圈墙）
		Painter.fill( this, 1, 1, WIDTH - 2, HEIGHT - 2, Terrain.EMPTY );

		//墙（照抄用户给的片段，left=0 / top=0）
		Painter.fill( this, 3, 1, 7, 1, Terrain.WALL );
		Painter.fill( this, 2, 2, 2, 1, Terrain.WALL );
		Painter.fill( this, 9, 2, 2, 1, Terrain.WALL );
		Painter.fill( this, 1, 3, 2, 1, Terrain.WALL );
		Painter.fill( this, 10, 3, 2, 1, Terrain.WALL );
		Painter.fill( this, 1, 4, 1, 6, Terrain.WALL );
		Painter.fill( this, 11, 4, 1, 6, Terrain.WALL );
		Painter.fill( this, 2, 9, 1, 2, Terrain.WALL );
		Painter.fill( this, 10, 9, 1, 2, Terrain.WALL );
		Painter.fill( this, 3, 10, 1, 7, Terrain.WALL );
		Painter.fill( this, 9, 10, 1, 7, Terrain.WALL );
		Painter.fill( this, 4, 11, 5, 1, Terrain.WALL );
		Painter.fill( this, 1, 12, 2, 1, Terrain.WALL );
		Painter.fill( this, 4, 12, 2, 1, Terrain.WALL );
		Painter.fill( this, 7, 12, 2, 1, Terrain.WALL );
		Painter.fill( this, 10, 12, 2, 1, Terrain.WALL );
		Painter.fill( this, 4, 13, 1, 2, Terrain.WALL );
		Painter.fill( this, 8, 13, 1, 2, Terrain.WALL );

		//门正上方（片段里的 top+11 那堵墙）开一格通道：
		//按片段原样画的话门后面是实心墙，玩家永远进不去竞技场，这里只补这一格。
		Level.set( pointToCell( DOOR_X, DOOR_Y - 1 ), Terrain.EMPTY, this );

		//门：一开始是能推开的普通门（否则玩家进不去），
		//Boss 发现玩家后会上锁（见 sealBossDoor），击败后重新打开。
		Level.set( pointToCell( DOOR_X, DOOR_Y ), Terrain.DOOR, this );

		//入口
		int entrance = pointToCell( ENTRANCE_X, ENTRANCE_Y );
		Level.set( entrance, Terrain.ENTRANCE, this );
		transitions.add( LevelTransition.branchUp( this, entrance,
				"convict:main-8", Branches.MAIN, 8 ) );

		return true;
	}

	/** Boss 发现玩家后把门锁上（玩家进来后被困住，直到击败他） */
	public void sealBossDoor() {
		//玩家还在门下方的走廊里时不能锁，否则会被关在竞技场外、又没有钥匙，直接卡死
		if (Dungeon.hero != null && Dungeon.hero.pos / width() > DOOR_Y) {
			return;
		}
		int door = pointToCell( DOOR_X, DOOR_Y );
		if (map[door] == Terrain.DOOR) {
			Level.set( door, Terrain.LOCKED_DOOR, this );
			//Level.set 只改数据不改贴图，必须通知场景重画这一格，否则看着还是普通门
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap( door );
		}
	}

	/** 击败传奇重犯后把上锁的门打开 */
	public void openBossDoor() {
		int door = pointToCell( DOOR_X, DOOR_Y );
		if (map[door] == Terrain.LOCKED_DOOR) {
			Level.set( door, Terrain.DOOR, this );
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap( door );
		}
	}

	private int pointToCell( int x, int y ) {
		return x + y * width();
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
		LegendaryFelon felon = new LegendaryFelon();
		//出生在竞技场中心
		felon.pos = pointToCell( 6, 6 );
		mobs.add( felon );
	}

	@Override
	public Actor addRespawner() {
		return null;
	}

	@Override
	protected void createItems() {
		// 无
	}

	@Override
	public void playLevelMusic() {
		Music.INSTANCE.play( Assets.Music.PRISON_TENSE, true );
	}
}
