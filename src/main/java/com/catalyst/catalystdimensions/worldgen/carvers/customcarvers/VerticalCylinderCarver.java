package com.catalyst.catalystdimensions.worldgen.carvers.customcarvers;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * PillarFieldCarver
 *
 * Creates rare, tall, wobbling chimney-like voids.
 *
 * IMPORTANT DIFFERENCE IN THIS VERSION:
 * - Even if this chunk is "active", we only carve AT MOST ONE pillar.
 *   This prevents "void world" when probability is high.
 */
public class VerticalCylinderCarver extends WorldCarver<CaveCarverConfiguration> {

    // distance between pillar grid cells in world coords
    private static final int CELL_SIZE = 64;

    // chance a given cell actually spawns a pillar at all
    private static final float PILLAR_CHANCE = 0.12f;

    // how far beyond the chunk edge to consider pillar centers
    private static final int PILLAR_SEARCH_RADIUS_BLOCKS = 24;

    // drop debug spine blocks down the pillar core so we can SEE them
    private static final boolean DEBUG_MARKER = true;

    public VerticalCylinderCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    @Override
    public boolean carve(
            CarvingContext ctx,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask carvingMask
    ) {
        // Log so we know which chunks even tried
        System.out.println("[PillarFieldCarver] start chunk " + chunkPos.x + ", " + chunkPos.z);

        if (!isStartChunk(config, random)) {
            return false;
        }

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxX = chunkMinX + 15;
        int chunkMaxZ = chunkMinZ + 15;

        int worldMinXToCheck = chunkMinX - PILLAR_SEARCH_RADIUS_BLOCKS;
        int worldMinZToCheck = chunkMinZ - PILLAR_SEARCH_RADIUS_BLOCKS;
        int worldMaxXToCheck = chunkMaxX + PILLAR_SEARCH_RADIUS_BLOCKS;
        int worldMaxZToCheck = chunkMaxZ + PILLAR_SEARCH_RADIUS_BLOCKS;

        int cellMinX = Math.floorDiv(worldMinXToCheck, CELL_SIZE);
        int cellMinZ = Math.floorDiv(worldMinZToCheck, CELL_SIZE);
        int cellMaxX = Math.floorDiv(worldMaxXToCheck, CELL_SIZE);
        int cellMaxZ = Math.floorDiv(worldMaxZToCheck, CELL_SIZE);

        // collect candidate pillars that touch this chunk
        List<PillarCandidate> candidates = new ArrayList<>();

        for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
            for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {

                long pillarSeed = getPillarSeed(cellX, cellZ);
                RandomSource pillarRandom = RandomSource.create(pillarSeed);

                // does this cell even spawn a pillar?
                if (pillarRandom.nextFloat() > PILLAR_CHANCE) {
                    continue;
                }

                float centerX = cellX * CELL_SIZE + pillarRandom.nextFloat() * CELL_SIZE;
                float centerZ = cellZ * CELL_SIZE + pillarRandom.nextFloat() * CELL_SIZE;

                // radius tuning:
                //  - normal pillar ~4-7 radius
                //  - rare "mega" pillar ~9-12 radius (smaller than before so it won't nuke 3 chunks)
                float baseRadius;
                if (pillarRandom.nextFloat() < 0.05f) {
                    baseRadius = 9.0f + pillarRandom.nextFloat() * 3.0f; // 9-12
                } else {
                    baseRadius = 4.0f + pillarRandom.nextFloat() * 3.0f; // 4-7
                }

                if (!pillarTouchesChunk(
                        centerX,
                        centerZ,
                        baseRadius + 4.0f,
                        chunkMinX,
                        chunkMinZ,
                        chunkMaxX,
                        chunkMaxZ
                )) {
                    continue;
                }

                float wobbleScale = 0.3f + pillarRandom.nextFloat() * 0.4f;
                float wobbleFreq  = 0.08f + pillarRandom.nextFloat() * 0.05f;
                float pulsePhase  = pillarRandom.nextFloat() * 1000f;

                candidates.add(new PillarCandidate(
                        pillarSeed,
                        centerX,
                        centerZ,
                        baseRadius,
                        wobbleScale,
                        wobbleFreq,
                        pulsePhase
                ));
            }
        }

