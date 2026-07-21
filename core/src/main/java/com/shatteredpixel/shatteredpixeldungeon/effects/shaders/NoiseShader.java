/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
 */

package com.shatteredpixel.shatteredpixeldungeon.effects.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * 噪声溶解着色器。
 */
public class NoiseShader extends GlslShaderScript {

    private Uniform uNoiseBounds;
    private Uniform uTime;
    private Uniform uAlpha;
    private int uNoiseTexLocation;
    
    private static Texture noiseTexture;
    
    public NoiseShader() {
        super("noise");
        uNoiseBounds = uniform("uNoiseBounds");
        uTime = uniform("uTime");
        uAlpha = uniform("uAlpha");
        
        Uniform uNoiseTex = uniform("uNoiseTex");
        if (uNoiseTex != null) {
            uNoiseTexLocation = uNoiseTex.location();
        }
    }
    
    /**
     * 设置噪声纹理
     */
    public void setupNoise() {
        if (noiseTexture == null) {
            noiseTexture = new Texture(Gdx.files.internal("shaders/noise_texture.png"));
            noiseTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            noiseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        }
        
        // 绑定到纹理槽 1
        Gdx.gl20.glActiveTexture(Gdx.gl20.GL_TEXTURE1);
        noiseTexture.bind();
        Gdx.gl20.glActiveTexture(Gdx.gl20.GL_TEXTURE0);
        
        // 设置噪声纹理 uniform（纹理槽索引）
        Gdx.gl20.glUniform1i(uNoiseTexLocation, 1);
        
        // 设置噪声纹理边界
        if (uNoiseBounds != null) {
            uNoiseBounds.value4f(0, 0, 256, 256);
        }
    }
    
    @Override
    public void setTime(float time) {
        if (uTime != null) {
            uTime.value1f(time);
        }
    }
    
    public void setProgress(float progress) {
        if (uAlpha != null) {
            uAlpha.value1f(progress);
        }
    }
}