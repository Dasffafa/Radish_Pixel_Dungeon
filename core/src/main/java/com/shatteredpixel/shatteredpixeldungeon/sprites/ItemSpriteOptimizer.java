/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024 TheCatist
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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;

/**
 * 物品贴图优化工具类
 * 自动计算最小外接矩形，使贴图渲染时居中
 *
 * @author TheCatist
 */
public class ItemSpriteOptimizer {

    private static final int SIZE = 16;
    private static final int MIN_ALPHA = 16;

    /**
     * 优化物品贴图
     * 计算最小外接矩形并更新 TextureFilm
     *
     * @param film TextureFilm 对象
     */
    public static void optimize(TextureFilm film) {
        optimize(film, Assets.Sprites.ITEMS, SIZE);
    }

    /**
     * 优化指定纹理的贴图
     *
     * @param film      TextureFilm 对象
     * @param assetPath 资源路径
     * @param cellSize  单元格大小
     */
    public static void optimize(TextureFilm film, String assetPath, int cellSize) {
        SmartTexture texture = TextureCache.get(assetPath);
        if (texture == null || texture.bitmap == null) {
            return;
        }

        Pixmap bitmap = texture.bitmap;
        int texWidth = texture.width;
        int texHeight = texture.height;
        int cols = texWidth / cellSize;
        int rows = texHeight / cellSize;
        int totalSlots = cols * rows;

        int optimizedCount = 0;

        for (int item = 0; item < totalSlots; item++) {
            int slotX = (item % cols) * cellSize;
            int slotY = (item / cols) * cellSize;

            BoundingBox bbox = analyzeSprite(bitmap, slotX, slotY, cellSize);

            if (bbox.hasContent()) {
                film.add(item,
                        slotX + bbox.minX,
                        slotY + bbox.minY,
                        slotX + bbox.maxX + 1,
                        slotY + bbox.maxY + 1);
                optimizedCount++;
            }
        }

        System.out.println("ItemSpriteOptimizer: " + assetPath +
                " - Total: " + totalSlots +
                ", Optimized: " + optimizedCount);
    }

    /**
     * 分析贴图区域，返回边界框信息
     */
    private static BoundingBox analyzeSprite(Pixmap bitmap, int slotX, int slotY, int size) {
        BoundingBox bbox = new BoundingBox();

        for (int py = 0; py < size; py++) {
            for (int px = 0; px < size; px++) {
                int pixel = bitmap.getPixel(slotX + px, slotY + py);
                int alpha = pixel & 0xFF;

                if (alpha >= MIN_ALPHA) {
                    bbox.isEmpty = false;
                    bbox.minX = Math.min(bbox.minX, px);
                    bbox.minY = Math.min(bbox.minY, py);
                    bbox.maxX = Math.max(bbox.maxX, px);
                    bbox.maxY = Math.max(bbox.maxY, py);
                }
            }
        }

        return bbox;
    }

    /**
     * 检查指定位置是否为空贴图
     *
     * @param item 物品 ID
     * @return 如果贴图全透明返回 true
     */
    public static boolean isEmptySprite(int item) {
        SmartTexture texture = TextureCache.get(Assets.Sprites.ITEMS);
        if (texture == null || texture.bitmap == null) return true;

        Pixmap bitmap = texture.bitmap;
        int cols = texture.width / SIZE;
        int slotX = (item % cols) * SIZE;
        int slotY = (item / cols) * SIZE;

        for (int py = 0; py < SIZE; py++) {
            for (int px = 0; px < SIZE; px++) {
                int pixel = bitmap.getPixel(slotX + px, slotY + py);
                int alpha = pixel & 0xFF;
                if (alpha >= MIN_ALPHA) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class BoundingBox {
        boolean isEmpty = true;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        boolean hasContent() {
            return !isEmpty && maxX >= minX && maxY >= minY;
        }
    }
}
