#!/usr/bin/env python3
"""
将 ItemSpriteSheet.java 从 int 常量转换为 String 常量
"""

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.parent
JAVA_PATH = PROJECT_ROOT / "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java"
OUTPUT_PATH = PROJECT_ROOT / "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java.new"

def convert():
    with open(JAVA_PATH, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    output = []
    in_class = False
    in_icons = False
    skip_until_class = True
    
    for i, line in enumerate(lines):
        # 跳过直到类定义
        if 'public class ItemSpriteSheet' in line:
            skip_until_class = False
            # 写入新的类头
            output.append('''/*
 * Pixel Dungeon - Radish Pixel Dungeon
 * 物品贴图表 - 动态 Atlas 版本
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

/**
 * 物品贴图表
 * 
 * 重构说明：
 * - 所有常量从 int 改为 String
 * - 值为对应的文件名（小写）
 * - 运行时通过 AtlasGenerator 动态生成 atlas
 */
public class ItemSpriteSheet {

    public static final int SIZE = 16;

    // 动态生成的 atlas（运行时初始化）
    private static AtlasGenerator atlas;
    
    /**
     * 初始化 atlas（游戏启动时调用）
     */
    public static void initAtlas() {
        if (atlas == null) {
            atlas = AtlasGenerator.getItemsAtlas();
        }
    }
    
    /**
     * 获取纹理区域
     */
    public static TextureRegion getRegion(String name) {
        initAtlas();
        return atlas.getRegion(name);
    }
    
    /**
     * 检查帧是否存在
     */
    public static boolean hasFrame(String name) {
        initAtlas();
        return atlas.hasFrame(name);
    }
    
    // 兼容旧代码：film 字段
    public static TextureFilm film = new TextureFilm(Assets.Sprites.ITEMS, SIZE, SIZE);

''')
            in_class = True
            continue
        
        if skip_until_class:
            continue
        
        # 处理 Icons 内部类
        if 'public static class Icons' in line:
            in_icons = True
            output.append('''
    // 8x8 图标类（暂保留 int）
    public static class Icons {
        public static final int SIZE = 8;
        public static TextureFilm film = new TextureFilm(Assets.Sprites.ITEM_ICONS, SIZE, SIZE);

''')
            continue
        
        if in_icons and line.strip() == '}' and 'Icons' not in lines[i-1] if i > 0 else True:
            # 检查是否是 Icons 类的结束
            # 找下一个非空行
            next_line = lines[i+1].strip() if i+1 < len(lines) else ''
            if next_line.startswith('static') or next_line.startswith('}') or next_line == '':
                in_icons = False
                output.append('    }\n\n')
                continue
        
        # 跳过旧的 xy 函数和私有基准常量
        if 'private static final int' in line and ('xy(' in line or '= xy(' in line):
            continue
        if 'private static int xy' in line:
            continue
        if 'TheCatist 2026' in line:
            continue
        if 'public static TextureFilm film' in line and not in_icons:
            continue
        if 'public static final int SIZE' in line and not in_icons:
            continue
        if 'private static final int WIDTH' in line:
            continue
        if 'ItemSpriteOptimizer' in line:
            continue
        if 'static {' in line and i < len(lines) - 1 and 'ItemSpriteOptimizer' in lines[i+1]:
            continue
        
        # 转换常量: public static final int NAME = ...;
        match = re.match(r'^(\s*)public\s+static\s+final\s+int\s+([A-Z_][A-Z0-9_]*)\s*=\s*([^;]+);\s*$', line)
        if match and not in_icons:
            indent = match.group(1)
            name = match.group(2)
            # 转 String
            output.append(f'{indent}public static final String {name} = "{name.lower()}";\n')
            continue
        
        # Icons 类中的常量保持 int
        if in_icons:
            # 保留原来的 int 定义
            output.append(line)
            continue
        
        # 其他行
        output.append(line)
    
    # 写入
    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        f.writelines(output)
    
    print(f"已生成: {OUTPUT_PATH}")

if __name__ == "__main__":
    convert()