/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Warped Pixel Dungeon
 * Copyright (C) 2026 Gabriel Duarte Guerra (gabriwar)
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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx.water;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.WaterFX;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Ramp;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Texture;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.particles.Emitter;

/**
 * The water's live surface (docs/fx/water-life.md, "A live surface"), one a scene, put in the
 * scene's sheen layer by the water's life (WaterLifeFx.sceneCreated).
 *
 * The palette cycle: the level's water texture swapped every frame of its liquid's cycle
 * (Element.Liquid.cycleFps) for the next of eight painted frames (tools/water_live_gen.py), so a
 * crest of light runs across the water - the same plane, the same scroll, no draw more. The
 * frames are sliced off their strip into eight 32x32 textures that repeat, made on the render
 * thread the first time they are wanted (and again after the texture cache was cleared, as a
 * region change does); the plane's texture field is set, never texture(), which would reset its
 * scroll. The liquid also sets how fast the plane scrolls (the lava crawls at 2 px/s, the city's
 * oil at 3.5). Frozen with every emitter (the hourglass).
 *
 * The sheens: two added pictures that tile at 64 px laid over the water (tools/water_live_gen.py)
 * - A, dashes of light along gentle crests, drifting (+3, +1) px/s; B, a sparse caustic net,
 * drifting (-2, +2.5) - in the liquid's glint by day and the moon's D8E4FF at night (leaning to the
 * aurora's green under one), at the liquid's alphas (the caves' dimmer, the city's oil in its own
 * two colours, none on the lava), dimmed by clouds over open water (to half), underground (0.8),
 * at night (0.6) and by a rain beating the water (to 0.3 in a downpour), breathing +-12% at 0.25 Hz half a cycle apart. Each is cut every frame to
 * the box of the water cells in view (a row scan, no allocation) and anchored to the world, so it
 * stays put as the view moves and over the overworld's slides (64 divides the window's 512); the
 * shore's tiles mask it where the box takes in dry ground. Hidden with no water in view, and as
 * the budget's governor asks (B off from REDUCED, A at MINIMAL). In a wind of 4 m/s or more under
 * an open sky a darker band 24 px wide (cat's paws: A at 60%) runs over the water with the wind,
 * at its gusting pace - A is drawn in three pieces while one crosses. Over the open sea they
 * drift half again as fast. On the lava, in their place, its glow (lava_glow.png) added in FF7A20,
 * swelling from 0.10 to 0.25 and back over 3.5 s, crawling with it. At most four quads (and the
 * rain's pocks), all of them clipped.
 *
 * Render thread only: built and run from the scene's update.
 */
public class WaterLive extends Group {

	private static final float SIZE = DungeonTilemap.SIZE;

	/** Debug only (the gallery's liquid carousel): this liquid shown instead of the level's. */
	public static volatile Element.Liquid debugLiquid;

	/** The liquid the water shows: the debug carousel's, else the level's (WaterFX.liquid()). */
	public static Element.Liquid liquid(){
		Element.Liquid d = debugLiquid;
		return d != null ? d : WaterFX.liquid();
	}

	/** A liquid's water texture (FROZEN's included), the one its cycle was painted from. */
	public static String baseOf( Element.Liquid l ){
		switch (l){
			case PRISON: return Assets.Environment.WATER_PRISON;
			case CAVE:   return Assets.Environment.WATER_CAVES;
			case CITY:   return Assets.Environment.WATER_CITY;
			case LAVA:   return Assets.Environment.WATER_HALLS;
			case FROZEN: return Assets.Environment.WATER_FROZEN;
			case TEMPLE: return Assets.Environment.WATER_TEMPLE;
			default:     return Assets.Environment.WATER_SEWERS;
		}
	}

	/** A liquid's cycle (a strip of eight frames), or null for one that keeps still (the ice). */
	public static String cycleOf( Element.Liquid l ){
		switch (l){
			case SEWER:  return Assets.Environment.WATER_SEWERS_CYCLE;
			case PRISON: return Assets.Environment.WATER_PRISON_CYCLE;
			case CAVE:   return Assets.Environment.WATER_CAVES_CYCLE;
			case CITY:   return Assets.Environment.WATER_CITY_CYCLE;
			case LAVA:   return Assets.Environment.WATER_HALLS_CYCLE;
			case TEMPLE: return Assets.Environment.WATER_TEMPLE_CYCLE;
			default:     return null;
		}
	}

