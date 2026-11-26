package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.JumbleTalents;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.JumbleSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.timing.VirtualTimer;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;


public class JumbleChangeBuff extends Buff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	// 距下次变身的剩余回合数
	private float turnsRemaining;
	private final ArrayList<Item> storedEquipment = new ArrayList<>();

	private volatile boolean changing = false;

	// 当前外观组是变身结果的一部分，必须独立于精灵动画持久化。
	private volatile int currentGroup = 0;
	private transient volatile int visualChangeToken = 0;

	/** Shared by transformation commit and Hero serialization. */
	public static final Object STATE_LOCK = new Object();

	private static final float MIN_INTERVAL = 180;
	private static final float MAX_INTERVAL = 220;
	// 两段各 10 帧、12 FPS，再留半秒余量；仅用于解锁，不参与游戏计时。
	private static final float VISUAL_TIMEOUT = 20f / 12f + 0.5f;

	private static final String TURNS = "turns";
	private static final String GROUP = "group";
	private static final String STORED_EQUIPMENT = "stored_equipment";

	public int currentGroup() {
		return currentGroup;
	}

	@Override
	public boolean act() {
		synchronized (STATE_LOCK) {
			if (target == Dungeon.hero && isJumbleActive() && !changing) {
				turnsRemaining -= 1;
				if (turnsRemaining <= 0) {
					changing = true;
					Hero hero = (Hero) target;
					hero.interrupt();
					hero.spend(TICK);
					startChange();
				}
			}
		}
		spend( TICK );
		return true;
	}

	/** 当前英雄是否为杂散。 */
	public static boolean isJumbleActive() {
		return Dungeon.hero != null
				&& Dungeon.hero.heroClass == HeroClasses.JUMBLE;
	}

	/** 附加或重置变身倒计时（持久化 buff，恒存在）。 */
	public static void resetCountdown() {
		synchronized (STATE_LOCK) {
			if (Dungeon.hero == null) return;
			JumbleChangeBuff b = Buff.affect(Dungeon.hero, JumbleChangeBuff.class);
			b.turnsRemaining = Random.NormalIntRange((int) MIN_INTERVAL, (int) MAX_INTERVAL);
		}
	}

	/** 若变身倒计时 buff 尚未存在，则创建并重置倒计时；已存在则不动。 */
	public static void resetCountdownIfMissing() {
		synchronized (STATE_LOCK) {
			if (Dungeon.hero == null) return;
			JumbleChangeBuff b = Dungeon.hero.buff(JumbleChangeBuff.class);
			if (b == null) {
				resetCountdown();
			}
		}
	}

	@Override
	public void detach() {
	}

	@Override
	public String icon() {
		return BuffIndicator.TIME;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public float iconFadePercent() {
		//随倒计时临近逐渐淡出
		return Math.max(0f, 1f - (turnsRemaining / MAX_INTERVAL));
	}

	@Override
	public String desc() {
		return super.desc() + "\n\n" + Messages.get(this, "countdown", (int) turnsRemaining);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		synchronized (STATE_LOCK) {
			super.storeInBundle(bundle);
			bundle.put(TURNS, turnsRemaining);
			bundle.put(GROUP, currentGroup);
			bundle.put(STORED_EQUIPMENT, storedEquipment);
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		synchronized (STATE_LOCK) {
			super.restoreFromBundle(bundle);
			turnsRemaining = bundle.getFloat(TURNS);
			currentGroup = ((bundle.getInt(GROUP) % 6) + 6) % 6;
			storedEquipment.clear();
			for (Bundlable stored : bundle.getCollection(STORED_EQUIPMENT)) {
				if (stored instanceof Item) storedEquipment.add((Item) stored);
			}
			// 动画和回调不属于存档协议。读档后直接显示已提交的变身结果。
			changing = false;
			visualChangeToken++;
		}
	}
	private void startChange() {
		Hero hero = (Hero) target;
		GLog.w(Messages.get(JumbleChangeBuff.class, "transform"));

		// 先提交全部逻辑结果。动画只展示这次已经确定的变身，不再决定变身是否成功。
		final int token;
		synchronized (STATE_LOCK) {
			try {
				transmuteClassSpecificItems(hero);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
			try {
				doEquipmentTransmute(hero);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
			try {
				doTalentMetamorph(hero);
			} catch (Throwable t) {
				logMetamorphError(t);
			}

			currentGroup = Random.Int(6);
			resetCountdown();
			token = ++visualChangeToken;
		}

		if (hero.sprite instanceof JumbleSprite) {
			JumbleSprite sprite = (JumbleSprite) hero.sprite;

			// 阻塞仍保留为演出效果；超时只负责解锁，不改变已提交的结果。
			try {
				hero.playAnimationNoTime(new Callback() {
					@Override
					public void call() {
						try {
							sprite.playChange(new Callback() {
								@Override
								public void call() {
									try {
										sprite.playAppear(currentGroup, new Callback() {
											@Override
											public void call() {
												finishVisualChange(token);
											}
										});
									} catch (Throwable t) {
										logMetamorphError(t);
										finishVisualChange(token);
									}
								}
							});
						} catch (Throwable t) {
							logMetamorphError(t);
							finishVisualChange(token);
						}
					}
				});
				Game.runOnRenderThread(() -> {
					try {
						VirtualTimer.countTime(VISUAL_TIMEOUT, () -> finishVisualChange(token));
					} catch (Throwable t) {
						logMetamorphError(t);
						finishVisualChange(token);
					}
				});
			} catch (Throwable t) {
				logMetamorphError(t);
				finishVisualChange(token);
			}
		} else {
			finishVisualChange(token);
		}
	}

	private void logMetamorphError(Throwable t) {
		com.watabou.utils.DeviceCompat.log("JumbleChangeBuff", "metamorph error: " + t);
		t.printStackTrace();
	}

	/** 场景重建时调用：旧精灵上的变身演出和兜底计时器都随旧场景丢了，直接把「变身中」状态收掉（逻辑结果早已提交）。 */
	public static void abortPendingVisual() {
		if (Dungeon.hero == null) return;
		JumbleChangeBuff b = Dungeon.hero.buff(JumbleChangeBuff.class);
		if (b == null) return;
		synchronized (STATE_LOCK) {
			if (!b.changing) return;
			b.changing = false;
			b.visualChangeToken++;
		}
	}

	private void finishVisualChange(int token) {
		Hero hero;
		synchronized (STATE_LOCK) {
			if (!changing || token != visualChangeToken) return;
			changing = false;
			hero = (Hero) target;
		}
		if (hero != null) {
			if (hero.sprite instanceof JumbleSprite) {
				JumbleSprite sprite = (JumbleSprite) hero.sprite;
				sprite.cancelAnimationCallback();
				sprite.setGroup(currentGroup);
				if (hero.isAlive()) {
					sprite.idle();
				}
			}
			if (hero.animationBusy) {
				hero.finishAnimationNoTime();
			}
		}
	}

	private void doTalentMetamorph(Hero hero) {
		JumbleTalents.metamorph(hero);
	}

	private void doEquipmentTransmute(Hero hero) {
		try {
			com.shatteredpixel.shatteredpixeldungeon.items.EquipmentTransmuter.transmuteAll(hero);
		} catch (Throwable t) {
			logMetamorphError(t);
		}
	}

	private void transmuteClassSpecificItems(Hero hero) {
		List<Item> items = new ArrayList<>();
		for (Item item : hero.belongings) {
			if (item.isClassSpecific()) items.add(item);
			if (item instanceof Armor) {
				BrokenSeal seal = ((Armor) item).checkSeal();
				if (seal != null) {
					((Armor) item).getToys().remove(seal);
					items.add(seal);
				}
			}
		}
		for (Item oldItem : items) {
			try {
				Item replacement = com.shatteredpixel.shatteredpixeldungeon.items.ChangingProfessionItem.transform(oldItem, hero);
				if (replacement != null) com.shatteredpixel.shatteredpixeldungeon.items.EquipmentTransmuter.replaceEquipped(hero, oldItem, replacement);
			} catch (Throwable t) {
				logMetamorphError(t);
			}
		}

	}
}
