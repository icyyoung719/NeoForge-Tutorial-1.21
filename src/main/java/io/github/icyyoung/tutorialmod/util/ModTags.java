package io.github.icyyoung.tutorialmod.util;

import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @Description 自定义的Tag
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class ModTags {
    public static class Blocks {
        //不需要在前面添加Tutorialmod:
        public static final TagKey<Block> METAL_DETECTOR_VALUABLES = tag("metal_detector_valuables");
        public static final TagKey<Block> NEEDS_SAPPHIRE_TOOL = tag("needs_sapphire_tool");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID,name));
        }
    }
    public static class Items {

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, name));
        }
    }
}
