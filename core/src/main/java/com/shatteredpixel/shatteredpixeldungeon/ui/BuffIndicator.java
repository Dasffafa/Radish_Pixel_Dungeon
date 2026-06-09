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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoBuff;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.GameMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class BuffIndicator extends Component {

	//transparent icon
	public static final int NONE    = 127;

	//FIXME this is becoming a mess, should do a big cleaning pass on all of these
	//and think about tinting options
	public static final int MIND_VISION = 0;
	public static final int LEVITATION  = 1;
	public static final int FIRE        = 2;
	public static final int POISON      = 3;
	public static final int PARALYSIS   = 4;
	public static final int HUNGER      = 5;
	public static final int STARVATION  = 6;
	public static final int TIME        = 7;
	public static final int OOZE        = 8;
	public static final int AMOK        = 9;
	public static final int TERROR      = 10;
	public static final int ROOTS       = 11;
	public static final int INVISIBLE   = 12;
	public static final int SHADOWS     = 13;
	public static final int WEAKNESS    = 14;
	public static final int FROST       = 15;
	public static final int BLINDNESS   = 16;
	public static final int COMBO       = 17;
	public static final int FURY        = 18;
	public static final int HERB_HEALING= 19;
	public static final int ARMOR       = 20;
	public static final int HEART       = 21;
	public static final int LIGHT       = 22;
	public static final int CRIPPLE     = 23;
	public static final int BARKSKIN    = 24;
	public static final int IMMUNITY    = 25;
	public static final int BLEEDING    = 26;
	public static final int MARK        = 27;
	public static final int DEFERRED    = 28;
	public static final int DROWSY      = 29;
	public static final int MAGIC_SLEEP = 30;
	public static final int THORNS      = 31;
	public static final int FORESIGHT   = 32;
	public static final int VERTIGO     = 33;
	public static final int RECHARGING  = 34;
	public static final int LOCKED_FLOOR= 35;
	public static final int CORRUPT     = 36;
	public static final int BLESS       = 37;
	public static final int RAGE        = 38;
	public static final int SACRIFICE   = 39;
	public static final int BERSERK     = 40;
	public static final int HASTE       = 41;
	public static final int PREPARATION = 42;
	public static final int WELL_FED    = 43;
	public static final int HEALING     = 44;
	public static final int WEAPON      = 45;
	public static final int VULNERABLE  = 46;
	public static final int HEX         = 47;
	public static final int DEGRADE     = 48;
	public static final int PINCUSHION  = 49;
	public static final int UPGRADE     = 50;
	public static final int MOMENTUM    = 51;
	public static final int ANKH        = 52;
	public static final int NOINV       = 53;
	public static final int TARGETED    = 54;
	public static final int IMBUE       = 55;
	public static final int ENDURE      = 56;
	public static final int INVERT_MARK = 57;
	public static final int NATURE_POWER= 58;
	public static final int AMULET      = 59;
	public static final int DUEL_CLEAVE = 60;
	public static final int DUEL_GUARD  = 61;
	public static final int DUEL_SPIN   = 62;
	public static final int DUEL_EVASIVE= 63;
	public static final int DUEL_DANCE  = 64;
	public static final int DUEL_BRAWL  = 65;
	public static final int DUEL_XBOW   = 66;
	public static final int CHALLENGE   = 67;
	public static final int MONK_ENERGY = 68;
	public static final int DUEL_COMBO  = 69;
	public static final int DAZE        = 70;

	public static final int HOLD_BREATH = 71;
	public static final int A_EVA	    = 72;
	//	public static final int DUEL_DANCE  = 73;
	public static final int SCYTHE_S    = 78;

	public static final int FOG_ROAD    = 79;

	public static final int TAI_COLD    = 80;

	public static final int TAI_CRIT    = 82;

	public static final int WAND    	= 83;

	public static final int BELIEF_LINK    	= 84;
	public static final int BELIEF_DNOT    	= 85;

	public static final int SIZE_SMALL  = 7;
	public static final int SIZE_LARGE  = 16;

	private static BuffIndicator heroInstance;
	// 修改：将单个bossInstance改为数组，支持最多4个Boss
	private static BuffIndicator[] bossInstances = new BuffIndicator[4];

	private LinkedHashMap<Buff, BuffButton> buffButtons = new LinkedHashMap<>();
	public boolean needsRefresh;
	private Char ch;

	private boolean large = false;

	public BuffIndicator( Char ch, boolean large ) {
		super();

		this.ch = ch;
		this.large = large;
		if (ch == Dungeon.hero) {
			heroInstance = this;
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		if (this == heroInstance) {
			heroInstance = null;
		}
		// 修改：销毁时清理对应的boss实例引用
		for (int i = 0; i < bossInstances.length; i++) {
			if (bossInstances[i] == this) {
				bossInstances[i] = null;
				break;
			}
		}
	}

	@Override
	public synchronized void update() {
		super.update();
		if (needsRefresh){
			needsRefresh = false;
			layout();
		}
	}

	private boolean buffsHidden = false;

	@Override
	protected void layout() {

		ArrayList<Buff> newBuffs = new ArrayList<>();
		for (Buff buff : ch.buffs()) {
			if (buff.icon() != NONE) {
				newBuffs.add(buff);
			}
		}

		int size = large ? SIZE_LARGE : SIZE_SMALL;

		//remove any icons no longer present
		for (Buff buff : buffButtons.keySet().toArray(new Buff[0])){
			if (!newBuffs.contains(buff)){
				Image icon = buffButtons.get( buff ).icon;
				icon.originToCenter();
				icon.alpha(0.6f);
				add( icon );
				add( new AlphaTweener( icon, 0, 0.6f ) {
					@Override
					protected void updateValues( float progress ) {
						super.updateValues( progress );
						image.scale.set( 1 + 5 * progress );
					}

					@Override
					protected void onComplete() {
						image.killAndErase();
					}
				} );

				buffButtons.get( buff ).destroy();
				remove(buffButtons.get( buff ));
				buffButtons.remove( buff );
			}
		}

		//add new icons
		for (Buff buff : newBuffs) {
			if (!buffButtons.containsKey(buff)) {
				BuffButton icon = new BuffButton(buff, large);
				add(icon);
				buffButtons.put( buff, icon );
			}
		}

		//layout
		int pos = 0;
		float lastIconLeft = 0;
		for (BuffButton icon : buffButtons.values()){
			icon.updateIcon();
			//button areas are slightly oversized, especially on small buttons
			icon.setRect(x + pos * (size + 1), y, size + 1, size + (large ? 0 : 5));
			PixelScene.align(icon);
			pos++;

			icon.visible = icon.left() <= right();
			lastIconLeft = icon.left();
		}

		buffsHidden = false;
		//squish buff icons together if there isn't enough room
		float excessWidth = lastIconLeft - right();
		if (excessWidth > 0) {
			float leftAdjust = excessWidth/(buffButtons.size()-1);
			//can't squish by more than 50% on large and 62% on small
			if (large && leftAdjust >= size*0.48f) leftAdjust = size*0.5f;
			if (!large && leftAdjust >= size*0.62f) leftAdjust = size*0.65f;
			float cumulativeAdjust = leftAdjust * (buffButtons.size()-1);

			ArrayList<BuffButton> buttons = new ArrayList<>(buffButtons.values());
			Collections.reverse(buttons);
			for (BuffButton icon : buttons) {
				icon.setPos(icon.left() - cumulativeAdjust, icon.top());
				icon.visible = icon.left() <= right();
				if (!icon.visible) buffsHidden = true;
				PixelScene.align(icon);
				bringToFront(icon);
				icon.givePointerPriority();
				cumulativeAdjust -= leftAdjust;
			}
		}
	}

	public boolean allBuffsVisible(){
		return !buffsHidden;
	}

	private static class BuffButton extends IconButton {

		private Buff buff;

		private boolean large;

		public Image grey; //only for small
		public BitmapText text; //only for large

		public BuffButton( Buff buff, boolean large ){
			super( new BuffIcon(buff, large));
			this.buff = buff;
			this.large = large;

			bringToFront(grey);
			bringToFront(text);
		}

		@Override
		protected void createChildren() {
			super.createChildren();
			grey = new Image( TextureCache.createSolid(0xCC666666));
			add( grey );

			text = new BitmapText(PixelScene.pixelFont);
			add( text );
		}

		public void updateIcon(){
			((BuffIcon)icon).refresh(buff);
			//如果有文字显示（如剩余次数），则显示文字而非灰色进度条
			if (!buff.iconTextDisplay().isEmpty()) {
				text.visible = true;
				grey.visible = false;
				if (buff.type == Buff.buffType.POSITIVE)        text.hardlight(CharSprite.POSITIVE);
				else if (buff.type == Buff.buffType.NEGATIVE)   text.hardlight(CharSprite.NEGATIVE);
				text.alpha(0.7f);

				text.text(buff.iconTextDisplay());
				text.measure();
			} else {
				//round up to the nearest pixel if <50% faded, otherwise round down
				text.visible = false;
				grey.visible = true;
				float fadeHeight = GameMath.gate(0, buff.iconFadePercent(), 1) * icon.height();
				float zoom = (camera() != null) ? camera().zoom : 1;
				if (fadeHeight < icon.height() / 2f) {
					grey.scale.set(icon.width(), (float) Math.ceil(zoom * fadeHeight) / zoom);
				} else {
					grey.scale.set(icon.width(), (float) Math.floor(zoom * fadeHeight) / zoom);
				}
			}
		}

		@Override
		protected void layout() {
			super.layout();
			grey.x = icon.x = this.x + (large ? 0 : 1);
			grey.y = icon.y = this.y + (large ? 0 : 2);

			//小型图标使用更小的字体
			if (!large) {
				text.scale.set(PixelScene.align(0.5f));
			} else if (text.width > width()){
				text.scale.set(PixelScene.align(0.5f));
			} else {
				text.scale.set(1f);
			}
			text.x = this.x + width() - text.width() - 1;
			text.y = this.y + width() - text.baseLine() - 2;
		}

		@Override
		protected void onClick() {
			if (buff.icon() != NONE) GameScene.show(new WndInfoBuff(buff));
		}

		@Override
		protected void onPointerDown() {
			//don't affect buff color
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			//don't affect buff color
		}

		@Override
		protected String hoverText() {
			return Messages.titleCase(buff.name());
		}
	}

	public static void refreshHero() {
		if (heroInstance != null) {
			heroInstance.needsRefresh = true;
		}
	}

	/**
	 * 兼容原有方法：刷新第一个Boss的Buff指示器
	 */
	public static void refreshBoss(){
		// 刷新第一个Boss
		if (bossInstances[0] != null) {
			bossInstances[0].needsRefresh = true;
		}
	}

	/**
	 * 新增方法：刷新所有活跃的Boss Buff指示器
	 */
	public static void refreshAllBosses() {
		for (BuffIndicator instance : bossInstances) {
			if (instance != null) {
				instance.needsRefresh = true;
			}
		}
	}

	/**
	 * 兼容原有方法：设置第一个Boss的Buff实例
	 */
	public static void setBossInstance(BuffIndicator boss){
		bossInstances[0] = boss;
	}

	/**
	 * 新增方法：设置指定索引的Boss Buff实例（适配多Boss血条）
	 */
	public static void setBossInstance(int index, BuffIndicator boss) {
		if (index >= 0 && index < bossInstances.length) {
			bossInstances[index] = boss;
		}
	}

	/**
	 * 新增方法：获取指定索引的Boss Buff实例
	 */
	public static BuffIndicator getBossInstance(int index) {
		if (index >= 0 && index < bossInstances.length) {
			return bossInstances[index];
		}
		return null;
	}
}