package dev.ubiquitouschests.compat;

import jeresources.entry.DungeonEntry;
import jeresources.jei.JEIConfig;
import jeresources.jei.dungeon.DungeonWrapper;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.level.storage.loot.LootTable;
import java.util.List;

/** Uses JER's existing dungeon category, item indexing and probability tooltips. */
final class JerRecipes {
    private static IJeiRuntime runtime;
    private static DungeonWrapper current;
    private static LootTable displayed;

    static void register(IRecipeRegistration registration) {
        runtime = null;
        current = null;
        displayed = null;
        if (ClientLoot.table() != null) {
            displayed = ClientLoot.table();
            current = recipe(displayed);
            registration.addRecipes(JEIConfig.DUNGEON_TYPE, List.of(current));
        }
    }

    private static DungeonWrapper recipe(LootTable table) {
        return new DungeonWrapper(new DungeonEntry("jer.ubiquitous_chests.underground_chest", table));
    }

    static void runtimeAvailable(IJeiRuntime value) {
        com.mojang.logging.LogUtils.getLogger().debug("JER runtime available; server loot received: {}", ClientLoot.table() != null);
        runtime = value;
        refresh();
    }

    static void runtimeUnavailable() {
        runtime = null;
        current = null;
        displayed = null;
    }

    static void refresh() {
        if (runtime == null || ClientLoot.table() == null || displayed == ClientLoot.table()) return;
        clear();
        displayed = ClientLoot.table();
        current = recipe(displayed);
        runtime.getRecipeManager().addRecipes(JEIConfig.DUNGEON_TYPE, List.of(current));
    }

    static void clear() {
        if (runtime != null && current != null) {
            runtime.getRecipeManager().hideRecipes(JEIConfig.DUNGEON_TYPE, List.of(current));
        }
        current = null;
        displayed = null;
    }
}
