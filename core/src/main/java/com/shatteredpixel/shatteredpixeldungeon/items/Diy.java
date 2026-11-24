/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 拆解与组装的规则
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.Game;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Calm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CriticalAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyEffectsTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyIdentify;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyItemPulse;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyReveal;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiySearch;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyWandCharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyWard;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyRegen;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnemyFlag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Morello;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Rlyeh;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.ShadowBooks;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Taijutsu;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WhiteKingGodSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * DIY 工作台的拆解/组装规则。
 *
 * <p>拆解一件物品会拆出好几样东西：</p>
 * <ul>
 *   <li>基础材料（铁件 / 魔尘 / 杂料）：按物品的类别与阶位，用来付组装的底价钱</li>
 *   <li>强化精华：物品每有 1 级就给 1 份，一份能在组装时换 +1 级</li>
 *   <li>附魔精粹：物品身上每一个附魔 / 铭文各拆出一份（DIY 贴上去的也会还回来）</li>
 *   <li>特性精粹：白帝圣剑那几把特殊武器的被动，以及 DIY 贴上去的特性</li>
 * </ul>
 *
 * <p>组装没有数量限制：想加多少级就加多少级，附魔精粹和特性精粹都能一次贴多个，
 * 成品会同时拥有这些附魔/铭文的全部效果（见 {@link Weapon#diyEnchantments()}、
 * {@link Armor#diyGlyphs()}）。</p>
 *
 * <p>经济上：基础材料的要价 = 拆解产出 × 1.5，而强化等级不再计入基础材料，
 * 所以「拆 → 装 → 拆」既不会凭空多出材料，也不会丢掉等级。</p>
 */
public final class Diy {

	private Diy() {
	}

	// ===== 背包遍历 =====

	/** 背包 + 各种收纳包里的全部物品（含包本身）。 */
	public static ArrayList<Item> allItems() {
		ArrayList<Item> out = new ArrayList<>();
		if (Dungeon.hero == null) {
			return out;
		}
		collectInto(Dungeon.hero.belongings.backpack, out);
		return out;
	}

	private static void collectInto(Bag bag, ArrayList<Item> out) {
		if (bag == null) return;
		for (Item it : bag.items.toArray(new Item[0])) {
			out.add(it);
			if (it instanceof Bag) {
				collectInto((Bag) it, out);
			}
		}
	}

	public static boolean hasWorkbench() {
		for (Item it : allItems()) {
			if (it instanceof DiyWorkbench) return true;
		}
		return false;
	}

	// ===== 基础材料 =====

	public static int kindOf(Item it) {
		if (it instanceof KindOfWeapon
				|| it instanceof Armor
				|| it instanceof SecondaryArmor) {
			return DiyMaterial.METAL;
		}
		if (it instanceof Wand
				|| it instanceof Ring
				|| it instanceof Artifact
				|| it instanceof Trinket
				|| it instanceof Potion
				|| it instanceof Scroll
				|| it instanceof Spell
				|| it instanceof Runestone) {
			return DiyMaterial.ARCANE;
		}
		return DiyMaterial.MISC;
	}

	public static String materialName(int kind) {
		return Messages.get(DiyMaterial.class, "name_" + DiyMaterial.KIND_KEYS[DiyMaterial.normalize(kind)]);
	}

	/** 物品的阶位：武器/护甲用 tier，法杖戒指神器按 3 算，其它按 1 算。 */
	private static int gradeOf(Item it) {
		if (it instanceof MeleeWeapon) {
			return Math.max(1, ((MeleeWeapon) it).tier);
		}
		if (it instanceof MissileWeapon) {
			return Math.max(1, ((MissileWeapon) it).tier);
		}
		if (it instanceof Armor) {
			return Math.max(1, ((Armor) it).tier);
		}
		if (it instanceof SecondaryArmor) {
			return Math.max(1, ((SecondaryArmor) it).tier);
		}
		if (it instanceof Wand || it instanceof Ring || it instanceof Artifact) {
			return 3;
		}
		return 1;
	}

	/** 基础材料产出（不计强化等级——等级单独拆成强化精华）。 */
	public static int baseYieldOf(Item it) {
		int grade = gradeOf(it);
		switch (kindOf(it)) {
			case DiyMaterial.METAL:
				return Math.max(1, 2 * grade + 1);
			case DiyMaterial.ARCANE:
				return Math.max(1, 2 * grade);
			default:
				return 1;
		}
	}

	/** 单件物品组装要花的基础材料数量。 */
	public static int unitCost(Item it) {
		return Math.max(2, baseYieldOf(it) * 3 / 2);
	}

	/** 一整叠（投掷物这类一次出几件的）要花的基础材料数量。 */
	public static int costOf(Item it) {
		return unitCost(it) * Math.max(1, it.quantity());
	}

	public static String materialsLine() {
		return materialName(DiyMaterial.METAL) + " " + materialCount(DiyMaterial.METAL)
				+ "    " + materialName(DiyMaterial.ARCANE) + " " + materialCount(DiyMaterial.ARCANE)
				+ "    " + materialName(DiyMaterial.MISC) + " " + materialCount(DiyMaterial.MISC);
	}

	// ===== 背包里的材料 / 部件 =====

	public static int materialCount(int kind) {
		DiyWorkbench bench = workbench();
		if (bench != null) {
			absorbLooseMaterials(bench);
			return bench.materialCount(kind);
		}
		int total = 0;
		for (Item it : allItems()) {
			if (it instanceof DiyMaterial && ((DiyMaterial) it).kind() == kind) {
				total += it.quantity();
			}
		}
		return total;
	}

	public static boolean canAfford(int kind, int amount) {
		return materialCount(kind) >= amount;
	}

	public static boolean takeMaterials(int kind, int amount) {
		if (amount <= 0) return true;
		if (!canAfford(kind, amount)) return false;
		DiyWorkbench bench = workbench();
		if (bench != null) {
			return bench.takeMaterial(kind, amount);
		}
		int left = amount;
		for (Item it : allItems()) {
			if (!(it instanceof DiyMaterial)) continue;
			if (((DiyMaterial) it).kind() != kind) continue;
			int take = Math.min(left, it.quantity());
			it.quantity(it.quantity() - take);
			left -= take;
			if (it.quantity() <= 0) {
				it.detachAll(Dungeon.hero.belongings.backpack);
			}
			if (left <= 0) break;
		}
		return left <= 0;
	}

	/** 工作台里某种精粹的「payload → 数量」。 */
	public static LinkedHashMap<String, Integer> componentStacks(int kind) {
		DiyWorkbench bench = workbench();
		if (bench != null) {
			absorbLooseEssences(bench);
			return bench.essenceStacks(kind);
		}
		LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
		for (Item it : allItems()) {
			if (!(it instanceof DiyComponent)) continue;
			DiyComponent c = (DiyComponent) it;
			if (c.kind() != kind) continue;
			Integer old = out.get(c.payload());
			out.put(c.payload(), (old == null ? 0 : old) + c.quantity());
		}
		return out;
	}

	public static int componentCount(int kind, String payload) {
		String key = payload == null ? "" : payload;
		DiyWorkbench bench = workbench();
		if (bench != null) {
			absorbLooseEssences(bench);
			return bench.essenceCount(kind, key);
		}
		int total = 0;
		for (Item it : allItems()) {
			if (!(it instanceof DiyComponent)) continue;
			DiyComponent c = (DiyComponent) it;
			if (c.kind() != kind) continue;
			if (!c.payload().equals(key)) continue;
			total += it.quantity();
		}
		return total;
	}

	/** 某种精粹的总量（不区分 payload）。 */
	public static int componentTotal(int kind) {
		DiyWorkbench bench = workbench();
		if (bench != null) {
			absorbLooseEssences(bench);
			return bench.essenceTotal(kind);
		}
		int total = 0;
		for (Item it : allItems()) {
			if (it instanceof DiyComponent && ((DiyComponent) it).kind() == kind) {
				total += it.quantity();
			}
		}
		return total;
	}

	public static boolean takeComponents(int kind, String payload, int amount) {
		if (amount <= 0) return true;
		String key = payload == null ? "" : payload;
		if (componentCount(kind, key) < amount) return false;
		DiyWorkbench bench = workbench();
		if (bench != null) {
			return bench.takeEssence(kind, key, amount);
		}
		int left = amount;
		for (Item it : allItems()) {
			if (!(it instanceof DiyComponent)) continue;
			DiyComponent c = (DiyComponent) it;
			if (c.kind() != kind || !c.payload().equals(key)) continue;
			int take = Math.min(left, c.quantity());
			c.quantity(c.quantity() - take);
			left -= take;
			if (c.quantity() <= 0) {
				c.detachAll(Dungeon.hero.belongings.backpack);
			}
			if (left <= 0) break;
		}
		return left <= 0;
	}

	/** 工作台只在「智能怪物」挑战开局发放，其它途径都拿不到。 */
	public static boolean shouldGrantStarterWorkbench() {
		return Dungeon.isChallenged(Challenges.REAL_INTELLIGENCE);
	}

	/** 拿到英雄身上的工作台（精粹仓库）。 */
	public static DiyWorkbench workbench() {
		if (Dungeon.hero == null || Dungeon.hero.belongings == null) return null;
		return Dungeon.hero.belongings.getItem(DiyWorkbench.class);
	}

	/** 旧版本背包里的材料搬进工作台。 */
	private static void absorbLooseMaterials(DiyWorkbench bench) {
		if (bench == null || Dungeon.hero == null) return;
		for (Item it : allItems()) {
			if (!(it instanceof DiyMaterial)) continue;
			DiyMaterial m = (DiyMaterial) it;
			bench.addMaterial(m.kind(), m.quantity());
			m.quantity(0);
			m.detachAll(Dungeon.hero.belongings.backpack);
		}
	}

	/** 往工作台里加材料；没有工作台时才退回背包物品。 */
	public static void giveMaterial(int kind, int amount) {
		if (amount <= 0) return;
		DiyWorkbench bench = workbench();
		if (bench != null) {
			bench.addMaterial(kind, amount);
		} else {
			give(new DiyMaterial(kind, amount));
		}
	}

	/** 旧版本残留的背包精粹，第一次访问时搬进工作台。 */
	private static void absorbLooseEssences(DiyWorkbench bench) {
		if (bench == null || Dungeon.hero == null) return;
		for (Item it : allItems()) {
			if (!(it instanceof DiyComponent)) continue;
			DiyComponent c = (DiyComponent) it;
			bench.addEssence(c.kind(), c.payload(), c.quantity());
			c.quantity(0);
			c.detachAll(Dungeon.hero.belongings.backpack);
		}
	}

	/** 往工作台里加精粹；没有工作台时才退回背包物品。 */
	public static void giveEssence(int kind, String payload, int amount) {
		if (amount <= 0) return;
		DiyWorkbench bench = workbench();
		if (bench != null) {
			bench.addEssence(kind, payload, amount);
		} else {
			give(new DiyComponent(kind, payload, amount));
		}
	}

	/** 组装窗口顶部的部件汇总。 */
	public static String componentsLine() {
		return Messages.get(DiyComponent.class, "name_level") + " " + componentTotal(DiyComponent.LEVEL)
				+ "    " + Messages.get(DiyComponent.class, "name_enchant") + " " + componentTotal(DiyComponent.ENCHANT)
				+ "    " + Messages.get(DiyComponent.class, "name_passive") + " " + componentTotal(DiyComponent.PASSIVE);
	}

	private static void give(Item item) {
		if (item == null || item.quantity() <= 0) return;
		if (!item.collect(Dungeon.hero.belongings.backpack) && Dungeon.level != null) {
			Dungeon.level.drop(item, Dungeon.hero.pos);
		}
	}

	// ===== 附魔 / 铭文 =====

	/** 物品自带的那个附魔（武器）或铭文（护甲）类名。 */
	public static String enchantOf(Item it) {
		if (it instanceof Weapon) {
			Weapon w = (Weapon) it;
			if (w.enchantment != null) return w.enchantment.getClass().getName();
		}
		if (it instanceof Armor) {
			Armor a = (Armor) it;
			if (a.glyph != null) return a.glyph.getClass().getName();
		}
		return null;
	}

	/** 物品身上的全部附魔：自带的 + DIY 贴上去的（去重，保持顺序）。 */
	public static ArrayList<String> allEnchantsOf(Item it) {
		ArrayList<String> out = new ArrayList<>();
		String nativeEnchant = enchantOf(it);
		if (nativeEnchant != null) out.add(nativeEnchant);
		if (it != null && it.diyEnchants != null) {
			for (String cls : it.diyEnchants) {
				if (cls != null && !out.contains(cls)) out.add(cls);
			}
		}
		return out;
	}

	public static boolean isWeaponEnchant(String clsName) {
		Class<?> cls = loadClass(clsName);
		return cls != null && Weapon.Enchantment.class.isAssignableFrom(cls);
	}

	public static boolean isArmorGlyph(String clsName) {
		Class<?> cls = loadClass(clsName);
		return cls != null && Armor.Glyph.class.isAssignableFrom(cls);
	}

	private static Class<?> loadClass(String clsName) {
		if (clsName == null || clsName.isEmpty()) return null;
		try {
			return Class.forName(clsName);
		} catch (Throwable t) {
			return null;
		}
	}

	/** 按类名造一个附魔实例（给 Weapon 挂 DIY 附魔用）。 */
	public static Object newEnchantInstance(String clsName) {
		Class<?> cls = loadClass(clsName);
		if (cls == null || !Weapon.Enchantment.class.isAssignableFrom(cls)) return null;
		try {
			return Reflection.newInstance(cls);
		} catch (Throwable t) {
			return null;
		}
	}

	/** 按类名造一个铭文实例（给 Armor 挂 DIY 铭文用）。 */
	public static Object newGlyphInstance(String clsName) {
		Class<?> cls = loadClass(clsName);
		if (cls == null || !Armor.Glyph.class.isAssignableFrom(cls)) return null;
		try {
			return Reflection.newInstance(cls);
		} catch (Throwable t) {
			return null;
		}
	}

	/** 附魔/铭文的显示名。 */
	public static String enchantName(String clsName) {
		Class<?> cls = loadClass(clsName);
		if (cls != null) {
			try {
				String name = Messages.get(cls, "name", "").trim();
				if (!name.isEmpty()) return name;
			} catch (Throwable ignored) {
			}
			return cls.getSimpleName();
		}
		return "?";
	}

	/** 类名最后一段（`com...Blazing` → `Blazing`）。 */
	private static String simpleName(String clsName) {
		if (clsName == null) return "";
		int dot = clsName.lastIndexOf('.');
		return dot >= 0 ? clsName.substring(dot + 1) : clsName;
	}

	/** 和 tools/diy_sprites.py 里的命名规则保持一致：`AntiMagic` → `anti_magic`。 */
	public static String snakeCase(String name) {
		if (name == null) return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c) && i > 0) sb.append('_');
			sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}

	/**
	 * 某个附魔/铭文专属的精粹贴图（`diy_enchant_*` / `diy_glyph_*`）。
	 * 认不出来时退回通用贴图。
	 */
	public static String enchantSprite(String clsName) {
		String simple = simpleName(clsName);
		if (simple.isEmpty()) return com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.DIY_ESSENCE_ENCHANT;
		boolean glyph = isArmorGlyph(clsName);
		boolean enchant = isWeaponEnchant(clsName);
		if (!glyph && !enchant) {
			return com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.DIY_ESSENCE_ENCHANT;
		}
		return (glyph ? "diy_glyph_" : "diy_enchant_") + snakeCase(simple);
	}

	/** 某种特性/效果的专属精粹贴图，认不出来时退回通用贴图。 */
	public static String passiveSprite(String id) {
		if (id != null) {
			for (String known : PASSIVE_IDS) {
				if (known.equals(id)) return "diy_passive_" + known;
			}
			if (isBuffEffect(id) || isSpecialEffect(id)) return "diy_effect_" + id;
		}
		return com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.DIY_ESSENCE_PASSIVE;
	}

	/** 这件底材能不能贴附魔精粹（武器附魔 → 武器，铭文 → 护甲）。 */
	public static boolean acceptsEnchant(Item base, String enchantClass) {
		if (base == null || enchantClass == null) return false;
		if (isWeaponEnchant(enchantClass)) return base instanceof Weapon;
		if (isArmorGlyph(enchantClass)) return base instanceof Armor;
		return false;
	}

	// ===== 特殊特性 =====

	/** 特殊武器身上能拆出来的特性 id（用于拆解产出/名字）。 */
	public static final String[] PASSIVE_IDS = { "eye", "flag", "cabala", "rlyeh", "shadow", "taijutsu" };

	/**
	 * 真正能「贴到别的武器上」的特性：效果写在 buff 自己身上的这几种。
	 * 莫雷洛 / 拉莱耶 / 幻影之书的被动其实写在各自武器的 proc 里，
	 * 脱离原武器就没效果，所以不开放组装（免得又变成“挂了没用”）。
	 */
	public static final String[] PORTABLE_PASSIVE_IDS = { "eye", "flag", "taijutsu" };

	public static boolean isPortablePassive(String id) {
		if (id == null) return false;
		for (String portable : PORTABLE_PASSIVE_IDS) {
			if (portable.equals(id)) return true;
		}
		return false;
	}

	/** 当前装备里的任意一件物品是否声明了这个 DIY 特性。 */
	public static boolean equippedItemProvides(com.shatteredpixel.shatteredpixeldungeon.actors.Char ch, String id) {
		if (ch == null || id == null) return false;
		LinkedHashSet<String> ids = equippedPassiveIds();
		return ids.contains(id);
	}

	/** 常驻增益类效果 id（由 tools/gen_diy_effects.py 生成的 DiyEffects 提供）。 */
	public static final String[] BUFF_EFFECT_IDS = DiyEffects.IDS;

	public static boolean isBuffEffect(String id) {
		return DiyEffects.isEffect(id);
	}

	/** 专属机制效果 id。 */
	public static final String EFFECT_HEAL = "heal_regen";
	public static final String EFFECT_IDENTIFY = "auto_identify";
	public static final String EFFECT_POISON = "poison_hit";
	public static final String EFFECT_THORNS = "thorns";

	public static final String EFFECT_WARD = "ward";
	public static final String EFFECT_WAND_CHARGE = "wand_charge";

	public static final String[] SPECIAL_EFFECT_IDS = {
			EFFECT_HEAL, EFFECT_IDENTIFY, EFFECT_POISON, EFFECT_THORNS,
			EFFECT_WARD, EFFECT_WAND_CHARGE };

	/** 会自己 detach 的临时 buff：靠周期重挂让它们持续生效。 */
	public static final String[] PULSE_EFFECT_IDS = {
			"healing", "well_fed", "barkskin", "arcane_armor", "earthen_armor",
			"fire_imbue", "frost_imbue", "toxic_imbue", "hold_fast", "fury",
			"adrenaline_surge", "brace_yourself", "lethal_defense",
			"magic_soothe", "sn_d_regen", "artifact_recharge" };

	public static boolean isPulseEffect(String id) {
		if (id == null) return false;
		for (String pulse : PULSE_EFFECT_IDS) {
			if (pulse.equals(id)) return true;
		}
		return false;
	}

	/**
	 * 物品效果表：id → { 类名, 类型, 来源, 显示名 }。
	 * 周期由 DiyItemPulse 统一驱动（每 45 回合来一次）。
	 */
	private static final String[][] ITEM_EFFECTS = {
			// 药水
			{ "item:potion_healing", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing", "potion", "药水", "治疗药水" },
			{ "item:potion_strength", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength", "potion", "药水", "力量药水" },
			{ "item:potion_invisibility", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility", "potion", "药水", "隐身药水" },
			{ "item:potion_haste", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste", "potion", "药水", "疾速药水" },
			{ "item:potion_levitation", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation", "potion", "药水", "浮空药水" },
			{ "item:potion_mindvision", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision", "potion", "药水", "灵视药水" },
			{ "item:potion_frost", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost", "potion", "药水", "冰霜药水" },
			{ "item:potion_flame", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame", "potion", "药水", "液火药水" },
			{ "item:potion_experience", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience", "potion", "药水", "经验药水" },
			{ "item:potion_purity", "com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity", "potion", "药水", "净化药水" },
			{ "item:potion_magic", "com.shatteredpixel.shatteredpixeldungeon.items.potions.MagicPotion", "potion", "药水", "秘法药水" },
			{ "item:potion_wine", "com.shatteredpixel.shatteredpixeldungeon.items.potions.DaughterWine", "potion", "药水", "女儿红" },
			// 卷轴
			{ "item:scroll_mapping", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping", "scroll", "卷轴", "魔法地图" },
			{ "item:scroll_identify", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify", "scroll", "卷轴", "鉴定卷轴" },
			{ "item:scroll_remove_curse", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse", "scroll", "卷轴", "解咒卷轴" },
			{ "item:scroll_recharging", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging", "scroll", "卷轴", "充能卷轴" },
			{ "item:scroll_teleport", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation", "scroll", "卷轴", "传送卷轴" },
			{ "item:scroll_lullaby", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby", "scroll", "卷轴", "催眠卷轴" },
			{ "item:scroll_rage", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage", "scroll", "卷轴", "暴怒卷轴" },
			{ "item:scroll_terror", "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror", "scroll", "卷轴", "恐惧卷轴" },
			// 炸弹
			{ "item:bomb_fire", "com.shatteredpixel.shatteredpixeldungeon.items.bombs.Firebomb", "bomb", "投掷物", "火焰弹" },
			{ "item:bomb_frost", "com.shatteredpixel.shatteredpixeldungeon.items.bombs.FrostBomb", "bomb", "投掷物", "冰霜弹" },
			{ "item:bomb_shock", "com.shatteredpixel.shatteredpixeldungeon.items.bombs.ShockBomb", "bomb", "投掷物", "闪电弹" },
			{ "item:bomb_stench", "com.shatteredpixel.shatteredpixeldungeon.items.bombs.StenchBomb", "bomb", "投掷物", "毒气弹" },
			{ "item:bomb_regrowth", "com.shatteredpixel.shatteredpixeldungeon.items.bombs.RegrowthBomb", "bomb", "投掷物", "再生弹" },
			{ "item:bomb_holy", "com.shatteredpixel.shatteredpixeldungeon.items.bombs.HolyBomb", "bomb", "投掷物", "圣光弹" },
			// 符石
			{ "item:stone_blast", "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlast", "stone", "符石", "冲击符石" },
			{ "item:stone_fear", "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFear", "stone", "符石", "恐惧符石" },
			{ "item:stone_flock", "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFlock", "stone", "符石", "群羊符石" },
			{ "item:stone_shock", "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfShock", "stone", "符石", "电击符石" },
			{ "item:stone_blink", "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink", "stone", "符石", "闪现符石" },
	};

	public static final String ITEM_EFFECT_PREFIX = "item:";

	public static boolean isItemEffect(String id) {
		return itemEffectRow(id) != null;
	}

	private static String[] itemEffectRow(String id) {
		if (id == null) return null;
		for (String[] row : ITEM_EFFECTS) {
			if (row[0].equals(id)) return row;
		}
		return null;
	}

	public static String itemEffectClass(String id) {
		String[] row = itemEffectRow(id);
		return row == null ? null : row[1];
	}

	public static String itemEffectKind(String id) {
		String[] row = itemEffectRow(id);
		return row == null ? null : row[2];
	}

	public static String itemEffectName(String id) {
		String[] row = itemEffectRow(id);
		return row == null ? id : row[4];
	}

	public static String itemEffectSource(String id) {
		String[] row = itemEffectRow(id);
		return row == null ? "其他" : row[3];
	}

	/** 触发一次物品效果（由 DiyItemPulse 每 45 回合调用）。 */
	public static void performItemEffect(String id) {
		String[] row = itemEffectRow(id);
		if (row == null || Dungeon.hero == null) return;
		Class<?> cls = loadClass(row[1]);
		if (cls == null) return;
		try {
			Object created = Reflection.newInstance(cls);
			if (!(created instanceof Item)) return;
			Item item = (Item) created;
			item.identify(false);
			// 药水/卷轴/符石的 activate/doRead 都依赖这两个静态上下文
			Item.curUser = Dungeon.hero;
			Item.curItem = item;
			switch (row[2]) {
				case "potion": {
					Method drink = com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.class
							.getDeclaredMethod("drink", com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.class);
					drink.setAccessible(true);
					drink.invoke(item, Dungeon.hero);
					break;
				}
				case "scroll": {
					GLog.i("物品效果：%s", row[4]);
					((Scroll) item).doRead();
					break;
				}
				case "bomb": {
					Mob target = nearestEnemy();
					if (target != null) {
						GLog.i("物品效果：%s → %s", row[4], target.name());
						((com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb) item).explode(target.pos);
					} else {
						GLog.i("物品效果：%s → 附近没有目标", row[4]);
					}
					break;
				}
				case "stone": {
					Mob target = nearestEnemy();
					int cell = target != null ? target.pos : Dungeon.hero.pos;
					GLog.i("物品效果：%s → %s", row[4], target != null ? target.name() : "自己");
					Method activate = com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone.class
							.getDeclaredMethod("activate", int.class);
					activate.setAccessible(true);
					activate.invoke(item, cell);
					break;
				}
				default:
					break;
			}
		} catch (Throwable t) {
			Game.reportException(t);
			GLog.w("物品效果失败：%s（%s）", row[4], t.getClass().getSimpleName());
		}
	}

	private static Mob nearestEnemy() {
		if (Dungeon.level == null) return null;
		Mob best = null;
		int bestDist = Integer.MAX_VALUE;
		for (Mob mob : Dungeon.level.mobs) {
			if (mob == null || !mob.isAlive()
					|| mob.alignment != com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY) continue;
			int dist = Math.abs(mob.pos % Dungeon.level.width() - Dungeon.hero.pos % Dungeon.level.width())
					+ Math.abs(mob.pos / Dungeon.level.width() - Dungeon.hero.pos / Dungeon.level.width());
			if (dist > 8) continue;
			// 视野内的优先；都没有就选 6 格内最近的
			boolean seen = Dungeon.level.heroFOV[mob.pos];
			int weight = seen ? dist : dist + 100;
			if (weight < bestDist) {
				bestDist = weight;
				best = mob;
			}
		}
		return best;
	}

	/** 效果条目统一前缀：附魔/铭文类效果用 e: 前缀，craft 时并入附魔槽。 */
	public static final String ENCHANT_EFFECT_PREFIX = "e:";

	public static boolean isEnchantEffect(String id) {
		return id != null && id.startsWith(ENCHANT_EFFECT_PREFIX);
	}

	public static String enchantEffectClass(String id) {
		return isEnchantEffect(id) ? id.substring(ENCHANT_EFFECT_PREFIX.length()) : null;
	}

	/** 效果栏里列出的全部附魔 + 铭文类名（不区分底材，craft 时再校验）。 */
	public static ArrayList<String> allEnchantEffectClasses() {
		ArrayList<String> out = new ArrayList<>();
		Class<?>[][] groups = new Class<?>[][]{
				Weapon.Enchantment.common, Weapon.Enchantment.uncommon,
				Weapon.Enchantment.rare, Weapon.Enchantment.curses,
				Armor.Glyph.common, Armor.Glyph.uncommon,
				Armor.Glyph.rare, Armor.Glyph.curses };
		for (Class<?>[] group : groups) {
			for (Class<?> cls : group) {
				if (cls != null && !out.contains(cls.getName())) out.add(cls.getName());
			}
		}
		for (Class<?> cls : EXTRA_WEAPON_ENCHANTS) {
			if (cls != null && !out.contains(cls.getName())) out.add(cls.getName());
		}
		return out;
	}

	public static boolean isSpecialEffect(String id) {
		if (id == null) return false;
		for (String special : SPECIAL_EFFECT_IDS) {
			if (special.equals(id)) return true;
		}
		return false;
	}

	/** 效果来自哪类物品（界面上分组用）。 */
	public static String effectSource(String id) {
		if (id == null) return "其他";
		if (isEnchantEffect(id)) {
			String cls = enchantEffectClass(id);
			return isArmorGlyph(cls) ? "护甲铭文" : "武器附魔";
		}
		if (isItemEffect(id)) return itemEffectSource(id);
		switch (id) {
			case "eye":
			case "flag":
			case "taijutsu":
			case "critical_attack":
			case "mastery":
			case "monk_energy":
			case "physical_empower":
				return "装备";
			case "stamina":
			case "levitation":
			case "mindvision":
			case "calm":
			case "adrenaline":
			case "blob_immunity":
			case "magic_soothe":
			case "sn_d_regen":
			case "healing":
			case EFFECT_HEAL:
				return "药水";
			case "foresight":
			case "revealed_area":
			case "magical_sight":
			case EFFECT_IDENTIFY:
				return "卷轴";
			case "wand_empower":
			case "fire_imbue":
			case "frost_imbue":
			case "toxic_imbue":
			case "artifact_recharge":
			case EFFECT_WAND_CHARGE:
				return "法杖";
			case "enhanced_rings":
			case "poem":
			case "well_fed":
			case "hold_fast":
			case "brace_yourself":
			case "lethal_defense":
			case "fury":
			case "adrenaline_surge":
			case "wheelchair_rush":
				return "消耗品";
			case "barkskin":
			case "arcane_armor":
			case "earthen_armor":
			case EFFECT_THORNS:
				return "护甲";
			case EFFECT_POISON:
				return "投掷物";
			case EFFECT_WARD:
				return "符石";
			default:
				return "其他";
		}
	}

	/** 效果显示名。 */
	public static String effectName(String id) {
		if (id == null) return "?";
		if (isEnchantEffect(id)) {
			return enchantName(enchantEffectClass(id));
		}
		if (isItemEffect(id)) return itemEffectName(id);
		switch (id) {
			case EFFECT_HEAL:
				return "治疗再生";
			case EFFECT_IDENTIFY:
				return "自动鉴定";
			case EFFECT_POISON:
				return "淬毒攻击";
			case EFFECT_THORNS:
				return "荆棘反伤";
			case EFFECT_WARD:
				return "护盾符文";
			case EFFECT_WAND_CHARGE:
				return "法杖充能";
			case "foresight":
				return "危险预知";
			default:
				return passiveName(id);
		}
	}

	/** 效果 id → buff 类（专属机制优先）。 */
	private static Class<? extends Buff> buffEffectClass(String id) {
		if (id == null) return null;
		if (EFFECT_HEAL.equals(id)) return DiyRegen.class;
		if (EFFECT_IDENTIFY.equals(id)) return DiyIdentify.class;
		if ("revealed_area".equals(id)) return DiyReveal.class;
		if ("foresight".equals(id)) return DiySearch.class;
		if (EFFECT_WARD.equals(id)) return DiyWard.class;
		if (EFFECT_WAND_CHARGE.equals(id)) return DiyWandCharge.class;
		return DiyEffects.byId(id);
	}

	/** 这件物品自身（不靠组装）带的特性 id。 */
	public static String intrinsicPassive(Item it) {
		if (it instanceof WhiteKingGodSword) return "eye";
		if (it instanceof EnemyFlag) return "flag";
		if (it instanceof Morello) return "cabala";
		if (it instanceof Rlyeh) return "rlyeh";
		if (it instanceof ShadowBooks) return "shadow";
		if (it instanceof Taijutsu) return "taijutsu";
		return null;
	}

	/** 这件物品的全部特性：自身带的 + 组装贴上去的。 */
	public static ArrayList<String> passiveIdsOf(Item it) {
		ArrayList<String> out = new ArrayList<>();
		String intrinsic = intrinsicPassive(it);
		if (intrinsic != null) out.add(intrinsic);
		if (it != null && it.diyPassives != null) {
			for (String id : it.diyPassives) {
				if (id != null && !out.contains(id)) out.add(id);
			}
		}
		return out;
	}

	public static String passiveName(String id) {
		if (id == null) return "?";
		if (isItemEffect(id)) return itemEffectName(id);
		if (isEnchantEffect(id)) return enchantName(enchantEffectClass(id));
		switch (id) {
			case "eye":
				return Messages.get(WhiteKingGodSword.class, "name");
			case "flag":
				return Messages.get(EnemyFlag.class, "name");
			case "cabala":
				return Messages.get(Morello.class, "name");
			case "rlyeh":
				return Messages.get(Rlyeh.class, "name");
			case "shadow":
				return Messages.get(ShadowBooks.class, "name");
			case "taijutsu":
				return Messages.get(Taijutsu.class, "name");
			default:
				break;
		}
		switch (id) {
			case EFFECT_HEAL:
				return "治疗再生";
			case EFFECT_IDENTIFY:
				return "自动鉴定";
			case EFFECT_POISON:
				return "淬毒攻击";
			case EFFECT_THORNS:
				return "荆棘反伤";
			case "revealed_area":
				return "区域揭示";
			case "foresight":
				return "危险预知";
			case EFFECT_WARD:
				return "护盾符文";
			case EFFECT_WAND_CHARGE:
				return "法杖充能";
			default:
				break;
		}
		Class<? extends Buff> buffClass = buffEffectClass(id);
		if (buffClass != null) {
			String name = Messages.get(buffClass, "name");
			// 自定义 buff 可能还没有词条，别把 "no text found" 显示出去
			if (name != null && !name.isEmpty() && !name.contains("no text found")) {
				return name;
			}
		}
		return id;
	}

	/** 直接挂一个效果（不参与装备绑定，主要给测试/调试用）。 */
	public static void applyPassive(String id) {
		ensurePassive(id);
	}

	/** 这件物品能不能携带特性（只有装备类物品可以）。 */
	public static boolean supportsPassives(Item it) {
		// 法杖不能装备，挂在上面的效果永远不会生效，所以只认能穿在身上的
		return it instanceof EquipableItem || it instanceof KindOfWeapon;
	}

	/** 效果不在身上才补（重复调用不会延长/刷新已有 buff）。 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void ensurePassive(String id) {
		if (id == null || Dungeon.hero == null) return;
		if (isItemEffect(id)) {
			DiyItemPulse.add(id);
			return;
		}
		if ("wand_empower".equals(id)) {
			WandEmpower empower = Dungeon.hero.buff(WandEmpower.class);
			if (empower == null) {
				empower = Buff.affect(Dungeon.hero, WandEmpower.class);
			}
			empower.set(1, 9999);
			return;
		}
		Class<? extends Buff> buffClass = buffEffectClass(id);
		if (buffClass != null) {
			if (Dungeon.hero.buff(buffClass) != null) return;
			if (FlavourBuff.class.isAssignableFrom(buffClass)) {
				Buff.affect(Dungeon.hero, (Class) buffClass, PERMANENT_DURATION);
			} else {
				Buff.affect(Dungeon.hero, (Class) buffClass);
			}
			return;
		}
		switch (id) {
			case "eye":
				if (Dungeon.hero.buff(WhiteKingGodSword.OnlyOneEyeAttack.class) == null) {
					Buff.affect(Dungeon.hero, WhiteKingGodSword.OnlyOneEyeAttack.class);
				}
				break;
			case "flag":
				if (Dungeon.hero.buff(EnemyFlag.MobWarningProject.class) == null) {
					Buff.affect(Dungeon.hero, EnemyFlag.MobWarningProject.class);
				}
				break;
			case "cabala":
				if (Dungeon.hero.buff(Morello.MorelloCabala.class) == null) {
					Buff.affect(Dungeon.hero, Morello.MorelloCabala.class).updateMaxCharge(Dungeon.hero);
				}
				break;
			case "rlyeh":
				if (Dungeon.hero.buff(Rlyeh.StateProject.class) == null) {
					Buff.affect(Dungeon.hero, Rlyeh.StateProject.class);
				}
				break;
			case "shadow":
				if (Dungeon.hero.buff(ShadowBooks.ShadowProject.class) == null) {
					Buff.affect(Dungeon.hero, ShadowBooks.ShadowProject.class);
				}
				break;
			case "taijutsu":
				if (Dungeon.hero.buff(Calm.class) == null) {
					Buff.affect(Dungeon.hero, Calm.class);
				}
				if (Dungeon.hero.buff(CriticalAttack.class) == null) {
					Buff.affect(Dungeon.hero, CriticalAttack.class);
				}
				break;
			default:
				break;
		}
	}

	/** 这个效果现在在不在英雄身上（给测试 / UI 用）。 */
	public static boolean hasEffect(String id) {
		if (id == null || Dungeon.hero == null) return false;
		if (isItemEffect(id)) {
			DiyItemPulse pulse = Dungeon.hero.buff(DiyItemPulse.class);
			return pulse != null && pulse.contains(id);
		}
		Class<? extends Buff> buffClass = buffEffectClass(id);
		if (buffClass != null) return Dungeon.hero.buff(buffClass) != null;
		switch (id) {
			case "eye":
				return Dungeon.hero.buff(WhiteKingGodSword.OnlyOneEyeAttack.class) != null;
			case "flag":
				return Dungeon.hero.buff(EnemyFlag.MobWarningProject.class) != null;
			case "cabala":
				return Dungeon.hero.buff(Morello.MorelloCabala.class) != null;
			case "rlyeh":
				return Dungeon.hero.buff(Rlyeh.StateProject.class) != null;
			case "shadow":
				return Dungeon.hero.buff(ShadowBooks.ShadowProject.class) != null;
			case "taijutsu":
				return Dungeon.hero.buff(Calm.class) != null && Dungeon.hero.buff(CriticalAttack.class) != null;
			default:
				return false;
		}
	}

	/** 当前「穿在身上」的物品带来的全部特性 id。 */
	public static LinkedHashSet<String> equippedPassiveIds() {
		LinkedHashSet<String> out = new LinkedHashSet<>();
		if (Dungeon.hero == null) return out;
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings b = Dungeon.hero.belongings;
		addPassivesOf(out, b.weapon);
		addPassivesOf(out, b.secondWep);
		addPassivesOf(out, b.armor);
		addPassivesOf(out, b.secondaryArmor());
		addPassivesOf(out, b.artifact);
		addPassivesOf(out, b.misc);
		addPassivesOf(out, b.ring);
		addPassivesOf(out, b.ring2);
		if (b.extraRings != null) {
			for (com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring ring : b.extraRings) {
				addPassivesOf(out, ring);
			}
		}
		return out;
	}

	private static void addPassivesOf(LinkedHashSet<String> out, Item item) {
		if (item == null || item.diyPassives == null) return;
		for (String id : item.diyPassives) {
			if (id != null && !id.isEmpty()) out.add(id);
		}
	}

	/**
	 * 周期重挂：临时型 buff 自己会 detach，只要装备还穿着就隔一阵补一次。
	 * 由 tracker 每 N 回合调用一次。
	 */
	public static void pulseEffects(DiyEffectsTracker tracker) {
		if (tracker == null || Dungeon.hero == null) return;
		for (String id : equippedPassiveIds()) {
			if (!isPulseEffect(id)) continue;
			if (hasEffect(id)) continue;
			ensurePassive(id);
		}
	}

	/** 只保留「装备着」的那些效果：穿上的挂上，脱下的摘掉。 */
	public static void syncEquippedEffects(DiyEffectsTracker tracker) {
		if (tracker == null || Dungeon.hero == null) return;
		LinkedHashSet<String> desired = equippedPassiveIds();

		for (String id : tracker.granted()) {
			if (desired.contains(id)) continue;
			if (!tracker.isBorrowed(id)) {
				detachPassive(id);
			}
			tracker.forget(id);
		}

		for (String id : desired) {
			if (hasEffect(id)) {
				if (!tracker.isGranted(id)) {
					// 本来就有的效果（喝药、天赋……）记成借来的，脱下装备时不删
					tracker.markBorrowed(id);
					tracker.markGranted(id);
				}
				continue;
			}
			// 记在册但身上没有（被驱散 / 时长掉了）：补回去
			ensurePassive(id);
			tracker.markGranted(id);
		}
	}

	/** 摘掉某个效果对应的 buff。 */
	public static void detachPassive(String id) {
		if (id == null || Dungeon.hero == null) return;
		if (isItemEffect(id)) {
			DiyItemPulse pulse = Dungeon.hero.buff(DiyItemPulse.class);
			if (pulse != null) pulse.remove(id);
			return;
		}
		if ("foresight".equals(id)) {
			Buff.detach(Dungeon.hero, DiySearch.class);
			Buff.detach(Dungeon.hero, Foresight.class);
			return;
		}
		Class<? extends Buff> buffClass = buffEffectClass(id);
		if (buffClass != null) {
			Buff.detach(Dungeon.hero, buffClass);
			return;
		}
		switch (id) {
			case "eye":
				Buff.detach(Dungeon.hero, WhiteKingGodSword.OnlyOneEyeAttack.class);
				break;
			case "flag":
				Buff.detach(Dungeon.hero, EnemyFlag.MobWarningProject.class);
				break;
			case "cabala":
				Buff.detach(Dungeon.hero, Morello.MorelloCabala.class);
				break;
			case "rlyeh":
				Buff.detach(Dungeon.hero, Rlyeh.StateProject.class);
				break;
			case "shadow":
				Buff.detach(Dungeon.hero, ShadowBooks.ShadowProject.class);
				break;
			case "taijutsu":
				Buff.detach(Dungeon.hero, Calm.class);
				Buff.detach(Dungeon.hero, CriticalAttack.class);
				break;
			default:
				break;
		}
	}

	// ===== 拆解 =====

	/** 一次拆解的完整产出。 */
	public static final class Yield {
		public int kind;
		public int material;
		public int levels;
		public final ArrayList<String> enchants = new ArrayList<>();
		public final ArrayList<String> passives = new ArrayList<>();

		public String describe() {
			StringBuilder sb = new StringBuilder();
			for (String part : parts()) {
				if (sb.length() > 0) sb.append("、");
				sb.append(part);
			}
			return sb.toString();
		}

		/** 一行一样，给确认窗口用（一行塞不下的时候不会挤在一起）。 */
		/** 只列精粹名字，给日志一行用。 */
		public String shortNames() {
			StringBuilder sb = new StringBuilder();
			for (String cls : enchants) {
				if (sb.length() > 0) sb.append("、");
				sb.append("附魔·").append(enchantName(cls));
			}
			for (String id : passives) {
				if (sb.length() > 0) sb.append("、");
				sb.append("特性·").append(passiveName(id));
			}
			return sb.toString();
		}

		public String describeLines() {
			StringBuilder sb = new StringBuilder();
			for (String part : parts()) {
				if (sb.length() > 0) sb.append("\n");
				sb.append("· ").append(part);
			}
			return sb.toString();
		}

		private ArrayList<String> parts() {
			ArrayList<String> out = new ArrayList<>();
			if (material > 0) {
				out.add(materialName(kind) + "×" + material);
			}
			if (levels > 0) {
				out.add(Messages.get(DiyComponent.class, "name_level") + "×" + levels);
			}
			for (String cls : enchants) {
				out.add(Messages.get(DiyComponent.class, "name_enchant") + "·" + enchantName(cls));
			}
			for (String id : passives) {
				out.add(Messages.get(DiyComponent.class, "name_passive") + "·" + passiveName(id));
			}
			return out;
		}
	}

	public static Yield analyze(Item it) {
		Yield y = new Yield();
		if (it == null) return y;
		y.kind = kindOf(it);
		y.material = baseYieldOf(it);
		y.levels = Math.max(0, it.level());
		for (String cls : allEnchantsOf(it)) {
			// 淬毒/荆棘借附魔槽实现，拆解时按「效果精粹」返还，不再重复算一份附魔精粹
			if (cls.equals(DiyPoison.class.getName()) || cls.equals(Thorns.class.getName())) continue;
			y.enchants.add(cls);
		}
		y.passives.addAll(passiveIdsOf(it));
		return y;
	}

	public static boolean canDismantle(Item it) {
		if (it == null || Dungeon.hero == null) return false;
		if (it.quantity() <= 0) return false;
		if (it instanceof DiyWorkbench || it instanceof DiyMaterial || it instanceof DiyComponent) return false;
		if (it instanceof Bag || it instanceof Key || it instanceof Gold) return false;
		// 唯一物品（任务/剧情/神器核心）不拆，免得把流程玩坏
		if (it.unique) return false;
		// 身上穿着的、手里拿着的也不拆
		if (isInUse(it)) return false;
		return true;
	}

	/** 这件东西正挂在英雄的装备栏里（含备用武器、额外的戒指位、投掷位）。 */
	public static boolean isInUse(Item it) {
		if (Dungeon.hero == null) return false;
		com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings b = Dungeon.hero.belongings;
		if (it == b.weapon || it == b.secondWep || it == b.armor
				|| it == b.secondaryArmor() || it == b.artifact || it == b.misc
				|| it == b.ring || it == b.ring2
				|| it == b.thrownWeapon || it == b.abilityWeapon) {
			return true;
		}
		if (b.extraRings != null && b.extraRings.contains(it)) return true;
		return it.isEquipped(Dungeon.hero);
	}

	public static boolean dismantle(Item item) {
		if (!canDismantle(item)) {
			GLog.w("这件东西不能拆解。");
			return false;
		}

		Yield y = analyze(item);
		String name = item.name();

		if (item.quantity() > 1) {
			item.quantity(item.quantity() - 1);
			item.updateQuickslot();
		} else {
			item.detachAll(Dungeon.hero.belongings.backpack);
		}

		giveMaterial(y.kind, y.material);
		if (y.levels > 0) {
			giveEssence(DiyComponent.LEVEL, "", y.levels);
		}
		for (String cls : y.enchants) {
			giveEssence(DiyComponent.ENCHANT, cls, 1);
		}
		for (String id : y.passives) {
			giveEssence(DiyComponent.PASSIVE, id, 1);
		}

		// 按物品类别额外掉一个随机效果精粹：保证「冷静/盈能法杖/魔能透视」这类
		// 没有天然产出途径的精粹也能靠拆解攒出来
		String bonus = randomCategoryEssence(item);
		if (bonus != null) {
			giveEssence(DiyComponent.PASSIVE, bonus, 1);
		}

		int essenceCount = y.levels + y.enchants.size() + y.passives.size() + (bonus == null ? 0 : 1);
		GLog.p("拆解 " + name + "：" + Diy.materialName(y.kind) + "×" + y.material
				+ (essenceCount > 0 ? "，精粹×" + essenceCount : ""));
		return true;
	}

	/** 这件物品拆解时该掉哪一类效果精粹。 */
	private static String effectEssenceSource(Item item) {
		if (item instanceof Potion) return "药水";
		if (item instanceof Food) return "消耗品";
		if (item instanceof Scroll) return "卷轴";
		if (item instanceof Runestone) return "符石";
		if (item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb) return "投掷物";
		if (item instanceof MissileWeapon) return "投掷物";
		if (item instanceof Wand) return "法杖";
		if (item instanceof Armor) return "护甲";
		if (item instanceof Ring || item instanceof Artifact || item instanceof Trinket) return "消耗品";
		if (item instanceof Weapon) return "装备";
		return null;
	}

	/** 随机挑一个和物品类别同源的效果 id（不含附魔/装备被动，它们有自己的产出途径）。 */
	private static String randomCategoryEssence(Item item) {
		String source = effectEssenceSource(item);
		if (source == null) return null;
		ArrayList<String> pool = new ArrayList<>();
		for (String id : passiveCatalog(null)) {
			if (isEnchantEffect(id)) continue;
			if (source.equals(effectSource(id))) pool.add(id);
		}
		if (pool.isEmpty()) return null;
		return pool.get(Random.Int(pool.size()));
	}

	// ===== 组装 =====

	/**
	 * 工作台里可选的「大类」。
	 *
	 * <p>注意：这个分支把武器 / 护甲 / 投掷物按阶位拆成了 WEP_T1..T5、ARM_T1..T5、MIS_T1..T5，
	 * `Generator.Category.WEAPON / ARMOR / MISSILE` 的 classes 是空表，不能直接拿来用，
	 * 否则列表会是空的。这里把各阶合起来再去重。</p>
	 */
	public enum Group {

		WEAPON( "武器", new Generator.Category[]{
				Generator.Category.WEP_T1, Generator.Category.WEP_T2,
				Generator.Category.WEP_T3, Generator.Category.WEP_T4,
				Generator.Category.WEP_T5,
				Generator.Category.WEP_T1_NEW, Generator.Category.WEP_T2_NEW,
				Generator.Category.WEP_T3_NEW, Generator.Category.WEP_T4_NEW,
				Generator.Category.WEP_T5_NEW } ),

		ARMOR( "护甲", new Generator.Category[]{
				Generator.Category.ARM_T1, Generator.Category.ARM_T2,
				Generator.Category.ARM_T3, Generator.Category.ARM_T4,
				Generator.Category.ARM_T5, Generator.Category.ARM_T5_NATURE } ),

		MISSILE( "投掷物", new Generator.Category[]{
				Generator.Category.MIS_T1, Generator.Category.MIS_T2,
				Generator.Category.MIS_T3, Generator.Category.MIS_T4,
				Generator.Category.MIS_T5 } ),

		WAND( "法杖", new Generator.Category[]{ Generator.Category.WAND } ),
		RING( "戒指", new Generator.Category[]{ Generator.Category.RING } ),
		ARTIFACT( "神器", new Generator.Category[]{ Generator.Category.ARTIFACT } ),
		POTION( "药剂", new Generator.Category[]{ Generator.Category.POTION } ),
		SCROLL( "卷轴", new Generator.Category[]{ Generator.Category.SCROLL } ),
		STONE( "符石", new Generator.Category[]{ Generator.Category.STONE } ),
		FOOD( "食物", new Generator.Category[]{ Generator.Category.FOOD } );

		public final String label;
		public final Generator.Category[] categories;

		Group( String label, Generator.Category[] categories ) {
			this.label = label;
			this.categories = categories;
		}
	}

	/** 某个大类里可以合成的东西（实例只用来显示名字和要价）。 */
	/** 玩家是否已经知道这类物品（身上有鉴定过的实例）。 */
	@SuppressWarnings("unchecked")
	private static boolean playerKnows(Class<?> rawCls) {
		if (Dungeon.hero == null) return false;
		Class<? extends Item> cls = (Class<? extends Item>) rawCls;
		Item owned = Dungeon.hero.belongings.getItem(cls);
		if (owned != null && owned.isIdentified()) return true;
		if (Dungeon.hero.belongings.backpack != null) {
			for (Item it : Dungeon.hero.belongings.backpack.items) {
				if (cls.isInstance(it) && it.isIdentified()) return true;
			}
		}
		return false;
	}

	public static ArrayList<Item> craftableSamples(Group group) {
		ArrayList<Item> out = new ArrayList<>();
		if (group == null) return out;

		for (Generator.Category cat : group.categories) {
			if (cat == null || cat.classes == null) continue;
			for (Class<?> raw : cat.classes) {
				if (!Item.class.isAssignableFrom(raw)) continue;
				Item sample;
				try {
					@SuppressWarnings("unchecked")
					Class<? extends Item> cls = (Class<? extends Item>) raw;
					sample = Reflection.newInstance(cls);
				} catch (Throwable t) {
					sample = null;
				}
				if (sample == null) continue;
				// 不替玩家鉴定：只列出已经知道的东西，未知的等鉴定后再来
				if (!playerKnows(sample.getClass())) continue;
				if (!craftable(sample)) continue;
				boolean dup = false;
				for (Item seen : out) {
					if (seen.getClass() == sample.getClass()) {
						dup = true;
						break;
					}
				}
				if (!dup) out.add(sample);
			}
		}
		return out;
	}

	/** 这个物品能不能被合成出来。 */
	public static boolean craftable(Item sample) {
		if (sample == null) return false;
		if (sample instanceof DiyWorkbench || sample instanceof DiyMaterial
				|| sample instanceof DiyComponent) return false;
		if (sample instanceof Bag || sample instanceof Key || sample instanceof Gold) return false;
		// 图鉴里的占位 / 测试物品不开放
		if (sample instanceof com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem) return false;
		// 唯一物品（剧情、任务、神器核心）不开放
		if (sample.unique) return false;
		// 挑战禁掉的东西（力量药水 / 升级卷轴）也不开放
		return true;
	}

	// ===== 自由组装：全开放，材料计价 =====

	/**
	 * 常驻效果用的「永久」时长（回合）。不能用 Float.POSITIVE_INFINITY：
	 * JSON 存不下无穷大，读档时这个字段会丢，FlavourBuff 下一回合就把自己 detach 了。
	 * 9999 回合足够长，而且存读档正常。
	 */
	public static final float PERMANENT_DURATION = 9999f;

	/** 每级强化吃 1 份强化精华。 */
	public static final int LEVEL_ESSENCE_COST = 1;
	/** 一个附魔/铭文：有对应精粹就吃 1 份精粹，否则吃 4 个魔尘。 */
	public static final int ENCHANT_DUST_COST = 4;
	/** 一个特性：有对应精粹就吃 1 份精粹，否则吃 6 个铁件。 */
	public static final int PASSIVE_METAL_COST = 6;
	/** 武器 +1 伤害 / 护甲 +1 减伤：各吃 2 个铁件。 */
	public static final int STAT_METAL_COST = 2;
	/** 武器 +1 攻击距离：吃 3 个铁件。 */
	public static final int REACH_METAL_COST = 3;
	/** 武器 +1 攻击速度（-0.05 延迟）：吃 4 个铁件。 */
	public static final int SPEED_METAL_COST = 4;
	/** 消耗品每 +1 数量：铁件×2。 */
	public static final int COUNT_METAL_COST = 2;
	/** 食物每 +1 饱食度（+50 能量）：铁件×2。 */
	public static final int ENERGY_METAL_COST = 2;
	/** 法杖每 +1 充能：铁件×3。 */
	public static final int CHARGE_METAL_COST = 3;

	/** 攻击速度下限（延迟系数不能太低）。 */
	public static final float MIN_DELAY = 0.4f;
	/** 每点攻击速度减少的延迟。 */
	public static final float DELAY_PER_SPEED = 0.05f;
	/** 属性最多加到多少（只是怕填错，实际上想加多少都行）。 */
	public static final int MAX_STAT_BONUS = 999;

	/** 生成池里没有、但游戏里真实存在的附魔（装备随机附魔不会出，DIY 里开放）。 */
	private static final Class<?>[] EXTRA_WEAPON_ENCHANTS = {
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blocking.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blooming.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Elastic.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Resonance.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Unstable.class,
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.WetEnchantment.class,
	};

	/** 这件底材能贴的全部附魔/铭文（武器 → 武器附魔，护甲 → 铭文，其它 → 空）。 */
	public static ArrayList<String> enchantCatalog(Item base) {
		ArrayList<String> out = new ArrayList<>();
		if (base instanceof Weapon) {
			addClasses(out, Weapon.Enchantment.common);
			addClasses(out, Weapon.Enchantment.uncommon);
			addClasses(out, Weapon.Enchantment.rare);
			addClasses(out, Weapon.Enchantment.curses);
			addClasses(out, EXTRA_WEAPON_ENCHANTS);
		} else if (base instanceof Armor) {
			addClasses(out, Armor.Glyph.common);
			addClasses(out, Armor.Glyph.uncommon);
			addClasses(out, Armor.Glyph.rare);
			addClasses(out, Armor.Glyph.curses);
		}
		return out;
	}

	private static void addClasses(ArrayList<String> out, Class<?>[] classes) {
		if (classes == null) return;
		for (Class<?> cls : classes) {
			if (cls == null) continue;
			String name = cls.getName();
			if (!out.contains(name)) out.add(name);
		}
	}

	/** 全部可选特性/效果 id。 */
	public static final String[] EFFECT_SOURCES = {
			"装备", "药水", "卷轴", "法杖", "消耗品", "投掷物", "护甲", "其他" };

	public static ArrayList<String> passiveCatalog() {
		return passiveCatalog(null);
	}

	/** 全部可选效果；传了底材就把它能用的附魔/铭文也列进来。 */
	public static ArrayList<String> passiveCatalog(Item base) {
		ArrayList<String> out = new ArrayList<>();
		for (String id : PORTABLE_PASSIVE_IDS) out.add(id);
		for (String id : BUFF_EFFECT_IDS) out.add(id);
		for (String id : SPECIAL_EFFECT_IDS) out.add(id);
		for (String[] row : ITEM_EFFECTS) out.add(row[0]);
		for (String cls : allEnchantEffectClasses()) {
			out.add(ENCHANT_EFFECT_PREFIX + cls);
		}
		// 按来源排序，界面上就是一组一组的
		out.sort((a, b) -> Integer.compare(sourceOrder(a), sourceOrder(b)));
		return out;
	}

	private static int sourceOrder(String id) {
		String source = effectSource(id);
		for (int i = 0; i < EFFECT_SOURCES.length; i++) {
			if (EFFECT_SOURCES[i].equals(source)) return i;
		}
		return EFFECT_SOURCES.length;
	}

	/** 一次组装要付的账。 */
	public static final class Price {
		public int metal;
		public int arcane;
		public int misc;
		public int levelEssence;
		public final LinkedHashMap<String, Integer> enchantEssences = new LinkedHashMap<>();
		public final LinkedHashMap<String, Integer> passiveEssences = new LinkedHashMap<>();
		public boolean affordable;

		/** 按底材种类往对应材料上记账。 */
		public void addBaseCost(int kind, int amount) {
			if (amount <= 0) return;
			switch (DiyMaterial.normalize(kind)) {
				case DiyMaterial.ARCANE:
					arcane += amount;
					break;
				case DiyMaterial.MISC:
					misc += amount;
					break;
				default:
					metal += amount;
					break;
			}
		}

		public String summary() {
			StringBuilder sb = new StringBuilder();
			append(sb, "铁件", metal);
			append(sb, "魔尘", arcane);
			append(sb, "杂料", misc);
			append(sb, "强化精华", levelEssence);
			for (String cls : enchantEssences.keySet()) {
				append(sb, enchantName(cls) + "精粹", enchantEssences.get(cls));
			}
			for (String id : passiveEssences.keySet()) {
				append(sb, passiveName(id) + "精粹", passiveEssences.get(id));
			}
			return sb.length() == 0 ? "免费" : sb.toString();
		}

		private static void append(StringBuilder sb, String label, int amount) {
			if (amount <= 0) return;
			if (sb.length() > 0) sb.append("　");
			sb.append(label).append("×").append(amount);
		}
	}

	/** 算一次组装的账（含是否付得起）。 */
	public static Price priceOf(Item base, int levels, Collection<String> enchants,
			Collection<String> passives, int attackBonus, int armorBonus, int reachBonus, int speedBonus) {
		return priceOf(base, levels, enchants, passives, attackBonus, armorBonus,
				reachBonus, speedBonus, 0, 0, 0);
	}

	/** 带消耗品专属属性（数量/饱食度/充能）的完整版本。 */
	public static Price priceOf(Item base, int levels, Collection<String> enchants,
			Collection<String> passives, int attackBonus, int armorBonus, int reachBonus, int speedBonus,
			int countBonus, int energyBonus, int chargeBonus) {
		Price price = new Price();
		if (base == null) return price;
		price.addBaseCost(kindOf(base), costOf(base));
		price.levelEssence = base.isUpgradable() ? Math.max(0, levels) * LEVEL_ESSENCE_COST : 0;

		int atk = base instanceof KindOfWeapon ? Math.max(0, attackBonus) : 0;
		int arm = base instanceof Armor ? Math.max(0, armorBonus) : 0;
		int reach = base instanceof Weapon ? Math.max(0, reachBonus) : 0;
		int speed = base instanceof Weapon ? Math.max(0, speedBonus) : 0;
		int count = base.stackable ? Math.max(0, countBonus) : 0;
		int energy = base instanceof Food ? Math.max(0, energyBonus) : 0;
		int charge = base instanceof Wand ? Math.max(0, chargeBonus) : 0;
		price.metal += (atk + arm) * STAT_METAL_COST
				+ reach * REACH_METAL_COST
				+ speed * SPEED_METAL_COST
				+ count * COUNT_METAL_COST
				+ energy * ENERGY_METAL_COST
				+ charge * CHARGE_METAL_COST;

		ArrayList<String> enchantList = uniqueList(enchants);
		// 附魔/特性只能用对应精粹，不能用材料替代
		for (String effectId : uniqueList(passives)) {
			if (!isEnchantEffect(effectId)) continue;
			String cls = enchantEffectClass(effectId);
			if (enchantList.contains(cls)) continue;
			addEssence(price.enchantEssences, cls);
		}
		for (String cls : enchantList) {
			addEssence(price.enchantEssences, cls);
		}
		for (String id : uniqueList(passives)) {
			if (isEnchantEffect(id)) continue;
			addEssence(price.passiveEssences, id);
		}

		price.affordable = materialCount(DiyMaterial.METAL) >= price.metal
				&& materialCount(DiyMaterial.ARCANE) >= price.arcane
				&& materialCount(DiyMaterial.MISC) >= price.misc
				&& componentCount(DiyComponent.LEVEL, "") >= price.levelEssence;
		for (String cls : price.enchantEssences.keySet()) {
			if (componentCount(DiyComponent.ENCHANT, cls) < price.enchantEssences.get(cls)) {
				price.affordable = false;
			}
		}
		for (String id : price.passiveEssences.keySet()) {
			if (componentCount(DiyComponent.PASSIVE, id) < price.passiveEssences.get(id)) {
				price.affordable = false;
			}
		}
		return price;
	}

	private static void addEssence(LinkedHashMap<String, Integer> map, String key) {
		if (key == null) return;
		Integer old = map.get(key);
		map.put(key, (old == null ? 0 : old) + 1);
	}

	/**
	 * 组装：底材 + 任意等级 / 任意附魔 / 任意特性 / 任意属性。
	 * 附魔和特性必须用对应精粹，材料（铁件/魔尘）只能买等级和属性。
	 *
	 * @param levels       强化等级（每级 1 份强化精华）
	 * @param enchants     附魔/铭文类名集合（必须消耗对应附魔精粹，不能用魔尘代替）
	 * @param passives     特性/效果 id 集合（必须消耗对应精粹，不能用铁件代替）
	 * @param attackBonus  武器额外伤害（只对 KindOfWeapon 生效）
	 * @param armorBonus   护甲额外减伤（只对 Armor 生效）
	 */
	public static Item craft(Class<? extends Item> cls, int levels,
			Collection<String> enchants, Collection<String> passives,
			int attackBonus, int armorBonus, int reachBonus, int speedBonus) {
		return craft(cls, levels, enchants, passives, attackBonus, armorBonus,
				reachBonus, speedBonus, 0, 0, 0);
	}

	/** 带消耗品专属属性（数量/饱食度/充能）的完整版本。 */
	public static Item craft(Class<? extends Item> cls, int levels,
			Collection<String> enchants, Collection<String> passives,
			int attackBonus, int armorBonus, int reachBonus, int speedBonus,
			int countBonus, int energyBonus, int chargeBonus) {
		if (cls == null || Dungeon.hero == null) return null;

		Item out;
		try {
			out = Generator.random(cls);
		} catch (Throwable t) {
			out = null;
		}
		if (out == null) {
			GLog.w("这件东西合成不了。");
			return null;
		}

		// 价格按「0 级、1 件」算，实际合成也统一成这个形态
		out.level(0);
		out.quantity(1);
		// 随机生成时自带的附魔/铭文要清掉：成品只保留玩家自己选的那些
		if (out instanceof Weapon) {
			((Weapon) out).enchant(null);
		} else if (out instanceof Armor) {
			((Armor) out).inscribe(null);
		}

		ArrayList<String> enchantList = uniqueList(enchants);
		ArrayList<String> passiveList = uniqueList(passives);
		int atk = out instanceof KindOfWeapon ? Math.max(0, Math.min(attackBonus, MAX_STAT_BONUS)) : 0;
		int arm = out instanceof Armor ? Math.max(0, Math.min(armorBonus, MAX_STAT_BONUS)) : 0;
		int reach = out instanceof Weapon ? Math.max(0, Math.min(reachBonus, MAX_STAT_BONUS)) : 0;
		int speed = out instanceof Weapon ? Math.max(0, Math.min(speedBonus, MAX_STAT_BONUS)) : 0;
		int useLevels = out.isUpgradable() ? Math.max(0, Math.min(levels, 9999)) : 0;

		for (String cls2 : enchantList) {
			if (!acceptsEnchant(out, cls2)) {
				GLog.w(enchantName(cls2) + "贴不到" + out.name() + "上。");
				return null;
			}
		}

		int count = out.stackable ? Math.max(0, Math.min(countBonus, MAX_STAT_BONUS)) : 0;
		int energy = out instanceof Food ? Math.max(0, Math.min(energyBonus, MAX_STAT_BONUS)) : 0;
		int charge = out instanceof Wand ? Math.max(0, Math.min(chargeBonus, MAX_STAT_BONUS)) : 0;

		Price price = priceOf(out, useLevels, enchantList, passiveList, atk, arm, reach, speed,
				count, energy, charge);
		if (!price.affordable) {
			GLog.w("材料不够：" + price.summary() + "。");
			return null;
		}

		// 先扣账
		if (price.metal > 0) takeMaterials(DiyMaterial.METAL, price.metal);
		if (price.arcane > 0) takeMaterials(DiyMaterial.ARCANE, price.arcane);
		if (price.misc > 0) takeMaterials(DiyMaterial.MISC, price.misc);
		if (price.levelEssence > 0) takeComponents(DiyComponent.LEVEL, "", price.levelEssence);
		for (String cls2 : price.enchantEssences.keySet()) {
			takeComponents(DiyComponent.ENCHANT, cls2, price.enchantEssences.get(cls2));
		}
		for (String id : price.passiveEssences.keySet()) {
			takeComponents(DiyComponent.PASSIVE, id, price.passiveEssences.get(id));
		}

		// 再改成品
		if (useLevels > 0) {
			out.level(useLevels);
		}
		if (count > 0) {
			out.quantity(1 + count);
		}
		if (energy > 0 && out instanceof Food) {
			((Food) out).energy += energy * 50f;
		}
		if (charge > 0 && out instanceof Wand) {
			((Wand) out).maxCharges += charge;
			((Wand) out).curCharges += charge;
		}
		ArrayList<String> effectEnchants = new ArrayList<>();
		for (String effectId : uniqueList(passiveList)) {
			if (!isEnchantEffect(effectId)) continue;
			String ecls = enchantEffectClass(effectId);
			if (ecls != null && !effectEnchants.contains(ecls)) effectEnchants.add(ecls);
		}
		if (!enchantList.isEmpty() || !effectEnchants.isEmpty()) {
			if (out.diyEnchants == null) out.diyEnchants = new ArrayList<>();
			out.diyEnchants.addAll(enchantList);
			for (String ecls : effectEnchants) {
				if (!out.diyEnchants.contains(ecls)) out.diyEnchants.add(ecls);
			}
		}
		if (passiveList.contains(EFFECT_POISON) && out instanceof Weapon) {
			if (out.diyEnchants == null) out.diyEnchants = new ArrayList<>();
			if (!out.diyEnchants.contains(DiyPoison.class.getName())) {
				out.diyEnchants.add(DiyPoison.class.getName());
			}
		}
		if (passiveList.contains(EFFECT_THORNS) && out instanceof Armor) {
			if (out.diyEnchants == null) out.diyEnchants = new ArrayList<>();
			if (!out.diyEnchants.contains(Thorns.class.getName())) {
				out.diyEnchants.add(Thorns.class.getName());
			}
		}
		if (!passiveList.isEmpty()) {
			if (!supportsPassives(out)) {
				GLog.w(out.name() + "不是装备，贴不了特性。");
				return null;
			}
			for (String id : passiveList) {
				if (isEnchantEffect(id)) {
					if (!acceptsEnchant(out, enchantEffectClass(id))) {
						GLog.w(effectName(id) + "贴不到这件装备上。");
						return null;
					}
					continue;
				}
				if (!isPortablePassive(id) && !isBuffEffect(id) && !isSpecialEffect(id) && !isItemEffect(id)) {
					GLog.w(passiveName(id) + "的效果写在原武器里，贴不到别的武器上。");
					return null;
				}
				if (EFFECT_POISON.equals(id) && !(out instanceof Weapon)) {
					GLog.w("淬毒只能贴在武器上。");
					return null;
				}
				if (EFFECT_THORNS.equals(id) && !(out instanceof Armor)) {
					GLog.w("荆棘反伤只能贴在护甲上。");
					return null;
				}
			}
			if (out.diyPassives == null) out.diyPassives = new ArrayList<>();
			// 只记录，不立刻生效：装备这件物品时才会挂到英雄身上（见 Diy.syncEquippedEffects）
			for (String id : passiveList) {
				if (isEnchantEffect(id)) continue;
				if (!out.diyPassives.contains(id)) out.diyPassives.add(id);
			}
		}
		if (atk > 0 && out instanceof KindOfWeapon) {
			((KindOfWeapon) out).diyMin += atk;
			((KindOfWeapon) out).diyMax += atk;
		}
		if (arm > 0 && out instanceof Armor) {
			((Armor) out).diyDRMin += arm;
			((Armor) out).diyDRMax += arm;
		}
		if (out instanceof Weapon) {
			Weapon weapon = (Weapon) out;
			if (reach > 0) {
				weapon.RCH += reach;
			}
			if (speed > 0) {
				weapon.DLY = Math.max(MIN_DELAY, weapon.DLY - speed * DELAY_PER_SPEED);
			}
		}

		out.cursed = false;
		out.cursedKnown = true;
		out.identify(false);
		give(out);
		DiyEffectsTracker.ensure(Dungeon.hero);

		StringBuilder msg = new StringBuilder("组装完成：").append(out.title());
		if (atk > 0) msg.append(" 伤害+").append(atk);
		if (arm > 0) msg.append(" 减伤+").append(arm);
		if (reach > 0) msg.append(" 距离+").append(reach);
		if (speed > 0) msg.append(" 攻速+").append(speed);
		if (count > 0) msg.append(" 数量+").append(count);
		if (energy > 0) msg.append(" 饱食+").append(energy);
		if (charge > 0) msg.append(" 充能+").append(charge);
		if (enchantList.size() > 0) msg.append(" 附魔×").append(enchantList.size());
		if (passiveList.size() > 0) msg.append(" 特性×").append(passiveList.size());
		GLog.p(msg.toString());
		return out;
	}

	private static ArrayList<String> uniqueList(Collection<String> source) {
		ArrayList<String> out = new ArrayList<>();
		if (source == null) return out;
		for (String value : source) {
			if (value != null && !value.isEmpty() && !out.contains(value)) {
				out.add(value);
			}
		}
		return out;
	}

	/** 组装窗口的实时预览：底材 + 等级 + 属性 + 全部附魔 + 全部特性。 */
	public static String assemblePreview(Item base, int levels, Collection<String> enchants,
			Collection<String> passives, int attackBonus, int armorBonus, int reachBonus, int speedBonus) {
		return assemblePreview(base, levels, enchants, passives, attackBonus, armorBonus,
				reachBonus, speedBonus, 0, 0, 0);
	}

	public static String assemblePreview(Item base, int levels, Collection<String> enchants,
			Collection<String> passives, int attackBonus, int armorBonus, int reachBonus, int speedBonus,
			int countBonus, int energyBonus, int chargeBonus) {
		if (base == null) return "";
		StringBuilder sb = new StringBuilder(base.name());
		if (levels > 0 && base.isUpgradable()) sb.append(" +").append(levels);
		if (attackBonus > 0 && base instanceof KindOfWeapon) sb.append(" 伤害+").append(attackBonus);
		if (armorBonus > 0 && base instanceof Armor) sb.append(" 减伤+").append(armorBonus);
		if (reachBonus > 0 && base instanceof Weapon) sb.append(" 距离+").append(reachBonus);
		if (speedBonus > 0 && base instanceof Weapon) sb.append(" 攻速+").append(speedBonus);
		if (countBonus > 0 && base.stackable) sb.append(" 数量+").append(countBonus);
		if (energyBonus > 0 && base instanceof Food) sb.append(" 饱食+").append(energyBonus);
		if (chargeBonus > 0 && base instanceof Wand) sb.append(" 充能+").append(chargeBonus);
		if (enchants != null) {
			for (String cls : enchants) {
				if (cls != null) sb.append(" · ").append(enchantName(cls));
			}
		}
		if (passives != null) {
			for (String id : passives) {
				if (id != null) sb.append(" · ").append(passiveName(id));
			}
		}
		return sb.toString();
	}

	/** 附魔精粹能不能贴到这件底材上（UI 用来把不适用的行置灰）。 */
	public static boolean acceptsEnchants(Item base, Collection<String> enchants) {
		if (enchants == null) return true;
		for (String cls : enchants) {
			if (!acceptsEnchant(base, cls)) return false;
		}
		return true;
	}

	/** 武器能不能加伤害 / 护甲能不能加减伤（UI 用来决定显不显示属性区）。 */
	public static boolean supportsAttackBonus(Item base) {
		return base instanceof KindOfWeapon;
	}

	public static boolean supportsArmorBonus(Item base) {
		return base instanceof Armor;
	}
}