	/**
	 * The `n` square frames of `size` px of a strip, each a texture of its own that repeats
	 * (keyed "<strip>#k" in the texture cache, so a scene finds the last one's), drawn NEAREST.
	 * Made from the strip's bitmap on the CPU: the GPU first sees them when they are drawn.
	 * Render thread.
	 */
	public static SmartTexture[] slices( String strip, int n, int size ){
		SmartTexture[] out = new SmartTexture[n];
		SmartTexture src = null;
		for (int k = 0; k < n; k++){
			String key = strip + "#" + k;
			if (TextureCache.contains( key )){
				out[k] = TextureCache.get( key );
				continue;
			}
			if (src == null) src = TextureCache.get( strip );
			SmartTexture t = TextureCache.create( key, size, size );
			Pixmap pm = t.bitmap;
			pm.setBlending( Pixmap.Blending.None );
			pm.drawPixmap( src.bitmap, 0, 0, k * size, 0, size, size );
			t.filter( Texture.NEAREST, Texture.NEAREST );
			t.wrap( Texture.REPEAT, Texture.REPEAT );
			out[k] = t;
		}
		return out;
	}

	//the liquid shown, its cycle's frames (null: the plane keeps its own texture), the frame on
	//show and the cycle's clock
	private Element.Liquid shown;
	private String shownTex;
	private SmartTexture[] cycle;
	private String cycleKey;
	private int frame = -1;
	private float cycleT;
	//the plane's own texture, as the scene made it
	private SmartTexture own;

	/** Test only: the water's plane while there is no game scene. */
	static SkinnedBlock planeForTests;

	static SkinnedBlock plane(){
		SkinnedBlock p = GameScene.waterPlane();
		return p != null ? p : planeForTests;
	}

	@Override
	public void update(){
		super.update();
		SkinnedBlock plane = plane();
		Level level = Dungeon.level;
		if (plane == null || level == null) return;
		if (own == null) own = plane.texture;
		Element.Liquid l = liquid();
		GameScene.waterScroll = l.scroll;
		cycle( plane, level, l );

		float dt = Emitter.freezeEmitters ? 0f : Game.elapsed;
		t += dt;
		WaterWalk.update( dt );
		WaterVoices.update( dt );
		if ((slowIn -= Game.elapsed) <= 0f){
			slowIn = 1f;
			look( level );
		}
		//the overworld's window mid-slide: its map is not the one drawn; keep where things are
		if (level.fogHeld()) return;
		water = view( level );
		if (DRAW_SHORE) shore( level, l, dt );
		if (DRAW_SURFACE){
			sheens( l, dt );
			if (DRAW_GLOW) lavaGlow( l, dt );
			if (DRAW_POCKS) pocks( l, dt );
		}
		FireReflections.update( dt );
		//雨点打在水面上的一圈圈涟漪（有天气时才有）
		WaterRain.rain( level, dt );
		//the rime creeping over freezing water
		IceCrust.frost( level, dt );
		//the low mist that lies over still water
		LowMist.mist( level, dt );
		//ice creeping in at the banks, and the waves that break on them
		IceFringe.frost( level, dt );
		ShoreSurf.surf( level, dt );
		driveFalls( level, l, dt );
		// 屋顶滴水：偶尔一颗从上方落下（带下落轨迹与落水的水花）
		if (dt > 0f && (dropIn -= dt) <= 0f){
			dropIn = 0.35f + FxRandom.Float( 0.9f );
			dripOne( level, l );
		}
		ambience.update( level, l, dt,
				(int)( boxX / SIZE ), (int)( boxY / SIZE ),
				(int)( (boxX + boxW) / SIZE ), (int)( (boxY + boxH) / SIZE ) );
	}

