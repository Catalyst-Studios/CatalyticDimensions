package com.catalyst.catalystdimensions.datagen;

import com.catalyst.catalystdimensions.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    private static final String MODID = "catalystdimensions";

    public ModItemModelProvider(PackOutput output, ExistingFileHelper efh) {
        super(output, MODID, efh);
    }

    @Override
    protected void registerModels() {
        for (var entry : ModBlocks.ALL) {
            var spec = entry.spec();
            if (entry.item() == null) continue;

            // explicit override wins
            if (spec.itemModel != null) {
                spec.itemModel.accept(this);
                continue;
            }

            if (spec.connected != null) {
                var c = spec.connected;
                String texBase = (c.textureBase != null && !c.textureBase.isBlank())
                        ? c.textureBase : spec.name;

                if (spec.tintRgb != null) {
                    buildTintableCubeItem(spec.name, "block/" + texBase);
                } else {
                    // normal cube_all fallback
                    withExistingParent(spec.name, mcLoc("block/cube_all"))
                            .texture("all", modLoc("block/" + texBase));
                }
                continue;
            }

            // non-CTM fallback
            withExistingParent(spec.name, mcLoc("block/cube_all"))
                    .texture("all", modLoc("block/" + spec.name));
        }
    }

    /**
     * Generates a cube model with tintindex=0 on all faces.
     */
    private void buildTintableCubeItem(String name, String texturePath) {
        ModelBuilder<?> builder = getBuilder(name)
                .parent(getExistingFile(mcLoc("block/block")))
                .texture("all", modLoc(texturePath));

        // one element, all faces tinted
        builder.element()
                .from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, faceBuilder) ->
                        faceBuilder.texture("#all").cullface(dir).tintindex(0))
                .end();
    }
}
