package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PaperCutter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCardSelect;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public final class CardEffects {

	private CardEffects() {
	}

	public static void play(Hero hero, Card card, Integer target) {
		if (hero == null || card == null || hero.cardDeck == null || hero.cardDeck.held != card) {
			return;
		}
		if (target != null && !CardTargeter.isValidTarget(hero, card, target)) {
			GLog.w(Messages.get(CardTargeter.class, "bad_target"));
			return;
		}

		int pos = target == null ? hero.pos : target;
		Char ch = Actor.findChar(pos);

		switch (card.type) {
			case BLANK: {
				// 陷阱师：对陷阱使用空白卡 -> 记录该陷阱，卡片转为陷阱卡
				Trap trap = Dungeon.level.traps.get(pos);
				if (trap != null && hero.subClass == HeroSubClasses.JOKER) {
					card.type = CardType.TRAP;
					card.trapClass = trap.getClass();
					GLog.i(Messages.get(CardEffects.class, "trap_recorded", trap.name()));
					ActionIndicator.refresh();
					return;
				}
				if (ch == hero) {
					// 收藏家：对自己使用空白卡 -> 选择一件装备转化为攻击/防御卡
					collectorTransform(hero, card);
					return;
				}
				if (ch != null) {
					int dmg = Random.NormalIntRange(1, 5);
					int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
					if (emp == 1) dmg += Random.NormalIntRange(0, 2);
					else if (emp == 2) dmg += Random.NormalIntRange(1, 3);

					int wind = hero.pointsInTalent(Talent.BORROW_WIND_CUT);
					if (wind > 0) {
						int dist = Math.min(4, Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos)));
						dmg += dist * (wind == 2 ? Random.NormalIntRange(2, 3) : Random.NormalIntRange(1, 2));
					}
					dealCardDamage(hero, ch, card, dmg);
				}
				finish(hero, card, false, card.type.time);
				break;
			}
			case SMALL_RATION:
				eatCard(hero, card, 50);
				finish(hero, card, false, foodTime(hero, card));
				break;
			case RATION:
				eatCard(hero, card, 100);
				finish(hero, card, false, card.type.time);
				break;
			case IDENTIFY: {
				identifyRandom(hero, 1);
				if (card.power >= 2) {
					GameScene.show(new WndOptions(
							Messages.get(CardEffects.class, "destroy_title"),
							Messages.get(CardEffects.class, "destroy_desc"),
							Messages.get(CardEffects.class, "destroy_yes"),
							Messages.get(CardEffects.class, "destroy_no")) {
						@Override
						protected void onSelect(int index) {
							finish(hero, card, index == 0, card.type.time);
						}
					});
				} else {
					finish(hero, card, false, card.type.time);
				}
				break;
			}
			case RIGID: {
				if (ch == null) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				Buff.affect(ch, Paralysis.class, 5f);
				CardDrawer.cancelCooldown(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case HORMONE: {
				Buff.affect(hero, Adrenaline.class, 2f);
				Buff.affect(hero, Haste.class, 2f);
				CardDrawer.cancelCooldown(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case SPIN: {
				int amount = card.power >= 3 ? 5 : card.power == 2 ? 4 : 3;
				int spun = hero.cardDeck.spinBlank(amount);
				GLog.i(Messages.get(CardEffects.class, "spin_done", amount, spun));
				if (hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 4) CardDrawer.cancelCooldown(hero);
				fancySpin(hero, spun);
				finish(hero, card, false, card.type.time);
				break;
			}
			case GRAND_FINALE: {
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				int dmg = Random.NormalIntRange(5, 10);
				for (int i = 0; i < hero.cardDeck.blankCount(); i++) {
					dmg += Random.NormalIntRange(5, 10);
				}
				if (card.power >= 3) {
					dmg += Math.round(weaponAverage(card) / 3f);
				}
				dealCardDamage(hero, ch, card, dmg);
				fancySpin(hero, hero.cardDeck.spinBlank(card.power >= 2 ? 3 : 2));
				finish(hero, card, false, card.type.time);
				break;
			}
			case PREPARED: {
				int look = card.power >= 3 ? 4 : card.power == 2 ? 3 : 2;
				int maxMove = card.power >= 3 ? 2 : 1;
				ArrayList<Card> top = hero.cardDeck.peekTop(look);
				if (top.isEmpty()) {
					finish(hero, card, false, card.type.time);
				} else {
					GameScene.show(new WndCardSelect(
							Messages.get(CardEffects.class, "prepared_title"),
							Messages.get(CardEffects.class, "prepared_desc", maxMove),
							top, maxMove, true, selected -> {
								for (Card c : selected) {
									if (hero.cardDeck.drawPile.remove(c)) {
										hero.cardDeck.addToBottom(c);
										hero.cardDeck.noteCycle(1);
									}
								}
								if (hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 4) CardDrawer.cancelCooldown(hero);
								finish(hero, card, false, card.type.time);
							}));
				}
				break;
			}
			case ATTACK: {
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				boolean free = Buff.affect(hero, NextAttackFree.class).consume();
				int dmg = Random.NormalIntRange(1, 5);

				int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
				if (emp == 1) dmg += Random.NormalIntRange(0, 2);
				else if (emp == 2) dmg += Random.NormalIntRange(1, 3);

				int wind = hero.pointsInTalent(Talent.BORROW_WIND_CUT);
				if (wind > 0) {
					int dist = Math.min(4, Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos)));
					dmg += dist * (wind == 2 ? Random.NormalIntRange(2, 3) : Random.NormalIntRange(1, 2));
				}

				int vault = hero.pointsInTalent(Talent.REFINED_ATTACK_DEFENSE);
				if (vault > 0) {
					int count = hero.cardDeck.count(CardType.ATTACK);
					if (vault == 1) dmg += count;
					else if (vault == 2) dmg += count * Random.NormalIntRange(1, 2);
					else dmg += count * Random.NormalIntRange(2, 3);
				}

				int pick = hero.pointsInTalent(Talent.CARD_ENCHANT);
				if (pick > 0) {
					float frac = pick == 1 ? 0.25f : pick == 2 ? 1f / 3f : 0.5f;
					dmg += Math.round(weaponAverage(card) * frac);
				}

				dealCardDamage(hero, ch, card, dmg);
				finish(hero, card, false, free ? 0f : card.type.time);
				break;
			}
			case DEFENSE: {
				int shield = 3;
				int vault = hero.pointsInTalent(Talent.REFINED_ATTACK_DEFENSE);
				if (vault > 0) {
					shield += hero.cardDeck.count(CardType.DEFENSE) * vault;
				}
				int pick = hero.pointsInTalent(Talent.CARD_ENCHANT);
				if (pick > 0) {
					float frac = pick == 1 ? 0.25f : pick == 2 ? 1f / 3f : 0.5f;
					shield += Math.round(armorMax(card) * frac);
				}

				// 防御卡护盾取高者覆盖，不叠加
				Barrier barrier = Buff.affect(hero, Barrier.class);
				if (barrier.shielding() < shield) {
					barrier.setShield(shield);
				}
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + shield);

				// 以守为攻
				int guard = hero.pointsInTalent(Talent.CARD_ASSIST);
				if (guard >= 1) {
					Buff.affect(hero, Stamina.class, 1f);
				}
				if (guard >= 2) {
					Card card2 = hero.cardDeck.drawPile.isEmpty() ? null
							: hero.cardDeck.drawPile.get(hero.cardDeck.drawPile.size() - 1);
					if (card2 != null && card2.type == CardType.ATTACK) {
						CardDrawer.reduceCooldown(hero, 5);
					}
				}
				if (guard >= 3) {
					Buff.affect(hero, NextAttackFree.class);
				}

				CardDrawer.cancelCooldown(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case GAZE: {
				Buff.affect(hero, MindVision.class, 20f);
				GLog.i(Messages.get(CardEffects.class, "gaze"));
				CardDrawer.cancelCooldown(hero);
				int gaze = hero.pointsInTalent(Talent.GAZE_CARD);
				if (gaze >= 4) Buff.affect(hero, DangerSense.class, 5f);
				float time = gaze >= 3 ? 0f : card.type.time;
				finish(hero, card, false, time);
				break;
			}
			case TRAP: {
				if (hero.buff(TrapCooldown.class) != null) {
					GLog.w(Messages.get(CardEffects.class, "trap_cooling", (int) hero.buff(TrapCooldown.class).cooldown()));
					return;
				}
				triggerTrap(card, pos);
				Buff.affect(hero, TrapCooldown.class, trapCooldown(hero));
				card.type = CardType.BLANK;
				card.trapClass = null;
				finish(hero, card, false, card.type.time);
				break;
			}
			case TELEPORT: {
				int cell = target == null ? hero.pos : target;
				int old = hero.pos;
				hero.pos = cell;
				hero.sprite.place(cell);
				Dungeon.level.pressCell(cell);
				Dungeon.observe();
				GameScene.updateFog();
				CardDrawer.cancelCooldown(hero);
				int swift = hero.pointsInTalent(Talent.SWIFT_STEP);
				if (swift > 0) {
					Buff.affect(hero, Haste.class, swift);
					CardDrawer.reduceCooldown(hero, swift);
				}
				finish(hero, card, true, 0f);
				// 传送卡变回原卡
				if (hero.cardDeck.stashedCard != null) {
					hero.cardDeck.held = hero.cardDeck.stashedCard;
					hero.cardDeck.stashedCard = null;
					HeldCardTracker.sync(hero);
					ActionIndicator.refresh();
				}
				if (hero.armorAbility instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.CardBarrage) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.CardBarrage) hero.armorAbility)
							.grantRepeat(hero.pointsInTalent(Talent.EMERGENCY_REPEAT));
				}
				break;
			}
		}
	}

	/**
	 * 收藏家：空白卡指定自己，选择一件已鉴定非诅咒的武器/护甲，把这张空白卡就地变成攻击/防御卡。
	 */
	public static void collectorTransform(Hero hero, Card card) {
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(CardEffects.class, "collect_prompt");
			}

			@Override
			public boolean itemSelectable(Item item) {
				return item.isIdentified() && !item.cursed
						&& (item instanceof MeleeWeapon || item instanceof Armor);
			}

			@Override
			public void onSelect(Item item) {
				if (item == null) return;
				if (hero.cardDeck == null || hero.cardDeck.held != card) return;

				if (item.isEquipped(hero) && item instanceof EquipableItem) {
					if (!((EquipableItem) item).doUnequip(hero, false, false)) return;
				}
				item.detachAll(hero.belongings.backpack);

				if (item instanceof MeleeWeapon) {
					card.type = CardType.ATTACK;
					card.gearTier = ((MeleeWeapon) item).tier;
					card.gearLevel = item.trueLevel();
					card.enchantment = ((Weapon) item).enchantment;
				} else {
					card.type = CardType.DEFENSE;
					card.gearTier = ((Armor) item).tier;
					card.gearLevel = item.trueLevel();
				}
				GLog.i(Messages.get(CardEffects.class, "collected", item.name(), card.name()));
				ActionIndicator.refresh();
				Item.updateQuickslot();
			}
		});
	}

	private static void triggerTrap(Card card, int pos) {
		if (card.trapClass == null) return;
		try {
			Trap trap = (Trap) card.trapClass.getDeclaredConstructor().newInstance();
			trap.set(pos);
			trap.activate();
			GLog.i(Messages.get(CardEffects.class, "trap_released", trap.name()));
		} catch (Exception e) {
			GLog.w(Messages.get(CardEffects.class, "trap_failed"));
		}
	}

	private static int trapCooldown(Hero hero) {
		int cd = 25;
		int craft = hero.pointsInTalent(Talent.TRAP_CRAFT);
		if (craft > 0) {
			HashSet<Class<?>> types = new HashSet<>();
			for (Card c : hero.cardDeck.allCards()) {
				if (c.type == CardType.TRAP && c.trapClass != null) types.add(c.trapClass);
			}
			int min = craft == 1 ? 20 : craft == 2 ? 15 : 10;
			cd = Math.max(min, 25 - types.size() * craft);
		}
		return cd;
	}

	private static void dealCardDamage(Hero hero, Char ch, Card card, int dmg) {
		// 伤害型卡片等效投掷武器：相邻命中下降、远距离命中加成；不享受神射戒指。
		float acc = Dungeon.level.adjacent(hero.pos, ch.pos) ? 0.5f : 1.5f;
		if (!Char.hit(hero, ch, acc, false)) {
			ch.sprite.showStatus(CharSprite.NEUTRAL, Messages.get(CardEffects.class, "miss"));
			return;
		}
		ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, hero, null, card));
		if (!ch.isAlive()) {
			BoneToGold gold = hero.buff(BoneToGold.class);
			if (gold != null) gold.onCardKill(hero, ch.pos);
		}
	}

	private static void finish(Hero hero, Card card, boolean exhaust, float time) {
		if (hero.cardDeck == null || hero.cardDeck.held != card) return;

		// 卡牌护体：打出任意卡获得不可叠加护盾
		int prot = hero.pointsInTalent(Talent.BLANK_PROTECTION);
		if (prot > 0) {
			int shield = prot == 1 ? 2 : 3;
			Barrier barrier = Buff.affect(hero, Barrier.class);
			if (barrier.shielding() < shield) {
				barrier.setShield(shield);
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + shield);
			}
		}

		// 僵化/激素强化：每打出一张卡，前置一格
		if (card.type != CardType.RIGID && card.type != CardType.HORMONE) {
			advanceEnhanced(hero);
		}

		hero.sprite.operate(hero.pos);
		if (time > 0) hero.busy();
		if (exhaust) {
			hero.cardDeck.held = null;
		} else if (card.fromStorage) {
			card.fromStorage = false;
			int eff = hero.pointsInTalent(Talent.EFFECTIVE_USE);
			if (eff > 0 && Random.Float() < 0.1f * eff) {
				hero.cardDeck.held = null;
				hero.cardDeck.stored.add(card);
			} else {
				hero.cardDeck.playDone(card);
			}
		} else if (hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 3
				&& (card.type == CardType.SPIN || card.type == CardType.GRAND_FINALE
					|| card.type == CardType.PREPARED)) {
			// 花式切牌 +3：打出后随机插入牌组
			hero.cardDeck.held = null;
			hero.cardDeck.insertRandom(card);
		} else {
			hero.cardDeck.playDone(card);
		}
		HeldCardTracker.clear(hero);
		ActionIndicator.refresh();
		Item.updateQuickslot();
		hero.spendAndNext(time);
	}

	private static void advanceEnhanced(Hero hero) {
		ArrayList<Card> pile = hero.cardDeck.drawPile;
		for (int i = 0; i < pile.size() - 1; i++) {
			Card c = pile.get(i);
			if ((c.type == CardType.RIGID || c.type == CardType.HORMONE) && c.power >= 2) {
				pile.remove(i);
				pile.add(i + 1, c);
			}
		}
	}

	private static float foodTime(Hero hero, Card card) {
		return hero.pointsInTalent(Talent.ADVANCED_FOOD) >= 1 ? 0f : card.type.time;
	}

	private static void eatCard(Hero hero, Card card, float energy) {
		if (card.type == CardType.SMALL_RATION && hero.buff(RationCooldown.class) != null) {
			GLog.w(Messages.get(CardEffects.class, "ration_cooling"));
			return;
		}
		float val = energy;
		if (Dungeon.isChallenged(Challenges.NO_FOOD)) val /= 3f;
		Buff.affect(hero, Hunger.class).satisfy(val);
		if (card.type == CardType.SMALL_RATION) {
			Buff.affect(hero, RationCooldown.class, 100f);
		}
		if (hero.pointsInTalent(Talent.ADVANCED_FOOD) >= 2) {
			CardDrawer.cancelCooldown(hero);
		}
		hero.sprite.operate(hero.pos);
		SpellSprite.show(hero, SpellSprite.FOOD);
		GLog.i(Messages.get(CardEffects.class, "eaten", (int) energy));
	}

	/** 临时管理：把存储槽内的卡打出。 */
	public static void playStored(Hero hero, Card card) {
		CardDeck deck = hero.cardDeck;
		if (deck == null || card == null || deck.held != null) return;
		if (!deck.stored.contains(card)) return;
		deck.stored.remove(card);
		deck.held = card;
		card.fromStorage = true;
		HeldCardTracker.sync(hero);
		CardTargeter.play(hero, card);
	}

	/** 存储槽到期：把卡打出。 */
	public static void releaseStored(Hero hero) {
		CardDeck deck = hero.cardDeck;
		if (deck == null || deck.stored.isEmpty() || deck.held != null) return;
		playStored(hero, deck.stored.get(0));
	}

	/** 花式切牌 +1：空转出的空白卡中随机一张打到视野内随机敌人。 */
	public static void fancySpin(Hero hero, int spunBlanks) {
		if (spunBlanks <= 0 || hero.pointsInTalent(Talent.FANCY_SHUFFLE) < 1) return;
		ArrayList<Char> targets = new ArrayList<>();
		for (Char c : Actor.chars()) {
			if (c.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[c.pos]) targets.add(c);
		}
		if (targets.isEmpty()) return;
		Char ch = targets.get(Random.Int(targets.size()));
		int dmg = Random.NormalIntRange(1, 5);
		int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
		if (emp == 1) dmg += Random.NormalIntRange(0, 2);
		else if (emp == 2) dmg += Random.NormalIntRange(1, 3);
		int wind = hero.pointsInTalent(Talent.BORROW_WIND_CUT);
		if (wind > 0) {
			int dist = Math.min(4, Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos)));
			dmg += dist * (wind == 2 ? Random.NormalIntRange(2, 3) : Random.NormalIntRange(1, 2));
		}
		dealCardDamage(hero, ch, null, dmg);
		GLog.i(Messages.get(CardEffects.class, "fancy_spin", ch.name()));
	}

	/** 陷阱师「灵力协调」：使用法杖后减少陷阱卡与抽卡冷却。 */
	public static void onWandUsed(Hero hero) {
		int p = hero.pointsInTalent(Talent.SPIRIT_COORDINATION);
		if (p <= 0) return;
		CardDrawer.reduceCooldown(hero, p);
		TrapCooldown cd = hero.buff(TrapCooldown.class);
		if (cd != null) {
			cd.reduce(p);
		}
	}

	public static void onDeckCycled(Hero hero) {		if (hero.subClass != HeroSubClasses.SPIN_MASTER || hero.cardDeck == null) return;
		int dmg = Math.max(1, hero.lvl);
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]) {
				ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL_NO_ARMOR, hero, null, hero.cardDeck));
			}
		}
		GLog.i(Messages.get(CardEffects.class, "cycle_damage", dmg));
	}

	public static void identifyRandom(Hero hero, int count) {
		for (int i = 0; i < count; i++) {
			Item item = findUnidentified(hero);
			if (item == null) {
				int roll = Random.Int(3);
				Generator.Category cat = roll == 0 ? Generator.Category.SCROLL
						: roll == 1 ? Generator.Category.POTION : Generator.Category.RING;
				item = Generator.random(cat);
				if (item != null) {
					item.identify();
					if (!item.collect(hero.belongings.backpack)) {
						Dungeon.level.drop(item, hero.pos).sprite.drop();
					}
				}
			} else {
				item.identify();
			}
		}
		GLog.i(Messages.get(CardEffects.class, "identified"));
	}

	private static Item findUnidentified(Hero hero) {
		for (Item item : hero.belongings) {
			if (!item.isIdentified() && (item instanceof Scroll || item instanceof Potion || item instanceof Ring)) {
				return item;
			}
		}
		return null;
	}

	private static float weaponAverage(Card card) {
		if (card.gearTier <= 0) return 0;
		int tier = Math.max(1, card.gearTier);
		int min = tier + card.gearLevel;
		int max = 5 * (tier + 1) + card.gearLevel * (tier + 1);
		return (min + max) / 2f;
	}

	private static float armorMax(Card card) {
		if (card.gearTier <= 0) return 0;
		return card.gearTier * (2 + card.gearLevel);
	}

	/** 临时管理：存储槽计时。 */
	public static class CardStorage extends FlavourBuff {
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.45f, 0.65f, 1f); }

		@Override
		public boolean act() {
			if (cooldown() <= 0) {
				releaseStored((Hero) target);
				detach();
				return true;
			}
			spend(TICK);
			return true;
		}
	}

	/** 点骨成金：卡牌击杀额外掉落。 */
	public static class BoneToGold extends FlavourBuff {
		public String icon() { return BuffIndicator.UPGRADE; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 0.85f, 0.3f); }

		public int wealthLevel(Hero hero) {
			return 2 + hero.pointsInTalent(Talent.HEAVY_REWARD);
		}

		public void onCardKill(Hero hero, int pos) {
			int gold = Math.max(1, Dungeon.depth * 6 * wealthLevel(hero));
			Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.Gold(gold), pos).sprite.drop();
			int heal = hero.pointsInTalent(Talent.EXTRA_BLESSING);
			if (heal > 0) {
				int amount = Math.round(hero.HT * (0.02f + 0.01f * heal));
				hero.HP = Math.min(hero.HT, hero.HP + amount);
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + amount);
			}
		}
	}

	/** 小口粮卡的 100 回合内部冷却。 */
	public static class RationCooldown extends FlavourBuff {
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.8f, 0.6f, 0.2f); }
	}

	/** 防御卡护盾取高者覆盖用。 */
	public static class NextAttackFree extends Buff {
		public boolean consumed;

		@Override
		public boolean act() {
			spend(TICK);
			return true;
		}

		public boolean consume() {
			if (consumed) return false;
			consumed = true;
			detach();
			return true;
		}

		@Override
		public String icon() { return BuffIndicator.NONE; }
	}

	/** 陷阱卡独立冷却。 */
	public static class TrapCooldown extends FlavourBuff {
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.45f, 0.3f, 0.1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 25f); }

		public void reduce(float amount) {
			if (cooldown() <= amount) detach();
			else spend(-amount);
		}
	}

	/** 危险感知（隧穿凝视 +4）。 */
	public static class DangerSense extends FlavourBuff {
		public String icon() { return BuffIndicator.MIND_VISION; }
		@Override
		public boolean act() {
			for (int cell = 0; cell < Dungeon.level.length(); cell++) {
				Trap t = Dungeon.level.traps.get(cell);
				if (t != null && Dungeon.level.heroFOV[cell]) {
					t.reveal();
				}
			}
			return super.act();
		}
	}

	/** 卡牌刻印强化（旧幻术师遗留，防御卡不再使用，保留以兼容旧存档）。 */
	public static class CardGlyphBoost extends FlavourBuff {
		private float strength = 1.5f;

		{
			type = buffType.POSITIVE;
		}

		public CardGlyphBoost setStrength(float value) {
			strength = value;
			return this;
		}

		public float strength() {
			return strength;
		}

		@Override
		public String icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.54f, 0.23f, 0.72f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Math.round(strength * 100));
		}

		private static final String STRENGTH = "strength";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STRENGTH, strength);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			strength = bundle.getFloat(STRENGTH);
		}
	}
}
