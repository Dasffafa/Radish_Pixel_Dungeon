/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxLight;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning.Afterglow;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning.Crackle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning.ElectricityFx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning.StrikeGround;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.lightning.StrikeLights;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.Arrays;
import java.util.List;

/**
 * Every lightning of the game: one or more arcs (Arc) that strike and fade in DURATION s, its
 * callback called on the frame it ends.
 *
 * 本仓库没有 Warped 的天气层（WeatherOverlay），所以没有闪屏/雷声/落雨；搬过来的是它"世界怎么回答
 * 一道光"的那一半：
 * - 落点上铺一片地面光池（StrikeLights + FxLight），每帧按光晕亮度驱动
 * - 打在干地上留一小片焦痕，打在水面上水面发亮（一圈圈 wash）、荡开水纹、鱼影散开（StrikeGround）
 * - 落点上一簇还在爬的电痕（Crackle）
 * - 大闪电（major()）消失后，上一帧的光芯在眼前多留 0.12 s（Afterglow）
 *
 * major() 由法杖/炸弹/宝石这类"整道打出去"的闪电标记；小怪的一次性小电击是 minor。
 */
public class Lightning extends Group {

	private static final float DURATION = 0.3f;

	//its brightness through its life, as (time, brightness) keys joined by straight runs
	static final float[] FLICKER = { 0f, 1f, 0.04f, 0.85f, 0.08f, 0.25f, 0.12f, 0.8f, 0.14f, 1f,
			0.18f, 0.65f, 0.2f, 0.4f, 0.22f, 0.5f, DURATION, 0f };
	/** How many majors at once get their extras. */
	static final int RICH_MAJORS = 3;
	/** Its light on the ground: a pool GROUND px under where it strikes, POOL_* px wide. */
	static final int POOL_COLOR = 0xA8C8FF, POOL_MAJOR = 31, POOL_MINOR = 15;
	static final float POOL_LEVEL = 0.5f;
	/** How bright its crackle where it lands. */
	static final float CRACKLE_LEVEL = 0.9f;
	/** An arc this long strikes: it lights the ground and crackles. */
	static final float GLOW_FROM = 30f;

	private static int richAlive;

	//验收用计数（selftest 的 Z_Lightning 场景会看）
	public static int poolsLaid, cracklesLit, afterglowsShown, scorchesLaid;

	private float life;

	private List<Arc> arcs;

	private Callback callback;

	private boolean begun, struck, major, rich, onWorld;
	private float strikeT;
	//its pools of light, what share of its halo each shows, and where each lay when it took it
	private FxLight[] pools;
	private float[] poolShare, poolX, poolY;
	private int poolCount;

	public Lightning(int from, int to, Callback callback){
		this(Arrays.asList(new Arc(from, to)), callback);
	}

	public Lightning(PointF from, int to, Callback callback){
		this(Arrays.asList(new Arc(from, to)), callback);
	}

	public Lightning(int from, PointF to, Callback callback){
		this(Arrays.asList(new Arc(from, to)), callback);
	}

	public Lightning(PointF from, PointF to, Callback callback){
		this(Arrays.asList(new Arc(from, to)), callback);
	}

	public Lightning( List<Arc> arcs, Callback callback ) {

		super();

		this.arcs = arcs;
		for (Arc arc : this.arcs)
			add(arc);

		this.callback = callback;

		life = DURATION;
	}

	/** 一道"整打出去"的闪电：法杖、炸弹、宝石。它才配地面焦痕与余光。 */
	public Lightning major(){
		major = true;
		return this;
	}

	public boolean isMajor(){
		return major;
	}

	/** A new scene: no rich majors alive. */
	public static synchronized void sceneCreated(){
		richAlive = 0;
		StrikeLights.reset();
		Crackle.reset();
		Afterglow.reset();
		ElectricityFx.sceneCreated();
	}

	private static synchronized boolean claimRich(){
		if (richAlive >= RICH_MAJORS) return false;
		richAlive++;
		return true;
	}

	private static synchronized void releaseRich(){
		if (richAlive > 0) richAlive--;
	}

	/** Its brightness `t` seconds into its life (FLICKER), none past its end. */
	static float flicker( float t ){
		if (t <= 0f) return FLICKER[1];
		for (int i = 2; i < FLICKER.length; i += 2){
			if (t <= FLICKER[i]){
				float t0 = FLICKER[i - 2], b0 = FLICKER[i - 1];
				return b0 + (FLICKER[i + 1] - b0) * (t - t0) / (FLICKER[i] - t0);
			}
		}
		return 0f;
	}

	/** Its halos' brightness `t` seconds in: steadier than its stroke. */
	static float halo( float t ){
		return 0.5f * flicker( t ) + 0.5f * Math.max( 0f, 1f - t / DURATION );
	}

	@Override
	public void update() {

		if (!begun){
			begun = true;
			begin();
		}

		life -= Game.elapsed;
		float t = DURATION - life;

		if (!struck && life >= 0){
			struck = true;
			strike();
		}

		if (life < 0) {

			if (rich) afterglow();
			release();
			if (callback != null) {
				callback.call();
			}

		} else if (struck) {

			float alpha = life / DURATION;
			for (Arc arc : arcs) {
				arc.alpha(alpha);
			}

			//its pools as bright as it is now
			float h = halo( t - strikeT );
			for (int i = 0; i < poolCount; i++){
				if (pools[i] == null) continue;
				if (drives( i )) pools[i].level( poolShare[i] * h );
				else pools[i] = null;
			}

			super.update();

		} else {

			super.update();
		}
	}

