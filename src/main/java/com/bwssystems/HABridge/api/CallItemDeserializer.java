package com.bwssystems.HABridge.api;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CallItemDeserializer implements JsonDeserializer<CallItem> {
    @Override
    public CallItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
    {
    	CallItem aCallItem = new CallItem();
        JsonObject jsonObj = json.getAsJsonObject();
        JsonElement jsonElem;
        jsonElem = jsonObj.get("item");
        aCallItem.setItem(jsonElem.getAsString());
        jsonElem = jsonObj.get("delay");
        aCallItem.setDelay(jsonElem.getAsInt());
        jsonElem = jsonObj.get("count");
        aCallItem.setCount(jsonElem.getAsInt());
        return aCallItem;
    }

}