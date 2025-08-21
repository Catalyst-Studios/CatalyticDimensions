package com.catalyst.catalystdimensions.datagen;


import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.minecraft.data.PackOutput;


import java.util.concurrent.CompletableFuture;


public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup) {
        super(out, lookup, CatalystDimensions.MODID, null);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var e : ModBlocks.ALL) {
            for (TagKey<Block> tag : e.spec().blockTags) {
                this.tag(tag).add(e.block().get());
            }
        }
    }
}