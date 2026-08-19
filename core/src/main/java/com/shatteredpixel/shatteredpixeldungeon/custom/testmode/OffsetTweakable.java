package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

/**
 * 可被游戏内特效微调工具实时调整偏移的接口。
 *
 * <p>实现此接口的特效，把 {@link #tweakOffsetX()}/{@link #tweakOffsetY()}
 * 作为附加偏移参与自身定位（叠加在锚点/朝向计算之上）。微调工具开启后，
 * 会直接修改这两个值并在 HUD 上显示，打印出的数值即你最终要硬编码的常量。</p>
 *
 * <p>调用约定：特效应在每帧 {@code update()} 里调用一次
 * {@link EffectOffsetTweaker#track(OffsetTweakable)} 登记自己，微调工具才能抓到它。
 * 未开启微调时该调用为无开销空操作。</p>
 */
public interface OffsetTweakable {

	void setTweakOffset(float x, float y);

	float tweakOffsetX();

	float tweakOffsetY();

	String tweakName();
}
