package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.watabou.noosa.Group;

/**
 * Warped 的水面"活体"（调色板循环、glints、foam）没有搬过来；
 * 这里只保留 WaterWalk/Wader 需要的容器：一个用来存放水线图片的组。
 */
public final class WaterLive {

	private static Group keeper;
	private static boolean made;

	private WaterLive(){}

	/** 上一场场景的组已经随场景销毁，这里重新建一个 */
	public static void sceneCreated(){
		made = false;
		keeper = null;
		WaterWalk.reset();
	}

	/** Warped 里由"活水面"每帧驱动；这里直接转发给 WaterWalk */
	public static void update( float dt ){
		WaterWalk.update( dt );
	}

	public static Group keeper(){
		if (!made){
			made = true;
			keeper = new Group();
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.light( keeper );
		}
		return keeper;
	}
}
