package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PoemBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 诗 — 连续攻击相同名字的敌人拥有 66% 伤害加成
 */
public class Poem extends ItemArmorAttachable {

	{
		image = ItemSpriteSheet.SNAKE_BITE;
	}

	public static boolean checkCombo(Char enemy) {
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero == null) return false;
		PoemBuff buff = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.buff(PoemBuff.class);
		return buff != null && buff.checkCombo(enemy);
	}

	public static float getDamageMultiplier() {
		if (com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero == null) return 1f;
		PoemBuff buff = com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.buff(PoemBuff.class);
		return buff != null ? buff.getDamageMultiplier() : 1f;
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, PoemBuff.class);
	}

	@Override
	public void removeEffect(Hero hero) {
		PoemBuff buff = hero.buff(PoemBuff.class);
		if (buff != null) buff.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", PoemBuff.REQUIRED_COMBO, (int)((PoemBuff.COMBO_DAMAGE_MULTIPLIER - 1) * 100));
	}
}
