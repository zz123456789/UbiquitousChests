package dev.ubiquitouschests.compat;

import dev.ubiquitouschests.LootSync;
import dev.ubiquitouschests.UbiquitousChests;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/** Client-only state; does not link optional mod classes unless both are present. */
@Mod.EventBusSubscriber(modid = UbiquitousChests.ID, value = Dist.CLIENT)
public final class ClientLoot {
    private static LootTable table;

    public static LootTable table() { return table; }

    public static void receive(String json) {
        com.mojang.logging.LogUtils.getLogger().debug("Received Ubiquitous Chests loot table ({} characters)", json.length());
        // Forge's pool deserializer requires a loot-table context. Mark this as
        // custom to avoid firing LootTableLoadEvent and replacing server data
        // with the client's different COMMON configuration.
        table = net.minecraftforge.common.ForgeHooks.loadLootTable(LootSync.GSON,
            UbiquitousChests.LOOT, com.google.gson.JsonParser.parseString(json), true);
        if (ModList.get().isLoaded("jei") && ModList.get().isLoaded("jeresources")) {
            JerPlugin.refresh();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        com.mojang.logging.LogUtils.getLogger().debug("Clearing Ubiquitous Chests client loot table on logout");
        table = null;
        if (ModList.get().isLoaded("jei") && ModList.get().isLoaded("jeresources")) {
            JerPlugin.clear();
        }
    }
}
