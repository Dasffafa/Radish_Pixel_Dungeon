# -*- coding: utf-8 -*-
import io

ZH = r'core/src/main/assets/messages/items/items_zh.properties'
EN = r'core/src/main/assets/messages/items/items.properties'

items = [
    ("championbelt", "冠军腰带", "只有最伟大的人才能带上这条腰带。", "你对精英或boss敌人造成的伤害增加（10+5*等级）%。", "You deal (10+5*level)% more damage to elite and boss enemies."),
    ("blackbelt", "黑带", "代表跆拳道最高水平的象征。", "你的空手攻击会使敌人虚弱（2+等级）回合。你的空手攻击伤害增加（2~4+等级）点。", "Your unarmed attacks weaken enemies for (2+level) turns, and deal (2-4+level) extra damage."),
    ("andromeda", "仙女座", "这对金光闪闪的手套似乎是由黄金打造的，这种珍贵的金属上附着的魔力似乎可以使你对危险状况预警。", "当你将要进入的房间内有3个或以上敌人时，发出一次提示弹窗警告。", "You are warned when entering a room with 3 or more enemies."),
    ("infusedarmband", "灌能臂缠", "这两条绘有符文的布缠可以从法杖中汲取能量。", "在你使用法杖造成伤害后，获得持续10回合的1-3点伤害加成与（2+等级）点护盾。", "After dealing damage with a wand, gain 1-3 bonus damage for 10 turns and (2+level) shielding."),
    ("catalystbracer", "催化手环", "你在饮用药水之后，似乎感受到它回馈了一部分能量给你。", "在你使用药水或卷轴后，获得（1+0.2*等级）点炼金能量。", "Using a potion or scroll grants (1+0.2*level) alchemy energy."),
    ("shelter", "庇护", "手臂上坚硬的触感使你有种厚重的安心感。", "在你原地等待时，获得（3+等级）点护盾。（不可叠加。）", "Waiting in place grants (3+level) shielding (does not stack)."),
    ("griffineye", "格里芬之眼", "冠以狮鹫之名的头冠，上面镶嵌了一颗明亮的黄色宝石。", "所有敌人受到的闪电伤害增加（25+15*等级）%。", "All enemies take (25+15*level)% more lightning damage."),
    ("webbelt", "蛛网腰带", "似乎带有毒性的腰带，让人不敢轻易碰触。", "所有敌人受到的毒素伤害增加（15+10*等级）%。你在受到物理攻击后，有20%概率在原地留下一团蛛网。", "All enemies take (15+10*level)% more poison damage. After taking a physical hit, 20% chance to leave a web."),
    ("thepure", "纯洁者", "在平时暗淡无光的领带。", "如果你没有所有标记和效果，则你受到的所有伤害减少（2+等级*0.2）点。", "If you have no debuffs or buffs, all damage you take is reduced by (2+0.2*level)."),
    ("tridentgauntlet", "三叉护手", "在前端分出了三支锋利的叉，但看上去并不能直接用于攻击敌人。那是做什么用的呢？", "造成的火焰伤害，冰霜伤害和闪电伤害增加（20+10*等级）%。造成的物理伤害下降30%。", "Fire, frost and lightning damage dealt +(20+10*level)%. Physical damage dealt -30%."),
    ("shininggloves", "闪亮手套", "一双有时闪着光，有时暗淡无光的手套。", "如果你的血量为满则造成的所有伤害提升（25+5*等级）%，对血量为满的敌人造成的伤害提升同样的值。", "At full HP, all damage you deal is +(25+5*level)%, and damage to full-HP enemies is increased by the same amount."),
    ("greaves", "护胫甲", "能够对你的小腿提供有效保护。", "获得（10+2*等级）%的物理伤害减免。", "Gain (10+2*level)% physical damage reduction."),
]

zh = []
en = []
zh.append("\n# 次要护甲")
en.append("\n# Secondary armor")
base_zh = ("items.armor.secondaryarmor.base_desc=这件%d阶的次要护甲能够格挡%d-%d点伤害，并且需要%d点力量来正常使用。"
           "当力量不足时，次要护甲的特殊效果失效，并且获得相当于主要护甲50%的超力惩罚。")
base_en = ("items.armor.secondaryarmor.base_desc=This tier-%d secondary armor blocks %d-%d damage and requires %d strength to use properly. "
           "If your strength is too low its effect stops working and you gain 50%% of the overweight penalty of your main armor.")
zh.append(base_zh)
en.append(base_en)
zh.append("items.armor.secondaryarmor.equipped=你把%1$s穿在了主要护甲外面。")
en.append("items.armor.secondaryarmor.equipped=You strap the %1$s over your armor.")
zh.append("items.armor.secondaryarmor.level_desc=这件次要护甲已被强化到 +%d。")
en.append("items.armor.secondaryarmor.level_desc=This secondary armor has been upgraded to +%d.")

for key, name, desc, eff, eff_en in items:
    zh.append("items.armor.secondary.%s.name=%s" % (key, name))
    zh.append("items.armor.secondary.%s.desc=%s" % (key, desc))
    zh.append("items.armor.secondary.%s.effect_desc=%s" % (key, eff))
    en.append("items.armor.secondary.%s.name=%s" % (key, name))
    en.append("items.armor.secondary.%s.desc=%s" % (key, desc))
    en.append("items.armor.secondary.%s.effect_desc=%s" % (key, eff_en))

for path, lines in ((ZH, zh), (EN, en)):
    s = io.open(path, encoding='utf-8').read()
    if not s.endswith("\n"):
        s += "\n"
    s += "\n".join(lines) + "\n"
    io.open(path, 'w', encoding='utf-8', newline='\n').write(s)
    print('appended', path, len(lines))