	/** 岸边泡沫边：每 0.22 秒重建一次，随机缺口 + 随机亮度，视觉上把直角边打碎 */
	private void shore( Level level, Element.Liquid l, float dt ){
		if (level == null || level.water == null || boxW <= 0f) return;
		if (shore == null){
			Group host = com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.waterSheen();
			if (host == null) return;
			shore = new Group();
			host.add( shore );
		}
		shoreT += dt;
		if (shoreT < 0.22f) return;
		shoreT = 0f;

		int w = level.width(), h = level.height();
		int x0 = Math.max( 0, (int)( boxX / SIZE ) ), x1 = Math.min( w - 1, (int)( (boxX + boxW) / SIZE ) );
		int y0 = Math.max( 0, (int)( boxY / SIZE ) ), y1 = Math.min( h - 1, (int)( (boxY + boxH) / SIZE ) );
		int used = 0;

		for (int y = y0; y <= y1; y++){
			for (int x = x0; x <= x1; x++){
				int cell = x + y * w;
				if (!level.water[cell]) continue;

				boolean landS = y + 1 >= h || !level.water[cell + w];
				boolean landN = y <= 0 || !level.water[cell - w];
				boolean landW = x <= 0 || !level.water[cell - 1];
				boolean landE = x + 1 >= w || !level.water[cell + 1];
				// 角落：两侧都是陆地时不画边，视觉上就是"缺角"
				boolean edgeS = landS && !(landW && landE);
				boolean edgeN = landN && !(landW && landE);
				boolean edgeW = landW && !(landN && landS);
				boolean edgeE = landE && !(landN && landS);
				if (!edgeS && !edgeN && !edgeW && !edgeE) continue;

				float bx = x * SIZE, by = y * SIZE;
				if (edgeS) used = shoreSeg( used, bx, by + SIZE - 2f, SIZE, 2f, l );
				if (edgeN) used = shoreSeg( used, bx, by, SIZE, 2f, l );
				if (edgeW) used = shoreSeg( used, bx, by, 2f, SIZE, l );
				if (edgeE) used = shoreSeg( used, bx + SIZE - 2f, by, 2f, SIZE, l );
				if (used >= SHORE_MAX) break;
			}
			if (used >= SHORE_MAX) break;
		}
		for (int i = used; i < shoreBlocks.size(); i++) shoreBlocks.get( i ).visible = false;
	}

	/** 画一段岸边泡沫；有缺口、有明暗变化 */
	private int shoreSeg( int used, float x, float y, float w, float h, Element.Liquid l ){
		if (FxRandom.chance( 0.35f )) return used;          // 缺口
		if (used >= SHORE_MAX) return used;
		com.watabou.noosa.ColorBlock b;
		if (used < shoreBlocks.size()){
			b = shoreBlocks.get( used );
		} else {
			b = new com.watabou.noosa.ColorBlock( 1, 1, 0xFFFFFFFF );
			shoreBlocks.add( b );
			shore.add( b );
		}
		b.x = x;
		b.y = y;
		b.size( w, h );
		b.visible = true;
		b.hardlight( darken( l.highlight, 0.45f ) );
		b.am = 0.30f + FxRandom.Float( 0.20f );
		return used + 1;
	}

