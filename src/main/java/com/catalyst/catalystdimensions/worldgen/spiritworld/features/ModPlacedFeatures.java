package com.catalyst.catalystdimensions.worldgen.spiritworld.features;

import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;
import java.util.stream.Stream;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> OLD_GROWTH_PLACED_KEY = registerKey("oldgrowth_placed");
    public static final ResourceKey<PlacedFeature> MANA_CRYSTAL_PURPLE_PLACED_KEY = registerKey("mana_crystal_purple_placed");

    public static void  bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeature = context.lookup(Registries.CONFIGURED_FEATURE);


        register(context, OLD_GROWTH_PLACED_KEY, configuredFeature.getOrThrow(ModConfiguredFeatures.OLD_GROWTH_KEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(1,0.1f,0),
                        Blocks.OAK_SAPLING));

        register(
                context,
                MANA_CRYSTAL_PURPLE_PLACED_KEY,
                configuredFeature.getOrThrow(ModConfiguredFeatures.MANA_CRYSTAL_PURPLE_KEY),
                Stream.concat(
                        Stream.of(
                                RarityFilter.onAverageOnceEvery(16),
                                InSquarePlacement.spread()
                        ),
                        VegetationPlacements.treePlacement(
                                PlacementUtils.countExtra(1, 0.1f, 0),
                                Blocks.OAK_SAPLING
                        ).stream()
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
