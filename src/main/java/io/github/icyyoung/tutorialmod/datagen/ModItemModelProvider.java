package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

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
        basicItem(ModItems.RAW_SAPPHIRE.get());
        basicItem(ModItems.METAL_DETECTOR.get());
        basicItem(ModItems.STRAWBERRY.get());
        basicItem(ModItems.PINE_CONE.get());

        //简单的block在物品栏中的模型
        withExistingParent(ModBlocks.BISMUTH_BLOCK.getRegisteredName(),modLoc("block/bismuth_block"));
        withExistingParent(ModBlocks.BISMUTH_ORE.getRegisteredName(),modLoc("block/bismuth_ore"));
        withExistingParent(ModBlocks.SAPPHIRE_BLOCK.getRegisteredName(),modLoc("block/sapphire_block"));
        withExistingParent(ModBlocks.SAPPHIRE_ORE.getRegisteredName(),modLoc("block/sapphire_ore"));
        withExistingParent(ModBlocks.RAW_SAPPHIRE_BLOCK.getRegisteredName(),modLoc("block/raw_sapphire_block"));
        withExistingParent(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.getRegisteredName(),modLoc("block/deepslate_sapphire_ore"));
        withExistingParent(ModBlocks.NETHER_SAPPHIRE_ORE.getRegisteredName(),modLoc("block/nether_sapphire_ore"));
        withExistingParent(ModBlocks.END_STONE_SAPPHIRE_ORE.getRegisteredName(),modLoc("block/end_stone_sapphire_ore"));
        withExistingParent(ModBlocks.SOUND_BLOCK.getRegisteredName(),modLoc("block/sound_block"));

        //fence button wall...

        simpleFenceItem(ModBlocks.SAPPHIRE_FENCE, ModBlocks.SAPPHIRE_BLOCK);
        simpleButtonItem(ModBlocks.SAPPHIRE_BUTTON, ModBlocks.SAPPHIRE_BLOCK);
        simpleWallItem(ModBlocks.SAPPHIRE_WALL, ModBlocks.SAPPHIRE_BLOCK);

        evenSimplerBlockItem(ModBlocks.SAPPHIRE_STAIRS);
        evenSimplerBlockItem(ModBlocks.SAPPHIRE_SLAB);
        evenSimplerBlockItem(ModBlocks.SAPPHIRE_PRESSURE_PLATE);
        evenSimplerBlockItem(ModBlocks.SAPPHIRE_FENCE_GATE);

        simpleTrapdoorItem(ModBlocks.SAPPHIRE_TRAPDOOR);
        simpleBlockItem(ModBlocks.SAPPHIRE_DOOR);
    }

    public void evenSimplerBlockItem(DeferredBlock<Block> block) {
        this.withExistingParent(block.getRegisteredName(),
                modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath()));
    }
    private ItemModelBuilder simpleBlockItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                mcLoc("item/generated")).texture("layer0",
                modLoc("item/" + item.getId().getPath()));
    }
    public void simpleTrapdoorItem(DeferredBlock<Block> block) {
        this.withExistingParent(block.getRegisteredName(),
                modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath())+ "_bottom");
    }

    public void simpleFenceItem(DeferredBlock<Block> block, DeferredBlock<Block> fullBlock) {
        withExistingParent(block.getRegisteredName(), mcLoc("block/fence_inventory"))
                .texture("texture", blockTexture(fullBlock));
    }

    public void simpleButtonItem(DeferredBlock<Block> block, DeferredBlock<Block> fullBlock) {
        withExistingParent(block.getRegisteredName(), mcLoc("block/button_inventory"))
                .texture("texture", blockTexture(fullBlock));
    }

    private void simpleWallItem(DeferredBlock<Block> block, DeferredBlock<Block> fullBlock) {
        withExistingParent(block.getRegisteredName(), mcLoc("block/wall_inventory"))
                .texture("wall", blockTexture(fullBlock));
    }


    //用来协助获取完整方块（相对于fence、button等）的texture地址
    private ResourceLocation blockTexture(DeferredBlock<Block> block) {
        return ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "block/" + block.getId().getPath());
    }
}
