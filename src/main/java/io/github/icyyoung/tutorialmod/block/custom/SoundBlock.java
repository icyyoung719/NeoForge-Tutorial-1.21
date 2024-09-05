package io.github.icyyoung.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * @Description SoundBlock的模板类
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class SoundBlock extends Block {
    public SoundBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                        Player player, BlockHitResult blockHitResult) {
        level.playSound(player, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 1.0f, 1.0f);

        return InteractionResult.SUCCESS;
    }
}
