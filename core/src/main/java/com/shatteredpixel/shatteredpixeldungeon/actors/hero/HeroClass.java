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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroRegistry;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.TalentSet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Image;



public class HeroClass {

	private final String name;
	private HeroDefinition definition;

	HeroClass(String name) {
		this.name = name;
	}

	public void bindDefinition(HeroDefinition def) { this.definition = def; }

	public HeroDefinition definition() {
		if (definition == null){
			definition = HeroRegistry.get(this);
		}
		return definition;
	}

	public String name() { return name; }

	public int ordinal() {
		int i = 0;
		for (HeroClass cls : HeroClasses.ALL){
			if (cls == this) return i;
			i++;
		}
		return 0;
	}

	public String spritesheet() { return definition().spritesheet(); }
	public String splashArt() { return definition().splashArt(); }
	public String GetSkinAssest() { return definition().avatarSkin(); }

	public boolean isUnlocked() {
		return definition().isUnlocked();
	}

	public HeroSubClass[] subClasses() { return definition().subClasses(); }

	public ArmorAbility[] armorAbilities() { return definition().armorAbilities(); }

	public void initHero( Hero hero ) {
		// 新建英雄默认使用盗贼和皮肤 0；初始化需读取全局选择，避免所选盗贼皮肤被默认值覆盖。
		SkinDefinition selectedSkin = skin(getGlobalSkin());
		(selectedSkin != null ? selectedSkin : definition()).initHero(hero);
	}

	public TalentSet talentSet() { return definition == null ? null : definition.talents(); }

	public HeroDefinition activeDefinition() {
		SkinDefinition skin = skin(GetSkin());
		return skin != null ? skin : definition();
	}

	public SkinDefinition activeSkin() {
		return skin(GetSkin());
	}

	// 基础职业和纯头像皮肤没有皮肤变体定义，索引有效也可能返回 null。
	public SkinDefinition skin(int index) {
		if (index <= 0) return null;
		for (SkinDefinition s : definition().skins()){
			if (s.skinIndex() == index) return s;
		}
		return null;
	}

	public String title() {
		return activeDefinition().heroName();
	}

	public String desc(){
		return activeDefinition().heroDesc();
	}

	public String shortDesc(){
		return activeDefinition().heroShortDesc();
	}

	public String unlockMsg() {
		return activeDefinition().heroUnlockMsg();
	}

	// 存档列表等界面没有当前英雄上下文，需显式传入存档中的皮肤索引。
	public String title(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroName() : definition().heroName();
	}

	public String desc(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroDesc() : definition().heroDesc();
	}

	public String shortDesc(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroShortDesc() : definition().heroShortDesc();
	}

	public String unlockMsg(int skinIndex) {
		SkinDefinition s = skin(skinIndex);
		return s != null ? s.heroUnlockMsg() : definition().heroUnlockMsg();
	}

	private static boolean onlyMode = false;

	// 仅供游戏内使用，调用前需已设置 Dungeon.hero。
	public HeroSprite createSkinSprite() {
		try {
			return activeDefinition().spriteClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException( "Failed to create hero sprite", e );
		}
	}

	public boolean isGamblerSkin() {
		return activeDefinition().customSprite();
	}

	// 普通头像皮肤索引对应头像贴图中的位置。
	public void SetSkin(int skinIndex){
		if (this == HeroClasses.JUMBLE) skinIndex = 0;
		boolean isSkinUnlock = false;
		Image img = new Image(this.GetSkinAssest());
		int skinCount = img.texture.width/64;

		if(skinIndex==0){
			isSkinUnlock = true;
		}else {
			while ( skinIndex < skinCount ) {
				isSkinUnlock = SPDSettings.isItemUnlock("avatars_" + name().toLowerCase() + "_" + skinIndex);
				if(!isSkinUnlock){
					skinIndex++;
				}else {
					break;
				}
			}

			// 独立贴图皮肤的索引可能超出头像贴图范围，仍需参与皮肤切换。
			if (skinIndex >= skinCount) {
				isSkinUnlock = false;
				for (SkinDefinition s : definition().skins()){
					if (s.customSprite() && s.skinIndex() >= skinIndex){
						skinIndex = s.skinIndex();
						isSkinUnlock = true;
						break;
					}
				}
			}
		}

		// 独立贴图皮肤不受头像皮肤的解锁状态限制。
		SkinDefinition skin = skin(skinIndex);
		if (skin != null && skin.customSprite()) {
			isSkinUnlock = true;
		}

		if(!isSkinUnlock){
			skinIndex=0;
			if(!onlyMode){
				ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndKeyBindings.class,"switch_skin")));
				onlyMode = true;
			}
		}

		// 游戏内同时写入英雄以随存档持久化；全局设置保留角色选择界面的默认值。
		if (Dungeon.hero != null && Dungeon.hero.heroClass == this){
			Dungeon.hero.skin = skinIndex;
		}
		SPDSettings.setHeroSkin(this.ordinal(),skinIndex);
	}

	// 本局皮肤以存档中的英雄为准，避免被全局选择覆盖。
	public int GetSkin(){
		if (this == HeroClasses.JUMBLE) return 0;
		if (Dungeon.hero != null && Dungeon.hero.heroClass == this){
			return Dungeon.hero.skin;
		}
		return SPDSettings.getHeroSkin(this.ordinal());
	}

	public int getGlobalSkin() {
		if (this == HeroClasses.JUMBLE) return 0;
		return SPDSettings.getHeroSkin(this.ordinal());
	}

	public String saveName(){ return name; }

}
