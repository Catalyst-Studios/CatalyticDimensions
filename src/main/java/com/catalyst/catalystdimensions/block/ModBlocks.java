package com.catalyst.catalystdimensions.block;


import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.spec.BlockSpec;
import com.catalyst.catalystdimensions.datagen.ModBlockLootTableProvider;
import com.catalyst.catalystdimensions.datagen.ModBlockStateProvider;
import com.catalyst.catalystdimensions.item.ModItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public final class ModBlocks {


    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CatalystDimensions.MODID);



    public static final List<Entry> ALL = new ArrayList<>();


    public record Entry(BlockSpec spec, DeferredBlock<Block> block, net.neoforged.neoforge.registries.DeferredHolder<Item, ? extends Item> item) {}


    public static void register(IEventBus bus){
        BLOCKS.register(bus);
    }

    //-----helpers------
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
    // generic Metal
    public static void generic_Metal(
            String name

    ) {
        register(BlockSpec.builder(name,
                        () -> new Block(BlockBehaviour.Properties.of()
                                .strength(5.0F, 6.0F)
                                .requiresCorrectToolForDrops()
                                .sound(SoundType.METAL)))
                .blockTag(BlockTags.MINEABLE_WITH_PICKAXE) // add NEEDS_*_TOOL if you want to gate it
                .blockTag(BlockTags.NEEDS_STONE_TOOL)
                .loot(p -> ((ModBlockLootTableProvider) p).modDropSelf(
                        ModBlocks.blockRef(name)
                ))

                // As above, blockstates/model/loot are inferred:
                //   - BlockStates: cubeAll("magic_block")
                .build());
    }
    // generic Metal
    public static void generic_log(
            String name

    ) {
        register(BlockSpec.builder(name,
                        () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                .strength(5.0F, 6.0F)
                                .sound(SoundType.WOOD)))
                .blockTag(BlockTags.MINEABLE_WITH_AXE) // add NEEDS_*_TOOL if you want to gate it
                .blockstates((blk, p) -> ((ModBlockStateProvider) p).modAxisLog(blk, name))
                .loot(p -> ((ModBlockLootTableProvider) p).modDropSelf(
                        ModBlocks.blockRef(name)
                ))

                // As above, blockstates/model/loot are inferred:
                //   - BlockStates: cubeAll("magic_block")
                .build());
    }
//tinted blocks
// tinted crystal block
public static void tintable_glass(String name, int rgb) {
    register(BlockSpec.builder(name,
                    () -> new Block(BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .strength(0.3F, 0.3F)
                            .sound(SoundType.GLASS)))
            .blockTag(BlockTags.MINEABLE_WITH_PICKAXE)
            .render(RenderType.translucent())
            .tint(rgb) // <— the key: push color into the spec
            .loot(p -> ((ModBlockLootTableProvider) p).modDropSelf(ModBlocks.blockRef(name)))
            .connected(16,47,"tintable_crystal_base")
            .connectedCull(true)

            .build());
}





    // ---- Define blocks here ----
    public static void bootstrap(){

        //tinted crystal blocks
        tintable_glass("white_crystal_block", 0xFFFFFF);
        tintable_glass("light_gray_crystal_block", 0xD4D4CC);
        tintable_glass("gray_crystal_block", 0x606B6F);
        tintable_glass("black_crystal_block", 0x27272D);
        tintable_glass("brown_crystal_block", 0xB17144);
        tintable_glass("red_crystal_block", 0xEE3E33);
        tintable_glass("orange_crystal_block", 0xFFAD27);
        tintable_glass("yellow_crystal_block", 0xFFFF52);
        tintable_glass("lime_crystal_block", 0xADFF2A);
        tintable_glass("green_crystal_block", 0x7FA71E);
        tintable_glass("cyan_crystal_block", 0x1ED3D3);
        tintable_glass("light_blue_crystal_block", 0x4EF2FF);
        tintable_glass("blue_crystal_block", 0x515CE6);
        tintable_glass("purple_crystal_block", 0xB944F8);
        tintable_glass("magenta_crystal_block", 0xFF69FF);
        tintable_glass("pink_crystal_block", 0xFFBCE6);
        //generic metals
        generic_Metal("cog_brass_block");
        generic_Metal("cog_steel_block");
        //generic logs
        generic_log("belt_rubber_block");


// Example: a simple storage block with default cubeAll, dropSelf, auto lang
        register(BlockSpec.builder("black_opal_block",
                        () -> new Block(BlockBehaviour.Properties.of()
                                .strength(2.0F, 2.0F)
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