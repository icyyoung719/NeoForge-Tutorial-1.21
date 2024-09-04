package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.concurrent.CompletableFuture;

/**
 * @Description 自动生成合成表
 * @Author icyyoung
 * @Date 2024/9/4
 */

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        //有序合成
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BISMUTH_BLOCK.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.BISMUTH.get())
                .unlockedBy(getHasName(ModItems.BISMUTH.get()), has(ModItems.BISMUTH.get()))
                .save(output);

        //无序合成
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BISMUTH.get(),9)
                .requires(ModBlocks.BISMUTH_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.BISMUTH_BLOCK.get()), has(ModBlocks.BISMUTH_BLOCK.get()))
                .save(output);
        //
    }
}
