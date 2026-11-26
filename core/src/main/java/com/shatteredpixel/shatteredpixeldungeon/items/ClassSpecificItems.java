package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.JumbleTalents;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Wheelchair;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.SmallWoodenCross;

import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 职业开局专属物品登记表。 */
public final class ClassSpecificItems {

	private static final List<Entry> ENTRIES = Collections.unmodifiableList(Arrays.asList(
			new Entry(HeroClasses.WARRIOR, BrokenSeal.class),
			new Entry(HeroClasses.MAGE, MagesStaff.class),
			new Entry(HeroClasses.ROGUE, CloakOfShadows.class),
			new Entry(HeroClasses.HUNTRESS, SpiritBow.class),
			new Entry(HeroClasses.RECTOR, SmallWoodenCross.class),
			new Entry(HeroClasses.MOONLIGHT, Wheelchair.class),
			new Entry(HeroClasses.CARDTRICKSTER, CardDrawer.class),
			new Entry(HeroClasses.ENGINEER, PulseWand.class)
	));

	private ClassSpecificItems() { }

	public static HeroClass ownerOf(Item item) {
		if (item == null) return null;
		for (Entry entry : ENTRIES) {
			if (entry.owns(item.getClass())) return entry.owner;
		}
		return null;
	}

	public static List<Class<? extends Item>> itemsFor(HeroClass owner) {
		for (Entry entry : ENTRIES) {
			if (entry.owner == owner) return entry.itemClasses;
		}
		return Collections.emptyList();
	}

	/** 创建其他职业的专属物品，首次出现时使用自身初始状态。 */
	public static Item replacementFor(Item oldItem) {
		return replacementFor(oldItem, null);
	}

	public static Item replacementFor(Item oldItem, com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {
		HeroClass oldOwner = ownerOf(oldItem);
		if (oldOwner == null) return null;

		ArrayList<Class<? extends Item>> candidates = new ArrayList<>();
		for (Entry entry : ENTRIES) {
			if (entry.owner != oldOwner) {
				for (Class<? extends Item> candidate : entry.itemClasses) {
					if (hero == null || JumbleTalents.canRollWithReplacement(hero, oldItem, candidate)) {
						candidates.add(candidate);
					}
				}
			}
		}
		if (candidates.isEmpty()) return null;

		Class<? extends Item> chosen = Random.element(candidates);
		Item replacement = chosen == MagesStaff.class
				? new MagesStaff(new WandOfMagicMissile())
				: Reflection.newInstance(chosen);
		if (replacement == null) return null;
		replacement.identify();

		return replacement;
	}

	private static final class Entry {
		final HeroClass owner;
		final List<Class<? extends Item>> itemClasses;

		@SafeVarargs
		Entry(HeroClass owner, Class<? extends Item>... itemClasses) {
			this.owner = owner;
			this.itemClasses = Collections.unmodifiableList(Arrays.asList(itemClasses));
		}

		boolean owns(Class<?> itemClass) {
			for (Class<? extends Item> registered : itemClasses) {
				if (registered.isAssignableFrom(itemClass)) return true;
			}
			return false;
		}
	}
}
