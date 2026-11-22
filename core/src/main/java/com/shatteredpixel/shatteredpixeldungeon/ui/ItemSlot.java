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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.utils.Rect;

public class ItemSlot extends Button {

	public static final int DEGRADED	= 0xFF4444;
	public static final int UPGRADED	= 0x44FF44;
	public static final int FADED       = 0x999999;
	public static final int WARNING		= 0xFF8800;
	public static final int ENHANCED	= 0x3399FF;
	public static final int MASTERED	= 0xFFFF44;
	public static final int CURSE_INFUSED	= 0x8800FF;

	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;

	private Rect margin = new Rect();

	protected ItemSprite sprite;
	protected Item       item;
	protected BitmapText status;
	protected BitmapText extra;
	protected Image      itemIcon;
	/** 附魔物品的旋转光环 */
	protected BitmapText level;
	protected BitmapText special;

	private static final String TXT_STRENGTH	= ":%d";
	private static final String TXT_TYPICAL_STR	= "%d?";

	private static final String TXT_LEVEL	= "%+d";

	// Special "virtual items"
	public static final Item CHEST = new Item() {
		public String image() { return ItemSpriteSheet.CHEST; }
		public String name() { return Messages.get(Heap.class, "chest"); }
	};
	public static final Item LOCKED_CHEST = new Item() {
		public String image() { return ItemSpriteSheet.LOCKED_CHEST; }
		public String name() { return Messages.get(Heap.class, "locked_chest"); }
	};
	public static final Item CRYSTAL_CHEST = new Item() {
		public String image() { return ItemSpriteSheet.CRYSTAL_CHEST; }
		public String name() { return Messages.get(Heap.class, "crystal_chest"); }
	};
	public static final Item TOMB = new Item() {
		public String image() { return ItemSpriteSheet.TOMB; }
		public String name() { return Messages.get(Heap.class, "tomb"); }
	};
	public static final Item SKELETON = new Item() {
		public String image() { return ItemSpriteSheet.BONES; }
		public String name() { return Messages.get(Heap.class, "skeleton"); }
	};
	public static final Item REMAINS = new Item() {
		public String image() { return ItemSpriteSheet.REMAINS; }
		public String name() { return Messages.get(Heap.class, "remains"); }
	};

	public ItemSlot() {
		super();
		sprite.visible(false);
		enable(false);
	}

	public ItemSlot( Item item ) {
		this();
		item( item );
	}

	@Override
	protected void createChildren() {

		super.createChildren();

		sprite = new ItemSprite();
		add(sprite);

		status = new BitmapText( PixelScene.pixelFont);
		add(status);

		extra = new BitmapText( PixelScene.pixelFont);
		add(extra);

		level = new BitmapText( PixelScene.pixelFont);
		add(level);

		special = new BitmapText( PixelScene.pixelFont);
		add(special);
	}

	@Override
	protected void layout() {
		super.layout();

		sprite.x = x + margin.left + (width - sprite.width - (margin.left + margin.right)) / 2f;
		sprite.y = y + margin.top + (height - sprite.height - (margin.top + margin.bottom)) / 2f;
		PixelScene.align(sprite);

		if (status != null) {
			status.measure();
			if (status.width > width - (margin.left + margin.right)){
				status.scale.set(PixelScene.align(0.8f));
			} else {
				status.scale.set(1f);
			}
			status.x = x + margin.left;
			status.y = y + margin.top;
			PixelScene.align(status);
		}

		if (extra != null) {
			extra.x = x + (width - extra.width()) - margin.right;
			extra.y = y + margin.top;
			PixelScene.align(extra);

			if ((status.width() + extra.width()) > width){
				extra.visible = false;
			} else if (item != null) {
				extra.visible = true;
			}
		}

		if (itemIcon != null){
			itemIcon.x = x + width - (ItemSpriteSheet.Icons.SIZE + itemIcon.width())/2f - margin.right;
			itemIcon.y = y + (ItemSpriteSheet.Icons.SIZE - itemIcon.height)/2f + margin.top;
			PixelScene.align(itemIcon);
		}

		if (level != null) {
			level.x = x + (width - level.width()) - margin.right;
			level.y = y + (height - level.baseLine() - 1) - margin.bottom;
			PixelScene.align(level);
		}
		if (special!=null){
			special.measure();
			if (special.width > width - (margin.left + margin.right)){
				special.scale.set(PixelScene.align(0.8f));
			} else {
				special.scale.set(1f);
			}
			special.x = x + margin.left;
			special.y = y + (height - level.baseLine() - 1) - margin.bottom;
			PixelScene.align(special);
		}
	}

