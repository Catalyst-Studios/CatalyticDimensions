package com.catalyst.catalystdimensions.worldgen.biomes;

import com.catalyst.catalystdimensions.CatalystDimensions;

import com.catalyst.catalystdimensions.worldgen.biomes.arcane.CrystalFieldsBiome;
import com.catalyst.catalystdimensions.worldgen.biomes.sanguine.SanguineDepthsBiome;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {
    public static final ResourceKey<Biome> CRYSTAL_FIELDS_BIOME = registerBiomeKey("crystal_fields");
    public static final ResourceKey<Biome>  SANGUINE_DEPTHS_BIOME = registerBiomeKey("sanguine_depths");



    public static void registerBiomes() {
        SpiritWorldBiomeRegistry.registerSpiritHighlandsBiome(CRYSTAL_FIELDS_BIOME, 10);
        SpiritWorldBiomeRegistry.registerSpiritLowlandsBiome(SANGUINE_DEPTHS_BIOME, 20);
        
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        var carver = context.lookup(Registries.CONFIGURED_CARVER);
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        register(context, CRYSTAL_FIELDS_BIOME, CrystalFieldsBiome.crystalFields(placedFeatures, carver));
        register(context, SANGUINE_DEPTHS_BIOME, SanguineDepthsBiome.sanguineDepths(placedFeatures, carver));
    }


    private static void register(BootstrapContext<Biome> context, ResourceKey<Biome> key, Biome biome) {
        context.register(key, biome);
    }

    private static ResourceKey<Biome> registerBiomeKey(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
}
