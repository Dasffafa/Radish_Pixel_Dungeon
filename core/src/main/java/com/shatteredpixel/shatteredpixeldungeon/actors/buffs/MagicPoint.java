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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDiceMageSpells;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.HashMap;

/**
 * 骰子法师魔力点追踪Buff
 * 存储当前魔力点数，以及骰子法师法术的持久状态。
 */
public class MagicPoint extends Buff implements ActionIndicator.Action {

    {
        type = buffType.POSITIVE;
        revivePersists = true;
    }

    private float currentPoints = 0f;
    private int healValue = 50;
    private int refreshValue = 40;
    private int surgeryUses = 0;
    private int activeSurgerySummon = -1;
    private Class<? extends Mob> lastKilledMob;

    // 法术冷却：Class -> 剩余回合数
    private final HashMap<Class<? extends DiceMageSpell>, Float> cooldowns = new HashMap<>();
    // 特殊学派本局的三个法术（按等级 1/2/3 存储），null 表示未生成
    private Class<? extends DiceMageSpell>[] specialSpells = null;

    private static final String POINTS = "points";
    private static final String HEAL_VALUE = "heal_value";
    private static final String REFRESH_VALUE = "refresh_value";
    private static final String SURGERY_USES = "surgery_uses";
    private static final String LAST_KILLED_MOB = "last_killed_mob";
    private static final String COOLDOWNS = "spell_cooldowns";
    private static final String SPECIAL_SPELLS = "special_spells";
    private static final String ACTIVE_SURGERY_SUMMON = "active_surgery_summon";

    public static MagicPoint inst() {
        return Dungeon.hero != null ? Dungeon.hero.buff(MagicPoint.class) : null;
    }

    public float getPoints() {
        return currentPoints;
    }

    public int getIntPoints() {
        return (int) currentPoints;
    }

    public void addPoints(float amount) {
        currentPoints += amount;
        updateAction();
        BuffIndicator.refreshHero();
    }

    public boolean spendPoints(int amount) {
        if (currentPoints >= amount) {
            currentPoints -= amount;
            updateAction();
            BuffIndicator.refreshHero();
            return true;
        }
        return false;
    }

    public int healValue() {
        return healValue;
    }

    public int refreshValue() {
        return refreshValue;
    }

    public void decayHealValue() {
        healValue = Math.max(0, healValue - 3);
    }

    public void decayRefreshValue() {
        refreshValue = Math.max(0, refreshValue - 2);
    }

    public int surgeryCostIncrease(int talentPoints) {
        return talentPoints == 1 ? 3 : talentPoints == 2 ? 2 : 1;
    }

    public int surgeryCost(int talentPoints) {
        return 3 + surgeryUses * surgeryCostIncrease(talentPoints);
    }

    public void surgeryUsed() {
        surgeryUses++;
    }

    public void recordKill(Class<? extends Mob> mobClass) {
        lastKilledMob = mobClass;
    }

    public Class<? extends Mob> lastKilledMob() {
        return lastKilledMob;
    }

    public void clearLastKilledMob() {
        lastKilledMob = null;
    }

    public boolean offCooldown(Class<? extends DiceMageSpell> spellClass) {
        Float left = cooldowns.get(spellClass);
        return left == null || left <= 0f;
    }

    public void setCooldown(Class<? extends DiceMageSpell> spellClass, float turns) {
        if (turns <= 0) cooldowns.remove(spellClass);
        else cooldowns.put(spellClass, turns);
    }

    public float cooldownLeft(Class<? extends DiceMageSpell> spellClass) {
        Float left = cooldowns.get(spellClass);
        return left == null ? 0f : left;
    }

    public Class<? extends DiceMageSpell>[] specialSpells() {
        return specialSpells;
    }

    public void setSpecialSpells(Class<? extends DiceMageSpell>[] spells) {
        specialSpells = spells;
    }

    public int activeSurgerySummon() {
        return activeSurgerySummon;
    }

    public void setActiveSurgerySummon(int id) {
        activeSurgerySummon = id;
    }

    @Override
    public boolean act() {
        if (!cooldowns.isEmpty()) {
            cooldowns.entrySet().removeIf(e -> {
                float v = e.getValue() - 1f;
                if (v <= 0f) return true;
                e.setValue(v);
                return false;
            });
        }
        updateAction();
        spend(TICK);
        return true;
    }