	public void alpha( float value ){
		if (!active) value *= 0.3f;
		if (sprite != null)     sprite.alpha(value);
		if (extra != null)      extra.alpha(value);
		if (status != null)     status.alpha(value);
		if (itemIcon != null)   itemIcon.alpha(value);
		if (level != null)      level.alpha(value);
	}

	public void clear(){
		item(null);
		enable(true);
		sprite.visible(true);
		sprite.view(ItemSpriteSheet.SOMETHING, null);
		layout();
	}

	public void item( Item item ) {
		if (this.item == item) {
			if (item != null) {
				sprite.view( item );
			}
			updateAura();
			updateText();
			return;
		}

		this.item = item;

		if (item == null) {

			enable(false);
			sprite.visible(false);

			updateText();

		} else {

			enable(true);
			sprite.visible(true);

			sprite.view( item );
			updateAura();
			updateText();
		}
	}

	// ======================================================================
	//  附魔物品的光效（参数抄自 APK 反编译的 effects/fx/enchant/ItemAura 与 ui/ItemSlot）
	//  1) streak：2s 一次、0.1s、-45° 的高光
	//  2) stillRing：2s 一次、前 0.53s 的脚下光环 RING_5
	//  3) 边框火花：沿格子边框跑动的 3 连块高光（数量按升级等级）
	//  4) vicious：刀身斜向火花 SPARK_2
	// ======================================================================

	/** 测试用：这个格子有没有附魔光效 */
	public boolean hasAura(){ return hasShine || sparkBlocks > 0; }
	/** 测试用：有多少可见火花块跑到了本格子外面 */
	public int sparksOutside(){
		int bad = 0;
		for (int i = 0; i < sparkBlocks; i++){
			com.watabou.noosa.ColorBlock b = sparks[i];
			if (b == null || !b.visible) continue;
			if (b.x < x || b.x > x + width || b.y < y || b.y > y + height) bad++;
		}
		return bad;
	}

	/** 测试用：取第 i 个边框火花块 */
	public com.watabou.noosa.ColorBlock sparkBlock( int i ){
		return i >= 0 && i < sparks.length ? sparks[i] : null;
	}

	/** 测试用：各 motif 是否可见 */
	public boolean streakVisible(){ return shine != null && shine.visible; }
	public boolean ringVisible(){ return ring != null && ring.visible; }
	public boolean viciousVisible(){ return vicious != null && vicious.visible; }
	public int borderSparksVisible(){
		int n = 0;
		for (int i = 0; i < sparkBlocks; i++) if (sparks[i] != null && sparks[i].visible) n++;
		return n;
	}

	/** 测试用：此刻有几种光效可见 */
	public int shineActive(){
		int n = 0;
		if (shine != null && shine.visible) n++;
		if (ring != null && ring.visible) n++;
		if (vicious != null && vicious.visible) n++;
		for (int i = 0; i < sparkBlocks; i++) if (sparks[i] != null && sparks[i].visible) n++;
		return n;
	}

	private static final float STREAK_PERIOD = 2f, STREAK_FOR = 0.1f;
	private static final float RING_FOR = 0.53f;
	private static final float SPARK_MIN_SPEED = 6f, SPARK_MAX_SPEED = 16f;

	private Image shine;
	private Image ring;
	private Image vicious;
	private final com.watabou.noosa.ColorBlock[] sparks = new com.watabou.noosa.ColorBlock[15];
	private int sparkBlocks;
	private final float[] sparkPos = new float[5];
	private final float[] sparkSpeed = new float[5];
	private boolean hasShine;
	private int shineColor;
	private float phase = com.watabou.utils.Random.Float( 2f );

