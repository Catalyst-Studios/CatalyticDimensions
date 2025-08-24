package com.catalyst.catalystdimensions.client.model.ctm;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class CTMUnbakedGeometry implements IUnbakedGeometry<CTMUnbakedGeometry> {
    private final int tileSize;
    private final int tiles;
    private final boolean tinted;
    private final boolean cullInterior; // NEW

    public CTMUnbakedGeometry(int tileSize, int tiles, boolean tinted, boolean cullInterior) {
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.tinted = tinted;
        this.cullInterior = cullInterior;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx,
                           ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState,
                           ItemOverrides overrides) {
        TextureAtlasSprite sprite = spriteGetter.apply(ctx.getMaterial("ctm"));
        int tintIndex = tinted ? 0 : -1;


        return new CTMBakedModel(sprite, tileSize, tiles, tintIndex, cullInterior);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver, IGeometryBakingContext ctx) {
        // no parents to resolve
    }
}
