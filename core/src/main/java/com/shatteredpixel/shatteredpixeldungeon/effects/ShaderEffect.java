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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.glwrap.GlslShaderScript;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.RectF;

import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * 着色器效果控制器。
 */
public class ShaderEffect extends Gizmo {

    private final CharSprite target;
    private final ShaderType shaderType;
    private final float duration;
    private float elapsed = 0;

    private final GlslShaderScript shader;
    private float[] combinedMatrix = new float[16];

    private FloatBuffer shaderVertices;
    private Vertexbuffer shaderVBO;

    private static int drawCallCount = 0;  // 诊断计数器

    public ShaderEffect(CharSprite target, ShaderType type, float duration) {
        super();
        this.target = target;
        this.shaderType = type;
        this.duration = duration;

        GLog.w("ShaderEffect: 构造函数被调用, type=" + type + ", duration=" + duration);

        shader = getShader(type);
        initShaderParams();
    }

    private GlslShaderScript getShader(ShaderType type) {
        switch (type) {
            case CUT: return Shaders.cut;
            case BURN: return Shaders.burn;
            case ELLIPSE: return Shaders.ellipse;
            case ACID: return Shaders.acid;
            case WIPE: return Shaders.wipe;
            case ALPHA: return Shaders.alpha;
            case SINGULARITY: return Shaders.singularity;
            case NOISE: return Shaders.noise;
            default: return Shaders.alpha;
        }
    }

    private void initShaderParams() {
        float sx = target.x, sy = target.y;
        float sw = target.width(), sh = target.height();
        
        shader.setAlpha(1.0f);
        shader.setTime(0);

        switch (shaderType) {
            case CUT:
                if (shader instanceof CutShader) {
                    CutShader c = (CutShader) shader;
                    c.setCutLine(0.5f, 0.5f, 1.0f, 1.0f);
                    c.setCutProgress(0);
                    c.setCutSide(1);
                }
                break;
            case BURN:
                if (shader instanceof BurnShader) {
                    BurnShader b = (BurnShader) shader;
                    b.setBounds(sx, sy, sw, sh);
                    b.setBurnProgress(0);
                    b.setRandom((float) Math.random());
                }
                break;
            case ELLIPSE:
                if (shader instanceof EllipseShader) {
                    EllipseShader e = (EllipseShader) shader;
                    e.setBounds(sx, sy, sw, sh);
                    e.setProgress(0);
                    e.setRandom((float) Math.random());
                }
                break;
            case ACID:
                if (shader instanceof AcidShader) {
                    AcidShader a = (AcidShader) shader;
                    a.setProgress(0);
                    a.setRandom((float) Math.random());
                }
                break;
            case WIPE:
                if (shader instanceof WipeShader) {
                    WipeShader w = (WipeShader) shader;
                    w.setBounds(sx, sy, sw, sh);
                    w.setProgress(0);
                    w.setDirection(1, 0);
                }
                break;
            case ALPHA:
                if (shader instanceof AlphaShader) {
                    AlphaShader a = (AlphaShader) shader;
                    a.setBounds(sx, sy, sw, sh);
                    a.setProgress(0);
                }
                break;
            case SINGULARITY:
                if (shader instanceof SingularityShader) {
                    SingularityShader s = (SingularityShader) shader;
                    s.setBounds(sx, sy, sw, sh);
                    s.setProgress(0);
                    s.setScale(1, 1);
                }
                break;
            case NOISE:
                if (shader instanceof NoiseShader) {
                    NoiseShader n = (NoiseShader) shader;
                    n.setupNoise();
                    n.setTime(0);
                    n.setProgress(0);
                }
                break;
        }
    }

    @Override
    public void update() {
        super.update();

        elapsed += Game.elapsed;
        float progress = Math.min(elapsed / duration, 1.0f);
        updateShaderProgress(progress);

        if (progress >= 1.0f) {
            if (target != null && target.isPendingDeathAfterShader()) {
                target.clearPendingDeath();
                target.setShaderEffect(null);
                if (target.parent != null) {
                    target.parent.add(new AlphaTweener(target, 0, 0.5f) {
                        @Override
                        protected void onComplete() {
                            target.killAndErase();
                        }
                    });
                } else {
                    target.killAndErase();
                }
            }
            killAndErase();
        }
    }

