/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HolyDecree;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

import java.util.ArrayList;
import java.util.Collection;

/** 圣宣武技「哀悼」铺下的黑白蝴蝶场地：每回合对敌人造成小额伤害并迟缓。 */
public class ButterflyField extends Blob {

	/** 场地每回合造成的伤害（原设计是 1 精神 + 1 侵蚀，剥离类型后合并为 2 点物理）。 */
	public static final int TICK_DAMAGE = 2;
	public static final float POUR_INTERVAL = 0.12f;

	@Override
	protected void evolve() {
		int cell;
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j * Dungeon.level.width();
				if (cell < 0 || cell >= cur.length) continue;
				if (cur[cell] > 0) {
					affect(cell);
					off[cell] = cur[cell] - 1;
					volume += off[cell];
				} else {
					off[cell] = 0;
				}
			}
		}
	}

	private void affect( int cell ){
		Char ch = Actor.findChar( cell );
		if (ch == null || ch.alignment != Char.Alignment.ENEMY || !ch.isAlive()) return;
		ch.damage( new DamageInfo( TICK_DAMAGE, DamageType.PHYSICAL_NO_ARMOR, null, null, this ) );
		Buff.prolong( ch, Slow.class, 8f );
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( HolyDecree.BUTTERFLIES, POUR_INTERVAL );
	}

	/** 按与施法者的真距由近及远铺开场地；逐格 seed，保证关卡序列化能重建。 */
	public static void spread( int from, Collection<Integer> cells, int duration, com.watabou.noosa.Group layer ){
		if (cells == null) return;
		ArrayList<Integer> sorted = new ArrayList<>(cells);
		sorted.sort( (a, b) -> Float.compare(
				Dungeon.level.trueDistance( from, a ), Dungeon.level.trueDistance( from, b ) ) );
		for (Integer cell : sorted){
			if (cell == null) continue;
			GameScene.add( Blob.seed( cell, duration, ButterflyField.class ) );
		}
	}

	@Override
	public String tileDesc() {
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( this, "desc" );
	}
}
