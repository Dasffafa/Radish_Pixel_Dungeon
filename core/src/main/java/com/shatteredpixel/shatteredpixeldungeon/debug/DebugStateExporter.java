/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shatteredpixel.shatteredpixeldungeon.debug;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * 把当前游戏状态导出成 JSON，供外部程序（AI 代理 / 调试脚本）读取。
 *
 * <p>设计原则（遵循 AGENTS.md §12 最小侵入）：
 * 本类**只读**游戏状态，不修改任何既有类，不参与游戏逻辑分支。
 * 唯一的副作用是往磁盘写一个 {@code debug/state.json}。
 *
 * <p>输出位置由 {@link #stateFile()} 决定，它走 {@code FileUtils.getFileHandle}，
 * 即「游戏数据目录」下的 {@code debug/} 子目录：
 * <ul>
 *   <li>桌面端实测为
 *       {@code %APPDATA%\.shatteredpixel\Radish Pixel Dungeon\debug\state.json}
 *       （目录名 = {@code .<appPackageName 第二段>} + {@code \} + {@code <appName>}，
 *       均取自根 {@code build.gradle}，改产品名会跟着变）</li>
 *   <li>Android 为 {@code getFilesDir()/debug/state.json}</li>
 * </ul>
 * 它与 {@code game1..game6} 存档槽同级，但位于独立子目录，可整体删除、不影响存档。
 */
public final class DebugStateExporter {

    private DebugStateExporter() {
    }

    /**
     * 生成当前状态的 JSON 字符串。永不返回 null；英雄未初始化时返回一个带
     * {@code "ready": false} 的最小对象。
     */
    public static String buildJson() {
        StringBuilder sb = new StringBuilder(2048);

        Hero hero = Dungeon.hero;
        Level level = Dungeon.level;

        if (hero == null || level == null) {
            sb.append("{\"ready\":false}");
            return sb.toString();
        }

        sb.append('{');
        sb.append("\"ready\":true");

        // ---- 关卡 ----
        sb.append(",\"depth\":").append(Dungeon.depth);
        sb.append(",\"branch\":").append(str(Dungeon.branchId));
        sb.append(",\"gold\":").append(Dungeon.gold);
        sb.append(",\"energy\":").append(Dungeon.energy);
        sb.append(",\"levelWidth\":").append(level.width());
        sb.append(",\"levelHeight\":").append(level.height());

        // ---- 英雄 ----
        sb.append(",\"hero\":{");
        sb.append("\"pos\":").append(hero.pos);
        sb.append(",\"hp\":").append(hero.HP);
        sb.append(",\"ht\":").append(hero.HT);
        sb.append(",\"level\":").append(hero.lvl);
        sb.append(",\"exp\":").append(hero.exp);
        sb.append(",\"str\":").append(hero.STR());
        sb.append('}');

        // ---- 周围怪物 ----
        sb.append(",\"mobs\":[");
        boolean first = true;
        for (Mob m : level.mobs) {
            if (m == null || !m.isAlive()) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{');
            sb.append("\"name\":").append(str(safeName(m)));
            sb.append(",\"pos\":").append(m.pos);
            sb.append(",\"hp\":").append(m.HP);
            sb.append(",\"ht\":").append(m.HT);
            sb.append(",\"dist\":").append(distance(hero.pos, m.pos, level.width()));
            sb.append('}');
        }
        sb.append(']');

        // ---- 背包 ----
        sb.append(",\"items\":[");
        first = true;
        for (Item it : hero.belongings) {
            if (it == null) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{');
            sb.append("\"name\":").append(str(safeName(it)));
            sb.append('}');
        }
        sb.append(']');

        sb.append('}');
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // 自动导出（挂在 Hero.act() 收尾处，见 Hero.java）
    // ---------------------------------------------------------------

    /** 导出目录名（位于游戏数据目录下，与 game1..game6 存档槽物理隔离，可整体删除）。 */
    public static final String EXPORT_DIR = "debug";

    /** 导出文件名。 */
    public static final String EXPORT_FILE = "state.json";

    /**
     * 两次写盘之间的最小间隔（毫秒）。
     *
     * <p>为什么需要节流：{@code Hero.act()} 在每个回合里会被调用，连击 / 多段位移时
     * 可能在同一秒内触发多次。每次同步写盘都会占用游戏主线程的 IO 时间，
     * 帧率会因此抖动。60ms 是「肉眼无感、AI 读取够新」的折中值
     * （DISPLAY 60fps ≈ 16ms/帧，60ms 约 4 帧）。
     */
    private static final long MIN_INTERVAL_MS = 60L;

    /** 上次成功写盘的时刻（{@link System#nanoTime()}）。 */
    private static long lastWriteNanos = 0L;

    /** 上一次导出是否成功，便于外部（如日志）判断。 */
    private static boolean lastWriteOk = false;

    /**
     * 由 {@code Hero.act()} 每回合调用。内部做节流，调用方无需关心频率。
     *
     * <p>全程静默：任何异常都被吞掉并返回 false，**绝不**因为导出失败打断游戏。
     * 这是本类的首要约束——它是一个旁观者，不是游戏逻辑的一部分。
     *
     * @return 本次是否真的写了盘（false 可能是被节流跳过，也可能是写失败）
     */
    public static boolean tick() {
        long now = System.nanoTime();
        if (lastWriteNanos != 0L && (now - lastWriteNanos) / 1_000_000L < MIN_INTERVAL_MS) {
            return false;
        }
        lastWriteNanos = now;

        Hero hero = Dungeon.hero;
        Level level = Dungeon.level;
        if (hero == null || level == null) {
            // 还没进关卡（标题界面 / 读档中），不写，避免外部读到 ready:false 的假状态
            return false;
        }

        lastWriteOk = writeTo(stateFile());
        return lastWriteOk;
    }

    /** 上一次 {@link #tick()} / {@link #writeTo} 是否成功。 */
    public static boolean lastWriteOk() {
        return lastWriteOk;
    }

    /**
     * 导出文件的绝对位置：{@code <游戏数据目录>/debug/state.json}。
     *
     * <p>用 {@link FileUtils#getFileHandle(String)} 而不是手拼路径——它读的是启动时
     * 由各平台 Launcher 注入的 {@code defaultPath}（桌面端拼自根 {@code build.gradle}
     * 的 {@code appName} 与 {@code appPackageName}，Android 是应用私有目录），
     * 手拼必然在某个平台上错，也会在产品改名后失效。
     */
    public static File stateFile() {
        return FileUtils.getFileHandle(EXPORT_DIR).child(EXPORT_FILE).file();
    }

    /** 把 JSON 写到指定文件（UTF-8，无 BOM）。失败时静默返回 false，绝不抛异常打断游戏。 */
    public static boolean writeTo(File target) {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        String json = buildJson();
        try (OutputStream os = new FileOutputStream(target);
             Writer w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            w.write(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // 内部工具
    // ---------------------------------------------------------------

    /**
     * 两个格位之间的欧氏距离，保留一位小数。
     *
     * <p>不做四舍五入到整数：斜向相邻的两格距离是 1.4 而不是 1，这个区别对
     * 「能不能一步走到」的判断有意义。保留一位小数是为了别把
     * {@code 1.4142135} 这种噪声塞进 JSON。
     */
    private static String distance(int a, int b, int width) {
        int ax = a % width;
        int ay = a / width;
        int bx = b % width;
        int by = b / width;
        int dx = ax - bx;
        int dy = ay - by;
        double d = Math.sqrt((double) (dx * dx + dy * dy));
        return String.format(java.util.Locale.ROOT, "%.1f", d);
    }

    private static String safeName(Object o) {
        try {
            if (o instanceof Char) {
                return ((Char) o).name();
            }
            if (o instanceof Item) {
                return ((Item) o).name();
            }
            return String.valueOf(o);
        } catch (Exception e) {
            return "?";
        }
    }

    /** 最小 JSON 字符串转义：引号、反斜杠、控制字符。 */
    private static String str(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder(s.length() + 8);
        b.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    b.append("\\\"");
                    break;
                case '\\':
                    b.append("\\\\");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                case '\r':
                    b.append("\\r");
                    break;
                case '\t':
                    b.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        b.append('"');
        return b.toString();
    }
}
