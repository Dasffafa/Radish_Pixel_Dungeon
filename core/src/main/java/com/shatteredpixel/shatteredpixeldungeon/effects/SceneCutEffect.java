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
import com.badlogic.gdx.graphics.GL20;
import com.watabou.glscripts.Script;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.utils.RectF;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.effects.shaders.SceneCutShader;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.KillBoatSword;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;

import java.nio.FloatBuffer;

/**
 * 斩舰刀的击杀演出：刀光 → 空间撕裂 → 怪物被斜切、上半身滑落。
 *
 * <p>整段由一个自绘通道完成（都在 {@code GameScene.draw()} 末尾），因此顺序可控：</p>
 * <ol>
 *   <li><b>0 ~ 0.8s</b>：怪物保持<b>活着时的姿态</b>（精灵在击杀瞬间就被隐藏，所以
 *       <b>不会</b>播死亡动画 / 不会出现尸体），上面叠 GIF 后半段的空拍 + 空间撕裂。
 *       <b>不回放前半段刀光</b> —— 那 7 帧只给未击杀的普通攻击用。</li>
 *   <li><b>0.8 ~ 1.25s</b>：撕裂残留，停一下。</li>
 *   <li><b>1.25s 起</b>：同一张贴图过一刀（固定左上 → 右下），上半片滑半格、落地、淡出。</li>
 * </ol>
 *
 * <p>刀光/撕裂素材直接按像素画在怪物中心，缩放 (1.85, 0.95) —— 纵向压扁让刀光更细，
 * 同时把素材约 45° 的斜度压到接近刀口的 30°。地图、HUD、掉落物一概不动。</p>
 */
public class SceneCutEffect {

	// ============== 时间轴（秒） ==============

	/** 击杀只播 GIF 的后半段：空拍 + 空间撕裂（8 帧 / 10fps）。 */
	private static final float CLIP_TIME = KillBoatSlashEffect.TEAR_TIME;
	/** 撕裂放完后的停顿（这时什么都不动）。 */
	private static final float TEAR_HOLD = 0.45f;
	/** 落刀时刻。 */
	private static final float CUT_TIME = CLIP_TIME + TEAR_HOLD;

	/** 上半片滑出的距离（格）。 */
	private static final float SLIDE_TILES = 0.5f;
	private static final float SLIDE_DELAY = 0.02f;
	private static final float SLIDE_TIME = 0.35f;
	/** 落体距离（相对精灵高度）与时长。 */
	private static final float FALL_FRAC = 0.5f;
	private static final float FALL_DELAY = SLIDE_DELAY + SLIDE_TIME;
	private static final float FALL_TIME = 0.28f;
	/** 淡出。 */
	private static final float FADE_DELAY = FALL_DELAY + FALL_TIME;
	private static final float FADE_TIME = 0.14f;

	/** 刀口倾斜角（度）：固定左上 → 右下（屏幕 y 向下 ⇒ dy 取正），不做镜像。 */
	private static final float CUT_ANGLE_DEG = 30f;

	/** 刀光素材的单帧尺寸与播放帧率（跟 GIF 一致）。 */
	private static final int CLIP_FRAME = 32;
	private static final float CLIP_FPS = 10f;
	/** 刀光在屏幕上的缩放：x 保长度，y 压扁变细。 */
	private static final float CLIP_SCALE_X = 1.85f;
	private static final float CLIP_SCALE_Y = 0.95f;

	// ============== 状态 ==============

	private static boolean armed = false;
	private static boolean active = false;

	private static float elapsed = 0f;
	private static float seed = 0f;

	private static Char pendingVictim;

	private static SmartTexture tex;          // 怪物贴图
	private static RectF frame;               // 怪物当前帧
	private static final float[] rect = new float[4];
	private static final float[] line = new float[4];

	private static SmartTexture tearTex;      // 撕裂帧条

	private static Vertexbuffer quad;
	private static FloatBuffer quadBuffer;

	// ============== 触发 ==============

