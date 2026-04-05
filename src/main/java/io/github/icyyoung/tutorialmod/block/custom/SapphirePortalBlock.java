package io.github.icyyoung.tutorialmod.block.custom;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.world.ModDimensions;
import io.github.icyyoung.tutorialmod.world.portal.SapphirePortalShape;
import io.github.icyyoung.tutorialmod.world.portal.SapphirePortalTravel;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class SapphirePortalBlock extends NetherPortalBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DustParticleOptions BLUE_PORTAL_PARTICLE = new DustParticleOptions(new Vector3f(0.15F, 0.45F, 1.0F), 1.0F);

    public SapphirePortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Intentionally no random mob spawning from this portal.
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (SapphirePortalShape.isPortalConstructionInProgress()) {
            return state;
        }

        Direction.Axis axis = state.getValue(AXIS);
        if (direction.getAxis().isHorizontal() && !SapphirePortalShape.isPortalStillValid(level, pos, axis)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        var targetKey = level.dimension() == ModDimensions.SAPPHIRE_FLAT ? Level.OVERWORLD : ModDimensions.SAPPHIRE_FLAT;
        ServerLevel targetLevel = level.getServer().getLevel(targetKey);
        if (targetLevel == null) {
            LOGGER.warn("Target dimension {} is not loaded, keeping entity in current level", targetKey.location());
            return new DimensionTransition(
                    level,
                    entity.position(),
                    entity.getDeltaMovement(),
                    entity.getYRot(),
                    entity.getXRot(),
                    DimensionTransition.DO_NOTHING
            );
        }

        Direction.Axis axis = level.getBlockState(pos).hasProperty(AXIS)
                ? level.getBlockState(pos).getValue(AXIS)
                : Direction.Axis.X;

        double scale = DimensionType.getTeleportationScale(level.dimensionType(), targetLevel.dimensionType());
        BlockPos scaledTarget = targetLevel.getWorldBorder().clampToBounds(
                entity.getX() * scale,
                entity.getY(),
                entity.getZ() * scale
        );

        BlockPos exitPortal = SapphirePortalTravel.findOrCreateExitPortal(targetLevel, scaledTarget, axis);
        Vec3 arrival = SapphirePortalTravel.getArrivalPos(entity, exitPortal);
        return new DimensionTransition(
                targetLevel,
                arrival,
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND
        );
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    net.minecraft.sounds.SoundEvents.PORTAL_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.5F,
                    random.nextFloat() * 0.4F + 0.8F,
                    false
            );
        }

        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double vx = (random.nextDouble() - 0.5D) * 0.5D;
            double vy = (random.nextDouble() - 0.5D) * 0.5D;
            double vz = (random.nextDouble() - 0.5D) * 0.5D;
            level.addParticle(BLUE_PORTAL_PARTICLE, x, y, z, vx, vy, vz);
        }
    }
}
