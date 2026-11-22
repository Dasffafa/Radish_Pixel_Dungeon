package com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant;

import com.shatteredpixel.shatteredpixeldungeon.effects.fx.Fx;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxBudget;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxDecal;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxRandom;

/**
 * 萝卜地牢特色附魔的专属特效（Warped 没有、由我们设计）：
 * 流光、结晶、脉冲、虚空裂隙、裂魂、陨星、骤雨、横扫。
 *
 * 用同一张 fx_sheet 的帧，靠"帧组合 + 运动方式 + 颜色"做出辨识度。
 */
final class SpecialFx {

	private SpecialFx(){}

	private static final int[][] RING_GROW_S = { FxFrames.RING_5, FxFrames.RING_9, FxFrames.RING_13 };
	private static final int[][] RING_GROW_M = { FxFrames.RING_9, FxFrames.RING_13, FxFrames.RING_17, FxFrames.RING_23 };

	private static FxParticle p( float x, float y, int prio ){
		FxEmitter air = Fx.air();
		if (air == null) return null;
		if (!FxBudget.keep( prio, x, y )) return null;
		return (FxParticle) air.recycle( FxParticle.class );
	}

	/** 按附魔类名播放专属特效；false = 没有专属，走族配方 */
	static boolean play( String simpleName, float x, float y, float feet, int tint, EnchantFx.Ctx ctx ){
		switch (simpleName.toLowerCase()){
			case "flowinglight": return flowingLight( x, y, tint, ctx );
			case "crystal":      return crystal( x, y, tint, ctx );
			case "pulse":        return pulse( x, y, tint, ctx );
			case "voidrift":     return voidRift( x, y, tint, ctx );
			case "soulrend":     return soulRend( x, y, tint, ctx );
			case "meteor":       return meteor( x, y, feet, tint, ctx );
			case "rainstorm":    return rainstorm( x, y, tint, ctx );
			case "sweeping":     return sweeping( x, y, tint, ctx );
			default:             return false;
		}
	}

	// ---------------------------------------------------------------- 流光：向上收束的光带 + 光环
	private static boolean flowingLight( float x, float y, int tint, EnchantFx.Ctx ctx ){
		FxParticle f = p( x, y, FxBudget.P1 );
		if (f != null) f.look( FxFrames.CORE_FLASH ).paint( EnchantFx.light( tint, 0.4f ) )
				.life( 0.25f ).alpha( ctx.a( 0.95f ), 0, 0.2f ).snap().priority( FxBudget.P1 );

		FxParticle r = p( x, y, FxBudget.P1 );
		if (r != null) r.look( RING_GROW_S, 22f ).paint( tint )
				.life( 0.35f ).alpha( ctx.a( 0.8f ), 0, 0.25f ).priority( FxBudget.P1 );

		for (int i = 0; i < 1 + ctx.tier; i++){
			FxParticle s = p( x + FxRandom.Float( -4f, 4f ), y + FxRandom.Float( -4f, 4f ), FxBudget.P3 );
			if (s == null) break;
			s.look( FxFrames.MOTE_1 ).paint( EnchantFx.light( tint, 0.6f ) )
					.vel( FxRandom.Float( -6f, 6f ), -FxRandom.Float( 14f, 24f ) )
					.life( FxRandom.Float( 0.4f, 0.7f ) )
					.alpha( ctx.a( 0.85f ), 0, 0.25f ).priority( FxBudget.P3 );
		}
		return true;
	}

	// ---------------------------------------------------------------- 结晶：方形冰晶向外崩开
	private static boolean crystal( float x, float y, int tint, EnchantFx.Ctx ctx ){
		FxParticle f = p( x, y, FxBudget.P0 );
		if (f != null) f.look( FxFrames.PLUS_5 ).paint( 0xFFFFFF )
				.life( 0.09f ).alpha( ctx.a( 1f ), 0, 0.03f ).snap().priority( FxBudget.P0 );

		int n = ctx.tier == 0 ? 3 : (ctx.tier == 1 ? 5 : 7);
		for (int i = 0; i < n; i++){
			FxParticle s = p( x, y, FxBudget.P2 );
			if (s == null) break;
			float a = (float)(2 * Math.PI * i / n) + FxRandom.Float( -0.2f, 0.2f );
			s.look( FxFrames.SQ_4 ).paint( i % 2 == 0 ? tint : EnchantFx.light( tint, 0.5f ) )
					.polar( a, FxRandom.Float( 26f, 46f ) ).spin( 0.2f )
					.life( FxRandom.Float( 0.35f, 0.6f ) )
					.alpha( ctx.a( 0.95f ), 0, 0.25f ).priority( FxBudget.P2 );
		}
		return true;
	}

	// ---------------------------------------------------------------- 脉冲：三层错开的扩散环
	private static boolean pulse( float x, float y, int tint, EnchantFx.Ctx ctx ){
		for (int i = 0; i < 1 + ctx.tier; i++){
			FxParticle r = p( x, y, FxBudget.P1 );
			if (r == null) break;
			r.look( i == 0 ? RING_GROW_S : RING_GROW_M, 18f + i * 4f ).paint( tint )
					.life( 0.35f + i * 0.06f ).alpha( ctx.a( 0.8f - i * 0.15f ), 0, 0.3f )
					.priority( FxBudget.P1 );
		}
		return true;
	}

