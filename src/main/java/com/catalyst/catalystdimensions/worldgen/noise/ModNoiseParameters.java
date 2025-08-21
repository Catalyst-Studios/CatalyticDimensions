package com.catalyst.catalystdimensions.worldgen.noise;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;


public class ModNoiseParameters {
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_SHAPE =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "island_shape"));
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_ELEVATION =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "island_elevation"));
    public static final ResourceKey<NormalNoise.NoiseParameters> BIOME_NOISE =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "biome_noise"));
    public static final ResourceKey<NormalNoise.NoiseParameters> TEMPERATURE =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "temperature"));

    public static final ResourceKey<NormalNoise.NoiseParameters> VEGETATION =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "vegetation"));

    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTALNESS =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "continentalness"));

    public static final ResourceKey<NormalNoise.NoiseParameters> EROSION =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "erosion"));

    public static final ResourceKey<NormalNoise.NoiseParameters> SHIFT =
            ResourceKey.create(Registries.NOISE,ResourceLocation.fromNamespaceAndPath("catalystdimensions", "shift"));

    public static final ResourceKey<NormalNoise.NoiseParameters> DEPTH =
            ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath("catalystdimensions", "depth"));

    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(ISLAND_SHAPE, new NormalNoise.NoiseParameters(1, 1.0, 0.5));
        context.register(ISLAND_ELEVATION, new NormalNoise.NoiseParameters(2, 1.0, 1.0));
        context.register(BIOME_NOISE, new NormalNoise.NoiseParameters(1, 1.0, 1.0));
        int firstOctave = -6;
        context.register( TEMPERATURE, new NormalNoise.NoiseParameters( -10 + firstOctave, 1.5, 1.0, 1.0, 0.0, 0.0, 0.0));
        context.register( VEGETATION, new NormalNoise.NoiseParameters(-8 + firstOctave, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0));
        context.register( CONTINENTALNESS, new NormalNoise.NoiseParameters(-9 + firstOctave, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0));
        context.register( EROSION, new NormalNoise.NoiseParameters(-9 + firstOctave, 1.0, 1.0, 0.0, 1.0, 1.0));
        context.register(SHIFT, new NormalNoise.NoiseParameters(-6, 1.0, 0.0, 0.0, 0.0));
        context.register(DEPTH, new NormalNoise.NoiseParameters(-10, List.of(1.0, -1.0)));






        }

    }
