package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.watabou.noosa.Image;

import java.util.ArrayList;

import static com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.*;

public class RA_v0_8_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v08_4_Changes(changeInfos);
        add_v08_3_Changes(changeInfos);
        add_v08_2_Changes(changeInfos);
        add_v08_1_Changes(changeInfos);
        add_v08_0_Changes(changeInfos);
    }

    public static void add_v08_4_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        //==========================================================
        // 新内容
        //==========================================================
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(SENBON), "新投掷物：千本",
                ("_-_ 千本：一次扇形甩出 12 枚钢针，各自飞行，真正扎中目标才结算一次普通物理伤害。")));

        changes.addButton(new ChangeButton(new ItemSprite(ARMOR_COUNTESS), "四件新护甲",
                ("_-_ 伯爵礼服：获得或花费金币后回血（赎回除外）。\n\n" +
                        "_-_ 寒铁大衣：免疫冻伤与冰冻。\n\n" +
                        "_-_ 棱镜护铠：远程减伤 25%，法术伤害有概率反射。\n\n" +
                        "_-_ 混灵：击杀敌人叠加护甲与武器的额外等级。")));

        changes.addButton(new ChangeButton(new ItemSprite(DIMENSIONAL_RIPPER), "新附魔：虚空",
                ("这个强大的附魔可以撕扯空间，若目标脚下的地块合规，则有可能在攻击后直接撕开它，使目标落入虚空。")));

        changes.addButton(new ChangeButton(new ItemSprite(HOLY_DECREE), "三把新武器",
                ("_-_ 圣宣：四阶长鞭，4 倍速、射程 8；命中撒黑白蝴蝶并随机造成神圣/腐蚀伤害，偶尔留下蝴蝶场地。\n\n" +
                        "_-_ 沉默的代价：四阶武器，第十二次命中蓄势，第十三次命中斩杀或追加魔法伤害。\n\n" +
                        "_-_ 次元撕裂者：三阶弯刀，命中概率随机传送目标或自己，附带腐蚀伤害并可开窗追击。")));

        changes.addButton(new ChangeButton(new ItemSprite(BONE_PILE), "一些新物品",
                ("_-_ 哥布林：简易投石索，使用投石索会消耗一颗石头投掷，将它的伤害提升4倍，此道具和石头会一并摧毁。\n\n" +
                        "_-_ 狗：狗腿，可以丢出去吸引怪物。\n\n" +
                        "_-_ 监狱内敌人：律法碎片，可以作为某些炼金配方中卷轴的替代品，也可以令视野中的犯人和狱警短暂麻痹。\n\n" +
                        "_-_ 灵魂：灵魂余烬，可以分解为1炼金能量。\n\n" +
                        "_-_ 机械类敌人：机械碎片。\n\n" +
                        "_-_ 矮人和豺狼人：闪晶，用于+3以下装备，50%概率令其上升一级，50%概率令其下降一级，0级装备会被摧毁。\n\n" +
                        "_-_ 矮人炮手：大炮，可以用来发射炸弹出去，造成多倍的爆炸伤害。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.ROGUE, 5, HeroClasses.ROGUE_SKIN_GAMBLER), "英雄皮肤系统",
                ("英雄角色现在可以换上来自 Slice & Dice 的新皮肤：\n\n" +
                        "_-_ 盗贼：赌徒\n\n" +
                        "_-_ 战士：流浪者\n\n" +
                        "_-_ 月华：圆球\n\n" +
                        "_-_ 全职业共享：杂散（Jumble）\n\n" +
                        "_-_ 这些皮肤都有独立的特殊效果，选择前请仔细查看英雄选择界面。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "职业系统重构",
                ("职业与子职业由枚举重构为定义式框架，为后续新英雄、新子职业与新皮肤提供统一的扩展入口。\n\n" +
                        "_-_ 职业、子职业、天赋层级、护甲技能、初始装备与皮肤统一定义。")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.GOBLIN_RIDER, 0, 0, 16, 18), "新小Boss：哥布林骑手",
                ("苔藓洞穴（跆拳洞穴）第2层新增一只小Boss：哥布林骑手。\n\n" +
                        "_-_ 驾驭青蛙作为坐骑，远程投出捕鱼矛，伤害高于普通哥布林的投石。\n\n" +
                        "_-_ 近战时与坐骑青蛙一起攻击，伤害叠加。\n\n" +
                        "_-_ 击败后掉落3只捕鱼矛与本层金钥匙；该层原本随机刷新的金钥匙改由它固定掉落。")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.WARDEN, 0, 0, 12, 16), "新Boss：典狱长",
                ("地牢第 10 层新增一名 Boss：典狱长，会与天狗随机替换出现。\n\n" +
                        "_-_ 半血之后眼睛会变红，攻击也更加凶狠。\n\n" +
                        "_-_ 战斗区里散布着 8 台 DM-100 与 3 台 DM-175 原型机，一开始都是未激活状态。\n\n" +
                        "_-_ 典狱长靠近原型机会花 2/4 回合把它唤醒为自己的部下，友军达到 3 台才会主动来找你。\n\n" +
                        "_-_ 你也可以用房间里捡到的「简易控制器」把原型机拉到自己这边，或者对它投掷「电击符石」。\n\n" +
                        "_-_ 每失去 30 点生命，地图里会再补一台 DM-100；失去 60 点生命后唤醒速度更快。\n\n" +
                        "_-_ 入口房间改成天狗第一间屋子的样式，顶墙上开门，开门直接进战斗区；踏进战斗区后典狱长和原型机才会出场。\n\n" +
                        "_-_ 击败它才能打开出口，并掉落「天狗的面具」。")));

        changes.addButton(new ChangeButton(new ItemSprite(ARMOR_HOLDER), "12 件次要护甲",
                ("装备界面新增一排 6 格的次要护甲槽，本版本先加入 12 件可强化、可减伤的次要护甲：\n\n" +
                        "_-_ 逐级强化，描述会按当前等级显示具体数值，力量需求与强化等级也会显示在背包格里。\n\n" +
                        "_-_ 已接入闪亮手套、护胫甲、纯洁者、三叉护手、冠军腰带、黑带、庇护、格里芬之眼、蛛网腰带、灌能臂缠、催化手环等装备的联动效果。\n\n" +
                        "_-_ 也接入了仙女座的房间敌人预警。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "游商与 NPC 系统",
                ("_-_ 地牢第 1 层新增流动商人「游商」，参考暗黑地牢的 BarterMan，以物换物：把你的东西摆上货架，换他的货。\n\n" +
                        "_-_ 游商只生成在房间内部，不会站到走廊上；货架内容会写进存档，读档后不会被清空。\n\n" +
                        "_-_ 灰羽的治疗服务改为先确认金币再收费，金币不足时会明确提示。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "德鲁伊与召唤物",
                ("_-_ 召唤物补全了名称与描述，技能描述里补上具体数值。\n\n" +
                        "_-_ 召唤物改为使用各自的贴图，不再共用一套。\n\n" +
                        "_-_ 修复太阳花满血时还在分血的bug。\n\n" +
                        "_-_ 修复多个召唤物重叠站位的问题。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "选人界面与清档",
                ("_-_ 英雄选择界面重新设计，职业与皮肤选择更直观，并可直接查看与开关挑战。\n\n" +
                        "_-_ 存档列表新增「删除所有存档」按钮，点按后先统计并确认，再一次性删掉所有进行中的存档。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "测试模式改进",
                ("_-_ 楼层跳转器支持直接输入层数，还能直达 Boss 层，并新增空白测试场。\n\n" +
                        "_-_ 怪物生成器改为网格排列，不会再刷到格子外面。\n\n" +
                        "_-_ 附魔选择改为双列并加宽；次要护甲生成器改为弹窗自选。")));

        //==========================================================
        // 调整
        //==========================================================
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_BLADES), "骰子法师法术调整",
                ("_-_ 刃雨：获得力量加成，投射物改为纯视觉表现，伤害改为同步结算，飞刀会穿过倒下的尸体，以避免实际投射物导致的卡顿。\n\n" +
                        "_-_ 收集：效果改为将7格内的物品全部拉到自己脚下，也会回收范围内怪物身上的投掷物。\n\n" +
                        "_-_ 预知：效果改为给予英雄20回合的魔能透视，施法不消耗回合。\n\n" +
                        "_-_ 藤蔓：施法消耗0.33回合，缠绕回合数会叠加。\n\n" +
                        "_-_ 电闪：类似电法的注能面，会随着当前法力值提高伤害。\n\n" +
                        "_-_ 恶咒：给3格内的一个目标7-15回合的虚弱、易伤、幻惑、失明、残废、禁疗，每个效果独立计时，对boss单位效果为25%，冷却50回合。\n\n" +
                        "_-_ 切割：额外附加33%武器伤害与力量加成，并触发武器附魔。\n\n" +
                        "_-_ 劈砍：对扇形敌人用手中武器进行一次高强度打击，享受力量加成。\n\n" +
                        "_-_ 烧焦：令3x3范围内敌人的火焰伤害立刻燃烧完毕，并再次点燃这些敌人。\n\n" +
                        "_-_ 爆燃：消耗6魔力点，对燃烧中的敌人造成100-150火焰伤害并引爆，火焰扩散到周围5×5范围，无冷却。")));

        changes.addButton(new ChangeButton(new ItemSprite(HEADCLEAVER), "近战武器调整",
                ("_-_ 斩首者：对大于30%血量的英雄生效时，会给英雄保留1点血量；小于30%仍会直接死亡。\n\n" +
                        "_-_ 锁镰：攻击被追寻附魔标记的敌人时，会把不管多远的敌人都拉过来并攻击，原本不能拉拽的情况依然不能拉拽。\n\n" +
                        "_-_ 环刃：增加1点攻击距离，成长改为4阶武器；防御转攻击的效果会对树肤生效；命中敌人时消耗所有护盾并转化为伤害；残像的必定闪避变为一次必定命中。\n\n" +
                        "_-_ 新星法杖：初始范围扩大一圈（3x3 → 5x5），范围扩大的等级要求由每四级改为每三级。")));

        changes.addButton(new ChangeButton(new ItemSprite(POUL_WATER), "敌人调整",
                ("_-_ 传奇重犯：生命上限 120 → 140；半血以下蓄力的那一回合还能朝目标挪动一步。\n\n" +
                        "_-_ 青蛙粘液：干涸脱落时会把身上的护盾一起清空，并造成 1 点伤害。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "背包与界面",
                ("_-_ 界面缩放调回破碎地牢原版：不再单独降低 UI 缩放，修复选择 8X 反而比 6X 更小、以及拖动画面时视野乱滑的问题。\n\n" +
                        "_-_ 背包按屏幕宽度自动增加列数（最多 9 列），并留出两边边距，6X 缩放下也不会被屏幕挡住。\n\n" +
                        "_-_ 背包可见区按屏幕高度限制并可上下拖动，滚动条加粗、贴合窗口，格子补齐成整行不再留空。\n\n" +
                        "_-_ 修复背包拖动时手指离开格子会误触发点击、直接把背包关掉。")));

        changes.addButton(new ChangeButton(new ItemSprite(SPIRIT_ARROW), "美术与贴图",
                ("_-_ 灵能宫与猎头蟹射出的箭还原为破碎地牢女猎手箭矢的原版尺寸与形状。\n\n" +
                        "_-_ 冰晶合剂的冰块、龙卷合剂的气旋改用专属贴图，气旋只保留风眼，并修复空帧崩溃。\n\n" +
                        "_-_ 陷阱图标区替换为新图集，已触发/已消耗的陷阱改为淡灰空迹，不再是旧版的深色图案。")));

        //==========================================================
        // 修复
        //==========================================================
        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight(CharSprite.NEGATIVE);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), "战斗与技能修复",
                ("_-_ 修复 骰子法师学派升级时在部分设备闪退、刃雨飞刀无法穿过尸体、伤害结算不稳定的bug\n\n" +
                        "_-_ 修复 黏稠刻印延迟伤害优先级错误（现在护甲先阻挡、再对剩余伤害做延迟）\n\n" +
                        "_-_ 修复 血色哨卫能被打死、牧师信仰值是小数的问题\n\n" +
                        "_-_ 修复 月华「弹射起步」在残废/冻伤早于天赋获得时完全不触发；免费轮椅窗口由1回合延长到10回合\n\n" +
                        "_-_ 修复 薪王化身的索命弯刀用投掷无法触发；凡体受神使用后不消耗回合\n\n" +
                        "_-_ 修复 小恶魔在已有护甲技能时仍能接受「激发潜力」，导致潜力无法正常使用\n\n" +
                        "_-_ 调整 圆球的动画速度加快1.5倍")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.WARDEN, 0, 0, 12, 16), "敌人与 Boss 修复",
                ("_-_ 修复 典狱长精灵帧索引错位、读档后红眼形态丢失、击杀时缺少 BOSS 横幅与音效\n\n" +
                        "_-_ 修复 进入典狱长关卡时因视野数组未初始化导致的首回合闪退\n\n" +
                        "_-_ 修复 12 台原型机生成时互相重叠在同一格\n\n" +
                        "_-_ 修复 原型机被激活后仍显示停机贴图，现在会换成 DM-100 / DM-175 原本的样子\n\n" +
                        "_-_ 修复 哥布林骑手远程投矛后动画不回调导致卡死；投矛改为虚拟投掷物，被闪避后不再掉出实体捕鱼矛\n\n" +
                        "_-_ 修复 传奇重犯的技能会被召唤物带偏，现在优先锁定英雄\n\n" +
                        "_-_ 修复 矮人护卫师会给敌对目标套盾，现在只对同阵营生效\n\n" +
                        "_-_ 修复典狱长在狂乱、恐惧或麻痹时仍优先激活原型机，并修复未激活原型机在迷雾中残留显示、被破灭之戒误伤的问题\n\n" +
                        "_-_ 修复典狱长隔门醒来、可被沉眠控制及战斗中离开楼层；校正原型机启动计时与受击、腐化后的阵营转换，战败后清除残留原型机")));

        changes.addButton(new ChangeButton(Icons.get(Icons.SCROLL_GREY), "界面与系统修复",
                ("_-_ 修复 商品堆可免费拾取的漏洞\n\n" +
                        "_-_ 修复 影舞斗篷隐身结束后无法触发破隐突袭\n\n" +
                        "_-_ 修复 普通炼金界面使用药水时英雄贴图为空导致的崩溃\n\n" +
                        "_-_ 修复 戏卡者能量胸甲贴图缺帧、法杖光效不随职业偏移\n\n" +
                        "_-_ 修复 卡牌选择窗口在挂载前布局滚动面板导致的闪退\n\n" +
                        "_-_ 修复 投石索卡死与伤害丢失\n\n" +
                        "_-_ 修复 戏卡者二级鉴定卡取消确认后可以重复使用的漏洞\n\n" +
                        "_-_ 戏卡者的攻击卡与终幕卡会优先使用最近选中的可见敌人作为目标\n\n" +
                        "_-_ 修复 存档列表撑满时清档按钮压住最后一个存档\n\n" +
                        "_-_ 修复 游商会站到走廊上、灰羽的治疗服务不扣金币\n\n" +
                        "_-_ 修复 投掷选格时残留自动索敌标记、地图外点击被映射到边缘格\n\n" +
                        "_-_ 修复 首次进入游戏时自动索敌标记尚未初始化导致的闪退\n\n" +
                        "_-_ 修复 鉴定卡没有可鉴定物品时会生成额外物品的漏洞\n\n" +
                        "_-_ 修复 已触发陷阱显示旧式图案，恢复新式空陷阱图案\n\n" +
                        "_-_ 修复 仙女座在高危房间开门前无法给出确认提示的问题\n\n" +
                        "_-_ 修复 次要护甲的元素增伤、满血增伤、受击撒网与法杖触发条件未生效的问题\n\n" +
                        "_-_ 修复 次要护甲无法用升级卷轴强化、强化后效果数值不增长的问题\n\n" +
                        "_-_ 修复 灌能臂缠对解离、新星等法杖不触发；校正次要护甲超力惩罚与减伤保底数值\n\n" +
                        "_-_ 修复 闪亮手套、灌能臂缠、三叉护手的伤害加成对法杖不生效的问题\n\n" +
                        "_-_ 补齐工程师天赋图标与中英文文本，修复天赋图标缺失导致的资源校验失败\n\n" +
                        "_-_ 修正 四件新护甲图标在图鉴中歪斜；护甲描述改显实际数值并用黄色标出\n\n" +
                        "_-_ 修复 测试模式五阶护甲生成器选错护甲；混灵的额外等级现在持续 _10_ 回合，并在描述中注明\n\n" +
                        "_-_ 修正 四件新护甲的角色外观贴图按项目 12x15 帧标准原样接入，解决穿戴时贴图错位\n\n" +
                        "_-_ 修正 棱镜护铠与寒铁大衣的物品图标互换\n\n" +
                        "_-_ 修正 工程师未出现在选人界面的问题\n\n" +
                        "_-_ 修复 典狱长 Boss 层站在门口就会看到并刷出原型机的问题，改为踏进战斗区才立刻生成\n\n" +
                        "_-_ 修正 左上角英雄头像未跟随新护甲外观\n\n" +
                        "_-_ 修正 除战士外角色的护甲外观贴图偏移（每张素材行起点不同），月华不再加载新外观\n\n" +
                        "_-_ 修复 踩虫洞陷阱传送后人物消失、传奇重犯踩门就出现的问题\n\n" +
                        "_-_ 修复 商店重叠物品被误判为待售，玩家放下的物品现在不需购买且可拾起\n\n" +
                        "_-_ 修正 月华的死亡动画不再从最后一帧倒回上一帧\n\n" +
                        "_-_ 补齐 戏卡者的四件新护甲动画，穿戴时游戏内小人与左上角头像同步切换\n\n" +
                        "_-_ 撤回 破坏球投掷物及其蓄力击飞机制，本次更新仅保留千本")));

        changes.addButton(new ChangeButton(new ItemSprite(DIMENSIONAL_RIPPER), "击退、传送与法杖修复",
                ("_-_ 修复 冲击波、水爆、间歇泉陷阱等击退无法把怪物和英雄推进深渊的问题\n\n" +
                        "_-_ 修复 踩传送陷阱、虫洞陷阱后人物贴图留在原地、位置错乱的问题\n\n" +
                        "_-_ 修复 震爆法杖爆炸时英雄或友军站在地图最上面一行会闪退的问题；友军现在按描述从爆心向外击退 2 格\n\n" +
                        "_-_ 修复 震爆法杖贴着地图左右边缘时爆炸范围绕到另一侧、换层后在新楼层的错误位置爆炸、未鉴定时显示真实等级伤害的问题\n\n" +
                        "_-_ 修复 在典狱长战斗区最上面一行把水爆魔药扔在脚下会闪退的问题\n\n" +
                        "_-_ 修复 「踹飞」、治愈圣启、神佑长枪在贴近地图边缘时把敌人推向错误方向的问题\n\n" +
                        "_-_ 修复 带治愈圣启施放凡体受神时，视野内每有一个敌人就多消耗一回合的问题\n\n" +
                        "_-_ 修复 凡体受神不消耗护甲充能的问题")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.WARDEN, 0, 0, 12, 16), "敌人与 Boss 修复（二）",
                ("_-_ 修复 典狱长战中读灵爆卷轴、攻击或触碰未激活的原型机会闪退的问题\n\n" +
                        "_-_ 修复 站在门外就能攻击典狱长却不开战的问题：典狱长和原型机改为英雄踏进战斗区后才出场\n\n" +
                        "_-_ 修复 DM-175 原型机的护盾被打掉后又重新获得的问题\n\n" +
                        "_-_ 修复 用放大镜查看原型机时显示成另一种机型的问题\n\n" +
                        "_-_ 修复 典狱长正在激活原型机时存的档读不回来、读档后 Boss 血条消失的问题\n\n" +
                        "_-_ 修复 蛇咬挑战下原型机被激活会闪退的问题\n\n" +
                        "_-_ 修复 索敌精英哥布林丢完石头后不会隔空攻击的问题；哥布林骑手、拷问官等成为索敌精英时同样用不上额外攻击距离\n\n" +
                        "_-_ 修复 豺狼大酋长战斗中的两处卡死和一处闪退\n\n" +
                        "_-_ 修复 传奇重犯出场时与其它角色重叠、旧存档里打败后重新出现、它在门外时门被锁死、复活后落在围墙外的问题\n\n" +
                        "_-_ 修复 地底亚龙、聚合骷髅、猎头蟹、青蛙、大蛇兹克等怪物的若干读档与结算问题")));

        changes.addButton(new ChangeButton(new ItemSprite(ARMOR_COUNTESS), "装备与商店修复",
                ("_-_ 修复 穿着带专属外观的护甲时被伪装成月华或工程师、以及工程师穿螃蟹护铠等护甲会闪退的问题\n\n" +
                        "_-_ 修复 混灵的额外等级读档后丢失、用法杖击杀不叠层的问题\n\n" +
                        "_-_ 修复 棱镜护铠对远程法术的减伤和反射此前从未生效的问题\n\n" +
                        "_-_ 修复 伯爵礼服在普通商店花钱不回血的问题\n\n" +
                        "_-_ 修复 未鉴定的伯爵礼服、棱镜护铠、混灵会从说明里看出强化等级的问题\n\n" +
                        "_-_ 修复 背包已满时更换或卸下次要护甲，护甲直接消失的问题\n\n" +
                        "_-_ 修复 蛛网腰带、三叉护手的说明显示成未替换的占位符的问题\n\n" +
                        "_-_ 修复 闪亮手套对满血的友方单位也增伤、穿戴纯洁者时饥饿不再掉血的问题\n\n" +
                        "_-_ 修复 掉在商品格上的物品被压在商品下面拿不到的问题\n\n" +
                        "_-_ 虚空附魔：撕开后会把路截断的位置只会出现转瞬即合的裂隙，不再留下深渊\n\n" +
                        "_-_ 沉默的代价与圣宣更换了贴图，圣宣有了专属的命中音效\n\n" +
                        "_-_ 修正 工程师的物品图标、天赋图标与角色贴图\n\n" +
                        "_-_ 修复 重启游戏后存档预览里的小人不显示新护甲外观、能量胸甲显示成没穿护甲的问题")));
    }

    public static void add_v08_3_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_FIRE), "骰子法师学派系统",
                ("骰子法师的法术体系全面重做为学派系统！\n\n" +
                        "_-_ 新增火焰、刀刃、咒法、法力、血液、自然、医疗、物理、紧急、特殊十大学派，每个学派投入点数逐级习得对应法术\n" +
                        "_-_ 新增专属学派升级窗口，升级时按权重抽取两个学派二选一，也可随机提升某学派或跳过\n" +
                        "_-_ 施法界面只显示当前学派等级对应的可用法术\n" +
                        "_-_ 特殊学派每局会随机分配3个特殊法术（光束、标记、充能、热量、闪耀等）\n" +
                        "_-_ 新增魔力点冷却机制与多个学派相关Buff（标记、预言、抚慰回复、星火等）\n" +
                        "_-_ 新增 法术图鉴：日志界面新增\"骰子法师法术\"栏目，列出所有法术的图标、名称、描述与所属学派\n" +
                        "_-_ 学派天赋图标现在会显示\"即将解锁\"法术的贴图")));

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_MANA), "骰子法师魔力系统",
                ("魔力获取、储存与消耗方式全面翻新！\n\n" +
                        "_-_ 每_20_回合自动获得_1_点魔力点；视野内的怪物死亡时获得_1_点魔力点\n" +
                        "_-_ 每_25_回合，超过_3_点以上的魔力会被清空（魔力Buff描述会显示剩余回合）\n" +
                        "_-_ 魔力不足时，可消耗背包内法杖充能：每点充能提供_1+0.33×法杖等级_点魔力，施法时会弹窗询问并列出将被消耗充能的法杖\n" +
                        "_-_ 魔力不足但法杖充能足够时，施法按钮也可点击（不再置灰）\n\n" +
                        "_-_ 新物品：魔力药水，饮用后恢复魔力点。配方为药剂+种子+卷轴，每局随机各选一个\"正确\"要素（不含力量药水与升级卷轴），命中数量决定成品等级：全对=高等(24)、中2=中等(12)、中1=下等(6)、全错=随机合剂或秘药（不含肌肉记忆合剂、龙血秘药）\n" +
                        "_-_ 只有骰子法师能炼制并饮用魔力药水，下等为暗淡低饱和贴图，中等正常，高等附带附魔彩色光环")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "伤害系统重构",
                ("统一并重构底层伤害处理管线：\n\n" +
                        "_-_ 新增 DamageInfo 修改器管线与混合伤害类型，以支持未来可能的复杂伤害计算逻辑和来源追踪逻辑；\n\n" +
                        "_-_ 新增统一伤害事件系统（原始伤害、最终伤害、攻击、Buff生效等事件）\n\n" +
                        "_-_ 重写多处涉及伤害的物品、法杖、附魔、符石、陷阱与Boss战逻辑\n\n" +
                        "_-_ 强化伤害显示与浮动文字表现\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "素材与视觉更新",
                ("_-_ 物品图标迁移与新图集整理，SND音效图集与天赋图标调整\n")));


        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 拥有遗忘碎片时砥砺锋芒天赋不生效的bug。\n" +
                        "_-_ 修复 替身木桩、豺狼王镜像、大地守护者、守卫雕像等NPC无法受到伤害的bug\n\n" +
                        "_-_ 修复 天狗层二阶段玩家卡墙play的bug\n\n" +
                        "_-_ 修复 猎杀直觉经验计数器未在存档中保存，读档后计数丢失的bug\n\n" +
                        "_-_ 修复 天狗层二阶段玩家卡墙play的bug\n\n" +
                        "_-_ 修复 大量文本位置不太正确的bug\n\n" +
                        "_-_ 修复 战士精巧纹章天赋受击刻印不正常触发的bug（原版本中战斗外刻印仍正常触发）\n\n" +
                        "_-_ 修复 战士破损纹章行为异常的bug\n\n" +
                        "_-_ 修复 豺狼双王不再掉落一把骷髅钥匙\n")));
    }

    public static void add_v08_2_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_ 镜像受到伤害后不死亡的bug\n" +
                        "_-_ 哨卫受到伤害后不死亡的bug\n" +
                        "_-_ 幽灵不受到伤害的bug\n" +
                        "_-_ 拉莱耶文本反弹伤害触发时没有特效且玩家身上跳字的bug\n" +
                        "_-_ 月华NPC开局交换武器可能出顽疾刻印的bug\n" +
                        "_-_ 遗忘碎片存在时砥砺锋芒天赋不生效的bug\n" +
                        "_-_ 重型回旋镖攻击后退出重进只需一回合飞回的bug\n" +
                        "_-_ 牧师惩戒之力施法到造成伤害需20多秒的bug\n" +
                        "_-_ 天狗锁血没有生效的bug\n" +
                        "_-_ 天狗二阶段没有正确切换场景的bug\n" +
                        "_-_ 轮椅0充能时无法用弹射起步开轮椅的bug\n" +
                        "_-_ 战士受衅怒火在破盾后下一次受攻击才错误触发的bug"));
    }


    public static void add_v08_1_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.COMPASS), "战争迷雾寻路改为设置项",
                ("_-_ 战争迷雾寻路改为设置里的可选项，点击后立即前往目标位置。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "破灭之戒调整",
                ("_-_ 破灭之戒的伤害现在会让中立生物转为敌对并开始攻击玩家")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "交互改进",
                ("_-_ 现在长按凳子可以触发右键效果，从而踢到凳子。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight(CharSprite.NEGATIVE);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_ 修复武器掌握对投掷武器生效的bug\n" +
                        "_-_ 修复武器掌握的武器切换时没有清除红色头带的bug\n" +
                        "_-_ 修复吃半个食物消耗3回合的bug\n" +
                        "_-_ 修复节制一餐天赋触发时第一次吃食物没有触发食物特殊效果的bug\n" +
                        "_-_ 修复伤害系统没有正确结算导致能电死NPC的bug\n" +
                        "_-_ 修复神圣泉水转化泉水异常闪退的bug\n" +
                        "_-_ 修复月华浮空贴图错误的bug\n" +
                        "_-_ 修复薪王形态的贴图bug\n" +
                        "_-_ 修复几个天赋的文本bug\n" +
                        "_-_ 修复种子分析无法使用的bug\n" +
                        "_-_ 移除皮甲遗留的事件订阅代码\n" +
                        "_-_ 修复震爆法杖的爆炸源最多只有1个的bug\n" +
                        "_-_ 拉莱耶文本反弹伤害触发时现在会显示诅咒特效，且玩家头上不会跳字\n" +
                        "_-_ 修复苔藓层2区没有生成赐福卷轴房间的bug\n" +
                        "_-_ 修复prison_quest相关异常\n" +
                        "_-_ 修复手术复活的boss异常\n" +
                        "_-_ 修复手机版因不能右键而踢不到凳子的bug\n" +
                        "_-_ 修复月华牧师商店卖的背包异常的bug\n" +
                        "_-_ 修复子层钥匙和主层钥匙通用的bug\n" +
                        "_-_ 修复轮椅狂飙状态死亡会崩溃的bug\n"));
    }

    public static void add_v08_0_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.MOONLIGHT, 1), "新英雄：月华",
                ("月华作为全新的可操作英雄正式加入地牢！\n\n" +
                        "_-_ 月华初始携带多种英雄武器，切换武器不消耗回合\n" +
                        "_-_ 携带经过改装的轮椅，可以短距跳跃并获得爆发加速\n" +
                        "_-_ 拥有小骑士、骰子法师和十手冠军三条专属分支\n" +
                        "_-_ 加入完整的专属天赋、护甲技能、英雄选择界面和永久解锁流程\n" +
                        "_-_ 选择月华时，1层的月华NPC会由猫权主义者的猫代班\n")));

        changes.addButton(new ChangeButton(new TalentIcon(Talent.LEARN_BLAZE), "骰子法师",
                ("来自Slice & Dice的异世界法术体系已经完整实装！\n\n" +
                        "_-_ 使用法杖积攒魔力点，也可以在炼金台用能量兑换魔力\n" +
                        "_-_ 可以学习并施放切割、愈合、烈焰、重压、瘴气、烈酒、操作和抚慰等法术\n" +
                        "_-_ 拥有专属的暗色骰面法术窗口、击杀特效和信息面板\n" +
                        "_-_ 背包、怪物与物品介绍、选项和消息窗口采用Slice & Dice风格\n" +
                        "_-_ 使用专属像素字体、攻击音效、法术音效、死亡音效和背景音乐\n")));

        changes.addButton(new ChangeButton(new ItemSprite(ARMOR_MOONLIGHT), "玩具背包",
                ("月华的玩具背包护甲技能已经完成。发动能力可以生成玩具并附着到护甲，为冒险构筑截然不同的效果组合。\n\n" +
                        "_-_ 实装诗、伤疤、铁心、箭矢、树肤、斗篷、水银、酊剂等基础玩具\n" +
                        "_-_ 追加30种具有阶级和独特机制的进阶玩具\n" +
                        "_-_ 玩具涵盖伤害、射程、投掷物、治疗、护盾、处决、保命与资源强化\n" +
                        "_-_ 玩具槽位已满时可以选择替换，信息窗口会显示全部已附着效果\n" +
                        "_-_ 玩具“诗”会依据中文名称押韵触发最终伤害加成\n")));

        changes.addButton(new ChangeButton(new ItemSprite(SNAKE_BITED_YENDOR), "蛇咬挑战",
                ("全新的彩蛋挑战：蛇咬！\n\n" +
                        "_-_ 开启挑战后，地牢中的物品和大量视觉表现都会变成蛇\n" +
                        "_-_ 挑战拥有专属交互、结局判定和稀有成就\n" +
                        "_-_ 具体规则可以在挑战界面中查看\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "全新戒指",
                ("地牢中新增两枚机制独特的戒指，所有戒指也获得了对应的宝石外观。\n\n" +
                        "_-_ 破灭之戒：持续消耗自身生命，对视野内敌人造成基于最大生命值的伤害\n" +
                        "_-_ 挤压之戒：强化击退距离和碰撞伤害\n" +
                        "_-_ 两枚新戒指已经加入正常生成、商店、奖励与嬗变池\n")));

        changes.addButton(new ChangeButton(new ItemSprite(YAMATO), "彩蛋武器：阎魔刀",
                ("“I am the……”\n\n" +
                        "_-_ 新增不会在地牢中随机生成的5阶彩蛋武器阎魔刀\n" +
                        "_-_ 阎魔刀攻击极快，攻击满血敌人时有概率令其麻痹\n" +
                        "_-_ 连续命中两次后可以发动不消耗回合的次元斩或疾走居合\n" +
                        "_-_ 在Yendor护符层寻找白色塑料椅，长按并选择“踹一脚”即可获得\n" +
                        "_-_ 生成的阎魔刀保留正常随机附魔与诅咒，并额外提升1级\n")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), "旧存档不兼容",
                ("v0.8.0包含大规模底层代码和资源结构升级，旧版本存档已不再兼容。\n\n" +
                        "请在更新前完成仍在进行的冒险，并在新版本中创建新的存档。\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "底层系统大规模重构",
                ("这个新版本重置了非常多原版破碎像素地牢的代码不规范和难以扩展的系统，所以这个版本更新如此慢带来的就是今后的超快速内容更新。好日子即将来临！\n\n" +
                        "_-_ 新增统一事件系统，为移动、拾取、使用物品、升级、死亡和切层等行为提供扩展入口\n" +
                        "_-_ 重构伤害类型系统，统一物理、法术、真实和特殊伤害的处理\n" +
                        "_-_ 全面重写支线系统，移除依赖具体楼层的硬编码实现\n" +
                        "_-_ 大幅调整着色器和渲染机制，为Slice & Dice效果及后续特效提供基础\n" +
                        "_-_ 重构物品、天赋和状态贴图管线，原始图片可以通过清单自动打包\n" +
                        "_-_ 缺失物品贴图时会显示对应资源ID，方便开发和定位问题\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "职业与机制调整",
                ("_-_ 重做多个职业的T4天赋，包括高端饮食、武器大师、老魔杖闪避、腐化怨灵、通识射击、药水涂镖、草地视野、光之永恒、月之辉煌、盾戳战术、骑士精神、法术强效和鸡蛋篮子\n" +
                        "_-_ 新星法杖重写为更稳定、易扩展的使用逻辑\n" +
                        "_-_ 种子查找器支持搜索饰品，并改为后台线程执行，超时后会自动停止\n" +
                        "_-_ 优化近距离战争迷雾绕路，绕远时会先询问玩家\n" +
                        "_-_ 英雄选择界面完成迭代，并加入永久物品与英雄解锁逻辑\n" +
                        "_-_ 精铁淬炼返还升级卷轴改为有等级的十手损坏时触发\n" +
                        "_-_ 钥匙栏仅在上一层仍有未使用钥匙时显示空钥匙提示\n" +
                        "_-_ 部分装备等级计算统一使用实际增益等级\n")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),
                Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 电脑版调试时间挑战中背包按钮无法全部显示的bug\n" +
                        "_-_ 修复 苔藓层仍会生成跳楼房的bug\n" +
                        "_-_ 修复 追寻附魔标记不会在攻击者或目标死亡后消失的bug\n" +
                        "_-_ 修复 诅咒的狂怒戒指导致攻击速度异常缓慢的bug\n" +
                        "_-_ 修复 集群骷髅生成的骷髅可能与玩家重叠的bug\n" +
                        "_-_ 修复 前线旗会在怪物尚未看见英雄时将其唤醒的bug\n" +
                        "_-_ 修复 宽恕裁决在视野内有多个目标时无法正确选择目标的bug\n" +
                        "_-_ 修复 回音锤击杀敌人的一击不显示伤害的bug\n" +
                        "_-_ 修复 恶魔领主能够穿过其他角色攻击玩家的bug\n" +
                        "_-_ 修复 卸下翼剑后仍会触发攻击特效并可能导致闪退的bug\n" +
                        "_-_ 修复 复合弩投射物飞行动画错误旋转的bug\n" +
                        "_-_ 修复 全局暴击率未显示在英雄属性面板的bug\n" +
                        "_-_ 修复 种子查找器搜索不存在物品时可能卡死的bug\n" +
                        "_-_ 修复 震爆法杖无法获得塑形玻璃增益且无法命中伪装宝箱怪的bug\n" +
                        "_-_ 修复 塑形玻璃与国王之戒的虚拟等级无法叠加的bug\n" +
                        "_-_ 修复 末日守卫公式错误导致护盾量异常增加的bug\n" +
                        "_-_ 修复 背包已满时转化十手会掉落原武器并错误返还升级卷轴的bug\n" +
                        "_-_ 修复 天狗出口层部分贴图显示错误的bug\n" +
                        "_-_ 部分文案补充\n")));
    }

}
