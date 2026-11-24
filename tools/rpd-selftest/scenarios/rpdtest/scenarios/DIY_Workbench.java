package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.Diy;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyComponent;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyPoison;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyMaterial;
import com.shatteredpixel.shatteredpixeldungeon.items.DiyWorkbench;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Corrupting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.VoidRift;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiyPick;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnemyFlag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WhiteKingGodSword;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventorySlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiyAssemble;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiyCraft;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiyCraftList;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.utils.Reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rpdtest.H;
import rpdtest.HarnessError;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * DIY 工作台 v3：专属贴图 + 无限制组装（多附魔、多特性、无等级上限）。
 *
 * 验证：材料/部件用的是新贴图；拆 +2 烈焰短剑出 2 强化精华 + 1 烈焰精粹；
 * 拆白帝圣剑/前线旗出对应特性精粹；一次组装贴 2 个附魔 + 2 个特性 + 2 级，
 * 成品两个附魔都能加载、两个被动都挂到英雄身上；附魔不兼容时拒绝；
 * 存读档后多附魔与多特性还在；真实窗口里「全选附魔 → 组装」能走通。
 */
public class DIY_Workbench implements Scenario {

	@Override
	public String describe() {
		return "DIY 工作台 v3：专属贴图 + 多附魔/多特性/无等级上限的组装";
	}

