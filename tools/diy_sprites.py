"""DIY 工作台的物品贴图生成器。

两类贴图：
1. 手绘的基础件（基础材料、强化精华、工作台、6 种特性精粹）——调色板 + 像素稿。
2. 按附魔/铭文自动生成的精粹（diy_enchant_* / diy_glyph_*）——同一套晶体/护符形状，
   每个附魔用一个固定色相（重点附魔手工指定主题色，其余用类名的稳定哈希）。

输出到 core/src/main/assets/sprites/items/，文件名即资源 ID。
运行：python tools/diy_sprites.py [--preview 预览图路径]
"""

import colorsys
import os
import sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from diy_effects_list import EFFECTS as BUFF_EFFECTS  # noqa: E402
from diy_effects_list import SPECIAL_EFFECTS  # noqa: E402
ROOT = os.path.join(HERE, "..")
OUT_DIR = os.path.join(ROOT, "core", "src", "main", "assets", "sprites", "items")
ENCHANT_DIRS = [
    os.path.join(ROOT, "core", "src", "main", "java", "com", "shatteredpixel", "shatteredpixeldungeon",
                 "items", "weapon", "enchantments"),
    os.path.join(ROOT, "core", "src", "main", "java", "com", "shatteredpixel", "shatteredpixeldungeon",
                 "items", "weapon", "curses"),
]
GLYPH_DIRS = [
    os.path.join(ROOT, "core", "src", "main", "java", "com", "shatteredpixel", "shatteredpixeldungeon",
                 "items", "armor", "glyphs"),
    os.path.join(ROOT, "core", "src", "main", "java", "com", "shatteredpixel", "shatteredpixeldungeon",
                 "items", "armor", "curses"),
]

# 每个字符对应一个颜色，"." 为透明
PALETTES = {
    "metal": {
        "k": (27, 27, 36, 255), "d": (74, 79, 99, 255), "m": (125, 131, 150, 255),
        "l": (182, 188, 202, 255), "h": (232, 238, 248, 255),
    },
    "arcane": {
        "k": (32, 20, 42, 255), "d": (75, 47, 122, 255), "m": (122, 82, 200, 255),
        "l": (177, 140, 255, 255), "h": (232, 220, 255, 255),
    },
    "scrap": {
        "k": (36, 26, 18, 255), "d": (90, 61, 34, 255), "m": (138, 95, 54, 255),
        "l": (184, 137, 90, 255), "h": (217, 192, 138, 255),
    },
    "level": {
        "k": (42, 24, 8, 255), "d": (163, 77, 10, 255), "m": (224, 138, 26, 255),
        "l": (255, 201, 77, 255), "h": (255, 243, 176, 255),
    },
    "enchant": {
        "k": (26, 16, 48, 255), "d": (61, 42, 138, 255), "m": (106, 75, 224, 255),
        "l": (180, 155, 255, 255), "h": (224, 212, 255, 255),
    },
    "passive": {
        "k": (42, 15, 32, 255), "d": (138, 31, 90, 255), "m": (214, 63, 143, 255),
        "l": (255, 154, 208, 255), "h": (255, 224, 240, 255),
    },
    "bench": {
        "k": (28, 20, 16, 255), "w": (94, 58, 30, 255), "b": (150, 98, 52, 255),
        "g": (110, 116, 138, 255), "s": (182, 188, 202, 255), "a": (86, 180, 240, 255),
        "h": (232, 238, 248, 255),
    },
    "holy": {
        "k": (40, 34, 20, 255), "d": (150, 116, 30, 255), "m": (226, 188, 60, 255),
        "l": (250, 244, 214, 255), "h": (255, 255, 255, 255), "w": (255, 255, 255, 255),
    },
    "banner": {
        "k": (38, 20, 20, 255), "d": (120, 40, 40, 255), "m": (206, 56, 56, 255),
        "l": (244, 116, 96, 255), "h": (250, 220, 180, 255), "p": (150, 150, 160, 255),
    },
    "tome": {
        "k": (34, 22, 14, 255), "d": (96, 60, 30, 255), "m": (150, 100, 50, 255),
        "l": (206, 160, 96, 255), "h": (240, 216, 160, 255), "g": (226, 188, 60, 255),
    },
    "abyss": {
        "k": (16, 28, 24, 255), "d": (34, 90, 70, 255), "m": (60, 160, 120, 255),
        "l": (140, 220, 170, 255), "h": (210, 255, 220, 255),
    },
    "steel": {
        "k": (22, 26, 34, 255), "d": (70, 80, 96, 255), "m": (130, 140, 160, 255),
        "l": (196, 206, 220, 255), "h": (240, 246, 255, 255), "r": (200, 60, 60, 255),
    },
}

