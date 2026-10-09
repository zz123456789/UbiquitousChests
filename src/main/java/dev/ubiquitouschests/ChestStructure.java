package dev.ubiquitouschests;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import java.util.Optional;
import java.util.List;

public final class ChestStructure extends Structure {
    public static final Codec<ChestStructure> CODEC = simpleCodec(ChestStructure::new);
    public ChestStructure(StructureSettings settings) { super(settings); }
    @Override
    public HolderSet<Biome> biomes() {
        var source = super.biomes();
        var blacklist = ChestConfig.BIOMES.get();
        return blacklist.isEmpty() ? source
            : HolderSet.direct(source.stream().filter(biome -> !blocked(biome, blacklist)).toList());
    }
    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        var dim = UbiquitousChests.DIMENSIONS.get(c.randomState());
        if (!ChestConfig.ENABLED.get() || dim == null || ChestConfig.DIMENSIONS.get().contains(dim.toString())) return Optional.empty();
        var columnPos = ChestPlacement.chestColumn(c.chunkPos());
        int x = columnPos.getX();
        int z = columnPos.getZ();
        int surface = c.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, c.heightAccessor(), c.randomState());
        int low = Math.min(ChestConfig.MIN_DEPTH.get(), ChestConfig.MAX_DEPTH.get());
        int high = Math.max(ChestConfig.MIN_DEPTH.get(), ChestConfig.MAX_DEPTH.get());
        int minY = Math.max(c.heightAccessor().getMinBuildHeight() + 5, surface - high);
        int maxY = Math.min(c.heightAccessor().getMaxBuildHeight() - 2, surface - low);
        if (minY > maxY || blocked(c, x, surface, z)) return Optional.empty();
        var column = c.chunkGenerator().getBaseColumn(x, z, c.heightAccessor(), c.randomState());
        // Randomize the starting height, then search only inside the configured interval.
        int count = maxY - minY + 1;
        int offset = c.random().nextInt(count);
        for (int i = 0; i < count; i++) {
            int y = minY + (offset + i) % count;
            var block = column.getBlock(y);
            if (!block.isSolid() || !block.getFluidState().isEmpty() || block.is(Blocks.BEDROCK)
                    || !column.getBlock(y + 1).isSolid() || !column.getBlock(y - 1).isSolid() || blocked(c, x, y, z)) continue;
            var pos = new BlockPos(x, y, z);
            return Optional.of(new GenerationStub(pos, builder -> builder.addPiece(new ChestPiece(pos))));
        }
        return Optional.empty();
    }
    private static boolean blocked(GenerationContext c, int x, int y, int z) {
        Holder<Biome> biome = c.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), c.randomState().sampler());
        return blocked(biome, ChestConfig.BIOMES.get());
    }
    private static boolean blocked(Holder<Biome> biome, List<? extends String> blacklist) {
        for (String entry : blacklist) {
            if (entry.startsWith("#")) {
                if (biome.is(TagKey.create(Registries.BIOME, new ResourceLocation(entry.substring(1))))) return true;
            } else if (biome.unwrapKey().map(key -> key.location().toString().equals(entry)).orElse(false)) return true;
        }
        return false;
    }
    @Override
    public StructureType<?> type() { return UbiquitousChests.STRUCTURE.get(); }
}
