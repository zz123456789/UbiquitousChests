package dev.ubiquitouschests.compat;

import com.mojang.logging.LogUtils;
import dev.ubiquitouschests.UbiquitousChests;
import jeresources.jei.JEIConfig;
import jeresources.jei.dungeon.DungeonCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

/** Development-only end-to-end check with a separate server and real JEI/JER. */
@JeiPlugin
@Mod.EventBusSubscriber(modid = UbiquitousChests.ID, value = Dist.CLIENT)
public final class CompatSmokeTest implements IModPlugin {
    private static IJeiRuntime runtime;
    private static int ticks;
    private static int stage;
    private static int totalTicks;
    @Override
    public ResourceLocation getPluginUid() { return new ResourceLocation(UbiquitousChests.ID, "zz_smoke_test"); }
    @Override
    public void onRuntimeAvailable(IJeiRuntime value) { runtime = value; LogUtils.getLogger().info("COMPAT_RUNTIME: {}", value); }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || stage == 2) return;
        totalTicks++;
        if (Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.DisconnectedScreen && totalTicks % 100 == 0) {
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),
                Minecraft.getInstance(), net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25585"),
                new net.minecraft.client.multiplayer.ServerData("Local compatibility test", "127.0.0.1:25585", false), false);
        }
        if (totalTicks == 200) {
            var mc = Minecraft.getInstance();
            LogUtils.getLogger().info("COMPAT_STATUS: screen={}, runtime={}, table={}", mc.screen, runtime, ClientLoot.table());
            Screenshot.grab(mc.gameDirectory, "jer-status.png", mc.getMainRenderTarget(), message -> {});
        }
        if (totalTicks > 2400) throw new AssertionError("Compatibility test timed out");
        if (runtime == null || ClientLoot.table() == null) return;
        ticks++;
        if (stage == 1 && ticks == 80) {
            Screenshot.grab(Minecraft.getInstance().gameDirectory, "jer-compat.png", Minecraft.getInstance().getMainRenderTarget(), message -> LogUtils.getLogger().info("COMPAT_SCREENSHOT: {}", message.getString()));
        }
        if (ticks < 40) return;
        var recipes = runtime.getRecipeManager().createRecipeLookup(JEIConfig.DUNGEON_TYPE).get()
            .filter(r -> r.chest.getName().equals("jer.ubiquitous_chests.underground_chest")).toList();
        if (recipes.size() != 1) throw new AssertionError("Expected exactly one visible chest recipe, got " + recipes.size());
        var recipe = recipes.get(0);
        var expected = stage == 0 ? Items.DIAMOND : Items.EMERALD;
        var drop = recipe.chest.getChestDrop(expected.getDefaultInstance());
        if (stage == 1 && drop == null) return; // Wait for a real server /reload.
        int count = stage == 0 ? 4 : 2;
        if (drop == null || drop.minDrop != count || drop.maxDrop != count || drop.chance != 1.0F
                || recipe.chest.getMinStacks() != 1 || recipe.chest.getMaxStacks() != 1
                || recipe.amountOfItems(null) != 1) throw new AssertionError("Wrong synced loot data");
        if (stage == 0) {
            LogUtils.getLogger().info("COMPAT_PASS_INITIAL: real JER page has diamond x4, 100%, one roll, one entry; client local config differs from server");
            runtime.getRecipesGui().showRecipes(new DungeonCategory(), List.of(recipe), List.of());
            stage = 1;
        } else {
            LogUtils.getLogger().info("COMPAT_PASS_RELOAD: real JER page updated to emerald x2 with no duplicate visible entries");
            stage = 2;
            Minecraft.getInstance().stop();
        }
    }
}
