package com.catalyst.catalystdimensions.worldgen.biomes;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class SpiritWorldBiomeRegistry {
    private static final List<WeightedEntry.Wrapper<ResourceKey<Biome>>> spirithighlandsBiomes = new ArrayList();
    private static final List<WeightedEntry.Wrapper<ResourceKey<Biome>>> spiritlowlandsBiomes = new ArrayList();
    private static final List<WeightedEntry.Wrapper<ResourceKey<Biome>>> caveBiomes = new ArrayList();

    public SpiritWorldBiomeRegistry() {
    }

    public static void registerSpiritHighlandsBiome(ResourceKey<Biome> biome, int weight) {
        spirithighlandsBiomes.add(WeightedEntry.wrap(biome, weight));
    }

    public static void registerSpiritLowlandsBiome(ResourceKey<Biome> biome, int weight) {
        spiritlowlandsBiomes.add(WeightedEntry.wrap(biome, weight));
    }

    public static void registerCaveBiome(ResourceKey<Biome> biome, int weight) {
        caveBiomes.add(WeightedEntry.wrap(biome, weight));
    }


    public static List<WeightedEntry.Wrapper<ResourceKey<Biome>>> getSpirithighlandsBiomes() {
        return List.copyOf(spirithighlandsBiomes);
    }

    public static List<WeightedEntry.Wrapper<ResourceKey<Biome>>> getSpiritlowlandsBiomes() {
        return List.copyOf(spiritlowlandsBiomes);
    }

    public static List<WeightedEntry.Wrapper<ResourceKey<Biome>>> getCaveBiomes() {
        return List.copyOf(caveBiomes);
    }



    }

