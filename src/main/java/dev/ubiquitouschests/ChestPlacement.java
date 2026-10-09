package dev.ubiquitouschests;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.*;

/** Remains a random-spread placement so vanilla /locate can search it. */
public final class ChestPlacement extends RandomSpreadStructurePlacement {
    public static final Codec<ChestPlacement> CODEC = Codec.unit(ChestPlacement::new);
    public ChestPlacement() { super(1, 0, RandomSpreadType.LINEAR, 18372951); }
    // /locate only supplies a chunk position. Share the same coordinate hash with
    // generation so its X/Z points at the chest, while keeping positions varied.
    public static BlockPos chestColumn(ChunkPos chunk) {
        var random = RandomSource.create(chunk.toLong() ^ 0x61EBC47A912DL);
        return new BlockPos(chunk.getBlockX(2 + random.nextInt(12)), 0, chunk.getBlockZ(2 + random.nextInt(12)));
    }
    @Override
    public BlockPos getLocatePos(ChunkPos chunk) { return chestColumn(chunk); }
    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int x, int z) {
        if (!ChestConfig.ENABLED.get() || ChestConfig.CHANCE.get() <= 0) return false;
        var dimension = UbiquitousChests.DIMENSIONS.get(state.randomState());
        if (dimension == null || ChestConfig.DIMENSIONS.get().contains(dimension.toString())) return false;
        var random = new WorldgenRandom(new LegacyRandomSource(0));
        random.setLargeFeatureWithSalt(state.getLevelSeed(), x, z, salt());
        return random.nextDouble() < ChestConfig.CHANCE.get();
    }
    @Override
    public StructurePlacementType<?> type() { return UbiquitousChests.PLACEMENT.get(); }
}
