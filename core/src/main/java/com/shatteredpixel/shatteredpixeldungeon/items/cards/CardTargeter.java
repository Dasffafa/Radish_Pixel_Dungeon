package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

public final class CardTargeter {

	private CardTargeter() {
	}

	public static void play(Hero hero, Card card) {
		if (hero == null || card == null || hero.cardDeck == null || hero.cardDeck.held != card) {
			return;
		}
		if (!hero.isAlive() || !hero.ready) return;

		// 陷阱师：空白卡可以对陷阱使用，因此改用格子选择
		if (card.type == CardType.BLANK && hero.subClass == HeroSubClasses.JOKER) {
			if (!hasValidTarget(hero, card)) {
				offerSafeReturn(hero, card);
			} else {
				GameScene.selectCell(new CellSelector.Listener() {
					@Override
					public void onSelect(Integer cell) {
						if (cell == null || !isValidTarget(hero, card, cell)) {
							if (cell != null) {
								com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
										Messages.get(CardTargeter.class, "bad_target"));
							}
							return;
						}
						CardEffects.play(hero, card, cell);
					}

					@Override
					public String prompt() {
						return Messages.get(CardTargeter.class, "prompt", card.name());
					}
				});
			}
			return;
		}

		switch (card.type.target) {
			case NONE:
			case SELF:
				CardEffects.play(hero, card, hero.pos);
				break;
			case ENEMY:
			case ANY:
				if (!hasValidTarget(hero, card)) {
					offerSafeReturn(hero, card);
				} else {
					GameScene.selectCell(new CellSelector.Listener() {
						@Override
						public void onSelect(Integer cell) {
							if (cell == null || !isValidTarget(hero, card, cell)) {
								if (cell != null) {
									com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
											Messages.get(CardTargeter.class, "bad_target"));
								}
								return;
							}
							CardEffects.play(hero, card, cell);
						}

						@Override
						public String prompt() {
							return Messages.get(CardTargeter.class, "prompt", card.name());
						}
					});
				}
				break;
			case CELL:
				if (!hasValidTarget(hero, card)) {
					offerSafeReturn(hero, card);
				} else {
					GameScene.selectCell(new CellSelector.Listener() {
						@Override
						public void onSelect(Integer cell) {
							if (cell == null || !isValidTarget(hero, card, cell)) {
								if (cell != null) {
									com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
											Messages.get(CardTargeter.class, "bad_target"));
								}
								return;
							}
							CardEffects.play(hero, card, cell);
						}

						@Override
						public String prompt() {
							return Messages.get(CardTargeter.class, "prompt", card.name());
						}
					});
				}
				break;
		}
	}

	public static boolean isValidTarget(Hero hero, Card card, int cell) {
		Char ch = Actor.findChar(cell);
		switch (card.type.target) {
			case NONE:
				return true;
			case SELF:
				return ch == hero;
			case ENEMY:
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) return false;
				// 卡牌攻击不能穿墙：需要视线可达
				return new Ballistica(hero.pos, ch.pos, Ballistica.STOP_SOLID)
						.collisionPos == ch.pos;
			case ANY:
				if (card.type == CardType.BLANK && ch == hero) {
					// 空白卡指定自己：仅收藏家可用（转化为攻击/防御卡）。
					return hero.subClass == HeroSubClasses.ILLUSIONIST;
				}
				if (card.type == CardType.BLANK && hero.subClass == HeroSubClasses.JOKER) {
					// 陷阱师：陷阱格或敌人
					return Dungeon.level.traps.get(cell) != null
							|| (ch != null && ch != hero && ch.alignment != Char.Alignment.ALLY);
				}
				return ch != null && (ch == hero || ch.alignment != Char.Alignment.ALLY);
			case CELL:
				return isValidCell(hero, card, cell);
			default:
				return false;
		}
	}

	private static boolean isValidCell(Hero hero, Card card, int cell) {
		if (Dungeon.level.solid[cell]) return false;
		if (card.type == CardType.TELEPORT) {
			int range = card.teleportRange > 0 ? card.teleportRange : 2;
			if (Dungeon.level.distance(hero.pos, cell) > range) return false;
			if (!Dungeon.level.passable[cell]) return false;
			// 允许传送到自己脚下以外的空地；有生物站着的格子不可落点
			if (cell != hero.pos && Actor.findChar(cell) != null) return false;
			// 传送不需要视线，只要能走到的平面即可
			return true;
		}
		// 陷阱卡：视野内 8 格，只需可达且非实体墙（不被生物/软障碍阻挡）
		if (Dungeon.level.distance(hero.pos, cell) > 8) return false;
		Ballistica b = new Ballistica(hero.pos, cell, Ballistica.IGNORE_SOFT_SOLID);
		return b.collisionPos == cell;
	}

	public static boolean hasValidTarget(Hero hero, Card card) {
		if (card.type.target == CardType.Target.SELF || card.type.target == CardType.Target.NONE) {
			return true;
		}
		if (card.type.target == CardType.Target.CELL) {
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.heroFOV[i] && isValidCell(hero, card, i)) return true;
			}
			return false;
		}
		if (card.type == CardType.BLANK && hero.subClass == HeroSubClasses.JOKER) {
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.heroFOV[i] && Dungeon.level.traps.get(i) != null) return true;
			}
		}
		for (Char ch : Actor.chars()) {
			if (isValidTarget(hero, card, ch.pos)) return true;
		}
		return false;
	}

	public static void offerSafeReturn(Hero hero, Card card) {
		GameScene.show(new WndOptions(
				Messages.get(CardTargeter.class, "no_target_title"),
				Messages.get(CardTargeter.class, "no_target_desc"),
				Messages.get(CardTargeter.class, "safe_return"),
				Messages.get(CardTargeter.class, "cancel")) {
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					if (hero.cardDeck != null && hero.cardDeck.held == card) {
						hero.cardDeck.putHeldBack(card);
						HeldCardTracker.clear(hero);
						hero.spendAndNext(1f);
						com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.refresh();
					}
				}
			}
		});
	}

	public static void offerSafeReturnIfNone(Hero hero) {
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			Card card = hero.cardDeck.held;
			if (card.type.needsTarget() && !hasValidTarget(hero, card)) {
				offerSafeReturn(hero, card);
			}
		}
	}

	/** Convenience for the deck window. */
	public static void playHeld(Hero hero) {
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			play(hero, hero.cardDeck.held);
		}
	}

	public static int heldCount(Hero hero) {
		return hero.cardDeck == null || hero.cardDeck.held == null ? 0 : 1;
	}

	public static boolean inCombat(Hero hero) {
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]) return true;
		}
		return false;
	}
}
