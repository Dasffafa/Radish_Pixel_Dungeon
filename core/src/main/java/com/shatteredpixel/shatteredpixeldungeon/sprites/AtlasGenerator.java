/*
 * Radish Pixel Dungeon
 * Atlas Generator - 运行时动态生成 Atlas
 * 
 * 功能：
 * 1. 扫描 items/ 目录的所有 PNG 文件
 * 2. 使用 bin-packing 算法紧凑排列
 * 3. 生成 Texture 和帧映射
 * 4. 支持任意尺寸图片（渲染时缩放）
 */

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.watabou.noosa.Game;
import com.watabou.utils.RectF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtlasGenerator {
    
    // 单例实例
    private static AtlasGenerator itemsAtlas;
    
    // 帧信息
    public static class Frame {
        public final String name;
        public final int x, y;
        public final int width, height;
        public final int originalWidth, originalHeight;
        
        public Frame(String name, int x, int y, int width, int height, int origW, int origH) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.originalWidth = origW;
            this.originalHeight = origH;
        }
        
        public RectF getRect(int atlasWidth, int atlasHeight) {
            return new RectF(
                (float)x / atlasWidth,
                (float)y / atlasHeight,
                (float)(x + width) / atlasWidth,
                (float)(y + height) / atlasHeight
            );
        }
    }
    
    // Bin-packing 节点
    private static class Node {
        int x, y, width, height;
        Node left, right;
        boolean used = false;
        
        Node(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        Node insert(int w, int h, int padding) {
            if (left != null || right != null) {
                Node node = left.insert(w, h, padding);
                if (node != null) return node;
                return right.insert(w, h, padding);
            }
            
            if (used) return null;
            if (w > width || h > height) return null;
            if (w == width && h == height) {
                used = true;
                return this;
            }
            
            int dw = width - w;
            int dh = height - h;
            
            if (dw > dh) {
                left = new Node(x, y, w + padding, height);
                right = new Node(x + w + padding, y, width - w - padding, height);
            } else {
                left = new Node(x, y, width, h + padding);
                right = new Node(x, y + h + padding, width, height - h - padding);
            }
            
            return left.insert(w, h, padding);
        }
    }
    
    // 图片信息
    private static class ImageInfo {
        String name;
        Pixmap pixmap;
        int width, height;
        
        ImageInfo(String name, Pixmap pixmap) {
            this.name = name;
            this.pixmap = pixmap;
            this.width = pixmap.getWidth();
            this.height = pixmap.getHeight();
        }
    }
    
    // 生成的结果
    private Texture texture;
    private Map<String, Frame> frames;
    private int atlasWidth, atlasHeight;
    private int targetSize;  // 目标渲染尺寸（用于缩放）
    
    private AtlasGenerator() {
        frames = new HashMap<>();
        targetSize = 16;  // 默认渲染为 16x16
    }
    
    /**
     * 获取 items atlas
     */
    public static AtlasGenerator getItemsAtlas() {
        if (itemsAtlas == null) {
            itemsAtlas = createFromDirectory("sprites/items", 16);
        }
        return itemsAtlas;
    }
    
    /**
     * 从目录创建 atlas
     */
    public static AtlasGenerator createFromDirectory(String path, int targetSize) {
        AtlasGenerator generator = new AtlasGenerator();
        generator.targetSize = targetSize;
        generator.generate(path);
        return generator;
    }
    
    /**
     * 生成 atlas
     */
    private void generate(String path) {
        // 1. 加载所有图片
        List<ImageInfo> images = loadImages(path);
        if (images.isEmpty()) {
            Gdx.app.error("AtlasGenerator", "No images found in " + path);
            return;
        }
        
        // 2. 按面积排序（大的先放）
        Collections.sort(images, new Comparator<ImageInfo>() {
            @Override
            public int compare(ImageInfo a, ImageInfo b) {
                return (b.width * b.height) - (a.width * a.height);
            }
        });
        
        // 3. 计算 atlas 尺寸
        int totalArea = 0;
        for (ImageInfo img : images) {
            totalArea += img.width * img.height;
        }
        
        int size = 32;
        while (size * size < totalArea * 2) {
            size *= 2;
        }
        size = Math.min(size, 2048);  // 最大 2048x2048
        
        // 4. Bin-packing
        int padding = 1;  // 像素间距
        Node root = new Node(0, 0, size, size);
        Map<ImageInfo, Node> placements = new HashMap<>();
        
        // 可能需要多次尝试增大尺寸
        while (true) {
            boolean allPlaced = true;
            placements.clear();
            root = new Node(0, 0, size, size);
            
            for (ImageInfo img : images) {
                Node node = root.insert(img.width, img.height, padding);
                if (node == null) {
                    allPlaced = false;
                    break;
                }
                placements.put(img, node);
            }
            
            if (allPlaced) break;
            
            size *= 2;
            if (size > 2048) {
                Gdx.app.error("AtlasGenerator", "Atlas too large!");
                break;
            }
        }
        
        // 5. 创建 pixmap 并绘制
        Pixmap atlas = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        for (Map.Entry<ImageInfo, Node> entry : placements.entrySet()) {
            ImageInfo img = entry.getKey();
            Node node = entry.getValue();
            
            atlas.drawPixmap(img.pixmap, node.x, node.y);
            
            // 记录帧信息
            Frame frame = new Frame(
                img.name,
                node.x, node.y,
                img.width, img.height,
                img.width, img.height
            );
            frames.put(img.name, frame);
            
            // 释放原图
            img.pixmap.dispose();
        }
        
        // 6. 创建纹理
        atlasWidth = size;
        atlasHeight = size;
        texture = new Texture(atlas);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        
        atlas.dispose();
        
        Gdx.app.log("AtlasGenerator", "Generated atlas: " + size + "x" + size + 
                    " with " + frames.size() + " frames");
    }
    
    /**
     * 加载目录下所有图片
     */
    private List<ImageInfo> loadImages(String path) {
        List<ImageInfo> images = new ArrayList<>();
        
        FileHandle dir = Gdx.files.internal(path);
        if (!dir.exists() || !dir.isDirectory()) {
            Gdx.app.error("AtlasGenerator", "Directory not found: " + path);
            return images;
        }
        
        for (FileHandle file : dir.list()) {
            if (file.extension().equalsIgnoreCase("png")) {
                try {
                    Pixmap pixmap = new Pixmap(file);
                    String name = file.nameWithoutExtension().toLowerCase();
                    images.add(new ImageInfo(name, pixmap));
                } catch (Exception e) {
                    Gdx.app.error("AtlasGenerator", "Failed to load: " + file.path());
                }
            }
        }
        
        return images;
    }
    
    /**
     * 获取帧信息
     */
    public Frame getFrame(String name) {
        return frames.get(name.toLowerCase());
    }
    
    /**
     * 获取纹理区域（用于渲染）
     */
    public TextureRegion getRegion(String name) {
        Frame frame = getFrame(name);
        if (frame == null) return null;
        return new TextureRegion(texture, frame.x, frame.y, frame.width, frame.height);
    }
    
    /**
     * 获取缩放后的纹理区域（统一缩放到目标尺寸）
     */
    public TextureRegion getRegionScaled(String name) {
        return getRegion(name);  // 缩放由渲染时处理
    }
    
    /**
     * 获取纹理
     */
    public Texture getTexture() {
        return texture;
    }
    
    /**
     * 获取目标渲染尺寸
     */
    public int getTargetSize() {
        return targetSize;
    }
    
    /**
     * 获取所有帧名称
     */
    public Iterable<String> getFrameNames() {
        return frames.keySet();
    }
    
    /**
     * 检查帧是否存在
     */
    public boolean hasFrame(String name) {
        return frames.containsKey(name.toLowerCase());
    }
    
    /**
     * 释放资源
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        frames.clear();
    }
}