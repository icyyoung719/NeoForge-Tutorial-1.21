package io.github.icyyoung.tutorialmod.datagen;

import io.github.icyyoung.tutorialmod.block.ModBlocks;
import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
    private static final List<ItemLike> SAPPHIRE_SMELTABLES = List.of(ModItems.RAW_SAPPHIRE.get(),
            ModBlocks.SAPPHIRE_ORE.get(), ModBlocks.DEEPSLATE_SAPPHIRE_ORE.get(), ModBlocks.NETHER_SAPPHIRE_ORE.get(),
            ModBlocks.END_STONE_SAPPHIRE_ORE.get());

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        //有序合成
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BISMUTH_BLOCK.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.BISMUTH.get())
                .unlockedBy(getHasName(ModItems.BISMUTH.get()), has(ModItems.BISMUTH.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SAPPHIRE_BLOCK.get())
                .pattern("SSS")
                .pattern("SSS")
                .pattern("SSS")
                .define('S', ModItems.SAPPHIRE.get())
                .unlockedBy(getHasName(ModItems.SAPPHIRE.get()), has(ModItems.SAPPHIRE.get()))
                .save(output);

        //无序合成
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BISMUTH.get(),9)
                .requires(ModBlocks.BISMUTH_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.BISMUTH_BLOCK.get()), has(ModBlocks.BISMUTH_BLOCK.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SAPPHIRE.get(), 9)
                .requires(ModBlocks.SAPPHIRE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.SAPPHIRE_BLOCK.get()), has(ModBlocks.SAPPHIRE_BLOCK.get()))
                .save(output);
        //smelting and blasting
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.SAPPHIRE_ORE), RecipeCategory.MISC, ModItems.SAPPHIRE.get(), 0.7f, 200);
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModBlocks.SAPPHIRE_ORE), RecipeCategory.MISC, ModItems.SAPPHIRE.get(), 0.7f, 100);
    }



    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput output,List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, RecipeSerializer<T> pCookingSerializer,AbstractCookingRecipe.Factory<T> pFactory) {
        for (ItemLike itemlike : pIngredients){
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pCookingSerializer, pFactory)
                    .unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(output);
        }

    }
}