ART = {}

# ---------------- 基础材料 / 工作台 ----------------

ART["diy_metal"] = ("metal", [
    "................",
    "................",
    "................",
    ".....kkkk.......",
    "....kmlmmk......",
    "....kmhhmk......",
    "....kmmmmk......",
    ".....kmk........",
    "..kkkkkkkkkk....",
    "..kmlmlmlmlmk...",
    "..kmmmmmmmmmk...",
    "..kkkkkkkkkkk...",
    "...kkkkkkkkk....",
    "...kmlmlmlmk....",
    "...kmmmmmmmk....",
    "...kkkkkkkkk....",
])

ART["diy_arcane"] = ("arcane", [
    "................",
    ".......h........",
    "......kmk.......",
    "....kkmmmkk.....",
    "...kmmlllmmk....",
    "..kmmllhllmmk...",
    "..kmlhllllhmk...",
    "..kmmlllllmmk...",
    "..kkmmlllmmkk...",
    "...kmmmllmmk....",
    "....kkmmmkk.....",
    "..h...kmk...h...",
    "..k....k....k...",
    "................",
    "................",
    "................",
])

ART["diy_scrap"] = ("scrap", [
    "................",
    "................",
    ".....kkk........",
    "....kdlmk..kkk..",
    "....kmlmk.kdmk..",
    ".....kkk..kmlk..",
    "..kkkk.....kkk..",
    ".kdmk..kkkk.....",
    ".kmlk.kdlmk.....",
    "..kkk.kmlmk.....",
    ".......kkk......",
    "................",
    "................",
    "................",
    "................",
    "................",
])

ART["diy_essence_level"] = ("level", [
    "................",
    ".......kk.......",
    "......kmmk......",
    ".....kmllmk.....",
    "....kmlhhlmk....",
    "....kmlhhlmk....",
    "...kmllllllmk...",
    "...kmllklllmk...",
    "...kmlkkkllmk...",
    "...kmllklllmk...",
    "...kmllllllmk...",
    "....kmllllmk....",
    ".....kmllmk.....",
    "......kmmk......",
    ".......kk.......",
    "................",
])

# 兜底：认不出具体附魔 / 特性时用的通用精粹
ART["diy_essence_enchant"] = ("enchant", [
    "................",
    ".......kk.......",
    "......klmk......",
    ".....kllmmk.....",
    "....kllhmmmk....",
    "....kllmmmdmk...",
    "....kllmmmdmk...",
    "....kllmmmdmk...",
    ".....klmmmdk....",
    ".....klmmmdk....",
    "......kmmmdk....",
    "......kmmmk.....",
    ".......kmmk.....",
    ".......kmk......",
    "........k.......",
    "................",
])

