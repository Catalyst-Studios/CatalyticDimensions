package com.catalyst.catalystdimensions.worldgen.features;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.WouldSurvivePredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;
import java.util.stream.Stream;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> OLD_GROWTH_PLACED_KEY = registerKey("oldgrowth_placed");
    public static final ResourceKey<PlacedFeature> CRYSTAL_PLACED_KEY = registerKey("mana_crystal_purple_placed");

    public static void  bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeature = context.lookup(Registries.CONFIGURED_FEATURE);


        register(context, OLD_GROWTH_PLACED_KEY, configuredFeature.getOrThrow(ModConfiguredFeatures.OLD_GROWTH_KEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(3,0.1f,1),
                        Blocks.OAK_SAPLING));

        register(
                context,
                CRYSTAL_PLACED_KEY,
                configuredFeature.getOrThrow(ModConfiguredFeatures.CRYSTAL_KEY),
                Stream.concat(
                        Stream.of(
                                RarityFilter.onAverageOnceEvery(5),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(VerticalAnchor.absolute(128), VerticalAnchor.absolute(320))
                        ),
                        VegetationPlacements.treePlacement(PlacementUtils.countExtra(1, 0.1f, 2),
                        Blocks.OAK_SAPLING).stream()
                ).toList()
        );



    }



    
    private static  ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
