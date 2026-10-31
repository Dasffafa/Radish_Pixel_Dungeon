package com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Andromeda;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.BlackBelt;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.CatalystBracer;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ChampionBelt;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Greaves;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.GriffinEye;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.InfusedArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Shelter;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ShiningGloves;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ThePure;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.TridentGauntlet;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.WebBelt;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/** 测试模式用：使用一次随机生成一件次要护甲放进背包。 */
public class SecondaryArmorSpawner extends TestGenerator {

	@Override
	public String name() {
		return "次要护甲生成器";
	}

	@Override
	public String desc() {
		return "使用后随机生成一件次要护甲放进背包。";
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_GIVE )) {
			final String[] names = { "冠军腰带", "黑带", "仙女座", "灌能臂缠", "催化手环", "庇护",
					"格里芬之眼", "蛛网腰带", "纯洁者", "三叉护手", "闪亮手套", "护胫甲" };
			GameScene.show( new WndOptions( "次要护甲生成器", "选择要生成的一件：", names ) {
				@Override
				protected void onSelect( int index ) {
					armorAt( index ).collect();
				}
			} );
		}
	}

	private SecondaryArmor armorAt( int index ) {
		switch (index) {
			case 0:  return new ChampionBelt();
			case 1:  return new BlackBelt();
			case 2:  return new Andromeda();
			case 3:  return new InfusedArmband();
			case 4:  return new CatalystBracer();
			case 5:  return new Shelter();
			case 6:  return new GriffinEye();
			case 7:  return new WebBelt();
			case 8:  return new ThePure();
			case 9:  return new TridentGauntlet();
			case 10: return new ShiningGloves();
			default: return new Greaves();
		}
	}
}
