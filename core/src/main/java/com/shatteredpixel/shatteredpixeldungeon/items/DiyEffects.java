/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 常驻效果目录（本文件由 tools/gen_diy_effects.py 生成，别手改）
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

/** id ↔ 常驻 buff 类的对照表。 */
public final class DiyEffects {

	private DiyEffects() {
	}

	/** 全部可选的常驻效果 id。 */
	public static final String[] IDS = {
			"adrenaline",
			"blob_immunity",
			"calm",
			"critical_attack",
			"enhanced_rings",
			"foresight",
			"levitation",
			"magical_sight",
			"mastery",
			"mindvision",
			"monk_energy",
			"physical_empower",
			"poem",
			"revealed_area",
			"stamina",
			"wand_empower",
			"wheelchair_rush",
			"healing",
			"well_fed",
			"barkskin",
			"arcane_armor",
			"earthen_armor",
			"fire_imbue",
			"frost_imbue",
			"toxic_imbue",
			"hold_fast",
			"fury",
			"adrenaline_surge",
			"brace_yourself",
			"lethal_defense",
			"magic_soothe",
			"sn_d_regen",
			"artifact_recharge",
	};

	/** 与 {@link #IDS} 一一对应的 buff 类。 */
	@SuppressWarnings("unchecked")
	public static final Class<? extends Buff>[] CLASSES = new Class[]{
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Calm.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CriticalAttack.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Mastery.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PoemBuff.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WheelchairRush.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WellFed.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EarthenArmorGuard.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ToxicImbue.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HoldFast.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AdrenalineSurge.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BraceYourself.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LethalDefense.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicSootheRegen.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnDRegeneration.class,
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge.class,
	};

	/** 按 id 找 buff 类，找不到返回 null。 */
	public static Class<? extends Buff> byId(String id) {
		if (id == null) return null;
		for (int i = 0; i < IDS.length; i++) {
			if (IDS[i].equals(id)) return CLASSES[i];
		}
		return null;
	}

	/** 这个 id 是不是常驻效果。 */
	public static boolean isEffect(String id) {
		return byId(id) != null;
	}
}