	@SubscribeEvent(event = AttackHitEvent.class, priority = 100)
	public static void onAttackHit(AttackHitEvent event) {
		if (!(event.getAttacker() instanceof Hero)) return;
		if (!(event.getAttackerWeapon() instanceof KillBoatSword)) return;

		Char victim = event.getDefender();
		if (victim == Dungeon.hero) return;
		if (victim.alignment == Char.Alignment.ALLY || victim.alignment == Char.Alignment.NEUTRAL) return;
		if (Dungeon.level == null || !Dungeon.level.heroFOV[victim.pos]) return;

		if (victim.isAlive()) {
			KillBoatSlashEffect.showOn(victim, false);   // 普通命中：只播刀光
		} else {
			trigger(victim);                             // 斩杀：整段演出
		}
	}

	/** 直接触发（测试模式可用）。 */
	public static void trigger(Char victim) {
		if (victim == null || victim.sprite == null) return;
		if (armed || active) return;
		if (Dungeon.hero == null || Dungeon.level == null) return;

		pendingVictim = victim;
		elapsed = 0f;
		seed = Random.Float(0f, 100f);
		armed = true;
	}

	// ============== 每帧绘制 ==============

	public static void postDraw() {
		if (armed) {
			armed = false;
			Char victim = pendingVictim;
			pendingVictim = null;
			if (!capture(victim)) return;
			active = true;
			elapsed = 0f;
		}
		if (!active) return;

		elapsed += Game.elapsed;
		if (elapsed >= CUT_TIME + FADE_DELAY + FADE_TIME) {
			finish();
			return;
		}
		render(elapsed);
	}

	/** 抓外观并立刻隐藏精灵 —— 这一刀下去，怪不演死亡动画、也不留尸体。 */
	private static boolean capture(Char victim) {
		if (victim == null || !(victim.sprite instanceof CharSprite)) return false;
		CharSprite s = victim.sprite;
		if (s.texture == null || s.parent == null) return false;

		tex = s.texture;
		frame = s.frame();

		rect[0] = s.x;
		rect[1] = s.y;
		rect[2] = s.width();
		rect[3] = s.height();
		if (rect[2] <= 0f || rect[3] <= 0f) return false;

		aim();
		s.visible = false;
		return true;
	}

	/** 刀口过精灵中心，方向固定左上 → 右下。 */
	private static void aim() {
		double rad = Math.toRadians(CUT_ANGLE_DEG);
		float dx = (float) Math.cos(rad);
		float dy = (float) Math.sin(rad);

		float len = (float) Math.sqrt(dx * dx + dy * dy);
		line[0] = spriteWidthPx() * 0.5f;
		line[1] = spriteHeightPx() * 0.5f;
		line[2] = dx / len;
		line[3] = dy / len;
	}

	private static float spriteWidthPx() {
		return frame.width() * tex.width;
	}

	private static float spriteHeightPx() {
		return frame.height() * tex.height;
	}

	private static float padPx() {
		return Math.max(SLIDE_TILES * DungeonTilemap.SIZE, FALL_FRAC * spriteHeightPx()) + 10f;
	}

