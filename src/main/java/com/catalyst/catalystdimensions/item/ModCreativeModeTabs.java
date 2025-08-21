package com.catalyst.catalystdimensions.item;

import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeModeTabs {
    private ModCreativeModeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CatalystDimensions.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CATALYST_TAB =
            TABS.register("catalystdimensions", () -> CreativeModeTab.builder()
                    // Name shown at the top of the tab (add a lang entry for this key)
                    .title(Component.translatable("catalystdimensions"))
                    // Tab icon – use any block/item you like
                    .icon(() -> new ItemStack(ModBlocks.blockRef("black_opal_block")))
                    // Populate entries. Only add if the spec generated a BlockItem.
                    .displayItems((params, out) -> {
                        for (var e : ModBlocks.ALL) {
                            if (e.item() != null) {
                                out.accept(e.item().get()); // shows the BlockItem
                            }
                        }
                    })
                    .build()
            );

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
