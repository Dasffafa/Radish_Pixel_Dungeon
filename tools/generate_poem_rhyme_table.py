#!/usr/bin/env python3
"""Generate the Chinese rhyme table used by Moonlight's Poem toy.

Only Chinese needs a maintained table because Han characters do not contain
explicit pronunciation data. Other locales should read localized item/monster
names through game code (Messages.java) and compare their first letter/digit at
runtime.

The script reads Chinese message bundles, extracts gameplay equipment and
monster names, uses pypinyin to compute the final of each name's last Chinese
character, and writes:

    core/src/main/assets/rhyme/poem_rhyme_table_zh.json

Run from the repository root:
    python3 tools/generate_poem_rhyme_table.py
"""

from __future__ import annotations

import argparse
import json
import re
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable

try:
    from pypinyin import Style, lazy_pinyin
except ImportError as exc:  # pragma: no cover - exercised only when dependency is absent
    raise SystemExit(
        "pypinyin is required. Install it with: python3 -m pip install pypinyin"
    ) from exc

ITEMS_ZH = Path("core/src/main/assets/messages/items/items_zh.properties")
ACTORS_ZH = Path("core/src/main/assets/messages/actors/actors_zh.properties")
DEFAULT_OUTPUT = Path("core/src/main/assets/rhyme/poem_rhyme_table_zh.json")

CJK_RE = re.compile(r"[\u4e00-\u9fff]")
FORMAT_RE = re.compile(r"%\d*\$?[sdif]|%%")


def decode_properties_value(value: str) -> str:
    """Decode the subset of Java .properties escaping used by message names."""
    value = value.rstrip("\n\r")
    out: list[str] = []
    i = 0
    while i < len(value):
        ch = value[i]
        if ch != "\\" or i + 1 >= len(value):
            out.append(ch)
            i += 1
            continue

        nxt = value[i + 1]
        if nxt == "u" and i + 5 < len(value):
            hex_part = value[i + 2 : i + 6]
            try:
                out.append(chr(int(hex_part, 16)))
                i += 6
                continue
            except ValueError:
                pass
        escapes = {"t": "\t", "n": "\n", "r": "\r", "f": "\f"}
        out.append(escapes.get(nxt, nxt))
        i += 2
    return "".join(out).strip()


def parse_properties(path: Path) -> dict[str, str]:
    entries: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or line.startswith("!"):
            continue
        sep_positions = [pos for pos in (line.find("="), line.find(":")) if pos != -1]
        if not sep_positions:
            continue
        sep = min(sep_positions)
        key = line[:sep].strip()
        entries[key] = decode_properties_value(line[sep + 1 :])
    return entries


def clean_name(name: str) -> str:
    name = FORMAT_RE.sub("", name)
    return name.replace("_", "").replace("\ufeff", "").strip()


def last_chinese_char(text: str) -> str | None:
    matches = CJK_RE.findall(text)
    return matches[-1] if matches else None


def first_letter_or_digit(text: str) -> str | None:
    for ch in text:
        if ch.isalnum():
            return ch.lower()
    return None


def chinese_final(ch: str) -> str | None:
    finals = lazy_pinyin(ch, style=Style.FINALS, strict=False, errors="ignore")
    return finals[0] if finals else None


def include_equipment_key(key: str, include_inner: bool) -> bool:
    if not key.endswith(".name"):
        return False
    if "$" in key and not include_inner:
        return False
    if key.startswith("items.weapon.curses.") or key.startswith("items.weapon.enchantments."):
        return False
    if key.startswith("items.armor.curses.") or key.startswith("items.armor.glyphs."):
        return False
    return key.startswith("items.weapon.") or key.startswith("items.armor.")


def include_monster_key(key: str, include_npcs: bool, include_inner: bool) -> bool:
    if not key.startswith("actors.mobs.") or not key.endswith(".name"):
        return False
    if "$" in key and not include_inner:
        return False
    if key.startswith("actors.mobs.npcs.") and not include_npcs:
        return False
    return True