        if (candidates.isEmpty()) {
            // This active chunk just didn't get a pillar intersecting it.
            return false;
        }

        // pick ONE pillar for this chunk so we can't spam 3+ overlapping voids
        PillarCandidate chosen = candidates.get(random.nextInt(candidates.size()));

        System.out.println("[PillarFieldCarver] using pillar at (" +
                chosen.centerX + ", " + chosen.centerZ + ") r=" + chosen.baseRadius +
                " in chunk " + chunkPos.x + ", " + chunkPos.z + " (candidates=" + candidates.size() + ")");

        // re-create the same pillarRandom that carved offsets (important: deterministic wobble)
        RandomSource pillarRandom = RandomSource.create(chosen.seed);

        boolean carvedAnything = carvePillarColumn(
                ctx,
                config,
                chunk,
                biomeGetter,
                aquifer,
                carvingMask,
                pillarRandom,
                chosen.centerX,
                chosen.centerZ,
                chosen.baseRadius,
                chosen.wobbleScale,
                chosen.wobbleFreq,
                chosen.pulsePhase
        );

        return carvedAnything;
    }

    private static class PillarCandidate {
        final long seed;
        final float centerX;
        final float centerZ;
        final float baseRadius;
        final float wobbleScale;
        final float wobbleFreq;
        final float pulsePhase;

        PillarCandidate(long seed,
                        float centerX,
                        float centerZ,
                        float baseRadius,
                        float wobbleScale,
                        float wobbleFreq,
                        float pulsePhase) {
            this.seed = seed;
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.baseRadius = baseRadius;
            this.wobbleScale = wobbleScale;
            this.wobbleFreq = wobbleFreq;
            this.pulsePhase = pulsePhase;
        }
    }

    private long getPillarSeed(int cellX, int cellZ) {
        long k = 12345L;
        k ^= (long) cellX * 341873128712L;
        k ^= (long) cellZ * 132897987541L;
        k ^= 0x9E3779B97F4A7C15L;
        return k;
    }

    private boolean pillarTouchesChunk(
            float centerX,
            float centerZ,
            float radius,
            int chunkMinX,
            int chunkMinZ,
            int chunkMaxX,
            int chunkMaxZ
    ) {
        float closestX = Mth.clamp(centerX, chunkMinX, chunkMaxX + 1);
        float closestZ = Mth.clamp(centerZ, chunkMinZ, chunkMaxZ + 1);
        float dx = centerX - closestX;
        float dz = centerZ - closestZ;
        float dist2 = dx * dx + dz * dz;
        return dist2 <= radius * radius;
    }

    private boolean carvePillarColumn(
            CarvingContext ctx,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Aquifer aquifer,
            CarvingMask carvingMask,
            RandomSource randForThisPillar,
            float centerX,
            float centerZ,
            float baseRadius,
            float wobbleScale,
            float wobbleFreq,
            float pulsePhase
    ) {
        boolean carvedAnything = false;

        int minY = ctx.getMinGenY();
        int maxY = minY + ctx.getGenDepth() - 1;
        int startY = maxY;

        float baseOffsetX = (randForThisPillar.nextFloat() - 0.5f) * 6f;
        float baseOffsetZ = (randForThisPillar.nextFloat() - 0.5f) * 6f;

        ChunkPos chunkPos = chunk.getPos();

        for (int y = startY; y >= minY + 1; y--) {

            float leanAngle = y * wobbleFreq + pulsePhase;
            float leanX = Mth.sin(leanAngle) * wobbleScale;
            float leanZ = Mth.cos(leanAngle * 0.9f) * wobbleScale;

            float cx = centerX + baseOffsetX + leanX;
            float cz = centerZ + baseOffsetZ + leanZ;

            float radiusPulse = Mth.sin(y * 0.15f + pulsePhase) * (baseRadius * 0.12f);
            float rHere = baseRadius + radiusPulse;

            // taper with depth, but not so hard that it vanishes at mid-height
            float depthFrac = (float)(startY - y) / (float)(startY - (minY + 1) + 1);
            rHere *= (1.0f - 0.45f * depthFrac);
            if (rHere < 1.5f) {
                rHere = 1.5f;
            }

            boolean carvedLayer = carveDiscLayer(
                    ctx,
                    config,
                    chunk,
                    biomeGetter,
                    aquifer,
                    carvingMask,
                    cx,
                    y,
                    cz,
                    rHere
            );

            if (carvedLayer) {
                carvedAnything = true;
            }

            // DEBUG: leave a bright spine to visually locate the pillar in-world
            if (DEBUG_MARKER) {
                BlockPos markerPos = BlockPos.containing(cx, y, cz);

                if (chunkPos.getMinBlockX() <= markerPos.getX() && markerPos.getX() <= chunkPos.getMaxBlockX()
                        && chunkPos.getMinBlockZ() <= markerPos.getZ() && markerPos.getZ() <= chunkPos.getMaxBlockZ()) {

                    if (chunk.getBlockState(markerPos).isAir()) {
                        chunk.setBlockState(markerPos, Blocks.SEA_LANTERN.defaultBlockState(), false);
                    }
                }
            }
        }

        if (carvedAnything) {
            System.out.println("[PillarFieldCarver] carved column in chunk " + chunk.getPos());
        }

        return carvedAnything;
    }

    private boolean carveDiscLayer(
            CarvingContext ctx,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Aquifer aquifer,
            CarvingMask carvingMask,
            float cx,
            int cy,
            float cz,
            float radius
    ) {
        boolean carved = false;

        ChunkPos chunkPos = chunk.getPos();

        int minLocalX = Mth.floor(cx - radius) - chunkPos.getMinBlockX();
        int maxLocalX = Mth.floor(cx + radius) - chunkPos.getMinBlockX();
        int minLocalZ = Mth.floor(cz - radius) - chunkPos.getMinBlockZ();
        int maxLocalZ = Mth.floor(cz + radius) - chunkPos.getMinBlockZ();

        minLocalX = Mth.clamp(minLocalX, 0, 15);
        maxLocalX = Mth.clamp(maxLocalX, 0, 15);
        minLocalZ = Mth.clamp(minLocalZ, 0, 15);
        maxLocalZ = Mth.clamp(maxLocalZ, 0, 15);

        MutableBlockPos carvePos = new MutableBlockPos();
        MutableBlockPos checkPos = new MutableBlockPos();
        MutableBoolean reachedSurface = new MutableBoolean(false);

        for (int localX = minLocalX; localX <= maxLocalX; localX++) {
            float worldX = chunkPos.getMinBlockX() + localX + 0.5f;
            float dx = worldX - cx;
            float dx2 = dx * dx;

            for (int localZ = minLocalZ; localZ <= maxLocalZ; localZ++) {
                float worldZ = chunkPos.getMinBlockZ() + localZ + 0.5f;
                float dz = worldZ - cz;
                float dist2 = dx2 + dz * dz;

                if (dist2 <= radius * radius) {

                    int bx = Mth.floor(worldX);
                    int bz = Mth.floor(worldZ);

                    carvePos.set(bx, cy, bz);
                    checkPos.set(bx, cy + 1, bz);

                    if (this.carveBlock(
                            ctx,
                            config,
                            chunk,
                            biomeGetter,
                            carvingMask,
                            carvePos,
                            checkPos,
                            aquifer,
                            reachedSurface

                    )) {
                        carved = true;
                    }
                }
            }
        }

        return carved;
    }

    protected boolean canReach(ChunkPos chunkPos, double x, double z, int radius) {
        int midX = chunkPos.getMiddleBlockX();
        int midZ = chunkPos.getMiddleBlockZ();
        double dx = x - midX;
        double dz = z - midZ;
        return (dx * dx + dz * dz) - radius <= 64.0 * 64.0;
    }
}
