package io.github.icyyoung.tutorialmod.client.minimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;

public class MapDataManager {
    // Map of chunk coordinates to a 16x16 array of map colors.
    // Encoded as: map.get(chunkZ).get(chunkX)[x][z] = color;
    public static final Map<Long, int[]> chunkData = new HashMap<>();

    public static void clear() {
        chunkData.clear();
    }

    public static int[] getOrGenerateChunkData(Level level, int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        return chunkData.computeIfAbsent(key, k -> generateChunkData(level, chunkX, chunkZ));
    }

    public static int[] getChunkData(int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        return chunkData.get(key);
    }

    public static void updateChunk(Level level, int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        chunkData.put(key, generateChunkData(level, chunkX, chunkZ));
    }

    private static int[] generateChunkData(Level level, int chunkX, int chunkZ) {
        int[] data = new int[256];
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) {
            return null; // Return null if chunk not loaded
        }
        
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunkX << 4) + x;
                int worldZ = (chunkZ << 4) + z;
                int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                
                // Get the block state at the surface
                mutablePos.set(worldX, y - 1, worldZ);
                BlockState state = level.getBlockState(mutablePos);
                
                // Find a block with a valid map color by going down if necessary
                while (state.getMapColor(level, mutablePos) == MapColor.NONE && mutablePos.getY() > level.getMinBuildHeight()) {
                    mutablePos.move(net.minecraft.core.Direction.DOWN);
                    state = level.getBlockState(mutablePos);
                }
                
                MapColor color = state.getMapColor(level, mutablePos);
                data[x + (z * 16)] = color.col;
            }
        }
        return data;
    }
}