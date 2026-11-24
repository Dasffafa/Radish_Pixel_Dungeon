/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 拆解出来的材料
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

/**
 * DIY 材料：用 DIY 工作台拆解物品后得到的原料。
 *
 * <ul>
 *   <li>铁件：武器、护甲、投掷物拆出来的金属零件</li>
 *   <li>魔尘：法杖、戒指、神器、药剂、卷轴、符石拆出来的魔法粉末</li>
 *   <li>杂料：食物、炸弹等其它东西拆出来的杂项</li>
 * </ul>
 *
 * 材料只用于在工作台里合成新物品，本身不能使用、不能强化。
 */
public class DiyMaterial extends Item {

	public static final int METAL  = 0;
	public static final int ARCANE = 1;
	public static final int MISC   = 2;

	/** 材料种类的消息后缀，和 messages 里的 name_xxx / desc_xxx 对应。 */
	public static final String[] KIND_KEYS = { "metal", "arcane", "misc" };

	private int kind = METAL;

	{
		stackable = true;
	}

	public DiyMaterial() {
		this(METAL, 1);
	}

	public DiyMaterial(int kind, int amount) {
		this.kind = normalize(kind);
		this.image = imageFor(this.kind);
		quantity(amount);
	}

	public static int normalize(int kind) {
		if (kind < METAL) return METAL;
		if (kind > MISC) return MISC;
		return kind;
	}

	public static String imageFor(int kind) {
		switch (normalize(kind)) {
			case ARCANE:
				return ItemSpriteSheet.DIY_ARCANE;
			case MISC:
				return ItemSpriteSheet.DIY_SCRAP;
			default:
				return ItemSpriteSheet.DIY_METAL;
		}
	}

	public int kind() {
		return kind;
	}

	public String kindKey() {
		return KIND_KEYS[normalize(kind)];
	}

	@Override
	public String trueName() {
		return Messages.get(DiyMaterial.class, "name_" + kindKey());
	}

	@Override
	public String name() {
		return trueName();
	}

	@Override
	public String desc() {
		return Messages.get(DiyMaterial.class, "desc_" + kindKey());
	}

	@Override
	public boolean isSimilar(Item item) {
		return item instanceof DiyMaterial && ((DiyMaterial) item).kind == kind;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 12 * quantity;
	}

	private static final String KIND = "kind";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KIND, kind);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		kind = normalize(bundle.getInt(KIND));
		image = imageFor(kind);
	}
}
