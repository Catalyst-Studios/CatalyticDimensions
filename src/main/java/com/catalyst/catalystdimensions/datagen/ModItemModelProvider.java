package com.catalyst.catalystdimensions.datagen;


import com.catalyst.catalystdimensions.block.ModBlocks;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.data.PackOutput;


public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput out, ExistingFileHelper efh) { super(out, "catalystdimensions", efh); }


    @Override
    protected void registerModels() {
        for (var e : ModBlocks.ALL) {
            if (e.item() == null) continue;
// Usually block items point to block model; BlockStateProvider already ensured default
            withExistingParent(e.spec().name, modLoc("block/" + e.spec().name));
        }
    }
}