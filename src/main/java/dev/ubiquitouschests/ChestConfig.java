package dev.ubiquitouschests;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public final class ChestConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue CHANCE;
    public static final ForgeConfigSpec.IntValue MIN_DEPTH, MAX_DEPTH, MIN_ROLLS, MAX_ROLLS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BIOMES, DIMENSIONS, ITEMS;
    static {
        var b = new ForgeConfigSpec.Builder();
        b.comment("随处可见的宝箱 / Ubiquitous Chests. 修改后重启游戏或服务器。仅影响新生成区块。", "Restart after editing. World generation changes affect new chunks only.").push("generation");
        ENABLED = b.comment("是否启用自然生成 / Enable natural generation").define("enabled", true);
        CHANCE = b.comment("每个区块尝试生成的概率，0~1；0.02 = 2%。地形不合适时跳过。", "Per-chunk attempt probability; unsuitable terrain is skipped.").defineInRange("chancePerChunk", 0.02, 0.0, 1.0);
        MIN_DEPTH = b.comment("距离地表的最小深度（含），海洋以海床为地表 / Minimum depth below terrain").defineInRange("minDepth", 8, 1, 2048);
        MAX_DEPTH = b.comment("距离地表的最大深度（含）；最小值大于最大值时自动交换 / Maximum depth").defineInRange("maxDepth", 48, 1, 2048);
        BIOMES = b.comment("禁止生成的群系 ID 或 #标签；同时检查地表和宝箱所在群系。空列表允许全部（包括模组群系）。", "Biome IDs or #tags; checks both surface and chest biomes.").defineListAllowEmpty("blockedBiomes", List.of(), v -> v instanceof String s && validId(s));
        DIMENSIONS = b.comment("禁止生成的维度 ID。默认不禁止任何维度；例如 minecraft:the_nether。", "Blocked dimension IDs. Empty allows all dimensions with suitable terrain.").defineListAllowEmpty("blockedDimensions", List.of(), v -> v instanceof String s && !s.startsWith("#") && validId(s));
        b.pop().push("loot");
        MIN_ROLLS = b.comment("每箱随机抽取次数下限 / Minimum weighted loot rolls").defineInRange("minRolls", 3, 0, 27);
        MAX_ROLLS = b.comment("每箱随机抽取次数上限；反向范围自动交换 / Maximum weighted loot rolls").defineInRange("maxRolls", 7, 0, 27);
        ITEMS = b.comment("格式：物品ID|权重|最小数量|最大数量。支持已安装模组的物品 ID。", "Format: item_id|weight|min_count|max_count. Weights 1..100000; counts 1..64.", "非法条目会跳过并在日志警告；空列表生成空箱。可重复抽中同种物品。")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot|30|2|8", "minecraft:gold_ingot|18|1|5", "minecraft:ender_pearl|8|1|3", "minecraft:obsidian|12|2|6", "minecraft:coal|20|4|12", "minecraft:bread|15|2|5", "minecraft:iron_pickaxe|5|1|1", "minecraft:iron_sword|5|1|1", "minecraft:iron_helmet|3|1|1", "minecraft:iron_chestplate|3|1|1", "minecraft:iron_leggings|3|1|1", "minecraft:iron_boots|3|1|1"), v -> v instanceof String);
        b.pop();
        SPEC = b.build();
    }
    private static boolean validId(String s) {
        return net.minecraft.resources.ResourceLocation.tryParse(s.startsWith("#") ? s.substring(1) : s) != null;
    }
}
