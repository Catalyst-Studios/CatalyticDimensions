
package com.catalyst.catalystdimensions.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    public ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }
    public void modDropSelf(Block b) { super.dropSelf(b); }

    public void modNoDrop(Block b) { super.noDrop(); }

    public void modDropOther(Block b, ItemLike item) { super.dropOther(b, item); }

    // “Ore-style” drop: silk-touch = block, otherwise drop item with proper handling
    public void modSilkOrItem(Block b, ItemLike item) {
        this.add(b, createSilkTouchDispatchTable(b, LootItem.lootTableItem(item)));
    }


    @Override
    protected void generate() {
        for (var e : com.catalyst.catalystdimensions.block.ModBlocks.ALL) {
            var block = e.block().get();
            var spec  = e.spec();

            // If the spec supplies a loot lambda, let it add the table.
            // (BlockSpec.loot is a Consumer<Object>, so pass `this`.)
            if (spec.loot != null) {
                spec.loot.accept(this);
            } else {
                // Default: block drops itself
                this.dropSelf(block);
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return com.catalyst.catalystdimensions.block.ModBlocks.ALL.stream()
                .map(e -> e.block().get())::iterator;
    }
}
