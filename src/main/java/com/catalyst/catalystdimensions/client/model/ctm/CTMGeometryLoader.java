package com.catalyst.catalystdimensions.client.model.ctm;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import java.util.ArrayList;
import java.util.List;

public class CTMGeometryLoader implements IGeometryLoader<CTMUnbakedGeometry> {

    @Override
    public CTMUnbakedGeometry read(JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        int tileSize = json.has("tile_size") ? json.get("tile_size").getAsInt() : 16;
        int tiles    = json.has("tiles")     ? json.get("tiles").getAsInt()     : 47;
        boolean tinted = json.has("tinted") && json.get("tinted").getAsBoolean();

        // cull_interior flag (existing)
        boolean cullInterior = json.has("cull_interior") && json.get("cull_interior").getAsBoolean();

        // NEW: per-layer CTM overlays
        // JSON shape:
        // "overlay_layers": [
        //   { "texture": "overlay_0", "tintindex": 0 },
        //   { "texture": "overlay_1", "tintindex": 1 }
        // ]
        List<CTMUnbakedGeometry.Overlay> overlays = new ArrayList<>();
        if (json.has("overlay_layers")) {
            JsonArray arr = json.getAsJsonArray("overlay_layers");
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                String texKey = o.has("texture") ? o.get("texture").getAsString() : null;
                if (texKey == null || texKey.isEmpty()) continue;
                int tintIndex = o.has("tintindex") ? o.get("tintindex").getAsInt() : -1;
                overlays.add(new CTMUnbakedGeometry.Overlay(texKey, tintIndex));
            }
        }

        return new CTMUnbakedGeometry(tileSize, tiles, tinted, cullInterior, overlays);
    }
}
