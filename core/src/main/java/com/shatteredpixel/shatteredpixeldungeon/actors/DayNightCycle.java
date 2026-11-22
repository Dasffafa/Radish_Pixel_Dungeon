/*
 * 摘自 Warped Pixel Dungeon（GPLv3，同源代码）的 DayNightCycle，
 * 去掉天气(ClimateManager)、季节(GameCalendar)、主世界与玩法影响，
 * 只保留"相位 → 色调/亮度"这一层，用于整场景明暗。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.Game;

/**
 * 昼夜循环（只做画面）：黎明 → 白天 → 黄昏 → 夜晚，按游戏时长轮转。
 * 相位时长可调；色调/亮度端点取自 Warped 原表。
 */
public final class DayNightCycle {

	private DayNightCycle(){}

	public enum Phase {
		DAWN, DAY, DUSK, NIGHT;

		public Phase next() {
			switch (this) {
				case DAWN:  return DAY;
				case DAY:   return DUSK;
				case DUSK:  return NIGHT;
				default:    return DAWN;
			}
		}
	}

	/** 一整轮昼夜的秒数 */
	public static float FULL_CYCLE = 480f;
	/** 黎明 / 黄昏 各占整轮的比例 */
	public static float DAWN_SHARE = 0.1f, DUSK_SHARE = 0.1f;
	/** 调试：强制某个相位（null = 按时间走） */
	public static Phase debugPhase = null;

	private static float phaseSeconds() { return FULL_CYCLE * DAWN_SHARE; }
	private static float duskSeconds()  { return FULL_CYCLE * DUSK_SHARE; }
	private static float daySeconds()   { return FULL_CYCLE * (1f - DAWN_SHARE - DUSK_SHARE) / 2f; }
	private static float nightSeconds() { return FULL_CYCLE * (1f - DAWN_SHARE - DUSK_SHARE) / 2f; }

	public static Phase phase() {
		if (debugPhase != null) return debugPhase;
		float t = Game.timeTotal % FULL_CYCLE;
		if (t < phaseSeconds()) return Phase.DAWN;
		t -= phaseSeconds();
		if (t < daySeconds()) return Phase.DAY;
		t -= daySeconds();
		if (t < duskSeconds()) return Phase.DUSK;
		return Phase.NIGHT;
	}

	/** 当前相位走了多远（0..1） */
	public static float phaseProgress() {
		if (debugPhase != null) return 0.5f;
		float t = Game.timeTotal % FULL_CYCLE;
		if (t < phaseSeconds()) return t / phaseSeconds();
		t -= phaseSeconds();
		if (t < daySeconds()) return t / daySeconds();
		t -= daySeconds();
		if (t < duskSeconds()) return t / duskSeconds();
		t -= duskSeconds();
		return Math.min(1f, t / nightSeconds());
	}

	public static boolean isNight() {
		return phase() == Phase.NIGHT;
	}

	// 色调端点：{A, R, G, B}
	private static final float[] NIGHT_TINT = {0.133f, 0.067f, 0.133f, 0.267f};
	private static final float[] DAWN_TINT  = {0.067f, 1.000f, 0.533f, 0.267f};
	private static final float[] DAY_TINT   = {0f, 0f, 0f, 0f};
	private static final float[] DUSK_TINT  = {0.067f, 0.533f, 0.267f, 0.400f};

	private static final float NIGHT_BRIGHT = 0.65f, DAWN_BRIGHT = 1.05f,
			DAY_BRIGHT = 1.00f, DUSK_BRIGHT = 0.85f;

	private static float lerp( float a, float b, float t ){
		return a + (b - a) * t;
	}

	/** 当前色调 {A, R, G, B}，过渡阶段线性插值 */
	public static float[] phaseTintSmooth() {
		Phase p = phase();
		float t = phaseProgress();
		float[] out = new float[4];
		switch (p) {
			case DAWN:
				if (t < 0.5f) {
					float s = t * 2f;
					for (int i = 0; i < 4; i++) out[i] = lerp(NIGHT_TINT[i], DAWN_TINT[i], s);
				} else {
					float s = (t - 0.5f) * 2f;
					for (int i = 0; i < 4; i++) out[i] = lerp(DAWN_TINT[i], DAY_TINT[i], s);
				}
				break;
			case DUSK:
				if (t < 0.5f) {
					float s = t * 2f;
					for (int i = 0; i < 4; i++) out[i] = lerp(DAY_TINT[i], DUSK_TINT[i], s);
				} else {
					float s = (t - 0.5f) * 2f;
					for (int i = 0; i < 4; i++) out[i] = lerp(DUSK_TINT[i], NIGHT_TINT[i], s);
				}
				break;
			case NIGHT:
				System.arraycopy(NIGHT_TINT, 0, out, 0, 4);
				break;
			case DAY:
			default:
				System.arraycopy(DAY_TINT, 0, out, 0, 4);
				break;
		}
		return out;
	}

	/** 亮度倍数（低于 1 时叠加一层暗色） */
	public static float brightnessMult() {
		if (debugPhase != null) return brightnessOf(debugPhase);
		return brightnessOf(phase());
	}

	private static float brightnessOf( Phase p ){
		switch (p) {
			case DAWN:  return DAWN_BRIGHT;
			case DAY:   return DAY_BRIGHT;
			case DUSK:  return DUSK_BRIGHT;
			case NIGHT:
			default:    return NIGHT_BRIGHT;
		}
	}

	/** 调试用：当前是第几层（暂无用途，保留给水面/生物使用） */
	public static int depthHint(){ return Dungeon.depth; }
}
