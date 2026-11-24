/*
 * Radish Pixel Dungeon
 * DIY 工作台 - 组装部件
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

/**
 * DIY 组装部件：拆解高级物品时拆出来的「可转移部件」。
 *
 * <ul>
 *   <li>{@link #LEVEL} 强化精华：一份换 +1 级，任何底材都能用</li>
 *   <li>{@link #ENCHANT} 附魔精粹：记着原物品的附魔/铭文（{@code payload} 是附魔类的全名）</li>
 *   <li>{@link #PASSIVE} 特性精粹：特殊武器上的被动（{@code payload} 是特性 id，见 {@link Diy}）</li>
 * </ul>
 *
 * 同一种部件的 {@code payload} 相同才会叠在一起。
 */
public class DiyComponent extends Item {

	public static final int LEVEL    = 0;
	public static final int ENCHANT  = 1;
	public static final int PASSIVE  = 2;

	private int kind = LEVEL;
	private String payload = "";

	{
		stackable = true;
	}

	public DiyComponent() {
		this(LEVEL, "", 1);
	}

	public DiyComponent(int kind, String payload, int amount) {
		this.kind = clampKind(kind);
		this.payload = payload == null ? "" : payload;
		this.image = imageFor(this.kind);
		quantity(amount);
	}

	public static int clampKind(int kind) {
		if (kind < LEVEL) return LEVEL;
		if (kind > PASSIVE) return PASSIVE;
		return kind;
	}

	public static String imageFor(int kind) {
		switch (clampKind(kind)) {
			case ENCHANT:
				return ItemSpriteSheet.DIY_ESSENCE_ENCHANT;
			case PASSIVE:
				return ItemSpriteSheet.DIY_ESSENCE_PASSIVE;
			default:
				return ItemSpriteSheet.DIY_ESSENCE_LEVEL;
		}
	}

	public int kind() {
		return kind;
	}

	public String payload() {
		return payload;
	}

	/** 显示名：附魔精粹·虚空 / 特性精粹·白帝圣剑 / 强化精华。 */
	public String displayName() {
		switch (kind) {
			case ENCHANT:
				return Messages.get(DiyComponent.class, "name_enchant") + "·" + Diy.enchantName(payload);
			case PASSIVE:
				return Messages.get(DiyComponent.class, "name_passive") + "·" + Diy.passiveName(payload);
			default:
				return Messages.get(DiyComponent.class, "name_level");
		}
	}

	@Override
	public String trueName() {
		return displayName();
	}

	@Override
	public String name() {
		return trueName();
	}

	@Override
	public String desc() {
		switch (kind) {
			case ENCHANT:
				return Messages.get(DiyComponent.class, "desc_enchant");
			case PASSIVE:
				return Messages.get(DiyComponent.class, "desc_passive");
			default:
				return Messages.get(DiyComponent.class, "desc_level");
		}
	}

	/**
	 * 附魔精粹和特性精粹不用同一张图：每个附魔/铭文/特性都有自己颜色的贴图，
	 * 名字由 {@link Diy#enchantSprite(String)} / {@link Diy#passiveSprite(String)} 算出来。
	 */
	@Override
	public String image() {
		if (kind == ENCHANT && payload != null && !payload.isEmpty()) {
			return Diy.enchantSprite(payload);
		}
		if (kind == PASSIVE && payload != null && !payload.isEmpty()) {
			return Diy.passiveSprite(payload);
		}
		return super.image();
	}

	@Override
	public boolean isSimilar(Item item) {
		return item instanceof DiyComponent
				&& ((DiyComponent) item).kind == kind
				&& ((DiyComponent) item).payload.equals(payload);
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
		return 20 * quantity;
	}

	private static final String KIND = "kind";
	private static final String PAYLOAD = "payload";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(KIND, kind);
		bundle.put(PAYLOAD, payload);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		kind = clampKind(bundle.getInt(KIND));
		payload = bundle.getString(PAYLOAD);
		if (payload == null) payload = "";
		image = imageFor(kind);
	}
}
