package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;

/**
 * 附魔的视觉：飞行拖尾、命中爆点、附魔触发时的元素效果。
 *
 * 本分支有 40+ 种附魔，按类名归纳到 9 个属性族，每族有自己的颜色与粒子形状：
 * 火（火星）/ 冰（霜晶）/ 雷（电弧）/ 毒（绿点）/ 暗（紫星）/ 血（红点）/
 * 光（金点）/ 水（青点）/ 物理（白刃）。
 */
public final class EnchantFx {

	private EnchantFx(){}

	/** 属性族 */
	public enum Family { FIRE, ICE, LIGHTNING, POISON, DARK, BLOOD, LIGHT, WATER, PHYS }

	public static int trailSpawned;
	public static int bursts;
	public static int procs;

	/** 该物品（或它的投掷来源）的附魔；无附魔 null */
	public static Weapon.Enchantment enchantOf( Item item ){
		if (item instanceof SpiritBow.SpiritArrow && Dungeon.hero != null
				&& Dungeon.hero.belongings.weapon() instanceof Weapon){
			return ((Weapon) Dungeon.hero.belongings.weapon()).enchantment;
		}
		if (item instanceof Weapon) return ((Weapon) item).enchantment;
		return null;
	}

	public static int color( Item item ){
		Weapon.Enchantment e = enchantOf( item );
		return e == null ? 0 : colorOf( e );
	}

	public static int colorOf( Weapon.Enchantment e ){
		return color( familyOf( e ) );
	}

	/** 族 → 颜色 */
	public static int color( Family f ){
		switch (f){
			case FIRE:      return 0xFF8A30;
			case ICE:       return 0x9FE4FF;
			case LIGHTNING: return 0xFFF07A;
			case POISON:    return 0x9CE87A;
			case DARK:      return 0xB48AFF;
			case BLOOD:     return 0xFF5A6A;
			case LIGHT:     return 0xFFD86A;
			case WATER:     return 0x7AD6D6;
			case PHYS:
			default:        return 0xE8E8F0;
		}
	}

	/** 附魔 → 属性族（按类名关键词） */
	public static Family familyOf( Weapon.Enchantment e ){
		String n = e.getClass().getSimpleName().toLowerCase();
		if (n.contains( "blaz" ) || n.contains( "burn" ) || n.contains( "fire" ) || n.contains( "meteor" )
				|| n.contains( "ember" ) || n.contains( "flam" )) return Family.FIRE;
		if (n.contains( "chill" ) || n.contains( "frost" ) || n.contains( "ice" ) || n.contains( "rime" )) return Family.ICE;
		if (n.contains( "shock" ) || n.contains( "storm" ) || n.contains( "rain" ) || n.contains( "bolt" )) return Family.LIGHTNING;
		if (n.contains( "venom" ) || n.contains( "poison" ) || n.contains( "bloom" ) || n.contains( "verd" )
				|| n.contains( "lamprey" )) return Family.POISON;
		if (n.contains( "grim" ) || n.contains( "void" ) || n.contains( "eclipse" ) || n.contains( "distort" )
				|| n.contains( "corrupt" ) || n.contains( "jealous" )) return Family.DARK;
		if (n.contains( "vamp" ) || n.contains( "soul" ) || n.contains( "reap" ) || n.contains( "blood" )) return Family.BLOOD;
		if (n.contains( "luck" ) || n.contains( "midas" ) || n.contains( "lamp" ) || n.contains( "heart" )
				|| n.contains( "harmony" ) || n.contains( "refin" )) return Family.LIGHT;
		if (n.contains( "flow" ) || n.contains( "pulse" ) || n.contains( "reson" ) || n.contains( "wet" )
				|| n.contains( "crystal" )) return Family.WATER;
		return Family.PHYS;
	}

	/** 拖尾用的帧（每族换形状） */
	private static int[] trailFrame( Family f ){
		switch (f){
			case FIRE:      return FxRandom.chance( 0.6f ) ? FxFrames.SPARK_3 : FxFrames.MOTE_1;
			case ICE:       return FxFrames.STAR4_5;
			case LIGHTNING: return FxFrames.SPARK_2;
			case POISON:    return FxFrames.MOTE_2;
			case DARK:      return FxFrames.STAR4_7;
			case LIGHT:     return FxFrames.STAR4_5;
			default:        return FxFrames.MOTE_1;
		}
	}

	/** 爆点用的帧 */
	private static int[] burstFrame( Family f ){
		switch (f){
			case FIRE:      return FxFrames.SPARK_3;
			case ICE:       return FxFrames.STAR4_5;
			case LIGHTNING: return FxFrames.SPARK_2;
			case POISON:    return FxFrames.MOTE_2;
			case DARK:      return FxFrames.STAR4_7;
			case LIGHT:     return FxFrames.STAR4_5;
			default:        return FxFrames.SPARK_3;
		}
	}

