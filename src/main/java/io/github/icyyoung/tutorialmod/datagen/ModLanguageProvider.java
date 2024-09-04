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
        add(ModItems.BISMUTH.get(), "Bismuth");
        add(ModItems.RAW_BISMUTH.get(), "Raw Bismuth");
        add(ModItems.SAPPHIRE.get(), "Sapphire");

        add(ModBlocks.BISMUTH_BLOCK.get(), "Bismuth Block");
        add(ModBlocks.BISMUTH_ORE.get(), "Bismuth Ore");

        //不能直接以这种方式汉化Tab的名称
        add(ModCreativeModeTabs.BISMUTH_ITEM_TAB.get().getDisplayName().getString(), "Bismuth Items");
        add(ModCreativeModeTabs.BISMUTH_BLOCK_TAB.get().getDisplayName().getString(),"Bismuth Blocks");
        add(ModCreativeModeTabs.TUTORIAL_TAB.get().getDisplayName().getString(),"Creative Mod Tab");
//        add(ModCreativeModeTabs.CREATIVE_MOD_TAB.getRegistryName().toLanguageKey(),"Creative Mod Tab");
    }
}
