package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.NaturesPower;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;

/**
 * 弓的持续光效（参照 Warped 的 StanceOrbit / SpiritBow）：
 * 1) 装备弓时角色身周一圈带拖尾的光点
 * 2) 自然之力生效时身周飘绿叶
 *
 * 本分支的武器可能挂在 weapon / secondWep / thrownWeapon / abilityWeapon / attackingWeapon()，
 * 所以取弓时全部扫一遍。
 */
public final class BowAura {

	private BowAura(){}

	private static final int MOTES = 10;

	public static int spawned;

	private static float orbitAngle;
	private static Group orbit;
	private static Image[] motes;
	private static float leafIn;

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

		if (hero.buff( NaturesPower.naturesPowerTracker.class ) != null){
			if ((leafIn -= Game.elapsed) <= 0f){
				leafIn = 0.12f;
				spawnLeaf( cx, cy );
			}
		} else {
			leafIn = 0f;
		}
	}

	/** 判断一个物品是不是弓/弩类 */
	public static boolean isBowItem( Item it ){
		if (it == null) return false;
		String n = it.getClass().getSimpleName().toLowerCase();
		return n.contains( "bow" ) || n.contains( "arbalest" ) || n.contains( "sling" );
	}

	/** 取英雄身上的第一把弓（武器可能挂在多个槽位） */
	public static Item bowOf( Hero hero ){
		if (hero == null || hero.belongings == null) return null;
		Item[] slots = {
				hero.belongings.weapon,
				hero.belongings.secondWep,
				hero.belongings.thrownWeapon,
				hero.belongings.abilityWeapon,
		};
		for (Item it : slots) if (isBowItem( it )) return it;
		try {
			Item atk = hero.belongings.attackingWeapon();
			if (isBowItem( atk )) return atk;
		} catch (Exception ignored){}
		return null;
	}

	private static void updateOrbit( CharSprite s, float cx, float cy, int tint ){
		if (s.parent == null) return;

		// 换层/换场景后旧 group 已经脱离父节点，必须重建
		if (orbit != null && orbit.parent == null){
			orbit = null;
			motes = null;
		}
		if (orbit == null){
			orbit = new Group();
			s.parent.add( orbit );
			int[] f = FxFrames.MOTE_1;
			motes = new Image[MOTES * 3];
			for (int i = 0; i < motes.length; i++){
				motes[i] = new Image( Assets.Effects.FX_SHEET, f[0], f[1], f[2], f[3] );
				orbit.add( motes[i] );
			}
		}
		orbit.visible = true;

		float breath = 0.55f + 0.35f * (float)Math.sin( Game.timeTotal * 0.6f );

		for (int i = 0; i < MOTES; i++){
			for (int k = 0; k < 3; k++){
				Image m = motes[i * 3 + k];
				float a = orbitAngle + (float)(Math.PI * 2 * i / MOTES) - k * 0.18f;
				m.visible = true;
				m.x = cx + (float)Math.cos( a ) * 13f - m.width() * 0.75f;
				m.y = cy + (float)Math.sin( a ) * 6.5f - m.height() * 0.75f - 2f;
				m.hardlight( EnchantFx.light( tint, 0.3f + k * 0.15f ) );
				float depth = 0.5f + 0.5f * (float)Math.sin( a );
				m.am = Math.max( 0.12f, breath * depth * (k == 0 ? 1f : (k == 1 ? 0.5f : 0.28f)) );
				m.scale.set( k == 0 ? 1.6f : 1.1f );
			}
			spawned++;
		}
	}

	private static void hideOrbit(){
		if (orbit != null) orbit.visible = false;
	}

	private static void spawnLeaf( float cx, float cy ){
		FxEmitter air = Fx.air();
		if (air == null) return;
		FxParticle p = (FxParticle) air.recycle( FxParticle.class );
		if (p == null) return;
		p.reset( cx + FxRandom.Float( -7f, 7f ), cy - 6f + FxRandom.Float( -4f, 4f ) )
				.look( FxFrames.LEAF_D, 6f ).paint( 0x6FBF4A )
				.vel( FxRandom.Float( -8f, 8f ), FxRandom.Float( 10f, 22f ) )
				.drag( 0.6f ).life( FxRandom.Float( 0.7f, 1.2f ) )
				.alpha( 0.9f, 0.05f, 0.4f );
	}

	/** 测试用 */
	public static boolean orbitVisible(){
		if (orbit == null || !orbit.visible || orbit.parent == null) return false;
		for (Image m : motes) if (m != null && m.visible) return true;
		return false;
	}

	public static String debugState(){
		Hero hero = Dungeon.hero;
		Item bow = BowAura.bowOf( hero );
		String w = "?";
		if (hero != null && hero.belongings != null){
			w = "weapon=" + (hero.belongings.weapon == null ? "null" : hero.belongings.weapon.getClass().getSimpleName())
					+ " second=" + (hero.belongings.secondWep == null ? "null" : hero.belongings.secondWep.getClass().getSimpleName());
		}
		return "bow=" + (bow == null ? "null" : bow.getClass().getSimpleName())
				+ " | orbit=" + (orbit != null) + " parent=" + (orbit != null && orbit.parent != null)
				+ " vis=" + (orbit != null && orbit.visible) + " | " + w;
	}
}
