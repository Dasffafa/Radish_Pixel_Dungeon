# -*- coding: utf-8 -*-
"""
NPC 总表 生成 / 回读工具（双向）。

build  : 从仓库当前的代码与文本数据生成 docs/萝卜地牢开发文档/NPC总表.xlsx（+ 同名 .csv）
dump   : 把（可能被用户手改过的）xlsx 读回成 JSON，供后续按表实施

用法：
    python tools/npc_table.py build
    python tools/npc_table.py dump  "docs/萝卜地牢开发文档/NPC总表.xlsx"

只读仓库源文件，不修改任何游戏资源。
"""

import json
import os
import re
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "core", "src", "main", "assets")
MSGS_ZH = os.path.join(ASSETS, "messages", "actors", "actors_zh.properties")
MSGS_EN = os.path.join(ASSETS, "messages", "actors", "actors.properties")
SHEET = os.path.join(ASSETS, "sprites", "new_npc.png")

OUT_XLSX = os.path.join(ROOT, "docs", "萝卜地牢开发文档", "NPC总表.xlsx")
OUT_CSV = os.path.join(ROOT, "docs", "萝卜地牢开发文档", "NPC总表.csv")

HEADERS = [
    ("编号", 7),
    ("图画", 11),
    ("中文名", 12),
    ("英文名", 12),
    ("描述", 46),
    ("对话", 52),
    ("功能", 40),
    ("出没层", 14),
    ("刷新规则", 34),
    ("实现状态", 11),
    ("代码位置 / 帧索引", 30),
    ("备注", 34),
]

STATUS_DONE = "已实现"
STATUS_TODO = "未实现"


def read_props(path):
    data = {}
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\r\n")
            if not line or line.lstrip().startswith("#"):
                continue
            if "=" not in line:
                continue
            k, v = line.split("=", 1)
            data[k.strip()] = v.replace("\\n", "\n")
    return data


ZH = read_props(MSGS_ZH)
EN = read_props(MSGS_EN)


def g(key, en=False):
    return (EN if en else ZH).get(key, "")


def talk_of(prefix, en=False):
    """把 msg0..N / loop* / msg / talk 之类的键按顺序拼成一段对话。"""
    src = EN if en else ZH
    out = []
    idx = 0
    while True:
        k = "%s.msg%d" % (prefix, idx)
        if k not in src:
            break
        out.append(src[k])
        idx += 1
    for k in ("msg", "loop"):
        if "%s.%s" % (prefix, k) in src:
            out.append(src["%s.%s" % (prefix, k)])
    idx = 0
    while True:
        k = "%s.loop%d" % (prefix, idx)
        if k not in src:
            break
        out.append(src[k])
        idx += 1
    if not out:
        for k in ("talk", "talk_more"):
            if "%s.%s" % (prefix, k) in src:
                out.append(src["%s.%s" % (prefix, k)])
    return "\n".join(x for x in out if x)


# ---------------------------------------------------------------- 数据定义