ART["diy_essence_passive"] = ("passive", [
    "................",
    "................",
    ".....kkkkkk.....",
    "....kmmlmmmk....",
    "...kmllhllmmk...",
    "...kmlhhhllmk...",
    "..kmllhhlllmmk..",
    "..kmllkllklmmk..",
    "..kmllhhlllmmk..",
    "...kmlhhhllmk...",
    "...kmllhllmmk...",
    "....kmmlmmmk....",
    ".....kkkkkk.....",
    "................",
    "................",
    "................",
])
ART["diy_workbench"] = ("bench", [
    "................",
    ".....kkkk.......",
    "....kssssk......",
    "....kkkkkk......",
    "...kbbbbbbk.....",
    "..kbbbbbbbbk....",
    "..kkkkkkkkkk....",
    "...kbbk..kbk....",
    "...kbbk..kbk....",
    "...kbbk..kbk....",
    "...kkkk..kkk....",
    "..kkkkkkkkkkk...",
    "..kggggggggggk..",
    "..kgsgggggggggk.",
    "..kgggggggggggk.",
    "..kkkkkkkkkkkkk.",
])

# ---------------- 特性精粹（每种特殊武器一张） ----------------

# 白帝圣剑：金白色的眼睛
ART["diy_passive_eye"] = ("holy", [
    "................",
    "................",
    "....kkkkkkkk....",
    "...kllllllllk...",
    "..kllhhwwhhllk..",
    "..klhwwwwwwhlk..",
    "..klhwwkkwwhlk..",
    "..klhwwkkwwhlk..",
    "..klhwwwwwwhlk..",
    "..kllhhwwhhllk..",
    "...kllllllllk...",
    "....kkkkkkkk....",
    "................",
    "................",
    "................",
    "................",
])

# 前线旗：灰杆上的红旗
ART["diy_passive_flag"] = ("banner", [
    "................",
    ".....kkk........",
    ".....kpk........",
    ".....kpk........",
    ".....kpkkkkkk...",
    ".....kpkmmmmmk..",
    ".....kpkmllmmk..",
    ".....kpkmmlmmk..",
    ".....kpkmmmmmk..",
    ".....kpkkkkkk...",
    ".....kpk........",
    ".....kpk........",
    ".....kkk........",
    "................",
    "................",
    "................",
])

# 莫雷洛秘典：棕色书 + 金扣
ART["diy_passive_cabala"] = ("tome", [
    "................",
    "..kkkkkkkkkk....",
    "..kddddddddk....",
    "..kdmmmmmmdk....",
    "..kdmkkkkmdk....",
    "..kdmkllkmdk....",
    "..kdmkkkkmdk....",
    "..kdmmmmmmdk....",
    "..kddddddddk....",
    "..kkkkkkkkkk....",
    "....kgggk.......",
    "................",
    "................",
    "................",
    "................",
    "................",
])

# 拉莱耶文本：绿色触手/眼
ART["diy_passive_rlyeh"] = ("abyss", [
    "................",
    "....kkkkkkkk....",
    "...kmmllllmmk...",
    "..kmlllhhllmmk..",
    "..kmlhkkkkhlmk..",
    "..kmlhkllkhlmk..",
    "..kmlhkkkkhlmk..",
    "..kmlllhhllmmk..",
    "...kmmllllmmk...",
    "....kkmmmmkk....",
    ".....kmk.kmmk...",
    ".....kk...kmk...",
    "...........kk...",
    "................",
    "................",
    "................",
])

# 幻影之书：深紫书 + 亮眼
ART["diy_passive_shadow"] = ("enchant", [
    "................",
    "..kkkkkkkkkk....",
    "..kddddddddk....",
    "..kdmmmmmmdk....",
    "..kdmlhhllmdk...",
    "..kdmhkklkmdk...",
    "..kdmlhhllmdk...",
    "..kdmmmmmmdk....",
    "..kddddddddk....",
    "..kkkkkkkkkk....",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
])

# 大太刀：斜放的刀刃 + 红柄
ART["diy_passive_taijutsu"] = ("steel", [
    "................",
    "...........kk...",
    "..........khlk..",
    ".........khlk...",
    "........khlk....",
    ".......khlk.....",
    "......khlk......",
    ".....khlk.......",
    "....khlk........",
    "...khlk.........",
    "..kkrk..........",
    ".krrk...........",
    ".kkk............",
    "................",
    "................",
    "................",
])

