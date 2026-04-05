package io.github.icyyoung.tutorialmod.worldgen;

import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.DimensionType.MonsterSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public final class ModSapphireWorldgen {
    private static final ResourceLocation SAPPHIRE_FLAT_LOCATION = ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, "sapphire_flat");

    public static final ResourceKey<Biome> SAPPHIRE_BIOME = registerKey(Registries.BIOME, "sapphire_biome");
    public static final ResourceKey<DimensionType> SAPPHIRE_FLAT_TYPE = registerKey(Registries.DIMENSION_TYPE, "sapphire_flat_type");
    public static final ResourceKey<LevelStem> SAPPHIRE_FLAT_STEM = ResourceKey.create(Registries.LEVEL_STEM, SAPPHIRE_FLAT_LOCATION);

    private ModSapphireWorldgen() {
    }

    public static void bootstrapBiomes(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        MobSpawnSettings.Builder mobSpawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(mobSpawns);

        BiomeGenerationSettings.Builder generationSettings = new BiomeGenerationSettings.Builder(placedFeatures, carvers);
        BiomeDefaultFeatures.addPlainGrass(generationSettings);
        BiomeDefaultFeatures.addDefaultFlowers(generationSettings);

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .fogColor(0xC0D8FF)
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .skyColor(0x77ADFF)
                .build();

        context.register(SAPPHIRE_BIOME, new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.8F)
                .downfall(0.4F)
                .specialEffects(effects)
                .mobSpawnSettings(mobSpawns.build())
                .generationSettings(generationSettings.build())
                .build());
    }

    public static void bootstrapDimensionTypes(BootstrapContext<DimensionType> context) {
        context.register(SAPPHIRE_FLAT_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                16.0D,
                true,
                false,
                -64,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new MonsterSettings(false, true, UniformInt.of(0, 7), 0)
        ));
    }

    public static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<StructureSet> structureSets = context.lookup(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        HolderSet<StructureSet> villages = HolderSet.direct(structureSets.getOrThrow(BuiltinStructureSets.VILLAGES));
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                Optional.of(villages),
                biomes.getOrThrow(SAPPHIRE_BIOME),
                FlatLevelGeneratorSettings.createLakesList(placedFeatures)
        ).withBiomeAndLayers(
                List.of(
                        new FlatLayerInfo(1, net.minecraft.world.level.block.Blocks.BEDROCK),
                        new FlatLayerInfo(2, net.minecraft.world.level.block.Blocks.DIRT),
                        new FlatLayerInfo(1, net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                ),
                Optional.of(villages),
                biomes.getOrThrow(SAPPHIRE_BIOME)
        );
        settings.setDecoration();

        context.register(SAPPHIRE_FLAT_STEM, new LevelStem(
                dimensionTypes.getOrThrow(SAPPHIRE_FLAT_TYPE),
                new FlatLevelSource(settings)
        ));
    }

    private static <T> ResourceKey<T> registerKey(ResourceKey<? extends Registry<T>> registryKey, String name) {
        return ResourceKey.create(registryKey, ResourceLocation.fromNamespaceAndPath(TutorialMod.MOD_ID, name));
    }
}