# new_npc.png 的 20 个非空行。row -> (中文名, 英文名, 实现状态, 代码位置, 功能, 备注)
# 已实现的 6 行来自 sprites/{Owo,ThunderStorm,Boat,SeaShore,PW,MoonLight}Sprite.java 的帧索引；
# 其余行未接任何代码（全仓库只有这 6 个类用 new_npc.png）。
SHEET_ROWS = [
    (0, "粉发黑衣少女A", "", STATUS_TODO, "", "", "11 帧，仅此一版，未见引用"),
    (1, "粉发少女B", "", STATUS_TODO, "", "", "11 帧，与第 0 行同族但像素不同，像是另一版或另一角色"),
    (2, "owo", "Owo", STATUS_DONE, "sprites/OwoSprite.java", "纯对话（闪一下手电筒）",
     "idle 2 段 + die/operate 帧"),
    (3, "电击雷暴", "ThunderStorm", STATUS_DONE, "sprites/ThunderStormSprite.java",
     "纯对话（自称能加闪电链，这层没枪，送电池状道具的台词）", "台词有 msg0..3 + loop"),
    (4, "船", "Boat", STATUS_DONE, "sprites/BoatSprite.java", "纯对话（千年的金牌骇客）",
     "有 20 帧 idle"),
    (5, "海滨", "SeaShore", STATUS_DONE, "sprites/SeaShoreSprite.java",
     "纯对话（研究黑魔法，随机念叨）", "台词有 msg0/msg1 + loop0..2"),
    (6, "桃井", "PW", STATUS_DONE, "sprites/PWSprite.java", "纯对话（文本目前是 ~~~ 占位）",
     "文本未写，需要补"),
    (7, "月华", "MoonLight", STATUS_DONE, "sprites/MoonLightSprite.java",
     "武器交换：给他任意近战武器 → 回一把二阶无诅咒近战武器",
     "另有 the_catist 变体文本，另有 0 层/1 层两处生成"),
    (8, "橙色路障火苗", "", STATUS_TODO, "", "", "11 帧，形似火焰路障"),
    (9, "蓝发戴冠少女", "", STATUS_TODO, "", "", "本行 11 帧 + 第 10 行 5 帧，像是同一角色两段动画"),
    (10, "（第 9 行续帧？）", "", STATUS_TODO, "", "", "仅第 1-5 列有内容，需确认是续帧还是独立角色"),
    (11, "白毛碎彩团子", "", STATUS_TODO, "", "", "本行 11 帧 + 第 12 行 5 帧，同第 9/10 行的排法"),
    (12, "（第 11 行续帧？）", "", STATUS_TODO, "", "", "仅第 1-5 列有内容，需确认"),
    (13, "黑白棋盘犬", "", STATUS_TODO, "", "", "11 帧，8-10 与 0 同帧"),
    (14, "棕发大牙兽", "", STATUS_TODO, "", "", "11 帧，形似河狸/水獭"),
    (15, "粉色小怪", "", STATUS_TODO, "", "", "只有 2 个有效帧 + 保持帧，素材偏少"),
    (16, "黄色小鸭", "", STATUS_TODO, "", "", "11 帧"),
    (17, "白粉蓝色泪痕团子", "", STATUS_TODO, "", "", "11 帧"),
    (30, "书页（站立）", "", STATUS_TODO, "", "", "11 帧几乎同图，像静物/书页"),
    (31, "绿发背袋少女", "", STATUS_TODO, "", "", "11 帧，背着袋子，像行商"),
]

# 游商 7 人：数据来自 PeddlerCatalog.java + actors(_zh).properties
PEDDLERS = [
    (0, "buy", "治疗药水 ×4 / 净化药水 ×2 / 鉴定卷轴 ×2 / 祛邪卷轴 ×1"),
    (1, "buy", "鉴定卷轴 ×4 / 心灵视界 ×2 / 魔法地图 ×2 / 治疗药水 ×2"),
    (2, "buy", "升级卷轴 ×2 / 充能卷轴 ×2 / 鉴定卷轴 ×3 / 经验药水 ×1"),
    (3, "buy", "魔法地图 ×3 / 传送卷轴 ×2 / 漂浮药水 ×2 / 急速药水 ×1"),
    (4, "buy", "祛邪卷轴 ×3 / 净化药水 ×2 / 液态火焰 ×2 / 冰霜药水 ×2"),
    (5, "service", "不卖货（数据表里却铺了 4 种货，当前拿不到）；收费治疗 120 金币"),
    (6, "talk", "纯情报，会给一张卷轴（货单数据同上，当前也拿不到）"),
]


def sheet_picture(row):
    im = Image.open(SHEET).convert("RGBA")
    cell = im.crop((0, row * 16, 16, row * 16 + 16)).resize((64, 64), Image.NEAREST)
    bg = Image.new("RGBA", cell.size, (40, 40, 46, 255))
    bg.alpha_composite(cell)
    return bg.convert("RGB")


def png_picture(name):
    path = os.path.join(ASSETS, "sprites", name)
    im = Image.open(path).convert("RGBA")
    cell = im.crop((0, 0, 16, 16)).resize((64, 64), Image.NEAREST)
    bg = Image.new("RGBA", cell.size, (40, 40, 46, 255))
    bg.alpha_composite(cell)
    return bg.convert("RGB")


