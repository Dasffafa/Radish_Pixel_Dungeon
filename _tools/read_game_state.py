#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""读取萝卜地牢的运行时状态导出（debug/state.json），供 AI / 调试脚本使用。

数据由 DebugStateExporter 在每个英雄回合收尾时写出，路径见 resolve_state_path()。

用法：
    python _tools/read_game_state.py            # 打印当前状态
    python _tools/read_game_state.py --raw      # 打印原始 JSON
    python _tools/read_game_state.py --watch 5  # 每 5 秒刷新一次（Ctrl+C 退出）

退出码：0 = 读到有效状态；1 = 文件不存在或内容不是有效状态（游戏没在运行 / 没进关卡）。
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import time
from pathlib import Path

# Windows 控制台默认用本地代码页（中文机器是 GBK），直接 print 中文会乱码；
# Python 3.7+ 的 reconfigure 可把流切到 UTF-8。失败就保持原样，不影响功能。
for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

# 与根 build.gradle 的 appName / appPackageName 对应。
# 桌面端 DesktopLauncher 拼的目录 = APPDATA\.<appPackageName 第二段>\<appName>\
APP_NAME = "Radish Pixel Dungeon"
APP_PACKAGE = "com.shatteredpixel.radishpixeldungeon"


def resolve_state_path() -> Path:

    """返回 state.json 的路径。

    优先读环境变量 RADISH_STATE_PATH（便于测试和多存档），否则按桌面端默认布局推导。
    顺序与游戏一致：先看 APPDATA（Windows），再看 XDG/HOME（Linux/macOS）。
    """
    override = os.environ.get("RADISH_STATE_PATH")
    if override:
        return Path(override)

    appdata = os.environ.get("APPDATA")
    if appdata:
        base = Path(appdata)
    else:
        base = Path.home() / ".local" / "share"

    vendor = "." + APP_PACKAGE.split(".")[1]
    return base / vendor / APP_NAME / "debug" / "state.json"


def load(path: Path) -> dict | None:
    """读并解析 JSON。文件缺失或内容无效时返回 None（不抛异常，便于轮询）。"""
    try:
        text = path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError):
        return None
    try:
        data = json.loads(text)
    except json.JSONDecodeError:
        return None
    if not isinstance(data, dict) or not data.get("ready"):
        return None
    return data


def format_state(d: dict) -> str:
    """把状态字典排成便于人读 / 便于 AI 消化的一段文本。"""
    hero = d.get("hero") or {}
    mobs = d.get("mobs") or []
    items = d.get("items") or []

    out = []
    out.append(
        f"层数 {d.get('depth')}  分支 {d.get('branch')}  "
        f"金币 {d.get('gold')}  充能 {d.get('energy')}"
    )
    out.append(
        f"地图 {d.get('levelWidth')}x{d.get('levelHeight')}  "
        f"英雄 位置 {hero.get('pos')}  "
        f"HP {hero.get('hp')}/{hero.get('ht')}  "
        f"等级 {hero.get('level')} 经验 {hero.get('exp')} 力量 {hero.get('str')}"
    )
    out.append(f"怪物 {len(mobs)} 个:")
    for m in sorted(mobs, key=lambda x: (x.get("dist") or 0)):
        out.append(
            f"  - {m.get('name')} 位置 {m.get('pos')} "
            f"HP {m.get('hp')}/{m.get('ht')} 距离 {m.get('dist')}"
        )
    out.append(f"背包 {len(items)} 件:")
    for it in items:
        out.append(f"  - {it.get('name')}")
    return "\n".join(out)


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description="读取萝卜地牢运行时状态导出")
    ap.add_argument("--raw", action="store_true", help="打印原始 JSON")
    ap.add_argument("--watch", type=float, metavar="SEC", help="每 SEC 秒刷新一次")
    ap.add_argument("--path", help="直接指定 state.json 路径（覆盖默认推导）")
    args = ap.parse_args(argv)

    path = Path(args.path) if args.path else resolve_state_path()

    def once() -> int:
        data = load(path)
        if data is None:
            print(f"[未就绪] 读不到有效状态：{path}", file=sys.stderr)
            print("  可能原因：游戏没启动 / 没进关卡 / 还没走一步。", file=sys.stderr)
            return 1
        if args.raw:
            print(json.dumps(data, ensure_ascii=False, indent=2))
        else:
            print(format_state(data))
        return 0

    if not args.watch:
        return once()

    try:
        while True:
            print("\x1b[2J\x1b[H", end="")  # 清屏
            print(f"# {path}")
            once()
            time.sleep(args.watch)
    except KeyboardInterrupt:
        return 0


if __name__ == "__main__":
    sys.exit(main())
