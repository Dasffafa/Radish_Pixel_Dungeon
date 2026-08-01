/*
 * Atlas 拆分工具
 * 从 items.png + ItemSpriteSheet.java 拆分出单个 PNG 文件
 * 
 * 使用方法：
 * javac SplitAtlas.java
 * java SplitAtlas <atlas.png> <ItemSpriteSheet.java> <output_dir>
 * 
 * 例如：
 * java SplitAtlas ../core/src/main/assets/sprites/items.png \
 *                  ../core/src/main/java/.../ItemSpriteSheet.java \
 *                  ../core/src/main/assets/sprites/items/
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class SplitAtlas {
    
    private static final int ATLAS_WIDTH = 32;  // 每行32个格子
    private static final int FRAME_SIZE = 16;   // 每个格子16x16
    
    // 解析结果
    private static Map<String, Integer> nameToId = new LinkedHashMap<>();
    
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("用法: java SplitAtlas <atlas.png> <ItemSpriteSheet.java> <output_dir>");
            System.out.println("例如: java SplitAtlas items.png ItemSpriteSheet.java items/");
            System.exit(1);
        }
        
        String atlasPath = args[0];
        String javaPath = args[1];
        String outputDir = args[2];
        
        System.out.println("=== Atlas 拆分工具 ===");
        System.out.println("Atlas: " + atlasPath);
        System.out.println("Java源: " + javaPath);
        System.out.println("输出目录: " + outputDir);
        System.out.println();
        
        // 1. 解析 Java 文件，提取常量定义
        System.out.println("步骤1: 解析 Java 常量...");
        parseJavaFile(javaPath);
        System.out.println("发现 " + nameToId.size() + " 个常量");
        
        // 2. 读取 atlas 图片
        System.out.println("\n步骤2: 读取 Atlas 图片...");
        BufferedImage atlas = ImageIO.read(new File(atlasPath));
        System.out.println("Atlas 尺寸: " + atlas.getWidth() + "x" + atlas.getHeight());
        
        // 3. 创建输出目录
        Files.createDirectories(Paths.get(outputDir));
        
        // 4. 拆分并保存每个帧
        System.out.println("\n步骤3: 拆分并保存帧...");
        int saved = 0;
        int skipped = 0;
        
        for (Map.Entry<String, Integer> entry : nameToId.entrySet()) {
            String name = entry.getKey();
            int id = entry.getValue();
            
            // 计算 xy 坐标
            int x = (id % ATLAS_WIDTH) * FRAME_SIZE;
            int y = (id / ATLAS_WIDTH) * FRAME_SIZE;
            
            // 检查是否在 atlas 范围内
            if (x + FRAME_SIZE > atlas.getWidth() || y + FRAME_SIZE > atlas.getHeight()) {
                System.out.println("  [跳过] " + name + " (ID=" + id + ") 坐标超出范围");
                skipped++;
                continue;
            }
            
            // 提取子图
            BufferedImage frame = atlas.getSubimage(x, y, FRAME_SIZE, FRAME_SIZE);
            
            // 保存为 PNG
            String fileName = name.toLowerCase() + ".png";
            File outputFile = new File(outputDir, fileName);
            ImageIO.write(frame, "PNG", outputFile);
            
            saved++;
            if (saved % 50 == 0) {
                System.out.println("  已保存 " + saved + " 个文件...");
            }
        }
        
        System.out.println("\n=== 完成 ===");
        System.out.println("保存: " + saved + " 个文件");
        System.out.println("跳过: " + skipped + " 个");
        System.out.println("输出目录: " + new File(outputDir).getAbsolutePath());
    }
    
    /**
     * 解析 Java 文件，提取静态常量定义
     */
    private static void parseJavaFile(String javaPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(javaPath));
        
        // 正则匹配: public static final int NAME = ...;
        Pattern constPattern = Pattern.compile(
            "^\\s*public\\s+static\\s+final\\s+int\\s+([A-Z_][A-Z0-9_]*)\\s*=\\s*(.+);\\s*$"
        );
        
        // 正则匹配: private static final int NAME = xy(x, y);
        Pattern xyPattern = Pattern.compile(
            "^\\s*private\\s+static\\s+final\\s+int\\s+([A-Z_][A-Z0-9_]*)\\s*=\\s*xy\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)\\s*;\\s*$"
        );
        
        Map<String, Integer> xyBase = new HashMap<>();  // 存储 xy(x,y) 基准值
        
        for (String line : lines) {
            // 检查是否是 xy(x, y) 基准定义
            Matcher xyMatcher = xyPattern.matcher(line);
            if (xyMatcher.matches()) {
                String name = xyMatcher.group(1);
                int x = Integer.parseInt(xyMatcher.group(2));
                int y = Integer.parseInt(xyMatcher.group(3));
                int id = xyToId(x, y);
                xyBase.put(name, id);
                System.out.println("  [基准] " + name + " = xy(" + x + ", " + y + ") => ID=" + id);
                continue;
            }
            
            // 检查是否是常量定义
            Matcher constMatcher = constPattern.matcher(line);
            if (constMatcher.matches()) {
                String name = constMatcher.group(1);
                String value = constMatcher.group(2).trim();
                
                try {
                    int id = evaluateExpression(value, xyBase);
                    nameToId.put(name, id);
                } catch (Exception e) {
                    // 跳过无法解析的表达式
                }
            }
        }
    }
    
    /**
     * xy(x, y) 转 ID
     */
    private static int xyToId(int x, int y) {
        return (x - 1) + ATLAS_WIDTH * (y - 1);
    }
    
    /**
     * 计算表达式值
     * 支持: 数字、+数字、BASE+数字、BASE+数字+数字 等
     */
    private static int evaluateExpression(String expr, Map<String, Integer> xyBase) {
        expr = expr.replaceAll("\\s+", "");
        
        // 纯数字
        if (expr.matches("\\d+")) {
            return Integer.parseInt(expr);
        }
        
        // BASE + offset 或 BASE + offset + extra
        // 例如: PLACEHOLDERS+0, UNCOLLECTIBLE+3, ARTIFACTS+16+16
        String[] parts = expr.split("\\+");
        
        int result = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            
            if (part.matches("\\d+")) {
                result += Integer.parseInt(part);
            } else if (xyBase.containsKey(part)) {
                result += xyBase.get(part);
            } else if (nameToId.containsKey(part)) {
                result += nameToId.get(part);
            } else {
                // 未知引用，尝试解析为数字
                throw new RuntimeException("Unknown reference: " + part);
            }
        }
        
        return result;
    }
}