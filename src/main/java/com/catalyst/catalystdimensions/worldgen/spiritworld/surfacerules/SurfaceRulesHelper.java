package com.catalyst.catalystdimensions.worldgen.spiritworld.surfacerules;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class SurfaceRulesHelper {

    /**
     * Creates a surface rule where the topmost layer is replaced with topMaterial,
     * and the next N layers beneath it (defined by surfaceDepth) are replaced with bottomMaterial.
     *
     * @param offset             Vertical offset from the terrain surface.
     * @param addSurfaceDepth    Whether to include the biome's surface depth noise.
     * @param surfaceDepth       How many blocks deep the rule applies.
     * @param topMaterial        The block for the top layer (e.g. grass).
     * @param bottomMaterial     The block for layers underneath (e.g. dirt).
     * @return                   A SurfaceRules.RuleSource configured with this layered surface rule.
     */
    public static RuleSource surface(int offset, boolean addSurfaceDepth, int surfaceDepth, Block topMaterial, Block bottomMaterial) {
        return SurfaceRules.ifTrue(
            SurfaceRules.stoneDepthCheck(offset, addSurfaceDepth, surfaceDepth, CaveSurface.FLOOR),
            SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.state(topMaterial.defaultBlockState())
                ),
                SurfaceRules.ifTrue(
                    SurfaceRules.UNDER_FLOOR,
                    SurfaceRules.state(bottomMaterial.defaultBlockState())
                )
            )
        );
    }
}
