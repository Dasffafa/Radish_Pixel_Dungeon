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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;

/** 宿傩，暂复用战士的精灵与天赋。 */
public class Sukuna extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.SUKUNA; }

	@Override public String spritesheet() { return Assets.Sprites.SUKUNA; }
	@Override public String splashArt() { return Assets.Splashes.SUKUNA; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_SUKUNA; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.QIANREN, HeroSubClasses.DUANZUI, HeroSubClasses.HUISHENG };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna.SpaceSlash(), new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna.MalevolentShrine(), new com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sukuna.Fuga() };
	}

	@Override public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{ Talent.SUKUNA_FEAST, Talent.CURSE_MASTER, Talent.CURSED_SURGE, Talent.COMBAT_INSTINCT });
		set.setTier2(new Talent[]{ Talent.SUKUNA_CLAW_MASTERY, Talent.SUKUNA_REACH, Talent.SUKUNA_LOW_HP, Talent.BLOODLUST, Talent.IRON_BODY });
		set.setTier3(new Talent[]{ Talent.SUKUNA_CLEAVE, Talent.SUKUNA_DISMANTLE });
		set.setTier4(new Talent[]{ Talent.SHRINE_MASTERY, Talent.ECHO_STRIKE });
		return set;
	}

	@Override protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SukunaClaws()).identify();
		hero.belongings.armor = new com.shatteredpixel.shatteredpixeldungeon.items.armor.CurseKimono();
		com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CursedEnergy.class);
		com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Senbon senbon =
				new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Senbon();
		senbon.quantity(2).collect();
		Dungeon.quickslot.setSlot(0, senbon);

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}
}