	/** 附魔/升级等级决定火花数量（Warped 用稀有度，这里用等级近似） */
	private int sparkCount(){
		if (item == null) return 0;
		int lvl = Math.max( 0, item.level() );
		int n = 2 + Math.min( 3, lvl / 2 );
		return hasShine ? n : (lvl >= 2 ? 1 : 0);
	}

	protected void updateAura(){
		int color = item == null ? 0
				: com.shatteredpixel.shatteredpixeldungeon.effects.fx.enchant.EnchantFx.uiColor( item );

		hasShine = color != 0;
		if (!hasShine && (item == null || item.level() <= 1)){
			hideAll();
			return;
		}
		if (color == 0) color = 0xE8F0FF; // 只有升级、没有附魔：冷白
		shineColor = color;

		if (shine == null){
			int[] f = com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames.STREAK_TAPER;
			shine = new Image( com.shatteredpixel.shatteredpixeldungeon.Assets.Effects.FX_SHEET, f[0], f[1], f[2], f[3] );
			shine.visible = false;
			add( shine );
		}
		if (ring == null){
			int[] f = com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames.RING_5;
			ring = new Image( com.shatteredpixel.shatteredpixeldungeon.Assets.Effects.FX_SHEET, f[0], f[1], f[2], f[3] );
			ring.visible = false;
			add( ring );
		}
		if (vicious == null){
			int[] f = com.shatteredpixel.shatteredpixeldungeon.effects.fx.FxFrames.SPARK_2;
			vicious = new Image( com.shatteredpixel.shatteredpixeldungeon.Assets.Effects.FX_SHEET, f[0], f[1], f[2], f[3] );
			vicious.visible = false;
			add( vicious );
		}
		if (sparks[0] == null){
			for (int i = 0; i < sparks.length; i++){
				sparks[i] = new com.watabou.noosa.ColorBlock( 1f, 1f, -1 );
				sparks[i].visible = false;
				add( sparks[i] );
			}
		}

		shine.hardlight( color );
		ring.hardlight( color );
		vicious.hardlight( color );

		int n = sparkCount();
		sparkBlocks = Math.min( 5, n ) * 3;
		for (int i = 0; i < sparks.length; i++){
			boolean on = i < sparkBlocks;
			sparks[i].visible = on;
			if (on){
				sparks[i].hardlight( color );
				if (i % 3 == 0 && sparkSpeed[i / 3] == 0f){
					sparkPos[i / 3] = com.watabou.utils.Random.Float( 80f );
					sparkSpeed[i / 3] = com.watabou.utils.Random.Float( SPARK_MIN_SPEED, SPARK_MAX_SPEED )
							* (com.watabou.utils.Random.Int( 2 ) == 0 ? 1f : -1f);
				}
			}
		}
		applyOrder();
	}

	/** 光效压在图标下面/上面 */
	private void applyOrder(){
		if (shine != null) bringToFront( shine );
		if (vicious != null) bringToFront( vicious );
		if (ring != null) sendToBack( ring );
		for (int i = 0; i < sparks.length; i++) if (sparks[i] != null) bringToFront( sparks[i] );
	}

	private void hideAll(){
		if (shine != null) shine.visible = false;
		if (ring != null) ring.visible = false;
		if (vicious != null) vicious.visible = false;
		for (com.watabou.noosa.ColorBlock b : sparks) if (b != null) b.visible = false;
		sparkBlocks = 0;
	}

	/** 高光时刻：2s 周期内前 0.1s */
	public static boolean streakOn( float t ){ return mod( t, STREAK_PERIOD ) < STREAK_FOR; }
	/** 脚下光环：2s 周期内前 0.53s */
	public static boolean ringOn( float t ){ return mod( t, STREAK_PERIOD ) < RING_FOR; }
	/** 刀身火花：0.8s 周期内前 0.12s */
	public static boolean viciousOn( float t ){ return mod( t, 0.8f ) < 0.12f; }

