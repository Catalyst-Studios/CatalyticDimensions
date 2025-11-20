package com.catalyst.catalystdimensions.block.spec;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Single source of truth for everything a block needs across registry and datagen.
 *
 * Supports:
 *  - Per-layer visuals with integer "height" ordering
 *  - Per-layer CTM flag
 *  - Per-layer tint colour + tintIndex
 *  - Optional global tintRgb for simple CTM blocks
 *  - NEW: per-layer item texture override and skip-on-item flag
 */
public final class BlockSpec {

    public final String name;
    public final Supplier<Block> factory;

    public final boolean generateBlockItem;
    @Nullable public final Consumer<Item.Properties> itemProps;

    public final Set<TagKey<Block>> blockTags;
    public final Set<TagKey<Item>>  itemTags;

    @Nullable public final BiConsumer<Block, Object> blockstates;
    @Nullable public final Consumer<Object>          loot;
    @Nullable public final Consumer<Object>          itemModel;

    @Nullable public final String      lang;
    @Nullable public final RenderType  renderType;
    @Nullable public final Integer     tintRgb;
    @Nullable public final Connected   connected;
    public final List<Layer>          layers;

    private BlockSpec(Builder b) {
        this.name           = b.name;
        this.factory        = b.factory;
        this.generateBlockItem = b.generateBlockItem;
        this.itemProps      = b.itemProps;
        this.blockTags      = Set.copyOf(b.blockTags);
        this.itemTags       = Set.copyOf(b.itemTags);
        this.blockstates    = b.blockstates;
        this.loot           = b.loot;
        this.itemModel      = b.itemModel;
        this.lang           = b.lang;
        this.renderType     = b.renderType;
        this.tintRgb        = b.tintRgb;
        this.connected      = b.connected;
        this.layers         = List.copyOf(b.layers);
    }

    public static Builder builder(String name, Supplier<Block> factory) {
        return new Builder(name, factory);
    }

    // ---------------------------------------------------------------------
    // Builder
    // ---------------------------------------------------------------------

    public static final class Builder {
        private final String name;
        private final Supplier<Block> factory;

        private boolean generateBlockItem = true;
        @Nullable private Consumer<Item.Properties> itemProps = null;

        private final Set<TagKey<Block>> blockTags = new HashSet<>();
        private final Set<TagKey<Item>>  itemTags  = new HashSet<>();

        @Nullable private BiConsumer<Block, Object> blockstates = null;
        @Nullable private Consumer<Object> loot       = null;
        @Nullable private Consumer<Object> itemModel  = null;

        @Nullable private String     lang       = null;
        @Nullable private RenderType renderType = null;
        @Nullable private Integer    tintRgb    = null;
        @Nullable private Connected  connected  = null;

        private final List<Layer> layers = new ArrayList<>();
        private int nextTintIndex = 0;

        private Builder(String name, Supplier<Block> factory) {
            this.name = name;
            this.factory = factory;
        }

        // basic

        public Builder noItem() {
            this.generateBlockItem = false;
            return this;
        }

        public Builder item(Consumer<Item.Properties> cfg) {
            this.itemProps = cfg;
            return this;
        }

        public Builder blockTag(TagKey<Block> tag) {
            this.blockTags.add(tag);
            return this;
        }

        public Builder itemTag(TagKey<Item> tag) {
            this.itemTags.add(tag);
            return this;
        }

        public Builder blockstates(BiConsumer<Block, Object> fn) {
            this.blockstates = fn;
            return this;
        }

        public Builder loot(Consumer<Object> fn) {
            this.loot = fn;
            return this;
        }

        public Builder itemModel(Consumer<Object> fn) {
            this.itemModel = fn;
            return this;
        }

        public Builder lang(String lang) {
            this.lang = lang;
            return this;
        }

        public Builder render(RenderType type) {
            this.renderType = type;
            return this;
        }

        /** Global/simple tint colour (legacy/CTM). */
        public Builder tint(int rgb) {
            this.tintRgb = rgb;
            return this;
        }

        // CTM

        public Builder connected(int tileSize, int tiles, @Nullable String textureBase, boolean cullInterior) {
            this.connected = new Connected(tileSize, tiles, textureBase, cullInterior);
            return this;
        }

