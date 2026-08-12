package com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.utils.Random;

import java.util.HashMap;

/**
 * 刃雨（刀刃学派 L3）：朝瞄准方向喷射 120° 扇形内共 16 枚飞刀（使用盗贼飞刀贴图）。
 * 每枚沿其射线飞行：命中遇到的第一个敌人则造成 20-30 物理伤害；
 * 命中墙体则直接消失。刀刃不会掉落到地上。每个敌人最多被命中 3 次，多余投射物无法被阻挡。
 */
public class BladeRainSpell extends DiceMageSpell {

    private static final int PROJECTILES = 16;
    private static final double FAN_HALF = Math.PI / 3d;      // 半角 60°，共 120°
    private static final int MAX_HITS_PER_ENEMY = 3;

    @Override
    public Talent school() {
        return Talent.SCHOOL_BLADES;
    }

    @Override
    public int level() {
        return 3;
    }

    @Override
    public int mpCost() {
        return 4;
    }

    @Override
    public String sndImageName() {
        return "blades";
    }

    @Override
    protected void onCast(Hero hero) {
        getTarget(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
                if (cell == null) return;
                if (!spendMagic(hero)) return;

                int width = Dungeon.level.width();
                int height = Dungeon.level.height();
                int fromX = hero.pos % width;
                int fromY = hero.pos / width;
                int aimX = cell % width;
                int aimY = cell / width;
                double baseAngle = Math.atan2(aimY - fromY, aimX - fromX);

                final HashMap<Integer, Integer> hitCounts = new HashMap<>();
                final ThrowingKnife knife = new ThrowingKnife();

                for (int i = 0; i < PROJECTILES; i++) {
                    double angle = baseAngle - FAN_HALF + i * (2d * FAN_HALF) / (PROJECTILES - 1d);
                    double dx = Math.cos(angle);
                    double dy = Math.sin(angle);
                    double xReach = dx > 0 ? (width - 1 - fromX) / dx : dx < 0 ? -fromX / dx : Double.MAX_VALUE;
                    double yReach = dy > 0 ? (height - 1 - fromY) / dy : dy < 0 ? -fromY / dy : Double.MAX_VALUE;
                    double reach = Math.min(xReach, yReach);
                    int endX = Math.max(0, Math.min(width - 1, fromX + (int) Math.round(dx * reach)));
                    int endY = Math.max(0, Math.min(height - 1, fromY + (int) Math.round(dy * reach)));
                    int collision = new Ballistica(hero.pos, endX + endY * width, Ballistica.MAGIC_BOLT).collisionPos;

                    ((MissileSprite) hero.sprite.parent.recycle(MissileSprite.class)).reset(
                            hero.sprite, collision, knife, () -> {
                                Char target = Actor.findChar(collision);
                                if (target == null || target == hero || target.alignment != Char.Alignment.ENEMY) {
                                    // 命中墙体：刀刃消失，不造成伤害
                                    CellEmitter.center(collision).burst(Speck.factory(Speck.RED_LIGHT), 2);
                                    return;
                                }
                                int prev = hitCounts.containsKey(target.id()) ? hitCounts.get(target.id()) : 0;
                                if (prev >= MAX_HITS_PER_ENEMY) return;
                                hitCounts.put(target.id(), prev + 1);
                                int dmg = Random.IntRange(20, 30);
                                target.damage(DamageInfo.physicalNoArmor(dmg, BladeRainSpell.this));
                                if (target.isAlive()) {
                                    CellEmitter.center(target.pos).burst(Speck.factory(Speck.RED_LIGHT), 3);
                                }
                            });
                }
                hero.spendAndNext(1f);
            }

            @Override
            public String prompt() {
                return Messages.get(BladeRainSpell.this, "prompt");
            }
        });
    }
}
