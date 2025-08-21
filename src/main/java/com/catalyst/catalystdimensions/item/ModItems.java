package com.catalyst.catalystdimensions.item;


import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    // NeoForge 1.21.1 item register (used by both standalone items and BlockItems from ModBlocks)
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CatalystDimensions.MODID);




    public static void register(IEventBus bus) { ITEMS.register(bus); }
}