def build_rows():
    rows = []
    n = 0

    for row, cn, en_name, status, code, func, note in SHEET_ROWS:
        n += 1
        if row == 2:
            cn, en_name = g("actors.mobs.npcs.owo.name"), "Owo"
            desc = g("actors.mobs.npcs.owo.desc")
            talk = talk_of("actors.mobs.npcs.owo")
        elif row == 3:
            cn, en_name = g("actors.mobs.npcs.thunderstorm.name"), "ThunderStorm"
            desc = g("actors.mobs.npcs.thunderstorm.desc")
            talk = talk_of("actors.mobs.npcs.thunderstorm")
        elif row == 4:
            cn, en_name = g("actors.mobs.npcs.boat.name"), "Boat"
            desc = g("actors.mobs.npcs.boat.desc")
            talk = talk_of("actors.mobs.npcs.boat")
        elif row == 5:
            cn, en_name = g("actors.mobs.npcs.seashore.name"), "SeaShore"
            desc = g("actors.mobs.npcs.seashore.desc")
            talk = talk_of("actors.mobs.npcs.seashore")
        elif row == 6:
            cn, en_name = g("actors.mobs.npcs.pw.name"), "PW"
            desc = g("actors.mobs.npcs.pw.desc")
            talk = talk_of("actors.mobs.npcs.pw")
        elif row == 7:
            cn, en_name = g("actors.mobs.npcs.moonlight.name"), "MoonLight"
            desc = g("actors.mobs.npcs.moonlight.desc")
            talk = talk_of("actors.mobs.npcs.moonlight")
        else:
            desc, talk = "", ""

        if status == STATUS_DONE:
            if row in (2, 3, 4, 5, 6, 7):
                floor = "0 层" + ("（1 层也有）" if row == 7 else "")
                spawn = ("0 层四角（NW/NE/SW/SE）随机 4 名之一；"
                         "候选池 = 船/owo/雷暴/海滨/桃井/月华，互不重复")
                if row == 7:
                    spawn += "；另外 1 层固定在入口前一格"
            else:
                floor, spawn = "", ""
            idx = "%d-%d" % (row * 16, row * 16 + 15)
        else:
            floor, spawn = "", ""
            idx = "%d-%d（现成 16 帧，未接代码）" % (row * 16, row * 16 + 15)

        rows.append(dict(
            id="N%02d" % n,
            img=("sheet", row),
            cn=cn, en=en_name, desc=desc, talk=talk,
            func=func, floor=floor, spawn=spawn,
            status=status,
            code=(code + "  ·  帧 " + idx) if code else ("帧 " + idx),
            note=note,
        ))

    for variant, mode, goods in PEDDLERS:
        n += 1
        p = "actors.mobs.npcs.peddler"
        cn = g("%s.name_%d" % (p, variant))
        en_name = g("%s.name_%d" % (p, variant), en=True)
        desc = g("%s.desc_%d" % (p, variant))
        talk = talk_of("%s.talk_%d" % (p, variant))
        if mode == "buy":
            func = "金币购买：打开货架窗口，按标准商人价买。货单：" + goods
        elif mode == "service":
            func = "收费服务：" + goods
        elif mode == "talk":
            func = "纯对话 + 送一张卷轴：" + goods
        else:
            func = goods
        rows.append(dict(
            id="P%d" % (variant + 1),
            img=("png", "npc_peddler%d.png" % variant),
            cn=cn, en=en_name, desc=desc, talk=talk,
            func=func, floor="1 层",
            spawn=("1 层房间内部随机撒点：先收全图可站立空格（排除坑洞/已有单位/已有物品/入口），"
                   "打乱后按真实距离 ≥5 格贪心抽取；抽不满就逐格放宽，最多 7 人"),
            status=STATUS_DONE,
            code="actors/mobs/npcs/Peddler.java + PeddlerCatalog.java + npc_peddler%d.png" % variant,
            note="" if variant != 5 else "服务已改为先验金币再扣费（本分支已修）",
        ))
    return rows


