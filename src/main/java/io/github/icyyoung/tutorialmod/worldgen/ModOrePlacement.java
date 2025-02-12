package io.github.icyyoung.tutorialmod.worldgen;

import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

/**
 * @Description TODO
 * @Author icyyoung
 * @Date 2025/2/12
 */

public class ModOrePlacement {
    public static List<PlacementModifier> orePlacement(PlacementModifier pCountPlacement, PlacementModifier pHeightModifier) {
        return List.of(pCountPlacement, InSquarePlacement.spread(), pHeightModifier, BiomeFilter.biome());
    }
    public static List<PlacementModifier> commonOrePlacement(int pCount, PlacementModifier pHeightModifier) {
        return orePlacement(CountPlacement.of(pCount), pHeightModifier);
    }
    public static List<PlacementModifier> rareOrePlacement(int pChance, PlacementModifier pHeightModifier) {
        return orePlacement(RarityFilter.onAverageOnceEvery(pChance), pHeightModifier);
    }
}
