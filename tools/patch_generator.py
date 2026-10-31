# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/Generator.java'
s = io.open(p, encoding='utf-8').read()

def sub(old, new, cnt=1):
    global s
    assert s.count(old) == cnt, (old, s.count(old))
    s = s.replace(old, new)

# 两处 ARMOR 生成点：25% 概率换成次要护甲
sub("""			case ARMOR:
				return randomArmor();""",
    """			case ARMOR:
				// 次要护甲占用普通护甲的生成池：四分之一左右的护甲掉落会是一件次要护甲
				if (Random.Float() < 0.25f) return randomSecondaryArmor();
				return randomArmor();""", 2)

# 新增生成方法，放在 randomArmor(int) 之后
sub("""	public static final Category[] wepTiers = new Category[]{""",
    """	/** 次要护甲的生成：按当前层数分段取对应阶，随机 0~2 级成长 */
	private static final Class<?>[][] SECONDARY_BY_TIER = new Class<?>[][]{
			{ com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.BlackBelt.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.CatalystBracer.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ThePure.class },
			{ com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ChampionBelt.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.InfusedArmband.class },
			{ com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Andromeda.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Shelter.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.TridentGauntlet.class },
			{ com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.WebBelt.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.ShiningGloves.class },
			{ com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.GriffinEye.class,
					com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Greaves.class },
	};

	public static com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor randomSecondaryArmor() {
		int floorSet = (int)GameMath.gate(0, Dungeon.depth / 5, SECONDARY_BY_TIER.length - 1);
		Class<?>[] pool = SECONDARY_BY_TIER[floorSet];
		com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor sa =
				(com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor)
						Reflection.newInstance(pool[Random.Int(pool.length)]);
		sa.level = Random.Int(0, 2);
		return sa;
	}

	public static final Category[] wepTiers = new Category[]{""")

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('Generator patched')
