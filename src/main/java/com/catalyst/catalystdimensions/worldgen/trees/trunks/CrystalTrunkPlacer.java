package com.catalyst.catalystdimensions.worldgen.trees.trunks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CrystalTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<CrystalTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("base_height").forGetter(p -> p.baseHeight),
            Codec.INT.fieldOf("height_rand_a").forGetter(p -> p.heightRandA),
            Codec.INT.fieldOf("height_rand_b").forGetter(p -> p.heightRandB)
    ).apply(instance, CrystalTrunkPlacer::new));

    public CrystalTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTrunkPlacers.CRYSTAL.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> replacer,
            RandomSource random,
            int height,
            BlockPos startPos,
            TreeConfiguration config
    ) {
        List<FoliagePlacer.FoliageAttachment> attachments = new ArrayList<>();
        int maxRadius = 5;

        // Generate the main tapered trunk with natural narrowing
        for (int y = 0; y < height; y++) {
            // Calculate how far along we are in the height, to taper gradually
            float progress = (float) y / (height - 1); // From 0 (base) to 1 (top)
            float taper = 1.0f - progress;             // Taper from 1.0 to 0.0
            int radius = Math.max(1, Math.round(maxRadius * taper)); // Shrink radius towards 1

            // Place the blocks in the radius at this height level
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        BlockPos pos = startPos.offset(dx, y, dz);
                        BlockState logState = config.trunkProvider.getState(random, pos);
                        replacer.accept(pos, logState);
                        placeFoliageShell(level, replacer, random, pos, config);
                    }
                }
            }
        }

        // Add branches (limited to bottom ~20% of tree)
        int branchMaxY = (int) Math.floor(height * 0.2); // Limit to bottom 20%, rounded down
        List<int[]> branchDirections = new ArrayList<>();  // Track directions to avoid duplicates

        for (int i = 0; i < 6 + random.nextInt(10); i++) { // 6–16 branches
            int branchStartY = random.nextInt(branchMaxY + 1); // Ensure branches start within bottom 20%
            BlockPos base = startPos.above(branchStartY);

            // Randomly choose a direction that doesn't match any existing ones
            int dx, dz;
            do {
                dx = random.nextInt(3) - 1;  // Will give values from -1 to 1
                dz = random.nextInt(3) - 1;  // Will give values from -1 to 1
            } while (dx == 0 && dz == 0 || branchDirections.contains(new int[]{dx, dz}));

            // Record this direction
            branchDirections.add(new int[]{dx, dz});

            int branchLength = 5 + random.nextInt(15); // 5–20
            int baseBranchRadius = 3 + random.nextInt(3); // 3–6
            BlockPos current = base;

            // Add the branch
            for (int j = 0; j < branchLength; j++) {
                current = current.offset(dx, 1, dz); // Diagonal upward

                float branchTaper = 1.0f - (float) j / branchLength;
                int branchRadius = Math.max(1, Math.round(baseBranchRadius * branchTaper));

                for (int bx = -branchRadius; bx <= branchRadius; bx++) {
                    for (int bz = -branchRadius; bz <= branchRadius; bz++) {
                        if (bx * bx + bz * bz <= branchRadius * branchRadius) {
                            BlockPos branchPos = current.offset(bx, 0, bz);
                            replacer.accept(branchPos, config.trunkProvider.getState(random, branchPos));
                            placeFoliageShell(level, replacer, random, branchPos, config);
                        }
                    }
                }

                // Add foliage attachment at the branch tip
                if (j == branchLength - 1) {
                    attachments.add(new FoliagePlacer.FoliageAttachment(current, 0, false));
                }
            }
        }

        return attachments;
    }

    // Adds a small foliage shell around the block
    private void placeFoliageShell(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, BlockPos center, TreeConfiguration config) {
        int foliageRadius = 1;
        for (int fx = -foliageRadius; fx <= foliageRadius; fx++) {
            for (int fy = -foliageRadius; fy <= foliageRadius; fy++) {
                for (int fz = -foliageRadius; fz <= foliageRadius; fz++) {
                    if (fx == 0 && fy == 0 && fz == 0) continue;
                    BlockPos foliagePos = center.offset(fx, fy, fz);
                    if (level.isStateAtPosition(foliagePos, BlockBehaviour.BlockStateBase::isAir)) {
                        replacer.accept(foliagePos, config.foliageProvider.getState(random, foliagePos));
                    }
                }
            }
        }
    }
}
