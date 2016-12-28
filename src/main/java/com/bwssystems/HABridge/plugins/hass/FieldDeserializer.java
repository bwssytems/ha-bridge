package com.bwssystems.HABridge.plugins.hass;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FieldDeserializer implements JsonDeserializer<Field> {
	private Map<String, JsonElement> fields;
    @Override
    public Field deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
    {
        JsonObject obj = json.getAsJsonObject();
        String theKey;

        fields = new HashMap<String, JsonElement>();
        for(Entry<String, JsonElement> entry:obj.entrySet()){
        	theKey = entry.getKey();
            JsonElement theRawDetail = obj.get(theKey);
            fields.put(theKey, theRawDetail);
        }
        
        return new Field(fields);
    }

}
