package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.JumbleChangeBuff;
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
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.JumbleSprite;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Jumble extends HeroDefinition {

	@Override public HeroClass heroClass() { return HeroClasses.JUMBLE; }
	@Override public String spritesheet() { return Assets.Sprites.JUMBLE; }
	@Override public String splashArt() { return Assets.Splashes.WARRIOR; }
	@Override public String avatarSkin() { return Assets.Sprites.AVATARS_WARRIOR; }
	@Override public boolean customSprite() { return true; }
	@Override public String asset() { return Assets.Sprites.JUMBLE; }
	@Override public Class<? extends HeroSprite> spriteClass() { return JumbleSprite.class; }
	@Override public int frameW() { return 12; }
	@Override public int frameH() { return 16; }
	@Override public int[] idleFrames() { return new int[]{ 0, 0, 0, 1, 0, 0, 1, 1 }; }
	@Override public boolean showGrass() { return false; }

	@Override public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.JUMBLE };
	}

	@Override public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new HeroicLeap(), new Shockwave(), new Endure() };
	}

	@Override public TalentSet talents() {
		return new TalentSet().setTier1(new Talent[0]).setTier2(new Talent[0])
				.setTier3(new Talent[0]).setTier4(new Talent[0]);
	}

	@Override public void initHero(Hero hero) {
		HeroClass loadout = Random.oneOf(HeroClasses.WARRIOR, HeroClasses.MAGE, HeroClasses.ROGUE,
				HeroClasses.HUNTRESS, HeroClasses.RECTOR, HeroClasses.MOONLIGHT, HeroClasses.CARDTRICKSTER);
		loadout.definition().initHero(hero);
		if (loadout == HeroClasses.MOONLIGHT) {
			hero.HT += 2;
			hero.HP += 2;
		}
		hero.jumbleLoadout = loadout;
		hero.heroClass = HeroClasses.JUMBLE;
		hero.skin = 0;
		hero.talents.clear();
		hero.metamorphedTalents.clear();
		Talent.initClassTalents(hero);
		JumbleChangeBuff.resetCountdownIfMissing();
	}
}
