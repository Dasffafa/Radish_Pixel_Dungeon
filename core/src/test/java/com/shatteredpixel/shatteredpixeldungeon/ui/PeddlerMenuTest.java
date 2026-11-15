package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.utils.GdxNativesLoader;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeddlerMenuTest {

    @BeforeClass
    public static void loadNatives() {
        GdxNativesLoader.load();
    }

    @Test
    public void menuConstructsBeforeCameraAssignmentAndOnlyClosesOutside() {
        TestMenu menu = new TestMenu();
        try {
            assertNull(menu.camera);
            menu.camera = new Camera(0, 0, 320, 240, 1);
            menu.placeAbove(160, 120);
            assertEquals(151, menu.left(), 0);
            assertEquals(100, menu.top(), 0);
            PointerArea outsideClick = menu.outsideClick();
            assertNotNull(outsideClick);
            outsideClick.onSignal(new PointerEvent(160, 109, 0, PointerEvent.Type.DOWN));
            assertTrue(menu.alive);
            outsideClick.onSignal(new PointerEvent(10, 10, 0, PointerEvent.Type.HOVER));
            assertTrue(menu.alive);
            outsideClick.onSignal(new PointerEvent(10, 10, 0, PointerEvent.Type.DOWN));
            assertFalse(menu.alive);
        } finally {
            menu.destroy();
        }
    }
    private static class TestMenu extends PeddlerMenu {
        TestMenu() {
            super(new Image[]{null}, new Runnable[]{() -> {}});
        }

        PointerArea outsideClick() {
            for (Gizmo child : members) {
                if (child instanceof PointerArea) return (PointerArea) child;
            }
            return null;
        }
    }
}
