package io.github.icyyoung.tutorialmod.block.custom;

import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * @Description 农作物草莓的定义类
 * @Author icyyoung
 * @Date 2024/9/11
 */

public class StrawberryCropBlock extends CropBlock {
    public static final int MAX_AGE=5;
    public static final IntegerProperty AGE= BlockStateProperties.AGE_5;

    public StrawberryCropBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.STRAWBERRY_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
