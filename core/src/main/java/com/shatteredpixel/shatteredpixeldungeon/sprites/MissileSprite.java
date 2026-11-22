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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.StormAttackArrow;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CompositeCrossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ScorpionCrossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WhiteKingGodSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Bolas;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Javelin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Kunai;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Perfidy;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Senbon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Trident;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Visual;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

import java.util.HashMap;

public class MissileSprite extends ItemSprite implements Tweener.Listener {

	private static final float SPEED	= 240f;

	private Callback callback;

	public void reset( int from, int to, Item item, Callback listener ) {
		reset(Dungeon.level.solid[from] ? DungeonTilemap.raisedTileCenterToWorld(from) : DungeonTilemap.raisedTileCenterToWorld(from),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.raisedTileCenterToWorld(to),
				item, listener);
	}

	public void reset( Visual from, int to, Item item, Callback listener ) {
		reset(from.center(),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.raisedTileCenterToWorld(to),
				item, listener );
	}

	public void reset( Visual from, int to, Item item, Callback listener, float speedMultiplier ) {
		reset(from.center(),
				Dungeon.level.solid[to] ? DungeonTilemap.raisedTileCenterToWorld(to) : DungeonTilemap.raisedTileCenterToWorld(to),
				item, listener, speedMultiplier);
	}

	// 新增方法：从怪物头上掉下来且瞬发执行的特效
	public void resetFromAbove(Char ch, int targetPos, Item item, Callback listener) {
		revive();

		if (item == null)   view(ItemSpriteSheet.SOMETHING, null);
		else                view(item);
		PointF from = new PointF(0, 0);
		// 目标位置保持不变
		PointF to = DungeonTilemap.raisedTileCenterToWorld(targetPos);
		from.x =  0;
		from.y -= 0;
		to.x -= ch.sprite.width-10;
		to.y -= ch.sprite.height;

		this.callback = listener;
		point(from);

		angle = 90;
		flipHorizontal = false;
		updateFrame();

		angularSpeed = 0;
		PosTweener tweener = new PosTweener(this, to, 1f);
		tweener.listener = this;
		parent.add(tweener);
	}

	public void reset( int from, Visual to, Item item, Callback listener ) {
		reset(Dungeon.level.solid[from] ? DungeonTilemap.raisedTileCenterToWorld(from) : DungeonTilemap.raisedTileCenterToWorld(from),
				to.center(),
				item, listener );
	}

	public void reset( Visual from, Visual to, Item item, Callback listener ) {
		reset(from.center(), to.center(), item, listener );
	}

	public void reset( PointF from, PointF to, Item item, Callback listener) {
		reset(from, to, item, listener, 1f);
	}

	private void reset( PointF from, PointF to, Item item, Callback listener, float speedMultiplier) {
		revive();

		if (item == null)   view(ItemSpriteSheet.SOMETHING, null);
		else {
			view( item );
			if (item instanceof Senbon) frame( ItemSpriteSheet.SENBON_PROJECTILE );
		}

		setup( from,
				to,
				item,
				listener, speedMultiplier );
	}
	
	private static final int DEFAULT_ANGULAR_SPEED = 720;
	
	private static final HashMap<Class<?extends Item>, Integer> ANGULAR_SPEEDS = new HashMap<>();
	static {
		ANGULAR_SPEEDS.put(Dart.class,          0);
		ANGULAR_SPEEDS.put(ThrowingKnife.class, 0);
		ANGULAR_SPEEDS.put(ThrowingSpike.class, 0);
		ANGULAR_SPEEDS.put(FishingSpear.class,  0);
		ANGULAR_SPEEDS.put(ThrowingSpear.class, 0);
		ANGULAR_SPEEDS.put(Kunai.class,         0);
		ANGULAR_SPEEDS.put(Javelin.class,       0);
		ANGULAR_SPEEDS.put(Trident.class,       0);
		ANGULAR_SPEEDS.put(SpiritBow.ALTSpiritArrow.class,       0);
		ANGULAR_SPEEDS.put(Perfidy.class,       0);
		ANGULAR_SPEEDS.put(SpiritBow.SpiritArrow.class,       0);
		ANGULAR_SPEEDS.put(CompositeCrossbow.CrossbowArrow.class, 0);
		ANGULAR_SPEEDS.put(ScorpioSprite.ScorpioShot.class,   0);

		// DoggingDog 20250121
		ANGULAR_SPEEDS.put(StormAttackArrow.class,   0);
		ANGULAR_SPEEDS.put(WhiteKingGodSword.class,   0);

		//720 is default

		ANGULAR_SPEEDS.put(GnollGeomancer.Boulder.class,   90);
		
		ANGULAR_SPEEDS.put(HeavyBoomerang.class,1440);
		ANGULAR_SPEEDS.put(Bolas.class,         1440);
		
		ANGULAR_SPEEDS.put(Shuriken.class,                  2160);
		//千本不旋转
		ANGULAR_SPEEDS.put(Senbon.class,                    0);
		ANGULAR_SPEEDS.put(TenguSprite.TenguShuriken.class, 2160);
	}

