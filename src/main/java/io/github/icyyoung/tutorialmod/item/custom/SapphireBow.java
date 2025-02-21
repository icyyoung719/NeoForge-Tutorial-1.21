package io.github.icyyoung.tutorialmod.item.custom;

import io.github.icyyoung.tutorialmod.item.ModItems;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/21
 */

public class SapphireBow extends BowItem {
    public SapphireBow(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.getPersistentData().putBoolean(ModItems.SAPPHIRE_BOW.getRegisteredName(), true);
        return arrow;
    }
}
