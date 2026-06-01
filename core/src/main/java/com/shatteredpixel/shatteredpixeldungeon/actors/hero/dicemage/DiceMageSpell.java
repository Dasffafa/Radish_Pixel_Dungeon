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
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 骰子法师法术基类
 * 所有法术继承自此类，实现具体的释放逻辑
 */
public abstract class DiceMageSpell {

    /**
     * 获取法术名称key（用于Messages.get）
     */
    public abstract String nameKey();

    /**
     * 获取消耗的魔力点数
     */
    public abstract int mpCost();

    /**
     * 检查是否可以释放法术
     */
    public boolean canCast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return false;

        MagicPoint mp = hero.buff(MagicPoint.class);
        return mp != null && mp.getIntPoints() >= mpCost();
    }

    /**
     * 释放法术的入口方法
     * 在UI按钮点击时调用
     */
    public void cast() {
        Hero hero = Dungeon.hero;
        if (hero == null) return;

        // 检查魔力是否足够
        if (!canCast()) {
            GLog.w(Messages.get(DiceMageSpell.class, "no_mp"));
            return;
        }

        // 扣除魔力
        MagicPoint mp = hero.buff(MagicPoint.class);
        if (mp != null) {
            mp.spendPoints(mpCost());
        }

        // 执行具体的法术逻辑
        onCast(hero);
    }

    /**
     * 子类必须实现的具体法术逻辑
     */
    protected abstract void onCast(Hero hero);

    /**
     * 获取一个目标格（需要玩家选择目标时使用）
     * @param listener 选择完成后的回调
     */
    protected void getTarget(CellSelector.Listener listener) {
        GameScene.selectCell(listener);
    }

    /**
     * 检查目标是否合法（例如：是否为敌人）
     */
    protected boolean isValidTarget(Char target) {
        return target != null && target.alignment == Char.Alignment.ENEMY;
    }
}