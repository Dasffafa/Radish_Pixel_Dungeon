# -*- coding: utf-8 -*-
import io, os

PKG = "com.shatteredpixel.shatteredpixeldungeon.items.armor.secondary"
DIR = r"core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/items/armor/secondary"
os.makedirs(DIR, exist_ok=True)

TMPL = '''/*
 * Radish Pixel Dungeon
 */

package {pkg};

import com.shatteredpixel.shatteredpixeldungeon.items.armor.SecondaryArmor;

public class {cls} extends SecondaryArmor {{

	public {cls}() {{
		super({tier}, {bmin}, {bmax}, {req});
		image = "{img}";
	}}
}}
'''

items = [
    ("ChampionBelt",     2, 1, 2, 12, "champion_belt"),
    ("BlackBelt",        1, 0, 1, 10, "black_belt"),
    ("Andromeda",        3, 2, 2, 14, "andromeda"),
    ("InfusedArmband",   2, 0, 2, 11, "infused_armband"),
    ("CatalystBracer",   1, 0, 1, 10, "catalyst_bracer"),
    ("Shelter",          3, 2, 4, 14, "shelter"),
    ("GriffinEye",       5, 3, 5, 18, "griffin_eye"),
    ("WebBelt",          4, 2, 4, 16, "web_belt"),
    ("ThePure",          1, 0, 1, 10, "the_pure"),
    ("TridentGauntlet",  3, 1, 3, 14, "trident_gauntlet"),
    ("ShiningGloves",    4, 1, 2, 16, "shining_gloves"),
    ("Greaves",          5, 2, 5, 18, "greaves"),
]

for cls, tier, bmin, bmax, req, img in items:
    path = os.path.join(DIR, cls + ".java")
    io.open(path, "w", encoding="utf-8", newline="\n").write(
        TMPL.format(pkg=PKG, cls=cls, tier=tier, bmin=bmin, bmax=bmax, req=req, img=img))
    print("wrote", path)
