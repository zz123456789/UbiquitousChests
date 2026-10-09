package dev.ubiquitouschests;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Mod(UbiquitousChests.ID)
public final class UbiquitousChests {
    public static final String ID = "ubiquitous_chests";
    public static final ResourceLocation LOOT = new ResourceLocation(ID, "chests/underground_chest");
    public static final Map<RandomState, ResourceLocation> DIMENSIONS = new ConcurrentHashMap<>();
    private static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE_TYPE, ID);
    private static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, ID);
    private static final DeferredRegister<StructurePlacementType<?>> PLACEMENTS = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, ID);
    public static final RegistryObject<StructureType<ChestStructure>> STRUCTURE = STRUCTURES.register("underground_chest", () -> () -> ChestStructure.CODEC);
    public static final RegistryObject<StructurePieceType> PIECE = PIECES.register("chest", () -> ChestPiece::new);
    public static final RegistryObject<StructurePlacementType<ChestPlacement>> PLACEMENT = PLACEMENTS.register("configured", () -> () -> ChestPlacement.CODEC);

    public UbiquitousChests() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        STRUCTURES.register(bus);
        PIECES.register(bus);
        PLACEMENTS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ChestConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(this::levelLoad);
        MinecraftForge.EVENT_BUS.addListener(this::levelUnload);
        MinecraftForge.EVENT_BUS.addListener(ConfiguredLoot::load);
        LootSync.register();
        MinecraftForge.EVENT_BUS.addListener(LootSync::sync);
        MinecraftForge.EVENT_BUS.addListener(LootSync::login);
    }

    private void levelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            DIMENSIONS.put(level.getChunkSource().randomState(), level.dimension().location());
        }
    }

    private void levelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) DIMENSIONS.remove(level.getChunkSource().randomState());
    }
}
