package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;

/**
 * 弓的持续光效（抄 Warped 的思路）：
 * 1) 装备弓时，角色身周有一圈绕转的光点（对应 Warped 的 StanceOrbit：MOTE_2 + 拖尾 + 呼吸）
 * 2) 自然之力激活时，身周持续飘落绿叶（对应 SpiritBow 里的 LeafParticle.GENERAL 喷发）
 */
public final class BowAura {

	private BowAura(){}

	public static int spawned;
	private static float orbitIn = 0f;
	private static float orbitAngle = 0f;
	private static float leafIn = 0f;

	/** 常驻的环绕光点（贴图，保证一定可见） */
	private static com.watabou.noosa.Group orbit;
	private static com.watabou.noosa.Image[] motes;
	private static com.watabou.noosa.Group leafGroup;

	/** 每帧调用（GameScene.update） */
	public static void update(){
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive() || hero.sprite == null || !hero.sprite.visible) return;

		CharSprite s = hero.sprite;
		float cx = s.x + s.width() / 2f;
		float cy = s.y + s.height() / 2f;

		Item bow = bowOf( hero );
		if (bow != null){
			orbitAngle += Game.elapsed * 2.4f;      // Warped StanceOrbit: speed(2.4f)
			updateOrbit( s, cx, cy, EnchantFx.itemColor( bow ) );
		} else {
			hideOrbit();
		}

		// 自然之力：绿叶环绕
		if (hero.buff( NaturesPower.naturesPowerTracker.class ) != null){
			if ((leafIn -= Game.elapsed) <= 0f){
				leafIn = 0.12f;
				spawnLeaf( cx, cy, s );
			}
		} else {
			leafIn = 0f;
		}
	}

	/** 建/更新常驻环绕光点（6 个，绕角色转） */
	private static void updateOrbit( CharSprite s, float cx, float cy, int tint ){
		if (s.parent == null) return;
		// 换层/换场景后旧的 group 已经脱离父节点，必须重建
		if (orbit != null && orbit.parent == null){
			orbit = null;
			motes = null;
		}
		if (orbit == null){
			orbit = new com.watabou.noosa.Group();
			s.parent.add( orbit );
			int[] f = FxFrames.MOTE_1;
			motes = new com.watabou.noosa.Image[10 * 3];   // 10 个光点，每个带 2 个拖尾
			for (int i = 0; i < motes.length; i++){
				motes[i] = new com.watabou.noosa.Image(
						com.shatteredpixel.shatteredpixeldungeon.Assets.Effects.FX_SHEET, f[0], f[1], f[2], f[3] );
				motes[i].scale.set( 1.5f );
				orbit.add( motes[i] );
			}
		}
		orbit.visible = true;
		float breath = 0.55f + 0.35f * (float)Math.sin( Game.timeTotal * 0.6f ); // StanceOrbit: breath(0.55,0.9,0.6)
		final int N = 10;
		for (int i = 0; i < N; i++){
			for (int k = 0; k < 3; k++){
				com.watabou.noosa.Image m = motes[i * 3 + k];
				float a = orbitAngle + (float)(Math.PI * 2 * i / N) - k * 0.18f;
				float rx = 13f, ry = 6.5f;
				m.visible = true;
				m.x = cx + (float)Math.cos( a ) * rx - m.width() * 0.75f;
				m.y = cy + (float)Math.sin( a ) * ry - m.height() * 0.75f - 2f;
				m.hardlight( EnchantFx.light( tint, 0.3f + k * 0.15f ) );
				float depth = 0.5f + 0.5f * (float)Math.sin( a );
				m.am = Math.max( 0.12f, breath * depth * (k == 0 ? 1f : (k == 1 ? 0.5f : 0.28f)) );
				m.scale.set( k == 0 ? 1.6f : 1.1f );
			}
			spawned++;
		}
	}

	/** 测试用：环绕光点是否挂着且可见 */
	public static boolean orbitVisible(){
		if (orbit == null || !orbit.visible || orbit.parent == null) return false;
		for (com.watabou.noosa.Image m : motes) if (m != null && m.visible) return true;
		return false;
	}

	/** 测试用：环绕光点数量 */
	public static int moteCount(){ return motes == null ? 0 : motes.length; }

	/** 测试用：当前挂着没 */
	public static String debugState(){
		return "orbit=" + (orbit != null) + " parent=" + (orbit != null && orbit.parent != null)
				+ " vis=" + (orbit != null && orbit.visible) + " motes=" + moteCount()
				+ " spriteParent=" + (Dungeon.hero != null && Dungeon.hero.sprite != null && Dungeon.hero.sprite.parent != null);
	}

	private static void hideOrbit(){
		if (orbit != null) orbit.visible = false;
	}

	/** 快捷栏/手持的弓（含 SpiritBow 与各种弩） */
	public static Item bowOf( Hero hero ){
		Item w = hero.belongings.weapon;
		if (w == null) return null;
		String n = w.getClass().getSimpleName().toLowerCase();
		return n.contains( "bow" ) ? w : null;
	}

	private static void spawnOrbitMote( float cx, float cy, int tint ){
		FxEmitter air = Fx.air();
		if (air == null) return;
		if (!FxBudget.keep( FxBudget.P2, cx, cy )) return;
		FxParticle p = (FxParticle) air.recycle( FxParticle.class );
		if (p == null) return;

		float a = orbitAngle + FxRandom.Float( -0.25f, 0.25f );
		float rx = 11f, ry = 5f;
		float x = cx + (float)Math.cos( a ) * rx;
		float y = cy + (float)Math.sin( a ) * ry - 2f;
		// 切向速度，形成绕转拖尾
		float vx = -(float)Math.sin( a ) * 26f;
		float vy = (float)Math.cos( a ) * 12f;

		spawned++;
		EnchantFx.boost( p, 2.0f ).reset( x, y )
				.look( FxFrames.MOTE_2 ).paint( EnchantFx.light( tint, 0.25f ) )
				.vel( vx, vy ).drag( 2.4f ).life( 0.55f )
				.alpha( 0.85f, 0.05f, 0.3f );
	}

	private static void spawnLeaf( float cx, float cy, CharSprite s ){
		FxEmitter air = Fx.air();
		if (air == null) return;
		if (!FxBudget.keep( FxBudget.P3, cx, cy )) return;
		FxParticle p = (FxParticle) air.recycle( FxParticle.class );
		if (p == null) return;
		p.reset( cx + FxRandom.Float( -7f, 7f ), cy - 6f + FxRandom.Float( -4f, 4f ) )
				.look( FxFrames.LEAF_D, 6f ).paint( 0x6FBF4A )
				.vel( FxRandom.Float( -8f, 8f ), FxRandom.Float( 10f, 22f ) )
				.drag( 0.6f ).life( FxRandom.Float( 0.7f, 1.2f ) )
				.alpha( 0.9f, 0.05f, 0.4f );
	}
}