	//its first frame: whether it is drawn on the world's camera (the ground answers it), and
	//whether it is one of the first RICH_MAJORS
	private void begin(){
		Camera cam = camera();
		boolean world = (cam == null || cam == Camera.main) && StrikeLights.canLight();
		onWorld = world;
		boolean strikes = false;
		for (Arc arc : arcs) strikes |= arc.strikes();
		if (major && world && strikes && claimRich()) rich = true;
	}

	//the strike: the world's answer where its arcs land
	private void strike(){
		if (!onWorld) return;
		pools = new FxLight[arcs.size()];
		poolShare = new float[pools.length];
		poolX = new float[pools.length];
		poolY = new float[pools.length];
		float h = halo( DURATION - life );
		int mains = 0;
		for (Arc arc : arcs){
			if (!arc.strikes()) continue;
			float x = arc.to().x;
			float gy = StrikeGround.groundY( x, arc.to().y );
			if (mains < pools.length){
				FxLight p;
				float share = POOL_LEVEL;
				if (major){
					p = StrikeLights.pool( this, x, gy, POOL_MAJOR, POOL_COLOR, share * h, DURATION, 0.06f );
				} else {
					p = StrikeLights.minor( this, x, gy, POOL_MINOR, POOL_COLOR, share * h, DURATION, 0.06f );
					if (StrikeLights.relit( this )) share *= StrikeLights.AGAIN;
				}
				if (p != null){
					keep( p, share );
					mains++;
					poolsLaid++;
				}
			}
			if (Fx.seen( x, gy )){
				if (Crackle.at( GameScene.layer( GameScene.Layer.LIGHTS ), x, gy, CRACKLE_LEVEL ) != null) cracklesLit++;
			}
			if (StrikeGround.struck( x, arc.to().y, major, rich )) {
				//水面被打亮（wash/ring/disturb）
			} else if (rich) {
				scorchesLaid++;
			}
		}
		if (!rich) return;
		strikeT = 0f;
	}

	//a pool of its own to drive at `share` of its halo, where it lies now
	private void keep( FxLight p, float share ){
		pools[poolCount] = p;
		poolShare[poolCount] = share;
		poolX[poolCount] = p.centerX();
		poolY[poolCount++] = p.centerY();
	}

	//whether its pool `i` is still its own to drive
	private boolean drives( int i ){
		FxLight p = pools[i];
		return p.alive && !p.isPersistent() && p.centerX() == poolX[i] && p.centerY() == poolY[i]
				&& StrikeLights.drives( this, p );
	}

	//what it leaves on the eye: its cores, a moment longer
	private void afterglow(){
		Group layer = GameScene.layer( GameScene.Layer.LIGHTS );
		if (layer == null) return;
		Afterglow glow = null;
		for (Arc arc : arcs){
			if (!arc.strikes()) continue;
			if (glow == null){
				glow = Afterglow.begin( layer );
				if (glow == null) return;
			}
			arc.afterglow( glow );
		}
		if (glow != null) afterglowsShown++;
	}

	private void release(){
		if (rich){
			rich = false;
			releaseRich();
		}
		killAndErase();
	}

	@Override
	public void destroy(){
		super.destroy();
		if (rich){
			rich = false;
			releaseRich();
		}
	}

	@Override
	public void draw() {
		Blending.setLightMode();
		super.draw();
		Blending.setNormalMode();
	}

	//A lightning object is meant to be loaded up with arcs.
	//these act as a means of easily expressing lighting between two points.
	public static class Arc extends Group {

		private static final double A = 180 / Math.PI;

		private Image arc1, arc2;

		//starting and ending x/y values
		private PointF start, end;

		public Arc(int from, int to){
			this( DungeonTilemap.tileCenterToWorld(from),
					DungeonTilemap.tileCenterToWorld(to));
		}

		public Arc(PointF from, int to){
			this( from, DungeonTilemap.tileCenterToWorld(to));
		}

		public Arc(int from, PointF to){
			this( DungeonTilemap.tileCenterToWorld(from), to);
		}

		public Arc(PointF from, PointF to){
			start = from;
			end = to;

			arc1 = new Image(Effects.get(Effects.Type.LIGHTNING));
			arc1.x = start.x - arc1.origin.x;
			arc1.y = start.y - arc1.origin.y;
			arc1.origin.set( 0, arc1.height()/2 );
			add( arc1 );

			arc2 = new Image(Effects.get(Effects.Type.LIGHTNING));
			arc2.origin.set( 0, arc2.height()/2 );
			add( arc2 );

			update();
		}

		/** Where it sets off and where it strikes. */
		public PointF from(){
			return start;
		}

		public PointF to(){
			return end;
		}

		/** 够长才算"打到了地上"：一格之内的小电火花不铺光池、不留焦痕。 */
		public boolean strikes(){
			return PointF.distance( start, end ) >= GLOW_FROM;
		}

		/** 它最后的样子，抄一份进余光（Afterglow）。 */
		public boolean afterglow( Afterglow glow ){
			if (!glow.line( arc1 )) return false;
			return glow.line( arc2 );
		}

		public void alpha(float alpha) {
			arc1.am = arc2.am = alpha;
		}

		@Override
		public void update() {
			float x2 = (start.x + end.x) / 2 + Random.Float( -4, +4 );
			float y2 = (start.y + end.y) / 2 + Random.Float( -4, +4 );

			float dx = x2 - start.x;
			float dy = y2 - start.y;
			arc1.angle = (float)(Math.atan2( dy, dx ) * A);
			arc1.scale.x = (float)Math.sqrt( dx * dx + dy * dy ) / arc1.width;

			dx = end.x - x2;
			dy = end.y - y2;
			arc2.angle = (float)(Math.atan2( dy, dx ) * A);
			arc2.scale.x = (float)Math.sqrt( dx * dx + dy * dy ) / arc2.width;
			arc2.x = x2 - arc2.origin.x;
			arc2.y = y2 - arc2.origin.x;
		}
	}
}
