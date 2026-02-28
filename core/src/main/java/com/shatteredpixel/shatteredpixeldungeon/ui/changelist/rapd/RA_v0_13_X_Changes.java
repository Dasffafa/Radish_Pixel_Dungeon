package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FrogSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollShamanKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KingSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GiantWormSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatKingSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ZikkSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class RA_v0_13_X_Changes {

    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
        add_v06_9_Changes(changeInfos);
        add_v06_8_Changes(changeInfos);
        add_v06_4_Changes(changeInfos);
        add_v06_3_Changes(changeInfos);
        add_v06_2_Changes(changeInfos);
        add_v06_1_Changes(changeInfos);
        add_v06_0_Changes(changeInfos);
        add_v05_8_Changes(changeInfos);
        add_v05_7_Changes(changeInfos);
        add_v05_6_Changes(changeInfos);
        add_v05_5_Changes(changeInfos);
        add_v05_0_Changes(changeInfos);
        add_v04_2_Changes(changeInfos);
        add_v04_1_Changes(changeInfos);
        add_v03_X_Changes(changeInfos);
        add_v03_9_Changes(changeInfos);
        add_v03_8_Changes(changeInfos);
        add_v03_7_Changes(changeInfos);
        add_v03_6_Changes(changeInfos);
        add_v03_5_Changes(changeInfos);
        add_v03_4_Changes(changeInfos);
        add_v03_3_Changes(changeInfos);
        add_v03_2_Changes(changeInfos);
        add_v03_1_Changes(changeInfos);
    }

    public static void add_v06_9_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new GnollKingSprite()), "新Boss：豺狼大酋长",
                "15层B面Boss，击败获得1500金币，必定掉落一个升级卷轴。\n\n注意：此为双Boss，因此必须全部击败后才会掉落奖励。"));

        changes.addButton( new ChangeButton((new GnollShamanKingSprite()), "新Boss：豺狼大祭司",
                "15层B面Boss，击败获得1500金币，必定掉落一个升级卷轴。\n\n注意：此为双Boss，因此必须全部击败后才会掉落奖励。"));

        changes.addButton( new ChangeButton((new FrogSprite()), "新敌人：青蛙",
                "栖息于苔藓洞穴，其危险性比啮齿小鼠更大。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISTANT_WELL), ("新地形：苔藓洞穴"),
                ("在一区2层必定生成，一个迷你副本，里面不会生成力量药水和升级卷轴，但据说有一个较为珍贵的宝藏在这里……")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.BLESS_SCROLL)), "新物品：赐福卷轴",
                "可为你提供护甲/武器临时+1升级，击败核心Boss后自动失效。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 6), ("牧师护甲天赋全面实装"),
                (       "牧师三大天赋技能：终末奇迹，暗影咒文，凡体受神全面实装，欢迎各位游玩。")));



        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复 隐没于人天赋不生效\n" +
                        "_-_ 修复 激素涌动攻速异常\n" +
                        "_-_ 修复 上一个版本的相关游戏崩溃问题")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("杂项修改"),
                ("1.现在Boss血条支持多血条，至多支持4个\n" +
                        "2.现在子层跳楼将自动返回到入口处\n" +
                        "3.部分素材优化迭代")));


    }


    public static void add_v06_8_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.DARTS+21)), "新武器：改锥",
                "4阶，力量需求16\n" +
                        "初始2-10，成长1-2，攻速0.4\n" +
                        "一对锋利的锥子，可以捅向敌人的伤患处，越来越深。\n\n在上回合每造成一次物理伤害，此武器的伤害就越致命。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.WAND_NEWSTAR)), "新法杖：新星法杖",
                "使用这根法杖可以对自己或者友军使用，随后以那个位置为中心，3*3圆形范围内的所有敌人受到2+等级-5+等级*4点伤害，友军获得法杖等级的护盾。等级每提升4级就会使范围扩大一圈。\n" +
                        "\n" +
                        "战法特效为使用新星法杖进行近战攻击时，有概率触发_新星治疗_，它会将_老魔杖_和_所有法杖_的总等级综合，并迅速回馈给自己的主人。\n\n" +
                        "新星法杖的元素风暴战技：没有特殊效果，但是它的伤害倍率是元素风暴基础伤害的2倍。\n\n" +
                        "谁知道制作这根法杖的家伙和牧师做了什么交易，但是不用祈祷就能使用的神圣法术可是许多人梦寐以求的，nova！"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.WAND_BOMBWAVES)), "新法杖：新星法杖",
                "使用这根法杖会先指定一处位置，在下回合以那个位置为中心，5×5圆形范围内的所有敌人受到3+等级-10+等级*4点伤害。并击退范围内的友军2格。\n" +
                        "\n" +
                        "战法特效为有1+等级/4+等级的概率向目标脚下放置一个爆炸源。\n\n" +
                        "震爆法杖的元素风暴战技：将会在英雄脚下生成一个威力十分巨大的震爆范围，此效果远超直接释放。\n\n" +
                        "此类法杖延迟生效和范围过大的特点，一直被魔法学会列为缺点，直到如今也没有人能替它翻案。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.MAGNETIC_CROWN)), "新神器：磁力王冠",
                "带上这顶王冠时，你感受到了空间中微弱的磁力，这种力量也许能把你和其他生物拖向某一地点……\n\n" +
                        "升级方式：在陷阱地块上获得经验。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.LIGHT_KING)), "新饰品：光明之冠",
                "你能感受得到这枚闪烁的皇冠中蕴藏着力量，不过需要以你良好的状态诱发之。\n" +
                        "\n" +
                        "在当前的等级下，当你的当前生命值大于或等于最大生命值的90%/85%/80%/75%时，这件饰物会使你造成的所有伤害增加25%/33%/41%/50%，反之则降低如上值。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.RIVER_GLASS)), "新饰品：塑形玻璃",
                "这件饰品有很强的延展性，你能感受到它给你的所有装备都镀了一层性能近似但更柔软的膜。\n" +
                        "\n" +
                        "在当前的等级下，这件饰物每级会使所有可被升级的装备获得1级虚拟升级，但也会使装备在发挥效用时多进行1次判定并取其中最小值结算。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 6), ("牧师护甲第一天赋实装"),
                (       "牧师首个护甲技能---终末圣祷，现已正式实装！")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复 嬗变饰品萝卜有概率会闪退\n" +
                        "_-_ 修复 天赋大地之心+1的加速未生效，+2 踩在草上就有露珠，并且扔下水袋后，踩不了草游戏异常\n" +
                        "_-_ 修复 钢铁烈阳会使自己释放的法术神罚不给护盾\n" +
                        "_-_ 修复 巨人杀手的暴击增益对精英强敌的精英无效\n" +
                        "_-_ 修复 幻影雾剑在攻击时未击中敌人时自身隐形\n" +
                        "_-_ 修复 使用回音锤攻击巨型精英时未击杀也可以产生特效\n" +
                        "_-_ 修复 狂战士怒气获取问题\n" +
                        "_-_ 修复 部分测试工具异常\n" +
                        "_-_ 修复 新星法杖的一堆问题\n" +
                        "_-_ 修复 护甲部分渲染素材异常")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.STONE_DISARM)), "符石重做：探测符石",
                "原先为拆除符石，现在二合一。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("主界面优化"),
                ("现在 日志 界面，可在游戏主界面打开。\n" +
                        "升级 界面 迭代新版")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), ("网络协议迭代"),
                ("从0.6.7-FD开始，迭代网络协议，重启游戏内部自动更新")));
    }

    public static void add_v06_4_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.DARTS+19)), "新武器：白帝圣剑",
                "三阶，力量需求14\n" +
                        "初始3-16，成长2-3\n" +
                        "在每位敌人首次出现在你视野中时，立刻对其造成一次相当于攻击力60%+10%*等级的伤害。\n" +
                        "御剑跟着我！\n\n现在可以被磨重或减轻效果影响。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复星界沟通和神赐之礼生效异常问题\n" +
                        "_-_ 修复复仇怒号未正确显示的问题\n" +
                        "_-_ 修复战斗牧师的极效疗愈会在处于冷却时错误的触发并增加冷却时间，并且会被护甲格挡的零伤害触发\n" +
                        "_-_ 现在治疗飞镖和治疗炸弹会被挑战-【伤痛难愈】-的1/5效率影响\n" +
                        "_-_ 修复光能灌注给的临时生命值少了八点")));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LENGDS_PAGE), "育言故事正式回归",
                "育言故事回归，在探索地牢时阅读一些睡前小故事！"));
    }

    public static void add_v06_3_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.DARTS+18)), "新武器：暗影之刃",
                "二阶，力量需求12\n" +
                        "初始2-12，成长1-2，精准1.2\n" +
                        "视野内的每位敌人都会为这把武器提供20%+5%*等级的攻击速度。\n" +
                        "敌人越多，这把剑的思绪也就越多。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.DARTS+19)), "新武器：白帝圣剑",
                "三阶，力量需求14\n" +
                        "初始3-16，成长2-3\n" +
                        "在每位敌人首次出现在你视野中时，立刻对其造成一次相当于攻击力60%+10%*等级的伤害。\n" +
                        "御剑跟着我！"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师恶魔天赋实装"),
                (       "除执行者恶魔天赋尚未完成，其他均已实装。")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.RADISH)), "新饰品：萝卜",
                "一株不应该在炼金锅里存在的蔬菜，似乎是整个地牢的精神象征，冥冥之中有人这么告诉你。\n" +
                        "萝卜地牢怎么能没有萝卜呢？\n" +
                        "在当前等级下，这件饰物会为你提供5%/10%/15%/20%的全局暴击率。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.GOLD_RADISH)), "新饰品：黄金萝卜",
                "这枚神秘纪念品闪烁着灿金色的光辉，并非暗金那种货色可比。你一定是把探索的运气全用到这上面了才能获得它。\n" +
                        "这件饰品的获取概率是其他饰品的1/10\n" +
                        "在当前等级下，这件饰物会使你所装备的非神器非传承装备等级固定为+1/+2/+3/+4。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复执行者的经验灌注天赋没有触发\n" +
                        "_-_ 修复战斗牧师的物理祈祷天赋有问题，投掷武器和复合弩的射击都能触发\n" +
                        "_-_ 修复圣地会把上下楼梯覆盖掉")));

        changes.addButton( new ChangeButton((new Image(new KingSprite())), "矮人国王调整",
                "如通过牧师击败矮人国王，直接掉落强化天赋书。\n\n牧师护甲技能还未完成，所以先用着恶魔4阶强化天赋。"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LENGDS_PAGE), "育言故事回归",
                "育言故事回归，在探索地牢时阅读一些睡前小故事！"));
    }

    public static void add_v06_2_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师转职-执行者"),
                (       "新增红衣主教T3天赋 和 转职后的效果，欢迎各位尝鲜\n\n" +
                        "代行神权，灵活信仰【公用天赋】\n\n" +
                        "战斗牧师专属天赋：黑暗奉献，殉道之力，经验灌注")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.APOWER)), "新技能：宽恕裁决",
                "立刻击杀攻击范围内一名_生命值低于60%的敌人_。\\n\\n这个技能需要消耗_4点信仰值_。\\n\\n释放失败不会扣减信仰值。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.BACKMESSAGE)), "新技能：背信弃义",
                "指定视野范围内的一点，其_3*3范围_内的所有生物获得初始值为_楼层数+3的流血_。\\n\\n这个技能需要消耗_12点信仰值_。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复牧师额外惩戒不消耗信仰的异常\n" +
                        "_-_ 修复一些小的崩溃异常\n" +
                        "_-_ 修复0层会掉饥饿的异常")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("现在电脑端所有界面都已支持新布局")));
    }

    public static void add_v06_1_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师转职-红衣主教"),
                (       "新增红衣主教T3天赋 和 转职后的效果，欢迎各位尝鲜\n\n" +
                        "代行神权，灵活信仰【公用天赋】\n\n" +
                        "战斗牧师专属天赋：圣火燎原，圣光洗礼，通天圣塔")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.HOLYFIRE)), "新技能：圣火审判",
                "指定一个地格，在其3×3区域生成圣火场，火场内的所有生物受到的伤害增加1.3倍且沾染上圣火。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.HOLYLAND)), "新技能：圣地领域",
                "指定一个地格，并以其为中心生成一片5*5圆形的圣地区域。" +
                        "\n\n在圣地区域内的非英雄非飞行角色都会减少33%移速，对亡灵与恶魔类怪物则是减少50%移速，并且每回合它们受到区域数点伤害。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复牧师额外惩戒不消耗信仰的异常\n" +
                        "_-_ 修复一些小的崩溃异常\n" +
                        "_-_ 修复0层会掉饥饿的异常")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("现在电脑端所有界面都已支持新布局")));
    }

    public static void add_v06_0_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.6.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师转职-战斗牧师"),
                (       "新增战斗牧师T3天赋 和 转职后的效果，欢迎各位尝鲜\n\n" +
                        "代行神权，灵活信仰【公用天赋】\n\n" +
                        "战斗牧师专属天赋：钢铁烈阳，物理祈祷，极效疗愈")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.CORRECT)), "技能Plus：神罚时刻",
                "战斗牧师的惩戒伤害会增加50%并会额外指定一个视野内的随机目标。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.BLESS)), "新技能：圣光之耀",
                "转职后自动替换_虔诚祈祷_，获得此技能。\n\n" +
                        "效果：获得25%伤害加成 + 25%的伤害减免，持续60回合。"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.LIGHTIMUEE)), "技能Plus：光明领域",
                "战斗牧师的光能灌注的效果变更至获得区域数*12点临时生命与区域数*4回合激素涌动，其他不变。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("UI优化"),
                ("现在牧师的临时血条可在血条上显示出来")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                (       "_-_ 修复牧师临时血条可以抵挡超出伤害的异常\n" +
                        "_-_ 灵魂干涉优化")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("现在电脑端所有界面都已支持新布局")));
    }

    public static void add_v05_8_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.5.8-9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), ("异常调试日志"),
                ("在游戏卡死时，通过此系统可有效进行反馈。")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.WAND_GNOLL)), "新法杖：豺狼法杖",
                "0.5.8--由彦木作者进行联动。\n\n" +
                        "0.5.9--修复了卡死异常"));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.SPOTOA)), "新饰品：发芽土豆",
                "0.5.8--由彦木作者进行联动。\n\n" +
                        "0.5.9--部分效果缺失修正"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师相关Bug批量修正"),
                (       "_-_ 修复暴击无法触发恩惠之雨与灵魂干涉\n" +
                        "_-_ 绝望祷言相关问题已修复\n" +
                        "_-_ 光能灌注相关问题已修复\n" +
                        "_-_ 祝福一餐相关问题已修复\n" +
                        "_-_ 惩戒伤害相关问题已修复\n" +
                        "_-_ 现在投掷武器不会触发灵魂干涉\n" +
                        "_-_ 现在灵魂干涉不再对精英怪生效，并且T2生效为低于当前最大生命的二分之一\n" +
                        "_-_ 将神圣护体的天赋数值下调为2/3回合，而非2/5回合\n" +
                        "_-_ 将灵魂干涉的生效条件改为生命值小于等于2/4")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("主背包数量调整为25，其他背包格子数量调整为24")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ( "_-_V0.5.8\n" +
                        "_-_ 修复惩戒伤害幂运算的异常\n" +
                        "_-_ 修复商品出售和购买价格不一致的异常\n" +
                        "_-_ 牧师的信仰值现在和最大经验挂钩\n" +
                        "_-_ 修正一些小崩溃异常\n"+
                        "_-_V0.5.7\n" +
                        "_-_ 修复牧师拾取天狗面具闪退的异常\n"+
                        "_-_ 修复上个版本FireBase报告的崩溃异常")));
    }

    public static void add_v05_7_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.5.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new ZikkSprite()), "新Boss：大蛇兹克",
                "有极小概率替换粘咕，更加狡猾，但战利品也更加丰厚。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师1-2阶开放测试"),
                ("牧师基础技能和1/2阶天赋已经可用，欢迎各位测试！\n\n" +
                        "天赋T1：餐前祈祷，心灵感应，恩惠之雨，虔诚祷告\n" +
                        "天赋T2：祝福一餐，灵魂干涉，光辉灌注，神圣护体，绝望祷言\n")));

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.DARTS+17)), "传承武器测试",
                "可在测试时间中进行测试，后续将会渐渐正式上线到正常游玩中。"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("其他调整"),
                ("1.一层刷怪数量-1\n" +
                        "2.调整伤痛难愈从1/10=>1/5\n" +
                        "3.修复弱点洞悉boss问题及描述\n" +
                        "4.每层有33%的概率额外一个食物，如果开启没入黑暗挑战，25%的概率额外一个火把\n" +
                        "5.部分生成器赘余破碎武器完全移除\n" +
                        "6.修复部分文案异常\n" +
                        "7.游戏检测更新的接口迭代")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.5.6\n" +
                        "_-_ 修复房间生成异常问题\n" +
                        "_-_ 修复怪物图鉴数据保存异常问题\n" +
                        "_-_ 修复0层可无限上楼的问题\n" +
                        "_-_ 修复祝福之戒未完全生效的问题\n"+
                        "_-_ 修复末日守卫导致法师天赋_储存护盾_失效的异常"));
    }

    public static void add_v05_6_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.5.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.SHADOW_BOOK)), "幻影之书效果优化",
                "你每阅读一次卷轴，就在你周围生成1+0.2*武器等级个镜像（向下取整）"));

        Image critImage = new Image(Assets.Effects.TEXT_ICONS,56,7,7,7);
        critImage.scale.set(PixelScene.align(1.72f));
        changes.addButton(new ChangeButton(critImage, ("暴击视觉效果调整"),
                ("暴击图标现在包含穿甲暴伤图标（白色）")));
    }

    public static void add_v05_5_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.5.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.RECTOR, 5), ("牧师预载"),
                ("牧师开始制作，目前已经预载")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton((new ItemSprite(ItemSpriteSheet.TAIKIG)), "太刀效果优化",
                "大太刀效果优化\n" +
                        "修复大太刀被缴械陷阱传送走冷静和必定暴击的buff仍在"));

        changes.addButton(new ChangeButton(Icons.get(Icons.GOLD), ("其他调整"),
                ("1.每区平均房间数增加1/1/2/2/2，物品生成数量增加15%，每层食物生成数量额外增加0.33机率\n" +
                        "2.挑战弱点洞悉的最低伤害上升至1/3/6/10/15\n" +
                        "3.更改地龙的贴图，让它在潜伏时更显眼。让地龙不会因为集群挑战而醒来")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.5.4\n" +
                        "_-_ 修复祝福之戒不生效的问题\n" +
                        "_-_ 完善稀有怪返程倍率 1.1/1.5/2.5/5/9"));
    }

    public static void add_v05_0_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.5.0-2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.HUNTRESS, 6), ("女猎恶魔天赋完全实装"),
                ("射技决斗 疾风骤雨 药镖专家 大地之心均已实装")));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.KILL_BOAT), "武器增强：斩舰刃",
                "成长从2-8改为2-10。"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RUNE_SLADE+1), "新武器：轮刃",
                "你的所有护甲值都会转化为此武器的攻击力。这件武器对目标周围的敌人造成溅射效果。\n\n笨重难用的武器。挥动它，几乎意味着放弃全身上下所有的防御手段。【穿戴后防御变为0】\n\n四阶，力量需求16，初始8-20，成长1-5\n\n开发组碎碎念：好像是骰杀里面武器改，不过咱也不知道啦，祝各位玩的开心。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.ROGUE, 6), ("盗贼恶魔天赋完全实装"),
                ("严阵以待 能量回收 动能转换 风暴奔袭均已实装")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new BuffIcon(BuffIndicator.DEGRADE, true), "降级增强",
                "修复武器/护甲降级不生效的问题，同时，在降级状态下，国王之戒的效果完全失效。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.GOLD), ("商店售卖调整"),
                ("每增加一级售卖的价格就增加30%，有任何附魔加20%，出售价格从之前的3倍改为2倍。")));

        Image critImage = new Image(Assets.Effects.TEXT_ICONS,49,7,7,7);
        critImage.scale.set(PixelScene.align(1.72f));
        changes.addButton(new ChangeButton(critImage, ("暴击视觉效果调整"),
                ("暴击现在不再显示为一个文本，而是一个图标\n\n图标灵感：某农暴击图标")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.5.2\n" +
                        "_-_ 修复武器攻速的严重异常\n" +
                        "_-_ 修复拉莱耶文本反伤异常\n" +
                        "_-_ 修复锁镰回合结算异常\n" +
                        "_-_ 修正轮刃文本显示异常",
                "_-_V0.5.1\n" +
                        "_-_ 修复始终暴击的异常\n" +
                        "_-_ 修复轮刃可以伤害自己的异常\n" +
                        "_-_ 优化决斗家的一些赘余代码\n" +
                        "_-_ 部分文案优化\n",
                "_-_V0.5.0\n" +
                        "_-_ 修复部分文案异常\n" +
                        "_-_ 修复藤蔓陷阱天赋失效\n" +
                        "_-_ 拉莱耶文本现在不会伤害英雄\n" +
                        "_-_ 修复巨型蠕虫特殊攻击效果失效\n" +
                        "_-_ 修复气动拳套未气动时仍然在说能量不足\n" +
                        "_-_ 修复装备武力之戒之后武器会出现决斗家的充能\n" +
                        "_-_ 修复在伤痛难愈挑战下，部分食物出现1血异常效果\n" +
                        "_-_ 移除育言故事\n" +
                        "_-_ 修复一堆异常" ));
    }

    public static void add_v04_2_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.9-R3->R6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.HUNTRESS, 8), ("女猎天赋：疾风骤雨"),
                ("疾风骤雨现在可以正常使用了！")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 8), ("战士天赋：精巧纹章"),
                ("修复 精巧纹章不生效 和 天赋2阶强化不生效")));

        changes.addButton(new ChangeButton(new Image(new RatKingSprite()), ("鼠王优化"),
                ("对于有恶魔之力的英雄，鼠王会有新的特殊对话。")));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_ELTIE7), "精英证章",
                "功能修正：精英证章充能异常和其他小问题"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_CONCEAL), "匿踪斗篷",
                "功能修正：匿踪斗篷充能异常修正"));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战重制：荒芜之地"),
                ("草本身也不是很能在地牢里长的多好……\n\n-在每区，有50%/60%/70%/80%/90%的草变为枯草，额外生成的草也遵循此规律。\n\n之前的荒芜之地挑战规则全部废弃")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.4.9-R6\n" +
                        "_-_ 修复英雄精英附魔特效异常问题\n" +
                        "_-_ 修复苦痛刻痕使用祝福的十字架死亡的异常\n" +
                        "_-_ 部分文案优化\n",
                "_-_V0.4.9-R5\n" +
                        "_-_ 修复顽疾诅咒的一些遗漏崩溃问题\n" +
                        "_-_ 修复重击附魔特效带来的无响应(ANR)异常\n" +
                        "_-_ 修复苦痛刻痕无法正常使用的问题\n" +
                        "_-_ 部分文案优化\n",
                "_-_V0.4.9-R4\n" +
                        "_-_ 修复战士1-1天赋，低于50%不生效异常\n" +
                        "_-_ 修复顽疾诅咒带来的各种严重闪退问题，并追加了特殊文本\n" +
                        "_-_ 修复因处理精巧纹章带来的各种底层异常，包括附魔符石&蜕变秘卷部分功能失效，以及导致符文剃刀失效\n" +
                        "_-_ 优化法师4-1恶魔天赋，使之兼容242破碎底层\n" +
                        "_-_ 移除十字弩\n" +
                        "_-_ 部分文案优化\n"+
                        "_-_ 法师天赋：充能强化部分效果异常或不生效修正\n",
                        "_-_V0.4.9-R3\n" +
                            "_-_ 修复法术序列实际效果与描述不符\n" +
                            "_-_ 修复藤蔓陷阱天赋失效\n" +
                            "_-_ 修复获得小恶魔的恶魔之力后回去见鼠王并没有特殊互动\n" +
                            "_-_ 修复奇迹树脂描述有问题，但实际效果没问题\n" +
                            "_-_ 修复匿踪斗篷不随使用而升级，修复精英证章无法充能和其他异常\n" +
                            "_-_ 修复仍有一些原版武器在生成池中未被删掉，但保留【十字弩】\n" +
                            "_-_  修复盗贼一层天赋的小干粮 和 矿洞任务文本缺失" ));
    }

    public static void add_v04_1_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.8->R2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), ("破碎 & 萝卜 UI"),
                ("现在萝卜地牢默认萝卜UI,如果不习惯可在界面设置中调回破碎经典界面。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 8), ("战士4层恶魔天赋"),
                ("战士恶魔天赋，完全实装，欢迎尝鲜")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.MAGE, 8), ("法师4层恶魔天赋"),
                ("除'缠怨恶灵'天赋禁用外，其他完全实装，欢迎尝鲜")));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SEED_CARD), "新初始物品：种子袋",
                "弥补开局关卡运营问题，可以自选一个种子，腐莓种除外，只能使用一次。"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_CRAD), "新初始物品：符石袋",
                "弥补开局关卡运营问题，可以自选一个符石，只能使用一次。"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);


        changes.addButton(new ChangeButton(new Image(new GiantWormSprite()), ("巨型蠕虫平衡调整"),
                ("巨型蠕虫的吸血现在固定为1。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 3), ("战士初始武器强化"),
                ("战士初始武器基准提升至2-10，成长基准为1-2。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.MAGE, 4), ("法师初始武器强化"),
                ("法师初始武器基准提升至1-8，成长为1-2。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.ROGUE, 5), ("盗贼初始武器强化"),
                ("盗贼初始武器基准提升至1-9，成长为1-2。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.HUNTRESS, 6), ("女猎初始武器强化"),
                ("女猎灵能弓箭伤害强化，从1-6提升到2-6基准数值。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战平衡：精英强敌"),
                ("精英怪生成概率由原先的1/8，调整为1/10")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "1.修复DM-175护盾异常问题\n" +
                        "2.修复部分天赋不生效的问题\n" +
                        "3.修复伤痛难愈部分挑战未生效的问题" ));
    }

    public static void add_v03_X_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LONG_STARK), "新武器：长棍",
                "正式实装此武器，"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.TAIKIG), "新武器：大太刀",
                "正式实装此武器"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.WONDROUS_RESIN), "奇迹树脂",
                "1.修复奇迹树脂不生效的问题\n" +
                        "2.同步诅咒法杖的破碎全新效果"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LENGDS_PAGE), "育言故事",
                "全新育言故事登场，在探索地牢时阅读一些野史！"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_AMETHYST), "T4天赋",
                "1.战士和法师的T4恶魔天赋已经实装\n" +
                        "2.部分天赋界面得到重制"));

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), ("新崩溃界面"),
                ("由Cold Mint制作的新崩溃界面实装，什么，你连这位都不知道？萝卜的内部更新服务器接口就是薄荷姐姐提供的哦")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), ("内部更新系统"),
                ("内部更新系统回归！")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战加强：伤痛难愈"),
                ("新效果：冻肉露珠/诅咒法杖的吸血都受伤痛难愈的影响变为1")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战修正：弱点洞悉"),
                ("修复全局伤害加成问题")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "1.少量文本缺失补充\n" +
                        "2.修复绝对闪避失效的问题\n" +
                        "3.修复闪避之戒数值加成异常的问题\n" +
                        "4.修复部分楼层贴图异常\n" +
                        "5.修复狂战士物理伤害不加怒气的问题\n" +
                        "6.修复法师天赋T4-短棍格斗1-3级不生效异常\n" +
                        "7.修复螃蟹护甲移速不生效的问题" ));
    }

    public static void add_v03_9_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_AMETHYST), "戒指调整",
                "1.狂怒之戒成长调整为每级固定20%\n" +
                           "2.神射之戒耐久从20%-->10%\n" +
                           "3.闪避之戒成长调整为-->20%"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RUNE_SLADE), "符文外刀",
                "修复附魔相关异常"));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战优化：弱点洞悉"),
                ("此挑战效果已正常。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战改动：伤痛难愈"),
                ("新效果：治疗药水及其制品的效果被削弱为原来的1/10")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "1.少量文本缺失补充\n" +
                            "2.修复末日守卫迁移后失效的问题\n" +
                            "3.修复蝎子巨弩迁移后失效的问题\n" +
                            "4.修复部分楼层贴图异常\n" +
                            "5.修复兵师直觉异常" ));
    }

    public static void add_v03_8_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RUNE_SLADE), "新武器：符文外刀",
                "这柄武器刀刃上的符文会将附魔力量巧妙的转化成更大的杀伤力。\n\n当你在附魔这件武器时，也会升级它。"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RUNE_SLADE,new ItemSprite.Glowing(0x00ff00)), ("全新附魔登场"),
                ("连击，追寻，附魔登场")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("新挑战：伤痛难愈"),
                ("替代 恐药异症，挑战详情请查阅挑战描述")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("新挑战：弱点洞悉"),
                ("替代 信念护体，挑战详情请查阅挑战描述")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("杂项调整"),
                (       "1.图鉴系统登场\n" +
                        "2.探险者日志登场\n" +
                        "3.新增混乱香炉，遗忘碎片饰品\n" +
                        "4.炼金釜可鉴定物品\n" +
                        "5.炸弹伤害全局提升50%\n" +
                        "6.重命名系统回归\n" +
                        "7.正式移除部分破碎武器\n" +
                        "8.上个版本的补偿系统移除")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.4.4:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复部分文案异常问题\n" +
                        "_-_ 1.修复部分闪退问题\n" +
                        "_-_ 2.修复部分房间贴图异常问题\n" +
                        "_-_ 3.修复回音锤的一个小Bug" ));
    }

    public static void add_v03_7_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.4.3:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复 能量胸甲闪退异常问题\n" +
                        "_-_ 1.修复 长棍存档异常问题\n" +
                        "_-_ 2.修复 DM175护盾丢失问题\n" +
                        "_-_ 3.修复 怪物数量全局+4问题，应为+2\n" +
                        "_-_ 4.修复 大太刀不能必定暴击的问题\n" +
                        "_-_ 5.修复 部分NPC素材异常问题\n" +
                        "_-_ 6.修复 锁镰的拉怪裂缝，不攻击，以及概率异常问题\n" +
                        "_-_ 7.部分文案优化迭代" ));

    }

    public static void add_v03_6_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LOCK_CHAIN), "新武器：锁镰",
                "三阶，力量需求14\n" +
                        "初始4-20，成长1-4，力量需求15\n" +
                        "有且仅有额外的攻击距离3，用远端攻击击中敌人后可以不消耗回合的将其拉近\n" +
                        "近战会交替使用流星锤或镰刀，用流星锤攻击有25%的概率使对手虚弱2+武器等级回合，用镰刀攻击则有25%的概率给予初始值为2+武器等级的流血\n" +
                        "你的下一次近战攻击会使用镰刀\n" +
                        "极其奇特的长链武器，战法灵活多变。但有时连续攻击只能顺着武器的势头走，并不受你自己控制"));

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.LONG_STARK), "新武器：长棍",
                "三阶，力量需求14\n" +
                        "三阶，力量需求14\n" +
                        "初始4-20，成长1-4\n" +
                        "此武器的命中值暴击率与攻击速度会随着闪避值的提升而提升。 \n" +
                        "（闪避每提升一点命中就提升一点，暴击率攻击速度提升1%）  \n" +
                        "随身而动，随心而行。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.STATS), ("全局优化"),
                ("每层的初始出怪数量增加4。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("恶魔之力天赋UI"),
                ("现在可以在英雄界面和开始游戏界面预览恶魔天赋")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_PORT), ("随机图层"),
                ("二次优化图层优化，如果不喜欢可在游戏设置里面启用“原始地图风格")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.4.0:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复 部分天赋异常问题\n" +
                        "_-_ 1.修复 部分素材效果异常问题\n" +
                        "_-_ 2.修复 上个版本的一些崩溃问题" ));
    }

    public static void add_v03_5_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.0-1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.PNEGLOVE_FIVE), "新武器：气动拳套",
                "在启动状态下的每次攻击需要消耗1炼金能量，此武器的伤害上升150%+10%*武器等级，" +
                        "\n\n" +
                        "攻击必定命中并会将敌人击退2+0.5*武器等级\n\n"+
                        "在启动状态下点击周围3*3范围内的非空地格时将会消耗1能量释放冲击波，（无伤害）击退范围内除自己外的单位2+0.5*武器等级"));

        changes.addButton(new ChangeButton(Icons.get(Icons.STATS), ("全局优化"),
                ("每层的初始出怪数量增加2，物品刷新率增加个10%，楼层大小增加15%。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("随机图层"),
                ("每层可以出现隐藏图块，但怪物不变。你也许能在新图块环境中有更好的作战积极性！")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("全新3大附魔登场"),
                ("汲能，狂热，重击 附魔登场")));

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), ("0层相关问题修复"),
                ("吞力量，升级，魔能触媒问题修正")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.4.0:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复 斩舰刃 和 两书的问题\n" +
                        "_-_ 1.移除决斗家\n" +
                        "_-_ 2.修复 上个版本的一些崩溃问题" ));
    }

    public static void add_v03_4_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.9-RC", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 8), ("战士改动：角斗士更新"),
                ("角斗士连携技大改")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DEPTH), ("0层回归"),
                ("萝卜地牢0层回归！")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.3.9:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复 恶魔天赋 重进失效的问题\n" +
                        "_-_ 1.修复角斗士连击技能面板全英文；你现在已有X块暗金矿英文；被魅惑后攻击怪物提示英文的文本丢失\n" +
                        "_-_ 2.修复 火印恶魔火印不可见但仍能触发 的异常\n" +
                        "_-_ 3.二次修复 蜂巢 武器的功能缺失异常\n" +
                        "_-_ 4.修复 恶魔领主 伤害异常问题\n" +
                        "_-_ 5.修复 刻印者 与 火印恶魔的素材异常问题\n" +
                        "_-_ 6.修复部分情况下，每次退回主菜单，鉴定天赋会退回升级" ));
    }

    public static void add_v03_3_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("界面更新"),
                ("过渡界面已迁移到2.5.0风格")));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "测试时间加强",
                "_-_ 追加属性生成器，更方便您的调试"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.3.8:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.优化萝卜更新记录的界面的显示问题\n" +
                        "_-_ 1.修复各种文本丢失的异常\n" +
                        "_-_ 2.修复 自然之覆 的功能异常\n" +
                        "_-_ 3.战术，法师，盗贼，女猎的T1-T3天赋迁移完成\n" +
                        "_-_ 4.修复 守卫者陷阱 崩溃异常\n" +
                        "_-_ 5.修复穿戴护甲必定崩溃游戏的异常\n" +
                        "_-_ 6.修复全局伤害翻倍异常\n" +
                        "_-_ 7.修复 蜂巢 武器的功能缺失异常\n" +
                        "_-_ 8.修复 战士 部分护甲状态下素材显示错误\n" +
                        "_-_ 9.修复 石像 给予Buff闪退的严重异常\n\n" +
                        "[修复者：Doge]：\n" +
                        "_-_ 10.修复 护盾 伤害失效异常\n" +
                        "_-_ 11.修复 地底亚龙 的各种异常\n" +
                        "_-_ 12.修复 Buff基类 的一些迁移迭代异常"));
    }

    public static void add_v03_2_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), ("重大更新"),
                ("萝卜现已更新底层到破碎V2.4.2版本，迁移者：JDSALing\n\n" +
                        "注意：迁移后可能还含有各种问题，请积极反馈！")));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "杂项生成器",
                "_-_ 同步 魔绫像素地牢的 生成器，让你的测试更加简单。"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n" +
                        "_来自于 v0.3.5-MD3:_\n" +
                        "_-_ 修复石像Buff状态异常问题\n" +
                        "_-_ 部分素材贴图校准\n" +
                        "_-_ 部分效果失效修正"));
    }


    public static void add_v03_1_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo("内部测试-MD3", false, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_EMERALD), "戒指生成",
                "内部测试1版：\n\n" +
                        "部分戒指的生成异常已经修正"));

        changes = new ChangeInfo("内部测试-MD2", false, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "1.迁移后的贴图定位错乱已经修正--Thanks(过去的事)\n\n" +
                        "2.部分界面的显示异常已经修正"));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n" +
                        "_来自于 v0.3.5-MD1:_\n" +
                        "_-_ 修复DM175脱战护盾异常问题\n" +
                        "_-_ 修复豺狼祭司的一些潜在问题\n" +
                        "_-_ 修复迁移后的导致的部分贴图错乱\n" +
                        "\n" +
                        "_来自于 v0.3.5-MD0:_\n" +
                        "_-_ 修复石像的调查文本异常\n" +
                        "_-_ 修复迁移后的护甲生成异常"));
    }

}
