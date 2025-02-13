package io.github.icyyoung.tutorialmod.worldgen.tree;

import io.github.icyyoung.tutorialmod.TutorialMod;
import io.github.icyyoung.tutorialmod.worldgen.ModConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/13
 */

public class ModTreeGrowers {
    public static final TreeGrower BLOODWOOD = new TreeGrower(TutorialMod.MOD_ID + ":bloodwood",
            Optional.empty(), Optional.of(ModConfiguredFeatures.BLOODWOOD_KEY), Optional.empty());
}
