package com.catalyst.catalystdimensions.client;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import com.catalyst.catalystdimensions.block.spec.BlockSpec;
import com.catalyst.catalystdimensions.client.model.ctm.CTMGeometryLoader;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side hooks: render layers, colour handlers, geometry loaders.
 */
@EventBusSubscriber(
        modid = CatalystDimensions.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ModClient {

    private ModClient() {}

    private static int argb(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    // ---------------------------------------------------------------------
    // Client setup: render layers
    // ---------------------------------------------------------------------

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            for (var entry : ModBlocks.ALL) {
                BlockSpec spec = entry.spec();
                if (spec.renderType != null) {
                    ItemBlockRenderTypes.setRenderLayer(entry.block().get(), rt -> rt == spec.renderType);
                }
            }
        });
    }

    // ---------------------------------------------------------------------
    // Helper: tint presence + layer resolution
    // ---------------------------------------------------------------------

    private static boolean hasAnyTint(BlockSpec spec) {
        if (spec.tintRgb != null) {
            return true;
        }
        if (spec.layers != null) {
            for (BlockSpec.Layer layer : spec.layers) {
                if (layer.tintRgb != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int resolveLayerTint(BlockSpec spec, int tintIndex) {
        if (spec.layers == null) {
            return -1;
        }
        for (BlockSpec.Layer layer : spec.layers) {
            if (layer.tintIndex == tintIndex && layer.tintRgb != null) {
                return argb(layer.tintRgb);
            }
        }
        return -1;
    }

    // ---------------------------------------------------------------------
    // Colour handlers (blocks)
    // ---------------------------------------------------------------------

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block e) {
        for (var entry : ModBlocks.ALL) {
            BlockSpec spec = entry.spec();
            if (!hasAnyTint(spec)) {
                continue;
            }

            e.register((state, level, pos, tintIndex) -> {
                        // 1) Per-layer tint takes priority
                        int fromLayer = resolveLayerTint(spec, tintIndex);
                        if (fromLayer != -1) {
                            return fromLayer;
                        }

                        // 2) Global tint (legacy/simple) – only index 0
                        if ((spec.layers == null || spec.layers.isEmpty())
                                && spec.tintRgb != null
                                && tintIndex == 0) {
                            return argb(spec.tintRgb);
                        }

                        // 3) No tint for this index
                        return -1;
                    },
                    entry.block().get()
            );
        }
    }

    // ---------------------------------------------------------------------
    // Colour handlers (items)
    // ---------------------------------------------------------------------

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item e) {
        for (var entry : ModBlocks.ALL) {
            BlockSpec spec = entry.spec();
            if (!hasAnyTint(spec) || entry.item() == null) {
                continue;
            }

            e.register((stack, tintIndex) -> {
                        // 1) Per-layer tint
                        int fromLayer = resolveLayerTint(spec, tintIndex);
                        if (fromLayer != -1) {
                            return fromLayer;
                        }

                        // 2) Global tint (only index 0)
                        if ((spec.layers == null || spec.layers.isEmpty())
                                && spec.tintRgb != null
                                && tintIndex == 0) {
                            return argb(spec.tintRgb);
                        }

                        return -1;
                    },
                    entry.block().get().asItem()
            );
        }
    }

    // ---------------------------------------------------------------------
    // Geometry loaders (CTM)
    // ---------------------------------------------------------------------

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders e) {
        e.register(
                ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, "ctm"),
                new CTMGeometryLoader()
        );
    }
}
