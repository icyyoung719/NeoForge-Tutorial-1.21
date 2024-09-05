package io.github.icyyoung.tutorialmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Description 自定义燃料类
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class FuelItem extends Item {
    private int burnTime=0;

    public FuelItem(Properties properties,int burnTime) {
        super(properties);
        this.burnTime=burnTime;
    }

    @Override
    public int getBurnTime(@NotNull ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return burnTime;
    }
}
