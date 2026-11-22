package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.noosa.particles.Emitter;

/**
 * 附魔的视觉：飞行拖尾与命中爆点。
 *
 * 颜色按附魔类名归类（本分支有 40+ 种附魔，不做一对一美术，只按属性族取色）：
 * 火/雷/冰/毒/暗/血/光/自然等。没有附魔的武器完全不产生粒子。
 */
public final class EnchantFx {

	private EnchantFx(){}

	/** 拖尾粒子间隔（秒） */
	private static final float TRAIL_EVERY = 0.035f;

	public static int trailSpawned;
	public static int bursts;

	/** 该物品（或它的投掷来源）的附魔颜色；无附魔返回 0 */
	public static int color( Item item ){
		Weapon.Enchantment e = enchantOf( item );
		if (e == null) return 0;
		return colorOf( e );
	}

	public static Weapon.Enchantment enchantOf( Item item ){
		if (item instanceof SpiritBow.SpiritArrow && Dungeon.hero != null
				&& Dungeon.hero.belongings.weapon() instanceof Weapon){
			return ((Weapon) Dungeon.hero.belongings.weapon()).enchantment;
		}
		if (item instanceof Weapon) return ((Weapon) item).enchantment;
		return null;
	}

	/** 附魔 → 颜色（按类名关键词归类） */
	public static int colorOf( Weapon.Enchantment e ){
		String n = e.getClass().getSimpleName().toLowerCase();
		if (n.contains( "blaz" ) || n.contains( "burn" ) || n.contains( "fire" ) || n.contains( "meteor" )
				|| n.contains( "ember" ) || n.contains( "flam" )) return 0xFF8A30;
		if (n.contains( "chill" ) || n.contains( "frost" ) || n.contains( "ice" ) || n.contains( "rime" )) return 0x9FE4FF;
		if (n.contains( "shock" ) || n.contains( "light" ) || n.contains( "storm" ) || n.contains( "rain" )
				|| n.contains( "bolt" )) return 0xFFF07A;
		if (n.contains( "venom" ) || n.contains( "poison" ) || n.contains( "bloom" ) || n.contains( "verd" )) return 0x9CE87A;
		if (n.contains( "grim" ) || n.contains( "void" ) || n.contains( "eclipse" ) || n.contains( "distort" )
				|| n.contains( "corrupt" )) return 0xB48AFF;
		if (n.contains( "vamp" ) || n.contains( "lamprey" ) || n.contains( "soul" ) || n.contains( "reap" )) return 0xFF5A6A;
		if (n.contains( "luck" ) || n.contains( "midas" ) || n.contains( "lamp" ) || n.contains( "light" )
				|| n.contains( "harmony" )) return 0xFFD86A;
		if (n.contains( "flow" ) || n.contains( "pulse" ) || n.contains( "reson" ) || n.contains( "refin" )) return 0x7AD6D6;
		if (n.contains( "blade" ) || n.contains( "sharp" ) || n.contains( "clash" )) return 0xE8E8F0;
		return 0x9FD8FF;
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

	/** 命中：一圈小爆点 */
	public static void burst( float x, float y, int color ){
		if (color == 0) return;
		FxEmitter air = Fx.air();
		if (air == null) return;
		int n = 5;
		for (int i = 0; i < n; i++){
			if (!FxBudget.keep( FxBudget.P2, x, y )) continue;
			float a = FxRandom.angle();
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( x, y )
					.look( FxRandom.chance( 0.5f ) ? FxFrames.STAR4_5 : FxFrames.SPARK_3 )
					.paint( color )
					.polar( a, FxRandom.Float( 20f, 55f ) )
					.gravity( 90f )
					.life( FxRandom.Float( 0.25f, 0.45f ) )
					.alpha( 1f, 0f, 0.2f )
					.priority( FxBudget.P2 );
		}
		bursts++;
	}
}
