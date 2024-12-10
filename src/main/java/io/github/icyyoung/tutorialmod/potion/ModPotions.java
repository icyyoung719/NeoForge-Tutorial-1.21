package io.github.icyyoung.tutorialmod.potion;

import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2024/12/10
 */

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, TutorialMod.MOD_ID);

    public static final Holder<Potion> SLIMEY_POTION = POTIONS.register("slimey_potion",
            () -> new Potion(new MobEffectInstance(MobEffects.GLOWING, 1200, 0)));

    public static void register(IEventBus eventBus){
        POTIONS.register(eventBus);
    }
}