    private void updateAction() {
        if (target instanceof Hero && ((Hero) target).subClass == HeroSubClass.DICE_MAGE) {
            if (ActionIndicator.action == null || ActionIndicator.action == this) {
                ActionIndicator.setAction(this);
            }
        } else {
            ActionIndicator.clearAction(this);
        }
    }

    @Override
    public void detach() {
        ActionIndicator.clearAction(this);
        super.detach();
    }

    @Override
    public String icon() {
        return BuffIndicator.MAGIC_POINT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0.5f, 0f, 1f); // 紫色调
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(getIntPoints());
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", getIntPoints(), healValue);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(POINTS, currentPoints);
        bundle.put(HEAL_VALUE, healValue);
        bundle.put(REFRESH_VALUE, refreshValue);
        bundle.put(SURGERY_USES, surgeryUses);
        bundle.put(ACTIVE_SURGERY_SUMMON, activeSurgerySummon);
        if (lastKilledMob != null) {
            bundle.put(LAST_KILLED_MOB, lastKilledMob.getName());
        }
        if (!cooldowns.isEmpty()) {
            Bundle cd = new Bundle();
            for (Class<? extends DiceMageSpell> c : cooldowns.keySet()) {
                cd.put(c.getName(), cooldowns.get(c));
            }
            bundle.put(COOLDOWNS, cd);
        }
        if (specialSpells != null) {
            String[] names = new String[specialSpells.length];
            for (int i = 0; i < specialSpells.length; i++) {
                names[i] = specialSpells[i] != null ? specialSpells[i].getName() : null;
            }
            bundle.put(SPECIAL_SPELLS, names);
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        currentPoints = bundle.getFloat(POINTS);
        healValue = bundle.contains(HEAL_VALUE) ? bundle.getInt(HEAL_VALUE) : 50;
        refreshValue = bundle.contains(REFRESH_VALUE) ? bundle.getInt(REFRESH_VALUE) : 40;
        surgeryUses = bundle.getInt(SURGERY_USES);
        activeSurgerySummon = bundle.getInt(ACTIVE_SURGERY_SUMMON);
        if (bundle.contains(LAST_KILLED_MOB)) {
            try {
                Class<?> cls = Class.forName(bundle.getString(LAST_KILLED_MOB));
                if (Mob.class.isAssignableFrom(cls)) {
                    lastKilledMob = cls.asSubclass(Mob.class);
                }
            } catch (ClassNotFoundException ignored) {
                lastKilledMob = null;
            }
        }
        if (bundle.contains(COOLDOWNS)) {
            cooldowns.clear();
            Bundle cd = bundle.getBundle(COOLDOWNS);
            for (String key : cd.getKeys()) {
                try {
                    Class<?> cls = Class.forName(key);
                    if (DiceMageSpell.class.isAssignableFrom(cls)) {
                        cooldowns.put(cls.asSubclass(DiceMageSpell.class), cd.getFloat(key));
                    }
                } catch (ClassNotFoundException ignored) {
                    // ignore unknown spell classes from older saves
                }
            }
        }
        if (bundle.contains(SPECIAL_SPELLS)) {
            String[] names = bundle.getStringArray(SPECIAL_SPELLS);
            if (names != null) {
                @SuppressWarnings("unchecked")
                Class<? extends DiceMageSpell>[] arr = new Class[names.length];
                for (int i = 0; i < names.length; i++) {
                    if (names[i] == null) continue;
                    try {
                        Class<?> cls = Class.forName(names[i]);
                        if (DiceMageSpell.class.isAssignableFrom(cls)) {
                            arr[i] = cls.asSubclass(DiceMageSpell.class);
                        }
                    } catch (ClassNotFoundException ignored) {
                        // ignore
                    }
                }
                specialSpells = arr;
            }
        }
        updateAction();
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public String actionIcon() {
        return HeroIcon.DICE_MAGE;
    }

    @Override
    public int indicatorColor() {
        return 0x8844FF;
    }

    @Override
    public void doAction() {
        if (Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.DICE_MAGE) {
            GameScene.show(new WndDiceMageSpells());
        }
    }
}
