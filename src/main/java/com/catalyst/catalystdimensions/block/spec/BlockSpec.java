package com.catalyst.catalystdimensions.block.spec;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
* Single source of truth for everything a block needs across registry and datagen.
*/
public final class BlockSpec {
public final String name; // registry path, e.g. "black_opal_block"
public final Supplier<Block> factory; // () -> new Block(Properties...)


// Optional: BlockItem control
public final boolean generateBlockItem;
public final Consumer<Item.Properties> itemProps; // customize item props


// Datagen metadata
public final Set<TagKey<Block>> blockTags;
public final Set<TagKey<Item>> itemTags;


// Provider hooks (typed as Object to prevent compile deps here)
public final BiConsumer<Block, Object> blockstates; // (block, BlockStateProvider)
public final Consumer<Object> loot; // (BlockLootSubProvider)
public final Consumer<Object> itemModel; // (ItemModelProvider)


// Extras
public final String lang; // Display name; null => title-case of name
public final RenderType renderType; // null => default solid


private BlockSpec(Builder b) {
this.name = b.name;
this.factory = b.factory;
this.generateBlockItem = b.generateBlockItem;
this.itemProps = b.itemProps;
this.blockTags = Set.copyOf(b.blockTags);
this.itemTags = Set.copyOf(b.itemTags);
this.blockstates = b.blockstates;
this.loot = b.loot;
this.itemModel = b.itemModel;
this.lang = b.lang;
this.renderType = b.renderType;
}


public static Builder builder(String name, Supplier<Block> factory){
return new Builder(name, factory);
}


public static final class Builder {
private final String name;
private final Supplier<Block> factory;
private boolean generateBlockItem = true;
private Consumer<Item.Properties> itemProps = null;
private final Set<TagKey<Block>> blockTags = new HashSet<>();
private final Set<TagKey<Item>> itemTags = new HashSet<>();
private BiConsumer<Block, Object> blockstates = null;
private Consumer<Object> loot = null;
private Consumer<Object> itemModel = null;
private String lang = null;
private RenderType renderType = null;


private Builder(String name, Supplier<Block> factory){
this.name = name; this.factory = factory;
}
public Builder noItem(){ this.generateBlockItem = false; return this; }
public Builder item(Consumer<Item.Properties> cfg){ this.itemProps = cfg; return this; }
public Builder blockTag(TagKey<Block> tag){ this.blockTags.add(tag); return this; }
public Builder itemTag(TagKey<Item> tag){ this.itemTags.add(tag); return this; }
public Builder blockstates(BiConsumer<Block, Object> fn){ this.blockstates = fn; return this; }
public Builder loot(Consumer<Object> fn){ this.loot = fn; return this; }
public Builder itemModel(Consumer<Object> fn){ this.itemModel = fn; return this; }
public Builder lang(String l){ this.lang = l; return this; }
public Builder render(RenderType r){ this.renderType = r; return this; }
public BlockSpec build(){ return new BlockSpec(this); }
}
}