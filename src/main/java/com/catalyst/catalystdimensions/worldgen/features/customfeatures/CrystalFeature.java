package com.catalyst.catalystdimensions.worldgen.features.customfeatures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrystalFeature extends Feature<CrystalFeatureConfiguration> {

    public CrystalFeature(Codec<CrystalFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalFeatureConfiguration> context) {
        LevelAccessor level = context.level();
        BlockPos origin = context.origin();
        CrystalFeatureConfiguration config = context.config();
        RandomSource random = context.random();

        CrystalFeatureConfiguration.MaterialSet selectedMaterial = config.materials().get(random.nextInt(config.materials().size()));
        BlockState coreState = selectedMaterial.core().getBlock().defaultBlockState();
        BlockState outerState = selectedMaterial.shell().getBlock().defaultBlockState();

        int branches = config.minBranches() + random.nextInt(config.maxBranches() - config.minBranches() + 1);
        Set<Direction3D> usedDirections = new HashSet<>();

        for (int i = 0; i < branches; i++) {
            Direction3D direction = Direction3D.random(random, usedDirections);
            usedDirections.add(direction);

            int length = config.baseBranchLength() + random.nextInt(config.branchLengthRandomness() + 1);
            int width = config.baseBranchWidth() + random.nextInt(config.branchWidthRandomness() + 1);

            for (int j = 0; j < length; j++) {
                BlockPos pos = origin.offset(
                        Math.round(direction.x * j),
                        Math.round(direction.y * j),
                        Math.round(direction.z * j)
                );

                float taper = 1.0f - (float) Math.pow((float) j / length, config.taperExponent());
                int radius = Math.max(1, Math.round(width * taper));

                fillSphere(level, pos, radius, coreState, outerState, config, random);
            }
        }

        return true;
    }

    private void fillSphere(LevelAccessor level, BlockPos center, int radius, BlockState core, BlockState outer, CrystalFeatureConfiguration config, RandomSource random) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq <= radius * radius) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        BlockState current = level.getBlockState(pos);

                        if (config.notReplaceable().notReplaceable().contains(current)) continue;

                        boolean isOuter = distSq >= (radius - 1) * (radius - 1);
                        level.setBlock(pos, isOuter ? outer : core, 2);
                    }
                }
            }
        }
    }

    // 3D direction with biasing
    private record Direction3D(float x, float y, float z) {
        static Direction3D random(RandomSource random, Set<Direction3D> used) {
            for (int i = 0; i < 20; i++) {
                float x = random.nextFloat() * 2 - 1;
                float y = random.nextFloat() * 2 - 1;
                float z = random.nextFloat() * 2 - 1;
                Direction3D dir = new Direction3D(x, y, z);
                if (used.stream().noneMatch(u -> dir.similarDirection(u))) return dir;
            }
            return new Direction3D(0, 1, 0); // fallback
        }

        boolean similarDirection(Direction3D other) {
            float dot = x * other.x + y * other.y + z * other.z;
            return dot > 0.95;
        }
    }
}
