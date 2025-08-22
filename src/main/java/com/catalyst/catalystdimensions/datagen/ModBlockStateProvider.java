package com.catalyst.catalystdimensions.datagen;


import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.data.PackOutput;


public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper efh) {
        super(output, CatalystDimensions.MODID, efh);


    }



    // --- “log-like” (side + top) ---
    public void modAxisLog(Block b, String name) {
        if (!(b instanceof RotatedPillarBlock pillar))
            throw new IllegalArgumentException("modAxisLog requires RotatedPillarBlock: " + b);

        ResourceLocation side = modLoc("block/" + name);         // bark
        ResourceLocation top  = modLoc("block/" + name + "_top"); // end grain
        axisBlock(pillar, side, top);

        // Item model: parent -> block/<name>
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }

    // --- “wood-like” (bark on all faces) ---
    public void modAxisWood(Block b, String name) {
        if (!(b instanceof RotatedPillarBlock pillar))
            throw new IllegalArgumentException("modAxisWood requires RotatedPillarBlock: " + b);

        ResourceLocation bark = modLoc("block/" + name);
        axisBlock(pillar, bark, bark);
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }

    // --- Simple cube_all helper (matches your default “cubeAll” case) ---
    public void modCubeAll(Block b, String name) {
        var model = models().cubeAll(name, modLoc("block/" + name));
        simpleBlock(b, model);
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }

    // Optional: direct pass-through if you already have explicit side/top textures
    public void modAxis(Block b, ResourceLocation side, ResourceLocation top) {
        if (!(b instanceof RotatedPillarBlock pillar))
            throw new IllegalArgumentException("modAxis requires RotatedPillarBlock: " + b);

        axisBlock(pillar, side, top);
        // Infer item model name from side path's filename:
        String name = side.getPath().substring(side.getPath().lastIndexOf('/') + 1);
        simpleBlockItem(b, models().getExistingFile(modLoc("block/" + name)));
    }
    // cube_all with tintindex=0 on every face (grayscale base texture)
    public void modTintedCubeAll(Block b, String modelName, ResourceLocation baseTexture) {
        var model = models().getBuilder(modelName)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("all", baseTexture)
                .element().from(0,0,0).to(16,16,16)
                .allFaces((dir, face) -> face
                        .uvs(0,0,16,16)
                        .texture("#all")
                        .tintindex(0)) // ← critical
                .end();

        simpleBlock(b, model);
        simpleBlockItem(b, model);
    }



    @Override
    protected void registerStatesAndModels() {
        for (var e : ModBlocks.ALL) {
            var block = e.block().get();
            var spec = e.spec();
            if (spec.blockstates != null) {
                spec.blockstates.accept(block, this);
            } else {
// Default: cubeAll
                simpleBlock(block, models().cubeAll(spec.name, blockTexture(block)));
            }
// Optional: item model default if none provided
            if (spec.itemModel != null) {
                spec.itemModel.accept(this);
            } else if (e.item() != null) {
                itemModels().withExistingParent(spec.name, modLoc("block/" + spec.name));
            }
        }
    }
}