	//TODO it might be nice to have a source and destination angle, to improve thrown weapon visuals
	private void setup( PointF from, PointF to, Item item, Callback listener, float speedMultiplier ){

		this.enchantSource = item;
		originToCenter();

		//adjust points so they work with the center of the missile sprite, not the corner
		from.x -= width()/2;
		to.x -= width()/2;
		from.y -= height()/2;
		to.y -= height()/2;

		this.callback = listener;

		point( from );

		PointF d = PointF.diff( to, from );
		speed.set(d).normalize().scale(SPEED * speedMultiplier);
		
		angularSpeed = DEFAULT_ANGULAR_SPEED;
		for (Class<?extends Item> cls : ANGULAR_SPEEDS.keySet()){
			if (cls.isAssignableFrom(item.getClass())){
				angularSpeed = ANGULAR_SPEEDS.get(cls);
				break;
			}
		}
		
		angle = 135 - (float)(Math.atan2( d.x, d.y ) / 3.1415926 * 180);
		
		if (d.x >= 0){
			flipHorizontal = false;
			updateFrame();
			
		} else {
			angularSpeed = -angularSpeed;
			angle += 90;
			flipHorizontal = true;
			updateFrame();
		}

		if (item instanceof GnollGeomancer.Boulder){
			angle = 0;
			flipHorizontal = false;
			updateFrame();
		}

		//千本贴图朝上，其他投掷物朝右上，差值 45 度；镜像贴图时方向相反
		if (item instanceof Senbon){
			angle += flipHorizontal ? -45 : 45;
			updateFrame();
		}
		
		// 飞多久由下面的位置补间决定；倍率只乘在上面的 speed 向量上不起作用，位置每帧都被补间盖掉
		float speed = SPEED * speedMultiplier;
		if ((item instanceof Dart && Dungeon.hero.belongings.weapon() instanceof Crossbow)
				||(Dungeon.hero.belongings.weapon() instanceof ScorpionCrossbow && (
				item instanceof Javelin
						||  item instanceof ThrowingSpear
						||  item instanceof Trident
		))){
			speed *= 3f;

		} else if (item instanceof SpiritBow.SpiritArrow
				|| item instanceof ScorpioSprite.ScorpioShot
				|| item instanceof TenguSprite.TenguShuriken
				|| item instanceof CompositeCrossbow.CrossbowArrow){
			speed *= 1.5f;
		}
		
		PosTweener tweener = new PosTweener( this, to, d.length() / speed );
		tweener.listener = this;
		parent.add( tweener );
	}

	private com.shatteredpixel.shatteredpixeldungeon.items.Item enchantSource;
	private float trailIn;
	/** 累计飞行距离：每 6px 留一个符号 */
	private float markDist;
	private boolean markRing;
	private float lastMarkX, lastMarkY;

	// ---- 残影（照 Warped 的 EnchantTrail：GHOSTS=4、GHOST_ALPHA=0.35、GHOST_LIFE=0.3）----
	private static final float GHOST_ALPHA = 0.35f, GHOST_LIFE = 0.3f;
	private float ghostIn = 0.03f;

	/** 自己会淡出、自己会消失的残影（不依赖弹体是否还活着） */
	public static class Ghost extends com.watabou.noosa.Image {
		private float life = GHOST_LIFE;
		Ghost( Object texture ){ super( texture ); }
		@Override
		public void update(){
			super.update();
			life -= com.watabou.noosa.Game.elapsed;
			am = Math.max( 0f, GHOST_ALPHA * life / GHOST_LIFE );
			if (life <= 0f) killAndErase();
		}
	}
	private float lastX, lastY;
	private float vx, vy;

	@Override
	public void update() {
		float px = x, py = y;
		super.update();
		// 拖尾：沿飞行轨迹等距留"符号"（加号/圆环交替），像 Warped 那样
		if (exists){
			vx = x - px;
			vy = y - py;
			float len = (float)Math.hypot( vx, vy );
			markDist += len;
			// 沿这一帧的线段按固定间距补符号，速度快时也不会挤成一堆
			while (markDist >= 12f){
				markDist -= 12f;
				markRing = !markRing;
				float ux = len > 0.01f ? vx / len : 1f;
				float uy = len > 0.01f ? vy / len : 0f;
				float back = 7f + markDist;   // 留在弹体后方
				// 抬高一点（Warped 的 EnchantTrail.RAISE = 6f），免得符号和箭在同一水平线上像铁轨
				com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx.trailMark(
						x - ux * back, y - uy * back - 6f, enchantSource, markRing );
			}
			lastMarkX = x;
			lastMarkY = y;
		}

		updateGhosts();
	}

	/** 残影：每 0.03s 在当前位置留一张同一帧的淡影（Ghost 自己淡出） */
	private void updateGhosts(){
		if ((ghostIn -= com.watabou.noosa.Game.elapsed) > 0f) return;
		ghostIn = 0.03f;
		if (parent == null) return;

		Ghost g = new Ghost( texture );
		g.frame( frame() );
		g.scale.set( scale.x, scale.y );
		g.angle = angle;
		g.x = x;
		g.y = y;
		g.am = GHOST_ALPHA;
		parent.add( g );
	}

	@Override
	public void onComplete( Tweener tweener ) {
		int color = com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx.color( enchantSource );
		if (color == 0) color = com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx.missileColor( enchantSource );
		if (color != 0){
			com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx.burst( x, y, color );
		}
		kill();
		if (callback != null) {
			callback.call();
		}
	}
}
