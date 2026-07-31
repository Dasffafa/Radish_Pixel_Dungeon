"""Embed a 16x16 pixel image into items.png sprite sheet at a given index."""
import sys
from PIL import Image

SHEET_PATH = "core/src/main/assets/sprites/items.png"
TILE = 16
COLS = 32

def main():
    if len(sys.argv) != 4:
        print(f"Usage: python {sys.argv[0]} <index> <16x16_image.png> <items.png>")
        print(f"  index: 0-1023, position in the 32x32 grid")
        sys.exit(1)

    index = int(sys.argv[1])
    src_path = sys.argv[2]
    sheet_path = sys.argv[3]

    col = index % COLS
    row = index // COLS
    px = col * TILE
    py = row * TILE

    sheet = Image.open(sheet_path).convert("RGBA")
    src = Image.open(src_path).convert("RGBA")

    if src.size != (TILE, TILE):
        print(f"Error: source image is {src.size}, expected ({TILE}, {TILE})")
        sys.exit(1)

    sheet.paste(src, (px, py))
    sheet.save(sheet_path)
    print(f"Embedded '{src_path}' at index {index} (col={col}, row={row}, px={px}, py={py}) -> '{sheet_path}'")

if __name__ == "__main__":
    main()
