package com.catalyst.catalystdimensions.datagen;

import com.catalyst.catalystdimensions.block.ModBlocks;
import com.catalyst.catalystdimensions.block.spec.BlockSpec;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.catalyst.catalystdimensions.CatalystDimensions.MODID;

/**
 * Datagen for blockstates and block models.
 *
 * Extended to support:
 *  - Simple axis log/wood helpers
 *  - CTM blocks via Connected config on BlockSpec
 *  - Layered non-CTM blocks using BlockSpec.layers
 */
public final class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper efh) {
        super(output, MODID, efh);
    }

    // --- “log-like” (side + top) ---
    public void modAxisLog(Block b, String name) {
        if (!(b instanceof RotatedPillarBlock pillar)) {
            throw new IllegalArgumentException("modAxisLog requires RotatedPillarBlock: " + b);
        }

        ResourceLocation side = modLoc("block/" + name);          // bark
        ResourceLocation top  = modLoc("block/" + name + "_top"); // end grain
        axisBlock(pillar, side, top);

        // Item model: parent -> block/<name>
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }

    // --- “wood-like” (bark on all faces) ---
    public void modAxisWood(Block b, String name) {
        if (!(b instanceof RotatedPillarBlock pillar)) {
            throw new IllegalArgumentException("modAxisWood requires RotatedPillarBlock: " + b);
        }

        ResourceLocation bark = modLoc("block/" + name);
        axisBlock(pillar, bark, bark);
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }

    // Simple cube_all helper (matches your default “cubeAll” case)
    public void modCubeAll(Block b, String name) {
        var model = models().cubeAll(name, modLoc("block/" + name));
        simpleBlock(b, model);
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }

    /**
     * Builds a layered cube model for non-CTM blocks using BlockSpec.layers.
     * Each layer becomes an element with its own texture and optional tint.
     *
     * CTM information on layers (layer.ctm) is currently metadata-only – CTM
     * still uses the single-block Connected config. You can extend your CTM
     * geometry later to consume per-layer CTM if desired.
     */
    private void buildLayeredBlock(Block block, BlockSpec spec) {
        List<BlockSpec.Layer> layers = new ArrayList<>(spec.layers);
        if (layers.isEmpty()) {
            // Fallback – shouldn't normally happen if caller checked
            simpleBlock(block, models().cubeAll(spec.name, blockTexture(block)));
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + spec.name)));
            return;
        }

        layers.sort(Comparator.comparingInt(l -> l.height));

        BlockModelBuilder builder = models().getBuilder(spec.name)
                .parent(models().getExistingFile(mcLoc("block/block")));

        int idx = 0;
        for (BlockSpec.Layer layer : layers) {
            String texKey = "layer" + idx++;
            builder.texture(texKey, modLoc(layer.texture));

            BlockModelBuilder.ElementBuilder element = builder.element()
                    .from(0, 0, 0)
                    .to(16, 16, 16);

            element.allFaces((dir, face) -> {
                face.texture("#" + texKey).cullface(dir);
                if (layer.tintIndex >= 0) {
                    face.tintindex(layer.tintIndex);
                }
            });

            element.end();
        }

        simpleBlock(block, builder);
        simpleBlockItem(block, builder);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var entry : ModBlocks.ALL) {
            BlockSpec spec = entry.spec();
            Block block = entry.block().get();

            boolean hasLayers = spec.layers != null && !spec.layers.isEmpty();

            // 1) Layered non-CTM blocks
            if (hasLayers && spec.connected == null) {
                buildLayeredBlock(block, spec);
                continue;
            }

            // 2) CTM blocks (single CTM texture per block)
            if (spec.connected != null) {
                BlockSpec.Connected c = spec.connected;

                // Collect CTM-enabled layers (layer.ctm == true), sorted by height
                List<BlockSpec.Layer> ctmLayers = new ArrayList<>();
                if (spec.layers != null) {
                    for (BlockSpec.Layer layer : spec.layers) {
                        if (layer.ctm) {
                            ctmLayers.add(layer);
                        }
                    }
                    ctmLayers.sort(Comparator.comparingInt(l -> l.height));
                }

                // Build CTM model
                BlockModelBuilder ctmModel = models().getBuilder(spec.name + "_ctm")
                        .parent(models().getExistingFile(mcLoc("block/block")))
                        .customLoader((builder, efh) -> new CustomLoaderBuilder<BlockModelBuilder>(
                                ResourceLocation.fromNamespaceAndPath(MODID, "ctm"),
                                builder,
                                efh,
                                true
                        ) {
                            @Override
                            public JsonObject toJson(JsonObject json) {
                                json = super.toJson(json);
                                json.addProperty("tile_size", c.tileSize);
                                json.addProperty("tiles", c.tiles);
                                json.addProperty("cull_interior", c.cullInterior);

                                // If no per-layer tints, fall back to global tintRgb
                                boolean anyTint = spec.tintRgb != null;
                                if (spec.layers != null) {
                                    for (BlockSpec.Layer layer : spec.layers) {
                                        if (layer.tintRgb != null) {
                                            anyTint = true;
                                            break;
                                        }
                                    }
                                }
                                if (anyTint) {
                                    json.addProperty("tinted", true);
                                }

                                // NEW: overlay_layers for CTM-per-layer
                                if (!ctmLayers.isEmpty()) {
                                    var arr = new com.google.gson.JsonArray();

                                    // first CTM layer is the base; remaining are overlays
                                    for (int i = 0; i < ctmLayers.size(); i++) {
                                        BlockSpec.Layer layer = ctmLayers.get(i);
                                        String texKey = (i == 0) ? "ctm" : ("overlay_" + (i - 1));

                                        com.google.gson.JsonObject o = new com.google.gson.JsonObject();
                                        o.addProperty("texture", texKey);
                                        o.addProperty("tintindex", layer.tintIndex);
                                        arr.add(o);
                                    }

                                    json.add("overlay_layers", arr);
                                }

                                return json;
                            }
                        }).end();

                // Bind textures:
                if (!ctmLayers.isEmpty()) {
                    // Base CTM texture from the first CTM layer
                    BlockSpec.Layer base = ctmLayers.get(0);
                    ctmModel.texture("particle", modLoc(base.texture));
                    ctmModel.texture("ctm",      modLoc(base.texture));

                    // Overlay CTM textures from the remaining CTM layers
                    for (int i = 1; i < ctmLayers.size(); i++) {
                        BlockSpec.Layer layer = ctmLayers.get(i);
                        String key = "overlay_" + (i - 1);
                        ctmModel.texture(key, modLoc(layer.texture));
                    }
                } else {
                    // Fallback: old behaviour (no per-layer CTM)
                    String texBase = (c.textureBase != null && !c.textureBase.isBlank())
                            ? c.textureBase
                            : spec.name;
                    ctmModel.texture("particle", modLoc("block/" + texBase + "_ctm"));
                    ctmModel.texture("ctm",      modLoc("block/" + texBase + "_ctm"));
                }

                // Alias simple model name to CTM model
                models().getBuilder(spec.name)
                        .parent(models().getExistingFile(modLoc("block/" + spec.name + "_ctm")));

                // Blockstates
                if (spec.blockstates != null) {
                    spec.blockstates.accept(block, this);
                } else {
                    getVariantBuilder(block).partialState().modelForState()
                            .modelFile(models().getExistingFile(modLoc("block/" + spec.name)))
                            .addModel();
                }


                continue;
            }


            // 3) Non-CTM blocks without layers: existing behaviour
            if (spec.blockstates != null) {
                spec.blockstates.accept(block, this);
            } else {
                simpleBlock(block, models().cubeAll(spec.name, blockTexture(block)));
            }
        }
    }
}
