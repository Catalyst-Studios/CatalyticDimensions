package com.catalyst.catalystdimensions.client.model.ctm;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.joml.Vector3f;

public final class CTMBakedModel implements BakedModel {

    private static final String MODID = "catalystdimensions";

    // ModelData keys (unchanged)
    public static final ModelProperty<BlockAndTintGetter> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos>           POS   = new ModelProperty<>();
    public static final ModelProperty<BlockState>         SELF  = new ModelProperty<>();

    private static final ModelState IDENTITY = new SimpleModelState(Transformation.identity());
    private static final float UV_EPS = 0.0005f;

    private final TextureAtlasSprite sprite;
    private final int tileSize;
    private final int tiles;
    private final int tilesPerRow;
    private final int tintIndex;

    // NEW: per-block toggle to cull faces touching the same block
    private final boolean cullInterior;

    public CTMBakedModel(TextureAtlasSprite sprite,
                         int tileSize,
                         int tiles,
                         int tintIndex,
                         boolean cullInterior) { // <-- NEW param
        this.sprite = sprite;
        this.tileSize = tileSize;
        this.tiles = tiles;
        this.tintIndex = tintIndex;
        this.cullInterior = cullInterior; // <-- assign once (final field)
        int sw = Math.max(1, sprite.contents().width());
        this.tilesPerRow = Math.max(1, sw / Math.max(1, tileSize));
    }

    // Vanilla signature
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    // NeoForge signature
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state,
                                    @Nullable Direction side,
                                    RandomSource rand,
                                    ModelData data,
                                    @Nullable RenderType layer) {
        if (side == null) return List.of();

        BlockAndTintGetter level = data.get(LEVEL);
        BlockPos pos             = data.get(POS);
        BlockState self          = data.get(SELF);

        // --- NEW: interior-face culling, guarded by toggle ---
        if (cullInterior && level != null && pos != null && self != null) {
            if (connects(level, pos, self, side)) {
                return List.of(); // touching same block on this face -> skip the quad entirely
            }
        }

        // CTM neighbor sampling (unchanged)
        int idx = 0;
        if (level != null && pos != null && self != null) {
            boolean u  = connects(level, pos, self, up(side));
            boolean r  = connects(level, pos, self, right(side));
            boolean d  = connects(level, pos, self, down(side));
            boolean l  = connects(level, pos, self, left(side));

            boolean ur = u && r && connects(level, pos.relative(up(side)).relative(right(side)), self);
            boolean rd = r && d && connects(level, pos.relative(right(side)).relative(down(side)), self);
            boolean dl = d && l && connects(level, pos.relative(down(side)).relative(left(side)), self);
            boolean lu = l && u && connects(level, pos.relative(left(side)).relative(up(side)), self);

            idx = CTMLookup.pick(u, r, d, l, ur, rd, dl, lu);
            if (idx < 0 || idx >= tiles) idx = 0;
        }

        return List.of(bakeFace(side, idx));
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData existing) {
        return ModelData.builder()
                .with(LEVEL, level)
                .with(POS, pos.immutable())
                .with(SELF, state)
                .build();
    }

    // BakedModel boilerplate (unchanged)
    @Override public TextureAtlasSprite getParticleIcon() { return sprite; }
    @Override public boolean useAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    // --- quad baking via FaceBakery (unchanged) ---
    private BakedQuad bakeFace(Direction face, int tileIndex) {
        int sheetW = sprite.contents().width();
        int sheetH = sprite.contents().height();

        int col = tileIndex % tilesPerRow;
        int row = tileIndex / tilesPerRow;

        // FaceBakery wants UVs in 0..16 relative to the SPRITE
        float u0 = 16f * (col * tileSize)        / (float) sheetW;
        float v0 = 16f * (row * tileSize)        / (float) sheetH;
        float u1 = 16f * ((col + 1) * tileSize)  / (float) sheetW;
        float v1 = 16f * ((row + 1) * tileSize)  / (float) sheetH;

        u0 += UV_EPS; v0 += UV_EPS; u1 -= UV_EPS; v1 -= UV_EPS;

        Vector3f from = new Vector3f(0, 0, 0);
        Vector3f to   = new Vector3f(16, 16, 16);

        BlockFaceUV uv = new BlockFaceUV(new float[]{u0, v0, u1, v1}, 0);
        BlockElementFace elemFace = new BlockElementFace(face, tintIndex, "", uv);

        return new FaceBakery().bakeQuad(
                from,
                to,
                elemFace,
                sprite,
                face,
                IDENTITY,
                (BlockElementRotation) null,
                true
        );
    }

    // neighbor helpers (unchanged)
    private static boolean connects(BlockAndTintGetter lvl, BlockPos p, BlockState self, Direction dir) {
        return lvl.getBlockState(p.relative(dir)).getBlock() == self.getBlock();
    }
    private static boolean connects(BlockAndTintGetter lvl, BlockPos p, BlockState self) {
        return lvl.getBlockState(p).getBlock() == self.getBlock();
    }

    private static Direction up(Direction f)   { return switch (f) {
        case UP -> Direction.NORTH; case DOWN -> Direction.SOUTH;
        case NORTH, SOUTH -> Direction.UP; case EAST, WEST -> Direction.UP;
    }; }
    private static Direction down(Direction f) { return up(f).getOpposite(); }
    private static Direction right(Direction f){ return switch (f) {
        case NORTH -> Direction.EAST; case SOUTH -> Direction.WEST;
        case EAST -> Direction.SOUTH; case WEST -> Direction.NORTH;
        case UP, DOWN -> Direction.EAST;
    }; }
    private static Direction left(Direction f) { return right(f).getOpposite(); }

    private static final class SimpleModelState implements ModelState {
        private final Transformation t;
        SimpleModelState(Transformation t) { this.t = t; }
        @Override public Transformation getRotation() { return t; }
        @Override public boolean isUvLocked() { return false; }
    }
}
