package io.github.icyyoung.tutorialmod.world.portal;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class SapphirePortalShape {
    private static final int MIN_WIDTH = 2;
    private static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_HEIGHT = 21;
    private static final ThreadLocal<Integer> PORTAL_BUILD_DEPTH = ThreadLocal.withInitial(() -> 0);

    private final BlockPos bottomLeft;
    private final Direction.Axis axis;
    private final Direction widthDirection;
    private final int width;
    private final int height;

    private SapphirePortalShape(BlockPos bottomLeft, Direction.Axis axis, Direction widthDirection, int width, int height) {
        this.bottomLeft = bottomLeft;
        this.axis = axis;
        this.widthDirection = widthDirection;
        this.width = width;
        this.height = height;
    }

    public static boolean trySpawnPortal(LevelAccessor level, BlockPos interiorPos) {
        return findEmpty(level, interiorPos, Direction.Axis.X)
                .or(() -> findEmpty(level, interiorPos, Direction.Axis.Z))
                .map(shape -> {
                    shape.createPortalBlocks(level);
                    return true;
                })
                .orElse(false);
    }

    public static boolean isPortalConstructionInProgress() {
        return PORTAL_BUILD_DEPTH.get() > 0;
    }

    public static boolean isPortalStillValid(LevelAccessor level, BlockPos portalPos, Direction.Axis axis) {
        return findExisting(level, portalPos, axis).isPresent();
    }

    public static BlockPos createMinimalPortal(LevelAccessor level, BlockPos bottomLeftInterior, Direction.Axis axis) {
        Direction widthDir = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        beginPortalBuild();
        try {
            for (int x = -1; x <= 2; x++) {
                for (int y = -1; y <= 3; y++) {
                    BlockPos target = bottomLeftInterior.relative(widthDir, x).above(y);
                    boolean isFrame = x == -1 || x == 2 || y == -1 || y == 3;
                    if (isFrame) {
                        level.setBlock(target, ModBlocks.SAPPHIRE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        level.setBlock(target, ModBlocks.SAPPHIRE_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis), Block.UPDATE_ALL);
                    }
                }
            }
        } finally {
            endPortalBuild();
        }

        return bottomLeftInterior.above();
    }

    private static Optional<SapphirePortalShape> findEmpty(LevelAccessor level, BlockPos interiorPos, Direction.Axis axis) {
        Direction widthDir = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction leftDir = widthDir.getOpposite();

        BlockPos floorCandidate = interiorPos;
        while (floorCandidate.getY() > level.getMinBuildHeight() && isPortalContent(level, floorCandidate.below())) {
            floorCandidate = floorCandidate.below();
        }

        if (!isFrame(level, floorCandidate.below())) {
            return Optional.empty();
        }

        BlockPos bottomLeft = floorCandidate;
        int leftMoves = 0;
        while (leftMoves < MAX_WIDTH && isPortalContent(level, bottomLeft.relative(leftDir)) && isFrame(level, bottomLeft.relative(leftDir).below())) {
            bottomLeft = bottomLeft.relative(leftDir);
            leftMoves++;
        }

        if (!isFrame(level, bottomLeft.relative(leftDir))) {
            return Optional.empty();
        }

        int width = 0;
        while (width < MAX_WIDTH && isPortalContent(level, bottomLeft.relative(widthDir, width)) && isFrame(level, bottomLeft.relative(widthDir, width).below())) {
            width++;
        }

        if (width < MIN_WIDTH || !isFrame(level, bottomLeft.relative(widthDir, width))) {
            return Optional.empty();
        }

        int height = 0;
        for (int y = 0; y < MAX_HEIGHT; y++) {
            if (!isFrame(level, bottomLeft.relative(leftDir).above(y)) || !isFrame(level, bottomLeft.relative(widthDir, width).above(y))) {
                return Optional.empty();
            }

            for (int x = 0; x < width; x++) {
                if (!isPortalContent(level, bottomLeft.relative(widthDir, x).above(y))) {
                    if (y < MIN_HEIGHT) {
                        return Optional.empty();
                    }
                    if (hasTopFrame(level, bottomLeft, widthDir, width, y)) {
                        return Optional.of(new SapphirePortalShape(bottomLeft, axis, widthDir, width, y));
                    }
                    return Optional.empty();
                }
            }

            if (hasTopFrame(level, bottomLeft, widthDir, width, y + 1)) {
                height = y + 1;
                break;
            }
        }

        if (height < MIN_HEIGHT) {
            return Optional.empty();
        }

        return Optional.of(new SapphirePortalShape(bottomLeft, axis, widthDir, width, height));
    }

    private static Optional<SapphirePortalShape> findExisting(LevelAccessor level, BlockPos portalPos, Direction.Axis axis) {
        Direction widthDir = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction leftDir = widthDir.getOpposite();

        if (!isPortalBlock(level, portalPos)) {
            return Optional.empty();
        }

        BlockPos bottomLeft = portalPos;
        while (isPortalBlock(level, bottomLeft.below())) {
            bottomLeft = bottomLeft.below();
        }
        while (isPortalBlock(level, bottomLeft.relative(leftDir))) {
            bottomLeft = bottomLeft.relative(leftDir);
        }

        int width = 0;
        while (width < MAX_WIDTH && isPortalBlock(level, bottomLeft.relative(widthDir, width))) {
            width++;
        }

        if (width < MIN_WIDTH || !isFrame(level, bottomLeft.relative(leftDir)) || !isFrame(level, bottomLeft.relative(widthDir, width))) {
            return Optional.empty();
        }

        int height = 0;
        while (height < MAX_HEIGHT) {
            for (int x = 0; x < width; x++) {
                BlockPos interior = bottomLeft.relative(widthDir, x).above(height);
                if (!isPortalBlock(level, interior)) {
                    if (height < MIN_HEIGHT || !hasTopFrame(level, bottomLeft, widthDir, width, height)) {
                        return Optional.empty();
                    }
                    return Optional.of(new SapphirePortalShape(bottomLeft, axis, widthDir, width, height));
                }
            }

            if (!isFrame(level, bottomLeft.relative(leftDir).above(height)) || !isFrame(level, bottomLeft.relative(widthDir, width).above(height))) {
                return Optional.empty();
            }

            height++;
        }

        return Optional.empty();
    }

    private static boolean hasTopFrame(LevelAccessor level, BlockPos bottomLeft, Direction widthDir, int width, int topY) {
        for (int x = -1; x <= width; x++) {
            if (!isFrame(level, bottomLeft.relative(widthDir, x).above(topY))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPortalContent(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.FIRE) || state.is(ModBlocks.SAPPHIRE_PORTAL.get());
    }

    private static boolean isPortalBlock(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.SAPPHIRE_PORTAL.get());
    }

    private static boolean isFrame(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.SAPPHIRE_BLOCK.get());
    }

    private void createPortalBlocks(LevelAccessor level) {
        BlockState portalState = ModBlocks.SAPPHIRE_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        beginPortalBuild();
        try {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    level.setBlock(bottomLeft.relative(widthDirection, x).above(y), portalState, Block.UPDATE_ALL);
                }
            }
        } finally {
            endPortalBuild();
        }
    }

    private static void beginPortalBuild() {
        PORTAL_BUILD_DEPTH.set(PORTAL_BUILD_DEPTH.get() + 1);
    }

    private static void endPortalBuild() {
        int depth = PORTAL_BUILD_DEPTH.get() - 1;
        if (depth <= 0) {
            PORTAL_BUILD_DEPTH.remove();
        } else {
            PORTAL_BUILD_DEPTH.set(depth);
        }
    }
}
