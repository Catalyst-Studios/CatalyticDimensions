package com.catalyst.catalystdimensions.worldgen.spiritworld;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.worldgen.trees.blobfoliage.CrystalFoliagePlacer;
import com.catalyst.catalystdimensions.worldgen.trees.trunks.CrystalTrunkPlacer;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

public class ModConfiguredFeatures {
    public static  final ResourceKey<ConfiguredFeature<?, ?>> OLD_GROWTH_KEY = registerKey("oldgrowth");
    public static  final ResourceKey<ConfiguredFeature<?, ?>> MANA_CRYSTAL_PURPLE_KEY = registerKey("manacrystalpurple");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        //test tree
        register(context, OLD_GROWTH_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new StraightTrunkPlacer(4, 5, 3),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(4), ConstantInt.of(2), 4),
                new TwoLayersFeatureSize(1, 0, 2)).build());
        //crystal formation above ground
        register(context, MANA_CRYSTAL_PURPLE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.PURPLE_STAINED_GLASS),
                new CrystalTrunkPlacer(15, 15, 30),
                BlockStateProvider.simple(Blocks.PINK_STAINED_GLASS),
                new CrystalFoliagePlacer(1, UniformInt.of(8, 32)),
                new TwoLayersFeatureSize(10, 1, 8)
        ).build());


    }
    public static  ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return  ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
    private static  <FC extends FeatureConfiguration, F extends  Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                            ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
}}
