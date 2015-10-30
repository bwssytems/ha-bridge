package com.bwssystems.HABridge.devicemanagmeent;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.delete;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.JsonTransformer;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.dao.DeviceRepository;
import com.bwssystems.harmony.HarmonyHandler;
import com.bwssystems.luupRequests.Sdata;
import com.bwssystems.vera.VeraInfo;
import com.google.gson.Gson;

/**
	spark core server for bridge configuration
 */
public class DeviceResource {
    private static final String API_CONTEXT = "/api/devices";
    private static final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    private DeviceRepository deviceRepository;
    private VeraInfo veraInfo;
    private HarmonyHandler myHarmonyHandler;
    private static final Set<String> supportedVerbs = new HashSet<>(Arrays.asList("get", "put", "post"));

	public DeviceResource(BridgeSettings theSettings, HarmonyHandler myHarmony) {
		super();
		deviceRepository = new DeviceRepository(theSettings.getUpnpDeviceDb());
		veraInfo = new VeraInfo(theSettings.getVeraAddress(), theSettings.isValidVera());
		myHarmonyHandler = myHarmony;
        setupEndpoints();
	}

	public DeviceRepository getDeviceRepository() {
		return deviceRepository;
	}

    private void setupEndpoints() {
    	log.info("HABridge device management service started.... ");
    	post(API_CONTEXT, "application/json", (request, response) -> {
	    	log.debug("Create a Device - request body: " + request.body());
    		DeviceDescriptor device = new Gson().fromJson(request.body(), DeviceDescriptor.class);
	    	if(device.getContentBody() != null ) {
	            if (device.getContentType() == null || device.getHttpVerb() == null || !supportedVerbs.contains(device.getHttpVerb().toLowerCase())) {
	            	device = null;
	            	response.status(HttpStatus.SC_BAD_REQUEST);
					log.debug("Bad http verb in create a Device: " + request.body());
					return device;
	            }
	        }

	    	deviceRepository.save(device);
			log.debug("Created a Device: " + request.body());

			response.status(HttpStatus.SC_CREATED);

            return device;
	    }, new JsonTransformer());

    	put (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Edit a Device - request body: " + request.body());
        	DeviceDescriptor device = new Gson().fromJson(request.body(), DeviceDescriptor.class);
	        DeviceDescriptor deviceEntry = deviceRepository.findOne(request.params(":id"));
	        if(deviceEntry == null){
		    	log.debug("Could not save an edited Device Id: " + request.params(":id"));
		    	response.status(HttpStatus.SC_BAD_REQUEST);
	        }
	        else
	        {
				log.debug("Saving an edited Device: " + deviceEntry.getName());

				deviceEntry.setName(device.getName());
				if (device.getDeviceType() != null)
					deviceEntry.setDeviceType(device.getDeviceType());
				deviceEntry.setOnUrl(device.getOnUrl());
				deviceEntry.setOffUrl(device.getOffUrl());
				deviceEntry.setHttpVerb(device.getHttpVerb());
				deviceEntry.setContentType(device.getContentType());
				deviceEntry.setContentBody(device.getContentBody());
				deviceEntry.setContentBodyOff(device.getContentBodyOff());

				deviceRepository.save(deviceEntry);
				response.status(HttpStatus.SC_OK);
	        }
	        return deviceEntry;
    	}, new JsonTransformer());

    	get (API_CONTEXT, "application/json", (request, response) -> {
    		List<DeviceDescriptor> deviceList = deviceRepository.findAll();
	    	log.debug("Get all devices");
	    	JsonTransformer aRenderer = new JsonTransformer();
	    	String theStream = aRenderer.render(deviceList);
	    	log.debug("The Device List: " + theStream);
			response.status(HttpStatus.SC_OK);
    		return deviceList;
    	}, new JsonTransformer());

    	get (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Get a device");
	        DeviceDescriptor descriptor = deviceRepository.findOne(request.params(":id"));
	        if(descriptor == null)
				response.status(HttpStatus.SC_NOT_FOUND);
	        else
	        	response.status(HttpStatus.SC_OK);
	        return descriptor;
	    }, new JsonTransformer());

    	delete (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Delete a device");
	        DeviceDescriptor deleted = deviceRepository.findOne(request.params(":id"));
	        if(deleted == null)
				response.status(HttpStatus.SC_NOT_FOUND);
	        else
	        {
	        	deviceRepository.delete(deleted);
				response.status(HttpStatus.SC_OK);
	        }
	        return null;
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/vera/devices", "application/json", (request, response) -> {
	    	log.debug("Get vera devices");
	        Sdata sData = veraInfo.getSdata();
	        if(sData == null){
				response.status(HttpStatus.SC_NOT_FOUND);
				return null;
	        }

	      	response.status(HttpStatus.SC_OK);
	        return sData.getDevices();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/vera/scenes", "application/json", (request, response) -> {
	    	log.debug("Get vera scenes");
	        Sdata sData = veraInfo.getSdata();
	        if(sData == null){
				response.status(HttpStatus.SC_NOT_FOUND);
	            return null;
	        }
	      	response.status(HttpStatus.SC_OK);
	        return sData.getScenes();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/harmony/activities", "application/json", (request, response) -> {
	    	log.debug("Get harmony activities");
	      	if(myHarmonyHandler == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
		      	return null;	      		
	      	}
	      	response.status(HttpStatus.SC_OK);
	      	return myHarmonyHandler.getActivities();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/harmony/show", "application/json", (request, response) -> {
	    	log.debug("Get harmony current activity");
	      	if(myHarmonyHandler == null) {
	      		response.status(HttpStatus.SC_NOT_FOUND);
	      		return null;
	      	}
	      	response.status(HttpStatus.SC_OK);
      		return myHarmonyHandler.getCurrentActivity();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/harmony/devices", "application/json", (request, response) -> {
	    	log.debug("Get harmony devices");
	      	if(myHarmonyHandler == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
		      	return null;	      		
	      	}
	      	response.status(HttpStatus.SC_OK);
	      	return myHarmonyHandler.getDevices();
	    }, new JsonTransformer());

    }
}