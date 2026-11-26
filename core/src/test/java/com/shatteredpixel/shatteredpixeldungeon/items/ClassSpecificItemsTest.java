package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.SmallWoodenCross;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MultiTool;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MetalCross;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PaperCutter;
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
    public void classSpecificStartingItemsExcludeOrdinaryStartingWeapons() {
        assertEquals(HeroClasses.RECTOR, ClassSpecificItems.ownerOf(new SmallWoodenCross()));
        assertEquals(HeroClasses.MAGE, ClassSpecificItems.ownerOf(new MagesStaff()));
        assertEquals(HeroClasses.ENGINEER, ClassSpecificItems.ownerOf(new com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand()));
        assertTrue(new com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand().isClassSpecific());
        assertNotNull(ClassSpecificItems.replacementFor(new com.shatteredpixel.shatteredpixeldungeon.items.wands.PulseWand()));
        for (Item weapon : new Item[]{new MetalCross(), new PaperCutter(), new MultiTool()}) {
            assertNull(ClassSpecificItems.ownerOf(weapon));
            assertNull(ClassSpecificItems.replacementFor(weapon));
        }
        assertNull(ClassSpecificItems.ownerOf(new Item()));
    }

    @Test
    public void replacementsChangeOwnerAndPreserveCurseAndKnowledge() {
        BrokenSeal original = new BrokenSeal();
        original.cursed = true;
        original.cursedKnown = true;
        original.levelKnown = true;
        Random.pushGenerator(71643L);
        try {
            for (int index = 0; index < 64; index++) {
                Item replacement = ClassSpecificItems.replacementFor(original);
                assertNotNull(replacement);
                assertNotEquals(HeroClasses.WARRIOR, ClassSpecificItems.ownerOf(replacement));
                assertFalse("New items do not inherit the previous item's curse", replacement.cursed);
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
