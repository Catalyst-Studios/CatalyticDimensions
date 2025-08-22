package com.catalyst.catalystdimensions.client;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import com.catalyst.catalystdimensions.block.spec.BlockSpec;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CatalystDimensions.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ModClient {

    private static int argb(int rgb) { return 0xFF000000 | (rgb & 0xFFFFFF); }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            for (var entry : ModBlocks.ALL) {
                BlockSpec spec = entry.spec();
                if (spec.renderType != null) {
                    // Predicate style works across versions
                    ItemBlockRenderTypes.setRenderLayer(entry.block().get(), rt -> rt == spec.renderType);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block e) {
        for (var entry : ModBlocks.ALL) {
            var spec = entry.spec();
            if (spec.tintRgb != null) {
                final int rgb = spec.tintRgb;
                e.register((state, level, pos, tintIndex) ->
                                tintIndex == 0 ? argb(spec.tintRgb) : 0xFFFFFFFF,   // <-- force alpha
                        entry.block().get()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item e) {
        for (var entry : ModBlocks.ALL) {
            var spec = entry.spec();
            if (spec.tintRgb != null) {
                final int rgb = spec.tintRgb;
                e.register((stack, tintIndex) ->
                                tintIndex == 0 ? argb(spec.tintRgb) : 0xFFFFFFFF,   // <-- force alpha
                        entry.block().get().asItem()
                );
            }
        }
    }
}
