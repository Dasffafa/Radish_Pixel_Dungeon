package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.SmallWoodenCross;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MultiTool;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClassSpecificItemsTest {
    @BeforeClass
    public static void initializeVersion() {
        Game.version = "test";
    }

    @Test
    public void priestCrossAndEngineerToolAreExclusiveButOrdinaryItemsAreNot() {
        assertEquals(HeroClasses.RECTOR, ClassSpecificItems.ownerOf(new SmallWoodenCross()));
        assertEquals(HeroClasses.ENGINEER, ClassSpecificItems.ownerOf(new MultiTool()));
        assertNull(ClassSpecificItems.ownerOf(new Item()));
    }

    @Test
    public void replacementsChangeOwnerAndPreserveCurseAndKnowledge() {
        MagesStaff original = new MagesStaff(new WandOfMagicMissile());
        original.cursed = true;
        original.cursedKnown = true;
        original.levelKnown = true;
        Random.pushGenerator(71643L);
        try {
            for (int index = 0; index < 64; index++) {
                Item replacement = ClassSpecificItems.replacementFor(original);
                assertNotNull(replacement);
                assertNotEquals(HeroClasses.MAGE, ClassSpecificItems.ownerOf(replacement));
                assertTrue(replacement.cursed);
                assertTrue(replacement.cursedKnown);
                assertTrue(replacement.levelKnown);
            }
        } finally {
            Random.popGenerator();
        }
    }

    @Test
    public void generatedStaffContainsUsableWand() {
        Random.pushGenerator(93017L);
        try {
            for (int index = 0; index < 256; index++) {
                Item replacement = ClassSpecificItems.replacementFor(new BrokenSeal());
                if (replacement instanceof MagesStaff) {
                    assertTrue(((MagesStaff) replacement).wand instanceof WandOfMagicMissile);
                    return;
                }
            }
            fail("The deterministic replacement sequence must include a staff");
        } finally {
            Random.popGenerator();
        }
    }
}
