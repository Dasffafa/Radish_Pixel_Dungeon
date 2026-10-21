package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Druid;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

/**
 * 德鲁伊法术书（自然法术选择界面）。
 *
 * <p>布局对齐破碎的牧师法术书：可滚动的法术列表（图标 + 名称 + 消耗），
 * 选中后在下方显示该法术的完整说明，最后用「释放」按钮施放。</p>
 */
public class WndDruidSkills extends Window {

	private static final int WIDTH = 132;
	private static final int MARGIN = 4;
	private static final int ROW_HEIGHT = 17;
	private static final int LIST_HEIGHT = 86;
	private static final int GAP = 3;

	private static final String[] KEYS = {
			"boar", "wolves", "bear", "oak_sage", "vines",
			"sunflower", "enchant_arrow", "firestorm", "thundercloud", "cataclysm"
	};
	private static final int[] COSTS = { 12, 16, 20, 15, 10, 30, 5, 10, 16, 30 };

	private final Druid.DruidEnergy energy;
	private final int inner;

	private RedButton[] rows = new RedButton[KEYS.length];
	private RenderedTextBlock desc;
	private RedButton cast;
	private int selected = -1;
	private float descTop;

	public WndDruidSkills( Druid.DruidEnergy energy ) {
		this.energy = energy;
		inner = WIDTH - MARGIN * 2;

		IconTitle titlebar = new IconTitle();
		titlebar.setRect( 0, -3, WIDTH, 0 );
		titlebar.icon( new HeroIcon( HeroIcon.DRUID_SKILL ) );
		titlebar.label( Messages.get( this, "title" ) );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock(
				Messages.get( this, "energy", Math.round( Druid.energy( Dungeon.hero ) ),
						(int) Druid.maxEnergy( Dungeon.hero ) ), 6 );
		message.maxWidth( inner );
		message.setPos( MARGIN, titlebar.bottom() + GAP );
		add( message );

		float top = message.bottom() + GAP;

		// 法术列表（滚动）
		Component listContent = new Component();
		ScrollPane pane = new ScrollPane( listContent );
		add( pane );
		pane.setRect( MARGIN, top, inner, LIST_HEIGHT );

		float y = 0;
		for (int i = 0; i < KEYS.length; i++) {
			final int index = i;
			RedButton row = new RedButton( label( i ) ) {
				@Override
				protected void onClick() {
					select( index );
				}
			};
			row.icon( new HeroIcon( HeroIcon.DRUID_SKILL ) );
			row.leftJustify = true;
			row.setRect( 0, y, inner - (pane.width() < inner ? 0 : 6), ROW_HEIGHT );
			listContent.add( row );
			rows[i] = row;
			y += ROW_HEIGHT + 1;
		}
		listContent.setSize( inner, y );

		descTop = top + LIST_HEIGHT + GAP;
		desc = PixelScene.renderTextBlock( 6 );
		desc.maxWidth( inner );
		desc.setPos( MARGIN, descTop );
		add( desc );

		cast = new RedButton( Messages.get( this, "cast" ) ) {
			@Override
			protected void onClick() {
				if (selected < 0) return;
				hide();
				runSkill( selected );
			}
		};
		add( cast );

		RedButton close = new RedButton( Messages.get( this, "cancel" ) ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		add( close );

		float btnY = descTop + 34;
		cast.setRect( MARGIN, btnY, inner / 2f - 1, 18 );
		close.setRect( MARGIN + inner / 2f + 1, btnY, inner / 2f - 1, 18 );

		select( firstAffordable() );
		resize( WIDTH, (int) (btnY + 18 + MARGIN) );
	}

	private String label( int i ) {
		return Messages.get( this, KEYS[i] ) + "  (" + COSTS[i] + ")";
	}

	private int firstAffordable() {
		Hero hero = Dungeon.hero;
		int have = hero == null ? 0 : Math.round( Druid.energy( hero ) );
		for (int i = 0; i < COSTS.length; i++) {
			if (COSTS[i] <= have) return i;
		}
		return 0;
	}

	private void select( int index ) {
		selected = index;
		for (int i = 0; i < rows.length; i++) {
			rows[i].textColor( i == index ? 0xFFFF44 : 0xFFFFFF );
		}
		desc.text( Messages.get( this, KEYS[index] + ".desc" ), inner );
		desc.setPos( MARGIN, descTop );
		Hero hero = Dungeon.hero;
		int have = hero == null ? 0 : Math.round( Druid.energy( hero ) );
		cast.enable( have >= COSTS[index] );
	}

	private void runSkill( int i ) {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return;
		}
		boolean ok;
		switch (i) {
			case 0: ok = Druid.summonBoar( hero ); break;
			case 1: ok = Druid.summonWolves( hero ); break;
			case 2: ok = Druid.summonBear( hero ); break;
			case 3: ok = Druid.summonOakSage( hero ); break;
			case 4: ok = Druid.summonVines( hero ); break;
			case 5: ok = Druid.summonSunflower( hero ); break;
			case 6: ok = Druid.castEnchantArrow( hero ); break;
			case 7: Druid.promptTargeted( hero, 0 ); return;
			case 8: Druid.promptTargeted( hero, 1 ); return;
			case 9: Druid.promptTargeted( hero, 2 ); return;
			default: return;
		}
		if (ok) {
			Sample.INSTANCE.play( Assets.Sounds.READ );
			hero.sprite.operate( hero.pos );
			hero.spendAndNext( 1f );
		} else {
			GLog.w( Messages.get( this, "no_energy" ) );
		}
	}
}
