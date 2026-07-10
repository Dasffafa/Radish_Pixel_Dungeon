/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight.SharpeningEdgeTalent;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CancelAttackBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CancelAttackCooldown;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.KickTracker;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.JutteChampionWeapon;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.Muramasa;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;
import java.util.Objects;

abstract public class KindOfWeapon extends EquipableItem {

	public static final String AC_SHARPENING_EDGE = "SHARPENING_EDGE";
	public static final String AC_CONVERT_TO_JUTTE = "CONVERT_TO_JUTTE";

	protected String hitSound = Assets.Sounds.HIT;
	protected float hitSoundPitch = 1f;

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		// 砥砺锋芒天赋：只有月华英雄且有天赋时显示
		if (SharpeningEdgeTalent.canUse(hero, this)) {
			actions.add(AC_SHARPENING_EDGE);
		}
		// 十手冠军：所有武器都可以转换为十手（但十手本身不能再转化）
		if (hero.subClass == HeroSubClass.JUTTE_CHAMPION && !(this instanceof JutteChampionWeapon)) {
			actions.add(AC_CONVERT_TO_JUTTE);
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (Objects.equals(action, AC_SHARPENING_EDGE)) {
			return Messages.get(KindOfWeapon.class, "ac_" + action);
		}
		if (Objects.equals(action, AC_CONVERT_TO_JUTTE)) {
			return Messages.get(KindOfWeapon.class, "ac_" + action);
		}
		return super.actionName(action, hero);
	}


	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_SHARPENING_EDGE)) {
			usesTargeting = false;
			SharpeningEdgeTalent.showTargetSelectionWindow(hero, this);
		} else if (action.equals(AC_CONVERT_TO_JUTTE)) {
			convertToJutte(hero);
		} else if (hero.subClass == HeroSubClass.CHAMPION && action.equals(AC_EQUIP)){
			usesTargeting = false;
			String primaryName = Messages.titleCase(hero.belongings.weapon != null ? hero.belongings.weapon.trueName() : Messages.get(KindOfWeapon.class, "empty"));
			String secondaryName = Messages.titleCase(hero.belongings.secondWep != null ? hero.belongings.secondWep.trueName() : Messages.get(KindOfWeapon.class, "empty"));
			if (primaryName.length() > 18) primaryName = primaryName.substring(0, 15) + "...";
			if (secondaryName.length() > 18) secondaryName = secondaryName.substring(0, 15) + "...";
			GameScene.show(new WndOptions(
					new ItemSprite(this),
					Messages.titleCase(name()),
					Messages.get(KindOfWeapon.class, "which_equip_msg"),
					Messages.get(KindOfWeapon.class, "which_equip_primary", primaryName),
					Messages.get(KindOfWeapon.class, "which_equip_secondary", secondaryName)
			){
				@Override
				protected void onSelect(int index) {
					super.onSelect(index);
					if (index == 0 || index == 1){
						//In addition to equipping itself, item reassigns itself to the quickslot
						//This is a special case as the item is being removed from inventory, but is staying with the hero.
						int slot = Dungeon.quickslot.getSlot( KindOfWeapon.this );
						slotOfUnequipped = -1;
						if (index == 0) {
							doEquip(hero);
						} else {
							equipSecondary(hero);
						}
						if (slot != -1) {
							Dungeon.quickslot.setSlot( slot, KindOfWeapon.this );
							updateQuickslot();
						//if this item wasn't quickslotted, but the item it is replacing as equipped was
						//then also have the item occupy the unequipped item's quickslot
						} else if (slotOfUnequipped != -1 && defaultAction() != null) {
							Dungeon.quickslot.setSlot( slotOfUnequipped, KindOfWeapon.this );
							updateQuickslot();
						}
					}
				}
			});
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.weapon() == this || hero.belongings.secondWep() == this;
	}

	private static boolean isSwiftEquipping = false;

	protected float timeToEquip( Hero hero ) {
		return isSwiftEquipping ? 0f : super.timeToEquip(hero);
	}
	
	@Override
	public boolean doEquip( Hero hero ) {

		// func 4 Muramasa mania
		// DoggingDog on 20250419
		if(Dungeon.hero.buff(Muramasa.MuramasaMania.class)!=null && Dungeon.hero!=null){
			GLog.n(Messages.get(Muramasa.MuramasaMania.class,"mania"));
			return false;
		}
		//


		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)){
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()){
				isSwiftEquipping = true;
			}
		}

		if (hero.heroClass == HeroClass.MOONLIGHT){
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null){
				isSwiftEquipping = true;
			}
		}

		detachAll( hero.belongings.backpack );
		
		if (hero.belongings.weapon == null || hero.belongings.weapon.doUnequip( hero, true )) {
			
			hero.belongings.weapon = this;
			activate( hero );
			Talent.onItemEquipped(hero, this);
			updateQuickslot();

			// 小骑士切换武器：给予取消攻击buff
			if (hero.subClass == HeroSubClass.LITTLE_KNIGHT) {
				// 检查冷却是否结束
				if (hero.buff(CancelAttackCooldown.class) == null) {
					Buff.affect(hero, CancelAttackBuff.class, 2f);
					Buff.affect(hero, CancelAttackCooldown.class, CancelAttackCooldown.getDuration());
					GLog.p(Messages.get("actors.hero.moonlight.cancel_attack_gained"));
				}
				// 检测踹飞技能
				KickTracker.checkKick(hero);
			}

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
			}

			hero.spendAndNext( timeToEquip(hero) );
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null){
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;
			
		} else {
			isSwiftEquipping = false;
			collect( hero.belongings.backpack );
			return false;
		}
	}

	public boolean equipSecondary( Hero hero ){

		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)){
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()){
				isSwiftEquipping = true;
			}
		}

		boolean wasInInv = hero.belongings.contains(this);
		detachAll( hero.belongings.backpack );

		if (hero.belongings.secondWep == null || hero.belongings.secondWep.doUnequip( hero, true )) {

			hero.belongings.secondWep = this;
			activate( hero );
			Talent.onItemEquipped(hero, this);
			updateQuickslot();

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
			}

			hero.spendAndNext( timeToEquip(hero) );
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null){
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;

		} else {
			isSwiftEquipping = false;
			collect( hero.belongings.backpack );
			return false;
		}
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {

		// func 4 Muramasa mania
		// DoggingDog on 20250419
		if(Dungeon.hero.buff(Muramasa.MuramasaMania.class)!=null && Dungeon.hero!=null){
			GLog.n(Messages.get(Muramasa.MuramasaMania.class,"mania"));
			return false;
		}
		//

		boolean second = hero.belongings.secondWep == this;

		if (second){
			//do this first so that the item can go to a full inventory
			hero.belongings.secondWep = null;
		}

		if (super.doUnequip( hero, collect, single )) {

			if (!second){
				hero.belongings.weapon = null;
			}
			return true;

		} else {

			if (second){
				hero.belongings.secondWep = this;
			}
			return false;

		}
	}

	public int min(){
		return min(buffedLvl());
	}

	public int max(){
		return max(buffedLvl());
	}

	abstract public int min(int lvl);
	abstract public int max(int lvl);

	public int damageRoll( Char owner ) {
		return Char.combatRoll( min(), max() );
	}
	
	public float accuracyFactor( Char owner, Char target ) {
		return 1f;
	}
	
	public float delayFactor( Char owner ) {
		return 1f;
	}

	public int reachFactor( Char owner ){
		return 1;
	}
	
	public boolean canReach( Char owner, int target){
		int reach = reachFactor(owner);
		if (Dungeon.level.distance( owner.pos, target ) > reach){
			return false;
		} else {
			boolean[] passable = BArray.not(Dungeon.level.solid, null);
			for (Char ch : Actor.chars()) {
				if (ch != owner) passable[ch.pos] = false;
			}
			
			PathFinder.buildDistanceMap(target, passable, reach);
			
			return PathFinder.distance[owner.pos] <= reach;
		}
	}

	public int defenseFactor( Char owner ) {
		return 0;
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		return damage;
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(hitSound, 1, pitch * hitSoundPitch);
	}

	/**
	 * 在攻击前调用，检查武器是否可以攻击
	 * @param attacker 攻击者
	 * @param defender 防御者
	 * @param action 攻击动作
	 * @return 如果可以攻击返回 true，如果需要等待返回 false
	 */
	public boolean actAttack(Hero attacker, Char defender, HeroAction.Attack action) {
		return true;
	}

	@Override
	public String desc() {
		String desc = super.desc();

		// 剑盾骑士天赋：显示护甲最小值提升
		if (Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.MOONLIGHT) {
			int points = Dungeon.hero.pointsInTalent(Talent.SWORD_SHIELD_KNIGHT);
			if (points > 0 && Dungeon.hero.belongings.armor != null) {
				// 计算天赋加成后的护甲最小值
				float multiplier = 1.0f + (points - 1) * 0.25f;
				int talentArmorMin = Math.round(min() * multiplier);
				int armorMax = Dungeon.hero.belongings.armor.DRMax();
				int actualMin = Math.min(talentArmorMin, armorMax);
				desc += "\n\n" + Messages.get(KindOfWeapon.class, "sword_shield_knight", actualMin);
			}
		}

		return desc;
	}

	/**
	 * 十手冠军：将当前武器转换为十手
	 */
	private void convertToJutte(Hero hero) {
		// 计算阶数（基于武器等级）
		int tier = Math.min(5, Math.max(1, level() + 1));

		// 创建十手并立即鉴定
		JutteChampionWeapon jutte = new JutteChampionWeapon(tier);
		jutte.identify();

		// 精铁淬炼天赋：让十手获得升级等级并返还升级卷轴
		int originalLevel = level();
		if (originalLevel > 0 && hero.hasTalent(Talent.IRON_QUENCH)) {
			// 十手获得升级等级（等于原武器的升级数）
			jutte.level(originalLevel);

			// 返还一张升级卷轴
			ScrollOfUpgrade scroll = new ScrollOfUpgrade();
			scroll.identify().collect();
			GLog.p(Messages.get(KindOfWeapon.class, "jutte_return_scroll"));
		}

		// 卸下当前武器
		if (isEquipped(hero)) {
			doUnequip(hero, true, true);
		}
		detach(hero.belongings.backpack);

		// 将十手放入背包
		jutte.collect();
		GLog.p(Messages.get(KindOfWeapon.class, "jutte_converted", tier));
	}

}