	// ---------------------------------------------------------------- 虚空裂隙：向内收的暗环 + 墨雾
	private static boolean voidRift( float x, float y, int tint, EnchantFx.Ctx ctx ){
		FxParticle core = p( x, y, FxBudget.P1 );
		if (core != null) core.look( FxFrames.INK_CORE ).paint( EnchantFx.dark( tint, 0.5f ) )
				.life( 0.9f ).alpha( ctx.a( 0.8f ), 0.2f, 0.5f ).snap().priority( FxBudget.P1 );

		for (int i = 0; i < 2 + ctx.tier; i++){
			FxParticle w = p( x + FxRandom.Float( -5f, 5f ), y + FxRandom.Float( -5f, 5f ), FxBudget.P2 );
			if (w == null) break;
			float a = FxRandom.angle();
			w.look( FxFrames.INK_WISP, 3f ).paint( tint ).smooth()
					.polar( a, FxRandom.Float( 8f, 20f ) ).drag( 1.4f )
					.life( FxRandom.Float( 0.5f, 0.9f ) )
					.alpha( ctx.a( 0.7f ), 0.15f, 0.5f ).priority( FxBudget.P2 );
		}
		return true;
	}

	// ---------------------------------------------------------------- 裂魂：十字裂口 + 上升魂点
	private static boolean soulRend( float x, float y, int tint, EnchantFx.Ctx ctx ){
		FxParticle c = p( x, y, FxBudget.P0 );
		if (c != null) c.look( FxFrames.CROSS_5 ).paint( EnchantFx.light( tint, 0.45f ) )
				.life( 0.14f ).alpha( ctx.a( 1f ), 0, 0.06f ).snap().priority( FxBudget.P0 );

		for (int i = 0; i < 2 + ctx.tier; i++){
			FxParticle m = p( x + FxRandom.Float( -3f, 3f ), y + FxRandom.Float( -3f, 3f ), FxBudget.P2 );
			if (m == null) break;
			m.look( FxFrames.MOTE_2 ).paint( tint )
					.vel( FxRandom.Float( -4f, 4f ), -FxRandom.Float( 16f, 30f ) ).drag( 1.1f )
					.life( FxRandom.Float( 0.5f, 0.9f ) )
					.alpha( ctx.a( 0.85f ), 0, 0.3f ).priority( FxBudget.P2 );
		}
		return true;
	}

	// ---------------------------------------------------------------- 陨星：从天而降的火痕 + 落地溅射
	private static boolean meteor( float x, float y, float feet, int tint, EnchantFx.Ctx ctx ){
		FxParticle trail = p( x, y - 26f, FxBudget.P1 );
		if (trail != null) trail.look( FxFrames.STREAK_FLAME, 20f ).paint( tint )
				.vel( 0, 150f ).life( 0.18f ).alpha( ctx.a( 0.95f ), 0, 0.05f ).priority( FxBudget.P1 );

		FxParticle core = p( x, y, FxBudget.P1 );
		if (core != null) core.look( FxFrames.CORE_FLASH ).paint( EnchantFx.light( tint, 0.5f ) )
				.life( 0.22f ).alpha( ctx.a( 1f ), 0, 0.1f ).snap().priority( FxBudget.P1 );

		int sp = ctx.tier == 0 ? 3 : (ctx.tier == 1 ? 5 : 8);
		for (int i = 0; i < sp; i++){
			FxParticle s = p( x, y, FxBudget.P2 );
			if (s == null) break;
			s.look( FxFrames.SPARK_3 ).paint( tint )
					.polar( (float)(-Math.PI / 2) + FxRandom.Float( -1.1f, 1.1f ), FxRandom.Float( 30f, 70f ) )
					.gravity( 180f ).drag( 0.6f )
					.life( FxRandom.Float( 0.3f, 0.55f ) )
					.alpha( ctx.a( 0.95f ), 0, 0.2f ).priority( FxBudget.P2 );
		}
		if (ctx.tier > 0 && !ctx.echo){
			FxDecal.at( x, feet + 1f, FxFrames.SCORCH_M, 0xFF7A20, 0.7f, 2.5f, 1f, false );
		}
		return true;
	}

	// ---------------------------------------------------------------- 骤雨：多个雨滴斜落
	private static boolean rainstorm( float x, float y, int tint, EnchantFx.Ctx ctx ){
		int n = ctx.tier == 0 ? 3 : (ctx.tier == 1 ? 5 : 8);
		for (int i = 0; i < n; i++){
			FxParticle d = p( x + FxRandom.Float( -10f, 10f ), y - FxRandom.Float( 14f, 26f ), FxBudget.P2 );
			if (d == null) break;
			d.look( FxFrames.DROP_3 ).paint( EnchantFx.light( tint, 0.3f ) )
					.vel( FxRandom.Float( -4f, 4f ), FxRandom.Float( 70f, 110f ) )
					.gravity( 120f ).life( FxRandom.Float( 0.18f, 0.3f ) )
					.alpha( ctx.a( 0.9f ), 0, 0.1f ).priority( FxBudget.P2 );
		}
		FxParticle r = p( x, y, FxBudget.P3 );
		if (r != null) r.look( RING_GROW_S, 20f ).paint( tint )
				.life( 0.3f ).alpha( ctx.a( 0.6f ), 0, 0.3f ).priority( FxBudget.P3 );
		return true;
	}

	// ---------------------------------------------------------------- 横扫：横向斩击弧
	private static boolean sweeping( float x, float y, int tint, EnchantFx.Ctx ctx ){
		FxParticle arc = p( x, y, FxBudget.P1 );
		if (arc != null) {
			arc.look( RING_GROW_M, 26f ).paint( EnchantFx.light( tint, 0.35f ) )
					.life( 0.26f ).alpha( ctx.a( 0.85f ), 0, 0.3f ).priority( FxBudget.P1 );
		}
		FxParticle c = p( x, y, FxBudget.P2 );
		if (c != null) c.look( FxFrames.CROSS_5 ).paint( tint )
				.life( 0.12f ).alpha( ctx.a( 0.9f ), 0, 0.05f ).snap().priority( FxBudget.P2 );
		return true;
	}
}
