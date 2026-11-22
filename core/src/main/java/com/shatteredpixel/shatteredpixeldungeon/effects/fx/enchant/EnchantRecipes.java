package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Element;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxDecal;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFactories;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRecipes;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Ramp;

/**
 * 附魔命中的具体粒子配方——帧、颜色、速度曲线取自 Warped 的
 * EmberFx / RimeFx / StormFx / VenomFx / DreadFx（同一张 fx_sheet）。
 * 去掉了它的 tier/echo/curse/tally 框架，只保留"一次命中该放哪些粒子"。
 */
final class EnchantRecipes {

	private EnchantRecipes(){}

	// ---------------------------------------------------------------- 火
	static final int FIRE_AFTERGLOW = 0xFF8418;

	static void fire( float cx, float cy, float feet ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		// 上窜的火舌（小/中）
		for (int i = 0; i < 2; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx + FxRandom.Float( -3f, 3f ), cy + FxRandom.Float( -2f, 4f ) )
					.look( i == 0 ? FxFrames.TONGUE_S : FxFrames.TONGUE_M, 12f )
					.ramp( FxFactories.EMBER, true )
					.vel( 0, -12f - i * 4f )
					.life( 0.35f )
					.alpha( 1f, 0, 0.15f )
					.priority( FxBudget.P2 );
		}

