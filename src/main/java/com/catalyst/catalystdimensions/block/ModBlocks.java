package com.catalyst.catalystdimensions.block;


import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.spec.BlockSpec;
import com.catalyst.catalystdimensions.datagen.ModBlockLootTableProvider;
import com.catalyst.catalystdimensions.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CatalystDimensions.MODID);


// Keep in Items to create BlockItems here (so spec stays central)
// ModItems.ITEMS exists in your project already.


    public static final List<Entry> ALL = new ArrayList<>();


    public record Entry(BlockSpec spec, DeferredBlock<Block> block, net.neoforged.neoforge.registries.DeferredHolder<Item, ? extends Item> item) {}


    public static void register(IEventBus bus){
        BLOCKS.register(bus);
    }


    // Helper to register a spec and wire BlockItem automatically
    public static Entry register(BlockSpec spec){
        DeferredBlock<Block> blockRO = BLOCKS.register(spec.name, spec.factory);
        var itemRO = spec.generateBlockItem
                ? ModItems.ITEMS.register(spec.name, () -> {
            Item.Properties props = new Item.Properties();
            if (spec.itemProps != null) spec.itemProps.accept(props);
            return new BlockItem(blockRO.get(), props);
        })
                : null;
        Entry e = new Entry(spec, blockRO, itemRO);
        ALL.add(e);
        return e;
    }


    // ---- Define blocks here ----
    public static void bootstrap(){
// Example: a simple storage block with default cubeAll, dropSelf, auto lang
        register(BlockSpec.builder("black_opal_block",
                        () -> new Block(BlockBehaviour.Properties.of()
                                .strength(5.0F, 6.0F)
                                .requiresCorrectToolForDrops()
                                .sound(SoundType.STONE)))


                .build());

// Example new block: “magic_block”
        register(BlockSpec.builder("magic_block",
                        () -> new Block(BlockBehaviour.Properties.of()
                                .strength(3.0F, 6.0F)
                                .requiresCorrectToolForDrops()
                                .sound(SoundType.AMETHYST)))
                .blockTag(BlockTags.MINEABLE_WITH_PICKAXE) // add NEEDS_*_TOOL if you want to gate it
                .loot(p -> ((ModBlockLootTableProvider) p).modSilkOrItem(
                        ModBlocks.blockRef("magic_block"),
                        Blocks.DIORITE
                ))
                // If the texture has cutouts or glass-like pixels, also set render type:
                // .renderType(RenderType.cutout())
                // As above, blockstates/model/loot are inferred:
                //   - BlockStates: cubeAll("magic_block")
                //   - Item model: item/magic_block -> parent block/magic_block
                //   - Loot: dropSelf(magic_block)
                .build());



    }
    public static net.minecraft.world.level.block.Block blockRef(String name) {
        for (var e : ALL) {
            if (e.spec().name.equals(name)) return e.block().get();
        }
        throw new IllegalArgumentException("Unknown block: " + name);
    }
}