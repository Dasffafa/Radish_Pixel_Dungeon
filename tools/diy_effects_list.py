"""DIY 常驻效果清单（唯一数据源）。

- tools/gen_diy_effects.py 用它生成 items/DiyEffects.java（id ↔ Buff 类）
- tools/diy_sprites.py 用它生成 diy_effect_<id> 精粹贴图

收录标准（三个都要满足）：
1. **有真实消费点**：全仓库至少有一处在读这个 buff（速度、视野、护甲、充能……），
   否则挂上去就是“有图标没效果”——Haste、PhysicalEmpower、Mastery、MonkEnergy、
   CriticalAttack、BlobImmunity 这类已经被剔除。
2. **挂得住**：应用后 3 回合仍在（Healing、WellFed、RiftWindow 这类会自己 detach 的不要）。
3. 正面、有本地化名字、无参构造。

新增效果：加一行 (id, 类名)，然后跑
    python tools/gen_diy_effects.py
    python tools/diy_sprites.py
    ./gradlew generateRuntimeAtlasManifests
并跑一次 DIY_Workbench（场景里的探针会列出挂不住 / 无效果的效果）。
"""

EFFECTS = [
    # 稳定型：挂上就一直生效
    ("adrenaline", "Adrenaline"),
    ("blob_immunity", "BlobImmunity"),
    ("calm", "Calm"),
    ("critical_attack", "CriticalAttack"),
    ("enhanced_rings", "EnhancedRings"),
    ("foresight", "Foresight"),
    ("levitation", "Levitation"),
    ("magical_sight", "MagicalSight"),
    ("mastery", "Mastery"),
    ("mindvision", "MindVision"),
    ("monk_energy", "MonkEnergy"),
    ("physical_empower", "PhysicalEmpower"),
    ("poem", "PoemBuff"),
    ("revealed_area", "RevealedArea"),
    ("stamina", "Stamina"),
    ("wand_empower", "WandEmpower"),
    ("wheelchair_rush", "WheelchairRush"),

    # 临时/触发型：靠 DiyEffectsTracker 周期重挂（PULSE）
    ("healing", "Healing"),
    ("well_fed", "WellFed"),
    ("barkskin", "Barkskin"),
    ("arcane_armor", "ArcaneArmor"),
    ("earthen_armor", "EarthenArmorGuard"),
    ("fire_imbue", "FireImbue"),
    ("frost_imbue", "FrostImbue"),
    ("toxic_imbue", "ToxicImbue"),
    ("hold_fast", "HoldFast"),
    ("fury", "Fury"),
    ("adrenaline_surge", "AdrenalineSurge"),
    ("brace_yourself", "BraceYourself"),
    ("lethal_defense", "LethalDefense"),
    ("magic_soothe", "MagicSootheRegen"),
    ("sn_d_regen", "SnDRegeneration"),
    ("artifact_recharge", "ArtifactRecharge"),
]

# 专属机制效果（实现写在 Diy 里，不走 DiyEffects 的 buff 表，但同样需要精粹贴图）
SPECIAL_EFFECTS = [
    "heal_regen",
    "auto_identify",
    "poison_hit",
    "thorns",
    "ward",
    "wand_charge",
]
