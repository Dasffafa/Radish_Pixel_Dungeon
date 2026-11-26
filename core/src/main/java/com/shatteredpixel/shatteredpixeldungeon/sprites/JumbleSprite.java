package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.JumbleChangeBuff;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

/**
 * 杂散职业精灵，所有动画均使用 jumble.png 的独立帧组。
 * <p>
 * 采用怪物的贴图模板：这是一个自足完整的精灵类，所有动画都在本类内定义，
 * 不依赖英雄的盔甲分层逻辑，可直接替换角色的原版动作。
 * 继承 {@link HeroSprite} 是为了让 17+ 处强转（移动 sprint、卷轴 read、伪装 disguise、
 * 换装 updateArmor 等）依然可用，从而让 Hero 无缝使用本精灵。
 * <p>
 * 贴图为 jumble.png（336x192），按 12x16 帧格划分，共 28 列 x 12 行。
 * 共 6 组贴图，每组占据两行，与职业无关（每 50 回合自动变身到其中一组）。
 * <p>
 * 组内第一行：站立 2 帧、行走 6 帧、死亡 4 帧、攻击 5 帧、使用 2 帧、阅读 3 帧；
 * 组内第二行：前 10 帧消失、后 10 帧出现。
 * 第 5 组攻击为 9 帧（12-20），第 6 组攻击为 10 帧（12-21），使用/阅读帧随之延后。
 */
public class JumbleSprite extends HeroSprite {

	private static final int FRAME_W = 12;
	private static final int FRAME_H = 16;

	private static final int COLS = 28;
	private static final int GROUP_COUNT = 6;

	// 各组攻击帧数与前摇帧数（0 表示无前摇/连击机制，>0 时攻击后停在前摇末帧，连续攻击直接播后续挥击帧）
	private static final int[] ATTACK_FRAME_COUNTS  = { 5, 5, 5, 5, 9, 10 };
	private static final int[] ATTACK_WINDUP_FRAMES = { 0, 0, 0, 0, 3, 7 };

	// 当前使用第几组贴图（0-5）
	private int group = 0;

	private Animation attackCombo;
	private Animation enterInvisible;
	private Animation exitInvisible;

	public JumbleSprite() {
		super();
	}

	private static int normalizeGroup( int g ) {
		return ((g % GROUP_COUNT) + GROUP_COUNT) % GROUP_COUNT;
	}

	/** 切换到指定组（0-5）的贴图，重建动画并回到待机。 */
	public void setGroup( int g ) {
		this.group = normalizeGroup( g );
		updateArmor();
	}

	public int group() {
		return group;
	}

	/** 清除变身演出的回调，供读档/超时收尾跳过未完成动画。 */
	public synchronized void cancelAnimationCallback() {
		animCallback = null;
	}

	private static Object[] rangeFrames( int start, int count ) {
		Object[] frames = new Object[count];
		for (int i = 0; i < count; i++) {
			frames[i] = start + i;
		}
		return frames;
	}

	private static Object[] offsetFrames( int base, int... offsets ) {
		Object[] frames = new Object[offsets.length];
		for (int i = 0; i < offsets.length; i++) {
			frames[i] = base + offsets[i];
		}
		return frames;
	}

	@Override
	public void updateArmor() {
		if (ch == Dungeon.hero) {
			JumbleChangeBuff buff = Dungeon.hero.buff(JumbleChangeBuff.class);
			if (buff != null) {
				group = normalizeGroup( buff.currentGroup() );
			}
		}
		texture( Assets.Sprites.JUMBLE );

		TextureFilm film = new TextureFilm( texture, FRAME_W, FRAME_H );

		int base = group * 2 * COLS;
		int base2 = (group * 2 + 1) * COLS;

		// 站立：列 0-1
		idle = new Animation( 2, true ).named( "jumble.idle g" + group );
		idle.frames( film, offsetFrames( base,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1 ) );

		// 行走：列 2-7
		run = new Animation( 20, true ).named( "jumble.run g" + group );
		run.frames( film, rangeFrames( base + 2, 6 ) );

		// 死亡：列 8-11
		die = new Animation( 20, false ).named( "jumble.die g" + group );
		die.frames( film, rangeFrames( base + 8, 4 ) );

		// 攻击、使用、飞行、阅读：列位随组别攻击帧数顺延
		int attackStart = base + 12;
		int attackCount = ATTACK_FRAME_COUNTS[group];
		int attackEnd = attackStart + attackCount - 1;

		attack = new Animation( 15, false ).named( "jumble.attack g" + group );
		attack.frames( film, rangeFrames( attackStart, attackCount ) );

		int windup = ATTACK_WINDUP_FRAMES[group];
		if (windup > 0 && windup < attackCount) {
			attackCombo = new Animation( 15, false ).named( "jumble.attackCombo g" + group );
			attackCombo.frames( film, rangeFrames( attackStart + windup, attackCount - windup ) );
		} else {
			attackCombo = null;
		}

		zap = attack.clone();

		// 使用：紧随攻击之后 2 帧
		operate = new Animation( 8, false ).named( "jumble.operate g" + group );
		operate.frames( film, offsetFrames( attackEnd, 1, 2, 1, 2 ) );

		fly = new Animation( 8, true ).named( "jumble.fly g" + group );
		fly.frames( film, attackEnd + 3 );

		// 阅读：紧随飞行之后 2 帧
		read = new Animation( 20, false ).named( "jumble.read g" + group );
		read.frames( film, offsetFrames( attackEnd, 4, 5, 5, 5, 5, 5, 5, 5, 4 ) );

		// 第二行：前 10 帧消失、后 10 帧出现
		enterInvisible = new Animation( 12, false ).named( "jumble.enterInvisible g" + group );
		enterInvisible.frames( film, rangeFrames( base2, 10 ) );

		exitInvisible = new Animation( 12, false ).named( "jumble.exitInvisible g" + group );
		exitInvisible.frames( film, rangeFrames( base2 + 10, 10 ) );

		// 确保变身倒计时 buff 存在（幂等，不重复）
		if (ch == Dungeon.hero) {
			JumbleChangeBuff.resetCountdownIfMissing();
		}

		paused = false;
		if (ch != null && ch.isAlive())
			idle();
		else
			die();
	}

	@Override
	public void idle() {
		if (paused && curAnim == attack && attackCombo != null) return;
		super.idle();
	}

	@Override
	public synchronized void attack( int cell, Callback callback ) {
		if (attackCombo != null && paused && curAnim == attack) {
			animCallback = callback;
			turnTo( ch.pos, cell );
			play( attackCombo, true );
			return;
		}
		super.attack( cell, callback );
	}

	@Override
	public synchronized void play( Animation anim, boolean force ) {
		paused = false;
		super.play( anim, force );
	}

	@Override
	public synchronized void onComplete( Animation anim ) {
		if ((anim == attack || anim == attackCombo) && animCallback == null && attackCombo != null) {
			int holdIdx = ATTACK_WINDUP_FRAMES[group] - 1;
			if (curAnim != attack) {
				super.play( attack, true );
			}
			curFrame = holdIdx;
			frame( attack.frames[holdIdx] );
			paused = true;
			if (ch != null) ch.onAttackComplete();
			return;
		}
		super.onComplete( anim );
	}

	public void playChange( Callback onComplete ) {
		animCallback = onComplete;
		play( enterInvisible );
	}

	public void playAppear( int newGroup, Callback onComplete ) {
		this.group = normalizeGroup( newGroup );
		updateArmor();
		animCallback = onComplete;
		play( exitInvisible );
	}
}
