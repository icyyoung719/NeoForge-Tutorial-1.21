package io.github.icyyoung.tutorialmod.world.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class SapphirePortalTravel {
    private static final int SEARCH_RADIUS = 128;

    private SapphirePortalTravel() {
    }

    public static BlockPos findOrCreateExitPortal(ServerLevel targetLevel, BlockPos preferred, Direction.Axis axis) {
        BlockPos found = findNearbyPortal(targetLevel, preferred);
        if (found != null) {
            return found;
        }

        BlockPos clamped = targetLevel.getWorldBorder().clampToBounds(preferred);
        int surfaceY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, clamped.getX(), clamped.getZ());
        int y = Math.max(targetLevel.getMinBuildHeight() + 2, Math.min(surfaceY + 1, targetLevel.getMaxBuildHeight() - 5));

        BlockPos interiorBottomLeft = new BlockPos(clamped.getX(), y, clamped.getZ());
        return SapphirePortalShape.createMinimalPortal(targetLevel, interiorBottomLeft, axis);
    }

    public static Vec3 getArrivalPos(Entity entity, BlockPos portalBlockPos) {
        // Keep entity feet centered inside portal to reduce accidental suffocation on arrival.
        return new Vec3(portalBlockPos.getX() + 0.5D, portalBlockPos.getY() + 0.1D, portalBlockPos.getZ() + 0.5D);
    }

    private static BlockPos findNearbyPortal(ServerLevel level, BlockPos center) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        BlockPos best = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos candidate = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    BlockState state = level.getBlockState(candidate);
                    if (!state.is(io.github.icyyoung.tutorialmod.block.ModBlocks.SAPPHIRE_PORTAL.get())) {
                        continue;
                    }

                    double distanceSq = candidate.distSqr(center);
                    if (distanceSq < bestDistanceSq) {
                        bestDistanceSq = distanceSq;
                        best = candidate;
                    }
                }
            }
        }

        return best;
    }
}
