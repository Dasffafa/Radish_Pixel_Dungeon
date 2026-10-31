# -*- coding: utf-8 -*-
import io
p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/SecondaryArmor.java'
s = io.open(p, encoding='utf-8').read()
old = """	@Override
	public SecondaryArmor doEquip(Hero hero) {
		if (hero.belongings.secondaryArmor == null
				|| hero.belongings.secondaryArmor.doUnequip(hero, true, false)) {
			hero.belongings.secondaryArmor = this;
			GLog.i(Messages.get(this, "equipped"));
			return this;
		}
		return null;
	}"""
new = """	@Override
	public boolean doEquip(Hero hero) {
		if (hero.belongings.secondaryArmor == null
				|| hero.belongings.secondaryArmor.doUnequip(hero, true, false)) {
			hero.belongings.secondaryArmor = this;
			GLog.i(Messages.get(this, "equipped"));
			return true;
		}
		return false;
	}"""
assert s.count(old) == 1
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('ok')
