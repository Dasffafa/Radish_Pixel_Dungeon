/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2026 TheCatist
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.engineer.mods.ModState;
import com.shatteredpixel.shatteredpixeldungeon.items.engineer.mods.ModType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

/**
 * 金属零件：工程师制作改装部件的材料与改装入口。
 */
public class MetalPart extends Item {

	private static final String AC_MODIFY = "modify";

	{
		defaultAction = AC_MODIFY;
		image = ItemSpriteSheet.METAL_PART;
		stackable = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_MODIFY);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_MODIFY)) {
			GameScene.selectItem(itemSelector);
		}
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
		return 10 * quantity;
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(MetalPart.class, "modify");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return ModType.Slot.of(item) != null;
		}

		@Override
		public void onSelect(Item item) {
			if (item == null) return;
			GameScene.show(new WndModify(item, MetalPart.this));
		}
	};

	public static class WndModify extends Window {

		private static final int WIDTH = 120;
		private static final int MARGIN = 2;

		public WndModify(Item target, MetalPart part) {
			IconTitle titlebar = new IconTitle(target);
			titlebar.setRect(0, 0, WIDTH, 0);
			add(titlebar);

			float pos = titlebar.bottom() + MARGIN;
			boolean isCraftsman = Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClasses.CRAFTSMAN;

			for (ModType type : ModType.values()) {
				if (!type.fits(target)) continue;
				if (type.craftsmanOnly && !isCraftsman) continue;

				boolean installed = ModState.has(target, type);
				boolean enoughParts = part.quantity() >= type.parts;
				boolean enoughEnergy = Dungeon.energy >= type.energy;

				String label = type.title() + " (" + type.parts + (type.energy > 0 ? "+" + type.energy + "E" : "") + ")";
				RedButton btn = new RedButton(label) {
					@Override
					protected void onClick() {
						if (installed) {
							GLog.w(Messages.get(MetalPart.class, "already_installed"));
							return;
						}
						if (!enoughParts) {
							GLog.w(Messages.get(MetalPart.class, "no_enough_part"));
							return;
						}
						if (!enoughEnergy) {
							GLog.w(Messages.get(MetalPart.class, "no_enough_energy"));
							return;
						}
						part.quantity(part.quantity() - type.parts);
						if (part.quantity() <= 0) {
							part.detachAll(Dungeon.hero.belongings.backpack);
						}
						Dungeon.energy -= type.energy;
						ModState.ensure(target).add(type, type.life, type.parts, type.energy > 0);
						updateQuickslot();
						Dungeon.hero.sprite.operate(Dungeon.hero.pos);
						Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
						Dungeon.hero.spendAndNext(1f);
						hide();
					}
				};
				btn.enable(!installed && enoughParts && enoughEnergy);
				btn.setRect(0, pos, WIDTH, 18);
				add(btn);
				pos = btn.bottom() + MARGIN;
			}

			RenderedTextBlock hint = PixelScene.renderTextBlock(
					Messages.get(MetalPart.class, "cost_hint", part.quantity(), Dungeon.energy), 6);
			hint.maxWidth(WIDTH);
			hint.setPos(0, pos + MARGIN);
			add(hint);
			pos = hint.bottom() + MARGIN;

			resize(WIDTH, (int) pos);
		}
	}
}
