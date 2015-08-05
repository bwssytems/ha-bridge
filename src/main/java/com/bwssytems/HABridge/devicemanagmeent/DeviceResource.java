package com.bwssytems.HABridge.devicemanagmeent;

import com.bwssytems.HABridge.JsonTransformer;
import com.bwssytems.HABridge.dao.DeviceDescriptor;
import com.bwssytems.HABridge.dao.DeviceRepository;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.delete;
 
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
	spark core server for bridge configuration
 */
public class DeviceResource {
    private static final String API_CONTEXT = "/api/devices";
    private static final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    private DeviceRepository deviceRepository;


	public DeviceResource() {
		super();
		deviceRepository = new DeviceRepository();
        setupEndpoints();
	}

	public DeviceRepository getDeviceRepository() {
		return deviceRepository;
	}

    private void setupEndpoints() {
    	log.debug("Setting up endpoints");
    	post(API_CONTEXT, "application/json", (request, response) -> {
	    	log.debug("Create a Device - request body: " + request.body());
    		DeviceDescriptor device = new Gson().fromJson(request.body(), DeviceDescriptor.class);
	        DeviceDescriptor deviceEntry = new DeviceDescriptor();
	        deviceEntry.setName(device.getName());
	    	log.debug("Create a Device - device json name: " + deviceEntry.getName());
	        deviceEntry.setDeviceType(device.getDeviceType());
	    	log.debug("Create a Device - device json type:" + deviceEntry.getDeviceType());
	        deviceEntry.setOnUrl(device.getOnUrl());
	    	log.debug("Create a Device - device json on URL:" + deviceEntry.getOnUrl());
	        deviceEntry.setOffUrl(device.getOffUrl());
	    	log.debug("Create a Device - device json off URL:" + deviceEntry.getOffUrl());
	
	        deviceRepository.save(deviceEntry);
	    	log.debug("Created a Device");
	
            response.status(201);
            return deviceEntry;
	    }, new JsonTransformer());

    	put (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Saved a Device");
        	DeviceDescriptor device = new Gson().fromJson(request.body(), DeviceDescriptor.class);
	        DeviceDescriptor deviceEntry = deviceRepository.findOne(request.params(":id"));
	        if(deviceEntry == null){
	            return null;
	        }
	
	        deviceEntry.setName(device.getName());
	        deviceEntry.setDeviceType(device.getDeviceType());
	        deviceEntry.setOnUrl(device.getOnUrl());
	        deviceEntry.setOffUrl(device.getOffUrl());
	
	        deviceRepository.save(deviceEntry);
	        return deviceEntry;
    	}, new JsonTransformer());

    	get (API_CONTEXT, "application/json", (request, response) -> {
    		List<DeviceDescriptor> deviceList = deviceRepository.findAll();
	    	log.debug("Get all devices");
	    	JsonTransformer aRenderer = new JsonTransformer();
	    	String theStream = aRenderer.render(deviceList);
	    	log.debug("The Device List: " + theStream);
    		return deviceList;
    	}, new JsonTransformer());

    	get (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Get a device");
	        DeviceDescriptor descriptor = deviceRepository.findOne(request.params(":id"));
	        if(descriptor == null){
	            return null;
	        }
	        return descriptor;
	    }, new JsonTransformer());

    	delete (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Delete a device");
	        DeviceDescriptor deleted = deviceRepository.findOne(request.params(":id"));
	        if(deleted == null){
	            return null;
	        }
	        deviceRepository.delete(deleted);
	        return null;
	    }, new JsonTransformer());
    }
}
