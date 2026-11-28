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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

import java.util.LinkedHashMap;

public class TalentButton extends Button {

	public static final int WIDTH = 20;
	public static final int HEIGHT = 26;

	int tier;
	Talent talent;
	int pointsInTalent;
	Mode mode;

	Image icon;
	Image bg;

	ColorBlock fill;
	RoundedFrame roundedFrame;

	public enum Mode {
		INFO,
		UPGRADE,
		METAMORPH_CHOOSE,
		METAMORPH_REPLACE
	}

	public TalentButton(int tier, Talent talent, int points, Mode mode){
		super();
		hotArea.blockLevel = PointerArea.NEVER_BLOCK;

		this.tier = tier;
		this.talent = talent;
		this.pointsInTalent = points;
		this.mode = mode;

		bg.frame(20*(talent.maxPoints()-1), 0, WIDTH, HEIGHT);

		// 骰子法师学派天赋：显示"即将解锁"法术的 SND 贴图，而非通用天赋图标
		Image schoolIcon = schoolIcon();
		icon = schoolIcon != null ? schoolIcon : new TalentIcon( talent );
		add(icon);
	}

	/** 学派天赋返回其"即将解锁"法术的 SND 图标；非学派或找不到时返回 null。 */
	private Image schoolIcon() {
		if (!Talent.isDiceMageSpellTalent(talent)) return null;
		String sndName = DiceMageSchools.nextSpellSndName(talent, pointsInTalent);
		if (sndName == null) return null;
		Image img = SNDItems.get(sndName);
		if (img == null) return null;
		// 将 SND 贴图放大到与天赋图标(16px)相同的显示尺寸
		float s = 16f / img.width();
		img.scale.set(s);
		return img;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		fill = new ColorBlock(0, 4, 0xFFFFFF44);
		add(fill);

		boolean useShattered = com.shatteredpixel.shatteredpixeldungeon.SPDSettings.NORMAL_SKIN()
				|| com.shatteredpixel.shatteredpixeldungeon.SPDSettings.statusStyle()
				== com.shatteredpixel.shatteredpixeldungeon.SPDSettings.STYLE_SHATTERED;
		bg = new Image(useShattered
				? Assets.Interfaces.TALENT_BUTTON
				: Assets.Interfaces.TALENT_BUTTON_RADISH);
		add(bg);

		roundedFrame = UITheme.roundedFrame(DiceMageUI.BLACK, DiceMageUI.ORANGE);
		roundedFrame.visible = false;
		addToBack(roundedFrame);
	}

	@Override
	protected void layout() {
		width = WIDTH;
		height = HEIGHT;

		super.layout();

		fill.x = x+2;
		fill.y = y + WIDTH - 1;
		fill.size( pointsInTalent/(float)talent.maxPoints() * (WIDTH-4), 5);

		bg.x = x;
		bg.y = y;
		bg.visible = !UITheme.isDiceMage();
		roundedFrame.setRect(x, y, width, height);
		roundedFrame.visible = UITheme.isDiceMage();
		if (roundedFrame.visible) {
			roundedFrame.setLineColor(DiceMageUI.ORANGE);
		}

		icon.x = x + 2;
		icon.y = y + 2;
		PixelScene.align(icon);
	}

	@Override
	protected void onClick() {
		super.onClick();

		Window toAdd;
		if (mode == Mode.UPGRADE
				&& Dungeon.hero != null
				&& Dungeon.hero.isAlive()
				&& !(tier == 3 && Dungeon.hero.subClass == HeroSubClasses.DICE_MAGE
						&& Talent.isDiceMageSpellTalent(talent))
				&& Dungeon.hero.talentPointsAvailable(tier) > 0
				&& Dungeon.hero.pointsInTalent(talent) < talent.maxPoints()){
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(WndInfoTalent.class, "upgrade"));
				}

