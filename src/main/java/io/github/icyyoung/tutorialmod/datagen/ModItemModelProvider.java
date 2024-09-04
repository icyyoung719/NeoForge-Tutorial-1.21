package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * @Description 自动生成item的贴图json
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TutorialMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //简单的item
        basicItem(ModItems.BISMUTH.get());
        basicItem(ModItems.RAW_BISMUTH.get());
        basicItem(ModItems.SAPPHIRE.get());

        //简单的block在物品栏中的模型
        withExistingParent(ModBlocks.BISMUTH_BLOCK.getRegisteredName(),modLoc("block/bismuth_block"));
        withExistingParent(ModBlocks.BISMUTH_ORE.getRegisteredName(),modLoc("block/bismuth_ore"));
    }
}