	private static float mod( float t, float period ){
		float m = t - (float)Math.floor( t / period ) * period;
		return m < 0 ? m + period : m;
	}

	@Override
	public void update(){
		super.update();
		if (item == null || sprite == null) return;
		if (shine == null) return;

		float t = com.watabou.noosa.Game.timeTotal + phase;
		float sx = sprite.x + sprite.width() / 2f;
		float sy = sprite.y + sprite.height() / 2f;

		// 1) streak
		float sm = mod( t, STREAK_PERIOD );
		if (sm < STREAK_FOR){
			shine.visible = true;
			shine.angle = -45f;
			shine.scale.set( 0.85f, 0.6f );
			shine.x = sx - shine.width() * 0.85f / 2f;
			shine.y = sy - shine.height() * 0.6f / 2f;
			shine.am = (sm < 0.05f ? 0.9f : 0.45f);
		} else {
			shine.visible = false;
		}

		// 2) stillRing：RING_5 在脚下 2s 一次、前 0.53s
		float rm = mod( t, STREAK_PERIOD );
		if (!hasShine || rm >= RING_FOR){
			if (ring != null) ring.visible = false;
		} else {
			float grow = Math.min( 1f, rm / 0.25f );
			float a = 0.8f;
			if (rm < 0.25f) a = grow * 0.25f + 0.55f;
			else if (rm >= 0.45f) a = 0.8f * (1f - ((rm - 0.45f) / 0.08f));
			ring.visible = a > 0.02f;
			ring.scale.set( grow * 0.8f + 1f );
			ring.x = sx - ring.width() * ring.scale.x / 2f;
			ring.y = sprite.y + sprite.height() - 2f;
			ring.am = Math.max( 0f, Math.min( 1f, a ) );
		}

		// 3) vicious：0.8s 一轮，刀身 45° 火花闪 0.12s
		float vm = mod( t, 0.8f );
		if (!hasShine || vm >= 0.12f){
			if (vicious != null) vicious.visible = false;
		} else {
			vicious.visible = true;
			vicious.angle = 45f;
			vicious.scale.set( 0.9f, 0.7f );
			vicious.x = sprite.x + sprite.width() * 0.55f - vicious.width() * 0.45f;
			vicious.y = sprite.y + sprite.height() * 0.45f - vicious.height() * 0.35f;
			vicious.am = 1f - (vm / 0.12f);
		}

		// 4) 边框火花：沿格子边框跑动
		if (sparkBlocks > 0 && sparkPos.length > 0){
			float fw = width - 1f, fh = height - 1f;
			float perim = (fw + fh - 2f) * 2f;
			for (int i = 0; i < sparkBlocks / 3; i++){
				sparkPos[i] = mod( sparkPos[i] + sparkSpeed[i] * com.watabou.noosa.Game.elapsed, perim );
				float sign = Math.signum( sparkSpeed[i] );
				for (int k = 0; k < 3; k++){
					com.watabou.noosa.ColorBlock b = sparks[i * 3 + k];
					placeOnFrame( b, mod( sparkPos[i] - k * sign, perim ), fw, fh );
					b.alpha( (k == 0 ? 1f : k == 1 ? 0.6f : 0.3f) );
					b.visible = true;
				}
			}
		}
	}

	/** 沿格子边框走：抄自 APK 的 placeOnFrame（坐标要加上本格子的左上角） */
	private void placeOnFrame( com.watabou.noosa.ColorBlock b, float d, float fw, float fh ){
		float px, py;
		if (d < fw){
			px = d; py = 0;
		} else if (d < fw + fh){
			px = fw; py = d - fw;
		} else if (d < 2f * fw + fh){
			px = fw - (d - fw - fh); py = fh;
		} else {
			px = 0; py = fh - (d - 2f * fw - fh);
		}
		// 子节点用的是场景绝对坐标：必须加上自身 x/y
		b.x = x + 1f + px;
		b.y = y + 1f + py;
		b.size( 1f, 1f );
	}