VANILLA = [
    ("商店老板", "Shopkeeper", "shopkeeper.png", "买卖：卖药水卷轴食物，可卖东西给他（原价约 1/5）",
     "6/11/16/21 层（商店层）", "Dungeon.shopOnLevel() 为真时该层必刷一个商店房，随机位置"),
    ("小恶魔商人", "ImpShopkeeper", "demon.png", "买卖：城市层的高价商店（4 层地区各一种货）",
     "16-20 层", "ImpShopRoom，任务/特殊房随机"),
    ("魔杖师", "Wandmaker", "wandmaker.png", "任务：用指定物品换一支随机魔杖",
     "7-9 层", "Wandmaker.questRoomSpawned 任务房，随机"),
    ("铁匠", "Blacksmith", "blacksmith.png", "任务：矿洞重铸/升级装备",
     "11-14 层", "MiningLevel + BlacksmithRoom，随机"),
    ("悲伤的幽灵", "Ghost", "ghost.png", "任务：完成支线给奖励",
     "1-4 层", "Ghost.Quest.spawn()，随机任务房"),
    ("鼠王", "RatKing", "ratking.png", "剧情：秘密房里的鼠王",
     "下水道层", "RatKingRoom（秘密房）随机"),
    ("羊", "Sheep", "sheep.png", "中立单位：由羊毛炸弹/羊群石/羊群陷阱/诅咒法杖生成",
     "任意层", "不预生成，由道具/陷阱召唤"),
]