def build_entry(kind: str, key: str, name: str) -> dict[str, str | None]:
    name = clean_name(name)
    ch = last_chinese_char(name)
    return {
        "kind": kind,
        "key": key,
        "name": name,
        "last_chinese": ch,
        "final": chinese_final(ch) if ch else None,
        "initial": first_letter_or_digit(name),
    }


def sort_entries(entries: Iterable[dict[str, str | None]]) -> list[dict[str, str | None]]:
    return sorted(entries, key=lambda e: (str(e.get("final") or ""), str(e.get("key") or "")))


def build_table(include_npcs: bool, include_inner: bool) -> dict:
    items = parse_properties(ITEMS_ZH)
    actors = parse_properties(ACTORS_ZH)

    equipment = [
        build_entry("equipment", key, value)
        for key, value in items.items()
        if include_equipment_key(key, include_inner)
    ]
    monsters = [
        build_entry("monster", key, value)
        for key, value in actors.items()
        if include_monster_key(key, include_npcs, include_inner)
    ]

    by_final: dict[str, dict[str, list[dict[str, str | None]]]] = defaultdict(lambda: {"equipment": [], "monsters": []})
    unmatched = {"equipment": [], "monsters": []}

    for entry in equipment:
        if entry["final"]:
            by_final[str(entry["final"])]["equipment"].append(entry)
        else:
            unmatched["equipment"].append(entry)
    for entry in monsters:
        if entry["final"]:
            by_final[str(entry["final"])]["monsters"].append(entry)
        else:
            unmatched["monsters"].append(entry)

    groups = []
    for final in sorted(by_final):
        group = by_final[final]
        groups.append({
            "bucket": final,
            "final": final,
            "equipment": sort_entries(group["equipment"]),
            "monsters": sort_entries(group["monsters"]),
        })

    return {
        "schema": 2,
        "locale": "zh",
        "match_mode": "final",
        "generated_at": datetime.now(timezone.utc).replace(microsecond=0).isoformat(),
        "generator": "tools/generate_poem_rhyme_table.py",
        "source_files": {
            "items": str(ITEMS_ZH),
            "actors": str(ACTORS_ZH),
        },
        "rules": {
            "chinese": "Use pypinyin Style.FINALS on the last Chinese character of each localized name.",
            "other_locales": "Do not generate tables. Game code should read localized names through Messages.java and compare first letter/digit at runtime.",
            "excluded_by_default": [
                "weapon curses",
                "weapon enchantments",
                "armor curses",
                "armor glyphs",
                "actors.mobs.npcs.*",
                "keys containing '$'",
            ],
        },
        "counts": {
            "equipment": len(equipment),
            "monsters": len(monsters),
            "groups": len(groups),
            "unmatched_equipment": len(unmatched["equipment"]),
            "unmatched_monsters": len(unmatched["monsters"]),
        },
        "groups": groups,
        "unmatched": {
            "equipment": sort_entries(unmatched["equipment"]),
            "monsters": sort_entries(unmatched["monsters"]),
        },
    }


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--include-npcs", action="store_true", help="include actors.mobs.npcs.* names")
    parser.add_argument("--include-inner", action="store_true", help="include keys containing '$'")
    parser.add_argument("--compact", action="store_true", help="write compact JSON")
    args = parser.parse_args(argv)

    payload = build_table(args.include_npcs, args.include_inner)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    indent = None if args.compact else 2
    args.output.write_text(json.dumps(payload, ensure_ascii=False, indent=indent, sort_keys=False) + "\n", encoding="utf-8")

    print(f"wrote {args.output}")
    print(
        f"locale=zh mode=final equipment={payload['counts']['equipment']} "
        f"monsters={payload['counts']['monsters']} groups={payload['counts']['groups']}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
