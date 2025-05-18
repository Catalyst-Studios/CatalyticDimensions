package com.catalyst.catalystdimensions.worldgen.trees.blobfoliage;

import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFoliagePlacers {
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, CatalystDimensions.MODID);

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<CrystalFoliagePlacer>> CRYSTAL_FOLIAGE =
            FOLIAGE_PLACERS.register("crystal_blob", () -> new FoliagePlacerType<>(CrystalFoliagePlacer.CODEC));
}
