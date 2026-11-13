package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.heroes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.Detonator;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.ForceField;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.engineer.SummoningBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.engineer.EngineerPassive;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MultiTool;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand;

/** 工程师 - 自 reXuns Pixel Dungeon (xun分支) 移植 */
public class Engineer extends HeroDefinition {

	@Override
	public HeroClass heroClass() {
		return HeroClasses.ENGINEER;
	}

	@Override
	public String spritesheet() {
		return Assets.Sprites.H_ENGINEER;
	}

	@Override
	public String splashArt() {
		return spritesheet();
	}

	@Override
	public String avatarSkin() {
		return Assets.Sprites.AVATARS_ENGINEER;
	}

	@Override
	public HeroSubClass[] subClasses() {
		return new HeroSubClass[]{ HeroSubClasses.CRAFTSMAN, HeroSubClasses.HACKER, HeroSubClasses.GRENADIER };
	}

	@Override
	public ArmorAbility[] armorAbilities() {
		return new ArmorAbility[]{ new ForceField(), new SummoningBeacon(), new Detonator() };
	}

	@Override
	public TalentSet talents() {
		TalentSet set = new TalentSet();
		set.setTier1(new Talent[]{
				Talent.RATION_PLANNING, Talent.FINE_INTUITION,
				Talent.GENERAL_DISARM, Talent.PULSE_ENERGY });
		set.setTier2(new Talent[]{
				Talent.TOILSOME_MEAL, Talent.SCROLL_VIGILANCE, Talent.STRONG_PULSE,
				Talent.DISMANTLE_URGE, Talent.FAVORITE_WORK });
		set.setTier3(new Talent[]{
				Talent.COORDINATED_ACTION, Talent.ELECTRONIC_REPAIR });
		//四阶通用天赋；小恶魔「激发潜力」开放四阶后才出现，与护甲技能二选一
		set.setTier4(new Talent[]{
				Talent.ATTACHED_PULSE, Talent.INSTANT_REPAIR });
		return set;
	}

	@Override
	protected void initClassLoadout(Hero hero) {
		(hero.belongings.weapon = new MultiTool()).identify();
		hero.belongings.weapon.activate(hero);
		Dungeon.quickslot.setSlot(0, hero.belongings.weapon);

		ThrowingSpike spikes = new ThrowingSpike();
		spikes.quantity(2).collect();
		Dungeon.quickslot.setSlot(1, spikes);

		//常驻状态：对机械敌人的被动、每回合驱动已骇入陷阱等都挂在它上面
		Buff.affect(hero, EngineerPassive.class);
		PulseWand.ensureGranted(hero);

		new PotionOfParalyticGas().identify();
		new ScrollOfRecharging().identify();
	}
}
