package com.catalyst.catalystdimensions.datagen;


import com.catalyst.catalystdimensions.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.data.PackOutput;


import java.util.concurrent.CompletableFuture;


public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup, ModBlockTagProvider blockTags) {
        super(out, lookup, blockTags.contentsGetter(), "catalystdimensions", null);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var e : ModBlocks.ALL) {
            if (e.item() == null) continue;
            for (TagKey<net.minecraft.world.item.Item> tag : e.spec().itemTags) {
                this.tag(tag).add(e.item().get());
            }
        }
    }
}