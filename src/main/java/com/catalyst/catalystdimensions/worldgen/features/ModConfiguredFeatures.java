package com.catalyst.catalystdimensions.worldgen.features;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import com.catalyst.catalystdimensions.worldgen.features.customfeatures.CrystalFeature;
import com.catalyst.catalystdimensions.worldgen.features.customfeatures.CrystalFeatureConfiguration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import com.catalyst.catalystdimensions.worldgen.features.customfeatures.ModFeaturesRegistry;




import java.util.List;

public class ModConfiguredFeatures {
    public static  final ResourceKey<ConfiguredFeature<?, ?>> OLD_GROWTH_KEY = registerKey("oldgrowth");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTAL_KEY = registerKey("crystal");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        //test tree
        register(context, OLD_GROWTH_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.OAK_LOG),
                new StraightTrunkPlacer(4, 5, 3),
                BlockStateProvider.simple(Blocks.OAK_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(4), ConstantInt.of(2), 4),
                new TwoLayersFeatureSize(1, 0, 2)).build());
        //crystal formation above ground
        context.register(CRYSTAL_KEY,
                new ConfiguredFeature<>(
                        ModFeaturesRegistry.CRYSTAL_FEATURE.get(),
                        new CrystalFeatureConfiguration(
                                List.of(                       // Material sets
                                        new CrystalFeatureConfiguration.MaterialSet(
                                                ModBlocks.blockRef("purple_crystal_block").defaultBlockState(),
                                                ModBlocks.blockRef("pink_crystal_block").defaultBlockState()
                                        ),
                                        new CrystalFeatureConfiguration.MaterialSet(
                                                ModBlocks.blockRef("green_crystal_block").defaultBlockState(),
                                                ModBlocks.blockRef("lime_crystal_block").defaultBlockState()
                                        ),
                                        new CrystalFeatureConfiguration.MaterialSet(
                                                ModBlocks.blockRef("blue_crystal_block").defaultBlockState(),
                                                ModBlocks.blockRef("light_blue_crystal_block").defaultBlockState()
                                        )

                                ),
                                4, 8,                          // minBranches, maxBranches
                                20, 10,                         // baseBranchLength, branchLengthRandomness
                                4, 2,                          // baseBranchWidth, branchWidthRandomness
                                0.8f,                         // taperExponent (1 = linear taper)
                                (new CrystalFeatureConfiguration.Block(List.of(
                                        Blocks.DIRT.defaultBlockState(),
                                        Blocks.GRASS_BLOCK.defaultBlockState(),
                                        Blocks.STONE.defaultBlockState(),
                                        Blocks.GRAVEL.defaultBlockState(),
                                        Blocks.GRANITE.defaultBlockState(),
                                        Blocks.ANDESITE.defaultBlockState(),
                                        Blocks.DIORITE.defaultBlockState()
                                )// notReplaceable
                                ))


                        )
                )
        );

    }
    public static  ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return  ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
    private static  <FC extends FeatureConfiguration, F extends  Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                            ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
}}
