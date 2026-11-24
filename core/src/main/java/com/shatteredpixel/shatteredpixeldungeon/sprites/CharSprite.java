/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.DarkBlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.EmoIcon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.IceBlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.ShaderEffect;
import com.shatteredpixel.shatteredpixeldungeon.effects.ShieldHalo;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TorchHalo;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.HalomethaneFlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.CharHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.nio.Buffer;

public class CharSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {

	/** Warped fx 框架给每个精灵留的槽位（本仓库只用了水那一格） */
	public Object[] fxSlots = new Object[ com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxModules.COUNT ];

	/** 是否"漂浮"（船/水面载具）。本仓库没有载具系统，恒为 false。 */
	public boolean afloat(){ return false; }

	/** 是否正在跳跃中。 */
	public boolean jumping(){ return false; }

	
	// Color constants for floating text
	public static final int DEFAULT		= 0xFFFFFF;
	public static final int POSITIVE	= 0x00FF00;
	public static final int NEGATIVE	= 0xFF0000;
	public static final int WARNING		= 0xFF8800;
	public static final int NEUTRAL		= 0xFFFF00;
	
	public static final float DEFAULT_MOVE_INTERVAL = 0.1f;
	private static float moveInterval = DEFAULT_MOVE_INTERVAL;
	private static final float FLASH_INTERVAL	= 0.05f;

	//the amount the sprite is raised from flat when viewed in a raised perspective
	protected float perspectiveRaise    = 6 / 16f; //6 pixels

	//the width and height of the shadow are a percentage of sprite size
	//offset is the number of pixels the shadow is moved down or up (handy for some animations)
	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1.2f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.25f;

	public enum State {
		BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED, CHILLED, DARKENED, MARKED, HEALING, SHIELDED, HEARTS,
		HALOMETHANEBURNING,ELMOBURNING,
		//Warped 的状态外观（effects/fx/combat/StateLooks）认得的其余状态：本仓库暂时没有系统会设置它们，
		//但枚举里要有，否则那套外观表没法搬
		GLOWING, AURA, ELECTRIC, BUTTER, HEATSTROKE, HYPOTHERMIA, TORCHLIT, ROOTED, BARKED, VERDANT, HERB,
		POISONED, CORRODED, OOZED, SEEDED, SURGING, BOAT_RAFT, BOAT_SAIL, RIDING
	}
	private int stunStates = 0;
	
	protected Animation idle;
	protected Animation run;
	protected Animation attack;
	protected Animation operate;
	protected Animation zap;
	protected Animation die;
	
	protected Callback animCallback;
	
	protected PosTweener motion;
	
	protected Emitter burning;

	protected Emitter ealoburning;

	protected Emitter chilled;
	protected Emitter marked;
	protected Emitter levitation;
	protected Emitter healing;
	protected Emitter hearts;
	protected Emitter haloburning;
	
	protected IceBlock iceBlock;
	protected DarkBlock darkBlock;
	protected TorchHalo light;
	protected ShieldHalo shield;
	protected AlphaTweener invisible;
	protected Flare aura;
	protected ShaderEffect shaderEffect;

	public void setShaderEffect(ShaderEffect effect) {
		if (shaderEffect == effect) return;
		ShaderEffect previous = shaderEffect;
		shaderEffect = effect;
		if (previous != null) {
			previous.killAndErase();
		}
	}

	public ShaderEffect getShaderEffect() {
		return shaderEffect;
	}

	/**
	 * 自定义死亡动画开始前的公共准备：清理休眠/麻痹状态，隐藏情绪图标与血条。
	 *
	 * @param playDeathAnimation 是否在特效播放期间继续播放正常的序列帧死亡动画；
	 *                           false 时冻结序列帧，让贴图停留在当前可见帧供 shader 接管。
	 */
	public void prepareDeathVisual(boolean playDeathAnimation) {
		sleeping = false;
		remove(State.PARALYSED);
		hideEmo();
		if (health != null) {
			health.killAndErase();
		}

		animCallback = null;
		if (playDeathAnimation) {
			play(die);
		} else {
			if (curAnim == die && idle != null && idle.frames != null && idle.frames.length > 0) {
				frame(idle.frames[0]);
			}
			curAnim = null;
		}
	}

	
	protected EmoIcon emo;
	protected CharHealthIndicator health;

