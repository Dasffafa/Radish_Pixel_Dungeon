package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.Card;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDeck;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardDrawer;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardTargeter;
import com.shatteredpixel.shatteredpixeldungeon.items.cards.CardType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class WndCardDrawer extends Window {

	private static final int MARGIN = 4;
	private static final int BUTTON_HEIGHT = 18;
	private static final int GAP = 3;

	private final Hero hero;
	private final int width;
	private final int inner;
	private float pos;

	public WndCardDrawer(Hero hero) {
		super();
		this.hero = hero;
		CardDeck deck = CardDeck.getOrCreate(hero);
		width = Math.min(160, PixelScene.uiCamera.width - chrome.marginHor());
		inner = width - MARGIN * 2;

		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width - title.width()) / 2f, MARGIN);
		add(title);
		pos = title.bottom() + MARGIN;

		CardDrawer.DrawCooldown cd = hero.buff(CardDrawer.DrawCooldown.class);
		int cooldown = cd == null ? 0 : (int) cd.cooldown() + 1;
		String status = Messages.get(this, "counts_no_discard", deck.drawPile.size());
		if (deck.freeDraws > 0) {
			status += "\n" + Messages.get(this, "free_draws", deck.freeDraws);
		} else if (cooldown > 0) {
			status += "\n" + Messages.get(this, "cooldown", cooldown);
		}
		if (deck.deletesLeft > 0) {
			status += "\n" + Messages.get(this, "deletes", deck.deletesLeft);
		}
		RenderedTextBlock info = PixelScene.renderTextBlock(status, 6);
		info.maxWidth(inner);
		info.setPos(MARGIN, pos);
		add(info);
		pos = info.bottom() + MARGIN;

		// 卡面预览：手上卡牌 + 牌堆顶
		pos = addCardPreview(deck, pos);

		if (deck.held != null) {
			Card card = deck.held;
			String heldLabel = Messages.get(this, "held", card.name());
			// 陷阱卡：显示出吸收的陷阱名称
			if (card.type == com.shatteredpixel.shatteredpixeldungeon.items.cards.CardType.TRAP
					&& card.trapClass != null) {
				heldLabel += "\n" + Messages.get(this, "trap_content",
						Messages.get(card.trapClass, "name"));
			}
			RedButton play = new RedButton(heldLabel) {
				@Override
				protected void onClick() {
					hide();
					CardTargeter.play(hero, card);
				}
			};
			play.icon(new ItemSprite(card.type.image));
			play.leftJustify = true;
			play.setRect(MARGIN, pos, inner - BUTTON_HEIGHT - GAP, BUTTON_HEIGHT);
			add(play);

			IconButton cardInfo = new IconButton(Icons.get(Icons.INFO)) {
				@Override
				protected void onClick() {
					GameScene.show(new WndTitledMessage(
							new ItemSprite(card.type.image), card.name(), card.desc()));
				}
			};
			cardInfo.setRect(play.right() + GAP, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
			add(cardInfo);
			pos = play.bottom() + GAP;
		} else {
			RenderedTextBlock empty = PixelScene.renderTextBlock(Messages.get(this, "hand_empty"), 6);
			empty.maxWidth(inner);
			empty.setPos(MARGIN, pos);
			add(empty);
			pos = empty.bottom() + GAP;
		}

		RedButton draw = new RedButton(Messages.get(this, "draw")) {
			@Override
			protected void onClick() {
				if (CardDrawer.drawCard(hero)) {
					hide();
					hero.spendAndNext(1f);
					GameScene.show(new WndCardDrawer(hero));
				}
			}
		};
		draw.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		draw.enable(deck.held == null && deck.totalSize() > 0 && (cooldown <= 0 || deck.freeDraws > 0));
		add(draw);
		pos = draw.bottom() + GAP;

		RedButton viewDraw = new RedButton(Messages.get(this, "view_draw")) {
			@Override
			protected void onClick() {
				openPile(deck.drawPile, Messages.get(WndCardDrawer.class, "draw_pile"));
			}
		};
		viewDraw.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		add(viewDraw);
		pos = viewDraw.bottom() + GAP;

		RedButton delete = new RedButton(Messages.get(this, "delete", deck.deletesLeft)) {
			@Override
			protected void onClick() {
				if (deck.deletesLeft <= 0) return;
				ArrayList<Card> all = deck.allCards();
				GameScene.show(new WndCardSelect(
						Messages.get(WndCardDrawer.class, "delete_title"),
						Messages.get(WndCardDrawer.class, "delete_desc"),
						all, 1, false, selected -> {
							if (selected.isEmpty()) return;
							Card c = selected.get(0);
							if (deck.delete(c)) {
								deck.deletesLeft--;
								if (deck.held == null) HeldCardTracker.clear(hero);
								hide();
								GameScene.show(new WndCardDrawer(hero));
							}
						}));
			}
		};
		delete.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		delete.enable(deck.deletesLeft > 0);
		add(delete);
		pos = delete.bottom() + GAP;

		if (deck.cleansesLeft > 0) {
			RedButton cleanse = new RedButton(Messages.get(this, "cleanse", deck.cleansesLeft)) {
				@Override
				protected void onClick() {
					if (deck.cleansesLeft <= 0) return;
					ArrayList<Card> all = new ArrayList<>();
					for (Card c : deck.allCards()) {
						if (c.type != com.shatteredpixel.shatteredpixeldungeon.items.cards.CardType.BLANK) {
							all.add(c);
						}
					}
					GameScene.show(new WndCardSelect(
							Messages.get(WndCardDrawer.class, "cleanse_title"),
							Messages.get(WndCardDrawer.class, "cleanse_desc"),
							all, 1, false, selected -> {
								if (selected.isEmpty()) return;
								Card c = selected.get(0);
								c.type = com.shatteredpixel.shatteredpixeldungeon.items.cards.CardType.BLANK;
								c.trapClass = null;
								deck.cleansesLeft--;
								hide();
								GameScene.show(new WndCardDrawer(hero));
							}));
				}
			};
			cleanse.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
			add(cleanse);
			pos = cleanse.bottom() + GAP;
		}

		if (hero.subClass == HeroSubClasses.ILLUSIONIST) {
			RedButton compress = new RedButton(Messages.get(this, "compress")) {
				@Override
				protected void onClick() {
					if (CardDrawer.compress(hero)) {
						hide();
						hero.spendAndNext(1f);
						GameScene.show(new WndCardDrawer(hero));
					}
				}
			};
			compress.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
			add(compress);
			pos = compress.bottom() + GAP;
		}

		if (!deck.stored.isEmpty()) {
			RenderedTextBlock storedInfo = PixelScene.renderTextBlock(
					Messages.get(this, "stored_info", deck.stored.size()), 6);
			storedInfo.maxWidth(inner);
			storedInfo.setPos(MARGIN, pos);
			add(storedInfo);
			pos = storedInfo.bottom() + GAP;

			for (Card c : new ArrayList<>(deck.stored)) {
				RedButton playStored = new RedButton(Messages.get(this, "stored_card", c.name())) {
					@Override
					protected void onClick() {
						hide();
						com.shatteredpixel.shatteredpixeldungeon.items.cards.CardEffects.playStored(hero, c);
					}
				};
				playStored.icon(new ItemSprite(c.type.image));
				playStored.leftJustify = true;
				playStored.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
				add(playStored);
				pos = playStored.bottom() + GAP;
			}
		}

		RedButton close = new RedButton(Messages.get(this, "close")) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
		add(close);
		pos = close.bottom() + MARGIN;

		resize(width, (int) pos);
	}

	/** 卡面预览行：显示手上卡牌与牌堆顶的卡图。 */
	private float addCardPreview(CardDeck deck, float top) {
		ArrayList<Card> preview = new ArrayList<>();
		if (deck.held != null) preview.add(deck.held);
		if (!deck.drawPile.isEmpty()) {
			Card next = deck.drawPile.get(deck.drawPile.size() - 1);
			if (next != null && next != deck.held) preview.add(next);
		}
		if (preview.isEmpty()) return top;

		int scale = 3;            // 16px 放大 3 倍
		int iconSize = 16 * scale;
		int gap = 6;
		float x = MARGIN;

		for (Card c : preview) {
			if (c == null || c.type == null) continue;
			com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite sprite =
					new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite(c.type.image);
			sprite.scale.set(scale);
			sprite.x = x;
			sprite.y = top;
			add(sprite);
			x += iconSize + gap;
		}
		return top + iconSize + MARGIN;
	}

	private void openPile(ArrayList<Card> pile, String title) {
		ArrayList<Card> shown = new ArrayList<>();
		if (pile != null) {
			// 抽牌堆顶在 drawPile 末尾，按抽牌顺序从上到下展示
			for (int i = pile.size() - 1; i >= 0; i--) {
				Card c = pile.get(i);
				if (c != null && c.type != null) shown.add(c);
			}
		}
		if (shown.isEmpty()) {
			hide();
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i(
					Messages.get(WndCardDrawer.class, "pile_empty"));
			return;
		}
		GameScene.show(new WndCardPile(title, shown));
	}

	/** 按抽牌顺序以图标网格展示抽牌堆，点击图标查看卡牌名称与详情。 */
	private static class WndCardPile extends Window {

		private static final int CELL = 22;
		private static final int MAX_LIST_HEIGHT = 120;

		private ScrollPane pane;
		private boolean paneSized;
		private int paneX, paneY, paneW, paneH;

		WndCardPile(String title, ArrayList<Card> cards) {
			super();
			int width = Math.min(160, PixelScene.uiCamera.width - chrome.marginHor());
			int inner = width - MARGIN * 2;

			RenderedTextBlock heading = PixelScene.renderTextBlock(title, 9);
			heading.hardlight(TITLE_COLOR);
			heading.maxWidth(inner);
			heading.setPos(MARGIN, MARGIN);
			add(heading);

			float pos = heading.bottom() + MARGIN;
			RenderedTextBlock info = PixelScene.renderTextBlock(
					Messages.get(WndCardDrawer.class, "pile_desc", cards.size()), 6);
			info.maxWidth(inner);
			info.setPos(MARGIN, pos);
			add(info);
			pos = info.bottom() + MARGIN;

			int cols = Math.max(1, inner / CELL);
			float xOffset = (inner - cols * CELL) / 2f;

			Component content = new Component();
			for (int i = 0; i < cards.size(); i++) {
				Card card = cards.get(i);
				int col = i % cols;
				int row = i / cols;
				float cx = xOffset + col * CELL;
				float cy = row * CELL;

				IconButton btn = new IconButton(new ItemSprite(card.type.image)) {
					@Override
					protected void onClick() {
						String name = card.name() + (card.power > 1 ? " +" + (card.power - 1) : "");
						String desc = card.desc();
						if (card.type == CardType.TRAP && card.trapClass != null) {
							desc += "\n\n" + Messages.get(WndCardDrawer.class, "trap_content",
									Messages.get(card.trapClass, "name"));
						}
						GameScene.show(new WndTitledMessage(
								new ItemSprite(card.type.image), name, desc));
					}
				};
				btn.setRect(cx, cy, CELL, CELL);
				content.add(btn);

				if (card.power > 1) {
					BitmapText lvl = new BitmapText("+" + (card.power - 1), PixelScene.pixelFont);
					lvl.hardlight(TITLE_COLOR);
					lvl.measure();
					lvl.x = cx + CELL - lvl.width() - 1;
					lvl.y = cy + CELL - lvl.height() - 1;
					content.add(lvl);
				}
			}

			int rows = (cards.size() + cols - 1) / cols;
			float totalH = Math.max(CELL, rows * CELL);
			content.setSize(inner, totalH);

			int listHeight = (int) Math.min(MAX_LIST_HEIGHT, totalH);
			pane = new ScrollPane(content);
			paneX = MARGIN;
			paneY = (int) pos;
			paneW = inner;
			paneH = listHeight;
			add(pane);
			pos += listHeight + MARGIN;

			RedButton close = new RedButton(Messages.get(WndCardDrawer.class, "close")) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			close.setRect(MARGIN, pos, inner, BUTTON_HEIGHT);
			add(close);

			resize(width, (int) (close.bottom() + MARGIN));
		}

		@Override
		public void update() {
			super.update();
			if (!paneSized && pane != null && camera() != null) {
				pane.setRect(paneX, paneY, paneW, paneH);
				paneSized = true;
			}
		}
	}
}
