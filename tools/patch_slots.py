# -*- coding: utf-8 -*-
import io

def patch(path, pairs):
    s = io.open(path, encoding='utf-8').read()
    for old, new in pairs:
        assert s.count(old) == 1, (path, old, s.count(old))
        s = s.replace(old, new)
    io.open(path, 'w', encoding='utf-8', newline='').write(s)
    print('patched', path)

# WndBag：装备槽 5 -> 6（多一个次要护甲槽）
patch(r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/windows/WndBag.java', [
    ("""		placeItem( stuff.armor != null ? stuff.armor : new Placeholder( ItemSpriteSheet.ARMOR_HOLDER ) );""",
     """		placeItem( stuff.armor != null ? stuff.armor : new Placeholder( ItemSpriteSheet.ARMOR_HOLDER ) );
		placeItem( stuff.secondaryArmor() != null ? stuff.secondaryArmor() : new Placeholder( "secondary_slot" ) );"""),
    ("""		int equipped = 5;""",
     """		int equipped = 6;"""),
])

# InventoryPane：槽位 5 -> 6
patch(r'core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/InventoryPane.java', [
    ("""		equipped.get(1).item(stuff.armor == null ? new WndBag.Placeholder( ItemSpriteSheet.ARMOR_HOLDER ) : stuff.armor);""",
     """		equipped.get(1).item(stuff.armor == null ? new WndBag.Placeholder( ItemSpriteSheet.ARMOR_HOLDER ) : stuff.armor);
		equipped.get(5).item(stuff.secondaryArmor() == null ? new WndBag.Placeholder( "secondary_slot" ) : stuff.secondaryArmor());"""),
])