	private Tweener jumpTweener;
	private Callback jumpCallback;

	protected float flashTime = 0;
	
	protected boolean sleeping = false;

	public Char ch;

	//used to prevent the actor associated with this sprite from acting until movement completes
	public volatile boolean isMoving = false;
	
	public CharSprite() {
		super();
		listener = this;
	}
	
	@Override
	public void play(Animation anim) {
		//Shouldn't interrupt the dieing animation
		if (curAnim == null || curAnim != die) {
			super.play(anim);
		}
	}
	
	//intended to be used for placing a character in the game world
	public void link( Char ch ) {
		linkVisuals( ch );
		
		this.ch = ch;
		ch.sprite = this;
		
		place( ch.pos );
		turnTo( ch.pos, Random.Int( Dungeon.level.length() ) );
		renderShadow = true;
		
		if (ch != Dungeon.hero) {
			if (health == null) {
				health = new CharHealthIndicator(ch);
			} else {
				health.target(ch);
			}
		}

		ch.updateSpriteState();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (ch != null && ch.sprite == this){
			ch.sprite = null;
		}
	}

	//used for just updating a sprite based on a given character, not linking them or placing in the game
	public void linkVisuals( Char ch ){
		//do nothin by default
	}
	
	public PointF worldToCamera( int cell ) {
		
		final int csize = DungeonTilemap.SIZE;
		
		return new PointF(
			PixelScene.align(Camera.main, ((cell % Dungeon.level.width()) + 0.5f) * csize - width() * 0.5f),
			PixelScene.align(Camera.main, ((cell / Dungeon.level.width()) + 1.0f) * csize - height() - csize * perspectiveRaise)
		);
	}
	
	public void place( int cell ) {
		point( worldToCamera( cell ) );
	}
	
	public void showStatus( int color, String text, Object... args ) {
		showStatusWithIcon(color, text, FloatingText.NO_ICON, args);
	}

	public void showStatusWithIcon( int color, String text, int icon, Object... args ) {
		if (visible) {
			if (args.length > 0) {
				text = Messages.format( text, args );
			}
			float x = destinationCenter().x;
			float y = destinationCenter().y - height()/2f;
			if (ch != null) {
				FloatingText.show( x, y, ch.pos, text, color, icon, true );
			} else {
				FloatingText.show( x, y, -1, text, color, icon, true );
			}
		}
	}

	/** 显示跳字并左侧叠加多个图标（用于混合伤害，图标顺序即展示顺序）。 */
	public void showStatusWithIcons( int color, String text, int[] icons, Object... args ) {
		if (visible) {
			if (args.length > 0) {
				text = Messages.format( text, args );
			}
			float x = destinationCenter().x;
			float y = destinationCenter().y - height()/2f;
			if (ch != null) {
				FloatingText.show( x, y, ch.pos, text, color, icons, true );
			} else {
				FloatingText.show( x, y, -1, text, color, icons, true );
			}
		}
	}
	
	public void idle() {
		play(idle);
	}
	
	public void move( int from, int to ) {
		com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxModules.stepped( this, from, to );
		turnTo( from , to );

		play( run );
		
		motion = new PosTweener( this, worldToCamera( to ), moveInterval );
		motion.listener = this;
		parent.add( motion );

		isMoving = true;
		
		if (visible && Dungeon.level.water[from] && !ch.flying) {
			GameScene.ripple( from );
		}

	}

	/** 自走棋：只启动跑动动画与朝向，位置由调用方逐帧插值（不创建补间）。 */
	public void autochessStartRun( int from, int to ) {
		turnTo( from, to );
		play( autochessWalkAnim() );
	}

	/** 大多数 SPD 怪物的 run 只有 1 帧（看不出动画），这种情况改用 idle 的呼吸帧。 */
	private float autoFrameTimer = 0f;
	private int autoFrameIdx = 0;

