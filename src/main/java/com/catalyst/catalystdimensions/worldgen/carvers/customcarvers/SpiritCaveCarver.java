package com.catalyst.catalystdimensions.worldgen.carvers.customcarvers;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.function.Function;

public class SpiritCaveCarver extends WorldCarver<CarverConfiguration> {
    public SpiritCaveCarver(Codec<CarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(CarvingContext context, CarverConfiguration config, ChunkAccess chunk, Function<BlockPos, Holder<Biome>> biomeGetter, RandomSource random, Aquifer aquifer, ChunkPos pos, CarvingMask mask) {
        // TODO: implement your own carving logic here
        return false;
    }

    @Override
    public boolean isStartChunk(CarverConfiguration config, RandomSource random) {
        return false;
    }
}
