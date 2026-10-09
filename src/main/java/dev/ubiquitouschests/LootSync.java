package dev.ubiquitouschests;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import dev.ubiquitouschests.compat.ClientLoot;

/** The server's loaded table is authoritative, including datapack changes. */
public final class LootSync {
    public static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private static final int MAX_JSON = 1_000_000;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(UbiquitousChests.ID, "loot"), () -> "1", "1"::equals, "1"::equals);

    public static void register() {
        CHANNEL.messageBuilder(TablePacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((packet, buffer) -> buffer.writeUtf(packet.json(), MAX_JSON))
            .decoder(buffer -> new TablePacket(buffer.readUtf(MAX_JSON)))
            .consumerMainThread((packet, context) -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientLoot.receive(packet.json()));
                context.get().setPacketHandled(true);
            }).add();
    }

    public static void sync(OnDatapackSyncEvent event) {
        // The per-player datapack event precedes the vanilla login packet. Send
        // initial data after login instead, so client level initialization cannot clear it.
        if (event.getPlayer() == null) CHANNEL.send(PacketDistributor.ALL.noArg(), packet(event.getPlayerList().getServer()));
    }

    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet(player.server));
        }
    }

    private static TablePacket packet(MinecraftServer server) {
        var table = server.getLootData().getLootTable(UbiquitousChests.LOOT);
        com.mojang.logging.LogUtils.getLogger().debug("Synchronizing Ubiquitous Chests loot table");
        return new TablePacket(GSON.toJson(table, LootTable.class));
    }

    private record TablePacket(String json) {}
}
