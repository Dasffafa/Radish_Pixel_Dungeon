# -*- coding: utf-8 -*-
import io

# 1) Hero：新增炼金能量字段 + 存档
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

a = "	public int lvl = 1;\n	public int exp = 0;\n"
assert s.count(a) == 1
s = s.replace(a, a + "\n	// 催化手环积累的炼金能量（暂时只做累积与提示）\n	public float alchemyEnergy = 0f;\n")

a = "			bundle.put( LEVEL, lvl );\n			bundle.put( EXPERIENCE, exp );\n"
assert s.count(a) == 1
s = s.replace(a, a + "			bundle.put( \"alchemy_energy\", alchemyEnergy );\n")

a = "		lvl = bundle.getInt( LEVEL );\n		exp = bundle.getInt( EXPERIENCE );\n"
assert s.count(a) == 1
s = s.replace(a, a + "		alchemyEnergy = bundle.getFloat( \"alchemy_energy\" );\n")

io.open(p, 'w', encoding='utf-8', newline='').write(s)

# 2) CatalystBracer：静态触发入口
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/secondary/CatalystBracer.java'
s = io.open(p, encoding='utf-8').read()
s = s.replace(
"""import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;""",
"""import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;""")
s = s.replace(
"""	public CatalystBracer() {
		super(1, 0, 1, 10);
		image = "catalyst_bracer";
	}
}""",
"""	public CatalystBracer() {
		super(1, 0, 1, 10);
		image = "catalyst_bracer";
	}

	/** 使用药水或卷轴后回馈炼金能量。 */
	public static void onUse( Hero hero ) {
		if (hero == null) return;
		SecondaryArmor armor = hero.belongings.secondaryArmor();
		if (!(armor instanceof CatalystBracer)) return;
		if (hero.STR() < armor.STRReq()) return;

		float gain = 1f + 0.2f * armor.level();
		hero.alchemyEnergy += gain;
		GLog.i( Messages.get( CatalystBracer.class, "energy_gain", gain ) );
	}
}""")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

# 3) Potion.drink 入口
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/potions/Potion.java'
s = io.open(p, encoding='utf-8').read()
a = "	protected void drink( Hero hero ) {"
assert s.count(a) == 1
s = s.replace(a, a + "\n\n		com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.CatalystBracer.onUse( hero );")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

# 4) Scroll.execute 的 doRead 之后
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/scrolls/Scroll.java'
s = io.open(p, encoding='utf-8').read()
a = "				doRead();\n"
assert s.count(a) == 1
s = s.replace(a, a + "				com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.CatalystBracer.onUse( hero );\n")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

# 5) 文本
p = r'core/src/main/assets/messages/items/items_zh.properties'
s = io.open(p, encoding='utf-8').read()
assert "items.armor.secondary.catalystbracer.name=" in s
s = s.replace("items.armor.secondary.catalystbracer.name=",
              "items.armor.secondary.catalystbracer.energy_gain=手环吸收了残余能量，炼金能量 +%s。\nitems.armor.secondary.catalystbracer.name=")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

p = r'core/src/main/assets/messages/items/items.properties'
s = io.open(p, encoding='utf-8').read()
assert "items.armor.secondary.catalystbracer.name=" in s
s = s.replace("items.armor.secondary.catalystbracer.name=",
              "items.armor.secondary.catalystbracer.energy_gain=The bracer absorbs the residual energy, alchemy energy +%s.\nitems.armor.secondary.catalystbracer.name=")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

print("CatalystBracer patched")
