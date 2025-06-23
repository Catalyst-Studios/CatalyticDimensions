package com.catalyst.catalystdimensions.worldgen.features.customfeatures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public record CrystalFeatureConfiguration(
        List<MaterialSet> materials,
        int minBranches,
        int maxBranches,
        int baseBranchLength,
        int branchLengthRandomness,
        int baseBranchWidth,
        int branchWidthRandomness,
        float taperExponent,
        Block notReplaceable // Now correctly typed as List<Block>
) implements FeatureConfiguration {

    public static final Codec<CrystalFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MaterialSet.CODEC.listOf().fieldOf("materials").forGetter(CrystalFeatureConfiguration::materials),
            Codec.INT.fieldOf("min_branches").forGetter(CrystalFeatureConfiguration::minBranches),
            Codec.INT.fieldOf("max_branches").forGetter(CrystalFeatureConfiguration::maxBranches),
            Codec.INT.fieldOf("base_branch_length").forGetter(CrystalFeatureConfiguration::baseBranchLength),
            Codec.INT.fieldOf("branch_length_randomness").forGetter(CrystalFeatureConfiguration::branchLengthRandomness),
            Codec.INT.fieldOf("base_branch_width").forGetter(CrystalFeatureConfiguration::baseBranchWidth),
            Codec.INT.fieldOf("branch_width_randomness").forGetter(CrystalFeatureConfiguration::branchWidthRandomness),
            Codec.FLOAT.fieldOf("taper_exponent").forGetter(CrystalFeatureConfiguration::taperExponent),
            Block.CODEC.fieldOf("not_replaceable").forGetter(CrystalFeatureConfiguration::notReplaceable)



    ).apply(instance, CrystalFeatureConfiguration::new));

    public record MaterialSet(BlockState core, BlockState shell) {
        public static final Codec<MaterialSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("core").forGetter(MaterialSet::core),
                BlockState.CODEC.fieldOf("shell").forGetter(MaterialSet::shell)
        ).apply(instance, MaterialSet::new));
    }
    public record Block(List<BlockState> notReplaceable) {
        public static final Codec<Block> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.listOf().fieldOf("not_replaceable").forGetter(Block::notReplaceable)
        ).apply(instance, Block::new));
    }}

