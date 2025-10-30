package com.catalyst.catalystdimensions.worldgen.carvers;

import com.catalyst.catalystdimensions.worldgen.carvers.customcarvers.LargeCaveCarver;
import com.catalyst.catalystdimensions.worldgen.carvers.customcarvers.PillarFieldCarver;
import com.catalyst.catalystdimensions.worldgen.carvers.customcarvers.VerticalCylinderCarver;
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
            CARVERS.register("large_cave_carver",
                    () -> new LargeCaveCarver(CaveCarverConfiguration.CODEC));

    // NEW: pillar field / column forest generator
    public static final DeferredHolder<WorldCarver<?>, PillarFieldCarver> PILLAR_FIELD_CARVER =
            CARVERS.register("pillar_field_carver",
                    () -> new PillarFieldCarver(CaveCarverConfiguration.CODEC));
    public static final DeferredHolder<WorldCarver<?>, VerticalCylinderCarver> VERTICAL_CYLINDER_CARVER =
            CARVERS.register("vertical_cylinder_carver",
                    () -> new VerticalCylinderCarver(CaveCarverConfiguration.CODEC));
}
