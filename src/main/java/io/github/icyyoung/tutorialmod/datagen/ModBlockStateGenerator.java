package io.github.icyyoung.tutorialmod.datagen;


import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * @Description 自动生成方块（放下后）的实际贴图，包括状态、方向等信息
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModBlockStateGenerator extends BlockStateProvider {
    public ModBlockStateGenerator(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TutorialMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ModBlocks.BISMUTH_BLOCK.get());
        simpleBlock(ModBlocks.BISMUTH_ORE.get());
    }


}
