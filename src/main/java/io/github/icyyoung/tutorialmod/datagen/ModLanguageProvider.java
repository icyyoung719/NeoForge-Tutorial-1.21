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
        add(ModItems.CHISEL.get(), "Chisel");
        add(ModItems.STRAWBERRY.get(), "Strawberry");
        add(ModItems.CORN.get(), "Corn");
        add(ModItems.PINE_CONE.get(), "Pine Cone");
        add(ModItems.SAPPHIRE_STAFF.get(), "Sapphire Staff");
        add(ModItems.STRAWBERRY_SEEDS.get(), "Strawberry Seeds");
        add(ModItems.CORN_SEEDS.get(), "Corn Seeds");
        add(ModItems.TOMAHAWK.get(), "Tomahawk");
        //sound disks
        add(ModItems.BAR_BRAWL_MUSIC_DISC.get(), "Bar Brawl Music Disk");
        add("item.tutorialmod.bar_brawl_music_disc.desc","Bryan Tech - Bar Brawl (CC0)");

        //potion and effect items
        add("effect.minecraft.slimey", "Slimey");

        add("item.minecraft.potion.effect.slimey_potion","Slimey Potion");
        add("item.minecraft.splash_potion.effect.slimey_potion","Slimey Splash Potion");
        add("item.minecraft.lingering_potion.effect.slimey_potion","Slimey Lingering Potion");

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

        add(ModBlocks.BLOODWOOD_LOG.get(), "Bloodwood Log");
        add(ModBlocks.BLOODWOOD_WOOD.get(), "Bloodwood Wood");
        add(ModBlocks.BLOODWOOD_PLANKS.get(), "Bloodwood Planks");
        add(ModBlocks.STRIPPED_BLOODWOOD_LOG.get(), "Stripped Bloodwood Log");
        add(ModBlocks.STRIPPED_BLOODWOOD_WOOD.get(), "Stripped Bloodwood Wood");
        add(ModBlocks.BLOODWOOD_LEAVES.get(), "Bloodwood Leaves");
        add(ModBlocks.BLOODWOOD_SAPLING.get(), "Bloodwood Sapling");


        //不能直接以这种方式汉化Tab的名称
        add(ModCreativeModeTabs.BISMUTH_ITEM_TAB.get().getDisplayName().getString(), "Bismuth Items");
        add(ModCreativeModeTabs.BISMUTH_BLOCK_TAB.get().getDisplayName().getString(),"Bismuth Blocks");
        add(ModCreativeModeTabs.TUTORIAL_TAB.get().getDisplayName().getString(),"Creative Mod Tab");
//        add(ModCreativeModeTabs.CREATIVE_MOD_TAB.getRegistryName().toLanguageKey(),"Creative Mod Tab");
        //tooltips
        add("tooltip.tutorialmod.metal_detector.tooltip", "Detects Valuables Underground!");
        add("tooltip.tutorialmod.sound_block.tooltip","Plays sweet sounds when right clicked!");
        //paintings
        add(getPaintingTitleKey("world"), "World");
        add(getPaintingAuthorKey("world"), "NanoAttack");
        add(getPaintingTitleKey("shrimp"), "Shrimp");
        add(getPaintingAuthorKey("shrimp"), "NanoAttack");
        add(getPaintingTitleKey("saw_them"), "Saw Them");
        add(getPaintingAuthorKey("saw_them"), "NanoAttack");
        // enchantments
        add(getEnchantmentKey("lightning_striker"), "Lightning Striker");
        // entities
        add(ModItems.GECKO_SPAWN_EGG.get(), "Gecko Spawn Egg");
        add(getEntityKey("gecko"), "Gecko");
        add(getEntityKey("tomahawk"), "Tomahawk");
    }




    // used for paintings to translate
    private static String getPaintingTitleKey(String name) {
        return "painting." + TutorialMod.MOD_ID + "." + name.toLowerCase() +".title";
    }
    private static String getPaintingAuthorKey(String name) {
        return "painting." + TutorialMod.MOD_ID + "." + name.toLowerCase() +".author";
    }
    // used for enchantments to translate
    private static String getEnchantmentKey(String name) {
        return "enchantment." + TutorialMod.MOD_ID + "." + name.toLowerCase();
    }
    private static String getEntityKey(String name) {
        return "entity." + "mccourse" + "." + name.toLowerCase();
    }
}
