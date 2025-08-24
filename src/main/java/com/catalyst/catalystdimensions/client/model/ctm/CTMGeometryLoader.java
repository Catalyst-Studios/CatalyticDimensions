package com.catalyst.catalystdimensions.client.model.ctm;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class CTMGeometryLoader implements IGeometryLoader<CTMUnbakedGeometry> {

    @Override
    public CTMUnbakedGeometry read(JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        int tileSize = json.has("tile_size") ? json.get("tile_size").getAsInt() : 16;
        int tiles    = json.has("tiles")     ? json.get("tiles").getAsInt()     : 47;
        boolean tinted = json.has("tinted") && json.get("tinted").getAsBoolean();

        // NEW: allow cull_interior flag in JSON (defaults to false)
        boolean cullInterior = json.has("cull_interior") && json.get("cull_interior").getAsBoolean();

        return new CTMUnbakedGeometry(tileSize, tiles, tinted, cullInterior);
    }
}
