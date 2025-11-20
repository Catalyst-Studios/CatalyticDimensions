package com.catalyst.catalystdimensions.client.model.ctm;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CTMUnbakedGeometry implements IUnbakedGeometry<CTMUnbakedGeometry> {
    private final int tileSize;
    private final int tiles;
    private final boolean tinted;
    private final boolean cullInterior;

    // NEW: overlay CTM layers (each has its own material key + tintIndex)
    public static final class Overlay {
        public final String textureKey;
        public final int tintIndex;

        public Overlay(String textureKey, int tintIndex) {
            this.textureKey = textureKey;
            this.tintIndex = tintIndex;
        }
    }

    private final List<Overlay> overlays;

    public CTMUnbakedGeometry(int tileSize,
                              int tiles,
                              boolean tinted,
                              boolean cullInterior,
                              List<Overlay> overlays) {
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.tinted = tinted;
        this.cullInterior = cullInterior;
        this.overlays = overlays != null ? List.copyOf(overlays) : List.of();
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {

        // Base CTM sprite (same as before)
        TextureAtlasSprite baseSprite = spriteGetter.apply(ctx.getMaterial("ctm"));
        int baseTintIndex = tinted ? 0 : -1;

        // NEW: overlay CTM sprites + tint indices
        List<TextureAtlasSprite> overlaySprites = new ArrayList<>();
        int[] overlayTintIndices = new int[overlays.size()];
        for (int i = 0; i < overlays.size(); i++) {
            Overlay o = overlays.get(i);
            TextureAtlasSprite sprite = spriteGetter.apply(ctx.getMaterial(o.textureKey));
            overlaySprites.add(sprite);
            overlayTintIndices[i] = o.tintIndex;
        }

        return new CTMBakedModel(
                baseSprite,
                tileSize,
                tiles,
                baseTintIndex,
                cullInterior,
                overlaySprites,
                overlayTintIndices
        );
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext ctx) {
        // no parents to resolve
    }
}
