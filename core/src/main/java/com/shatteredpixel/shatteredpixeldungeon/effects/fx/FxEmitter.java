/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Warped Pixel Dungeon
 * Copyright (C) 2026 Gabriel Duarte Guerra (gabriwar)
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

package com.shatteredpixel.shatteredpixeldungeon.effects.fx;

import com.watabou.noosa.particles.Emitter;

/**
 * The kit's emitter: an Emitter whose start and spawn points draw from FxRandom, never from the
 * game's seeded Random, so starting one costs gameplay nothing whichever thread it is on; and
 * never sent on to a co-op guest (the trigger it answers to already reaches him). Pooled by the
 * game's scene in its three layers: GameScene.fxEmitter() among the effects, lightEmitter() in
 * the light layer, floorFxEmitter() on the floor. Frozen with every emitter (freezeEmitters).
 */
public class FxEmitter extends Emitter {

	@Override
	public void start( Factory factory, float interval, int quantity ){
		super.start( factory, interval, quantity );
	}

	@Override
	public void burst( Factory factory, int quantity ){
		start( factory, 0, quantity );
	}

	@Override
	protected void emit( int index ){
		if (target == null){
			factory.emit( this, index, x + FxRandom.Float( width ), y + FxRandom.Float( height ) );
		} else if (fillTarget){
			factory.emit( this, index,
					target.x + FxRandom.Float( target.width() ),
					target.y + FxRandom.Float( target.height() ) );
		} else {
			factory.emit( this, index,
					target.x + x + FxRandom.Float( width ),
					target.y + y + FxRandom.Float( height ) );
		}
	}

	@Override
	public void revive(){
		super.revive();
		lightMode = false;
	}

	/**
	 * 回收粒子：本仓库的 Emitter 没有覆写 recycle，Group.recycle 只返回死掉的池子对象、
	 * 不会 revive —— 不补这一步，粒子永远 alive=false、既不动也不销毁，
	 * 越攒越多直到预算耗尽，滴水就"过一会儿不掉了"。
	 */
	@Override
	public synchronized com.watabou.noosa.Gizmo recycle( Class<? extends com.watabou.noosa.Gizmo> c ){
		com.watabou.noosa.Gizmo g = super.recycle( c );
		if (g != null && !g.alive){
			g.revive();
		}
		return g;
	}

	/** 当前挂着的粒子数（调试/验收） */
	public int live(){
		return length;
	}

	/** 本仓库的 Emitter 没暴露子节点，给 fx 模块遍历用 */
	public java.util.List<com.watabou.noosa.Gizmo> members(){
		return members;
	}

	/** Drawn as light (added) though it is never started: a holder of loose light. */
	public void holdLight(){
		lightMode = true;
	}
}
