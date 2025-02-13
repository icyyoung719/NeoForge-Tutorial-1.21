package io.github.icyyoung.tutorialmod.datagen;


import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.block.custom.CornCropBlock;
import io.github.icyyoung.tutorialmod.block.custom.StrawberryCropBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Function;

/**
 * @Description 自动生成方块（放下后）的实际贴图，包括状态、方向等信息
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TutorialMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(ModBlocks.BISMUTH_BLOCK.get());
        simpleBlock(ModBlocks.BISMUTH_ORE.get());
        simpleBlock(ModBlocks.RAW_SAPPHIRE_BLOCK.get());
        simpleBlock(ModBlocks.SAPPHIRE_BLOCK.get());
        simpleBlock(ModBlocks.SAPPHIRE_ORE.get());
        simpleBlock(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get());
        simpleBlock(ModBlocks.NETHER_SAPPHIRE_ORE.get());
        simpleBlock(ModBlocks.END_STONE_SAPPHIRE_ORE.get());

        simpleBlock(ModBlocks.SOUND_BLOCK.get());

        stairsBlock(((StairBlock) ModBlocks.SAPPHIRE_STAIRS.get()),blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));
        slabBlock(((SlabBlock) ModBlocks.SAPPHIRE_SLAB.get()),blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()), blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));
        buttonBlock(((ButtonBlock) ModBlocks.SAPPHIRE_BUTTON.get()), blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));
        pressurePlateBlock(((PressurePlateBlock) ModBlocks.SAPPHIRE_PRESSURE_PLATE.get()), blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));
        wallBlock(((WallBlock) ModBlocks.SAPPHIRE_WALL.get()),blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));
        fenceBlock(((FenceBlock) ModBlocks.SAPPHIRE_FENCE.get()),blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));
        fenceGateBlock(((FenceGateBlock) ModBlocks.SAPPHIRE_FENCE_GATE.get()),blockTexture(ModBlocks.SAPPHIRE_BLOCK.get()));

        doorBlock(((DoorBlock) ModBlocks.SAPPHIRE_DOOR.get()),modLoc("block/sapphire_door_top"), modLoc("block/sapphire_door_bottom"));
        trapdoorBlockWithRenderType(((TrapDoorBlock) ModBlocks.SAPPHIRE_TRAPDOOR.get()), modLoc("block/sapphire_trapdoor"), true, "cutout");

        //crops
        makeStrawberryCrop((CropBlock) ModBlocks.STRAWBERRY_CROP.get(), "strawberry_stage", "strawberry_stage");
        makeCornCrop(((CropBlock) ModBlocks.CORN_CROP.get()),"corn_stage_","corn_stage_");
        //flower
        simpleBlockWithItem(ModBlocks.CATMINT.get(), models().cross(blockTexture(ModBlocks.CATMINT.get()).getPath(),
                blockTexture(ModBlocks.CATMINT.get())).renderType("cutout"));
        simpleBlockWithItem(ModBlocks.POTTED_CATMINT.get(), models().singleTexture("potted_catmint", mcLoc("flower_pot_cross"), "plant",
                blockTexture(ModBlocks.CATMINT.get())).renderType("cutout"));
        // tree
        logBlock(((RotatedPillarBlock) ModBlocks.BLOODWOOD_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.BLOODWOOD_WOOD.get()), blockTexture(ModBlocks.BLOODWOOD_LOG.get()), blockTexture(ModBlocks.BLOODWOOD_LOG.get()));
        logBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_BLOODWOOD_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_BLOODWOOD_WOOD.get()), blockTexture(ModBlocks.STRIPPED_BLOODWOOD_LOG.get()), blockTexture(ModBlocks.STRIPPED_BLOODWOOD_LOG.get()));

        blockItem(ModBlocks.BLOODWOOD_LOG);
        blockItem(ModBlocks.BLOODWOOD_WOOD);
        blockItem(ModBlocks.STRIPPED_BLOODWOOD_LOG);
        blockItem(ModBlocks.STRIPPED_BLOODWOOD_WOOD);

        blockWithItem(ModBlocks.BLOODWOOD_PLANKS);
        leavesBlock(ModBlocks.BLOODWOOD_LEAVES);
        saplingBlock(ModBlocks.BLOODWOOD_SAPLING);

    }

    private void saplingBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(blockRegistryObject.get(),
                models().cross(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }
    private void leavesBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().singleTexture(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), ResourceLocation.parse("minecraft:block/leaves"),
                        "all", blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

    public void makeStrawberryCrop(CropBlock cropBlock, String modelName, String textureName) {
        Function<BlockState,ConfiguredModel[]> function = state -> strawberryStates(state, cropBlock, modelName, textureName);

        getVariantBuilder(cropBlock).forAllStates(function);
    }
    private ConfiguredModel[] strawberryStates(BlockState state, CropBlock block, String modelName, String textureName) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        models[0] = new ConfiguredModel(models().crop(modelName + state.getValue(((StrawberryCropBlock) block).getAgeProperty()),
                modLoc("block/" + textureName + state.getValue(((StrawberryCropBlock) block).getAgeProperty()))).renderType("cutout"));

        return models;
    }
    public void makeCornCrop(CropBlock cropBlock, String modelName, String textureName) {
        Function<BlockState,ConfiguredModel[]> function = state -> cornStates(state, cropBlock, modelName, textureName);

        getVariantBuilder(cropBlock).forAllStates(function);
    }
    private ConfiguredModel[] cornStates(BlockState state, CropBlock block, String modelName, String textureName) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        models[0] = new ConfiguredModel(models().crop(modelName + state.getValue(((CornCropBlock) block).getAgeProperty()),
                modLoc("block/" + textureName + state.getValue(((CornCropBlock) block).getAgeProperty()))).renderType("cutout"));

        return models;
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("tutorialmod:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("tutorialmod:block/" + deferredBlock.getId().getPath() + appendix));
    }
}
