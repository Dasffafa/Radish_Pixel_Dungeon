/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 骰子法师法术基类。
 *
 * 每个法术可隶属于一个"学派"(school)，并占据该学派的某一等级位(level)。
 * 当学派投入 N 点天赋时，只允许使用该学派列表中的第 N 个法术（前序法术不可用）。
 * 被动技能(isPassive)不消耗魔力点、不可主动施放。
 */
public abstract class DiceMageSpell {

    public String name() {
        return Messages.get(getClass(), "name");
    }

    public String desc() {
        return Messages.get(getClass(), "desc");
    }

    /**
     * 所属学派。返回 null 表示不隶属任何学派（固定初始法术，如"迸发"）。
     */
    public Talent school() {
        return null;
    }

    /**
     * 在该学派列表中的等级位(1..3)。0 表示不适用。
     */
    public int level() {
        return 0;
    }

    /**
     * 是否为被动技能（不可施放）。
     */
    public boolean isPassive() {
        return false;
    }

    public abstract int mpCost();

    public Talent iconTalent() {
        Talent s = school();
        if (s != null) return s;
        return null;
    }

    /**
     * 该法术在 SND atlas（snd/atlas_image.png）中对应的法术图标名。
     * 返回 null 表示不使用 SND 贴图。
     */
    public String sndImageName() {
        return null;
    }

    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        if (isPassive()) return false;

        Talent s = school();
        if (s != null && hero.pointsInTalent(s) != level()) {
            // 学派等级决定可用的第 N 个法术；其余不可用
            return false;
        }

        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return false;

        if (mp.offCooldown(getClass()) == false) return false;

        // 魔力点足够，或背包法杖充能可覆盖本次消耗，均可施放
        return mp.getIntPoints() >= mpCost() || mp.canAfford(mpCost());
    }

    public void cast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;

        if (!canCast()) {
            GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
            return;
        }

        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null && mp.getIntPoints() < mpCost() && mp.canAfford(mpCost())) {
            // 魔力不足但法杖充能足够：弹窗询问是否消耗法杖充能
            GameScene.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndDiceMageWandDrain(mp, mpCost(), this));
            return;
        }

        onCast(hero);
    }

    /** 法杖充能消耗确认后，继续本次施法（魔力已被补充到足够）。 */
    public void continueCast(Hero hero) {
        if (hero == null) return;
        if (!mpCheck()) return;
        onCast(hero);
    }

    private boolean mpCheck() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp == null) return false;
        if (!mp.offCooldown(getClass())) return false;
        return mp.getIntPoints() >= mpCost();
    }

    protected boolean spendMagic(Hero hero) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null && mp.spendPoints(mpCost())) {
            return true;
        }
        GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
        return false;
    }

    /**
     * 施法后为当前法术设置冷却回合数。
     */
    protected void startCooldown(Hero hero, float turns) {
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null) mp.setCooldown(getClass(), turns);
    }

    protected abstract void onCast(Hero hero);

    protected void getTarget(CellSelector.Listener listener) {
        GameScene.selectCell(listener);
    }

    protected boolean isValidEnemy(Char target) {
        return target != null && target.alignment == Char.Alignment.ENEMY;
    }

    protected boolean isValidAlly(Char target) {
        return target != null && target.alignment == Char.Alignment.ALLY;
    }
}
