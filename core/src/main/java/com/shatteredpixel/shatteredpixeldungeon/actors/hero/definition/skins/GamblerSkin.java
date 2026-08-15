package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.skins;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.HeroDefinition;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.SkinDefinition;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GamblerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;

/**
 * 赌徒皮肤 - 盗贼的可选皮肤变体。
 * <p>共享盗贼的转职（刺客/自由行者）与天赋，仅替换外观精灵；若有需要可覆盖
 * {@code initClassLoadout} 提供不同的初始物品。</p>
 */
public class GamblerSkin extends SkinDefinition {

	public static final int INDEX = HeroClasses.ROGUE_SKIN_GAMBLER;

	public GamblerSkin(HeroDefinition base) {
		super(base);
	}

	@Override public int skinIndex() { return INDEX; }

	@Override public String skinName() { return "GAMBLER"; }

	@Override public boolean customSprite() { return true; }

	@Override public String asset() { return Assets.Sprites.GAMBLER; }

	@Override public Class<? extends HeroSprite> spriteClass() { return GamblerSprite.class; }

	@Override public int frameW() { return 12; }
	@Override public int frameH() { return 16; }

	@Override public int[] idleFrames() { return new int[]{ 0, 1, 0, 2, 3, 4, 3, 2 }; }
}
