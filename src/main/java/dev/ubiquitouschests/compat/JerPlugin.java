package dev.ubiquitouschests.compat;

import dev.ubiquitouschests.UbiquitousChests;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/** JEI discovers this only when installed; JER integration is further isolated. */
@JeiPlugin
public final class JerPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() { return new ResourceLocation(UbiquitousChests.ID, "jer_loot"); }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (ModList.get().isLoaded("jeresources")) JerRecipes.register(registration);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (ModList.get().isLoaded("jeresources")) JerRecipes.runtimeAvailable(runtime);
    }

    @Override
    public void onRuntimeUnavailable() {
        if (ModList.get().isLoaded("jeresources")) JerRecipes.runtimeUnavailable();
    }

    public static void refresh() { JerRecipes.refresh(); }
    public static void clear() { JerRecipes.clear(); }
}