	/** 自走棋：手动推进走位动画帧（不依赖 MovieClip 的 finished/paused 判定）。 */
	/** 自走棋：站立时也手动推进 idle 呼吸帧，避免看起来完全静止。 */
	public void autochessIdleTick(float dt) {
		if (curAnim != idle) {
			play(idle);
			autoFrameIdx = 0;
			autoFrameTimer = 0f;
		}
		if (idle == null || idle.frames == null || idle.frames.length < 2 || idle.delay <= 0f) return;
		autoFrameTimer += dt;
		int guard = 0;
		while (autoFrameTimer > idle.delay && guard++ < 8) {
			autoFrameTimer -= idle.delay;
			autoFrameIdx = (autoFrameIdx + 1) % idle.frames.length;
			frame(idle.frames[autoFrameIdx]);
		}
	}

	public void autochessAdvance(float dt) {
		Animation a = autochessWalkAnim();
		if (a == null || a.frames == null || a.frames.length < 2 || a.delay <= 0f) return;
		autoFrameTimer += dt;
		int guard = 0;
		while (autoFrameTimer > a.delay && guard++ < 8) {
			autoFrameTimer -= a.delay;
			autoFrameIdx = (autoFrameIdx + 1) % a.frames.length;
			frame(a.frames[autoFrameIdx]);
		}
	}

	private Animation autochessWalkAnim() {
		if (run != null && run.frames != null && run.frames.length > 1) return run;
		if (idle != null && idle.frames != null && idle.frames.length > 1) return idle;
		return run != null ? run : idle;
	}

	/** 自走棋：滑行过程中每帧确保仍在播跑动动画（防被 idle 覆盖）。 */
	public void autochessTickRun() {
		Animation want = autochessWalkAnim();
		if (curAnim != want) {
			play( want );
		}
	}

	/** 自走棋：到达目的地，切回待机。 */
	public void autochessStopRun() {
		play( idle );
	}

	/** 调试：当前动画是不是 run。 */
	public int autochessFrame() { return curFrame; }
	public boolean autochessPaused() { return paused; }
	public int autochessAnimDelayMs() { return curAnim == null ? -1 : (int)(curAnim.delay * 1000); }
	public int autochessWalkFrame() { return autoFrameIdx; }
	public int autochessWalkFrames() { Animation a = autochessWalkAnim(); return a == null || a.frames == null ? 0 : a.frames.length; }
	public boolean autochessIsRunning() {
		return curAnim == autochessWalkAnim();
	}
	
	public static void setMoveInterval( float interval){
		moveInterval = interval;
	}
	
	//returns where the center of this sprite will be after it completes any motion in progress
	public PointF destinationCenter(){
		PosTweener motion = this.motion;
		if (motion != null && motion.elapsed >= 0){
			return new PointF(motion.end.x + width()/2f, motion.end.y + height()/2f);
		} else {
			return center();
		}
	}
	
	public void interruptMotion() {
		if (motion != null) {
			motion.stop(false);
		}
	}
	
	public void attack( int cell ) {
		attack( cell, null );
	}
	
	public synchronized void attack( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		play( attack );
	}
	
	public void operate( int cell ) {
		operate( cell, null );
	}
	
	public synchronized void operate( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		play( operate );
	}
	
	public void zap( int cell ) {
		zap( cell, null );
	}
	
	public synchronized void zap( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		play( zap );
	}
	
	public void turnTo( int from, int to ) {
		int fx = from % Dungeon.level.width();
		int tx = to % Dungeon.level.width();
		if (tx > fx) {
			flipHorizontal = false;
		} else if (tx < fx) {
			flipHorizontal = true;
		}
	}

	public void jump( int from, int to, Callback callback ) {
		float distance = Math.max( 1f, Dungeon.level.trueDistance( from, to ));
		jump( from, to, distance * 2, distance * 0.1f, callback );
	}

	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		jumpCallback = callback;

		jumpTweener = new JumpTweener( this, worldToCamera( to ), height, duration );
		jumpTweener.listener = this;
		parent.add( jumpTweener );

