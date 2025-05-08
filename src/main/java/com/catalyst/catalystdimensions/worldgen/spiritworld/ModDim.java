package com.catalyst.catalystdimensions.worldgen.spiritworld;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;



import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class ModDim {
    public static final ResourceKey<LevelStem> CATALYSTDIM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, "catalystdim"));
    public static final ResourceKey<Level> CATALYST_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, "catalystdim"));
    public static final ResourceKey<DimensionType> CATALYST_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, "catalystdim_type"));




    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(CATALYST_DIM_TYPE, new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                0, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);



        NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.JAGGED_PEAKS)),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS));

        NoiseBasedChunkGenerator noiseBasedChunkGenerator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(Pair.of(
                                        Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.JAGGED_PEAKS)),
                                Pair.of(
                                        Climate.parameters(0.1F, 0.2F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.BIRCH_FOREST)),
                                Pair.of(
                                        Climate.parameters(0.3F, 0.6F, 0.1F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.OCEAN)),
                                Pair.of(
                                        Climate.parameters(0.4F, 0.3F, 0.2F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.DARK_FOREST))

                        ))),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.FLOATING_ISLANDS));

    LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDim.CATALYST_DIM_TYPE), noiseBasedChunkGenerator);

        context.register(CATALYSTDIM_KEY, stem);

    }
}