		// 火星
		int embers = 4;
		for (int i = 0; i < embers; i++){
			if (!FxBudget.keep( FxBudget.P3, cx, cy )) break;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -4f, 4f ) )
					.look( FxRandom.chance( 0.6f ) ? FxFrames.EMBER_1 : FxFrames.EMBER_2 )
					.ramp( FxFactories.EMBER, true )
					.polar( FxRandom.angle(), 26f )
					.drag( 3f )
					.gravity( -10f )
					.life( FxRandom.Float( 0.45f, 0.75f ) )
					.alpha( 0.9f, 0, 0.2f )
					.priority( FxBudget.P3 );
		}

		// 地上的焦痕
		FxDecal.at( cx, feet + 1f, FxFrames.SCORCH_M, FxRecipes.SCORCH_COLOR, 0.7f,
				FxRecipes.SCORCH_HOLD, FxRecipes.SCORCH_FADE, false );
	}

	// ---------------------------------------------------------------- 冰
	static final float RIME_HOLD = 2f, RIME_FADE = 1f;
	static final int RIME_AFTERGLOW = 0xBCE0FF;

	static void ice( float cx, float cy, float feet ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		// 白闪
		if (FxBudget.keep( FxBudget.P0, cx, cy )){
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx, cy ).look( FxFrames.PLUS_5 ).paint( 0xFFFFFF )
					.life( 0.08f ).alpha( 1f, 0, 0.03f ).snap().priority( FxBudget.P0 );
		}

		// 霜雾
		for (int i = 0; i < 2; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( 0f, 4f ) )
					.look( FxFrames.PUFF_S, 0 )
					.ramp( Element.FROST.smoke, false ).smooth()
					.vel( FxRandom.Float( -3f, 3f ), -FxRandom.Float( 5f, 9f ) )
					.drag( 0.8f ).life( 0.9f ).alpha( 0.55f, 0.1f, 0.5f )
					.priority( FxBudget.P2 );
		}

		// 冰碴（弹跳一次）
		int shards = 4;
		for (int i = 0; i < shards; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			boolean glass = i == shards - 1;
			float a = (float)(-Math.PI / 2) + FxRandom.Float( -1.4f, 1.4f );
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx, cy )
					.look( glass ? FxFrames.SHARD_GLASS : FxFrames.SHARD_ICE, 0 )
					.paint( glass ? 0xD8F4FF : 0xFFFFFF )
					.polar( a, FxRandom.Float( 30f, 60f ) )
					.gravity( 160f ).spin( 0.1f )
					.ground( feet + FxRandom.Float( -1f, 2f ), FxParticle.BOUNCE, 0.3f )
					.life( FxRandom.Float( 0.45f, 0.65f ) )
					.alpha( 1f, 0, 0.2f )
					.priority( FxBudget.P2 );
		}

		// 地面霜迹
		FxDecal.at( cx, feet + 1f, FxFrames.RIME[Math.min( FxFrames.RIME.length - 1, 1 )],
				0xBCE0FF, 0.65f, RIME_HOLD, RIME_FADE, false );
	}

	// ---------------------------------------------------------------- 雷
	static final int ETCH = 0x2A2A40;

	static void storm( float cx, float cy, float feet ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		// 电弧：从中心向外的三条
		for (int i = 0; i < 3; i++){
			if (!FxBudget.keep( i == 0 ? FxBudget.P0 : FxBudget.P1, cx, cy )) break;
			float a = FxRandom.angle();
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx, cy )
					.look( FxFrames.SPARK_2 )
					.orient()
					.slow( FxFrames.MOTE_1, 15f )
					.ramp( Element.ELECTRIC.ember, true )
					.polar( a, FxRandom.Float( 30f, 70f ) )
					.gravity( 120f ).drag( 1.2f )
					.ground( feet + FxRandom.Float( -1f, 3f ), FxParticle.BOUNCE, 0.35f )
					.life( FxRandom.Float( 0.25f, 0.45f ) )
					.alpha( 1f, 0, 0.1f )
					.priority( FxBudget.P2 );
		}

		// 中心闪光
		if (FxBudget.keep( FxBudget.P0, cx, cy )){
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx, cy ).look( FxFrames.SPARK_3 ).paint( 0xFFF07A )
					.life( 0.07f ).alpha( 1f, 0, 0.03f ).snap().priority( FxBudget.P0 );
		}

		// 地面焦痕
		FxDecal.at( cx, feet + 1f, FxFrames.ETCH, ETCH, 0.6f, 3f, 0.5f, false );
	}

	// ---------------------------------------------------------------- 毒
	static void venom( float cx, float cy, float feet ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		for (int i = 0; i < 4; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -4f, 2f ) )
					.look( FxFrames.DROP_3 )
					.paint( 0x9CE87A )
					.orient()
					.polar( (float)(Math.PI / 2) + FxRandom.Float( -1f, 1f ), FxRandom.Float( 20f, 45f ) )
					.gravity( 240f )
					.alpha( 0.95f, 0, 0.2f )
					.priority( FxBudget.P2 );
		}

		// 绿雾
		for (int i = 0; i < 2; i++){
			if (!FxBudget.keep( FxBudget.P3, cx, cy )) break;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -2f, 4f ) )
					.look( FxFrames.PUFF_S, 0 )
					.ramp( Element.SMOKE.smoke, false ).smooth()
					.vel( FxRandom.Float( -2f, 2f ), FxRandom.Float( -6f, -2f ) )
					.life( 1f ).alpha( 0.45f, 0.1f, 0.5f )
					.priority( FxBudget.P3 );
		}

		FxDecal.at( cx, feet + 1f, FxFrames.ETCH, 0x2A3A1E, 0.6f, 3f, 0.8f, false );
	}

	// ---------------------------------------------------------------- 暗
	static final int DARK = 0x1A0C2C;

	static void dark( float cx, float cy, float feet ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		for (int i = 0; i < 4; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx + FxRandom.Float( -5f, 5f ), cy + FxRandom.Float( -4f, 4f ) )
					.look( FxFrames.INK_WISP, 3f )
					.paint( 0x6A4A9A )
					.smooth()
					.vel( FxRandom.Float( -2f, 2f ), -FxRandom.Float( 6f, 10f ) )
					.life( FxRandom.Float( 0.6f, 1.1f ) )
					.alpha( 0.75f, 0.1f, 0.5f )
					.priority( FxBudget.P2 );
		}

		// 中间一团暗核
		if (FxBudget.keep( FxBudget.P1, cx, cy )){
			((FxParticle) air.recycle( FxParticle.class ))
					.reset( cx, cy ).look( FxFrames.INK_CORE ).paint( DARK )
					.life( 1.2f ).alpha( 0.7f, 0.15f, 0.6f ).snap().priority( FxBudget.P1 );
		}
	}
}