	public void updateText(){

		if (itemIcon != null){
			remove(itemIcon);
			itemIcon = null;
		}

		if (item == null){
			status.visible = extra.visible = level.visible = special.visible = false;
			return;
		} else {
			status.visible = extra.visible = level.visible = special.visible = true;
		}

		status.text( item.status() );
			//thrown weapons on their last use show quantity in orange, unless they are single-use
			if (item instanceof MissileWeapon
					&& ((MissileWeapon) item).durabilityLeft() <= 50f
					&& ((MissileWeapon) item).durabilityLeft() <= ((MissileWeapon) item).durabilityPerUse()){
				status.hardlight(WARNING);
			} else {
				status.resetColor();
			}

		if (item.icon() != null && (item.isIdentified() || (item instanceof Ring && ((Ring) item).isKnown()))){
			extra.text( null );

			itemIcon = ItemSpriteSheet.Icons.image(item.icon());
			add(itemIcon);

		} else if (item instanceof Weapon || item instanceof Armor || item instanceof SecondaryArmor) {

			if (item.levelKnown){
				int str = item instanceof Weapon ? ((Weapon)item).STRReq() : (item instanceof Armor ? ((Armor)item).STRReq() : ((SecondaryArmor)item).STRReq());
				extra.text( Messages.format( TXT_STRENGTH, str ) );
				if (Dungeon.hero != null && str > Dungeon.hero.STR()) {
					extra.hardlight( DEGRADED );
				} else if (item instanceof Weapon && ((Weapon) item).masteryPotionBonus){
					extra.hardlight( MASTERED );
				} else if (item instanceof Armor && ((Armor) item).masteryPotionBonus) {
					extra.hardlight( MASTERED );
				} else {
					extra.resetColor();
				}
			} else {
				int str = item instanceof Weapon ? ((Weapon)item).STRReq(0) : (item instanceof Armor ? ((Armor)item).STRReq(0) : ((SecondaryArmor)item).STRReq());
				extra.text( Messages.format( TXT_TYPICAL_STR, str ) );
				extra.hardlight( WARNING );
			}
			extra.measure();

		} else {

			extra.text( null );

		}

		int trueLvl = item.visiblyUpgraded();
		int buffedLvl = item.buffedVisiblyUpgraded();

		if (trueLvl != 0 || buffedLvl != 0) {
			level.text( Messages.format( TXT_LEVEL, buffedLvl ) );
			level.measure();
			if (trueLvl == buffedLvl || buffedLvl <= 0) {
				if (buffedLvl > 0){
					if ((item instanceof Weapon && ((Weapon) item).curseInfusionBonus)
							|| (item instanceof Armor && ((Armor) item).curseInfusionBonus)
							|| (item instanceof Wand && ((Wand) item).curseInfusionBonus)){
						level.hardlight(CURSE_INFUSED);
					} else {
						level.hardlight(UPGRADED);
					}
				} else {
					level.hardlight( DEGRADED );
				}
			} else {
				level.hardlight(buffedLvl > trueLvl ? ENHANCED : WARNING);
			}
		} else {
			level.text( null );
		}

		special.text( item.special() );
		if (item.specialColorChange()){
			special.hardlight(WARNING);
		}else {
			special.resetColor();
		}

		layout();
	}

	public void enable( boolean value ) {

		active = value;

		float alpha = value ? ENABLED : DISABLED;
		sprite.alpha( alpha );
		status.alpha( alpha );
		extra.alpha( alpha );
		level.alpha( alpha );
		if (itemIcon != null) itemIcon.alpha( alpha );
	}

	public void showExtraInfo( boolean show ){

		if (show){
			add(extra);
		} else {
			remove(extra);
		}

	}

	public void setMargins( int left, int top, int right, int bottom){
		margin.set(left, top, right, bottom);
		layout();
	}

	@Override
	protected String hoverText() {
		if (item != null && item.name() != null) {
			return Messages.titleCase(item.name());
		} else {
			return super.hoverText();
		}
	}
}
