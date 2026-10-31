# -*- coding: utf-8 -*-
import io

p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero.java'
s = io.open(p, encoding='utf-8').read()

# 字段
a = "	// 催化手环积累的炼金能量（暂时只做累积与提示）\n	public float alchemyEnergy = 0f;\n"
assert s.count(a) == 1
s = s.replace(a, a + "\n	// 仙女座：上一次视野内的敌人数与预警冷却\n	private int andromedaSeenFoes = 0;\n	private int andromedaCd = 0;\n")

# act() 里插入
a = "		int mobcount = 0;\n		if (hero.hasTalent(Talent.HIDE_IN_CROWD)){"
assert s.count(a) == 1
add = """		// 仙女座：视野内敌人从少于 3 个变成 3 个或以上时，预警一次
		if (belongings.secondaryArmor() instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Andromeda
				&& STR() >= belongings.secondaryArmor().STRReq()) {
			int androFoes = 0;
			for (Mob am : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (am.alignment == Alignment.ENEMY && Dungeon.level.heroFOV[am.pos]) androFoes++;
			}
			if (androFoes >= 3 && andromedaSeenFoes < 3 && andromedaCd <= 0) {
				GLog.w( Messages.get( com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary.Andromeda.class, "warning" ) );
				andromedaCd = 30;
			}
			andromedaSeenFoes = androFoes;
		}
		if (andromedaCd > 0) andromedaCd--;

""" + a
s = s.replace(a, add)
io.open(p, 'w', encoding='utf-8', newline='').write(s)

# 文本
p = r'core/src/main/assets/messages/items/items_zh.properties'
s = io.open(p, encoding='utf-8').read()
assert "items.armor.secondary.andromeda.name=" in s
s = s.replace("items.armor.secondary.andromeda.name=",
              "items.armor.secondary.andromeda.warning=仙女座的宝石泛起微光：前面的房间里有 3 个以上的敌人。\nitems.armor.secondary.andromeda.name=")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

p = r'core/src/main/assets/messages/items/items.properties'
s = io.open(p, encoding='utf-8').read()
assert "items.armor.secondary.andromeda.name=" in s
s = s.replace("items.armor.secondary.andromeda.name=",
              "items.armor.secondary.andromeda.warning=The gem on the Andromeda flickers: three or more enemies ahead.\nitems.armor.secondary.andromeda.name=")
io.open(p, 'w', encoding='utf-8', newline='').write(s)

print("Andromeda patched")
