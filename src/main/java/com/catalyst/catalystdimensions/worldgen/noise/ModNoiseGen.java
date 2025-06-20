
package com.catalyst.catalystdimensions.worldgen.noise;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import com.catalyst.catalystdimensions.worldgen.spiritworld.surfacerules.ModSurfaceRules;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;

public class ModNoiseGen {

    public static final ResourceKey<NoiseGeneratorSettings> CUSTOM_FLOATING_ISLANDS = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath("catalystdimensions", "custom_floating_islands")
    );


    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        final int seaLevel = 63;




        // ========== BASE ISLAND TERRAIN ==========
        DensityFunction base3DNoise = DensityFunctions.blendDensity(
                BlendedNoise.createUnseeded(0.4, 0.25, 400.0, 250.0, 4)
        );
        DensityFunction base3DNoiseInverted = DensityFunctions.mul(base3DNoise, DensityFunctions.constant(-1.0));
        DensityFunction verticalFade = DensityFunctions.yClampedGradient(64, 160, -1, 1);
        DensityFunction terrainBlob = DensityFunctions.add(base3DNoiseInverted, verticalFade);
        DensityFunction clamped = DensityFunctions.rangeChoice(terrainBlob, -1., 1.0, DensityFunctions.constant(-1.0), DensityFunctions.constant(1.0));
        DensityFunction finalTerrain = DensityFunctions.mul(clamped, DensityFunctions.constant(1.2));

        // ========== BASE ISLAND TERRAIN END ==========

        // ========== MOUNTGEN ==========


        // ========== MOUNTGEN END ==========

        // ========== TEMPERATURE ==========
        HolderGetter<NormalNoise.NoiseParameters> noiseRegistry = context.lookup(Registries.NOISE);
        Holder<NormalNoise.NoiseParameters> shift = noiseRegistry.getOrThrow(ModNoiseParameters.SHIFT);
        DensityFunction shiftFunction = DensityFunctions.noise(shift);
        Holder<NormalNoise.NoiseParameters> temperatureNoise = noiseRegistry.getOrThrow(ModNoiseParameters.TEMPERATURE);
        DensityFunction temperature = DensityFunctions.shiftedNoise2d(shiftFunction, shiftFunction, 1.0, temperatureNoise);

        // ========== TEMPERATURE END ==========

        // ========== VEGETATION ==========
        Holder<NormalNoise.NoiseParameters> vegNoise = noiseRegistry.getOrThrow(ModNoiseParameters.VEGETATION);
        DensityFunction vegetation = DensityFunctions.shiftedNoise2d(shiftFunction, shiftFunction, 1.0, vegNoise);
        // ========== VEGETATION END ==========

        // ========== CONTINENTS ==========
        Holder<NormalNoise.NoiseParameters> continentsNoise = noiseRegistry.getOrThrow(ModNoiseParameters.VEGETATION);
        DensityFunction continents = DensityFunctions.shiftedNoise2d(shiftFunction, shiftFunction, 1.0, continentsNoise);
        // ========== CONTINENTS END ==========

        // ========== EROSION ==========
        Holder<NormalNoise.NoiseParameters> erosionNoise = noiseRegistry.getOrThrow(ModNoiseParameters.VEGETATION);
        DensityFunction erosion= DensityFunctions.shiftedNoise2d(shiftFunction, shiftFunction, 1.0, erosionNoise);
        // ========== EROSION END ==========

        // ========== DEPTH ==========
        Holder<NormalNoise.NoiseParameters> depthNoise = noiseRegistry.getOrThrow(ModNoiseParameters.VEGETATION);
        DensityFunction depthclamped = DensityFunctions.yClampedGradient(-64, 320, -1, 1);
        DensityFunction depth = DensityFunctions.add(DensityFunctions.noise(depthNoise),depthclamped);


        // ========== DEPTH END ==========

        // ========== RIDGES ==========
        DensityFunction ridges = DensityFunctions.mul(finalTerrain, DensityFunctions.constant(4));
        // ========== RIDGES END ==========

        // ========== NOISE ROUTER ==========
        NoiseRouter router = new NoiseRouter(
                DensityFunctions.zero(), // barrier
                DensityFunctions.zero(), // fluidLevelFloodedness
                DensityFunctions.zero(), // fluidLevelSpread
                DensityFunctions.zero(), // lava
                temperature,
                vegetation,
                continents,
                erosion,
                depth,
                ridges,
                base3DNoise,
                finalTerrain,
                finalTerrain,
                finalTerrain,
                finalTerrain
        );
        // ========== NOISE ROUTER END ==========

        context.register(
                CUSTOM_FLOATING_ISLANDS,
                new NoiseGeneratorSettings(
                        new NoiseSettings(-64, 384, 2, 1),
                        Blocks.STONE.defaultBlockState(),
                        Blocks.AIR.defaultBlockState(),
                        router,
                        ModSurfaceRules.makeSpiritWorldRules(),
                        List.of(),
                        seaLevel,
                        true, false, false, false
                )
        );
    }
}