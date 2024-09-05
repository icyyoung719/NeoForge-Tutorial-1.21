package io.github.icyyoung.tutorialmod.item.custom;

import io.github.icyyoung.tutorialmod.util.ModTags;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @Description MetalDector模板类
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class MetalDetectorItem extends Item {
    public MetalDetectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(!pContext.getLevel().isClientSide){
            BlockPos positionClicked = pContext.getClickedPos();
            Player player = pContext.getPlayer();
            boolean foundBlock = false;

            for(int i = 0; i < positionClicked.getY()+64; i++){
                BlockState state = pContext.getLevel().getBlockState(positionClicked.below(i));

                if(isValuableBlock(state)){

                    outputValuableCoordinates(player, positionClicked.below(i), state.getBlock());
                    foundBlock = true;

                    break;
                }
            }

            if(!foundBlock) {
                player.sendSystemMessage(Component.literal("No valuables Found!"));
            }
        }
        pContext.getItemInHand().hurtAndBreak(1, pContext.getPlayer(),
                EquipmentSlot.MAINHAND);

        return InteractionResult.SUCCESS;
    }


    private void outputValuableCoordinates(Player player, BlockPos blockPos, Block block) {
        player.sendSystemMessage(Component.literal("Found "+ I18n.get(block.getDescriptionId()) +" at " +
                blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()));
    }

    private boolean isValuableBlock(BlockState state) {
        return state.is(ModTags.Blocks.METAL_DETECTOR_VALUABLES);
    }

}
