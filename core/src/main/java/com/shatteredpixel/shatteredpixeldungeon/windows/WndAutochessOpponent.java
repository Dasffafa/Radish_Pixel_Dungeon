package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessBot;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessPool;
import com.shatteredpixel.shatteredpixeldungeon.challenges.autochess.AutochessRun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.AutochessIcons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 对手阵容预览：血量、等级、上阵棋子与羁绊。
 */
public class WndAutochessOpponent extends Window {

	private static final int WIDTH = 190;
	private static final int MARGIN = 5;
	private static final int GAP = 3;
	private static final int ROW = 22;

	public WndAutochessOpponent(AutochessBot bot) {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock(
				Messages.get(WndAutochessOpponent.class, "title", bot.name), 9);
		title.hardlight(0xFFCC66);
		title.setPos(MARGIN, MARGIN);
		add(title);

		float pos = title.bottom() + GAP;

		RenderedTextBlock info = PixelScene.renderTextBlock(
				Messages.get(WndAutochessOpponent.class, "info", bot.hp, bot.level, bot.roster.size()), 7);
		info.maxWidth(WIDTH - MARGIN * 2);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + GAP;

		AutochessRun run = AutochessRun.get();
		ArrayList<String> units = bot.deployed();
		if (units.isEmpty()) {
			RenderedTextBlock empty = PixelScene.renderTextBlock(
					Messages.get(WndAutochessOpponent.class, "empty"), 7);
			empty.setPos(MARGIN, pos);
			add(empty);
			pos = empty.bottom() + GAP;
		}

		// 棋子一行一个：图标 + 名字 + 星级 + 羁绊
		LinkedHashMap<String, Integer> traitCounts = new LinkedHashMap<>();
		for (String entry : units) {
			String[] parts = entry.split(":");
			String cls = parts[0];
			int star = parts.length > 1 ? parseInt(parts[1]) : 1;
			AutochessPool.Def def = AutochessPool.ofName(cls);
			if (def == null) continue;

			CharSprite icon = AutochessIcons.spriteFor(cls);
			if (icon != null) {
				AutochessIcons.place(icon, MARGIN, pos, 18);
				add(icon);
			}

			String stars = star >= 3 ? "★★★" : (star == 2 ? "★★" : "★");
			String text = nameOf(def) + " " + stars;
			RenderedTextBlock row = PixelScene.renderTextBlock(text, 7);
			row.maxWidth(WIDTH - MARGIN * 2 - 24);
			row.hardlight(costColor(def.cost));
			row.setPos(MARGIN + 22, pos + 2);
			add(row);

			StringBuilder traits = new StringBuilder();
			for (String trait : def.traits) {
				if (traits.length() > 0) traits.append(" · ");
				traits.append(traitName(trait));
				traitCounts.merge(trait, 1, Integer::sum);
			}
			RenderedTextBlock traitText = PixelScene.renderTextBlock(traits.toString(), 7);
			traitText.hardlight(0x999999);
			traitText.setPos(MARGIN + 22, pos + 11);
			add(traitText);

			pos += ROW;
		}

		// 羁绊统计
		if (!traitCounts.isEmpty()) {
			StringBuilder sb = new StringBuilder(Messages.get(WndAutochessOpponent.class, "traits"));
			for (String key : traitCounts.keySet()) {
				sb.append("\n").append(traitName(key)).append(" ×").append(traitCounts.get(key));
			}
			RenderedTextBlock traitSummary = PixelScene.renderTextBlock(sb.toString(), 7);
			traitSummary.maxWidth(WIDTH - MARGIN * 2);
			traitSummary.hardlight(0x66DDFF);
			traitSummary.setPos(MARGIN, pos + 2);
			add(traitSummary);
			pos = traitSummary.bottom() + GAP;
		}

		RedButton close = new RedButton(Messages.get(WndAutochessOpponent.class, "close"), 8) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos + GAP, WIDTH - MARGIN * 2, 16);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(WIDTH, (int) Math.ceil(pos));
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 1;
		}
	}

	private static String nameOf(AutochessPool.Def def) {
		try {
			com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob =
					com.watabou.utils.Reflection.newInstance(def.cls);
			if (mob != null) return mob.name();
		} catch (Exception ignored) {
		}
		return def.cls.getSimpleName();
	}

	private static String traitName(String trait) {
		return Messages.get(AutochessPool.class, "trait_" + trait);
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