	/** 把颜色按比例调暗 */
	private static int darken( int color, float k ){
		int r = (int)( ((color >> 16) & 0xFF) * k );
		int g = (int)( ((color >> 8) & 0xFF) * k );
		int b = (int)( (color & 0xFF) * k );
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	/** 岸边零星泡沫点（2x2），让边线更碎 */
	private int shoreDots( int used, float x, float y, Element.Liquid l ){
		if (!FxRandom.chance( 0.45f )) return used;
		if (used >= SHORE_MAX) return used;
		com.watabou.noosa.ColorBlock b;
		if (used < shoreBlocks.size()) b = shoreBlocks.get( used );
		else {
			b = new com.watabou.noosa.ColorBlock( 1, 1, 0xFFFFFFFF );
			shoreBlocks.add( b );
			shore.add( b );
		}
		b.x = x + FxRandom.Float( SIZE - 4f );
		b.y = y + FxRandom.Float( 3f );
		b.size( 2f, 2f );
		b.visible = true;
		b.hardlight( l.highlight );
		b.am = 0.25f + FxRandom.Float( 0.45f );
		return used + 1;
	}

	/** 视野内"水→深渊"的边缘：把水沿边滴下去（Warped 的 WaterFalls） */
	private void driveFalls( Level level, Element.Liquid l, float dt ){
		if (dt <= 0f || level == null || level.water == null) return;
		com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter air =
				com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx.air();
		if (air == null) return;

		seenFalls.clear();
		int w = level.width(), h = level.height();
		int x0 = Math.max( 0, (int)( boxX / SIZE ) ), x1 = Math.min( w - 1, (int)( (boxX + boxW) / SIZE ) );
		int y0 = Math.max( 0, (int)( boxY / SIZE ) ), y1 = Math.min( h - 1, (int)( (boxY + boxH) / SIZE ) );

		for (int y = y0; y <= y1 && falls.size() < 12; y++){
			for (int x = x0; x <= x1 && falls.size() < 12; x++){
				int cell = x + y * w;
				if (!level.water[cell]) continue;

				int side = -2;
				if (y + 1 < h && isPit( level, cell + w )) side = 0;
				else if (x - 1 >= 0 && isPit( level, cell - 1 )) side = -1;
				else if (x + 1 < w && isPit( level, cell + 1 )) side = 1;
				if (side == -2) continue;

				WaterFalls.Fall f = falls.get( cell );
				if (f == null){
					f = WaterFalls.fall( level, cell, side );
					falls.put( cell, f );
					air.add( f.lip() );
					fallEdges++;
				}
				seenFalls.add( cell );
				WaterFalls.fall( f, air, level.heroFOV[cell], dt );
			}
		}
		// 不在视野/不再成立的边：撤掉
		java.util.Iterator<java.util.Map.Entry<Integer,WaterFalls.Fall>> it = falls.entrySet().iterator();
		while (it.hasNext()){
			java.util.Map.Entry<Integer,WaterFalls.Fall> e = it.next();
			if (!seenFalls.contains( e.getKey() )){
				e.getValue().lip().kill();
				it.remove();
			}
		}
	}

	/** 只把深渊算作边（岸边用 lip 会画出一圈白点，已弃用） */
	private static boolean isPit( Level level, int cell ){
		if (cell < 0 || cell >= level.map.length) return false;
		return level.map[cell] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM;
	}

	/** 在视野内随机一格水面滴水：三成是一颗从上落下的水珠，其余是落水的小涟漪 */
	private void dripOne( Level level, Element.Liquid l ){
		if (level == null || level.water == null || boxW <= 0 || boxH <= 0) return;
		for (int tries = 0; tries < 8; tries++){
			int x = (int)( boxX / SIZE ) + FxRandom.IntRange( 0, Math.max( 0, (int)( boxW / SIZE ) - 1 ) );
			int y = (int)( boxY / SIZE ) + FxRandom.IntRange( 0, Math.max( 0, (int)( boxH / SIZE ) - 1 ) );
			if (x < 0 || y < 0 || x >= level.width() || y >= level.height()) continue;
			int cell = x + y * level.width();
			if (!level.water[cell] || !level.heroFOV[cell]) continue;

			float px = x * SIZE + FxRandom.Float( SIZE );
			float py = y * SIZE + FxRandom.Float( SIZE );
			if (FxRandom.chance( 0.35f )){
				com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter air =
						com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx.air();
				if (air != null) com.shatteredpixel.shatteredpixeldungeon.effects.fx.water.WaterDrops.fall( air, px, py, l.foam );
			} else {
				com.shatteredpixel.shatteredpixeldungeon.effects.fx.water.WaterDrops.drip( px, py );
			}
			drips++;
			return;
		}
	}

	//glints, lap foam, whitecaps, the swamp's gas

	/** The lava's banks lit now (tests). */
	int banksLitForTests(){
		return ambience.banksLit();
	}

	/** The water cells in view the hero saw at the live surface's last look. */
	public static int waterInView(){
		return SEEN;
	}

	/** A new scene: nothing of the last one's water in view. */
	/** 新场景：建一个活水面挂到 SURFACE 层，并清掉上一场的状态 */
	public static void sceneCreated(){
		debugLiquid = null;
		SEEN = 0;
		Marks.reset();
		WaterLava.reset();
		WaterFalls.reset();
		WaterRain.reset();
		WaterWalk.reset();
		WaterVoices.reset();
		current = null;
		SEEN = 0;

		WaterLive live = new WaterLive();
		// 必须挂在地块之下：水格不画瓦片，sheen 才只在上面露出来
		com.watabou.noosa.Group host = com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.waterSheen();
		if (host != null) host.add( live );
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.subsurface( new FishShadows() );
	}

	/** GameScene 每帧调用：驱动 WaterWalk（水线/涟漪） */
	public static void update( float dt ){
		WaterWalk.update( dt );
		if (Dungeon.level != null) SEEN = 16;
	}

	/** The water at a cell laps its banks now (a LAP voice heard from it). Render thread. */
	public static void lap( int cell ){
		WaterLive c = current;
		Level level = Dungeon.level;
		if (c == null || level == null || cell < 0 || cell >= level.length() || !level.water[cell]) return;
		Element.Liquid l = liquid();
		// 本仓库没有水声系统
	}

	//the scene's live surface
	private static WaterLive current;
	/** 视野内水格数（本仓库没有 WaterAmbience，用一个常数代替预算判断） */
	static int SEEN = 0;

	public WaterLive(){
		super();
		current = this;
	}

	/** The scene's live surface while it stands, to keep pictures that go with the scene but are
	 *  drawn by something else (a wader's waterline); null off the scene. */
	static Group keeper(){
		WaterLive c = current;
		return c != null && c.parent != null ? c : null;
	}

	// ------------------------------------------------------------------ the lava's glow

	/** The lava's glow over the whole of it: added in GLOW, its alpha swelling from GLOW_MIN to
	 *  GLOW_MAX and back every GLOW_PERIOD s, riding the lava's crawl. */
	static final int GLOW = 0xFF7A20;
	static final float GLOW_MIN = 0.10f, GLOW_MAX = 0.25f, GLOW_PERIOD = 3.5f;

	private Plane glow;
	private float glowY;

	/** The lava glow's alpha `t` s in: 0.10 to 0.25 and back over 3.5 s. */
	static float glowAlpha( float t ){
		float k = 0.5f - 0.5f * (float)Math.cos( 2 * Math.PI * t / GLOW_PERIOD );
		return GLOW_MIN + (GLOW_MAX - GLOW_MIN) * k;
	}

	//in place of the sheens on the lava (off with sheen A as the governor asks)
	private void lavaGlow( Element.Liquid l, float dt ){
		if (!water || l != Element.Liquid.LAVA || !FxBudget.sheenA()){
			hide( glow );
			return;
		}
		if (glow == null) glow = (Plane) add( new Plane( Assets.Effects.LAVA_GLOW, true ) );
		glowY = wrap( glowY + l.scroll * dt );
		glow.cut( boxX, boxY, boxW, boxH, 0f, glowY, GLOW, Math.min( SHEEN_CAP, glowAlpha( t ) ) );
	}

	// ------------------------------------------------------------------ the rain's pocks

	/** The pocks' frames swap this often, s, each at a new offset so their loop never shows; at
	 *  night they are lifted by POCK_NIGHT_LIFT (under the night's tint), never past POCK_CAP. */
	static final float POCK_SWAP = 0.08f, POCK_NIGHT_LIFT = 0.4f, POCK_CAP = 0.6f;

	private Plane pocks;
	private SmartTexture[] pockFrames;
	private int pockFrame;
	private float pockIn, pockX, pockY;

	//past 0.4 of rain or sleet, the rain's landing all over the water at once
	private void pocks( Element.Liquid l, float dt ){
		float a = Math.min( POCK_CAP, WaterGeom.pockAlpha( 0f )
				* WaterGeom.nightLift( GameScene.nightTintAlpha(), POCK_NIGHT_LIFT ) );
		if (!water || a <= 0f || l == Element.Liquid.LAVA || !FxBudget.sheenA()){
			if (pocks != null) pocks.visible = false;
			return;
		}
		if (pockFrames == null || !TextureCache.contains( Assets.Effects.WATER_POCKS + "#3" )){
			pockFrames = slices( Assets.Effects.WATER_POCKS, 4, WaterGeom.SHEEN_TILE );
			if (pocks != null) pocks.texture = pockFrames[pockFrame];
		}
		if (pocks == null) pocks = (Plane) add( new Plane( pockFrames[0], false ) );
		if ((pockIn -= dt) <= 0f){
			pockIn = POCK_SWAP;
			pockFrame = (pockFrame + 1) % pockFrames.length;
			pocks.texture = pockFrames[pockFrame];
			pockX = FxRandom.Int( WaterGeom.SHEEN_TILE );
			pockY = FxRandom.Int( WaterGeom.SHEEN_TILE );
		}
		pocks.cut( boxX, boxY, boxW, boxH, pockX, pockY, l.foam, a );
	}

	//the plane's texture: the cycle's frame now, or the liquid's own still texture
	/** 调试：true 时用基础水面贴图，不做调色板循环 */
	public static boolean NO_CYCLE = true; // 默认关：调色板循环会让水面出现 32px 网格感
	/** 是否绘制水面叠加层（sheen/pocks/岩浆辉光）。关掉后水面只保留基础滚动贴图 */
	public static boolean DRAW_SURFACE = true;
	public static boolean DRAW_GLOW = false, DRAW_POCKS = false;
	/** 岸边泡沫边（打破笔直的直角边） */
	public static boolean DRAW_SHORE = false; // 有拼接图块后不需要程序化岸线

	private void cycle( SkinnedBlock plane, Level level, Element.Liquid l ){
		if (NO_CYCLE){
			if (cycle != null){ cycle = null; plane.texture = own; }
			return;
		}
		String tex = level.waterTex();
		boolean debug = debugLiquid != null;
		if (l != shown || tex != shownTex || (cycle != null && !TextureCache.contains( cycleKey ))){
			shown = l;
			shownTex = tex;
			frame = -1;
			String strip = cycleOf( l );
			//a level's water cycles only when it shows its liquid's own texture
			if (strip != null && (debug || baseOf( l ).equals( tex ))){
				cycle = slices( strip, WaterGeom.CYCLE_FRAMES, WaterGeom.CYCLE_TILE );
				cycleKey = strip + "#" + (WaterGeom.CYCLE_FRAMES - 1);
			} else {
				cycle = null;
				//ice is a surface of its own: a freezing sky puts the frozen sheet over the water
				boolean ice = l == Element.Liquid.FROZEN;
				plane.texture = (debug || ice) ? repeating( baseOf( l ) ) : own;
			}
		}
		if (cycle == null) return;
		if (!Emitter.freezeEmitters) cycleT += Game.elapsed;
		int f = WaterGeom.cycleFrame( cycleT, l.cycleFps );
		if (f != frame || plane.texture != cycle[f]){
			frame = f;
			plane.texture = cycle[f];
		}
	}

	// ------------------------------------------------------------------ the water in view

	//the water cells in view (cells, inclusive), and as world px; whether there are any
	private final int[] box = new int[4];
	private boolean water;
	/** 屋顶滴水：计时与计数 */
	private float dropIn = 0.3f;
	public static int drips;
	/** 水沿边缘往深渊里滴 */
	private final java.util.Map<Integer,WaterFalls.Fall> falls = new java.util.HashMap<>();
	private final java.util.Set<Integer> seenFalls = new java.util.HashSet<>();
	public static int fallEdges;
	/** 岸边泡沫：一组短线段，随机明暗/缺口，让水边不笔直 */
	private final WaterAmbience ambience = new WaterAmbience();
	private Group shore;
	private static final int SHORE_MAX = 420;
	private final java.util.List<com.watabou.noosa.ColorBlock> shoreBlocks = new java.util.ArrayList<>();
	private float shoreT;
	private float boxX, boxY, boxW, boxH;
	//the cells in view, and one round them
	private int vx0, vy0, vx1, vy1;

	/** The water cells within the camera's view and a cell round it, cut to their box; false
	 *  for none. The whole level off the game's scene (a test's). */
	private boolean view( Level level ){
		Camera c = Camera.main;
		vx0 = 0;
		vy0 = 0;
		vx1 = level.width() - 1;
		vy1 = level.height() - 1;
		if (c != null && GameScene.waterPlane() != null){
			vx0 = (int)Math.floor( c.scroll.x / SIZE ) - 1;
			vy0 = (int)Math.floor( c.scroll.y / SIZE ) - 1;
			vx1 = (int)Math.floor( (c.scroll.x + c.width) / SIZE ) + 1;
			vy1 = (int)Math.floor( (c.scroll.y + c.height) / SIZE ) + 1;
		}
		if (!WaterGeom.waterBox( level.water, level.width(), level.height(), vx0, vy0, vx1, vy1, box )) return false;
		boxX = box[0] * SIZE;
		boxY = box[1] * SIZE;
		boxW = (box[2] - box[0] + 1) * SIZE;
		boxH = (box[3] - box[1] + 1) * SIZE;
		return true;
	}

	//once a second: the open sea, the aurora
	private float slowIn;
	private boolean ocean, aurora;

	private void look( Level level ){
		ocean = false;
		aurora = false;
	}

	// ------------------------------------------------------------------ the sheens

	/** The sheens' drift, px/s: A (+3, +1), B (-2, +2.5); over the open sea half again as fast. */
	static final float A_VX = 3f, A_VY = 1f, B_VX = -2f, B_VY = 2.5f, OCEAN_PACE = 1.5f;
	/** The moon's tint the sheens take at night, and the aurora's they lean toward under it. */
	static final int MOON = 0xD8E4FF, AURORA = 0x40E888;
	static final float AURORA_SHARE = 0.15f;
	/** No sheen past this alpha: an added layer over the whole water. */
	static final float SHEEN_CAP = 0.30f;
	/** The cat's paws: under an open sky in a wind of PAWS_WIND m/s or more, a band PAWS_W px wide
	 *  where sheen A shows at PAWS_DIM runs over the water with the wind, at its gusting pace. */
	static final float PAWS_WIND = 4f, PAWS_DIM = 0.6f;
	static final int PAWS_W = 24;

	//sheen A in up to three pieces (before, in and after a cat's paw), and sheen B
	private Plane a0, a1, a2, b;
	private float driftAX, driftAY, driftBX, driftBY, t;
	//the cat's paw's left edge, world px (NaN: none), and the wait for the next
	private float pawX = Float.NaN, pawWait;

	private void sheens( Element.Liquid l, float dt ){
		boolean showA = water && l.sheenA > 0 && FxBudget.sheenA();
		boolean showB = water && l.sheenB > 0 && FxBudget.sheenB();
		if (!showA && !showB){
			hide( a0 );
			hide( a1 );
			hide( a2 );
			hide( b );
			pawX = Float.NaN;
			return;
		}
		if (b == null){
			b = (Plane) add( new Plane( Assets.Effects.WATER_SHEEN_B, true ) );
			a0 = (Plane) add( new Plane( Assets.Effects.WATER_SHEEN_A, true ) );
			a1 = (Plane) add( new Plane( Assets.Effects.WATER_SHEEN_A, true ) );
			a2 = (Plane) add( new Plane( Assets.Effects.WATER_SHEEN_A, true ) );
		}
		float pace = (ocean ? OCEAN_PACE : 1f) * dt;
		driftAX = wrap( driftAX + A_VX * pace );
		driftAY = wrap( driftAY + A_VY * pace );
		driftBX = wrap( driftBX + B_VX * pace );
		driftBY = wrap( driftBY + B_VY * pace );

		float tint = GameScene.nightTintAlpha();
		float n = WaterGeom.night( tint );
		boolean open = Fx.openSky();
		float cover = 0f;
		//a rain beating the water breaks its calm shine
		float rain = 1f;

		if (showB){
			int c = sheenColor( l.sheenTintB != Element.NONE ? l.sheenTintB : l.glint, n );
			float a = Math.min( SHEEN_CAP, sheenB( l ) * rain * WaterGeom.sheenShare( open, cover, tint, t, (float)Math.PI ) );
			b.cut( boxX, boxY, boxW, boxH, driftBX, driftBY, c, a );
		} else {
			hide( b );
		}

		if (!showA){
			hide( a0 );
			hide( a1 );
			hide( a2 );
			return;
		}
		int c = sheenColor( l.sheenTintA != Element.NONE ? l.sheenTintA : l.glint, n );
		float a = Math.min( SHEEN_CAP, sheenA( l ) * rain * WaterGeom.sheenShare( open, cover, tint, t, 0f ) );
		float right = boxX + boxW;
		if (paws( open, dt ) && pawX < right && pawX + PAWS_W > boxX){
			float l0 = Math.max( boxX, pawX ), r0 = Math.min( right, pawX + PAWS_W );
			cutOrHide( a0, boxX, l0 - boxX, c, a );
			cutOrHide( a1, l0, r0 - l0, c, a * PAWS_DIM );
			cutOrHide( a2, r0, right - r0, c, a );
		} else {
			a0.cut( boxX, boxY, boxW, boxH, driftAX, driftAY, c, a );
			hide( a1 );
			hide( a2 );
		}
	}

	private void cutOrHide( Plane p, float x, float w, int color, float alpha ){
		if (w < 1f) hide( p );
		else p.cut( x, boxY, w, boxH, driftAX, driftAY, color, alpha );
	}

	//the cat's paw under way: true while one crosses the water
	private boolean paws( boolean open, float dt ){
		if (true){
			pawX = Float.NaN;
			return false;
		}
		float wind = Fx.windX();
		float dir = wind >= 0 ? 1f : -1f, speed = Math.max( PAWS_WIND, Math.abs( wind ) );
		if (Float.isNaN( pawX )){
			if ((pawWait -= dt) > 0f) return false;
			pawX = dir > 0 ? boxX - PAWS_W : boxX + boxW;
		}
		pawX += dir * speed * dt;
		if (dir > 0 ? pawX > boxX + boxW : pawX + PAWS_W < boxX){
			pawX = Float.NaN;
			pawWait = FxRandom.Float( 1f, 4f );
			return false;
		}
		return true;
	}

	/** The caves' sheens, brighter than their liquid's own 0.12 and 0.08 (Element.Liquid.CAVE, the
	 *  kit's: a kit request to take these): on their dark water under a roof the dimmer ones barely
	 *  showed. */
	static final float CAVE_SHEEN_A = 0.18f, CAVE_SHEEN_B = 0.12f;

	static float sheenA( Element.Liquid l ){
		return l == Element.Liquid.CAVE ? CAVE_SHEEN_A : l.sheenA;
	}

	static float sheenB( Element.Liquid l ){
		return l == Element.Liquid.CAVE ? CAVE_SHEEN_B : l.sheenB;
	}

	//the sheen's colour: its own by day, the moon's at night, leaning to the aurora's under one
	private int sheenColor( int day, float night ){
		int c = Ramp.lerp( day, MOON, night );
		return aurora && night > 0f ? Ramp.lerp( c, AURORA, AURORA_SHARE * night ) : c;
	}

	private static float wrap( float v ){
		v %= WaterGeom.SHEEN_TILE;
		return v < 0 ? v + WaterGeom.SHEEN_TILE : v;
	}

	private static void hide( Plane p ){
		if (p != null) p.visible = false;
	}

	/**
	 * A picture that tiles over the water (a sheen, the pocks, the lava's glow): cut to a box each
	 * frame and anchored to the world, so the same world point always shows the same texel, its
	 * drift aside. Added (light) or laid on (matter) in one colour at one alpha.
	 */
	static final class Plane extends SkinnedBlock {

		final boolean additive;

		Plane( Object texture, boolean additive ){
			super( 1, 1, texture );
			this.additive = additive;
			visible = false;
		}

		/** Over the box at (x, y), w x h world px, the texture drifted (dx, dy) px, in `color` at `alpha`. */
		void cut( float x, float y, float w, float h, float dx, float dy, int color, float alpha ){
			if (alpha <= 0.004f){
				visible = false;
				return;
			}
			this.x = x;
			this.y = y;
			if (w != width || h != height) size( w, h );
			offsetTo( WaterGeom.anchor( x, -dx, texture.width ), WaterGeom.anchor( y, -dy, texture.height ) );
			hardlight( color );
			am = alpha;
			visible = true;
		}

		@Override
		public void draw(){
			if (additive){
				Blending.setLightMode();
				super.draw();
				Blending.setNormalMode();
			} else {
				super.draw();
			}
		}
	}

	//a still water texture that repeats, for the debug carousel
	private static SmartTexture repeating( String asset ){
		SmartTexture t = TextureCache.get( asset );
		t.wrap( Texture.REPEAT, Texture.REPEAT );
		return t;
	}
}
