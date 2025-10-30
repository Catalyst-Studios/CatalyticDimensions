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
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PillarFieldCarver extends WorldCarver<CaveCarverConfiguration> {

    /* =========================================================
     *  CONFIGURABLE PARAMETERS
     * =========================================================
     */

    // --- Pillar distribution ---
    private static final int CELL_SIZE = 48;          // Distance between potential pillar origins
    private static final float PILLAR_CHANCE = 0.30f; // Chance of pillar appearing in a cell
    private static final int SEARCH_RADIUS = 40;      // Extra range to check nearby pillars

    // --- Pillar size ---
    private static final float NORMAL_RADIUS_MIN = 12.0f;
    private static final float NORMAL_RADIUS_MAX = 24.0f;
    private static final float MEGA_RADIUS_MIN = 48.0f;
    private static final float MEGA_RADIUS_MAX = 64.0f;
    private static final float MEGA_PILLAR_CHANCE = 0.07f;
    private static final float CLEAR_BAND = 32.0f;    // Moat ring width around each pillar

    // --- Wobble / organic deformation ---
    private static final float WOBBLE_SCALE_MIN = 0.6f;
    private static final float WOBBLE_SCALE_MAX = 1.4f;
    private static final float NOISE_VERTICAL_SCALE = 0.05f; // How fast pillars bend along Y

    // --- Shape & taper ---
    private static final float TAPER_STRENGTH = 0.00f;
    private static final float MIN_CORE_RADIUS = 12.0f;

    // --- Moat spiral twist ---
    private static final float TWIST_RADIUS = 2.5f;
    private static final float TWIST_FREQ   = 0.08f;

    // --- Random center offsets per pillar ---
    private static final float BASE_OFFSET_RANGE = 8.0f;

    // --- Debug visuals ---
    private static final boolean DEBUG_MARKER = false;

    /* =========================================================
     *  CONSTRUCTOR
     * =========================================================
     */
    public PillarFieldCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    /* =========================================================
     *  MAIN CARVE LOGIC
     * =========================================================
     */
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
        if (!isStartChunk(config, random)) return false;

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxX = chunkMinX + 15;
        int chunkMaxZ = chunkMinZ + 15;

        int worldMinX = chunkMinX - SEARCH_RADIUS;
        int worldMinZ = chunkMinZ - SEARCH_RADIUS;
        int worldMaxX = chunkMaxX + SEARCH_RADIUS;
        int worldMaxZ = chunkMaxZ + SEARCH_RADIUS;

        int cellMinX = Math.floorDiv(worldMinX, CELL_SIZE);
        int cellMinZ = Math.floorDiv(worldMinZ, CELL_SIZE);
        int cellMaxX = Math.floorDiv(worldMaxX, CELL_SIZE);
        int cellMaxZ = Math.floorDiv(worldMaxZ, CELL_SIZE);

        List<PillarCandidate> pillars = new ArrayList<>();

        // --- Generate pillar candidates around this chunk ---
        for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
            for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {

                long seed = pillarSeed(cellX, cellZ);
                RandomSource r = RandomSource.create(seed);

                if (r.nextFloat() > PILLAR_CHANCE) continue;

                float cx = cellX * CELL_SIZE + r.nextFloat() * CELL_SIZE;
                float cz = cellZ * CELL_SIZE + r.nextFloat() * CELL_SIZE;

                float radius = (r.nextFloat() < MEGA_PILLAR_CHANCE)
                        ? MEGA_RADIUS_MIN + r.nextFloat() * (MEGA_RADIUS_MAX - MEGA_RADIUS_MIN)
                        : NORMAL_RADIUS_MIN + r.nextFloat() * (NORMAL_RADIUS_MAX - NORMAL_RADIUS_MIN);

                if (!touchesChunk(cx, cz, radius + CLEAR_BAND + 4.0f, chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ))
                    continue;

                float wobble = WOBBLE_SCALE_MIN + r.nextFloat() * (WOBBLE_SCALE_MAX - WOBBLE_SCALE_MIN);
                float phase = r.nextFloat() * 1000f;

                RandomSource nxRand = RandomSource.create(seed ^ 0x4f321abfL);
                RandomSource nzRand = RandomSource.create(seed ^ 0x9e3779b97f4a7c15L);
                PerlinSimplexNoise noiseX = new PerlinSimplexNoise(nxRand, List.of(0,0,1,1));
                PerlinSimplexNoise noiseZ = new PerlinSimplexNoise(nzRand,List.of(0,1,0,1));

                float offsetX = (r.nextFloat() - 0.5f) * BASE_OFFSET_RANGE;
                float offsetZ = (r.nextFloat() - 0.5f) * BASE_OFFSET_RANGE;

                pillars.add(new PillarCandidate(seed, cx, cz, radius, wobble, phase, noiseX, noiseZ, offsetX, offsetZ));
            }
        }

        if (pillars.isEmpty()) return false;

        PillarCandidate chosen = pillars.get(random.nextInt(pillars.size()));
        RandomSource r = RandomSource.create(chosen.seed);

        return carvePillarColumn(ctx, config, chunk, biomeGetter, aquifer, carvingMask, r, chosen, pillars);
    }

    /* =========================================================
     *  PILLAR CARVING
     * =========================================================
     */
    private boolean carvePillarColumn(
            CarvingContext ctx,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Aquifer aquifer,
            CarvingMask carvingMask,
            RandomSource rand,
            PillarCandidate pillar,
            List<PillarCandidate> all
    ) {
        boolean carved = false;

        int minY = ctx.getMinGenY();
        int maxY = minY + ctx.getGenDepth() - 1;
        int startY = maxY;
        ChunkPos pos = chunk.getPos();

        for (int y = startY; y >= minY + 1; y--) {
            PillarSample s = samplePillarCoreAtY(pillar, y, startY, minY);

            // Spiral carve around pillar
            float twistA = (y * TWIST_FREQ) + pillar.pulsePhase;
            float twistX = Mth.sin(twistA) * TWIST_RADIUS;
            float twistZ = Mth.cos(twistA) * TWIST_RADIUS;

            float carveX = s.cx + twistX;
            float carveZ = s.cz + twistZ;

            float innerR = s.radius;
            float outerR = s.radius + CLEAR_BAND;

            if (carveDiscLayer(ctx, config, chunk, biomeGetter, aquifer, carvingMask, carveX, y, carveZ,
                    innerR, outerR, all, y, startY, minY)) carved = true;

            // Optional debug: pillar core line
            if (DEBUG_MARKER) {
                BlockPos p = BlockPos.containing(s.cx, y, s.cz);
                if (pos.getMinBlockX() <= p.getX() && p.getX() <= pos.getMaxBlockX()
                        && pos.getMinBlockZ() <= p.getZ() && p.getZ() <= pos.getMaxBlockZ()) {
                    if (chunk.getBlockState(p).isAir())
                        chunk.setBlockState(p, Blocks.SEA_LANTERN.defaultBlockState(), false);
                }
            }
        }
        return carved;
    }

    /* =========================================================
     *  DISC CARVING (MOAT)
     * =========================================================
     */
    private boolean carveDiscLayer(
            CarvingContext ctx,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            Aquifer aquifer,
            CarvingMask carvingMask,
            float cx, int cy, float cz,
            float innerR, float outerR,
            List<PillarCandidate> pillars,
            int y, int startY, int minY
    ) {
        boolean carved = false;
        ChunkPos pos = chunk.getPos();

        float inner2 = innerR * innerR;
        float outer2 = outerR * outerR;

        MutableBlockPos carvePos = new MutableBlockPos();
        MutableBlockPos checkPos = new MutableBlockPos();
        MutableBoolean surface = new MutableBoolean(false);

        int minLocalX = Mth.clamp(Mth.floor(cx - outerR) - pos.getMinBlockX(), 0, 15);
        int maxLocalX = Mth.clamp(Mth.floor(cx + outerR) - pos.getMinBlockX(), 0, 15);
        int minLocalZ = Mth.clamp(Mth.floor(cz - outerR) - pos.getMinBlockZ(), 0, 15);
        int maxLocalZ = Mth.clamp(Mth.floor(cz + outerR) - pos.getMinBlockZ(), 0, 15);

        for (int lx = minLocalX; lx <= maxLocalX; lx++) {
            float wx = pos.getMinBlockX() + lx + 0.5f;
            float dx = wx - cx;
            float dx2 = dx * dx;

            for (int lz = minLocalZ; lz <= maxLocalZ; lz++) {
                float wz = pos.getMinBlockZ() + lz + 0.5f;
                float dz = wz - cz;
                float dist2 = dx2 + dz * dz;

                if (dist2 < inner2 || dist2 > outer2) continue;

                // 🔒 Stop carving if this position is inside any pillar's core
                if (isInsideAnyPillarCore(wx, wz, pillars, y, startY, minY))
                    continue;

                carvePos.set(Mth.floor(wx), cy, Mth.floor(wz));
                checkPos.set(carvePos.getX(), cy + 1, carvePos.getZ());

                if (this.carveBlock(ctx, config, chunk, biomeGetter, carvingMask, carvePos, checkPos, aquifer, surface))
                    carved = true;
            }
        }
        return carved;
    }

    /* =========================================================
     *  HELPER FUNCTIONS
     * =========================================================
     */
    private boolean isInsideAnyPillarCore(float wx, float wz, List<PillarCandidate> pillars, int y, int startY, int minY) {
        for (PillarCandidate p : pillars) {
            PillarSample s = samplePillarCoreAtY(p, y, startY, minY);
            float dx = wx - s.cx;
            float dz = wz - s.cz;
            if (dx * dx + dz * dz <= s.radius * s.radius) return true;
        }
        return false;
    }

    private PillarSample samplePillarCoreAtY(PillarCandidate p, int y, int startY, int minY) {
        double ny = y * NOISE_VERTICAL_SCALE;
        float leanX = (float) p.noiseX.getValue(ny, 0.0, true) * p.wobbleScale;
        float leanZ = (float) p.noiseZ.getValue(ny, 0.0, true) * p.wobbleScale;

        float cx = p.centerX + p.baseOffsetX + leanX;
        float cz = p.centerZ + p.baseOffsetZ + leanZ;

        float pulse = Mth.sin(y * 0.15f + p.pulsePhase) * (p.baseRadius * 0.12f);
        float radius = p.baseRadius + pulse;

        float depthFrac = (float) (startY - y) / (float) (startY - (minY + 1) + 1);
        radius *= (1.0f - TAPER_STRENGTH * depthFrac);
        return new PillarSample(cx, cz, Math.max(radius, MIN_CORE_RADIUS));
    }

    private boolean touchesChunk(float cx, float cz, float r, int minX, int minZ, int maxX, int maxZ) {
        float closestX = Mth.clamp(cx, minX, maxX + 1);
        float closestZ = Mth.clamp(cz, minZ, maxZ + 1);
        float dx = cx - closestX;
        float dz = cz - closestZ;
        return dx * dx + dz * dz <= r * r;
    }

    private long pillarSeed(int cx, int cz) {
        long k = 12345L;
        k ^= (long) cx * 341873128712L;
        k ^= (long) cz * 132897987541L;
        k ^= 0x9E3779B97F4A7C15L;
        return k;
    }

    protected boolean canReach(ChunkPos chunkPos, double x, double z, int radius) {
        int midX = chunkPos.getMiddleBlockX();
        int midZ = chunkPos.getMiddleBlockZ();
        double dx = x - midX;
        double dz = z - midZ;
        return (dx * dx + dz * dz) - radius <= 64.0 * 64.0;
    }

    /* =========================================================
     *  INTERNAL CLASSES
     * =========================================================
     */
    private static class PillarCandidate {
        final long seed;
        final float centerX, centerZ;
        final float baseRadius;
        final float wobbleScale;
        final float pulsePhase;
        final PerlinSimplexNoise noiseX, noiseZ;
        final float baseOffsetX, baseOffsetZ;

        PillarCandidate(long seed, float x, float z, float radius, float wobble, float phase,
                        PerlinSimplexNoise nx, PerlinSimplexNoise nz, float offX, float offZ) {
            this.seed = seed;
            this.centerX = x;
            this.centerZ = z;
            this.baseRadius = radius;
            this.wobbleScale = wobble;
            this.pulsePhase = phase;
            this.noiseX = nx;
            this.noiseZ = nz;
            this.baseOffsetX = offX;
            this.baseOffsetZ = offZ;
        }
    }

    private static class PillarSample {
        final float cx, cz, radius;
        PillarSample(float cx, float cz, float radius) {
            this.cx = cx;
            this.cz = cz;
            this.radius = radius;
        }
    }
}
