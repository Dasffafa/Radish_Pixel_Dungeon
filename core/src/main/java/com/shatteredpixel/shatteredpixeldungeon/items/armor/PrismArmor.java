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
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

/** 棱镜护铠：四阶护甲，远程伤害 -25%，法术伤害有概率反射回去。 */
public class PrismArmor extends Armor {

	{ image = ItemSpriteSheet.ARMOR_PRISM; }

	public PrismArmor() { super( 4 ); }

	@Override public int DRMax(int lvl){ return 6 + 4 * lvl + augment.defenseFactor(lvl); }
	@Override public int DRMin(int lvl){ return 2 + lvl; }

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		if (attacker != null && defender != null && Dungeon.level != null
				&& !Dungeon.level.adjacent( attacker.pos, defender.pos )){
			damage = Math.round( damage * 0.75f );
		}
		return super.proc( attacker, defender, damage );
	}

	/** 命中后事件能拿到本次 DamageInfo，据此判定「伤害类型为法术」。 */
	@SubscribeEvent(event = AttackHitEvent.class, priority = 0)
	public static void onSpellHit( AttackHitEvent event ){
		Hero hero = Dungeon.hero;
		if (hero == null || event.getDefender() != hero) return;
		if (!(hero.belongings.armor instanceof PrismArmor)) return;
		DamageInfo info = event.getDamage();
		if (info == null || !info.getType().isMagical()) return;
		Char attacker = event.getAttacker();
		if (attacker == null || attacker == hero || !attacker.isAlive()) return;

		PrismArmor armor = (PrismArmor) hero.belongings.armor;
		float chance = (10 + 7 * armor.buffedLvl()) / 100f;
		if (Random.Float() >= chance) return;
		int reflected = Math.max( 1, event.getEffectiveDamage() );
		attacker.damage( new DamageInfo( reflected, DamageType.MAGICAL, hero, armor, armor ) );
		attacker.sprite.showStatus( CharSprite.NEGATIVE, Messages.get( PrismArmor.class, "reflect" ) );
	}
}
