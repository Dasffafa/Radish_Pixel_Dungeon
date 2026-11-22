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

	/** 把颜色按比例提亮（往白里混） */
	static int light( int color, float k ){
		int r=(color>>16)&0xFF, g=(color>>8)&0xFF, b=color&0xFF;
		r=(int)(r+(255-r)*k); g=(int)(g+(255-g)*k); b=(int)(b+(255-b)*k);
		return 0xFF000000 | (r<<16) | (g<<8) | b;
	}

	/** 把颜色压暗 */
	static int dark( int color, float k ){
		int r=(int)(((color>>16)&0xFF)*(1f-k)), g=(int)(((color>>8)&0xFF)*(1f-k)), b=(int)((color&0xFF)*(1f-k));
		return 0xFF000000 | (r<<16) | (g<<8) | b;
	}

	// ---------------------------------------------------------------- 火
	static final int FIRE_AFTERGLOW = 0xFF8418;

	static void fire( float cx, float cy, float feet, int tint, EnchantFx.Ctx ctx ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		// 上窜的火舌（小/中）：弱 1 条、中 2 条、强 3 条
		int tongues = ctx.tier == 0 ? 1 : (ctx.tier == 1 ? 2 : 3);
		for (int i = 0; i < tongues; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx + FxRandom.Float( -3f, 3f ), cy + FxRandom.Float( -2f, 4f ) )
					.look( i == 0 ? FxFrames.TONGUE_S : FxFrames.TONGUE_M, 12f )
					.paint( tint )
					.vel( 0, -12f - i * 4f )
					.life( 0.35f )
					.alpha( ctx.a( 1f ), 0, 0.15f )
					.priority( FxBudget.P2 );
		}

		// 火星
		int embers = ctx.tier == 0 ? 2 : (ctx.tier == 1 ? 4 : 6);
		for (int i = 0; i < embers; i++){
			if (!FxBudget.keep( FxBudget.P3, cx, cy )) break;
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -4f, 4f ) )
					.look( FxRandom.chance( 0.6f ) ? FxFrames.EMBER_1 : FxFrames.EMBER_2 )
					.paint( light( tint, 0.35f ) )
					.polar( FxRandom.angle(), 26f )
					.drag( 3f )
					.gravity( -10f )
					.life( FxRandom.Float( 0.45f, 0.75f ) )
					.alpha( ctx.a( 0.9f ), 0, 0.2f )
					.priority( FxBudget.P3 );
		}

		// 地上的焦痕（弱/回响不留）
		if (ctx.tier > 0 && !ctx.echo) FxDecal.at( cx, feet + 1f, FxFrames.SCORCH_M, FxRecipes.SCORCH_COLOR, 0.7f,
				FxRecipes.SCORCH_HOLD, FxRecipes.SCORCH_FADE, false );
	}

	// ---------------------------------------------------------------- 冰
	static final float RIME_HOLD = 2f, RIME_FADE = 1f;
	static final int RIME_AFTERGLOW = 0xBCE0FF;

	static void ice( float cx, float cy, float feet, int tint, EnchantFx.Ctx ctx ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		// 白闪
		if (FxBudget.keep( FxBudget.P0, cx, cy )){
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx, cy ).look( FxFrames.PLUS_5 ).paint( 0xFFFFFF )
					.life( 0.08f ).alpha( ctx.a( 1f ), 0, 0.03f ).snap().priority( FxBudget.P0 );
		}

		// 霜雾
		for (int i = 0; i < 1 + ctx.tier; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( 0f, 4f ) )
					.look( FxFrames.PUFF_S, 0 )
					.paint( light( tint, 0.5f ) ).smooth()
					.vel( FxRandom.Float( -3f, 3f ), -FxRandom.Float( 5f, 9f ) )
					.drag( 0.8f ).life( 0.9f ).alpha( ctx.a( 0.55f ), 0.1f, 0.5f )
					.priority( FxBudget.P2 );
		}

		// 冰碴（弹跳一次）
		int shards = ctx.tier == 0 ? 2 : (ctx.tier == 1 ? 4 : 6);
		for (int i = 0; i < shards; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			boolean glass = i == shards - 1;
			float a = (float)(-Math.PI / 2) + FxRandom.Float( -1.4f, 1.4f );
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx, cy )
					.look( glass ? FxFrames.SHARD_GLASS : FxFrames.SHARD_ICE, 0 )
					.paint( glass ? light( tint, 0.6f ) : tint )
					.polar( a, FxRandom.Float( 30f, 60f ) )
					.gravity( 160f ).spin( 0.1f )
					.ground( feet + FxRandom.Float( -1f, 2f ), FxParticle.BOUNCE, 0.3f )
					.life( FxRandom.Float( 0.45f, 0.65f ) )
					.alpha( ctx.a( 1f ), 0, 0.2f )
					.priority( FxBudget.P2 );
		}

		// 地面霜迹（弱/回响不留）
		if (ctx.tier > 0 && !ctx.echo) FxDecal.at( cx, feet + 1f, FxFrames.RIME[Math.min( FxFrames.RIME.length - 1, 1 )],
				dark( tint, 0.35f ), 0.65f, RIME_HOLD, RIME_FADE, false );
	}

	// ---------------------------------------------------------------- 雷
	static final int ETCH = 0x2A2A40;

	static void storm( float cx, float cy, float feet, int tint, EnchantFx.Ctx ctx ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		// 电弧：弱 2 条、中 3 条、强 4 条
		for (int i = 0; i < 2 + ctx.tier; i++){
			if (!FxBudget.keep( i == 0 ? FxBudget.P0 : FxBudget.P1, cx, cy )) break;
			float a = FxRandom.angle();
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx, cy )
					.look( FxFrames.SPARK_2 )
					.orient()
					.slow( FxFrames.MOTE_1, 15f )
					.paint( tint )
					.polar( a, FxRandom.Float( 30f, 70f ) )
					.gravity( 120f ).drag( 1.2f )
					.ground( feet + FxRandom.Float( -1f, 3f ), FxParticle.BOUNCE, 0.35f )
					.life( FxRandom.Float( 0.25f, 0.45f ) )
					.alpha( ctx.a( 1f ), 0, 0.1f )
					.priority( FxBudget.P2 );
		}

		// 中心闪光
		if (FxBudget.keep( FxBudget.P0, cx, cy )){
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx, cy ).look( FxFrames.SPARK_3 ).paint( light( tint, 0.5f ) )
					.life( 0.07f ).alpha( ctx.a( 1f ), 0, 0.03f ).snap().priority( FxBudget.P0 );
		}

		// 地面焦痕（弱/回响不留）
		if (ctx.tier > 0 && !ctx.echo) FxDecal.at( cx, feet + 1f, FxFrames.ETCH, ETCH, 0.6f, 3f, 0.5f, false );
	}

	// ---------------------------------------------------------------- 毒
	static void venom( float cx, float cy, float feet, int tint, EnchantFx.Ctx ctx ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		for (int i = 0; i < 2 + ctx.tier; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -4f, 2f ) )
					.look( FxFrames.DROP_3 )
					.paint( tint )
					.orient()
					.polar( (float)(Math.PI / 2) + FxRandom.Float( -1f, 1f ), FxRandom.Float( 20f, 45f ) )
					.gravity( 240f )
					.alpha( ctx.a( 0.95f ), 0, 0.2f )
					.priority( FxBudget.P2 );
		}

		// 绿雾
		for (int i = 0; i < 2; i++){
			if (!FxBudget.keep( FxBudget.P3, cx, cy )) break;
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -2f, 4f ) )
					.look( FxFrames.PUFF_S, 0 )
					.paint( dark( tint, 0.35f ) ).smooth()
					.vel( FxRandom.Float( -2f, 2f ), FxRandom.Float( -6f, -2f ) )
					.life( 1f ).alpha( ctx.a( 0.45f ), 0.1f, 0.5f )
					.priority( FxBudget.P3 );
		}

		if (ctx.tier > 0 && !ctx.echo) FxDecal.at( cx, feet + 1f, FxFrames.ETCH, 0x2A3A1E, 0.6f, 3f, 0.8f, false );
	}

	// ---------------------------------------------------------------- 暗
	static final int DARK = 0x1A0C2C;

	static void dark( float cx, float cy, float feet, int tint, EnchantFx.Ctx ctx ){
		FxEmitter air = Fx.air();
		if (air == null) return;

		for (int i = 0; i < 4; i++){
			if (!FxBudget.keep( FxBudget.P2, cx, cy )) break;
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx + FxRandom.Float( -5f, 5f ), cy + FxRandom.Float( -4f, 4f ) )
					.look( FxFrames.INK_WISP, 3f )
					.paint( tint )
					.smooth()
					.vel( FxRandom.Float( -2f, 2f ), -FxRandom.Float( 6f, 10f ) )
					.life( FxRandom.Float( 0.6f, 1.1f ) )
					.alpha( ctx.a( 0.75f ), 0.1f, 0.5f )
					.priority( FxBudget.P2 );
		}

		// 中间一团暗核
		if (FxBudget.keep( FxBudget.P1, cx, cy )){
			EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f )
					.reset( cx, cy ).look( FxFrames.INK_CORE ).paint( dark( tint, 0.45f ) )
					.life( 1.2f ).alpha( ctx.a( 0.7f ), 0.15f, 0.6f ).snap().priority( FxBudget.P1 );
		}
	}

	// ---------------------------------------------------------------- 击杀特效（各族）

	/** 击杀：火（大火焰 + 火星 + 焦痕）/ 冰（全身碎裂）/ 雷（连续电弧 + 烟）/ 毒 / 暗 */
	static void finisher( EnchantFx.Family f, float cx, float cy, float feet, int tint, EnchantFx.Ctx ctx ){
		FxEmitter air = Fx.air();
		if (air == null) return;
		switch (f){
			case FIRE: {
				FxParticle p = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 2.4f );
				p.reset( cx, cy + 2f ).look( FxFrames.TONGUE_L, 10f ).paint( tint )
						.vel( 0, -14f ).life( 0.5f ).alpha( ctx.a( 1f ), 0, 0.2f ).priority( FxBudget.P0 );
				for (int i = 0; i < 7; i++){
					FxParticle e = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.6f );
					e.reset( cx + FxRandom.Float( -5f, 5f ), cy + FxRandom.Float( -4f, 4f ) )
							.look( FxFrames.EMBER_1 ).paint( EnchantFx.light( tint, 0.3f ) )
							.polar( FxRandom.angle(), 30f ).drag( 2.4f ).gravity( -14f )
							.life( FxRandom.Float( 0.7f, 1.1f ) ).alpha( ctx.a( 0.95f ), 0, 0.2f )
							.priority( FxBudget.P2 );
				}
				FxDecal.at( cx, feet + 1f, FxFrames.SCORCH_M, FxRecipes.SCORCH_COLOR, 0.8f,
						FxRecipes.SCORCH_HOLD, FxRecipes.SCORCH_FADE, false );
				break;
			}
			case ICE: {
				for (int i = 0; i < 8; i++){
					FxParticle s = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.7f );
					boolean glass = i >= 6;
					s.reset( cx + FxRandom.Float( -3f, 3f ), cy + FxRandom.Float( -3f, 3f ) )
							.look( glass ? FxFrames.SHARD_GLASS : FxFrames.SHARD_ICE, 0 )
							.paint( glass ? EnchantFx.light( tint, 0.6f ) : tint )
							.polar( FxRandom.angle(), FxRandom.Float( 40f, 80f ) ).gravity( 170f ).spin( 0.08f )
							.ground( feet + FxRandom.Float( -1f, 2f ), FxParticle.BOUNCE, 0.35f )
							.life( FxRandom.Float( 0.6f, 0.95f ) ).alpha( ctx.a( 1f ), 0, 0.25f )
							.priority( FxBudget.P2 );
				}
				FxDecal.at( cx, feet + 1f, FxFrames.RIME[FxFrames.RIME.length - 1], EnchantFx.dark( tint, 0.35f ),
						0.7f, RIME_HOLD, RIME_FADE, false );
				break;
			}
			case LIGHTNING: {
				for (int i = 0; i < 6; i++){
					FxParticle s = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f );
					s.reset( cx + FxRandom.Float( -4f, 4f ), cy + FxRandom.Float( -4f, 4f ) )
							.look( FxFrames.SPARK_2 ).orient().slow( FxFrames.MOTE_1, 15f ).paint( tint )
							.polar( FxRandom.angle(), FxRandom.Float( 40f, 80f ) )
							.gravity( 130f ).drag( 1.1f )
							.ground( feet + FxRandom.Float( -1f, 3f ), FxParticle.BOUNCE, 0.35f )
							.life( FxRandom.Float( 0.35f, 0.6f ) ).alpha( ctx.a( 1f ), 0, 0.15f )
							.priority( FxBudget.P2 );
				}
				FxParticle puff = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 2f );
				puff.reset( cx, cy - 2f ).look( FxFrames.PUFF_S, 0 ).paint( 0x9AA0B0 ).smooth()
						.vel( 0, -10f ).life( 0.8f ).alpha( ctx.a( 0.5f ), 0.05f, 0.4f )
						.priority( FxBudget.P2 );
				FxDecal.at( cx, feet + 1f, FxFrames.ETCH, ETCH, 0.7f, 3f, 0.6f, false );
				break;
			}
			case POISON: {
				for (int i = 0; i < 6; i++){
					FxParticle d = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.7f );
					d.reset( cx + FxRandom.Float( -5f, 5f ), cy + FxRandom.Float( -4f, 3f ) )
							.look( FxFrames.DROP_3 ).paint( tint ).orient()
							.polar( (float)(Math.PI / 2) + FxRandom.Float( -1.2f, 1.2f ), FxRandom.Float( 30f, 60f ) )
							.gravity( 240f ).life( FxRandom.Float( 0.5f, 0.9f ) )
							.alpha( ctx.a( 0.95f ), 0, 0.25f ).priority( FxBudget.P2 );
				}
				for (int i = 0; i < 3; i++){
					FxParticle g = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 2.2f );
					g.reset( cx + FxRandom.Float( -6f, 6f ), cy ).look( FxFrames.GAS_S, 0 ).paint( tint ).smooth()
							.vel( FxRandom.Float( -3f, 3f ), -6f ).life( 1.1f )
							.alpha( ctx.a( 0.4f ), 0.1f, 0.5f ).priority( FxBudget.P3 );
				}
				break;
			}
			case DARK: {
				FxParticle core = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 2.6f );
				core.reset( cx, cy ).look( FxFrames.INK_CORE ).paint( EnchantFx.dark( tint, 0.45f ) )
						.life( 1.3f ).alpha( ctx.a( 0.85f ), 0.1f, 0.6f ).snap().priority( FxBudget.P0 );
				FxParticle ring = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 2.2f );
				ring.reset( cx, cy ).look( new int[][]{ FxFrames.RING_23, FxFrames.RING_17, FxFrames.RING_13, FxFrames.RING_9 }, 8f )
						.paint( tint ).life( 0.7f ).alpha( ctx.a( 0.8f ), 0, 0.3f ).priority( FxBudget.P1 );
				for (int i = 0; i < 5; i++){
					FxParticle w = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.7f );
					w.reset( cx + FxRandom.Float( -5f, 5f ), cy + FxRandom.Float( -5f, 5f ) )
							.look( FxFrames.INK_WISP, 3f ).paint( tint ).smooth()
							.vel( FxRandom.Float( -3f, 3f ), -FxRandom.Float( 8f, 16f ) )
							.life( FxRandom.Float( 0.7f, 1.2f ) ).alpha( ctx.a( 0.8f ), 0.1f, 0.5f )
							.priority( FxBudget.P2 );
				}
				break;
			}
			default: {
				for (int i = 0; i < 5; i++){
					FxParticle s = EnchantFx.boost( (FxParticle) air.recycle( FxParticle.class ), 1.8f );
					s.reset( cx, cy ).look( FxFrames.STAR4_5 ).paint( tint )
							.polar( FxRandom.angle(), FxRandom.Float( 30f, 70f ) ).gravity( 100f )
							.life( FxRandom.Float( 0.4f, 0.8f ) ).alpha( ctx.a( 0.95f ), 0, 0.2f )
							.priority( FxBudget.P2 );
				}
				break;
			}
		}
	}
}