	@Override
	public void run( H h ) {
		h.newGameInArena( HeroClasses.WARRIOR, "DIYTEST" );

		// 工作台现在只在「智能怪物」挑战开局发放；普通模式测试里手动发一个
		if ( h.get( () -> Dungeon.hero.belongings.getItem( DiyWorkbench.class ) ) == null ) {
			h.world( "发一个工作台", () -> new DiyWorkbench().collect( Dungeon.hero.belongings.backpack ) );
		}
		DiyWorkbench bench = h.get( () -> Dungeon.hero.belongings.getItem( DiyWorkbench.class ) );
		h.require( bench != null, "测试需要一个 DIY 工作台" );

		// ---------- 专属贴图 ----------
		h.check( h.get( () -> new DiyMaterial( DiyMaterial.METAL, 1 ).image ).equals( ItemSpriteSheet.DIY_METAL ),
				"铁件用的是新贴图 diy_metal" );
		h.check( h.get( () -> new DiyMaterial( DiyMaterial.ARCANE, 1 ).image ).equals( ItemSpriteSheet.DIY_ARCANE ),
				"魔尘用的是新贴图 diy_arcane" );
		h.check( h.get( () -> new DiyMaterial( DiyMaterial.MISC, 1 ).image ).equals( ItemSpriteSheet.DIY_SCRAP ),
				"杂料用的是新贴图 diy_scrap" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.LEVEL, "", 1 ).image ).equals( ItemSpriteSheet.DIY_ESSENCE_LEVEL ),
				"强化精华用的是新贴图 diy_essence_level" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.ENCHANT, "", 1 ).image ).equals( ItemSpriteSheet.DIY_ESSENCE_ENCHANT ),
				"附魔精粹用的是新贴图 diy_essence_enchant" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.PASSIVE, "", 1 ).image ).equals( ItemSpriteSheet.DIY_ESSENCE_PASSIVE ),
				"特性精粹用的是新贴图 diy_essence_passive" );
		// 每个附魔/铭文/特性都有自己的精粹贴图
		h.check( h.get( () -> Diy.enchantSprite( Blazing.class.getName() ) ).equals( "diy_enchant_blazing" ),
				"烈焰精粹用专属贴图 diy_enchant_blazing" );
		h.check( h.get( () -> Diy.enchantSprite( Chilling.class.getName() ) ).equals( "diy_enchant_chilling" ),
				"冰霜精粹用专属贴图 diy_enchant_chilling" );
		h.check( h.get( () -> Diy.enchantSprite( Swiftness.class.getName() ) ).equals( "diy_glyph_swiftness" ),
				"铭文精粹用护符贴图 diy_glyph_swiftness" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.ENCHANT, Blazing.class.getName(), 1 ).image() )
						.equals( "diy_enchant_blazing" ),
				"精粹实例的贴图跟着附魔走" );
		h.check( h.get( () -> !new DiyComponent( DiyComponent.ENCHANT, Blazing.class.getName(), 1 ).image()
						.equals( new DiyComponent( DiyComponent.ENCHANT, Chilling.class.getName(), 1 ).image() ) ),
				"烈焰和冰霜的精粹贴图不一样" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.PASSIVE, "eye", 1 ).image() ).equals( "diy_passive_eye" ),
				"白帝特性精粹有专属贴图 diy_passive_eye" );
		String benchImage = h.get( () -> Dungeon.hero.belongings.getItem( DiyWorkbench.class ).image );
		h.check( ItemSpriteSheet.DIY_WORKBENCH.equals( benchImage ),
				"工作台本体用的是新贴图 diy_workbench" );

		// ---------- 普通拆解：只出基础材料 ----------
		Shortsword plainA = h.give( new Shortsword() );
		Shortsword plainB = h.give( new Shortsword() );
		int metalBefore = h.get( () -> Diy.materialCount( DiyMaterial.METAL ) );
		h.world( "拆普通短剑 A", () -> Diy.dismantle( plainA ) );
		h.world( "拆普通短剑 B", () -> Diy.dismantle( plainB ) );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.METAL ) ) == metalBefore + 10,
				"拆两把 0 级短剑多了 10 个铁件" );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.LEVEL, "" ) ) == 0, "0 级装备不产强化精华" );

		// ---------- 高级拆解：强化 + 附魔 ----------
		Shortsword blazing = h.give( new Shortsword() );
		h.world( "把短剑改成 +2 烈焰", () -> {
			blazing.level( 2 );
			blazing.enchant( Reflection.newInstance( Blazing.class ) );
		} );
		Diy.Yield blazingYield = h.get( () -> Diy.analyze( blazing ) );
		h.check( blazingYield.levels == 2, "+2 的短剑拆出 2 份强化精华" );
		h.check( blazingYield.enchants.contains( Blazing.class.getName() ), "拆出烈焰附魔精粹" );
		h.world( "拆 +2 烈焰短剑", () -> Diy.dismantle( blazing ) );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.ENCHANT, Blazing.class.getName() ) ) == 1,
				"背包里有 1 份烈焰精粹" );

		Shortsword chilling = h.give( new Shortsword() );
		h.world( "把短剑改成冰霜", () -> chilling.enchant( Reflection.newInstance( Chilling.class ) ) );
		h.world( "拆冰霜短剑", () -> Diy.dismantle( chilling ) );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.ENCHANT, Chilling.class.getName() ) ) == 1,
				"背包里有 1 份冰霜精粹" );

		Shortsword shocking = h.give( new Shortsword() );
		h.world( "把短剑改成电击", () -> shocking.enchant( Reflection.newInstance( Shocking.class ) ) );
		h.world( "拆电击短剑", () -> Diy.dismantle( shocking ) );

		Shortsword corrupting = h.give( new Shortsword() );
		h.world( "把短剑改成腐蚀", () -> corrupting.enchant( Reflection.newInstance( Corrupting.class ) ) );
		h.world( "拆腐蚀短剑", () -> Diy.dismantle( corrupting ) );
		h.check( h.get( () -> Diy.componentStacks( DiyComponent.ENCHANT ).size() ) == 4,
				"背包里有 4 种不同的附魔精粹（每种一个贴图）" );

		// 在拆解窗口里截一张：4 种附魔精粹应该是 4 种颜色
		h.use( bench, DiyWorkbench.AC_DISMANTLE );
		h.frames( 10 );
		h.screenshot( "diy-bag-multi-enchant" );
		h.closeWindows();

		// ---------- 特殊武器：特性精粹 ----------
		WhiteKingGodSword god = h.give( new WhiteKingGodSword() );
		h.world( "拆白帝圣剑", () -> Diy.dismantle( god ) );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.PASSIVE, "eye" ) ) >= 1, "背包里有白帝特性精粹" );

		EnemyFlag flag = h.give( new EnemyFlag() );
		h.world( "拆前线旗", () -> Diy.dismantle( flag ) );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.PASSIVE, "flag" ) ) >= 1, "背包里有前线旗特性精粹" );

		// ---------- 无限制组装：2 级 + 2 附魔 + 2 特性 ----------
		final Object[] box = new Object[1];
		h.world( "组装 2 级四附魔双特性 +3 伤害短剑", () -> box[0] = Diy.craft( Shortsword.class, 2,
				Arrays.asList( Blazing.class.getName(), Chilling.class.getName(),
						Shocking.class.getName(), Corrupting.class.getName() ),
				Arrays.asList( "eye", "flag" ), 3, 0, 0, 0 ) );

		Item crafted = (Item) box[0];
		h.check( crafted instanceof Shortsword, "组装出了一把短剑" );
		h.check( h.get( () -> crafted.level() ) == 2, "成品是 +2" );
		h.check( h.get( () -> ( (Weapon) crafted ).diyEnchantments().size() ) == 4,
				"成品同时挂着 4 个附魔（" + h.get( () -> ( (Weapon) crafted ).diyEnchantments().size() ) + "）" );
		h.check( h.get( () -> Diy.allEnchantsOf( crafted ).size() ) == 4, "成品记录了四个附魔类名" );
		h.check( h.get( () -> ( (Weapon) crafted ).diyEnchantments().get( 2 ) instanceof Shocking ), "第三个附魔是电击" );
		h.check( h.get( () -> ( (Weapon) crafted ).diyEnchantments().get( 3 ) instanceof Corrupting ), "第四个附魔是腐蚀" );
		h.check( h.get( () -> ( (KindOfWeapon) crafted ).diyMin ) == 3, "伤害 +3 写进了 diyMin" );
		h.check( h.get( () -> {
			Shortsword probe = new Shortsword();
			probe.level( 2 );
			return ( (KindOfWeapon) crafted ).min() == ( (KindOfWeapon) probe ).min() + 3;
		} ), "min() 真的多了 3 点伤害" );
		h.check( h.get( () -> ( (Weapon) crafted ).diyEnchantments().get( 0 ) instanceof Blazing ), "第一个附魔是烈焰" );
		h.check( h.get( () -> ( (Weapon) crafted ).diyEnchantments().get( 1 ) instanceof Chilling ), "第二个附魔是冰霜" );
		h.check( h.get( () -> Diy.passiveIdsOf( crafted ).containsAll( Arrays.asList( "eye", "flag" ) ) ),
				"成品记录了两个特性" );
		h.check( h.get( () -> Dungeon.hero.buff( WhiteKingGodSword.OnlyOneEyeAttack.class ) == null
				&& Dungeon.hero.buff( EnemyFlag.MobWarningProject.class ) == null ), "刚合成、还没装备时不给特性" );

		final boolean[] equippedOk = new boolean[1];
		h.world( "装上组装短剑", () -> equippedOk[0] = ( (KindOfWeapon) crafted ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.check( equippedOk[0], "组装短剑装备成功" );
		h.check( h.get( () -> Dungeon.hero.buff( WhiteKingGodSword.OnlyOneEyeAttack.class ) != null
				&& Dungeon.hero.buff( EnemyFlag.MobWarningProject.class ) != null ), "装备后两个特性都生效" );

		h.world( "脱下组装短剑", () -> ( (KindOfWeapon) crafted ).doUnequip( Dungeon.hero, true ) );
		syncNow( h );
		h.check( h.get( () -> Dungeon.hero.buff( WhiteKingGodSword.OnlyOneEyeAttack.class ) == null
				&& Dungeon.hero.buff( EnemyFlag.MobWarningProject.class ) == null ), "脱下后两个特性都消失" );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.ENCHANT, Blazing.class.getName() ) ) == 0
				&& Diy.componentCount( DiyComponent.ENCHANT, Chilling.class.getName() ) == 0, "两份附魔精粹都用掉了" );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.LEVEL, "" ) ) == 0, "强化精华用掉了" );

		// ---------- 护甲：多铭文 ----------
		ClothArmor armorA = h.give( new ClothArmor() );
		h.world( "给布甲刻上迅捷", () -> armorA.inscribe( Reflection.newInstance( Swiftness.class ) ) );
		h.world( "拆迅捷布甲", () -> Diy.dismantle( armorA ) );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.ENCHANT, Swiftness.class.getName() ) ) == 1,
				"背包里有迅捷铭文精粹" );

		ClothArmor armorB = h.give( new ClothArmor() );
		h.world( "给布甲刻上粘稠", () -> armorB.inscribe( Reflection.newInstance( Viscosity.class ) ) );
		h.world( "拆粘稠布甲", () -> Diy.dismantle( armorB ) );

		h.world( "组装双铭文 +4 减伤布甲", () -> box[0] = Diy.craft( ClothArmor.class, 0,
				Arrays.asList( Swiftness.class.getName(), Viscosity.class.getName() ),
				Collections.<String>emptyList(), 0, 4, 0, 0 ) );
		Item armorOut = (Item) box[0];
		h.check( armorOut instanceof ClothArmor, "组装出了一件布甲" );
		h.check( h.get( () -> ( (Armor) armorOut ).diyGlyphs().size() ) == 2, "布甲同时挂着 2 个铭文" );
		h.check( h.get( () -> Reflect.get( ( (Armor) armorOut ).diyGlyphs().get( 0 ), "armor" ) ) != null,
				"铭文拿到了 armor 引用（proc 里不会空指针）" );
		h.check( h.get( () -> ( (Armor) armorOut ).diyDRMin ) == 4, "减伤 +4 写进了 diyDRMin" );

		// ---------- 多附魔/多铭文的 proc 都能跑 ----------
		final Rat dummy = h.spawn( new Rat(), h.xy( 14, 10 ) );
		final boolean[] procOk = new boolean[1];
		h.world( "把两个附魔和两个铭文的 proc 各跑一遍", () -> {
			for ( Object o : ( (Weapon) crafted ).diyEnchantments() ) {
				if ( o instanceof Weapon.Enchantment ) {
					( (Weapon.Enchantment) o ).proc( (Weapon) crafted, Dungeon.hero, dummy, 10 );
				}
			}
			for ( Object o : ( (Armor) armorOut ).diyGlyphs() ) {
				if ( o instanceof Armor.Glyph ) {
					( (Armor.Glyph) o ).proc( (Armor) armorOut, dummy, Dungeon.hero, 10 );
				}
			}
			procOk[0] = true;
		} );
		h.check( procOk[0], "多附魔/多铭文的 proc 都跑得通（没有空指针）" );
		// 后面要长时间等待，先把测试用的老鼠清掉，免得把英雄打死
		h.world( "清掉测试老鼠", () -> dummy.die( null ) );

		// ---------- 附魔不兼容：拒绝且不扣东西 ----------
		int metalBeforeBad = h.get( () -> Diy.materialCount( DiyMaterial.METAL ) );
		h.world( "把武器附魔贴到法杖上", () -> box[0] = Diy.craft( WandOfMagicMissile.class, 0,
				Collections.singletonList( Blazing.class.getName() ), Collections.<String>emptyList(), 0, 0, 0, 0 ) );
		h.check( box[0] == null, "武器附魔贴不到法杖上" );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.METAL ) ) == metalBeforeBad, "拒绝时材料没动" );

		// ---------- 全开放：没有精粹的附魔也能用魔尘贴 ----------
		h.check( h.get( () -> Diy.enchantCatalog( new Shortsword() ).size() ) >= 50,
				"武器附魔目录里有全部可选附魔（" + h.get( () -> Diy.enchantCatalog( new Shortsword() ).size() ) + " 种）" );
		h.check( h.get( () -> Diy.enchantCatalog( new ClothArmor() ).size() ) >= 25,
				"护甲铭文目录里有全部可选铭文" );
		h.check( h.get( () -> Diy.enchantCatalog( new Shortsword() ).contains( VoidRift.class.getName() ) ),
				"目录里有背包里没有精粹的附魔（虚空裂隙）" );

		PotionOfHealing potionStack = new PotionOfHealing();
		potionStack.quantity( 6 );
		PotionOfHealing potions = h.give( potionStack );
		for ( int i = 0; i < 6; i++ ) {
			h.world( "拆一瓶治疗药水", () -> Diy.dismantle( potions ) );
		}
		int arcaneBeforeFree = h.get( () -> Diy.materialCount( DiyMaterial.ARCANE ) );
		h.check( arcaneBeforeFree >= 4, "拆药水攒到了魔尘（" + arcaneBeforeFree + "）" );

		final int passiveTotalBefore = h.get( () -> Diy.componentTotal( DiyComponent.PASSIVE ) );
		PotionOfHealing bonusPotion = h.give( new PotionOfHealing() );
		h.world( "拆一瓶药水掉类别精粹", () -> Diy.dismantle( bonusPotion ) );
		h.check( h.get( () -> Diy.componentTotal( DiyComponent.PASSIVE ) ) > passiveTotalBefore,
				"拆药水会额外掉一个药水类效果精粹" );

		final int arcaneBeforeReject = h.get( () -> Diy.materialCount( DiyMaterial.ARCANE ) );
		h.world( "没有精粹还硬贴虚空裂隙", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.singletonList( VoidRift.class.getName() ), Collections.<String>emptyList(), 0, 0, 0, 0 ) );
		h.check( box[0] == null, "没有对应附魔精粹时拒绝组装（不能用魔尘代替）" );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.ARCANE ) ) == arcaneBeforeReject,
				"被拒绝时不扣魔尘" );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.METAL ) ) > 0, "拒绝时也不扣铁件" );

		// ---------- 常驻效果：装备后才生效，脱下消失 ----------
		h.check( h.get( () -> Diy.passiveCatalog().size() ) >= 15,
				"特性/效果目录有 " + h.get( () -> Diy.passiveCatalog().size() ) + " 种" );
		h.check( h.get( () -> Diy.passiveName( "stamina" ) ).equals( "体力充沛" ), "常驻效果有名字（体力充沛）" );

		final float baseSpeed = h.get( () -> Dungeon.hero.speed() );
		grantPassiveEssences( h, "stamina" );
		h.world( "组装一把带体力充沛的短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "stamina" ), 0, 0, 0, 0 ) );
		Item staminaSword = (Item) box[0];
		h.check( staminaSword != null, "组装出了带体力充沛的短剑" );
		h.check( h.get( () -> Dungeon.hero.buff( Stamina.class ) ) == null, "没装备时不给体力充沛" );

		h.world( "装上体力充沛短剑", () -> ( (KindOfWeapon) staminaSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.check( h.get( () -> Dungeon.hero.buff( Stamina.class ) ) != null, "装备后体力充沛生效" );
		h.check( h.get( () -> Dungeon.hero.speed() ) > baseSpeed,
				"体力充沛真的让速度变快（" + baseSpeed + " → " + h.get( () -> Dungeon.hero.speed() ) + "）" );

		h.world( "贴漂浮", () -> Diy.ensurePassive( "levitation" ) );
		h.check( h.get( () -> Dungeon.hero.flying ), "漂浮让英雄处于飞行状态" );

		h.world( "脱下体力充沛短剑", () -> ( (KindOfWeapon) staminaSword ).doUnequip( Dungeon.hero, true ) );
		syncNow( h );
		h.check( h.get( () -> Dungeon.hero.buff( Stamina.class ) ) == null, "脱下后体力充沛消失" );
		h.check( Math.abs( h.get( () -> Dungeon.hero.speed() ) - baseSpeed ) < 0.001f, "速度回到原值" );

		h.world( "拆掉体力充沛短剑", () -> Diy.dismantle( staminaSword ) );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.PASSIVE, "stamina" ) ) >= 1,
				"拆回来的是体力充沛精粹" );

		// 装备状态下存读档：特性应该跟着装备一起回来
		h.world( "再组装一把体力充沛短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "stamina" ), 0, 0, 0, 0 ) );
		final Item staminaSword2 = (Item) box[0];
		h.world( "装上第二把体力充沛短剑", () -> ( (KindOfWeapon) staminaSword2 ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.saveAndReload();
		h.rest( 2 );
		h.check( h.get( () -> Dungeon.hero.buff( Stamina.class ) ) != null, "装备状态下读档后体力充沛还在" );

		// 兜底：效果被清掉后，只要还穿着，记录表会补回来
		h.world( "手动删掉体力充沛 buff", () -> {
			Stamina staminaBuff = Dungeon.hero.buff( Stamina.class );
			if ( staminaBuff != null ) staminaBuff.detach();
		} );
		h.check( h.get( () -> Dungeon.hero.buff( Stamina.class ) ) == null, "体力充沛 buff 被手动删掉了" );
		h.rest( 50 );
		h.check( h.get( () -> Dungeon.hero.buff( Stamina.class ) ) != null, "还穿着时记录表把体力充沛补了回来" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.PASSIVE, "stamina", 1 ).image() ).equals( "diy_effect_stamina" ),
				"体力充沛精粹有专属贴图 diy_effect_stamina" );

		// ---------- 效果目录：能应用、不崩 ----------
		h.world( "清掉 DIY 效果记录", () -> {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyEffectsTracker tracker =
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyEffectsTracker.get( Dungeon.hero );
			if ( tracker != null ) tracker.detach();
		} );
		final java.util.ArrayList<String> applyFailures = new java.util.ArrayList<>();
		for ( String effectId : Diy.BUFF_EFFECT_IDS ) {
			h.world( "应用效果 " + effectId, () -> Diy.ensurePassive( effectId ) );
			if ( !h.get( () -> Diy.hasEffect( effectId ) ) ) {
				applyFailures.add( effectId );
			}
		}
		h.check( applyFailures.isEmpty(), "全部 " + Diy.BUFF_EFFECT_IDS.length + " 种效果都能应用（失败：" + applyFailures + "）" );
		h.check( h.get( () -> Diy.PULSE_EFFECT_IDS.length ) >= 10, "临时效果有周期脉冲机制" );

		// 补点材料，脉冲测试要组装
		h.world( "补材料", () -> {
			new DiyMaterial( DiyMaterial.METAL, 400 ).collect();
			new DiyMaterial( DiyMaterial.ARCANE, 200 ).collect();
		} );

		// ---------- 周期脉冲：治疗会隔一阵回一次血 ----------
		h.world( "把血降到一半", () -> Dungeon.hero.HP = Math.max( 1, Dungeon.hero.HT / 2 ) );
		grantPassiveEssences( h, "healing" );
		h.world( "组装治疗脉冲短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "healing" ), 0, 0, 0, 0 ) );
		Item pulseSword = (Item) box[0];
		h.world( "装备治疗脉冲短剑", () -> ( (KindOfWeapon) pulseSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		final int hpBeforePulse = h.get( () -> Dungeon.hero.HP );
		h.rest( 45 );
		h.check( h.get( () -> Dungeon.hero.HP ) > hpBeforePulse, "治疗脉冲真的会回血" );

		// ---------- 补齐材料，后面要连续组装 ----------
		h.world( "补材料", () -> {
			new DiyMaterial( DiyMaterial.METAL, 600 ).collect();
			new DiyMaterial( DiyMaterial.ARCANE, 300 ).collect();
			new DiyMaterial( DiyMaterial.MISC, 200 ).collect();
		} );

		// ---------- 效果条目数量：物品效果 + 附魔/铭文 ----------
		h.check( h.get( () -> Diy.passiveCatalog( new Shortsword() ).size() ) >= 150,
				"武器效果条目 ≥150（实际 " + h.get( () -> Diy.passiveCatalog( new Shortsword() ).size() ) + "）" );
		h.check( h.get( () -> Diy.passiveCatalog( new ClothArmor() ).size() ) >= 150,
				"护甲效果条目 ≥150（实际 " + h.get( () -> Diy.passiveCatalog( new ClothArmor() ).size() ) + "）" );
		h.check( h.get( () -> Diy.isItemEffect( "item:potion_healing" ) ), "药水也能当效果条目" );
		h.check( h.get( () -> Diy.isEnchantEffect( "e:" + Blazing.class.getName() ) ), "附魔也能当效果条目" );
		grantEnchantEssences( h, Blazing.class );
		h.world( "用效果条目贴一个烈焰", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(),
				Collections.singletonList( "e:" + Blazing.class.getName() ), 0, 0, 0, 0 ) );
		Item effectEnchantSword = (Item) box[0];
		h.check( h.get( () -> Diy.allEnchantsOf( effectEnchantSword ).contains( Blazing.class.getName() ) ),
				"效果条目贴的附魔真的进了附魔槽" );

		// ---------- 物品效果：治疗药水每 45 回合自动喝一次 ----------
		h.world( "把血降到四成", () -> Dungeon.hero.HP = Math.max( 1, Dungeon.hero.HT * 4 / 10 ) );
		grantPassiveEssences( h, "item:potion_healing" );
		h.world( "组装药水效果短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(),
				Collections.singletonList( "item:potion_healing" ), 0, 0, 0, 0 ) );
		Item potionSword = (Item) box[0];
		h.world( "装备药水效果短剑", () -> ( (KindOfWeapon) potionSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		final int hpBeforePotion = h.get( () -> Dungeon.hero.HP );
		h.rest( 50 );
		h.check( h.get( () -> Dungeon.hero.HP ) > hpBeforePotion, "物品效果（治疗药水）真的生效了" );

		// ---------- 物品效果：炸弹/符石真的打到敌人 ----------
		final Rat bombTarget = h.spawn( new Rat(), h.xy( 14, 10 ) );
		h.world( "让炸弹测试鼠站桩", () -> {
			bombTarget.state = bombTarget.PASSIVE;
			bombTarget.HP = bombTarget.HT = 300;
		} );
		grantPassiveEssences( h, "item:bomb_fire" );
		h.world( "组装炸弹效果短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(),
				Collections.singletonList( "item:bomb_fire" ), 0, 0, 0, 0 ) );
		Item bombSword = (Item) box[0];
		h.world( "装备炸弹效果短剑", () -> ( (KindOfWeapon) bombSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		final int bombHpBefore = h.get( () -> bombTarget.HP );
		h.rest( 12 );
		h.check( h.get( () -> bombTarget.HP ) < bombHpBefore || !bombTarget.isAlive(),
				"炸弹物品效果真的炸到了敌人" );

		final Rat stoneTarget = h.spawn( new Rat(), h.xy( 15, 10 ) );
		h.world( "让符石测试鼠站桩", () -> {
			stoneTarget.state = stoneTarget.PASSIVE;
			stoneTarget.HP = stoneTarget.HT = 300;
		} );
		grantPassiveEssences( h, "item:stone_shock" );
		h.world( "组装符石效果短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(),
				Collections.singletonList( "item:stone_shock" ), 0, 0, 0, 0 ) );
		Item stoneSword = (Item) box[0];
		h.world( "装备符石效果短剑", () -> ( (KindOfWeapon) stoneSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		final int stoneHpBefore = h.get( () -> stoneTarget.HP );
		h.rest( 12 );
		h.check( h.get( () -> stoneTarget.HP ) < stoneHpBefore || !stoneTarget.isAlive(),
				"符石物品效果真的打到了敌人" );
		h.world( "清掉物品效果测试鼠", () -> {
			if ( bombTarget.isAlive() ) bombTarget.die( null );
			if ( stoneTarget.isAlive() ) stoneTarget.die( null );
		} );

		// ---------- 药水/卷轴/符石/食物没有等级 ----------
		h.world( "补杂料", () -> new DiyMaterial( DiyMaterial.MISC, 200 ).collect() );
		h.check( h.get( () -> Diy.priceOf( new PotionOfHealing(), 3,
				Collections.<String>emptyList(), Collections.<String>emptyList(), 0, 0, 0, 0 ).levelEssence ) == 0,
				"药水的等级不收费" );
		final int essenceBefore = h.get( () -> Diy.componentCount( DiyComponent.LEVEL, "" ) );
		h.world( "给药水加 3 级", () -> box[0] = Diy.craft( PotionOfHealing.class, 3,
				Collections.<String>emptyList(), Collections.<String>emptyList(), 0, 0, 0, 0 ) );
		Item potionOut = (Item) box[0];
		h.check( potionOut != null && h.get( () -> potionOut.level() ) == 0, "药水不会被加到 +3" );
		h.check( h.get( () -> Diy.componentCount( DiyComponent.LEVEL, "" ) ) == essenceBefore,
				"药水不吃强化精华" );
		h.check( h.get( () -> Diy.priceOf( new ClothArmor(), 3,
				Collections.<String>emptyList(), Collections.<String>emptyList(), 0, 0, 0, 0 ).levelEssence ) == 3,
				"护甲的等级照常收费" );

		// ---------- 消耗品专属属性：数量 / 饱食度 / 充能 ----------
		final int potionsBefore = h.get( () -> {
			int n = 0;
			for ( Item it : Dungeon.hero.belongings.backpack.items ) {
				if ( it instanceof PotionOfHealing ) n += it.quantity();
			}
			return n;
		} );
		h.world( "组装 3 瓶药水", () -> box[0] = Diy.craft( PotionOfHealing.class, 0,
				Collections.<String>emptyList(), Collections.<String>emptyList(),
				0, 0, 0, 0, 2, 0, 0 ) );
		h.check( h.get( () -> {
			int n = 0;
			for ( Item it : Dungeon.hero.belongings.backpack.items ) {
				if ( it instanceof PotionOfHealing ) n += it.quantity();
			}
			return n;
		} ) - potionsBefore == 3, "数量 +2 真的多出 3 瓶药水" );

		final float baseFoodEnergy = new com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation().energy;
		h.world( "组装加饱食度的口粮", () -> box[0] = Diy.craft(
				com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation.class, 0,
				Collections.<String>emptyList(), Collections.<String>emptyList(),
				0, 0, 0, 0, 0, 2, 0 ) );
		h.check( h.get( () -> {
			for ( Item it : Dungeon.hero.belongings.backpack.items ) {
				if ( it instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.SmallRation
						&& ( (com.shatteredpixel.shatteredpixeldungeon.items.food.Food) it ).energy
							>= baseFoodEnergy + 100f ) {
					return true;
				}
			}
			return false;
		} ), "饱食度 +2 真的加了 100 能量" );

		final int baseCharges = new WandOfMagicMissile().initialCharges();
		h.world( "组装加 2 点充能的法杖", () -> box[0] = Diy.craft( WandOfMagicMissile.class, 0,
				Collections.<String>emptyList(), Collections.<String>emptyList(),
				0, 0, 0, 0, 0, 0, 2 ) );
		Item chargedWand = (Item) box[0];
		h.check( chargedWand instanceof WandOfMagicMissile, "组装出了加充能的法杖" );
		h.check( h.get( () -> ( (WandOfMagicMissile) chargedWand ).maxCharges ) == baseCharges + 2,
				"充能 +2 真的加到法杖上限" );

		// ---------- 属性：攻击距离 / 攻击速度 ----------
		h.world( "组装 +2 距离 +3 攻速 短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.<String>emptyList(), 0, 0, 2, 3 ) );
		Item attrSword = (Item) box[0];
		h.check( attrSword instanceof Weapon, "组装出带距离/攻速的短剑" );
		h.check( h.get( () -> ( (Weapon) attrSword ).RCH ) == 3, "攻击距离 1 → 3" );
		h.check( Math.abs( h.get( () -> ( (Weapon) attrSword ).DLY ) - 0.85f ) < 0.001f,
				"攻击速度延迟 1.00 → 0.85" );
		h.saveAndReload();
		h.check( h.get( () -> {
			for ( Item it : Dungeon.hero.belongings.backpack.items ) {
				if ( it instanceof Weapon && Diy.passiveIdsOf( it ).isEmpty()
						&& Math.abs( ( (Weapon) it ).DLY - 0.85f ) < 0.001f ) {
					return ( (Weapon) it ).RCH == 3;
				}
			}
			return false;
		} ), "读档后攻击距离/速度还在" );

		// ---------- 专属效果：投掷淬毒 / 护甲荆棘 / 药水再生 / 卷轴自动鉴定 ----------
		grantPassiveEssences( h, "poison_hit" );
		h.world( "组装淬毒短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( Diy.EFFECT_POISON ), 0, 0, 0, 0 ) );
		Item poisonSword = (Item) box[0];
		h.check( h.get( () -> Diy.allEnchantsOf( poisonSword ).contains( DiyPoison.class.getName() ) ),
				"淬毒写进了附魔槽" );
		h.check( h.get( () -> Diy.passiveIdsOf( poisonSword ).contains( Diy.EFFECT_POISON ) ),
				"成品记录了淬毒效果" );

		grantPassiveEssences( h, "thorns" );
		h.world( "组装荆棘布甲", () -> box[0] = Diy.craft( ClothArmor.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( Diy.EFFECT_THORNS ), 0, 0, 0, 0 ) );
		Item thornsArmor = (Item) box[0];
		h.check( h.get( () -> Diy.allEnchantsOf( thornsArmor ).contains( Thorns.class.getName() ) ),
				"荆棘写进了护甲铭文槽" );

		h.world( "把血降到一半", () -> Dungeon.hero.HP = Math.max( 1, Dungeon.hero.HT / 2 ) );
		grantPassiveEssences( h, "heal_regen" );
		h.world( "组装治疗再生短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( Diy.EFFECT_HEAL ), 0, 0, 0, 0 ) );
		final Item healSword = (Item) box[0];
		h.world( "装备治疗再生短剑", () -> ( (KindOfWeapon) healSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.check( h.get( () -> Dungeon.hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyRegen.class ) != null ),
				"治疗再生的 buff 挂上了" );
		final int hpBeforeHeal = h.get( () -> Dungeon.hero.HP );
		h.rest( 26 );
		h.check( h.get( () -> Dungeon.hero.HP ) > hpBeforeHeal, "治疗再生让血真的变多" );

		grantPassiveEssences( h, "auto_identify" );
		h.world( "组装自动鉴定短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( Diy.EFFECT_IDENTIFY ), 0, 0, 0, 0 ) );
		final Item idSword = (Item) box[0];
		h.world( "装备自动鉴定短剑", () -> ( (KindOfWeapon) idSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		final ScrollOfMagicMapping unknownScroll = new ScrollOfMagicMapping();
		h.world( "塞一张没鉴定的卷轴", () -> unknownScroll.collect() );
		h.check( h.get( () -> !unknownScroll.isIdentified() ), "卷轴一开始是没鉴定的" );
		h.rest( 12 );
		h.check( h.get( () -> unknownScroll.isIdentified() ), "自动鉴定把卷轴鉴定了" );

		// 周期触发：buff 自己的计数应该一直涨
		final int[] act1 = new int[1];
		act1[0] = h.get( () -> {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyIdentify buff =
					Dungeon.hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyIdentify.class );
			return buff == null ? -1 : buff.activations;
		} );
		h.rest( 30 );
		final int[] act2 = new int[1];
		act2[0] = h.get( () -> {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyIdentify buff =
					Dungeon.hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyIdentify.class );
			return buff == null ? -1 : buff.activations;
		} );
		h.check( act2[0] > act1[0] + 1,
				"自动鉴定在持续触发（" + act1[0] + " → " + act2[0] + "）" );

		// 第二张换一个没鉴定过的种类
		final ScrollOfTeleportation secondScroll = new ScrollOfTeleportation();
		h.world( "再塞一张没鉴定的传送卷轴", () -> secondScroll.collect() );
		h.check( h.get( () -> !secondScroll.isIdentified() ), "第二张一开始没鉴定" );
		h.rest( 14 );
		h.check( h.get( () -> secondScroll.isIdentified() ), "自动鉴定把第二张也鉴定了" );

		// 子袋子里的东西也要鉴定（以前只扫主背包）
		final com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder holder =
				new com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder();
		h.world( "给一个卷轴筒", () -> holder.collect() );
		// 用一个「游戏目录里没见过」的自定义物品，避免同类自动鉴定的干扰
		final MysteryItem bagScroll = new MysteryItem();
		h.world( "把没鉴定的神秘物品塞进卷轴筒", () -> holder.items.add( bagScroll ) );
		h.check( h.get( () -> !bagScroll.isIdentified() ), "袋子里的物品一开始没鉴定" );
		h.rest( 12 );
		h.check( h.get( () -> bagScroll.isIdentified() ), "自动鉴定把袋子里的物品也鉴定了" );

		// ---------- 所有效果都要有正常名字 ----------
		h.check( h.get( () -> {
			for ( String id : Diy.passiveCatalog() ) {
				String name = Diy.effectName( id );
				if ( name == null || name.isEmpty() || name.contains( "no text found" ) || name.equals( id ) ) {
					return false;
				}
			}
			return true;
		} ), "所有效果都有正常名字（不会出现 no text found）" );
		h.check( h.get( () -> Diy.effectName( "revealed_area" ).equals( "区域揭示" ) ), "区域揭示有正常名字" );
		h.check( h.get( () -> Diy.effectName( "item:bomb_fire" ).equals( "火焰弹" ) ), "炸弹效果有中文名" );
		h.check( h.get( () -> Diy.passiveName( "item:bomb_fire" ).equals( "火焰弹" ) ),
				"物品描述/拆解里的炸弹效果名也是中文" );
		h.check( h.get( () -> new DiyComponent( DiyComponent.PASSIVE, "item:bomb_fire", 1 ).name() ).contains( "火焰弹" ),
				"炸弹效果精粹名字是中文" );

		// ---------- 区域揭示：每 20 回合揭开周围地图 ----------
		grantPassiveEssences( h, "revealed_area" );
		h.world( "组装区域揭示短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "revealed_area" ), 0, 0, 0, 0 ) );
		Item revealSword = (Item) box[0];
		h.world( "装备区域揭示短剑", () -> ( (KindOfWeapon) revealSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.world( "清掉地图探索标记", () -> {
			boolean[] mapped = Dungeon.level.mapped;
			boolean[] discoverable = Dungeon.level.discoverable;
			for ( int i = 0; i < mapped.length; i++ ) {
				if ( discoverable[i] ) mapped[i] = false;
			}
		} );
		h.rest( 22 );
		h.check( h.get( () -> {
			boolean[] mapped = Dungeon.level.mapped;
			boolean[] discoverable = Dungeon.level.discoverable;
			for ( int i = 0; i < mapped.length; i++ ) {
				if ( discoverable[i] && mapped[i] ) return true;
			}
			return false;
		} ), "区域揭示把周围地图标出来了" );
		h.check( h.get( () -> {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyReveal r =
					Dungeon.hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyReveal.class );
			return r != null && r.activations > 0;
		} ), "区域揭示在周期触发" );

		// ---------- 危险预知：周期自动搜索 ----------
		grantPassiveEssences( h, "foresight" );
		h.world( "组装危险预知短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "foresight" ), 0, 0, 0, 0 ) );
		Item searchSword = (Item) box[0];
		h.world( "装备危险预知短剑", () -> ( (KindOfWeapon) searchSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.rest( 20 );
		h.check( h.get( () -> {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiySearch s =
					Dungeon.hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiySearch.class );
			return s != null && s.activations > 0;
		} ), "危险预知在周期自动搜索" );

		// ---------- 法杖效果：盈能法杖 ----------
		grantPassiveEssences( h, "wand_empower" );
		h.world( "组装盈能短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "wand_empower" ), 0, 0, 0, 0 ) );
		Item empowerSword = (Item) box[0];
		h.world( "装备盈能短剑", () -> ( (KindOfWeapon) empowerSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.check( h.get( () -> {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower e =
					Dungeon.hero.buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower.class );
			return e != null && e.dmgBoost >= 1;
		} ), "盈能法杖真的给了法杖伤害加成" );

		// ---------- 法杖效果：法杖充能 ----------
		final WandOfMagicMissile chargeWand = h.give( new WandOfMagicMissile() );
		h.world( "把法杖用空", () -> chargeWand.curCharges = 0 );
		grantPassiveEssences( h, "wand_charge" );
		h.world( "组装充能短剑", () -> box[0] = Diy.craft( Shortsword.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "wand_charge" ), 0, 0, 0, 0 ) );
		Item chargeSword = (Item) box[0];
		h.world( "装备充能短剑", () -> ( (KindOfWeapon) chargeSword ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.rest( 14 );
		h.check( h.get( () -> chargeWand.curCharges ) > 0, "法杖充能给法杖补了充能" );

		// ---------- 符石效果：护盾符文 ----------
		grantPassiveEssences( h, "ward" );
		h.world( "组装护盾符文布甲", () -> box[0] = Diy.craft( ClothArmor.class, 0,
				Collections.<String>emptyList(), Collections.singletonList( "ward" ), 0, 0, 0, 0 ) );
		Item wardArmor = (Item) box[0];
		h.world( "穿上护盾符文布甲", () -> ( (Armor) wardArmor ).doEquip( Dungeon.hero ) );
		syncNow( h );
		h.rest( 27 );
		h.check( h.get( () -> Dungeon.hero.shielding() ) > 0, "护盾符文给了护盾" );

		// ---------- 拆料也进工作台，不占背包 ----------
		final int metalStored = h.get( () -> Diy.materialCount( DiyMaterial.METAL ) );
		final Shortsword metalSword = h.give( new Shortsword() );
		h.world( "拆一把短剑把铁件进仓库", () -> Diy.dismantle( metalSword ) );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.METAL ) ) > metalStored,
				"拆出来的铁件进工作台仓库" );
		h.check( h.get( () -> {
			for ( Item it : Dungeon.hero.belongings.backpack.items ) {
				if ( it instanceof DiyMaterial ) return false;
			}
			return true;
		} ), "背包里不再出现材料物品" );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.METAL ) ) > metalStored, "材料数量可读" );

		// ---------- 精粹存在工作台里，不占背包 ----------
		grantPassiveEssences( h, "healing" );
		final int healingStored = h.get( () -> Diy.componentCount( DiyComponent.PASSIVE, "healing" ) );
		h.check( healingStored >= 1, "精粹记进了工作台（" + healingStored + " 份）" );
		h.check( h.get( () -> {
			for ( Item it : Dungeon.hero.belongings.backpack.items ) {
				if ( it instanceof DiyComponent ) return false;
			}
			return true;
		} ), "背包里不再出现精粹物品" );
		h.saveAndReload();
		h.check( h.get( () -> Diy.componentCount( DiyComponent.PASSIVE, "healing" ) ) == healingStored,
				"读档后工作台里的精粹还在" );

		// ---------- 存读档：多附魔 + 多特性 + 属性都要留住 ----------
		int weaponsBeforeReload = h.get( DIY_Workbench::countMeleeWeapons );
		h.saveAndReload();
		h.check( h.get( () -> Diy.hasWorkbench() ), "存读档后工作台还在" );
		h.check( h.get( DIY_Workbench::countMeleeWeapons ) == weaponsBeforeReload, "存读档后近战武器数量不变" );
		h.check( h.get( DIY_Workbench::hasDoubleEnchantWeapon ), "存读档后两个附魔还在（能重新加载附魔实例）" );
		h.check( h.get( DIY_Workbench::hasDoublePassiveWeapon ), "存读档后两个特性还在" );
		h.check( h.get( DIY_Workbench::hasStatWeapon ), "存读档后 +3 伤害还在（diyMin 有存档）" );
		bench = h.get( () -> Dungeon.hero.belongings.getItem( DiyWorkbench.class ) );
		h.require( bench != null, "存读档后要重新拿到工作台实例" );

		// 给窗口里试组装补一份强化精华和一份附魔精粹
		Shortsword spare = h.give( new Shortsword() );
		h.world( "造一把 +1 冰霜短剑备料", () -> {
			spare.level( 1 );
			spare.enchant( Reflection.newInstance( Chilling.class ) );
		} );
		h.world( "拆备料短剑", () -> Diy.dismantle( spare ) );
		h.note( "备料后：" + h.get( Diy::materialsLine )
				+ " / 强化精华 " + h.get( () -> Diy.componentCount( DiyComponent.LEVEL, "" ) )
				+ " / 附魔精粹 " + h.get( () -> Diy.componentStacks( DiyComponent.ENCHANT ).size() ) );

		grantEnchantEssences( h, Blazing.class );
		grantPassiveEssences( h, "eye" );

		// ---------- 真实窗口：查看精粹 ----------
		h.use( bench, DiyWorkbench.AC_ESSENCES );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyEssence" ),
				"「查看精粹」打开精粹仓库窗口：" + h.windowNames() );
		h.screenshot( "diy-essence-storage" );
		h.closeWindows();
		h.frames( 5 );

		// ---------- 真实窗口：分类 → 底材 → 全选附魔 → 组装 ----------
		h.use( bench, DiyWorkbench.AC_CRAFT );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyCraft" ), "「合成」打开了 WndDiyCraft：" + h.windowNames() );
		h.screenshot( "diy-craft-categories" );

		clickFirstEnabledButton( h, WndDiyCraft.class );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyCraftList" ),
				"点分类后打开了 WndDiyCraftList：" + h.windowNames() );

		clickFirstEnabledButton( h, WndDiyCraftList.class );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyAssemble" ),
				"点底材后进入了组装窗口：" + h.windowNames() );
		h.screenshot( "diy-assemble-empty" );

		clickButtonByLabel( h, WndDiyAssemble.class, "选择附魔" );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyPick" ),
				"打开全附魔列表：" + h.windowNames() );
		h.screenshot( "diy-pick-enchants" );

		clickButtonByLabel( h, WndDiyPick.class, "精粹×1" );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyPick" ), "选一个附魔后还留在列表里" );

		clickButtonByLabel( h, WndDiyPick.class, "完成" );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyAssemble" ),
				"完成选择后回到组装窗口：" + h.windowNames() );
		int enchantPicked = h.get( DIY_Workbench::assembledEnchantCount );
		h.check( enchantPicked >= 1, "从全附魔列表里选上了附魔（" + enchantPicked + " 个）" );
		h.screenshot( "diy-assemble-multiselect" );

		// 效果列表也截一张：16 种常驻效果 + 武器特性
		clickButtonByLabel( h, WndDiyAssemble.class, "选择效果" );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyPick" ), "打开效果列表：" + h.windowNames() );
		h.screenshot( "diy-pick-effects" );
		clickButtonByLabel( h, WndDiyPick.class, "完成" );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyAssemble" ), "完成效果选择后回到组装：" + h.windowNames() );

		int metalBeforeUi = h.get( () -> Diy.materialCount( DiyMaterial.METAL ) );
		int weaponsBeforeUi = h.get( DIY_Workbench::countMeleeWeapons );
		clickLastEnabledButton( h, WndDiyAssemble.class );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndDiyCraftList" ), "组装完回到物品列表：" + h.windowNames() );
		h.check( h.get( DIY_Workbench::countMeleeWeapons ) == weaponsBeforeUi + 1, "窗口里组装出了一把近战武器" );
		h.check( h.get( () -> Diy.materialCount( DiyMaterial.METAL ) ) < metalBeforeUi, "窗口里组装扣了材料" );
		h.screenshot( "diy-craft-after" );
		h.closeWindows();

		// ---------- 真实窗口：拆解 ----------
		h.use( bench, DiyWorkbench.AC_DISMANTLE );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndBag" ), "「拆解」打开了选物品的 WndBag：" + h.windowNames() );
		h.screenshot( "diy-dismantle" );

		clickBackpackItem( h );
		h.frames( 10 );
		h.check( h.findWindow( WndOptions.class ) != null, "点物品后先显示拆解产出：" + h.windowNames() );
		h.screenshot( "diy-dismantle-confirm" );
		h.chooseOption( 1 );
		h.frames( 10 );
		h.check( h.windowNames().contains( "windows.WndBag" ), "取消后回到背包：" + h.windowNames() );
		h.closeWindows();

		// ---------- 商店：身上没有工作台时才补货 ----------
		DiyWorkbench current = h.get( () -> Dungeon.hero.belongings.getItem( DiyWorkbench.class ) );
		h.require( current != null, "现在应该还能找到工作台" );
		h.world( "把工作台从背包里拿走", () -> current.detachAll( Dungeon.hero.belongings.backpack ) );
		h.check( h.get( () -> !Diy.hasWorkbench() ), "拿走之后包里没有工作台了" );

		ArrayList<Item> stockWithout = h.get( DIY_Workbench::generateShopStock );
		h.check( !containsWorkbench( stockWithout ),
				"商店永远不卖工作台（" + stockWithout.size() + " 件商品）" );

		h.world( "把一件新的工作台放回包里",
				() -> new DiyWorkbench().collect( Dungeon.hero.belongings.backpack ) );
		ArrayList<Item> stockWith = h.get( DIY_Workbench::generateShopStock );
		h.check( !containsWorkbench( stockWith ), "身上有工作台时商店也没有" );
		h.check( h.get( () -> !Diy.shouldGrantStarterWorkbench() ),
				"普通模式开局不给工作台（只有智能怪物挑战给）" );

		// 截一张消耗品专属属性（数量/饱食度/充能）的界面
		h.world( "打开药水组装窗口截图", () -> com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new WndDiyAssemble( null, PotionOfHealing.class ) ) );
		h.frames( 10 );
		h.screenshot( "diy-assemble-consumable" );

		h.check( h.heroAlive(), "英雄还活着" );
	}

	/** 测试用：游戏目录里没有的物品，不会被「同类已知」自动鉴定。 */
	public static class MysteryItem extends com.shatteredpixel.shatteredpixeldungeon.items.Item {
		@Override
		public String name() {
			return "神秘物品";
		}

		@Override
		public String desc() {
			return "测试用物品。";
		}
	}

	// ------------------------------------------------------------------ 状态查询

	/** 发一些效果精粹/附魔精粹，方便测试「必须用精粹」的规则。 */
	private static void grantPassiveEssences( H h, String... ids ) {
		for ( String id : ids ) {
			h.world( "发效果精粹 " + id, () -> new DiyComponent( DiyComponent.PASSIVE, id, 1 ).collect() );
		}
	}

	private static void grantEnchantEssences( H h, Class<?>... classes ) {
		for ( Class<?> cls : classes ) {
			h.world( "发附魔精粹 " + cls.getSimpleName(),
					() -> new DiyComponent( DiyComponent.ENCHANT, cls.getName(), 1 ).collect() );
		}
	}

	/** 等装备动作结束，然后立刻按当前装备同步一次特性。 */
	private static void syncNow( H h ) {
		h.waitReady();
		h.world( "同步特性", () -> Diy.syncEquippedEffects(
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DiyEffectsTracker.ensure( Dungeon.hero ) ) );
	}

	private static int assembledEnchantCount() {
		WndDiyAssemble window = (WndDiyAssemble) lastWindowOfType( WndDiyAssemble.class );
		return window == null ? -1 : window.state().enchants.size();
	}

	private static int countMeleeWeapons() {
		int n = 0;
		for ( Item it : Dungeon.hero.belongings ) {
			if ( it instanceof MeleeWeapon ) n += it.quantity();
		}
		return n;
	}

	private static boolean hasDoubleEnchantWeapon() {
		for ( Item it : Dungeon.hero.belongings ) {
			if ( it instanceof Weapon && Diy.allEnchantsOf( it ).size() >= 2 ) {
				return ( (Weapon) it ).diyEnchantments().size() >= 2;
			}
		}
		return false;
	}

	private static boolean hasStatWeapon() {
		for ( Item it : Dungeon.hero.belongings ) {
			if ( it instanceof KindOfWeapon && ( (KindOfWeapon) it ).diyMin >= 3
					&& ( (KindOfWeapon) it ).min() > 0 ) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasDoublePassiveWeapon() {
		for ( Item it : Dungeon.hero.belongings ) {
			if ( it instanceof MeleeWeapon && Diy.passiveIdsOf( it ).size() >= 2 ) {
				return true;
			}
		}
		return false;
	}

	private static boolean containsWorkbench( ArrayList<Item> items ) {
		for ( Item it : items ) {
			if ( it instanceof DiyWorkbench ) return true;
		}
		return false;
	}

	/** 直接调商店的生成函数，看这一车货里有没有工作台（不用真的跑到第 6 层）。 */
	private static ArrayList<Item> generateShopStock() {
		try {
			java.lang.reflect.Method method = Reflect.method(
					com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ShopRoom.class, "generateItems" );
			@SuppressWarnings( "unchecked" )
			ArrayList<Item> out = (ArrayList<Item>) method.invoke( null );
			return out;
		} catch ( Throwable t ) {
			throw new HarnessError( "调用 ShopRoom.generateItems 失败：" + t, t );
		}
	}

	// ------------------------------------------------------------------ 窗口点击

	private static void clickFirstEnabledButton( H h, Class<?> windowType ) {
		clickButton( h, windowType, "第一个" );
	}

	private static void clickLastEnabledButton( H h, Class<?> windowType ) {
		clickButton( h, windowType, "最后一个" );
	}

	private static void clickButton( H h, Class<?> windowType, String which ) {
		final String names = String.join( ",", h.windowNames() );
		h.input( "点 " + windowType.getSimpleName() + " 里" + which + "可点按钮", () -> {
			Group target = findWindowGroup( windowType );
			if ( target == null ) {
				throw new HarnessError( "找不到窗口 " + windowType.getSimpleName() + "：" + names );
			}
			List<RedButton> buttons = new ArrayList<>();
			collectEnabledButtons( target, buttons );
			if ( buttons.isEmpty() ) throw new HarnessError( windowType.getSimpleName() + " 里没有可点的按钮" );
			RedButton button = "第一个".equals( which ) ? buttons.get( 0 ) : buttons.get( buttons.size() - 1 );
			Reflect.call( button, "onClick", new Class<?>[]{} );
		} );
	}

	private static void clickButtonByLabel( H h, Class<?> windowType, String labelPart ) {
		final String names = String.join( ",", h.windowNames() );
		h.input( "点 " + windowType.getSimpleName() + " 里的「" + labelPart + "」", () -> {
			Group target = findWindowGroup( windowType );
			if ( target == null ) {
				throw new HarnessError( "找不到窗口 " + windowType.getSimpleName() + "：" + names );
			}
			List<RedButton> buttons = new ArrayList<>();
			collectEnabledButtons( target, buttons );
			for ( RedButton button : buttons ) {
				String label = buttonLabel( button );
				if ( label != null && label.contains( labelPart ) ) {
					Reflect.call( button, "onClick", new Class<?>[]{} );
					return;
				}
			}
			throw new HarnessError( windowType.getSimpleName() + " 里没有写着「" + labelPart + "」的按钮" );
		} );
	}

	private static String buttonLabel( RedButton button ) {
		Object text = Reflect.get( button, "text" );
		if ( text instanceof RenderedTextBlock ) {
			return ( (RenderedTextBlock) text ).text();
		}
		return null;
	}

	private static void clickBackpackItem( H h ) {
		final String names = String.join( ",", h.windowNames() );
		h.input( "点背包里第一件物品", () -> {
			Group bag = findWindowGroup( WndBag.class );
			if ( bag == null ) throw new HarnessError( "没有打开的背包窗口：" + names );
			InventorySlot slot = firstBackpackSlot( bag );
			if ( slot == null ) throw new HarnessError( "背包里没有可点的物品" );
			Reflect.call( slot, "onClick", new Class<?>[]{} );
		} );
	}

	private static InventorySlot firstBackpackSlot( Group group ) {
		for ( Gizmo g : membersOf( group ) ) {
			if ( g instanceof InventorySlot ) {
				Item item = Reflect.get( g, "item" );
				if ( item != null && Dungeon.hero.belongings.backpack.contains( item ) ) {
					return (InventorySlot) g;
				}
			}
			if ( g instanceof Group ) {
				InventorySlot inner = firstBackpackSlot( (Group) g );
				if ( inner != null ) return inner;
			}
		}
		return null;
	}

	private static Group findWindowGroup( Class<?> windowType ) {
		for ( Gizmo g : membersOf( Game.scene() ) ) {
			if ( windowType.isInstance( g ) ) return (Group) g;
		}
		return null;
	}

	private static Object lastWindowOfType( Class<?> windowType ) {
		Object found = null;
		for ( Gizmo g : membersOf( Game.scene() ) ) {
			if ( windowType.isInstance( g ) ) found = g;
		}
		return found;
	}

	private static void collectEnabledButtons( Group group, List<RedButton> out ) {
		for ( Gizmo g : membersOf( group ) ) {
			if ( g instanceof RedButton && isEnabled( (RedButton) g ) ) {
				out.add( (RedButton) g );
			}
			if ( g instanceof Group ) {
				collectEnabledButtons( (Group) g, out );
			}
		}
	}

	private static boolean isEnabled( RedButton button ) {
		Boolean active = Reflect.get( button, "active" );
		return active == null || active;
	}

	@SuppressWarnings( "unchecked" )
	private static List<Gizmo> membersOf( Group group ) {
		List<Gizmo> out = new ArrayList<>();
		if ( group == null ) return out;
		try {
			Field field = Reflect.field( Group.class, "members" );
			synchronized ( group ) {
				List<Gizmo> all = (List<Gizmo>) field.get( group );
				if ( all != null ) {
					for ( Gizmo g : all ) {
						if ( g != null ) out.add( g );
					}
				}
			}
		} catch ( IllegalAccessException e ) {
			throw new HarnessError( "读 Group.members 失败", e );
		}
		return out;
	}
}
