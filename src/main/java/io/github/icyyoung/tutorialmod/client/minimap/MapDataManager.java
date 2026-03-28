package io.github.icyyoung.tutorialmod.client.minimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapDataManager {
    // dimension -> chunk key -> 16x16 array of ARGB colors.
    public static final Map<String, Map<Long, int[]>> dimensionMapData = new ConcurrentHashMap<>();

    public static void clear() {
        dimensionMapData.clear();
    }

    public static Map<Long, int[]> getChunkDataMap(Level level) {
        if (level == null) return new HashMap<>();
        String dimKey = level.dimension().location().toString();
        return dimensionMapData.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());
    }

    public static Map<Long, int[]> getChunkDataMap(String dimKey) {
        return dimensionMapData.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());
    }

    public static int[] getOrGenerateChunkData(Level level, int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        Map<Long, int[]> map = getChunkDataMap(level);
        return map.computeIfAbsent(key, k -> generateChunkData(level, chunkX, chunkZ));
    }

    public static int[] getChunkData(Level level, int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        return getChunkDataMap(level).get(key);
    }

    public static void updateChunk(Level level, int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        int[] data = generateChunkData(level, chunkX, chunkZ);
        if (data != null) {
            getChunkDataMap(level).put(key, data);
        }
    }

    private static BlockState getCorrectStateForFluidBlock(Level level, BlockState state, BlockPos pos) {
        FluidState fluidstate = state.getFluidState();
        return !fluidstate.isEmpty() && !state.isFaceSturdy(level, pos, Direction.UP) ? fluidstate.createLegacyBlock() : state;
    }

    private static int[] generateChunkData(Level level, int chunkX, int chunkZ) {
        int[] data = new int[256];
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) {
            return null; // Return null if chunk not loaded
        }
        
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();
        
        for (int x = 0; x < 16; x++) {
            // We need d0 which is the height of the block to the north.
            // For z = 0, we check z = -1 (in the neighboring chunk).
            int worldX = (chunkX << 4) + x;
            int northWorldZ = (chunkZ << 4) - 1;
            int d0 = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, northWorldZ);
            
            for (int z = 0; z < 16; z++) {
                int worldZ = (chunkZ << 4) + z;
                int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
                
                BlockState blockstate;
                int fluidDepth = 0;
                
                if (y <= level.getMinBuildHeight() + 1) {
                    blockstate = Blocks.BEDROCK.defaultBlockState();
                } else {
                    do {
                        --y;
                        mutablePos.set(worldX, y, worldZ);
                        blockstate = chunk.getBlockState(mutablePos);
                    } while(blockstate.getMapColor(level, mutablePos) == MapColor.NONE && y > level.getMinBuildHeight());

                    if (y > level.getMinBuildHeight() && !blockstate.getFluidState().isEmpty()) {
                        int currentY = y - 1;
                        mutablePos2.set(mutablePos);
                        BlockState blockstate1;
                        do {
                            mutablePos2.setY(currentY--);
                            blockstate1 = chunk.getBlockState(mutablePos2);
                            ++fluidDepth;
                        } while(currentY > level.getMinBuildHeight() && !blockstate1.getFluidState().isEmpty());

                        blockstate = getCorrectStateForFluidBlock(level, blockstate, mutablePos);
                    }
                }
                
                double d1 = (double) y;
                MapColor mapcolor = blockstate.getMapColor(level, mutablePos);
                MapColor.Brightness brightness;

                if (mapcolor == MapColor.WATER) {
                    double d2 = (double)fluidDepth * 0.1 + (double)(worldX + worldZ & 1) * 0.2;
                    if (d2 < 0.5) {
                        brightness = MapColor.Brightness.HIGH;
                    } else if (d2 > 0.9) {
                        brightness = MapColor.Brightness.LOW;
                    } else {
                        brightness = MapColor.Brightness.NORMAL;
                    }
                } else {
                    double d3 = (d1 - d0) * 4.0 / 5.0 + ((double)(worldX + worldZ & 1) - 0.5) * 0.4;
                    if (d3 > 0.6) {
                        brightness = MapColor.Brightness.HIGH;
                    } else if (d3 < -0.6) {
                        brightness = MapColor.Brightness.LOW;
                    } else {
                        brightness = MapColor.Brightness.NORMAL;
                    }
                }
                
                d0 = (int) d1;
                
                if (mapcolor == MapColor.NONE) {
                    data[x + (z * 16)] = 0; // transparent
                } else {
                    // ARGB color; we set full alpha 0xFF000000 
                    int rgb = mapcolor.calculateRGBColor(brightness);
                    data[x + (z * 16)] = 0xFF000000 | rgb;
                }
            }
        }
        return data;
    }
}