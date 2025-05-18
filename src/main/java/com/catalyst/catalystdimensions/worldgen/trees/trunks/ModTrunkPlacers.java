package com.catalyst.catalystdimensions.worldgen.trees.trunks;

import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModTrunkPlacers {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, CatalystDimensions.MODID);

    // Register the custom trunk placer
    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<CrystalTrunkPlacer>> CRYSTAL =
            TRUNK_PLACERS.register("crystal", () -> new TrunkPlacerType<>(CrystalTrunkPlacer.CODEC));
}
