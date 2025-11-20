package com.catalyst.catalystdimensions.client.model.ctm;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * CTM lookup that supports up to 47 tiles.
 *
 * It keeps the original shape classification (based on 4 cardinals + 4 diagonals),
 * but instead of returning a single index (0-23), it maps each shape to a small
 * range of tile indices and uses a position-based pseudo-random variant.
 *
 * Shapes:
 *   - 0..15 : base shapes from BASE16 (same as before, URDL mask)
 *   - 16..19: open-corner shapes (UR, RD, DL, LU) when diagonal is missing
 *
 * Tile layout (for tiles >= 47):
 *   - Shape 0  -> tiles  0.. 2  (3 variants)
 *   - Shape 1  -> tiles  3.. 5  (3 variants)
 *   - Shape 2  -> tiles  6.. 8  (3 variants)
 *   - Shape 3  -> tiles  9..11  (3 variants)
 *   - Shape 4  -> tiles 12..14  (3 variants)
 *   - Shape 5  -> tiles 15..17  (3 variants)
 *   - Shape 6  -> tiles 18..20  (3 variants)
 *   - Shape 7  -> tiles 21..22  (2 variants)
 *   - Shape 8  -> tiles 23..24  (2 variants)
 *   - Shape 9  -> tiles 25..26  (2 variants)
 *   - Shape 10 -> tiles 27..28  (2 variants)
 *   - Shape 11 -> tiles 29..30  (2 variants)
 *   - Shape 12 -> tiles 31..32  (2 variants)
 *   - Shape 13 -> tiles 33..34  (2 variants)
 *   - Shape 14 -> tiles 35..36  (2 variants)
 *   - Shape 15 -> tiles 37..38  (2 variants)
 *   - Shape 16 -> tiles 39..40  (2 variants)
 *   - Shape 17 -> tiles 41..42  (2 variants)
 *   - Shape 18 -> tiles 43..44  (2 variants)
 *   - Shape 19 -> tiles 45..46  (2 variants)
 *
 * Total = 47 tiles (0..46).
 */
public final class CTMLookup {

    // Same 16-shape core mapping you already had.
    private static final int[] BASE16 = {
            /*0000*/0, /*0001 L*/4, /*0010 D*/3, /*0011 DL*/12,
            /*0100 R*/2, /*0101 RL*/10, /*0110 RD*/11, /*0111 RDL*/15,
            /*1000 U*/1, /*1001 UL*/13, /*1010 UD*/9,  /*1011 UDL*/14,
            /*1100 UR*/8, /*1101 URL*/7, /*1110 URD*/6, /*1111 URDL*/5
    };

    // We treat open corners as 4 separate shapes: 16..19
    private static final int SHAPE_OPEN_UR = 16;
    private static final int SHAPE_OPEN_RD = 17;
    private static final int SHAPE_OPEN_DL = 18;
    private static final int SHAPE_OPEN_LU = 19;

    // For 20 shapes (0..19), define how many tile variants each shape gets.
    // Sum = 47 so we can fill a 0..46 sheet.
    // Shapes 0..6 get 3 variants (more common shapes), rest get 2.
    private static final int[] VARIANT_COUNTS = {
            3,3,3,3,3,3,3,  // 0..6
            2,2,2,2,2,2,2,2,2,2,2,2,2  // 7..19
    };

    // Base tile index for each shape in the 47-tile sheet.
    private static final int[] BASE47 = new int[VARIANT_COUNTS.length];
    static {
        int acc = 0;
        for (int i = 0; i < VARIANT_COUNTS.length; i++) {
            BASE47[i] = acc;
            acc += VARIANT_COUNTS[i];
        }
        // acc should be 47 if VARIANT_COUNTS are correct
    }

    private CTMLookup() {}

    /**
     * Compute a tile index [0, maxTiles) for this face, given neighbour connectivity.
     *
     * @param u  up neighbour (same block)
     * @param r  right neighbour
     * @param d  down neighbour
     * @param l  left neighbour
     * @param ur diagonal up-right
     * @param rd diagonal right-down
     * @param dl diagonal down-left
     * @param lu diagonal left-up
     * @param maxTiles number of tiles available in the CTM sheet (e.g. 47)
     * @param pos world position (used for stable variant selection), may be null
     */
    public static int pick(boolean u, boolean r, boolean d, boolean l,
                           boolean ur, boolean rd, boolean dl, boolean lu,
                           int maxTiles,
                           @Nullable BlockPos pos) {

        if (maxTiles <= 0) {
            return 0;
        }

        // 1) Base shape from 4-cardinal mask (same as old code)
        int mask = (u ? 8 : 0) | (r ? 4 : 0) | (d ? 2 : 0) | (l ? 1 : 0);
        int baseShape = BASE16[mask]; // 0..15

        int shape = baseShape;

        // 2) Open-corner override (same logic as before, now as 16..19)
        if (u && r && !ur) {
            shape = SHAPE_OPEN_UR;
        } else if (r && d && !rd) {
            shape = SHAPE_OPEN_RD;
        } else if (d && l && !dl) {
            shape = SHAPE_OPEN_DL;
        } else if (l && u && !lu) {
            shape = SHAPE_OPEN_LU;
        }

        if (shape < 0 || shape >= VARIANT_COUNTS.length) {
            shape = 0;
        }

        int base = BASE47[shape];
        int variants = VARIANT_COUNTS[shape];

        // If the CTM sheet has fewer tiles than our full 47, clamp gracefully.
        // Effective variant count is at most the remaining tiles after 'base'.
        int maxUsable = Math.max(1, Math.min(variants, maxTiles - base));

        if (maxUsable <= 1 || maxTiles == 1) {
            // Single tile for this shape
            int idx = base;
            if (idx >= maxTiles) {
                idx = idx % maxTiles;
            }
            return idx;
        }

        // 3) Stable pseudo-random variant based on position
        int seed = 0;
        if (pos != null) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            // Simple integer hash; fast and deterministic
            seed = x * 73428767 ^ y * 912931 ^ z * 438289;
            seed ^= (seed >>> 16);
        }

        int variant = Math.floorMod(seed, maxUsable);
        int idx = base + variant;

        if (idx < 0) {
            idx = 0;
        } else if (idx >= maxTiles) {
            idx = maxTiles - 1;
        }

        return idx;
    }
}
