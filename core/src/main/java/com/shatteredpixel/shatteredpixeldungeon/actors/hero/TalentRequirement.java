package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.items.talentitem.SpellQueue;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TalentRequirement {

	private TalentRequirement() { }

	public static void sync(Hero hero) {
		if (hero == null) return;
		hero.pendingTalentRequirements = false;
		for (Talent talent : Talent.removableRequirements) {
			if (!hero.hasTalent(talent)) removeItem(hero, talent.requirement().asSubclass(Item.class));
		}
		for (LinkedHashMap<Talent, Integer> tier : hero.talents) {
			for (Map.Entry<Talent, Integer> entry : tier.entrySet()) {
				if (entry.getValue() <= 0) continue;
				Class<? extends Bundlable> type = entry.getKey().requirement();
				if (type == null) continue;
				if (Buff.class.isAssignableFrom(type)) {
					Class<? extends Buff> buffType = type.asSubclass(Buff.class);
					if (hero.buff(buffType) == null && Buff.affect(hero, buffType) != null) {
						hero.talentRequirementBuffs.add(type.getName());
					}
				} else if (Item.class.isAssignableFrom(type)) {
					provideItem(hero, type.asSubclass(Item.class));
				} else {
					throw new IllegalArgumentException("Talent requirement must be an Item or Buff: " + type.getName());
				}
			}
		}
		if (hero.hasTalent(Talent.SPELL_QUEUE) && hero.belongings.getItem(SpellQueue.class) != null) {
			Buff.affect(hero, SpellQueue.imageListner.class);
		} else {
			Buff.detach(hero, SpellQueue.imageListner.class);
		}
		if (hero.hasTalent(Talent.CARD_ENCHANT) || hero.hasTalent(Talent.REFINED_ATTACK_DEFENSE)
				|| hero.hasTalent(Talent.CARD_ASSIST) || hero.hasTalent(Talent.SPIN_CARD)
				|| hero.hasTalent(Talent.GRAND_FINALE) || hero.hasTalent(Talent.PREPARED)) CardDeck.getOrCreate(hero);
	}

	private static void removeItem(Hero hero, Class<? extends Item> type) {
		ArrayList<Item> removed = null;
		// 直接遍历原始物品，失去背包期间也必须回收。
		for (Item item : hero.belongings) {
			if (type.isInstance(item)) {
				if (removed == null) removed = new ArrayList<>();
				removed.add(item);
			}
		}
		if (removed != null) {
			for (Item item : removed) item.detachAll(hero.belongings.backpack);
		}
		if (Dungeon.level != null) {
			for (Heap heap : Dungeon.level.heaps.valueList()) {
				for (int i = heap.items.size() - 1; i >= 0; i--) {
					Item item = heap.items.get(i);
					if (type.isInstance(item)) {
						Dungeon.quickslot.clearItem(item);
						heap.remove(item);
					}
				}
			}
		}
		hero.talentRequirementItems.remove(type.getName());
	}

	private static void provideItem(Hero hero, Class<? extends Item> type) {
		String key = type.getName();
		if (hero.talentRequirementItems.contains(key)) {
			if (type == CardDrawer.class && hero.cardDeck == null) CardDeck.getOrCreate(hero);
			return;
		}
		if (hero.belongings.lostInventory()) {
			hero.pendingTalentRequirements = true;
			return;
		}
		if (hero.belongings.getItem(type) != null) {
			hero.talentRequirementItems.add(key);
			return;
		}
		Item item = type == MagesStaff.class ? new MagesStaff(new WandOfMagicMissile()) : Reflection.newInstance(type);
		if (item == null) throw new IllegalStateException("Cannot create talent requirement: " + key);
		item.identify();
		if (!item.collect(hero.belongings.backpack)) {
			if (Dungeon.level == null) {
				hero.pendingTalentRequirements = true;
				return;
			}
			Dungeon.level.drop(item, hero.pos);
		}
		if (type == CardDrawer.class && hero.cardDeck == null) hero.cardDeck = CardDeck.starterDeck();
		hero.talentRequirementItems.add(key);
	}

	public static boolean isActive(Hero hero, Class<? extends Buff> type) {
		if (type == Belief.class && (hero.heroClass == HeroClasses.RECTOR
				|| (hero.heroClass == HeroClasses.JUMBLE && hero.jumbleLoadout == HeroClasses.RECTOR)
				|| hero.hasSubClass(HeroSubClasses.BATTLEPREIST) || hero.hasSubClass(HeroSubClasses.REDCARDINAL)
				|| hero.hasSubClass(HeroSubClasses.DEAD_KNIGHT))) return true;
		if (type == MagicPoint.class && hero.hasSubClass(HeroSubClasses.DICE_MAGE)) return true;
		if (!hero.talentRequirementBuffs.contains(type.getName())) return true;
		for (LinkedHashMap<Talent, Integer> tier : hero.talents) {
			for (Map.Entry<Talent, Integer> entry : tier.entrySet()) {
				if (entry.getValue() > 0 && entry.getKey().requirement() == type) return true;
			}
		}
		return false;
	}

	public static boolean isValidTalentItem(Hero hero, Item item) {
		if (hero == null || item == null) return true;
		for (Talent talent : Talent.removableRequirements) {
			Class<? extends Bundlable> req = talent.requirement();
			if (req != null && req.isInstance(item) && !hero.hasTalent(talent)) {
				return false;
			}
		}
		return true;
	}

	public static void store(Bundle bundle, Hero hero) {
		bundle.put("talent_requirement_items", hero.talentRequirementItems.toArray(new String[0]));
		bundle.put("talent_requirement_buffs", hero.talentRequirementBuffs.toArray(new String[0]));
	}

	public static void restore(Bundle bundle, Hero hero) {
		hero.talentRequirementItems.clear();
		hero.talentRequirementBuffs.clear();
		Collections.addAll(hero.talentRequirementItems, bundle.getStringArray("talent_requirement_items"));
		Collections.addAll(hero.talentRequirementBuffs, bundle.getStringArray("talent_requirement_buffs"));
	}
}
