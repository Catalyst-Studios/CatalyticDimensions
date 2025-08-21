package com.catalyst.catalystdimensions.datagen;


import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.data.PackOutput;


public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper efh) {
        super(output, CatalystDimensions.MODID, efh);
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