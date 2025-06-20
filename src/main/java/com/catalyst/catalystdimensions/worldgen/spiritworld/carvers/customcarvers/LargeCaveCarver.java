package com.catalyst.catalystdimensions.worldgen.spiritworld.carvers.customcarvers;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.function.Function;

public class LargeCaveCarver extends WorldCarver<CaveCarverConfiguration> {
    public LargeCaveCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);

    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    @Override
    public boolean carve(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask carvingMask
    ) {
        int x = chunkPos.getBlockX(random.nextInt(16));

        // Safe Y sampling with fallback
        int sampledY = config.y.sample(random, context);
        if (sampledY <= 0) {
            sampledY = 1; // prevent IllegalArgumentException
        }
        int y = random.nextInt(sampledY);

        int z = chunkPos.getBlockZ(random.nextInt(16));

        double horizontalRadius = 6.0 + random.nextDouble() * 4.0;
        double verticalRadius = 5.0 + random.nextDouble() * 2.0;

        return this.carveEllipsoid(
                context,
                config,
                chunk,
                biomeAccessor,
                aquifer,
                x, y, z,
                horizontalRadius,
                verticalRadius,
                carvingMask,
                createPillarCaveSkipChecker(random)
        );
    }

    private CarveSkipChecker createPillarCaveSkipChecker(RandomSource random) {
        return (context, relX, relY, relZ, y) -> {
            double horizontal = relX * relX + relZ * relZ;
            double vertical = relY * relY;

            // Carve less near the extremes to simulate stalactites/stalagmites
            if (vertical > 0.8) return true;

            // Random chance to leave a central column
            if (horizontal < 0.1 && random.nextFloat() < 0.3f) return true;

            return (horizontal + vertical) > 1.0;
        };
    }
}
