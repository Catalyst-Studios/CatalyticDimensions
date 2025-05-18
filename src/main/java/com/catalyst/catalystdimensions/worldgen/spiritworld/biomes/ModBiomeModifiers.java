package com.catalyst.catalystdimensions.worldgen.spiritworld.biomes;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.worldgen.spiritworld.ModPlacedFeatures;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;

import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_TREE_OLD_GROWTH = registerKey("add_tree_old_growth");
    public static final ResourceKey<BiomeModifier> ADD_MANA_CRYSTAL_PURPLE = registerKey("add_mana_crystal_purple");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        context.register(ADD_TREE_OLD_GROWTH, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModBiomes.CRYSTAL_FIELDS_BIOME), biomes.getOrThrow(Biomes.BIRCH_FOREST)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.OLD_GROWTH_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        context.register(ADD_MANA_CRYSTAL_PURPLE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(ModBiomes.CRYSTAL_FIELDS_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MANA_CRYSTAL_PURPLE_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
}