# ---------------- 附魔 / 铭文：程序化上色 ----------------

CRYSTAL = [
    "................",
    ".......kk.......",
    "......klmk......",
    ".....kllmmk.....",
    "....kllhmmmk....",
    "....kllmmmdmk...",
    "....kllmmmdmk...",
    "....kllmmmdmk...",
    ".....klmmmdk....",
    ".....klmmmdk....",
    "......kmmmdk....",
    "......kmmmk.....",
    ".......kmmk.....",
    ".......kmk......",
    "........k.......",
    "................",
]

# 常驻效果精粹：四角星形的光点
EFFECT_STAR = [
    "................",
    "................",
    ".......kk.......",
    "......khhk......",
    "......khhk......",
    "......klmk......",
    "..kkkkklmkkkk...",
    "..klllllmmlllk..",
    "..klmmmmmmmmmk..",
    "..kkkklmmkkkk...",
    "......kmmk......",
    "......kllk......",
    ".....kkllkk.....",
    ".....kllllk.....",
    "......kkkk......",
    "................",
]

HEX_PLATE = [
    "................",
    "................",
    ".....kkkkkk.....",
    "....kllllllk....",
    "...klmmmmlmmk...",
    "..klmmhhlmmmk...",
    "..klmmlllmmmk...",
    "..klmmlllmmmk...",
    "..klmmlllmmmk...",
    "..klmmhhlmmmk...",
    "...klmmmmlmk....",
    "....kllllllk....",
    ".....kkkkkk.....",
    "................",
    "................",
    "................",
]

# 主题色：重点附魔手工指定，剩下的用类名稳定哈希
HUE_OVERRIDES = {
    # 武器附魔
    "Blazing": 16, "BlazingFury": 4, "Chilling": 196, "Shocking": 52,
    "Corrupting": 122, "Grim": 282, "Vampiric": 352, "Midas": 44,
    "Refined": 205, "Reversal": 178, "VoidRift": 268, "Meteor": 22,
    "Striking": 48, "Euphoria": 318, "Survival": 158, "Saturation": 100,
    "Harmony": 300, "Jealousy": 86, "Lamprey": 8, "FlowingLight": 60,
    "Crystal": 172, "Pulse": 330, "Seeking": 238, "Sweeping": 130,
    "Projecting": 218, "Flawless": 258, "Clash": 20, "Combos": 38,
    "Insight": 200, "Distortion": 250, "Propulsion": 30, "Lamp": 46,
    "Rainstorm": 186, "Overwrite": 308, "Fluctuation": 290, "Reaping": 140,
    "Eclipse": 262, "Blocking": 208, "Blooming": 110, "Elastic": 164,
    "Kinetic": 230, "Lucky": 40, "Unstable": 312, "WetEnchantment": 190,
    # 护甲铭文
    "Swiftness": 192, "Viscosity": 28, "Obfuscation": 260, "Potential": 212,
    "SkyWalker": 204, "Resonance": 176, "Brimstone": 6, "Stone": 96,
    "Entanglement": 116, "Repulsion": 224, "Camouflage": 136, "Flow": 200,
    "Affection": 332, "AntiMagic": 244, "Thorns": 102, "HolyLight": 50,
    "Requiem": 278, "Profane": 274, "AntiEntropy": 302, "Corrosion": 84,
    "Displacement": 254, "Metabolism": 64, "Multiplicity": 306, "Stench": 74,
    "Overgrowth": 106, "Bulk": 24, "Swamp": 126, "Heavy": 240, "Ultralight": 216,
}


def hue_for(name):
    if name in HUE_OVERRIDES:
        return HUE_OVERRIDES[name]
    value = 2166136261
    for ch in name:
        value = ((value ^ ord(ch)) * 16777619) & 0xFFFFFFFF
    return value % 360