	/** 飞行中的一帧：按距离/时间补拖尾粒子 */
	public static void trail( float x, float y, int color, float vx, float vy ){
		if (color == 0 || !FxBudget.keep( FxBudget.P3, x, y )) return;
		FxEmitter air = Fx.air();
		if (air == null) return;
		((FxParticle) air.recycle( FxParticle.class ))
				.reset( x + FxRandom.Float( -2f, 2f ), y + FxRandom.Float( -2f, 2f ) )
				.look( FxRandom.chance( 0.5f ) ? FxFrames.MOTE_1 : FxFrames.MOTE_2 )
				.paint( color )
				.vel( -vx * 0.08f + FxRandom.Float( -8f, 8f ), -vy * 0.08f + FxRandom.Float( -8f, 8f ) )
				.life( FxRandom.Float( 0.22f, 0.42f ) )
				.alpha( 0.85f, 0f, 0.15f )
				.snap().priority( FxBudget.P3 );
		trailSpawned++;
	}

	public static void trail( float x, float y, Weapon.Enchantment e, float vx, float vy ){
		if (e == null) return;
		Family f = familyOf( e );
		if (!FxBudget.keep( FxBudget.P3, x, y )) return;
		FxEmitter air = Fx.air();
		if (air == null) return;
		((FxParticle) air.recycle( FxParticle.class ))
				.reset( x + FxRandom.Float( -2f, 2f ), y + FxRandom.Float( -2f, 2f ) )
				.look( trailFrame( f ) )
				.paint( color( f ) )
				.vel( -vx * 0.08f + FxRandom.Float( -8f, 8f ), -vy * 0.08f + FxRandom.Float( -8f, 8f ) )
				.life( FxRandom.Float( 0.22f, 0.42f ) )
				.alpha( 0.85f, 0f, 0.15f )
				.snap().priority( FxBudget.P3 );
		trailSpawned++;
	}

	/** 命中：一圈小爆点 */
	public static void burst( float x, float y, int color ){
		burst( x, y, Family.PHYS, color );
	}

	public static void burst( float x, float y, Family f, int color ){
		FxEmitter air = Fx.air();
		if (air == null) return;
		int n = f == Family.FIRE || f == Family.LIGHTNING ? 7 : 5;
		for (int i = 0; i < n; i++){
			if (!FxBudget.keep( FxBudget.P2, x, y )) continue;
			float a = FxRandom.angle();
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( x, y )
					.look( burstFrame( f ) )
					.paint( color )
					.polar( a, FxRandom.Float( 20f, 55f ) )
					.gravity( 90f )
					.life( FxRandom.Float( 0.25f, 0.45f ) )
					.alpha( 1f, 0f, 0.2f )
					.priority( FxBudget.P2 );
		}
		bursts++;
	}

	/** 附魔触发（命中时）：在目标身上放元素效果 */
	public static void proc( Char defender, Weapon.Enchantment e ){
		if (defender == null || e == null) return;
		Family f = familyOf( e );
		float Size = DungeonTilemap.SIZE;
		float x = defender.sprite != null ? defender.sprite.x + defender.sprite.width() / 2f
				: (defender.pos % Dungeon.level.width()) * Size + Size / 2f;
		float y = defender.sprite != null ? defender.sprite.y + defender.sprite.height() / 2f
				: (defender.pos / Dungeon.level.width()) * Size + Size / 2f;
		burst( x, y, f, color( f ) );
		// 各族再加一点特征粒子
		FxEmitter air = Fx.air();
		if (air != null){
			int extra = f == Family.FIRE ? 4 : f == Family.ICE ? 3 : 2;
			for (int i = 0; i < extra; i++){
				if (!FxBudget.keep( FxBudget.P3, x, y )) continue;
				((FxParticle) air.recycle( FxParticle.class ))
						.reset( x + FxRandom.Float( -6f, 6f ), y + FxRandom.Float( -6f, 6f ) )
						.look( f == Family.FIRE ? FxFrames.MOTE_1 : FxFrames.STAR4_5 )
						.paint( color( f ) )
						.vel( FxRandom.Float( -10f, 10f ), f == Family.FIRE ? -22f : FxRandom.Float( -6f, 6f ) )
						.life( FxRandom.Float( 0.35f, 0.7f ) )
						.alpha( 0.9f, 0f, 0.2f )
						.priority( FxBudget.P3 );
			}
		}
		procs++;
	}
}
