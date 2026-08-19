package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.watabou.noosa.Image;

/**
 * 带可调偏移的特效基类。继承 {@link Image} 并实现 {@link OffsetTweakable}，
 * 让特效能在 test mode 下被 {@link EffectOffsetTweaker} 实时微调偏移。
 *
 * <p>用法：子类在自身定位（锚点/朝向翻转计算）上叠加
 * {@link #tweakOffsetX()}/{@link #tweakOffsetY()}，并在每帧 {@code update()} 里调用
 * {@link #trackForTweak()} 登记自己。微调工具打印出的偏移即你要硬编码进子类的常量。</p>
 */
public abstract class TweakableEffect extends Image implements OffsetTweakable {

	protected float tweakX;
	protected float tweakY;

	@Override public void setTweakOffset(float x, float y) { tweakX = x; tweakY = y; }
	@Override public float tweakOffsetX() { return tweakX; }
	@Override public float tweakOffsetY() { return tweakY; }
	@Override public String tweakName() { return getClass().getSimpleName(); }

	/** 子类每帧 update 中调用，把自身登记为微调目标（未开启微调时为无操作）。 */
	protected void trackForTweak() {
		EffectOffsetTweaker.track(this);
	}
}
