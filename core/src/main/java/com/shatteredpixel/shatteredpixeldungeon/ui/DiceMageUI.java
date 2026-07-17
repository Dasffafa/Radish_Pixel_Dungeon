package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class DiceMageUI {

    public static final int DARK       = 0x120F17;
    public static final int BLACK      = 0x09070B;
    public static final int PANEL      = 0x211A20;
    public static final int PANEL_ALT  = 0x2A2022;
    public static final int CREAM      = 0xF1E5B5;
    public static final int GOLD       = 0xB59E09;
    public static final int ORANGE     = 0xC45E16;
    public static final int RED        = 0xAD1F1F;
    public static final int BLUE       = 0x217B91;
    public static final int PURPLE     = 0x6A4484;
    public static final int GREEN      = 0x388044;
    public static final int GREY_LINE  = 0x51464D;

    public static boolean active() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.DICE_MAGE;
    }

    public static String diceFace(int value) {
        switch (Math.min(6, Math.max(1, value))) {
            case 1: return "[1]";
            case 2: return "[2]";
            case 3: return "[3]";
            case 4: return "[4]";
            case 5: return "[5]";
            default: return "[6]";
        }
    }

    public static int itemLineColor(Item item, boolean equipped) {
        if (item == null) {
            return GREY_LINE;
        } else if (item.cursed && item.cursedKnown) {
            return RED;
        } else if (!item.isIdentified()) {
            if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown) {
                return BLUE;
            } else {
                return PURPLE;
            }
        } else if (equipped) {
            return GOLD;
        } else {
            return GREY_LINE;
        }
    }

    public static int optionLineColor(int index) {
        switch (index % 4) {
            case 0: return BLUE;
            case 1: return PURPLE;
            case 2: return GOLD;
            default: return RED;
        }
    }

    public static class Frame extends Component {

        private final int fillColor;
        private final int lineColor;
        private ColorBlock fill;
        private ColorBlock top;
        private ColorBlock bottom;
        private ColorBlock left;
        private ColorBlock right;

        public Frame(int fillColor, int lineColor) {
            super();
            this.fillColor = fillColor;
            this.lineColor = lineColor;
            fill.hardlight(fillColor);
            top.hardlight(lineColor);
            bottom.hardlight(lineColor);
            left.hardlight(lineColor);
            right.hardlight(lineColor);
        }

        @Override
        protected void createChildren() {
            fill = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(fill);
            top = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(top);
            bottom = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(bottom);
            left = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(left);
            right = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(right);
        }

        @Override
        protected void layout() {
            fill.x = x;
            fill.y = y;
            fill.size(width, height);

            top.x = x;
            top.y = y;
            top.size(width, 1);

            bottom.x = x;
            bottom.y = y + height - 1;
            bottom.size(width, 1);

            left.x = x;
            left.y = y;
            left.size(1, height);

            right.x = x + width - 1;
            right.y = y;
            right.size(1, height);
        }
    }

    // Slice&Dice 风格按钮：黑色底 + 1px 边框 + 居中文字
    public static class DiceButton extends Button {

        private Frame bg;
        private RenderedTextBlock text;

        public DiceButton(String label) {
            super();
            text.text(label);
        }

        public void text(String value) {
            text.text(value);
            layout();
        }

        public void textColor(int value) {
            text.hardlight(value);
        }

        @Override
        protected void createChildren() {
            bg = new Frame(BLACK, GREY_LINE);
            add(bg);

            text = PixelScene.renderTextBlock(8);
            text.hardlight(CREAM);
            add(text);

            super.createChildren();
        }

        @Override
        protected void layout() {
            bg.setRect(x, y, width, height);

            text.setPos(
                x + (width - text.width()) / 2f,
                y + (height - text.height()) / 2f
            );
            PixelScene.align(text);

            super.layout();
        }

        @Override
        protected void onPointerDown() {
            bg.top.hardlight(GOLD);
            bg.bottom.hardlight(GOLD);
            bg.left.hardlight(GOLD);
            bg.right.hardlight(GOLD);
        }

        @Override
        protected void onPointerUp() {
            bg.top.hardlight(GREY_LINE);
            bg.bottom.hardlight(GREY_LINE);
            bg.left.hardlight(GREY_LINE);
            bg.right.hardlight(GREY_LINE);
        }
    }
}
