package io.github.icyyoung.tutorialmod.item;

import io.github.icyyoung.tutorialmod.util.ModTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * @Description 存放自定义的工具的等级
 * @Author icyyoung
 * @Date 2024/9/6
 */

public class ModToolTiers {
    public static final Tier SAPPHIRE = new SimpleTier(ModTags.Blocks.INCORRECT_FOR_BLACK_OPAL_TOOL,
            1500,5f,4f,25,
            ()-> Ingredient.of(ModItems.SAPPHIRE.get()));
}
