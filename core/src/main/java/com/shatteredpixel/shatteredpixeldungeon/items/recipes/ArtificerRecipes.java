package com.shatteredpixel.shatteredpixeldungeon.items.recipes;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * 奇械师的炼金配方：把最多 3 件投掷武器 / 药剂合成 1 件。
 *
 * <p>投掷武器按「力量需求最高、攻速最慢」的那一件作为结算结果；
 * 药剂按价值最高的一瓶作为结果。</p>
 */
public final class ArtificerRecipes {

	private ArtificerRecipes() {
	}

	private static boolean isArtificer() {
		return Dungeon.hero != null
				&& Dungeon.hero.subClass == HeroSubClasses.ARTIFICER;
	}

	private static Item copyOf(Item src) {
		Item copy = Reflection.newInstance(src.getClass());
		if (copy == null) return null;
		copy.quantity(1);
		copy.identify();
		return copy;
	}

	public static class MissileCombine extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (!isArtificer() || ingredients.isEmpty() || ingredients.size() > 3) return false;
			if (ingredients.size() == 1 && ingredients.get(0).quantity() < 2) return false;
			for (Item i : ingredients) {
				if (!(i instanceof MissileWeapon) || !i.isIdentified()) return false;
			}
			return true;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					? Math.min(3, ingredients.get(0).quantity())
					: ingredients.size();
		}

		/** 力量需求最高优先；同力量时取攻速最慢（delay 最大）的。 */
		private MissileWeapon best(ArrayList<Item> ingredients) {
			Hero hero = Dungeon.hero;
			MissileWeapon best = null;
			for (Item i : ingredients) {
				MissileWeapon m = (MissileWeapon) i;
				if (best == null) {
					best = m;
					continue;
				}
				int s = m.STRReq(m.buffedLvl()), bs = best.STRReq(best.buffedLvl());
				if (s > bs) {
					best = m;
				} else if (s == bs && m.delayFactor(hero) > best.delayFactor(hero)) {
					best = m;
				}
			}
			return best;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			return copyOf(best(ingredients));
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			MissileWeapon best = best(ingredients);
			Item out = copyOf(best);
			if (ingredients.size() == 1) {
				Item only = ingredients.get(0);
				only.quantity(Math.max(0, only.quantity() - Math.min(3, only.quantity())));
			} else {
				for (Item i : ingredients) i.quantity(i.quantity() - 1);
			}
			return out;
		}
	}

	public static class PotionCombine extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (!isArtificer() || ingredients.isEmpty() || ingredients.size() > 3) return false;
			if (ingredients.size() == 1 && ingredients.get(0).quantity() < 2) return false;
			for (Item i : ingredients) {
				if (!(i instanceof Potion) || !i.isIdentified()) return false;
			}
			return true;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					? Math.min(3, ingredients.get(0).quantity())
					: ingredients.size();
		}

		private Potion best(ArrayList<Item> ingredients) {
			Potion best = null;
			for (Item i : ingredients) {
				Potion p = (Potion) i;
				if (best == null || p.value() > best.value()) best = p;
			}
			return best;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			return copyOf(best(ingredients));
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			Potion best = best(ingredients);
			Item out = copyOf(best);
			if (ingredients.size() == 1) {
				Item only = ingredients.get(0);
				only.quantity(Math.max(0, only.quantity() - Math.min(3, only.quantity())));
			} else {
				for (Item i : ingredients) i.quantity(i.quantity() - 1);
			}
			return out;
		}
	}
}
