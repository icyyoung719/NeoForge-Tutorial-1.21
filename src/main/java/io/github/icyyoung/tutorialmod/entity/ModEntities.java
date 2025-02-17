package io.github.icyyoung.tutorialmod.entity;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.entity.custom.GeckoEntity;
import io.github.icyyoung.tutorialmod.entity.custom.TomahawkProjectileEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/17
 */

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TutorialMod.MOD_ID);

    public static final Supplier<EntityType<GeckoEntity>> GECKO =
            ENTITY_TYPES.register("gecko", () -> EntityType.Builder.of(GeckoEntity::new, MobCategory.CREATURE)
                    .sized(0.75f, 0.35f).build("gecko"));

    public static final Supplier<EntityType<TomahawkProjectileEntity>> TOMAHAWK =
            ENTITY_TYPES.register("tomahawk", () -> EntityType.Builder.<TomahawkProjectileEntity>of(TomahawkProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 1.15f).build("tomahawk"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
