package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.ui.AutochessIcons;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Reflection;

/**
 * 棋子详情：数值 + 羁绊 + 技能说明。
 */
public class WndAutochessInfo extends Window {

	private static final int WIDTH = 190;
	private static final int MARGIN = 5;
	private static final int GAP = 3;

	public WndAutochessInfo(AutochessPool.Def def) {
		this(def, null);
	}

	public WndAutochessInfo(AutochessPool.Def def, java.util.List<String> itemIds) {
		super();

		Mob mob = null;
		try {
			mob = Reflection.newInstance(def.cls);
		} catch (Exception ignored) {
		}

		String name = mob == null ? def.cls.getSimpleName() : mob.name();
		RenderedTextBlock title = PixelScene.renderTextBlock(name, 9);
		title.hardlight(costColor(def.cost));
		title.setPos(MARGIN, MARGIN);
		add(title);

		float pos = title.bottom() + GAP;

		// 图标
		CharSprite icon = AutochessIcons.spriteFor(def.cls.getSimpleName());
		if (icon != null) {
			AutochessIcons.place(icon, MARGIN, pos, 32);
			add(icon);
		}

		float textX = MARGIN + 36f;
		StringBuilder head = new StringBuilder();
		head.append(Messages.get(WndAutochessInfo.class, "cost", def.cost));
		head.append("\n").append(traitNames(def));

		RenderedTextBlock headText = PixelScene.renderTextBlock(head.toString(), 7);
		headText.maxWidth((int) (WIDTH - textX - MARGIN));
		headText.setPos(textX, pos);
		add(headText);

		pos = Math.max(pos + 34, headText.bottom() + GAP);

		// 数值
		int hp = mob == null ? 0 : mob.HT;
		int dmg = 0;
		try {
			dmg = mob == null ? 0 : mob.damageRoll();
		} catch (Exception ignored) {
		}
		int def0 = mob == null ? 0 : mob.defenseSkill;
		String stats = Messages.get(WndAutochessInfo.class, "stats", hp, dmg, def0);
		RenderedTextBlock statText = PixelScene.renderTextBlock(stats, 7);
		statText.maxWidth(WIDTH - MARGIN * 2);
		statText.setPos(MARGIN, pos);
		add(statText);
		pos = statText.bottom() + GAP;

		// 技能
		String skillName = def.skill == null ? "-" : def.skill.name();
		int mana = def.skill == null ? 0 : def.skill.mana;
		RenderedTextBlock skillTitle = PixelScene.renderTextBlock(
				Messages.get(WndAutochessInfo.class, "skill", skillName, mana), 8);
		skillTitle.hardlight(0x66DDFF);
		skillTitle.maxWidth(WIDTH - MARGIN * 2);
		skillTitle.setPos(MARGIN, pos);
		add(skillTitle);
		pos = skillTitle.bottom() + 1;

		String desc = def.skill == null ? "-" : def.skill.desc();
		RenderedTextBlock descText = PixelScene.renderTextBlock(desc, 7);
		descText.maxWidth(WIDTH - MARGIN * 2);
		descText.setPos(MARGIN, pos);
		add(descText);
		pos = descText.bottom() + GAP;

		// 装备
		java.util.ArrayList<String> items = new java.util.ArrayList<>();
		if (itemIds != null) items.addAll(itemIds);
		if (!items.isEmpty()) {
			StringBuilder eq = new StringBuilder(Messages.get(WndAutochessInfo.class, "items"));
			for (String id : items) {
				eq.append(" ").append(com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessItems.name(id));
			}
			RenderedTextBlock eqText = PixelScene.renderTextBlock(eq.toString(), 7);
			eqText.maxWidth(WIDTH - MARGIN * 2);
			eqText.hardlight(0xFFCC66);
			eqText.setPos(MARGIN, pos);
			add(eqText);
			pos = eqText.bottom() + 1;
		}

		// 星级说明
		RenderedTextBlock starText = PixelScene.renderTextBlock(
				Messages.get(WndAutochessInfo.class, "stars"), 7);
		starText.maxWidth(WIDTH - MARGIN * 2);
		starText.hardlight(0xAAAAAA);
		starText.setPos(MARGIN, pos);
		add(starText);
		pos = starText.bottom() + GAP;

		RedButton close = new RedButton(Messages.get(WndAutochessInfo.class, "close"), 8) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos, WIDTH - MARGIN * 2, 16);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(WIDTH, (int) Math.ceil(pos));
	}

	private static String traitNames(AutochessPool.Def def) {
		StringBuilder sb = new StringBuilder();
		for (String trait : def.traits) {
			if (sb.length() > 0) sb.append(" · ");
			sb.append(Messages.get(AutochessPool.class, "trait_" + trait));
		}
		return sb.toString();
	}

	private static int costColor(int cost) {
		switch (cost) {
			case 1: return 0xCCCCCC;
			case 2: return 0x66DD66;
			case 3: return 0x66AAFF;
			case 4: return 0xCC77FF;
			default: return 0xFFAA33;
		}
	}
}
