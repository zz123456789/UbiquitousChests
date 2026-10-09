package dev.ubiquitouschests;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;

public final class ConfiguredLoot {
    public static void load(LootTableLoadEvent event) {
        if (!event.getName().equals(UbiquitousChests.LOOT)) return;
        int min = Math.min(ChestConfig.MIN_ROLLS.get(), ChestConfig.MAX_ROLLS.get());
        int max = Math.max(ChestConfig.MIN_ROLLS.get(), ChestConfig.MAX_ROLLS.get());
        var pool = LootPool.lootPool().setRolls(UniformGenerator.between(min, max));
        int valid = 0;
        for (String entry : ChestConfig.ITEMS.get()) {
            try {
                String[] fields = entry.split("\\|", -1);
                if (fields.length != 4) throw new IllegalArgumentException("expected item|weight|min|max");
                var id = new ResourceLocation(fields[0].trim());
                if (!BuiltInRegistries.ITEM.containsKey(id)) throw new IllegalArgumentException("unknown item " + id);
                var item = BuiltInRegistries.ITEM.get(id);
                if (item == Items.AIR) throw new IllegalArgumentException("air is not loot");
                int weight = Integer.parseInt(fields[1].trim());
                int from = Integer.parseInt(fields[2].trim());
                int to = Integer.parseInt(fields[3].trim());
                if (weight < 1 || weight > 100000 || from < 1 || to < from || to > 64) throw new IllegalArgumentException("invalid weight/count range");
                pool.add(LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(from, to))));
                valid++;
            } catch (RuntimeException e) {
                LogUtils.getLogger().warn("[Ubiquitous Chests] Skipping loot entry '{}': {}", entry, e.getMessage());
            }
        }
        var table = LootTable.lootTable();
        if (valid > 0) table.withPool(pool);
        event.setTable(table.build());
    }
}