				@Override
				public void call() {
					upgradeTalent();
				}
			});
		} else if (mode == Mode.METAMORPH_CHOOSE && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}

				@Override
				public void call() {
					if (ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE != null){
						ScrollOfMetamorphosis.WndMetamorphChoose.INSTANCE.hide();
					}
					GameScene.show(new ScrollOfMetamorphosis.WndMetamorphReplace(talent, tier));
				}
			});
		} else if (mode == Mode.METAMORPH_REPLACE && Dungeon.hero != null && Dungeon.hero.isAlive()) {
			toAdd = new WndInfoTalent(talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {

				@Override
				public String prompt() {
					return Messages.titleCase(Messages.get(ScrollOfMetamorphosis.class, "metamorphose_talent"));
				}

				@Override
				public boolean metamorphDesc() {
					return true;
				}

				@Override
				public void call() {
					Talent replacing = ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.replacing;

					for (LinkedHashMap<Talent, Integer> tier : Dungeon.hero.talents){
						if (tier.containsKey(replacing)){
							LinkedHashMap<Talent, Integer> newTier = new LinkedHashMap<>();
							for (Talent t : tier.keySet()){
								if (t == replacing){
									newTier.put(talent, tier.get(replacing));

									if (!Dungeon.hero.metamorphedTalents.containsValue(replacing)){
										Dungeon.hero.metamorphedTalents.put(replacing, talent);

									//if what we're replacing is already a value, we need to simplify the data structure
									} else {
										//a->b->a, we can just remove the entry entirely
										if (Dungeon.hero.metamorphedTalents.get(talent) == replacing){
											Dungeon.hero.metamorphedTalents.remove(talent);

										//a->b->c, we need to simplify to a->c
										} else {
											for (Talent t2 : Dungeon.hero.metamorphedTalents.keySet()){
												if (Dungeon.hero.metamorphedTalents.get(t2) == replacing){
													Dungeon.hero.metamorphedTalents.put(t2, talent);
												}
											}
										}
									}

								} else {
									newTier.put(t, tier.get(t));
								}
							}
							Dungeon.hero.talents.set(ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.tier-1, newTier);
							break;
						}
					}

					ScrollOfMetamorphosis.onMetamorph(replacing, talent);

					if (ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE != null){
						ScrollOfMetamorphosis.WndMetamorphReplace.INSTANCE.hide();
					}

				}
			});
		} else {
			toAdd = new WndInfoTalent(talent, pointsInTalent, null);
		}

		if (ShatteredPixelDungeon.scene() instanceof GameScene){
			GameScene.show(toAdd);
		} else {
			ShatteredPixelDungeon.scene().addToFront(toAdd);
		}
	}

	@Override
	protected void onPointerDown() {
		icon.brightness( 1.5f );
		bg.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}

	@Override
	protected void onPointerUp() {
		icon.resetColor();
		bg.resetColor();
	}

	@Override
	protected String hoverText() {
		return Messages.titleCase(talent.title());
	}

	public void enable(boolean value ) {
		active = value;
		icon.alpha( value ? 1.0f : 0.3f );
		bg.alpha( value ? 1.0f : 0.3f );
	}

	public void upgradeTalent(){
		if (Dungeon.hero.talentPointsAvailable(tier) > 0 && parent != null) {
			Dungeon.hero.upgradeTalent(talent);
			float oldWidth = fill.width();
			pointsInTalent++;
			layout();
			Sample.INSTANCE.play(Assets.Sounds.LEVELUP, 0.7f, 1.2f);
			Emitter emitter = (Emitter) parent.recycle(Emitter.class);
			emitter.revive();
			emitter.pos(fill.x + (fill.width() + oldWidth) / 2f, fill.y + fill.height() / 2f);
			emitter.burst(Speck.factory(Speck.STAR), 12);
			if (Talent.isReef(talent)) {
				showReefLockWindow();
			}
		}
	}

	private void showReefLockWindow() {
		int tierIndex = tier - 1;
		if (Dungeon.hero == null || tierIndex < 0 || tierIndex >= Dungeon.hero.talents.size()) return;
		java.util.ArrayList<Talent> candidates = new java.util.ArrayList<>();
		for (Talent candidate : Dungeon.hero.talents.get(tierIndex).keySet()) {
			if (!Talent.isReef(candidate)) {
				candidates.add(candidate);
			}
		}
		if (candidates.isEmpty()) return;

		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(new Window() {
			{
				int cols = Math.min(candidates.size(), 5);
				int rows = (candidates.size() + cols - 1) / cols;
				int gap = 4;
				int winWidth = Math.max(110, cols * WIDTH + (cols - 1) * gap + 8);

				RenderedTextBlock title = PixelScene.renderTextBlock(talent.title(), 9);
				title.hardlight(TITLE_COLOR);
				title.setPos((winWidth - title.width()) / 2f, 2);
				PixelScene.align(title);
				add(title);

				RenderedTextBlock desc = PixelScene.renderTextBlock("选择本层一个天赋锁定，使其不再因变身而改变：", 6);
				desc.maxWidth(winWidth - 8);
				desc.setPos(4, title.bottom() + 4);
				add(desc);

				float startY = desc.bottom() + 6;
				for (int i = 0; i < candidates.size(); i++) {
					final Talent candidate = candidates.get(i);
					int col = i % cols;
					int row = i / cols;
					float rowWidth = Math.min(cols, candidates.size() - row * cols) * WIDTH
							+ (Math.min(cols, candidates.size() - row * cols) - 1) * gap;
					float startX = (winWidth - rowWidth) / 2f;

					TalentButton btn = new TalentButton(tier, candidate, Dungeon.hero.pointsInTalent(candidate), Mode.INFO) {
						@Override
						protected void onClick() {
							com.shatteredpixel.shatteredpixeldungeon.actors.hero.JumbleTalents
									.lockTalent(Dungeon.hero, tierIndex, candidate);
							hide();
						}
					};
					if (Dungeon.hero.jumbleLockedTalents[tierIndex] == candidate) {
						btn.bg.hardlight(0x66CCFF);
					}
					btn.setPos(startX + col * (WIDTH + gap), startY + row * (HEIGHT + gap));
					add(btn);
				}

				int winHeight = (int) (startY + rows * HEIGHT + (rows - 1) * gap + 4);
				resize(winWidth, winHeight);
			}
		});
	}
}
