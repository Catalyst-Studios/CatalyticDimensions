package com.catalyst.catalystdimensions.datagen;


import com.catalyst.catalystdimensions.CatalystDimensions;
import com.catalyst.catalystdimensions.block.ModBlocks;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.minecraft.data.PackOutput;


public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput out) {
        super(out, CatalystDimensions.MODID, "en_us");
    }


    @Override
    protected void addTranslations() {
        for (var e : ModBlocks.ALL) {
            var block = e.block().get();
            var spec  = e.spec();

            String key  = block.getDescriptionId();
            String name = spec.lang != null ? spec.lang : toTitle(spec.name);

            add(block, name);

            if (e.item() != null) {
                var item = e.item().get();
                String itemKey = item.getDescriptionId();
                if (!itemKey.equals(key)) {
                    add(item, name);
                }
            }
        }
    }




    private static String toTitle(String s){
        String[] parts = s.split("_");
        StringBuilder b = new StringBuilder();
        for (String p : parts){
            if (p.isEmpty()) continue;
            b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return b.toString().trim();
    }
}