# -*- coding: utf-8 -*-
import io

p = r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings.java'
s = io.open(p, encoding='utf-8').read()

def sub(old, new, cnt=1):
    global s
    assert s.count(old) == cnt, (old, s.count(old))
    s = s.replace(old, new)

# 1) 字段
sub("""	public KindOfWeapon weapon = null;
	public Armor armor = null;""",
    """	public KindOfWeapon weapon = null;
	public Armor armor = null;
	/** 次要护甲：与主要护甲并列的附加装备槽 */
	public com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor secondaryArmor = null;""")

# 2) 访问器（放在 armor() 后面）
sub("""	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){""",
    """	public com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor secondaryArmor(){
		if (!lostInventory() || (secondaryArmor != null && secondaryArmor.keptThroughLostInventory())){
			return secondaryArmor;
		} else {
			return null;
		}
	}

	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){""")

# 3) 常量
sub("""	private static final String SECOND_WEP = "second_wep";""",
    """	private static final String SECOND_WEP = "second_wep";
	private static final String SECONDARY_ARMOR = "secondary_armor";""")

# 4) 存档
sub("""		bundle.put( SECOND_WEP, secondWep );""",
    """		bundle.put( SECOND_WEP, secondWep );
		bundle.put( SECONDARY_ARMOR, secondaryArmor );""")

# 5) 读档
sub("""		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);""",
    """		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);

		if (bundle.contains(SECONDARY_ARMOR)) {
			secondaryArmor = (com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor) bundle.get(SECONDARY_ARMOR);
		}""")

io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('Belongings patched')
