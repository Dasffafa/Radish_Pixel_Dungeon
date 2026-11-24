package com.shatteredpixel.shatteredpixeldungeon.effects.fx;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.water.WaterTerrain;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.water.WaterWalk;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/**
 * 特效模块的最小接线：本仓库只搬了 Warped 的"水"这一块，
 * 其它模块（火/雷/附魔/技能等）没有搬，这里不注册。
 */
public final class FxModules {

	private FxModules(){}

	/** 只注册"水"一个模块 */
	public static final int COUNT = 1;
	public static final int WATER_LIFE = 0;
	public static final int KIT = COUNT;

	/** The game's scene stands, every layer made. */
	public static void sceneCreated( GameScene scene ){
		WaterFX.sceneCreated();
	}

	public static void stepped( CharSprite s, int from, int to ){
		Steps.record( from, to, s.visible );
		if (s.ch == null) return;
		WaterWalk.stepped( s, from, to );
	}

	public static void jumped( CharSprite s ){
		if (s.ch == null) return;
		WaterWalk.jumped( s );
	}

	public static void beforeDraw( CharSprite s ){
	}

	public static void afterDraw( CharSprite s ){
		if (s.ch == null) return;
		WaterWalk.afterDraw( s );
	}

	public static void gone( CharSprite s ){
		WaterWalk.gone( s );
	}

	public static void terrainChanged( int cell, int oldTerrain, int newTerrain ){
		WaterTerrain.changed( cell, oldTerrain, newTerrain );
		com.shatteredpixel.shatteredpixeldungeon.effects.fx.nature.GrowthPulse.changed( cell, oldTerrain, newTerrain );
	}
}