        public Builder connected(int tileSize, int tiles, @Nullable String textureBase) {
            this.connected = new Connected(tileSize, tiles, textureBase, false);
            return this;
        }

        public Builder connected(int tileSize, int tiles) {
            this.connected = new Connected(tileSize, tiles, null, false);
            return this;
        }

        public Builder connectedCull(boolean cullInterior) {
            if (this.connected == null) {
                throw new IllegalStateException("connectedCull() called before connected()");
            }
            this.connected = new Connected(
                    this.connected.tileSize,
                    this.connected.tiles,
                    this.connected.textureBase,
                    cullInterior
            );
            return this;
        }

        public Builder noConnected() {
            this.connected = null;
            return this;
        }

        // layers

        public LayerBuilder layer(int height, String texture) {
            return new LayerBuilder(this, height, texture);
        }

        public BlockSpec build() {
            return new BlockSpec(this);
        }

        // -------------------------------------------------------------
        // LayerBuilder
        // -------------------------------------------------------------

        public static final class LayerBuilder {
            private final Builder parent;
            private final int height;
            private final String texture;

            @Nullable private String  name = null;
            private boolean           ctm  = false;
            @Nullable private Integer tintRgb = null;

            // NEW: item overrides
            @Nullable private String  itemTextureOverride = null;
            private boolean           showOnItem          = true;

            private LayerBuilder(Builder parent, int height, String texture) {
                this.parent = parent;
                this.height = height;
                this.texture = texture;
            }

            public LayerBuilder name(String name) {
                this.name = name;
                return this;
            }

            /** Mark this layer as CTM-backed. */
            public LayerBuilder ctm() {
                this.ctm = true;
                return this;
            }

            public LayerBuilder ctm(boolean ctm) {
                this.ctm = ctm;
                return this;
            }

            /** Per-layer tint colour. */
            public LayerBuilder tint(int rgb) {
                this.tintRgb = rgb;
                return this;
            }

            /**
             * NEW: override the texture used for the *item* model for this layer.
             * Useful to point items at a non-CTM 16x16 icon.
             */
            public LayerBuilder itemTexture(String path) {
                this.itemTextureOverride = path;
                return this;
            }

            /**
             * NEW: do not include this layer in the item model at all.
             */
            public LayerBuilder noItemLayer() {
                this.showOnItem = false;
                return this;
            }

            public Builder done() {
                String id = (this.name != null ? this.name : ("layer_" + this.height));
                int tintIndex = (this.tintRgb != null) ? parent.nextTintIndex++ : -1;

                parent.layers.add(new Layer(
                        this.height,
                        id,
                        this.texture,
                        this.ctm,
                        this.tintRgb,
                        tintIndex,
                        this.itemTextureOverride,
                        this.showOnItem
                ));
                return parent;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Layer + CTM descriptors
    // ---------------------------------------------------------------------

    public static final class Layer {
        public final int height;
        public final String name;
        public final String texture;
        public final boolean ctm;
        @Nullable public final Integer tintRgb;
        public final int tintIndex;

        // NEW: item model overrides
        @Nullable public final String itemTexture; // if null, use texture
        public final boolean showOnItem;           // false => skip in item model

        public Layer(int height,
                     String name,
                     String texture,
                     boolean ctm,
                     @Nullable Integer tintRgb,
                     int tintIndex,
                     @Nullable String itemTexture,
                     boolean showOnItem) {
            this.height = height;
            this.name = name;
            this.texture = texture;
            this.ctm = ctm;
            this.tintRgb = tintRgb;
            this.tintIndex = tintIndex;
            this.itemTexture = itemTexture;
            this.showOnItem = showOnItem;
        }
    }

    public static final class Connected {
        public final int tileSize;
        public final int tiles;
        @Nullable public final String textureBase;
        public final boolean cullInterior;

        public Connected(int tileSize, int tiles, @Nullable String textureBase, boolean cullInterior) {
            this.tileSize = tileSize;
            this.tiles = tiles;
            this.textureBase = textureBase;
            this.cullInterior = cullInterior;
        }
    }
}
