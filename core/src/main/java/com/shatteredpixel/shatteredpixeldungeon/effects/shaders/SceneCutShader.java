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

import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Uniform;

/**
 * 斩舰刀击杀的「怪物图层」劈裂着色器：左上→右下切一刀，上半片沿刀口滑走。
 *
 * <p>坐标是<b>精灵局部像素</b>（不是图集 uv）：{@code SceneCutEffect} 把怪物精灵隐藏后，
 * 用它的贴图 / 帧 / 世界矩形自绘一个只覆盖这只怪的 quad。帧范围 {@code uFrame} 由基类提供。</p>
 */
public class SceneCutShader extends GlslShaderScript {

    private Uniform uSize;
    private Uniform uPad;
    private Uniform uLine;
    private Uniform uSlide;
    private Uniform uFall;
    private Uniform uSilhouette;
    private Uniform uFade;
    private Uniform uCrack;
    private Uniform uGlow;
    private Uniform uSeed;
    private Uniform uGlowOnly;

    public SceneCutShader() {
        super("scenecut");
        uSize = uniform("uSize");
        uPad = uniform("uPad");
        uLine = uniform("uLine");
        uSlide = uniform("uSlide");
        uFall = uniform("uFall");
        uSilhouette = uniform("uSilhouette");
        uFade = uniform("uFade");
        uCrack = uniform("uCrack");
        uGlow = uniform("uGlow");
        uSeed = uniform("uSeed");
        uGlowOnly = uniform("uGlowOnly");
    }

    /** 精灵像素尺寸，用来把局部 uv 换算成像素空间。 */
    public void setSize(float width, float height) {
        if (uSize != null) uSize.value2f(width, height);
    }

    /** quad 相对精灵四边外扩的像素；外扩后滑出去的半片才不会被 quad 边界裁掉。 */
    public void setPad(float pad) {
        if (uPad != null) uPad.value1f(pad);
    }

    /** 刀口：xy 为精灵局部像素上的一点，zw 为单位方向（左上→右下）。 */
    public void setLine(float px, float py, float dx, float dy) {
        if (uLine != null) uLine.value4f(px, py, dx, dy);
    }

    /** 上半片沿刀口滑出的距离（像素，视觉方向 -dir）。 */
    public void setSlide(float slide) {
        if (uSlide != null) uSlide.value1f(slide);
    }

    /** 上半片自由落体位移（像素，向下）。 */
    public void setFall(float fall) {
        if (uFall != null) uFall.value1f(fall);
    }

    /** 1 = 怪物画成纯黑剪影。 */
    public void setSilhouette(float silhouette) {
        if (uSilhouette != null) uSilhouette.value1f(silhouette);
    }

    /** 整体淡出。 */
    public void setFade(float fade) {
        if (uFade != null) uFade.value1f(fade);
    }

    /** 刀光显现进度 0..1。 */
    public void setCrack(float crack) {
        if (uCrack != null) uCrack.value1f(crack);
    }

    /** 次元斩刀光强度。 */
    public void setGlow(float glow) {
        if (uGlow != null) uGlow.value1f(glow);
    }

    /** 1 = 只输出刀光（第二遍绘制用的刀光层，不采样贴图）。 */
    public void setGlowOnly(float glowOnly) {
        if (uGlowOnly != null) uGlowOnly.value1f(glowOnly);
    }

    /** 每次击杀的随机种子，决定刀口锯齿形状。 */
    public void setSeed(float seed) {
        if (uSeed != null) uSeed.value1f(seed);
    }
}
