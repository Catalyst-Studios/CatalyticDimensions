package com.catalyst.catalystdimensions.datagen;

import com.catalyst.catalystdimensions.CatalystDimensions;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput out) {
        super(out, CatalystDimensions.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Creative tab title
        add("itemGroup." + CatalystDimensions.MODID, "Catalyst Dimensions");


    }
}
