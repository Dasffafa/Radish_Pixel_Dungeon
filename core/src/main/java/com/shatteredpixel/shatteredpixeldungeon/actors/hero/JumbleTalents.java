package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public final class JumbleTalents {

	private JumbleTalents() { }

	public static void initClass(Hero hero) {
		ensureTiers(hero);
		fill(hero, 0, commonPool(0), 4);
		fill(hero, 1, commonPool(1), 5);
		fill(hero, 2, commonPool(2), 2);
	}

	public static void initSubclass(Hero hero) {
		initClass(hero);
		if (hero.jumbleFirst == HeroSubClasses.NONE || hero.jumbleSecond == HeroSubClasses.NONE) {
			ArrayList<HeroSubClass> pool = new ArrayList<>();
			for (HeroClass cls : HeroClasses.ALL) {
				if (cls == HeroClasses.JUMBLE) continue;
				for (HeroSubClass sub : cls.subClasses()) {
					if (sub != HeroSubClasses.NONE && sub != HeroSubClasses.JOKER
							&& sub != HeroSubClasses.JUTTE_CHAMPION && !pool.contains(sub)) pool.add(sub);
				}
			}
			ArrayList<HeroSubClass> pairs = new ArrayList<>();
			for (int first = 0; first < pool.size(); first++) {
				for (int second = first + 1; second < pool.size(); second++) {
					LinkedHashSet<Talent> tier4 = new LinkedHashSet<>();
					addTalents(tier4, pool.get(first).subclassT4());
					addTalents(tier4, pool.get(second).subclassT4());
					if (tier4.size() >= 2) {
						pairs.add(pool.get(first));
						pairs.add(pool.get(second));
					}
				}
			}
			int pair = 2 * Random.Int(pairs.size() / 2);
			hero.jumbleFirst = pairs.get(pair);
			hero.jumbleSecond = pairs.get(pair + 1);
		}
		fill(hero, 2, specialPool(hero, 2), 5);
		TalentRequirement.sync(hero);
	}

	public static void initImp(Hero hero) {
		initSubclass(hero);
		ArrayList<Talent> special = specialPool(hero, 3);
		ArrayList<Talent> common = commonPool(3);
		common.removeAll(special);
		fill(hero, 3, common, 2);
		fill(hero, 3, special, 4);
	}

	private static void ensureTiers(Hero hero) {
		while (hero.talents.size() < Talent.MAX_TALENT_TIERS) hero.talents.add(new LinkedHashMap<>());
	}

	private static ArrayList<Talent> commonPool(int tier) {
		LinkedHashSet<Talent> pool = new LinkedHashSet<>();
		for (HeroClass cls : HeroClasses.ALL) {
			if (cls == HeroClasses.JUMBLE) continue;
			TalentSet set = cls.definition().talents();
			Talent[] source;
			switch (tier) {
				case 0: source = set.getTier1(); break;
				case 1: source = set.getTier2(); break;
				case 2: source = set.getTier3(); break;
				case 3: source = set.getTier4(); break;
				default: throw new IllegalArgumentException("Invalid talent tier: " + tier);
			}
			addTalents(pool, source);
		}
		return new ArrayList<>(pool);
	}

	private static ArrayList<Talent> specialPool(Hero hero, int tier) {
		LinkedHashSet<Talent> pool = new LinkedHashSet<>();
		addTalents(pool, tier == 2 ? hero.jumbleFirst.subclassT3() : hero.jumbleFirst.subclassT4());
		addTalents(pool, tier == 2 ? hero.jumbleSecond.subclassT3() : hero.jumbleSecond.subclassT4());
		return new ArrayList<>(pool);
	}

	private static void addTalents(LinkedHashSet<Talent> pool, Talent[] source) {
		if (source == null) return;
		for (Talent talent : source) {
			// 计数槽和原职业尚未实装的占位天赋不能成为随机奖励。
			if (talent != Talent.D3_SKIPPED && talent != Talent.ERROR
					&& !talent.name().startsWith("PLACEHOLDER_")) pool.add(talent);
		}
	}

	private static void fill(Hero hero, int tier, ArrayList<Talent> pool, int size) {
		LinkedHashMap<Talent, Integer> talents = hero.talents.get(tier);
		pool.removeAll(talents.keySet());
		while (talents.size() < size && !pool.isEmpty()) {
			talents.put(pool.remove(Random.Int(pool.size())), 0);
		}
		if (talents.size() < size) throw new IllegalStateException("Insufficient Jumble talent pool for tier " + tier);
	}

	public static void metamorph(Hero hero) {
		int tiers = hero.powerOfImp ? 4 : 3;
		for (int tier = 0; tier < tiers; tier++) {
			LinkedHashMap<Talent, Integer> previous = hero.talents.get(tier);
			LinkedHashMap<Talent, Integer> next = new LinkedHashMap<>();
			ArrayList<Talent> common = commonPool(tier);
			ArrayList<Talent> special = tier >= 2 ? specialPool(hero, tier) : common;
			if (tier >= 2) common.removeAll(special);
			int index = 0;
			for (Talent old : previous.keySet()) {
				ArrayList<Talent> pool = tier >= 2 && index >= 2 ? special : common;
				pool.removeAll(next.keySet());
				if (pool.isEmpty()) throw new IllegalStateException("Insufficient Jumble metamorph pool");
				Talent replacement = pool.get(Random.Int(pool.size()));
				next.put(replacement, Math.min(previous.get(old), replacement.maxPoints()));
				index++;
			}
			hero.talents.set(tier, next);
			for (Talent talent : next.keySet()) {
				if (!previous.containsKey(talent) && next.get(talent) > 0) {
					Talent.suppressGain = true;
					try {
						Talent.onTalentUpgraded(hero, talent);
					} finally {
						Talent.suppressGain = false;
					}
				}
			}
		}
		TalentRequirement.sync(hero);
	}

	public static void store(Bundle bundle, Hero hero) {
		bundle.put("jumble_loadout", hero.jumbleLoadout == null ? "" : hero.jumbleLoadout.name());
		bundle.put("jumble_first", hero.jumbleFirst.name());
		bundle.put("jumble_second", hero.jumbleSecond.name());
		for (int tier = 0; tier < Talent.MAX_TALENT_TIERS; tier++) {
			ArrayList<String> names = new ArrayList<>();
			for (Talent talent : hero.talents.get(tier).keySet()) names.add(talent.name());
			bundle.put("jumble_tier_" + tier, names.toArray(new String[0]));
		}
	}

	public static boolean restore(Bundle bundle, Hero hero) {
		if (!bundle.contains("jumble_tier_0")) return false;
		hero.jumbleLoadout = HeroClasses.fromSaveName(bundle.getString("jumble_loadout"));
		hero.jumbleFirst = HeroSubClasses.fromSaveName(bundle.getString("jumble_first"));
		hero.jumbleSecond = HeroSubClasses.fromSaveName(bundle.getString("jumble_second"));
		ensureTiers(hero);
		for (int tier = 0; tier < Talent.MAX_TALENT_TIERS; tier++) {
			LinkedHashMap<Talent, Integer> talents = hero.talents.get(tier);
			talents.clear();
			for (String name : bundle.getStringArray("jumble_tier_" + tier)) {
				Talent talent = Talent.byName(name);
				if (talent != null) talents.put(talent, 0);
			}
		}
		return true;
	}

	public static void migrate(Hero hero) {
		hero.jumbleLoadout = hero.heroClass;
		ArrayList<LinkedHashMap<Talent, Integer>> previous = new ArrayList<>(hero.talents);
		hero.heroClass = HeroClasses.JUMBLE;
		hero.skin = 0;
		hero.talents.clear();
		hero.metamorphedTalents.clear();
		initClass(hero);
		if (hero.subClass != HeroSubClasses.NONE) {
			hero.subClass = HeroSubClasses.JUMBLE;
			initSubclass(hero);
		}
		if (hero.powerOfImp) initImp(hero);
		else if (hero.armorAbility != null) Talent.initArmorTalents(hero);
		for (int tier = 0; tier < Math.min(previous.size(), hero.talents.size()); tier++) {
			if (tier == 3 && !hero.powerOfImp && hero.armorAbility != null) {
				hero.talents.set(tier, previous.get(tier));
				continue;
			}
			ArrayList<Integer> points = new ArrayList<>(previous.get(tier).values());
			int index = 0;
			for (Talent talent : hero.talents.get(tier).keySet()) {
				if (index < points.size()) hero.talents.get(tier).put(talent, Math.min(points.get(index), talent.maxPoints()));
				index++;
			}
		}
	}
}
