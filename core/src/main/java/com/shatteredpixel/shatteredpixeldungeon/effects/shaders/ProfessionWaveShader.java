package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.badlogic.gdx.Gdx;
import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * 职业专属道具的紫黑色水波扭曲与变色着色器。
 */
public class ProfessionWaveShader extends GlslShaderScript {

	private final Uniform uTime;
	private int uTimeLocation;

	public ProfessionWaveShader() {
		super("profession_wave");
		uTime = uniform("uTime");
		if (uTime != null) {
			uTimeLocation = uTime.location();
		}
	}

	public void setTime(float time) {
		if (uTime != null) {
			Gdx.gl20.glUniform1f(uTimeLocation, time);
		}
	}
}
