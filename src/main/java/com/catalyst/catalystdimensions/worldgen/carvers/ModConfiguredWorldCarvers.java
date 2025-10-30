package com.catalyst.catalystdimensions.worldgen.carvers;

import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class ModConfiguredWorldCarvers {

    public static final ResourceKey<ConfiguredWorldCarver<?>> CARVER_TEST_KEY =
            registerKey("test_cave");
    public static final ResourceKey<ConfiguredWorldCarver<?>> LARGE_CAVE_CONFIGURED =
            registerKey("large_cave_configured");
    public static final ResourceKey<ConfiguredWorldCarver<?>> PILLAR_FIELD =
            registerKey("pillar_field");

    public static void bootstrap(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        Holder<Block> stone = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.STONE).orElseThrow());
        Holder<Block> dirt = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.DIRT).orElseThrow());
        Holder<Block> grass = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.GRASS_BLOCK).orElseThrow());
        Holder<Block> granite = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.GRANITE).orElseThrow());
        Holder<Block> diorite = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.DIORITE).orElseThrow());
        Holder<Block> andesite = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.ANDESITE).orElseThrow());
        Holder<Block> gravel = BuiltInRegistries.BLOCK.getHolderOrThrow(BuiltInRegistries.BLOCK.getResourceKey(Blocks.GRAVEL).orElseThrow());

        HolderSet<Block> replaceable = HolderSet.direct(stone, dirt, granite, diorite, andesite, gravel,grass);

        context.register(
                CARVER_TEST_KEY,
                WorldCarver.CAVE.configured(
                        new CaveCarverConfiguration(
                0.25F, // higher cave spawn chance
                UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(192)),
                UniformFloat.of(0.7F, 1.5F),
                VerticalAnchor.aboveBottom(8),
                CarverDebugSettings.of(false, Blocks.LIGHT.defaultBlockState()),
                replaceable,
                UniformFloat.of(1.5F, 2.8F),  // width scale
                UniformFloat.of(1.5F, 2.5F),  // height scale
                UniformFloat.of(-0.6F, -0.2F) // thickness smoothing
        )));

        context.register(
                        LARGE_CAVE_CONFIGURED,
                        ModWorldCarversRegistry.LARGE_CAVE_CARVER.get().configured(
                        new CaveCarverConfiguration(
                                0.25F,
                                UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(300)),
                                UniformFloat.of(0.7F, 1.5F),
                                VerticalAnchor.aboveBottom(8),
                                CarverDebugSettings.of(true, Blocks.OAK_BUTTON.defaultBlockState()),
                                replaceable,
                                UniformFloat.of(0.7F, 1.3F),
                                UniformFloat.of(0.8F, 1.2F),
                                UniformFloat.of(-1.0F, -0.3F)
                        )
                )
        );
        context.register(
                PILLAR_FIELD,
                ModWorldCarversRegistry.PILLAR_FIELD_CARVER.get().configured(
                        new CaveCarverConfiguration(
                                0.4f, // probability per chunk to run this carver at all
                                UniformHeight.of(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(320)
                                ),
                                UniformFloat.of(0.7F, 1.5F), // matches your constructor shape
                                VerticalAnchor.aboveBottom(8),
                                CarverDebugSettings.of(
                                        false, Blocks.OAK_PLANKS.defaultBlockState()
                                ),
                                replaceable,
                                UniformFloat.of(0.7F, 1.3F),
                                UniformFloat.of(0.8F, 1.2F),
                                UniformFloat.of(-1.0F, -0.3F)
                        )
                )
        );

    }

    public static ResourceKey<ConfiguredWorldCarver<?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_CARVER,
                ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, name));
    }
}
