package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.HeroicLeap;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Shockwave;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;

/**
 * 宿傩（Ryomen Sukuna）。
 * 目前复用战士的精灵与天赋，只替换职业名称与描述，后续再逐步做专属能力。
 */
public class Sukuna extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.SUKUNA; }

	//复用战士的美术资源
	@Override public String spritesheet() { return Assets.Sprites.SUKUNA; }
	@Override public String splashArt() { return Assets.Splashes.SUKUNA; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_SUKUNA; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.BERSERKER, HeroSubClasses.GLADIATOR, HeroSubClasses.MAGIC_WARRIOR };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new HeroicLeap(), new Shockwave(), new Endure() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.HEARTY_MEAL, Talent.ARMSMASTERS_INTUITION, Talent.PROVOKED_ANGER, Talent.IRON_WILL });
		set.setTier2(new Talent[]{ Talent.IRON_STOMACH, Talent.EMERGENCY_PROTECTION, Talent.RUNIC_TRANSFERENCE, Talent.LETHAL_MOMENTUM, Talent.IMPROVISED_PROJECTILES });
		set.setTier3(new Talent[]{ Talent.HOLD_FAST, Talent.STRONGMAN });
		set.setTier4(new Talent[]{ Talent.IRON_MUSCLE, Talent.HIGH_DIET });
		return set;
	}

	@Override protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new WornShortsword()).identify();
		ThrowingStone stones = new ThrowingStone();
		stones.quantity(3).collect();
		Dungeon.quickslot.setSlot(0, stones);

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
		}

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}
}