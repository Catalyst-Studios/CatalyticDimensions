package com.catalyst.catalystdimensions.worldgen.spiritworld.carvers;

import com.catalyst.catalystdimensions.worldgen.spiritworld.carvers.customcarvers.LargeCaveCarver;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.catalyst.catalystdimensions.CatalystDimensions;

public class ModWorldCarversRegistry {
    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, CatalystDimensions.MODID);

    public static final DeferredHolder<WorldCarver<?>, LargeCaveCarver> LARGE_CAVE_CARVER =
            CARVERS.register("large_cave_carver", () -> new LargeCaveCarver(CaveCarverConfiguration.CODEC));

}
