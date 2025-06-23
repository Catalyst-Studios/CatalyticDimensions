package com.catalyst.catalystdimensions.worldgen.structures;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.worldgen.structures.structures.EndIslandStructures;
import com.catalyst.catalystdimensions.worldgen.structures.structures.OceanStructures;
import com.catalyst.catalystdimensions.worldgen.structures.structures.SkyStructures;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class STStructures {

    /**
     * We are using the Deferred Registry system to register our structure as this is the preferred way on NeoForge.
     * This will handle registering the base structure for us at the correct time so we don't have to handle it ourselves.
     */
    public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(Registries.STRUCTURE_TYPE, CatalystDimensions.MODID);

    /**
     * Registers the base structure itself and sets what its path is. In this case,
     * this base structure will have the resourcelocation of structure_tutorial:sky_structures.
     */
    public static final DeferredHolder<StructureType<?>, StructureType<SkyStructures>> SKY_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("sky_structures", () -> explicitStructureTypeTyping(SkyStructures.CODEC));
    public static final DeferredHolder<StructureType<?>, StructureType<OceanStructures>> OCEAN_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("ocean_structures", () -> explicitStructureTypeTyping(OceanStructures.CODEC));
    public static final DeferredHolder<StructureType<?>, StructureType<EndIslandStructures>> END_ISLAND_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("end_island_structures", () -> explicitStructureTypeTyping(EndIslandStructures.CODEC));

    /**
     * Originally, I had a double lambda ()->()-> for the RegistryObject line above, but it turns out that
     * some IDEs cannot resolve the typing correctly. This method explicitly states what the return type
     * is so that the IDE can put it into the DeferredRegistry properly.
     */
    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(MapCodec<T> structureCodec) {
        return () -> structureCodec;
    }
}
