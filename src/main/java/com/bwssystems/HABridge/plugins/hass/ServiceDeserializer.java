package com.bwssystems.HABridge.plugins.hass;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ServiceDeserializer implements JsonDeserializer<Service> {
	private Map<String, ServiceElement> services;
	private String domain;
    @Override
    public Service deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
    {
        JsonObject objServices = json.getAsJsonObject();
        String theKey;
        domain = objServices.get("domain").getAsString();
        JsonObject obj = objServices.get("services").getAsJsonObject();
        services = new HashMap<String, ServiceElement>();
        for(Entry<String, JsonElement> entry:obj.entrySet()){
        	ServiceElement theServiceElement = new ServiceElement();
        	theKey = entry.getKey();
	            JsonObject theRawDetail = obj.getAsJsonObject(theKey);
	            
	            theServiceElement.setDescription(theRawDetail.get("description").getAsString());
	            theServiceElement.setField(ctx.deserialize(theRawDetail.get("fields"), Field.class));
	            services.put(theKey, theServiceElement);
        } 
        return new Service(domain, services);
    }

}
