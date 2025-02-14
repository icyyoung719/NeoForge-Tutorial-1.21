package io.github.icyyoung.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/14
 */

public class ModSaplingBlock extends SaplingBlock {
    private final Supplier<Block> blockToSurviveOn;

    public ModSaplingBlock(TreeGrower treeGrower, Properties properties, Supplier<Block> blockToSurviveOn) {
        super(treeGrower, properties);
        this.blockToSurviveOn = blockToSurviveOn;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return blockToSurviveOn.get() == state.getBlock();
    }
}
