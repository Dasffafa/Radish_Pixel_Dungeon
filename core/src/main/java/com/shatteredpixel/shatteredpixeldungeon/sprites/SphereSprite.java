package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.RectF;

/**
 * 圆球皮肤精灵（月华 MOONLIGHT 的可选皮肤）。
 * <p>
 * 采用怪物的贴图模板：这是一个自足完整的精灵类，所有动画都在本类内定义，
 * 不依赖英雄的盔甲分层逻辑，可直接替换角色的原版动作。
 * 继承 {@link HeroSprite} 是为了让 17+ 处强转（移动 sprint、卷轴 read、伪装 disguise、
 * 换装 updateArmor 等）依然可用，从而让 Hero 无缝使用本精灵。
 * <p>
 * 贴图为 sphere.png（168x48），按 12x16 帧格划分，共 14 列 x 3 行：
 * 第 1 行：闪烁待机 5 帧（首帧时长 7 倍）、滚动 10 帧；
 * 第 2 行：跳跃 10 帧（用作互动动画）、卷轴阅读 4 帧；
 * 第 3 行：死亡裂开 4 帧。
 * <p>
 * <b>特殊移动表现</b>：移动结束后不再回退到待机闪烁，而是冻结在当前滚动帧；
 * 再次移动时从该帧继续滚动，形成连贯的滚动效果。滚动帧动画与基础移动时长同步。
 */
public class SphereSprite extends HeroSprite {

	private static final int FRAME_W = 12;
	private static final int FRAME_H = 16;

	// 周长为 2.5 格；滚动只跟随位移，不受动画帧率和冲刺速度影响。
	private static final float MOVE_ANGLE = 360f / 2.5f;
	private boolean rolling;
	private float moveStartAngle;
	private float spinTargetAngle;
	private Animation rollAnim;

	@Override
	public void updateArmor() {
		texture( Assets.Sprites.SPHERE );

		TextureFilm film = new TextureFilm( texture, FRAME_W, FRAME_H );

		idle = new Animation( 10, true );
		idle.frames( film, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 0, 0, 1, 2, 3, 4 );

		run = new Animation( 30, true );
		run.frames( film, 0, 5, 6, 7, 8, 9, 10, 11, 12, 13 );

		operate = new Animation( 30, false );
		operate.frames( film, 14, 15, 16, 17, 17, 18, 18, 19, 20, 21, 22, 23 );

		attack = operate.clone();
		zap = operate.clone();

		read = new Animation( 10, false );
		read.frames( film, 23, 24, 25, 26, 27, 27, 27, 27);

		die = new Animation( 5, false );
		die.frames( film, 28, 29, 30, 31 );

		fly = idle.clone();

		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	@Override
	public void move( int from, int to ) {
		Hero hero = (Hero) ch;
		moveStartAngle = hero.sphereAngle();
		rolling = true;
		super.move( from, to );
		motion.interval *= 1.5f;
		hero.sphereAngle( moveStartAngle + MOVE_ANGLE );
	}

	public void spin( float fromAngle, float toAngle ){

		int fromIdx = angleToFrame( fromAngle );
		int toIdx   = angleToFrame( toAngle );

		int diff = toIdx - fromIdx;
		if (diff > 5) diff -= 10;
		else if (diff < -5) diff += 10;

		int steps = Math.abs( diff );
		int dir   = (int)Math.signum( diff );

		if (steps == 0){
			setAngle( toAngle );
			return;
		}

		RectF[] cells = new RectF[steps + 1];
		int idx = fromIdx;
		for (int i = 0; i <= steps; i++){
			cells[i] = run.frames[idx];
			idx = (idx + dir + 10) % 10;
		}
		Animation rotate = new Animation( Math.round( 1f / run.delay ), false ).frames( cells );
		spinTargetAngle = toAngle;
		rollAnim = rotate;
		flipHorizontal = false;
		play( rotate, true );
	}

	private int angleToFrame( float angle ){
		float a = ((angle % 360f) + 360f) % 360f;
		return (Math.round( a / 36f )) % 10;
	}

	public void setAngle( float angle ){
		// curFrame 不会立即刷新贴图，暂停前必须同步实际显示的帧。
		if (curAnim != run) super.play( run, true );
		curFrame = angleToFrame( angle );
		frame( run.frames[curFrame] );
		paused = true;
		flipHorizontal = false;
	}

	@Override
	public void link( Char ch ) {
		super.link( ch );
		if (ch instanceof Hero && ((Hero) ch).isSphereSkin()){
			setAngle( ((Hero) ch).sphereAngle() );
		}
	}

	@Override
	public void idle() {
		if (curAnim == run || curAnim == rollAnim) return;
		super.idle();
	}

	@Override
	public synchronized void play( Animation anim, boolean force ) {
		if (rolling && (anim == run || anim == fly)) {
			setAngle( moveStartAngle );
			return;
		}
		paused = false;
		super.play( anim, force );
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == rollAnim) {
			setAngle( spinTargetAngle );
		} else if (anim == attack && animCallback == null) {
			setAngle( 0f );
			if (ch != null) ch.onAttackComplete();
		} else {
			super.onComplete( anim );
		}
	}

	@Override
	public void update() {
		if (rolling) paused = false;
		super.update();
	}

	@Override
	protected synchronized void updateAnimation() {
		if (rolling && motion != null) {
			float dx = motion.end.x - motion.start.x;
			float dy = motion.end.y - motion.start.y;
			float distanceSquared = dx * dx + dy * dy;
			float progress = distanceSquared == 0f ? 1f
					: ((x - motion.start.x) * dx + (y - motion.start.y) * dy) / distanceSquared;
			setAngle( moveStartAngle + MOVE_ANGLE * Math.max( 0f, Math.min( 1f, progress ) ) );
		} else if (curAnim != run) {
			super.updateAnimation();
		}
	}

	@Override
	public void onComplete( com.watabou.noosa.tweeners.Tweener tweener ) {
		if (tweener == motion && rolling) {
			setAngle( moveStartAngle + MOVE_ANGLE );
			rolling = false;
		}
		super.onComplete( tweener );
	}
}
