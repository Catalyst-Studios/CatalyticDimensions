package com.catalyst.catalystdimensions.worldgen.features.customfeatures;

import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModFeaturesRegistry {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, CatalystDimensions.MODID);

    public static final DeferredHolder<Feature<?>, CrystalFeature> CRYSTAL_FEATURE =
            FEATURES.register("crystal_feature",
                    () -> new CrystalFeature(CrystalFeatureConfiguration.CODEC));
}