	private static void render(float now) {
		if (tex == null || frame == null) {
			finish();
			return;
		}

		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

		float t = now - CUT_TIME;

		// ---- 1) 怪物本体：落刀前是完整的活体姿态，落刀后才切开 ----
		float crack = t >= 0f ? 1f : 0f;
		float slide = t > 0f ? clamp01((t - SLIDE_DELAY) / SLIDE_TIME) * SLIDE_TILES * DungeonTilemap.SIZE : 0f;
		float fallT = t > 0f ? clamp01((t - FALL_DELAY) / FALL_TIME) : 0f;
		float fall  = fallT * fallT * spriteHeightPx() * FALL_FRAC;
		float fade  = t > 0f ? 1f - clamp01((t - FADE_DELAY) / FADE_TIME) : 1f;

		float pad = padPx();
		beginPass(tex, frame, spriteWidthPx(), spriteHeightPx(), pad, slide, fall, crack, fade);
		drawQuad(rect[0] - pad, rect[1] - pad, rect[2] + pad * 2f, rect[3] + pad * 2f);

		// ---- 2) 空间撕裂：击杀不回放前半段刀光，只从撕裂开始 ----
		if (now < CLIP_TIME) {
			int f = Math.min(7, (int) (now * CLIP_FPS));
			SmartTexture clip = tearTex();
			float w = CLIP_FRAME * CLIP_SCALE_X;
			float h = CLIP_FRAME * CLIP_SCALE_Y;
			float cx = rect[0] + rect[2] * 0.5f;
			float cy = rect[1] + rect[3] * 0.5f;

			beginPass(clip, clipFrame(clip, f), CLIP_FRAME, CLIP_FRAME, 0f, 0f, 0f, 0f, 1f);
			drawQuad(cx - w * 0.5f, cy - h * 0.5f, w, h);
		}

		Script.unuse();
		Blending.setNormalMode();
	}

	private static SmartTexture tearTex() {
		if (tearTex == null) tearTex = TextureCache.get(Assets.Effects.KILL_BOAT_TEAR);
		return tearTex;
	}

	/** 帧条里第 i 帧的 uv 范围。 */
	private static RectF clipFrame(SmartTexture tex, int i) {
		float fw = CLIP_FRAME / (float) tex.width;
		float left = i * fw;
		return new RectF(left, 0f, left + fw, 1f);
	}

	private static void beginPass(SmartTexture texture, RectF uv, float w, float h, float pad,
	                              float slide, float fall, float crack, float fade) {
		SceneCutShader s = Shaders.sceneCut;
		s.use();
		s.setCamera(Camera.main.matrix);
		s.setFrame(uv.left, uv.top, uv.width(), uv.height());
		s.setSize(w, h);
		s.setPad(pad);
		s.setGlowOnly(0f);
		s.setLine(line[0], line[1], line[2], line[3]);
		s.setSlide(slide);
		s.setFall(fall);
		s.setSilhouette(0f);
		s.setFade(fade);
		s.setCrack(crack);
		s.setGlow(0f);
		s.setSeed(seed);
		s.bindTextures();
		texture.bind();

		Blending.enable();
		Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	private static void drawQuad(float x, float y, float w, float h) {
		if (quadBuffer == null) quadBuffer = Quad.create();
		quadBuffer.position(0);
		quadBuffer.put(new float[]{
				x,     y,     0f, 0f,
				x + w, y,     1f, 0f,
				x + w, y + h, 1f, 1f,
				x,     y + h, 0f, 1f
		});
		quadBuffer.position(0);
		if (quad == null) quad = new Vertexbuffer(quadBuffer);
		else quad.updateVertices(quadBuffer);

		SceneCutShader s = Shaders.sceneCut;
		s.aXYZW.enable();
		s.aUV.enable();
		quad.updateGLData();
		quad.bind();
		s.aXYZW.vertexBuffer(2, 4, 0);
		s.aUV.vertexBuffer(2, 4, 2);
		quad.release();

		Quad.bindIndices();
		Gdx.gl.glDrawElements(GL20.GL_TRIANGLES, Quad.SIZE, GL20.GL_UNSIGNED_SHORT, 0);
		// 绝不能 releaseIndices()：NoosaScript.drawQuad() 之后不会再绑索引缓冲，
		// 一旦解绑，后续普通精灵绘制的 glDrawElements 会解引用空指针（AMD 上实测崩溃）。
	}

	private static void finish() {
		active = false;
		armed = false;
		elapsed = 0f;
		pendingVictim = null;
		tex = null;
		frame = null;
		Script.unuse();
		Blending.setNormalMode();
	}

	/** 场景切换 / 重开时清干净。 */
	public static void reset() {
		finish();
		quad = null;
		quadBuffer = null;
	}

	private static float clamp01(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}
}
