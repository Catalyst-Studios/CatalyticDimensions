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
                final int tinted = argb(spec.tintRgb);
                e.register((state, level, pos, tintIndex) ->
                                tintIndex == 0 ? tinted : -1, // <- NO TINT for other layers
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
                final int tinted = argb(spec.tintRgb);
                e.register((stack, tintIndex) ->
                                tintIndex == 0 ? tinted : -1, // <- NO TINT for other layers
                        entry.block().get().asItem()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders e) {
        e.register(ResourceLocation.fromNamespaceAndPath(CatalystDimensions.MODID, "ctm"), new CTMGeometryLoader());
    }
}
