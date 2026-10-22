package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 炽炎附魔：命中后在目标身后划出一条 1 格宽的火径，点燃路径上的敌人。
 */
public class BlazingFury extends Weapon.Enchantment {

    private static final ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFF6600 );

    private static final int PATH_LENGTH = 3;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        int w = Dungeon.level.width();
        int ax = attacker.pos % w, ay = attacker.pos / w;
        int dx = defender.pos % w, dy = defender.pos / w;

        int stepX = Integer.signum( dx - ax );
        int stepY = Integer.signum( dy - ay );
        if (Math.abs( dx - ax ) >= Math.abs( dy - ay )) {
            stepY = 0;
        } else {
            stepX = 0;
        }
        if (stepX == 0 && stepY == 0) {
            return damage;
        }

        int cell = defender.pos + stepY * w + stepX;
        for (int i = 0; i < PATH_LENGTH; i++) {
            if (cell < 0 || cell >= Dungeon.level.length()) {
                break;
            }
            if (Dungeon.level.solid[cell]) {
                break;
            }
            int terrain = Dungeon.level.map[cell];
            if (terrain != Terrain.EMPTY && terrain != Terrain.EMPTY_SP && terrain != Terrain.EMPTY_DECO
                    && terrain != Terrain.GRASS && terrain != Terrain.HIGH_GRASS
                    && terrain != Terrain.FURROWED_GRASS && terrain != Terrain.EMBERS) {
                break;
            }

            Level.set( cell, Terrain.EMBERS );
            GameScene.updateMap( cell );

            Char ch = Actor.findChar( cell );
            if (ch != null && ch != attacker) {
                Buff.affect( ch, Burning.class ).reignite( ch );
            }

            cell += stepY * w + stepX;
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return ORANGE;
    }
}
