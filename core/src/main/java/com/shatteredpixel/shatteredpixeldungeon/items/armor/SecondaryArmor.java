/*
 * Radish Pixel Dungeon
 *
 * 次要护甲：穿在主要护甲之外的一件附加装备，只提供少量格挡，并附带一个特殊效果。
 * 无法附魔、无法打磨；成长固定为 0/0/0/1/1/1/1/1/2（+1 到 +8 时的格挡增量）。
 */

package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public abstract class SecondaryArmor extends EquipableItem {

	/** 等级成长：+1..+8 时格挡值的增量 */
	private static final int[] GROWTH = {0, 0, 0, 1, 1, 1, 1, 1, 2};

	public int tier;
	public int blockMin;
	public int blockMax;
	public int strReq;
	public int level = 0;

	protected SecondaryArmor(int tier, int blockMin, int blockMax, int strReq) {
		this.tier = tier;
		this.blockMin = blockMin;
		this.blockMax = blockMax;
		this.strReq = strReq;
	}

	public int growth() {
		return GROWTH[Math.min(Math.max(level, 0), GROWTH.length - 1)];
	}

	public int DRMin() {
		return blockMin + growth();
	}

	public int DRMax() {
		return blockMax + growth();
	}

	public int STRReq() {
		return strReq;
	}

	@Override
	public int level() {
		return level;
	}

	@Override
	public boolean isUpgradable() {
		// 次要护甲无法打磨
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public boolean isEquipped(Hero hero) {
		return hero.belongings.secondaryArmor() == this;
	}

	/** 是否因为力量不足而让特殊效果失效 */
	public boolean powerShort(Hero hero) {
		return hero.STR() < STRReq();
	}

	/** 已经正常装备时，超力惩罚（相当于主要护甲 50%） */
	public void applyOverweightPenalty(Hero hero) {
		// 具体惩罚在 Hero 的回避/速度结算里按“次要护甲超力”统一处理，这里只留钩子
	}

	@Override
	public boolean doEquip(Hero hero) {
		if (hero.belongings.secondaryArmor == null
				|| hero.belongings.secondaryArmor.doUnequip(hero, true, false)) {
			hero.belongings.secondaryArmor = this;
			// 装到次要护甲栏之后必须从背包里摘掉，否则背包里会同时留着一份（还带着“已装备”的高亮框）
			detach( hero.belongings.backpack );
			GLog.i(Messages.get(this, "equipped", name()));
			return true;
		}
		return false;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (hero.belongings.secondaryArmor == this) {
			hero.belongings.secondaryArmor = null;
		}
		if (collect) {
			this.collect();
		}
		return true;
	}

	/**
	 * 效果描述里要显示的当前等级数值，按顺序对应 effect_desc 里的 %d。
	 * 子类覆写；没有数值的子类保持空数组。
	 */
	public Object[] effectArgs() {
		return new Object[0];
	}

	@Override
	public String desc() {
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.get(this, "base_desc", tier, (int)DRMin(), (int)DRMax(), STRReq()));
		// 力量不足那段只在真的不满足力量需求时显示
		if (Dungeon.hero != null && Dungeon.hero.STR() < STRReq()) {
			sb.append("\n\n").append(Messages.get(this, "strength_penalty"));
		}
		sb.append("\n\n");
		sb.append(Messages.get(this, "flavor"));
		sb.append("\n\n");
		sb.append(Messages.get(this, "effect_desc", effectArgs()));
		if (level > 0) {
			sb.append("\n\n").append(Messages.get(this, "level_desc", level));
		}
		if (level <= 0) {
			sb.append("

").append(Messages.get(this, "level_desc_zero"));
		}
		return sb.toString();
	}

	private static final String LEVEL = "level";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEVEL, level);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(LEVEL)) level = bundle.getInt(LEVEL);
	}
}
