package com.catalyst.catalystdimensions.worldgen.trees.blobfoliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CrystalFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<CrystalFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(p -> p.radius),
            IntProvider.CODEC.fieldOf("height").forGetter(p -> p.heightProvider)
    ).apply(instance, CrystalFoliagePlacer::new));

    private final int radius;
    private final IntProvider heightProvider;

    public CrystalFoliagePlacer(int radius, IntProvider heightProvider) {
        super(ConstantInt.of(radius), ConstantInt.of(0));
        this.radius = radius;
        this.heightProvider = heightProvider;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModFoliagePlacers.CRYSTAL_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader levelSimulatedReader, FoliageSetter foliageSetter, RandomSource randomSource, TreeConfiguration treeConfiguration, int i, FoliageAttachment foliageAttachment, int i1, int i2, int i3) {
        // Not used in this custom placer
    }

    @Override
    public int foliageHeight(RandomSource randomSource, int i, TreeConfiguration treeConfiguration) {
        return this.heightProvider.sample(randomSource);
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource randomSource, int i, int j, int k, int l, boolean b) {
        return false; // Place foliage at all positions
    }

    public List<FoliageAttachment> placeFoliage(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> replacer,
            RandomSource random,
            int foliageHeight,
            BlockPos pos,
            TreeConfiguration config
    ) {
        List<FoliageAttachment> attachments = new ArrayList<>();

        for (int y = 0; y < foliageHeight; y++) {
            float taper = 1.0f - (float) y / foliageHeight;
            int effectiveRadius = Math.max(1, Math.round(this.radius * taper));

            for (int dx = -effectiveRadius; dx <= effectiveRadius; dx++) {
                for (int dz = -effectiveRadius; dz <= effectiveRadius; dz++) {
                    if (dx * dx + dz * dz <= effectiveRadius * effectiveRadius) {
                        BlockPos foliagePos = pos.offset(dx, y, dz);
                        if (level.isStateAtPosition(foliagePos, BlockBehaviour.BlockStateBase::isAir)) {
                            replacer.accept(foliagePos, config.foliageProvider.getState(random, foliagePos));
                        }
                    }
                }
            }
        }

        // Final point
        BlockPos tip = pos.above(foliageHeight);
        if (level.isStateAtPosition(tip, BlockBehaviour.BlockStateBase::isAir)) {
            replacer.accept(tip, config.foliageProvider.getState(random, tip));
        }

        attachments.add(new FoliageAttachment(tip, 0, false));
        return attachments;
    }
}
