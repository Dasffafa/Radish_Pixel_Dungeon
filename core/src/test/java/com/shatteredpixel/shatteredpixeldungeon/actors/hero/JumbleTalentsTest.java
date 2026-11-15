package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import org.junit.AfterClass;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ClassSpecificItems;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class JumbleTalentsTest {
    @BeforeClass
    public static void initializeVersion() {
        Game.version = "test";
        GdxNativesLoader.load();
        TextureCache.put(Assets.Effects.TEXT_ICONS,
                new SmartTexture(new Pixmap(256, 256, Pixmap.Format.RGBA8888)) {
                    @Override public void delete() { bitmap.dispose(); bitmap = null; }
                });
    }

    @AfterClass
    public static void releaseTexture() { TextureCache.remove(Assets.Effects.TEXT_ICONS); }

    @Test
    public void initialAndRepeatedTalentsKeepCountsAndMissingBudget() throws IllegalAccessException {
        Random.pushGenerator(451287L);
        Hero previous = Dungeon.hero;
        try {
            Hero hero = new Hero();
            Dungeon.hero = hero;
            hero.heroClass = HeroClasses.JUMBLE;
            for (java.lang.reflect.Field field : Talent.class.getFields()) {
                if (field.getType() != Talent.class) continue;
                Talent talent = (Talent) field.get(null);
                if (talent.requirement() != null && Item.class.isAssignableFrom(talent.requirement())) {
                    hero.talentRequirementItems.add(talent.requirement().getName());
                }
            }
            JumbleTalents.initClass(hero);
            hero.subClass = HeroSubClasses.JUMBLE;
            JumbleTalents.initSubclass(hero);
            hero.powerOfImp = true;
            JumbleTalents.initImp(hero);
            HeroSubClass first = hero.jumbleFirst, second = hero.jumbleSecond;
            for (int iteration = 0; iteration < 64; iteration++) {
                assertEquals(4, hero.talents.get(0).size());
                assertEquals(5, hero.talents.get(1).size());
                assertEquals(5, hero.talents.get(2).size());
                assertEquals(4, hero.talents.get(3).size());
                for (LinkedHashMap<Talent, Integer> tier : hero.talents) {
                    int missing = 0;
                    for (Talent talent : tier.keySet()) {
                        boolean exclusiveRequirement = false;
                        for (HeroClass owner : HeroClasses.ALL) {
                            for (Class<? extends Item> type : ClassSpecificItems.itemsFor(owner)) {
                                if (type == talent.requirement()) exclusiveRequirement = true;
                            }
                        }
                        if (talent == Talent.IRON_WILL || talent == Talent.RUNIC_TRANSFERENCE
                                || talent == Talent.LIGHT_CLOAK || exclusiveRequirement) missing++;
                    }
                    assertTrue("Missing item budget exceeded", missing <= 1);
                }
                assertSame(first, hero.jumbleFirst);
                assertSame(second, hero.jumbleSecond);
                JumbleTalents.metamorph(hero);
            }
        } finally {
            Dungeon.hero = previous;
            Random.popGenerator();
        }
    }

    @Test
    public void restrictedSubclassPairRejectsLosingOnlyUsableBow() {
        Hero hero = new Hero();
        hero.heroClass = HeroClasses.JUMBLE;
        hero.jumbleFirst = HeroSubClasses.SNIPER;
        hero.jumbleSecond = HeroSubClasses.CRAFTSMAN;
        hero.talentRequirementItems.add(SpiritBow.class.getName());
        SpiritBow bow = new SpiritBow();
        hero.belongings.backpack.items.add(bow);
        assertFalse(JumbleTalents.canRollWithReplacement(hero, bow, BrokenSeal.class));
        assertTrue(JumbleTalents.canRollWithReplacement(hero, bow, SpiritBow.class));
        assertTrue(JumbleTalents.canRollWithReplacement(hero, new CloakOfShadows(), MagesStaff.class));
    }
}