def build(force_dir=None):
    from openpyxl import Workbook
    from openpyxl.drawing.image import Image as XLImage
    from openpyxl.styles import Alignment, Font, PatternFill
    from openpyxl.utils import get_column_letter

    out_dir = force_dir or os.path.dirname(OUT_XLSX)
    os.makedirs(out_dir, exist_ok=True)
    tmp_dir = os.path.join(out_dir, "_npc_pics")
    os.makedirs(tmp_dir, exist_ok=True)

    rows = build_rows()

    wb = Workbook()
    ws = wb.active
    ws.title = "NPC总表"

    head_fill = PatternFill("solid", fgColor="3B3B46")
    head_font = Font(bold=True, color="FFD979", size=11)
    for c, (title, width) in enumerate(HEADERS, start=1):
        cell = ws.cell(row=1, column=c, value=title)
        cell.fill = head_fill
        cell.font = head_font
        cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
        ws.column_dimensions[get_column_letter(c)].width = width
    ws.row_dimensions[1].height = 26
    ws.freeze_panes = "C2"

    todo_fill = PatternFill("solid", fgColor="4A3A2A")
    for i, r in enumerate(rows, start=2):
        ws.row_dimensions[i].height = 52
        kind, key = r["img"]
        pic = sheet_picture(key) if kind == "sheet" else png_picture(key)
        pic_path = os.path.join(tmp_dir, "%s.png" % r["id"])
        pic.save(pic_path)
        img = XLImage(pic_path)
        img.width = img.height = 52
        ws.add_image(img, "B%d" % i)
        values = [r["id"], "", r["cn"], r["en"], r["desc"], r["talk"], r["func"],
                  r["floor"], r["spawn"], r["status"], r["code"], r["note"]]
        for c, v in enumerate(values, start=1):
            if c == 2:
                continue
            cell = ws.cell(row=i, column=c, value=v)
            cell.alignment = Alignment(vertical="top", wrap_text=(c in (5, 6, 7, 9, 11, 12)))
            if r["status"] != STATUS_DONE:
                cell.fill = todo_fill
        if r["status"] != STATUS_DONE:
            ws.cell(row=i, column=2).fill = todo_fill

    # ---- 说明页
    ws2 = wb.create_sheet("填写说明")
    ws2.column_dimensions["A"].width = 20
    ws2.column_dimensions["B"].width = 110
    notes = [
        ("这是什么", "本项目当前所有 NPC 的可编辑清单。每行一个 NPC，右边列都是纯文本，直接在格子里改就行。"),
        ("图画", "从 new_npc.png / npc_peddler*.png 自动截的第 1 帧，放大 4 倍。改图不用动这里，"
                 "只要告诉我换哪一行的图即可。"),
        ("实现状态", "“已实现”= 游戏里已经能刷出来、有对应类；“未实现”= 图在素材里但没接代码。橙色底的就是未实现的。"),
        ("帧索引", "new_npc.png 是 16×16 一帧、每行 16 帧。第 r 行第 c 列的帧号 = r*16+c，"
                   "写 Sprite 类时 idle.frames(frames, ...) 直接用这个号。"),
        ("中文名 / 英文名 / 描述 / 对话", "对应 properties：name / desc / msg*。多个数字后缀的 msg0、msg1 就是交互时按顺序说的话。"),
        ("功能", "这个 NPC 到底干什么：纯对话、买卖、任务、收费服务。想改成别的就在这里写清楚。"),
        ("出没层 / 刷新规则", "现在是怎么刷的。想改（比如让游商去 2 层、改成必刷 3 个）也写这两列。"),
        ("改完怎么给我", "存盘后把文件发回来（或说“改好了，路径在 xxx”），我会用 tools/npc_table.py dump 把它读成 JSON，"
                       "再逐行对照代码实现，不需要你再打一遍字。"),
        ("注意", "第 10、12 行我标了“（续帧？）”：它们的帧和上一行风格一致但像素不相同，"
                 "需要你确认是同一角色的第二段动画，还是两个独立角色——这个我判断不了。"),
    ]
    for i, (k, v) in enumerate(notes, start=1):
        ws2.cell(row=i, column=1, value=k).font = Font(bold=True)
        c = ws2.cell(row=i, column=2, value=v)
        c.alignment = Alignment(wrap_text=True, vertical="top")

    # ---- 原版 NPC 页
    ws3 = wb.create_sheet("原版NPC")
    heads3 = ["图画", "中文名", "英文名", "功能", "出没层", "刷新规则"]
    for c, h in enumerate(heads3, start=1):
        cell = ws3.cell(row=1, column=c, value=h)
        cell.fill = head_fill
        cell.font = head_font
    for c, w in enumerate([11, 14, 14, 46, 16, 40], start=1):
        ws3.column_dimensions[get_column_letter(c)].width = w
    for i, (cn, en_name, png, func, floor, spawn) in enumerate(VANILLA, start=2):
        ws3.row_dimensions[i].height = 52
        try:
            pic = png_picture(png)
            p = os.path.join(tmp_dir, "V%d.png" % i)
            pic.save(p)
            img = XLImage(p)
            img.width = img.height = 52
            ws3.add_image(img, "A%d" % i)
        except FileNotFoundError:
            ws3.cell(row=i, column=1, value="（缺图 %s）" % png)
        for c, v in enumerate(["", cn, en_name, func, floor, spawn], start=1):
            if c == 1:
                continue
            ws3.cell(row=i, column=c, value=v).alignment = Alignment(
                vertical="top", wrap_text=(c in (4, 6)))

    wb.save(OUT_XLSX)

    # ---- 纯文本备份，方便 git diff / 直接看
    import csv
    with open(OUT_CSV, "w", encoding="utf-8-sig", newline="") as f:
        w = csv.writer(f)
        w.writerow([h for h, _ in HEADERS])
        for r in rows:
            w.writerow([r["id"], "<%s:%s>" % r["img"], r["cn"], r["en"], r["desc"],
                        r["talk"], r["func"], r["floor"], r["spawn"], r["status"],
                        r["code"], r["note"]])

    print(OUT_XLSX)
    print(OUT_CSV)
    print("rows:", len(rows))


def dump(path):
    from openpyxl import load_workbook
    wb = load_workbook(path, data_only=True)
    ws = wb["NPC总表"]
    out = []
    for row in ws.iter_rows(min_row=2, values_only=True):
        if not row or not row[0]:
            continue
        out.append({
            "id": row[0], "cn": row[2], "en": row[3], "desc": row[4], "talk": row[5],
            "func": row[6], "floor": row[7], "spawn": row[8], "status": row[9],
            "code": row[10], "note": row[11],
        })
    print(json.dumps(out, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] == "build":
        build(sys.argv[2] if len(sys.argv) > 2 else None)
    elif sys.argv[1] == "dump":
        dump(sys.argv[2] if len(sys.argv) > 2 else OUT_XLSX)
    else:
        print(__doc__)