def hsl(h, s, l):
    r, g, b = colorsys.hls_to_rgb((h % 360) / 360.0, l, s)
    return (int(r * 255), int(g * 255), int(b * 255), 255)


def shade_palette(hue):
    """同一色相的 5 档明暗，跟手绘贴图一样有描边和高光。"""
    return {
        "k": hsl(hue, 0.55, 0.10),
        "d": hsl(hue, 0.70, 0.30),
        "m": hsl(hue, 0.78, 0.52),
        "l": hsl(hue, 0.80, 0.72),
        "h": hsl(hue, 0.40, 0.93),
    }


# ---------------- 渲染 ----------------

def render(name, palette, rows, out_dir):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(rows[:16]):
        for x, ch in enumerate(row[:16]):
            if ch in (".", " "):
                continue
            color = palette.get(ch)
            if color is None:
                raise SystemExit("unknown color char %r in %s at (%d,%d)" % (ch, name, x, y))
            px[x, y] = color
    path = os.path.join(out_dir, name + ".png")
    img.save(path)
    return path


def snake_case(name):
    out = []
    for i, ch in enumerate(name):
        if ch.isupper() and i > 0:
            out.append("_")
        out.append(ch.lower())
    return "".join(out)


def discover(directories):
    names = set()
    for directory in directories:
        if not os.path.isdir(directory):
            continue
        for f in os.listdir(directory):
            if f.endswith(".java"):
                names.add(f[:-5])
    return sorted(names)


def main():
    preview = None
    args = sys.argv[1:]
    if "--preview" in args:
        preview = args[args.index("--preview") + 1]

    paths = []
    # 手绘
    for name, (palette_name, rows) in ART.items():
        paths.append(render(name, PALETTES[palette_name], rows, OUT_DIR))

    # 附魔精粹：一个附魔一个色相
    enchants = discover(ENCHANT_DIRS)
    for cls in enchants:
        name = "diy_enchant_" + snake_case(cls)
        paths.append(render(name, shade_palette(hue_for(cls)), CRYSTAL, OUT_DIR))

    # 铭文精粹：换成六边形护符，色相也按铭文区分
    glyphs = discover(GLYPH_DIRS)
    for cls in glyphs:
        name = "diy_glyph_" + snake_case(cls)
        paths.append(render(name, shade_palette(hue_for(cls)), HEX_PLATE, OUT_DIR))

    # 常驻效果精粹：四角星光点，色相按 id 稳定取色
    for effect_id, _cls in BUFF_EFFECTS:
        paths.append(render("diy_effect_" + effect_id, shade_palette(hue_for(effect_id)), EFFECT_STAR, OUT_DIR))
    # 专属机制效果：同样给一张星形精粹
    for effect_id in SPECIAL_EFFECTS:
        paths.append(render("diy_effect_" + effect_id, shade_palette(hue_for(effect_id)), EFFECT_STAR, OUT_DIR))

    print("wrote %d sprites to %s (%d enchant, %d glyph)" % (
        len(paths), OUT_DIR, len(enchants), len(glyphs)))

    if preview:
        write_preview(paths, preview)
        print("preview:", preview)


def write_preview(paths, preview_path, scale=4, pad=4, cols=12):
    images = [(os.path.basename(p)[:-4], Image.open(p).convert("RGBA")) for p in paths]
    rows = (len(images) + cols - 1) // cols
    cell = 16 * scale + pad
    width = cols * cell + pad
    height = rows * cell + pad
    canvas = Image.new("RGBA", (width, height), (38, 38, 46, 255))
    for i, (_, im) in enumerate(images):
        cx = pad + (i % cols) * cell
        cy = pad + (i // cols) * cell
        big = im.resize((16 * scale, 16 * scale), Image.NEAREST)
        canvas.alpha_composite(big, (cx, cy))
    canvas.save(preview_path)


if __name__ == "__main__":
    main()
