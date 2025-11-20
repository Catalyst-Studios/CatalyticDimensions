package com.catalyst.catalystdimensions.datagen;

import com.catalyst.catalystdimensions.block.ModBlocks;
import com.catalyst.catalystdimensions.block.spec.BlockSpec;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ModItemModelProvider extends ItemModelProvider {

    private static final String MODID = "catalystdimensions";

    public ModItemModelProvider(PackOutput output, ExistingFileHelper efh) {
        super(output, MODID, efh);
    }

    @Override
    protected void registerModels() {
        for (var entry : ModBlocks.ALL) {
            BlockSpec spec = entry.spec();
            if (entry.item() == null) {
                continue;
            }

            boolean hasLayers = spec.layers != null && !spec.layers.isEmpty();

            // 1) Explicit override wins
            if (spec.itemModel != null) {
                spec.itemModel.accept(this);
                continue;
            }

            // 2) Layered non-CTM blocks -> layered 3D cube item
            if (hasLayers && spec.connected == null) {
                buildLayeredItemModel(spec);
                continue;
            }

            // 3) CTM blocks
            if (spec.connected != null) {
                if (hasLayers) {
                    // CTM + layers: use layered item model (no CTM tiling for items)
                    buildLayeredItemModel(spec);
                } else {
                    // Legacy CTM-only behaviour
                    BlockSpec.Connected c = spec.connected;
                    String texBase = (c.textureBase != null && !c.textureBase.isBlank())
                            ? c.textureBase
                            : spec.name;

                    if (spec.tintRgb != null) {
                        buildTintableCubeItem(spec.name, "block/" + texBase);
                    } else {
                        withExistingParent(spec.name, mcLoc("block/cube_all"))
                                .texture("all", modLoc("block/" + texBase));
                    }
                }
                continue;
            }

            // 4) Non-CTM, non-layered: simple cube_all fallback
            withExistingParent(spec.name, mcLoc("block/cube_all"))
                    .texture("all", modLoc("block/" + spec.name));
        }
    }

    /**
     * Builds a layered 3D cube item model using BlockSpec.layers.
     * Respects per-layer item overrides and skip flags.
     */
    private void buildLayeredItemModel(BlockSpec spec) {
        List<BlockSpec.Layer> layers = new ArrayList<>(spec.layers);
        if (layers.isEmpty()) {
            return;
        }
        layers.sort(Comparator.comparingInt(l -> l.height));

        ItemModelBuilder builder = getBuilder(spec.name)
                .parent(getExistingFile(mcLoc("block/block")));

        int idx = 0;
        for (BlockSpec.Layer layer : layers) {
            // NEW: skip layers that shouldn't appear on the item
            if (!layer.showOnItem) {
                continue;
            }

            String texKey = "layer" + idx++;

            // Base texture path = override (if any) else block layer texture
            String texPath = (layer.itemTexture != null)
                    ? layer.itemTexture
                    : layer.texture;

            // Optional convenience: if this is a CTM sheet, you MAY strip "_ctm".
            // If you don't want that, just comment out this block.
            if (layer.ctm && texPath.endsWith("_ctm")) {
                texPath = texPath.substring(0, texPath.length() - "_ctm".length());
            }

            builder.texture(texKey, modLoc(texPath));

            ItemModelBuilder.ElementBuilder element = builder.element()
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
    }

    /**
     * Generates a cube model with tintindex=0 on all faces,
     * for simple tinted CTM blocks without layers.
     */
    private void buildTintableCubeItem(String name, String texturePath) {
        ItemModelBuilder builder = getBuilder(name)
                .parent(getExistingFile(mcLoc("block/block")))
                .texture("all", modLoc(texturePath));

        builder.element()
                .from(0, 0, 0)
                .to(16, 16, 16)
                .allFaces((dir, faceBuilder) ->
                        faceBuilder.texture("#all").cullface(dir).tintindex(0))
                .end();
    }
}
