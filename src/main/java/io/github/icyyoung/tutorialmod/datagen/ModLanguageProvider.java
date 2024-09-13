package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.item.ModCreativeModeTabs;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * @Description 自动生成语言翻译json
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, TutorialMod.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        //items
        add(ModItems.BISMUTH.get(), "Bismuth");
        add(ModItems.RAW_BISMUTH.get(), "Raw Bismuth");
        add(ModItems.SAPPHIRE.get(), "Sapphire");
        add(ModItems.RAW_SAPPHIRE.get(), "Raw Sapphire");
        add(ModItems.METAL_DETECTOR.get(), "Metal Detector");
        add(ModItems.STRAWBERRY.get(), "Strawberry");
        add(ModItems.CORN.get(), "Corn");
        add(ModItems.PINE_CONE.get(), "Pine Cone");
        add(ModItems.SAPPHIRE_STAFF.get(), "Sapphire Staff");
        add(ModItems.STRAWBERRY_SEEDS.get(), "Strawberry Seeds");
        add(ModItems.CORN_SEEDS.get(), "Corn Seeds");

        //tool items
        add(ModItems.SAPPHIRE_SWORD.get(), "Sapphire Sword");
        add(ModItems.SAPPHIRE_PICKAXE.get(), "Sapphire Pickaxe");
        add(ModItems.SAPPHIRE_AXE.get(), "Sapphire Axe");
        add(ModItems.SAPPHIRE_SHOVEL.get(), "Sapphire Shovel");
        add(ModItems.SAPPHIRE_HOE.get(), "Sapphire Hoe");
        //armor items
        add(ModItems.SAPPHIRE_HELMET.get(), "Sapphire Helmet");
        add(ModItems.SAPPHIRE_CHESTPLATE.get(), "Sapphire Chestplate");
        add(ModItems.SAPPHIRE_LEGGINGS.get(), "Sapphire Leggings");
        add(ModItems.SAPPHIRE_BOOTS.get(), "Sapphire Boots");

        add(ModBlocks.BISMUTH_BLOCK.get(), "Bismuth Block");
        add(ModBlocks.BISMUTH_ORE.get(), "Bismuth Ore");
        add(ModBlocks.SAPPHIRE_BLOCK.get(), "Sapphire Block");
        add(ModBlocks.SAPPHIRE_ORE.get(), "Sapphire Ore");
        add(ModBlocks.RAW_SAPPHIRE_BLOCK.get(), "Raw Sapphire Block");
        add(ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), "Deepslate Sapphire Ore");
        add(ModBlocks.NETHER_SAPPHIRE_ORE.get(), "Nether Sapphire Ore");
        add(ModBlocks.END_STONE_SAPPHIRE_ORE.get(), "End Stone Sapphire Ore");
        add(ModBlocks.SOUND_BLOCK.get(), "Sound Block");

        add(ModBlocks.SAPPHIRE_BUTTON.get(), "Sapphire Button");
        add(ModBlocks.SAPPHIRE_PRESSURE_PLATE.get(), "Sapphire Pressure Plate");
        add(ModBlocks.SAPPHIRE_STAIRS.get(), "Sapphire Stairs");
        add(ModBlocks.SAPPHIRE_SLAB.get(), "Sapphire Slab");
        add(ModBlocks.SAPPHIRE_WALL.get(), "Sapphire Wall");
        add(ModBlocks.SAPPHIRE_DOOR.get(), "Sapphire Door");
        add(ModBlocks.SAPPHIRE_TRAPDOOR.get(), "Sapphire Trapdoor");
        add(ModBlocks.SAPPHIRE_FENCE.get(), "Sapphire Fence");
        add(ModBlocks.SAPPHIRE_FENCE_GATE.get(), "Sapphire Fence Gate");
        add(ModBlocks.CATMINT.get(), "Catmint");
        add(ModBlocks.POTTED_CATMINT.get(), "Potted Catmint");


        //不能直接以这种方式汉化Tab的名称
        add(ModCreativeModeTabs.BISMUTH_ITEM_TAB.get().getDisplayName().getString(), "Bismuth Items");
        add(ModCreativeModeTabs.BISMUTH_BLOCK_TAB.get().getDisplayName().getString(),"Bismuth Blocks");
        add(ModCreativeModeTabs.TUTORIAL_TAB.get().getDisplayName().getString(),"Creative Mod Tab");
//        add(ModCreativeModeTabs.CREATIVE_MOD_TAB.getRegistryName().toLanguageKey(),"Creative Mod Tab");
        //tooltips
        add("tooltip.tutorialmod.metal_detector.tooltip", "Detects Valuables Underground!");
        add("tooltip.tutorialmod.sound_block.tooltip","Plays sweet sounds when right clicked!");
    }
}
