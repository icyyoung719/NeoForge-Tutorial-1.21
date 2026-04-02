package io.github.icyyoung.tutorialmod.world;

import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class ModDimensions {

    public static final ResourceKey<Level> SAPPHIRE_FLAT = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "sapphire_flat")
    );

    private ModDimensions() {
    }

}
