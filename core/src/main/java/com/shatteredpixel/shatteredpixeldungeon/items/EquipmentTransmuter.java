package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfConcealment;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Wheelchair;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * 杂散变身时负责已装备与背包内非职业专属装备的随机变换，保留等级与 BUC 状态并重新随机词条。
 */
public final class EquipmentTransmuter {

	private EquipmentTransmuter() { }

	public static void transmuteAll(Hero hero) {
		Artifact artifact = hero.belongings.artifact();
		if (artifact != null && !artifact.isClassSpecific()) {
			transmuteArtifact(hero, artifact);
		}

		Ring ring = hero.belongings.ring();
		if (ring != null) {
			boolean ringTypeKnown = ring.isKnown();
			Item result = ScrollOfTransmutation.changeItem(ring);
			if (result instanceof Ring) {
				if (ringTypeKnown) ((Ring) result).setKnown();
				result.cursed = ring.cursed;
				result.cursedKnown = ring.cursedKnown;
				result.levelKnown = ring.levelKnown;
			}
			replaceEquipped(hero, ring, result);
		}

		KindOfWeapon weapon = hero.belongings.weapon();
		if (weapon != null && !weapon.isClassSpecific()) {
			Item result = ScrollOfTransmutation.changeItem(weapon);
			if (weapon instanceof Weapon && result instanceof Weapon) {
				rerollWeaponEnchant((Weapon) weapon, (Weapon) result);
			}
			replaceEquipped(hero, weapon, result);
		}

		Armor armor = hero.belongings.armor();
		if (armor != null) {
			Armor replacement = createTransmutedArmor(armor);
			if (replacement != null) replaceEquipped(hero, armor, replacement);
		}

		for (Item item : new ArrayList<>(hero.belongings.backpack.items)) {
			if (item.isClassSpecific()) continue;
			if (item instanceof Armor) {
				Armor replacement = createTransmutedArmor((Armor) item);
				if (replacement != null) {
					item.detachAll(hero.belongings.backpack);
					replacement.collect(hero.belongings.backpack);
				}
			} else if (item instanceof MeleeWeapon || item instanceof MissileWeapon || item instanceof Wand) {
				Item result = ScrollOfTransmutation.changeItem(item);
				if (result != null && result != item) {
					if (item instanceof Weapon && result instanceof Weapon) {
						rerollWeaponEnchant((Weapon) item, (Weapon) result);
					}
					item.detachAll(hero.belongings.backpack);
					result.collect(hero.belongings.backpack);
				}
			}
		}
	}

	private static void transmuteArtifact(Hero hero, Artifact old) {
		if (hero.jumbleLoadout == HeroClasses.MOONLIGHT && old instanceof Wheelchair) return;
		if (hero.jumbleLoadout == HeroClasses.ROGUE && old instanceof CloakOfConcealment) return;

		Item replacement = ScrollOfTransmutation.changeItem(old);
		if (replacement != null && replacement != old) {
			replaceEquipped(hero, old, replacement);
		}
	}

	public static Armor createTransmutedArmor(Armor old) {
		int tier = old.tier;
		if (tier < 1 || tier > 5) return null;

		Generator.Category cat = Generator.armTiers[tier - 1];
		Armor replacement;
		do {
			replacement = (Armor) Generator.randomUsingDefaults(cat);
		} while (replacement.getClass() == old.getClass() && cat.classes.length > 1);
		if (replacement != null) {
			replacement.level(0);
			int level = old.trueLevel();
			if (level > 0) replacement.upgrade(level);
			else if (level < 0) replacement.degrade(-level);
			replacement.levelKnown = old.levelKnown;
			replacement.cursedKnown = old.cursedKnown;
			replacement.cursed = old.cursed;
			rerollArmorGlyph(old, replacement);
			replacement.curseInfusionBonus = old.curseInfusionBonus;
			replacement.glyphHardened = old.glyphHardened;
			replacement.masteryPotionBonus = old.masteryPotionBonus;
			replacement.augment = old.augment;
		}
		return replacement;
	}

	public static void rerollWeaponEnchant(Weapon oldW, Weapon newW) {
		if (oldW.hasCurseEnchant()) {
			newW.enchant(Weapon.Enchantment.randomCurse());
		} else if (oldW.hasGoodEnchant()) {
			newW.enchant();
		} else {
			newW.enchant(null);
		}
	}

	public static void rerollArmorGlyph(Armor oldA, Armor newA) {
		if (oldA.hasCurseGlyph()) {
			newA.inscribe(Armor.Glyph.randomCurse());
		} else if (oldA.hasGoodGlyph()) {
			newA.inscribe();
		} else {
			newA.inscribe(null);
		}
	}

	public static void replaceEquipped(Hero hero, Item oldItem, Item newItem) {
		if (oldItem == null || newItem == null || oldItem == newItem) return;

		int slot = Dungeon.quickslot.getSlot(oldItem);
		if (oldItem instanceof EquipableItem && ((EquipableItem) oldItem).isEquipped(hero)) {
			((EquipableItem) oldItem).doUnequip(hero, false);
			if (newItem instanceof EquipableItem) {
				((EquipableItem) newItem).doEquip(hero);
			} else if (!newItem.collect(hero.belongings.backpack)) {
				Dungeon.level.drop(newItem, hero.pos);
			}
		} else {
			oldItem.detachAll(hero.belongings.backpack);
			if (!newItem.collect(hero.belongings.backpack)) {
				Dungeon.level.drop(newItem, hero.pos);
			}
		}

		if (slot != -1) {
			Dungeon.quickslot.setSlot(slot, newItem);
			Item.updateQuickslot();
		}
	}
}