		turnTo( from, to );
	}

	public void die() {
		sleeping = false;
		remove( State.PARALYSED );
		play( die );

		hideEmo();
		
		if (health != null){
			health.killAndErase();
		}
	}

	public void skipDieAnimation() {
		sleeping = false;
		remove( State.PARALYSED );
		hideEmo();

		if (health != null){
			health.killAndErase();
		}
	}
	
	public Emitter emitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( this );
		return emitter;
	}
	
	public Emitter centerEmitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( center() );
		return emitter;
	}
	
	public Emitter bottomEmitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( x, y + height, width, 0 );
		return emitter;
	}
	
	public void burst( final int color, int n ) {
		if (visible) {
			Splash.at( center(), color, n );
		}
	}
	
	public void bloodBurstA( PointF from, int damage ) {
		if (visible) {
			PointF c = center();
			int n = (int)Math.min( 9 * Math.sqrt( (double)damage / ch.HT ), 9 );
			Splash.at( c, PointF.angle( from, c ), 3.1415926f / 2, blood(), n );
		}
	}

	/** The blow as the eye takes it (fx/combat/HitFx): its star, crescent, dust or sparks. */
	public void hitFx( float fromX, float fromY, int damage, int style, boolean heroInvolved ){
		//inside an open blow the star is one of its pictures (the presenter draws it on the lead);
		//outside one (a trap, a hazard) it draws itself at once
		if (ch != null && com.shatteredpixel.shatteredpixeldungeon.effects.fx.combat.Blow.offer(
				new com.shatteredpixel.shatteredpixeldungeon.effects.fx.combat.HitFx.StarPiece(
						ch, fromX, fromY, damage, style, heroInvolved ) ) ) return;
		com.shatteredpixel.shatteredpixeldungeon.effects.fx.combat.HitFx.hit( this, fromX, fromY, damage, style, heroInvolved );
	}

	/** A blow from `from` that missed this sprite: a whoosh past it and a puff at its feet. */
	public void dodgeFx( float fromX, float fromY ){
		com.shatteredpixel.shatteredpixeldungeon.effects.fx.combat.HitFx.dodged( this, fromX, fromY );
	}

	/** The frame it shows now (Warped's looks slice a crop of it). */
	public com.watabou.utils.RectF frameNow(){
		return frame();
	}

	/** A shiver of `dx` whole pixels (Warped's cold looks ask for one): not offset here. */
	public void shiver( int dx ){
	}

	/** Whether this sprite is mid-flash. */
	public boolean isFlashing(){
		return flashTime > 0;
	}

	/** Whether nothing is tinting this sprite. */
	public boolean tintFree( Object unused ){
		return flashTime <= 0;
	}

	/** A boat's rise and fall (Warped's sea look): this repo has none, so it stays where it is. */
	public void lift( int dy ){
	}

	public int lift(){
		return 0;
	}

	/** The colour of its aura (Warped's AURA state look). */
	public int auraColor(){
		return 0xFFE9A8;
	}

	/** Whether its stun stars are already drawn elsewhere. */
	public boolean stunStars(){
		return false;
	}

	public int blood() {
		return 0xFFBB0000;
	}
	
	public void flash() {
		ra = ba = ga = 1f;
		flashTime = FLASH_INTERVAL;
	}
	
	public void add( State state ) {
		switch (state) {
			case BURNING:
				burning = emitter();
				burning.pour( FlameParticle.FACTORY, 0.06f );
				if (visible) {
					Sample.INSTANCE.play( Assets.Sounds.BURNING );
				}
				break;
			case ELMOBURNING:
				ealoburning = emitter();
				ealoburning.pour(ElmoParticle.FACTORY,0.05f);
				break;
			case LEVITATING:
				levitation = emitter();
				levitation.pour( Speck.factory( Speck.JET ), 0.02f );
				break;
			case INVISIBLE:
				if (invisible != null) {
					invisible.killAndErase();
				}
				invisible = new AlphaTweener( this, 0.4f, 0.4f );
				if (parent != null){
					parent.add(invisible);
				} else
					alpha( 0.4f );
				break;
			case PARALYSED:
				paused = true;
				break;
			case FROZEN:
				iceBlock = IceBlock.freeze( this );
				break;
			case ILLUMINATED:
				GameScene.effect( light = new TorchHalo( this ) );
				break;
			case CHILLED:
				chilled = emitter();
				chilled.pour(SnowParticle.FACTORY, 0.1f);
				break;
			case DARKENED:
				darkBlock = DarkBlock.darken( this );
				break;
			case MARKED:
				marked = emitter();
				marked.pour(ShadowParticle.UP, 0.1f);
				break;
			case HEALING:
				healing = emitter();
				healing.pour(Speck.factory(Speck.HEALING), 0.5f);
				break;
			case SHIELDED:
				if (shield != null) {
					shield.killAndErase();
				}
				GameScene.effect(shield = new ShieldHalo(this));
				break;
			case HEARTS:
				hearts = emitter();
				hearts.pour(Speck.factory(Speck.HEART), 0.5f);
				break;
			case HALOMETHANEBURNING:
				haloburning = emitter();
				haloburning.pour(HalomethaneFlameParticle.FACTORY, 0.06f);
				if (visible) {
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				}
				break;
		}
	}
	
	public void remove( State state ) {
		switch (state) {
			case HALOMETHANEBURNING:
				if (haloburning != null) {
					haloburning.on = false;
					haloburning = null;
				}
				break;
			case BURNING:
				if (burning != null) {
					burning.on = false;
					burning = null;
				}
				break;
			case ELMOBURNING:
				if(ealoburning != null){
					ealoburning.on = false;
					ealoburning = null;
				}
				break;
			case LEVITATING:
				if (levitation != null) {
					levitation.on = false;
					levitation = null;
				}
				break;
			case INVISIBLE:
				if (invisible != null) {
					invisible.killAndErase();
					invisible = null;
				}
				alpha( 1f );
				break;
			case PARALYSED:
				paused = false;
				break;
			case FROZEN:
				if (iceBlock != null) {
					iceBlock.melt();
					iceBlock = null;
				}
				break;
			case ILLUMINATED:
				if (light != null) {
					light.putOut();
				}
				break;
			case CHILLED:
				if (chilled != null){
					chilled.on = false;
					chilled = null;
				}
				break;
			case DARKENED:
				if (darkBlock != null) {
					darkBlock.lighten();
					darkBlock = null;
				}
				break;
			case MARKED:
				if (marked != null){
					marked.on = false;
					marked = null;
				}
				break;
			case HEALING:
				if (healing != null){
					healing.on = false;
					healing = null;
				}
				break;
			case SHIELDED:
				if (shield != null){
					shield.putOut();
				}
				break;
			case HEARTS:
				if (hearts != null){
					hearts.on = false;
					hearts = null;
				}
				break;
		}
	}

	public void aura( int color ){
		if (aura != null){
			aura.killAndErase();
		}
		float size = Math.max(width(), height());
		size = Math.max(size+4, 16);
		aura = new Flare(5, size);
		aura.angularSpeed = 90;
		aura.color(color, true);
		aura.visible = visible;

		if (parent != null) {
			aura.show(this, 0);
		}
	}

	public void clearAura(){
		if (aura != null){
			aura.killAndErase();
			aura = null;
		}
	}
	
	@Override
	public void update() {
		if (paused && ch != null && curAnim != null && !curAnim.looped && !finished){
			listener.onComplete(curAnim);
			finished = true;
		}
		
		super.update();
		
		if (flashTime > 0 && (flashTime -= Game.elapsed) <= 0) {
			resetColor();
		}
		
		if (burning != null) {
			burning.visible = visible;
		}
		// 这两种燃烧的火焰同样要跟着贴图隐藏，否则视野外的角色身上会露出一团火
		if (haloburning != null) {
			haloburning.visible = visible;
		}
		if (ealoburning != null) {
			ealoburning.visible = visible;
		}
		if (levitation != null) {
			levitation.visible = visible;
		}
		if (iceBlock != null) {
			iceBlock.visible = visible;
		}
		if (chilled != null) {
			chilled.visible = visible;
		}
		if (marked != null) {
			marked.visible = visible;
		}
		if (healing != null){
			healing.visible = visible;
		}
		if (hearts != null){
			hearts.visible = visible;
		}
		if (aura != null){
			if (aura.parent == null){
				aura.show(this, 0);
			}
			aura.visible = visible;
			aura.point(center());
		}
		if (sleeping) {
			showSleep();
		} else {
			hideSleep();
		}
		synchronized (EmoIcon.class) {
			if (emo != null && emo.alive) {
				emo.visible = visible;
			}
		}
	}
	
	@Override
	public void resetColor() {
		super.resetColor();
		if (invisible != null){
			alpha(0.4f);
		}
	}
	
	public void showSleep() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Sleep)) {
				if (emo != null) {
					emo.killAndErase();
				}
				emo = new EmoIcon.Sleep(this);
				emo.visible = visible;
			}
		}
		idle();
	}
	
	public void hideSleep() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Sleep) {
				emo.killAndErase();
				emo = null;
			}
		}
	}
	
	public void showAlert() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Alert)) {
				if (emo != null) {
					emo.killAndErase();
				}
				emo = new EmoIcon.Alert(this);
				emo.visible = visible;
			}
		}
	}
	
	public void hideAlert() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Alert) {
				emo.killAndErase();
				emo = null;
			}
		}
	}
	
	public void showLost() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Lost)) {
				if (emo != null) {
					emo.killAndErase();
				}
				emo = new EmoIcon.Lost(this);
				emo.visible = visible;
			}
		}
	}
	
	public void hideLost() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Lost) {
				emo.killAndErase();
				emo = null;
			}
		}
	}

	public void hideEmo(){
		synchronized (EmoIcon.class) {
			if (emo != null) {
				emo.killAndErase();
				emo = null;
			}
		}
	}
	
	@Override
	public void kill() {
		com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxModules.gone( this );
		super.kill();
		
		hideEmo();
		
		for( State s : State.values()){
			remove(s);
		}
		
		if (health != null){
			health.killAndErase();
		}
	}

	private float[] shadowMatrix = new float[16];

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		Matrix.copy(matrix, shadowMatrix);
		Matrix.translate(shadowMatrix,
				(width * (1f - shadowWidth)) / 2f,
				(height * (1f - shadowHeight)) + shadowOffset);
		Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
	}

	@Override
	public void draw() {
		if (texture == null || (!dirty && buffer == null))
			return;

		if (renderShadow) {
			if (dirty) {
				((Buffer)verticesBuffer).position(0);
				verticesBuffer.put(vertices);
				if (buffer == null)
					buffer = new Vertexbuffer(verticesBuffer);
				else
					buffer.updateVertices(verticesBuffer);
				dirty = false;
			}

			NoosaScript script = script();

			texture.bind();

			script.camera(camera());

			updateMatrix();

			script.uModel.valueM4(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(buffer);
					}

					// 如果有着色器效果，使用着色器渲染
					if (shaderEffect != null && !shaderEffect.isFinished()) {
//						GLog.w("CharSprite.draw: 检测到着色器效果");
						if (dirty) {
							((Buffer)verticesBuffer).position(0);
							verticesBuffer.put(vertices);
							if (buffer == null)
								buffer = new Vertexbuffer(verticesBuffer);
							else
								buffer.updateVertices(verticesBuffer);
							dirty = false;
						}
						updateMatrix();
						shaderEffect.drawWithShader(texture, frame(), width, height, matrix, verticesBuffer, buffer);
					} else {
						super.draw();
					}

		com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxModules.afterDraw( this );
				}

	@Override
	public void onComplete( Tweener tweener ) {
		if (tweener == jumpTweener) {

			if (visible && Dungeon.level.water[ch.pos] && !ch.flying) {
				GameScene.ripple( ch.pos );
			}
			if (jumpCallback != null) {
				jumpCallback.call();
			}
			GameScene.sortMobSprites();

		} else if (tweener == motion) {

			synchronized (this) {
				isMoving = false;

				motion.killAndErase();
				motion = null;
				ch.onMotionComplete();

				GameScene.sortMobSprites();
				notifyAll();
			}

		}
	}

	@Override
	public synchronized void onComplete( Animation anim ) {
		
		if (animCallback != null) {
			Callback executing = animCallback;
			animCallback = null;
			executing.call();
		} else {
			
			if (anim == attack) {
				
				idle();
				ch.onAttackComplete();
				
			} else if (anim == operate) {
				
				idle();
				ch.onOperateComplete();
				
			}
			
		}
	}

	private static class JumpTweener extends Tweener {

		public CharSprite visual;

		public PointF start;
		public PointF end;

		public float height;

		public JumpTweener( CharSprite visual, PointF pos, float height, float time ) {
			super( visual, time );

			this.visual = visual;
			start = visual.point();
			end = pos;

			this.height = height;
		}

		@Override
		protected void updateValues( float progress ) {
			float hVal = -height * 4 * progress * (1 - progress);
			visual.point( PointF.inter( start, end, progress ).offset( 0, hVal ) );
			visual.shadowOffset = 0.25f - hVal*0.8f;
		}
	}
}
