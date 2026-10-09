package dev.ubiquitouschests;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class ChestPiece extends StructurePiece {
    public ChestPiece(BlockPos pos) { super(UbiquitousChests.PIECE.get(), 0, new BoundingBox(pos)); }
    public ChestPiece(StructurePieceSerializationContext context, CompoundTag tag) { super(UbiquitousChests.PIECE.get(), tag); }
    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {}
    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox bounds, ChunkPos chunk, BlockPos pivot) {
        BlockPos pos = new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
        createChest(level, bounds, random, pos, UbiquitousChests.LOOT, Blocks.CHEST.defaultBlockState());
    }
}