    private void updateShaderProgress(float progress) {
        shader.setTime(elapsed);

        switch (shaderType) {
            case CUT:
                if (shader instanceof CutShader) ((CutShader) shader).setCutProgress(progress);
                break;
            case BURN:
                if (shader instanceof BurnShader) ((BurnShader) shader).setBurnProgress(progress);
                break;
            case ELLIPSE:
                if (shader instanceof EllipseShader) ((EllipseShader) shader).setProgress(progress);
                break;
            case ACID:
                if (shader instanceof AcidShader) ((AcidShader) shader).setProgress(progress);
                break;
            case WIPE:
                if (shader instanceof WipeShader) ((WipeShader) shader).setProgress(progress);
                break;
            case ALPHA:
                if (shader instanceof AlphaShader) ((AlphaShader) shader).setProgress(progress);
                break;
            case SINGULARITY:
                if (shader instanceof SingularityShader) ((SingularityShader) shader).setProgress(progress);
                break;
            case NOISE:
                if (shader instanceof NoiseShader) ((NoiseShader) shader).setProgress(progress);
                break;
        }
    }

    /**
     * 由 CharSprite.draw() 调用，接管渲染。
     */
    public void drawWithShader(com.watabou.gltextures.SmartTexture texture, RectF frame,
                               float spriteWidth, float spriteHeight,
                               float[] spriteMatrix,
                               FloatBuffer verticesBuffer, Vertexbuffer buffer) {
        drawCallCount++;
        
        // 只每60帧输出一次，避免刷屏
        if (drawCallCount % 60 == 1) {
//            GLog.w("ShaderEffect.drawWithShader 被调用! count=" + drawCallCount +
//                   " texture=" + (texture != null) + " buffer=" + (buffer != null));
        }

        if (texture == null) {
//            GLog.w("drawWithShader: texture is null!");
            return;
        }

        Camera cam = camera();
        if (cam == null) cam = Camera.main;
        if (cam == null || cam.matrix == null) {
//            GLog.w("drawWithShader: camera is null!");
            return;
        }

        if (buffer == null) {
//            GLog.w("drawWithShader: buffer is null!");
            return;
        }

        Matrix.multiply(cam.matrix, spriteMatrix, combinedMatrix);

        shader.use();
        shader.bindTextures();
        texture.bind();
        shader.setCamera(combinedMatrix);

        if (shader.aXYZW != null) {
            shader.aXYZW.enable();
        } else {
//            GLog.w("drawWithShader: aXYZW is null!");
        }
        if (shader.aUV != null) {
            shader.aUV.enable();
        } else {
//            GLog.w("drawWithShader: aUV is null!");
        }

        buffer.bind();
        shader.aXYZW.vertexBuffer(2, 4, 0);
        shader.aUV.vertexBuffer(2, 4, 2);
        buffer.release();

        Quad.bindIndices();
        Gdx.gl20.glDrawElements(Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0);

        com.watabou.glscripts.Script.unuse();
    }

    public boolean isFinished() {
        return elapsed >= duration;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (target != null && target.getShaderEffect() == this) {
            target.setShaderEffect(null);
        }
        if (shaderVBO != null) {
            shaderVBO.delete();
        }
    }

    // ========== 静态入口 ==========

    public static void apply(Char target, ShaderType type, float duration) {
//        GLog.w("ShaderEffect.apply 被调用! target=" + target + " type=" + type);
        
        if (target == null || target.sprite == null) {
//            GLog.w("ShaderEffect.apply: target 或 sprite 为 null!");
            return;
        }

        CharSprite sprite = target.sprite;

        sprite.dieAfterShader();

        final CharSprite finalSprite = sprite;
        final ShaderType finalType = type;
        final float finalDuration = duration;
        
        Game.runOnRenderThread(() -> {
//            GLog.w("ShaderEffect: 渲染线程回调执行中...");
            
            if (finalSprite.parent == null) {
//                GLog.w("ShaderEffect: sprite.parent 为 null，已移除?");
                return;
            }
            
            ShaderEffect old = finalSprite.getShaderEffect();
            if (old != null) {
                old.killAndErase();
            }

            ShaderEffect effect = new ShaderEffect(finalSprite, finalType, finalDuration);
            finalSprite.setShaderEffect(effect);
//            GLog.w("ShaderEffect: setShaderEffect 已调用, shaderEffect=" + finalSprite.getShaderEffect());

            if (finalSprite.parent != null) {
                finalSprite.parent.add(effect);
            }
        });
    }

    public enum ShaderType {
        CUT, BURN, ELLIPSE, ACID, WIPE, ALPHA, SINGULARITY, NOISE
    }
}