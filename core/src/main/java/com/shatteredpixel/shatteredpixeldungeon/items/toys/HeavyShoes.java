package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeavyShoesBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 沉重的鞋 — 伤害提升 33%，移动速度下降为 0.8
 */
public class HeavyShoes extends ItemArmorAttachable {

	{
		image = ItemSpriteSheet.SNAKE_BITE;
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, HeavyShoesBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		HeavyShoesBuff buff = hero.buff(HeavyShoesBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)((HeavyShoesBuff.DAMAGE_MULTIPLIER - 1) * 100), (int)(HeavyShoesBuff.SPEED_MULTIPLIER * 100));
	}
}
