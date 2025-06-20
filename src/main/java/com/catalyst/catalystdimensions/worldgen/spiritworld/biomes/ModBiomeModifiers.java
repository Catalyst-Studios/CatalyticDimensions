package com.catalyst.catalystdimensions.worldgen.spiritworld.biomes;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.worldgen.spiritworld.carvers.ModConfiguredWorldCarvers;
import com.catalyst.catalystdimensions.worldgen.spiritworld.features.ModPlacedFeatures;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;


import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_TREE_OLD_GROWTH = registerKey("add_tree_old_growth");
    public static final ResourceKey<BiomeModifier> ADD_MANA_CRYSTAL_PURPLE = registerKey("add_mana_crystal_purple");
    public static final ResourceKey<BiomeModifier> ADDCARVERTEST = registerKey("add_craver_test");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);
        var carverGetter = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(ADD_TREE_OLD_GROWTH, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModBiomes.CRYSTAL_FIELDS_BIOME), biomes.getOrThrow(Biomes.BIRCH_FOREST)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.OLD_GROWTH_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_MANA_CRYSTAL_PURPLE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModBiomes.CRYSTAL_FIELDS_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MANA_CRYSTAL_PURPLE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADDCARVERTEST, new BiomeModifiers.AddCarversBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModBiomes.CRYSTAL_FIELDS_BIOME)),
                HolderSet.direct(carverGetter.getOrThrow(ModConfiguredWorldCarvers.LARGE_CAVE_CONFIGURED)),
                GenerationStep.Carving.AIR
        ));
        }


    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
}

