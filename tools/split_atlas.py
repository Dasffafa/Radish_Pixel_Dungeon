#!/usr/bin/env python3
"""
Atlas 拆分工具
从 items.png + ItemSpriteSheet.java 拆分出单个 PNG 文件

使用方法:
    python3 split_atlas.py

输出:
    assets/sprites/items/*.png
"""

import re
import os
from PIL import Image
from pathlib import Path

# 配置
ATLAS_WIDTH = 32      # 每行32个格子
FRAME_SIZE = 16       # 每个格子16x16

# 项目路径
PROJECT_ROOT = Path(__file__).parent.parent
ATLAS_PATH = PROJECT_ROOT / "core/src/main/assets/sprites/items.png"
JAVA_PATH = PROJECT_ROOT / "core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/sprites/ItemSpriteSheet.java"
OUTPUT_DIR = PROJECT_ROOT / "core/src/main/assets/sprites/items"


def xy_to_id(x, y):
    """xy(x, y) 转 ID，与 Java 中的逻辑一致"""
    return (x - 1) + ATLAS_WIDTH * (y - 1)


def parse_java_file(java_path):
    """解析 Java 文件，提取所有常量定义"""
    with open(java_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 存储解析结果
    xy_bases = {}    # xy(x, y) 基准定义
    constants = {}   # 常量名 -> ID
    
    # 1. 解析 xy(x, y) 基准定义
    # 例如: private static final int PLACEHOLDERS = xy(1, 1);
    xy_pattern = re.compile(
        r'private\s+static\s+final\s+int\s+([A-Z_][A-Z0-9_]*)\s*=\s*xy\(\s*(\d+)\s*,\s*(\d+)\s*\)\s*;'
    )
    
    for match in xy_pattern.finditer(content):
        name = match.group(1)
        x = int(match.group(2))
        y = int(match.group(3))
        xy_bases[name] = xy_to_id(x, y)
    
    print(f"发现 {len(xy_bases)} 个 xy 基准定义:")
    for name, id in xy_bases.items():
        print(f"  {name} = {id}")
    
    # 2. 解析 public static final int 常量
    # 例如: public static final int GOLD = UNCOLLECTIBLE+0;
    const_pattern = re.compile(
        r'public\s+static\s+final\s+int\s+([A-Z_][A-Z0-9_]*)\s*=\s*([^;]+);'
    )
    
    def evaluate_expr(expr, xy_bases, constants):
        """计算表达式值"""
        expr = expr.strip()
        
        # 纯数字
        if expr.isdigit():
            return int(expr)
        
        # BASE+offset 或 BASE+offset+extra
        parts = expr.replace('+', ' ').split()
        result = 0
        
        for part in parts:
            if part.isdigit():
                result += int(part)
            elif part in xy_bases:
                result += xy_bases[part]
            elif part in constants:
                result += constants[part]
            else:
                # 未知引用
                return None
        
        return result
    
    # 按顺序解析，处理依赖关系
    lines = content.split('\n')
    
    for line in lines:
        match = const_pattern.search(line)
        if match:
            name = match.group(1)
            expr = match.group(2)
            
            id = evaluate_expr(expr, xy_bases, constants)
            if id is not None:
                constants[name] = id
    
    print(f"\n发现 {len(constants)} 个常量定义")
    
    return constants


def split_atlas(atlas_path, output_dir, constants):
    """拆分 atlas 为单个 PNG"""
    
    # 读取 atlas
    atlas = Image.open(atlas_path)
    print(f"\nAtlas 尺寸: {atlas.width}x{atlas.height}")
    
    # 创建输出目录
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 拆分每个帧
    saved = 0
    skipped = 0
    
    for name, id in sorted(constants.items(), key=lambda x: x[1]):
        # 计算 xy 坐标
        x = (id % ATLAS_WIDTH) * FRAME_SIZE
        y = (id // ATLAS_WIDTH) * FRAME_SIZE
        
        # 检查是否在范围内
        if x + FRAME_SIZE > atlas.width or y + FRAME_SIZE > atlas.height:
            print(f"  [跳过] {name} (ID={id}) 坐标 ({x},{y}) 超出范围")
            skipped += 1
            continue
        
        # 提取子图
        frame = atlas.crop((x, y, x + FRAME_SIZE, y + FRAME_SIZE))
        
        # 保存为 PNG
        filename = name.lower() + ".png"
        frame.save(output_dir / filename)
        saved += 1
    
    print(f"\n完成!")
    print(f"  保存: {saved} 个文件")
    print(f"  跳过: {skipped} 个")
    print(f"  输出: {output_dir.absolute()}")


def main():
    print("=" * 50)
    print("Atlas 拆分工具")
    print("=" * 50)
    
    print(f"\nAtlas: {ATLAS_PATH}")
    print(f"Java源: {JAVA_PATH}")
    print(f"输出目录: {OUTPUT_DIR}")
    
    # 解析 Java 文件
    print("\n步骤1: 解析 Java 常量...")
    constants = parse_java_file(JAVA_PATH)
    
    # 拆分 atlas
    print("\n步骤2: 拆分 Atlas...")
    split_atlas(ATLAS_PATH, OUTPUT_DIR, constants)
    
    # 列出部分输出文件
    print("\n输出文件预览:")
    files = sorted(OUTPUT_DIR.glob("*.png"))[:10]
    for f in files:
        print(f"  {f.name}")
    if len(list(OUTPUT_DIR.glob("*.png"))) > 10:
        print(f"  ... 共 {len(list(OUTPUT_DIR.glob('*.png')))} 个文件")


if __name__ == "__main__":
    main()