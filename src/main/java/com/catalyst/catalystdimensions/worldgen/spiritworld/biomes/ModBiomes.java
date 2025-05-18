package com.catalyst.catalystdimensions.worldgen.spiritworld.biomes;


import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;


public class ModBiomes {
    public static final ResourceKey<Biome> CRYSTAL_FIELDS_BIOME = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("catalystdimensions", "crystal_fields_biome")
    );

    public static void bootstrap(BootstrapContext<Biome> context) {
        context.register(CRYSTAL_FIELDS_BIOME, createCrystalFieldsBiome(context));
    }

    private static Biome createCrystalFieldsBiome(BootstrapContext<Biome> context) {
        // Correct way to get HolderGetter<PlacedFeature> and HolderGetter<ConfiguredWorldCarver>
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var worldCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        // Define the features you want to spawn in the biome
        BiomeGenerationSettings.Builder generationSettings = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);


        // Create the biome's spawn settings (which mobs and animals spawn in this biome)
        MobSpawnSettings.Builder mobSpawnSettings = new MobSpawnSettings.Builder();
        // Define spawners for creatures in the biome

        // Return the created biome with all settings
        return new Biome.BiomeBuilder()
                .temperature(0.8f)  // Warm temperature
                .downfall(0.4f)  // Moderate rainfall
                .generationSettings(generationSettings.build())  // Add generation settings
                .mobSpawnSettings(mobSpawnSettings.build())  // Add mob spawns
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(0xA29BFF)
                        .waterColor(0xA29BFF)
                        .waterFogColor(0x050533)
                        .skyColor(0xC0A9FF)
                        .foliageColorOverride(0x8A2BE2)
                        .grassColorOverride(0x4B0082)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.ENCHANT, 0.005F))
                        .ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111))
                        .backgroundMusic(new Music(SoundEvents.MUSIC_DISC_OTHERSIDE, 12000, 24000, true))
                        .build())
                .build();
    }
}
