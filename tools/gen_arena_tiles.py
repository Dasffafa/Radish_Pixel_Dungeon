"""Generate core/src/main/assets/environment/tiles_arena.png.

Source: tiles_moss.png (SPD moss tileset, 256x256, 16x16 frames).
We repaint floor frames (row 0) and wall frames (rows 3..6 and 12..15)
to a clean stone / TFT-ish look while keeping the original silhouettes.
"""

from PIL import Image

REPO = r"E:\work-2\Radish_Pixel_Dungeon"
SRC = REPO + r"\core\src\main\assets\environment\tiles_moss.png"
DST = REPO + r"\core\src\main\assets\environment\tiles_arena.png"

TILE = 16
COLS = 16

img = Image.open(SRC).convert("RGBA")
px = img.load()

# ---------- palettes ----------
FLOOR_PLAYER = (205, 194, 168, 255)
FLOOR_PLAYER_SEAM = (176, 165, 140, 255)
FLOOR_ENEMY = (84, 84, 92, 255)
FLOOR_ENEMY_SEAM = (66, 66, 74, 255)
FLOOR_MID = (104, 100, 92, 255)
FLOOR_MID_SEAM = (86, 82, 76, 255)
FLOOR_TEX_LIGHT = 6

WALL_BODY = (69, 74, 83, 255)
WALL_BODY_DARK = (56, 60, 68, 255)
WALL_CAP = (104, 111, 122, 255)
WALL_CAP_DARK = (88, 94, 104, 255)
WALL_MORTAR = (36, 40, 46, 255)
WALL_OUTLINE = (20, 22, 27, 255)
WALL_SHADOW = (30, 33, 39, 255)


def frame_xy(frame):
    return (frame % COLS) * TILE, (frame // COLS) * TILE


def alpha_mask(x0, y0):
    return [[px[x0 + x, y0 + y][3] > 8 for x in range(TILE)] for y in range(TILE)]


def paint_floor(frame, base, seam, tex=6):
    x0, y0 = frame_xy(frame)
    for y in range(TILE):
        for x in range(TILE):
            px[x0 + x, y0 + y] = base
    # seams: right + bottom
    for x in range(TILE):
        px[x0 + x, y0 + TILE - 1] = seam
    for y in range(TILE):
        px[x0 + TILE - 1, y0 + y] = seam
    # deterministic little speckles
    seed = frame * 977 + 13
    for i in range(10):
        seed = (seed * 1103515245 + 12345) & 0x7FFFFFFF
        sx = 1 + seed % 14
        seed = (seed * 1103515245 + 12345) & 0x7FFFFFFF
        sy = 1 + seed % 14
        r, g, b, a = px[x0 + sx, y0 + sy]
        if i % 3 == 0:
            px[x0 + sx, y0 + sy] = (min(255, r + tex), min(255, g + tex), min(255, b + tex), 255)
        else:
            px[x0 + sx, y0 + sy] = (max(0, r - tex), max(0, g - tex), max(0, b - tex), 255)


def paint_wall(frame, overhang=False):
    x0, y0 = frame_xy(frame)
    mask = alpha_mask(x0, y0)
    if not any(any(row) for row in mask):
        return
    # 1) body + top cap
    for y in range(TILE):
        for x in range(TILE):
            if not mask[y][x]:
                px[x0 + x, y0 + y] = (0, 0, 0, 0)
                continue
            if y <= 1:
                col = WALL_CAP
            elif y == 3:
                col = WALL_CAP
            elif y == 2:
                col = WALL_CAP_DARK
            elif y == TILE - 2:
                col = WALL_SHADOW
            else:
                col = WALL_BODY
            px[x0 + x, y0 + y] = col

    # 2) brick mortar
    for x in range(TILE):
        if mask[7][x]:
            px[x0 + x, y0 + 7] = WALL_MORTAR
        if mask[15][x]:
            px[x0 + x, y0 + 15] = WALL_MORTAR
    offset = 0 if (frame % 2 == 0) else 8
    for i in range(2):
        jx = (offset + i * 8) % TILE
        for y in range(3, 7):
            if mask[y][jx]:
                px[x0 + jx, y0 + y] = WALL_MORTAR
        for y in range(8, 15):
            if mask[y][jx]:
                px[x0 + jx, y0 + y] = WALL_MORTAR

    # 3) crisp silhouette outline (any opaque pixel touching transparency)
    edges = []
    for y in range(TILE):
        for x in range(TILE):
            if not mask[y][x]:
                continue
            left = x == 0 or not mask[y][x - 1]
            right = x == TILE - 1 or not mask[y][x + 1]
            up = y == 0 or not mask[y - 1][x]
            down = y == TILE - 1 or not mask[y + 1][x]
            if left or right or up or down:
                edges.append((x, y))
    for x, y in edges:
        px[x0 + x, y0 + y] = WALL_OUTLINE

    # 4) warm inner highlight under the cap (very subtle)
    if not overhang:
        for x in range(TILE):
            if mask[4][x] and px[x0 + x, y0 + 4] == WALL_BODY:
                px[x0 + x, y0 + 4] = WALL_CAP_DARK


# ---------- floors ----------
paint_floor(4, FLOOR_PLAYER, FLOOR_PLAYER_SEAM)
paint_floor(10, FLOOR_PLAYER, FLOOR_PLAYER_SEAM)
paint_floor(1, FLOOR_ENEMY, FLOOR_ENEMY_SEAM)
paint_floor(7, FLOOR_ENEMY, FLOOR_ENEMY_SEAM)
for f in (0, 6, 12):
    paint_floor(f, FLOOR_MID, FLOOR_MID_SEAM)

# ---------- walls ----------
for row in (3, 4, 5, 6):
    for col in range(COLS):
        paint_wall(row * COLS + col)
for row in (12, 13, 14, 15):
    for col in range(COLS):
        paint_wall(row * COLS + col, overhang=True)


# ---------- kill any leftover moss green anywhere ----------
for y in range(img.height):
    for x in range(img.width):
        r, g, b, a = px[x, y]
        if a <= 8:
            continue
        if g > r * 1.03 and g > b * 1.03:
            lum = 0.3 * r + 0.6 * g + 0.1 * b
            px[x, y] = (int(min(255, lum * 0.80)), int(min(255, lum * 0.86)),
                        int(min(255, lum * 1.00)), a)

img.save(DST)
print("saved", DST)
