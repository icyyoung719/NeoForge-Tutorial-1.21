package io.github.icyyoung.tutorialmod.datagen;


import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

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
    }


}
