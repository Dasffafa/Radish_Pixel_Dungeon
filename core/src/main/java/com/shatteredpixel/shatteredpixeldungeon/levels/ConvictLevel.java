/*
 * Radish Pixel Dungeon
 * 传奇重犯副本楼层（开阔竞技场，类似天狗二阶段）
 */

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.LegendaryFelon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

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
	//传奇重犯的出生点：竞技场正中
	private static final int FELON_X = 6, FELON_Y = 6;

	private static final String FELON_SPAWNED = "felon_spawned";
	private boolean felonSpawned = false;

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
		//不在建关时放 Boss：站在门口就能看见他是不对的，要等英雄踏进竞技场才生成（见 occupyCell）
	}

	@Override
	public void occupyCell( Char ch ) {
		super.occupyCell( ch );
		if (ch == Dungeon.hero && !felonSpawned && inArena( ch.pos )) {
			spawnFelon();
		}
	}

	/** 门上方算竞技场里面，门格本身不算。 */
	private boolean inArena( int cell ) {
		return cell / width() < DOOR_Y;
	}

	private void spawnFelon() {
		for (Mob m : mobs) {
			//更早的存档里他在建关时就放好了，不再多生成一个
			if (m instanceof LegendaryFelon) {
				felonSpawned = true;
				return;
			}
		}

		int cell = felonSpawnCell();
		if (cell == -1) {
			//竞技场里一个能站的格子都没有：这次先不生成，英雄下一次挪动时再试
			return;
		}

		//出生格上的绵羊直接顶掉，和原版矮人王召唤随从时一样，不能因为一只羊就不出来
		Char sheep = Actor.findChar( cell );
		if (sheep != null) {
			sheep.die( null );
		}

		felonSpawned = true;
		LegendaryFelon felon = new LegendaryFelon();
		felon.pos = cell;
		GameScene.add( felon );
	}

	/**
	 * 传奇重犯该站的格子：离出生点最近、上面没有别的角色的那一格（绵羊不算，会被顶掉）。
	 * 出生点上只有一只羊时还是出生点；被别的角色占着时（比如英雄直接传送到了正中间）让到旁边。
	 * 只在竞技场里、从出生点走得到的格子里找，找不到返回 -1。
	 */
	private int felonSpawnCell() {
		PathFinder.buildDistanceMap( pointToCell( FELON_X, FELON_Y ), passable );
		int best = -1;
		for (int cell = 0; cell < length(); cell++) {
			if (!inArena( cell ) || PathFinder.distance[cell] == Integer.MAX_VALUE) {
				continue;
			}
			Char ch = Actor.findChar( cell );
			if (ch != null && !(ch instanceof Sheep)) {
				continue;
			}
			if (best == -1 || spawnCost( cell ) < spawnCost( best )) {
				best = cell;
			}
		}
		return best;
	}

	//离出生点越近越好；一样近时，空格优先于要顶掉一只绵羊的格子
	private int spawnCost( int cell ) {
		return 2 * PathFinder.distance[cell] + (Actor.findChar( cell ) == null ? 0 : 1);
	}

	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle( bundle );
		bundle.put( FELON_SPAWNED, felonSpawned );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle( bundle );
		felonSpawned = bundle.getBoolean( FELON_SPAWNED );
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
