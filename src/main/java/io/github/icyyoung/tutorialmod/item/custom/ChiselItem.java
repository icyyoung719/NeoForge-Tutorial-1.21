package io.github.icyyoung.tutorialmod.item.custom;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2024/10/31
 */

public class ChiselItem extends Item {
    private static final Map<Block,Block> CHISEL_MAP =
        Map.of(
                Blocks.STONE, Blocks.STONE_BRICKS,
                Blocks.END_STONE, Blocks.END_STONE_BRICKS,
                Blocks.DEEPSLATE, Blocks.DEEPSLATE_BRICKS,
                Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK,
                Blocks.IRON_BLOCK, Blocks.STONE,
                Blocks.NETHERRACK, ModBlocks.SAPPHIRE_BLOCK.get()
        );

    public ChiselItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Block clickedBlock = level.getBlockState(context.getClickedPos()).getBlock();
        if(CHISEL_MAP.containsKey(clickedBlock)) {
            if(!level.isClientSide()) {
                level.setBlockAndUpdate(context.getClickedPos(), CHISEL_MAP.get(clickedBlock).defaultBlockState());
                context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), context.getPlayer(),
                        item -> context.getPlayer().onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                level.playSound(null, context.getClickedPos(), ModSounds.CHISEL_USE.get(), SoundSource.BLOCKS);
            }
        }
        return InteractionResult.SUCCESS;
    }
}