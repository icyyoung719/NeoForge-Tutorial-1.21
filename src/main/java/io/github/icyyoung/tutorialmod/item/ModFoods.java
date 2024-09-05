package io.github.icyyoung.tutorialmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

/**
 * @Description 食物
 * @Author icyyoung
 * @Date 2024/9/5
 */

public class ModFoods {
    public static final FoodProperties STRAWBERRY = new FoodProperties.Builder().nutrition(2).fast()
            .saturationModifier(0.2f).effect(()->new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200),
                    0.1f).build();
}
