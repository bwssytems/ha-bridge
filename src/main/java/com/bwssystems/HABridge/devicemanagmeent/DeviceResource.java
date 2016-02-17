package com.bwssystems.HABridge.devicemanagmeent;

import static spark.Spark.get;
import static spark.Spark.options;
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

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.JsonTransformer;
import com.bwssystems.HABridge.dao.BackupFilename;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.dao.DeviceRepository;
import com.bwssystems.HABridge.dao.ErrorMessage;
import com.bwssystems.NestBridge.NestHome;
import com.bwssystems.harmony.HarmonyHome;
import com.bwssystems.luupRequests.Device;
import com.bwssystems.luupRequests.Scene;
import com.bwssystems.vera.VeraHome;
import com.google.gson.Gson;

/**
	spark core server for bridge configuration
 */
public class DeviceResource {
    private static final String API_CONTEXT = "/api/devices";
    private static final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    private DeviceRepository deviceRepository;
    private VeraHome veraHome;
    private HarmonyHome myHarmonyHome;
    private NestHome nestHome;
    private static final Set<String> supportedVerbs = new HashSet<>(Arrays.asList("get", "put", "post"));

	public DeviceResource(BridgeSettingsDescriptor theSettings, HarmonyHome theHarmonyHome, NestHome aNestHome) {
		this.deviceRepository = new DeviceRepository(theSettings.getUpnpDeviceDb());

		if(theSettings.isValidVera())
			this.veraHome = new VeraHome(theSettings);
		else
			this.veraHome = null;
		
		if(theSettings.isValidHarmony())
			this.myHarmonyHome = theHarmonyHome;
		else
			this.myHarmonyHome = null;
		
		if(theSettings.isValidNest())
			this.nestHome = aNestHome;
		else
			this.nestHome = null;
		
        setupEndpoints();
	}

	public DeviceRepository getDeviceRepository() {
		return deviceRepository;
	}

    private void setupEndpoints() {
    	log.info("HABridge device management service started.... ");
	    // http://ip_address:port/api/devices CORS request
	    options(API_CONTEXT, "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post(API_CONTEXT, "application/json", (request, response) -> {
	    	log.info("Create a Device(s) - request body: " + request.body());
	    	DeviceDescriptor devices[];
	    	if(request.body().substring(0,1).equalsIgnoreCase("[") == true) {
	    		devices = new Gson().fromJson(request.body(), DeviceDescriptor[].class);
	    	}
	    	else {
	    		devices = new Gson().fromJson("[" + request.body() + "]", DeviceDescriptor[].class);
	    	}
	    	for(int i = 0; i < devices.length; i++) {
		    	if(devices[i].getContentBody() != null ) {
		            if (devices[i].getContentType() == null || devices[i].getHttpVerb() == null || !supportedVerbs.contains(devices[i].getHttpVerb().toLowerCase())) {
		            	response.status(HttpStatus.SC_BAD_REQUEST);
						log.debug("Bad http verb in create a Device(s): " + request.body());
						return new ErrorMessage("Bad http verb in create a Device(s): " + request.body() + " ");
		            }
		        }
	    	}

	    	deviceRepository.save(devices);
			log.debug("Created a Device(s): " + request.body());

	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.status(HttpStatus.SC_CREATED);

            return devices;
	    }, new JsonTransformer());

	    // http://ip_address:port/api/devices/:id CORS request
	    options(API_CONTEXT + "/:id", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	put (API_CONTEXT + "/:id", "application/json", (request, response) -> {
	    	log.debug("Edit a Device - request body: " + request.body());
        	DeviceDescriptor device = new Gson().fromJson(request.body(), DeviceDescriptor.class);
	        DeviceDescriptor deviceEntry = deviceRepository.findOne(request.params(":id"));
	        if(deviceEntry == null){
		    	log.debug("Could not save an edited Device Id: " + request.params(":id"));
		    	response.status(HttpStatus.SC_BAD_REQUEST);
		    	return new ErrorMessage("Could not save an edited Device Id: " + request.params(":id") + " ");
	        }
	        else
	        {
				log.debug("Saving an edited Device: " + deviceEntry.getName());

				deviceEntry.setName(device.getName());
				if (device.getDeviceType() != null)
					deviceEntry.setDeviceType(device.getDeviceType());
				deviceEntry.setMapId(device.getMapId());
				deviceEntry.setMapType(device.getMapType());
				deviceEntry.setTargetDevice(device.getTargetDevice());
				deviceEntry.setOnUrl(device.getOnUrl());
				deviceEntry.setOffUrl(device.getOffUrl());
				deviceEntry.setHttpVerb(device.getHttpVerb());
				deviceEntry.setContentType(device.getContentType());
				deviceEntry.setContentBody(device.getContentBody());
				deviceEntry.setContentBodyOff(device.getContentBodyOff());

				DeviceDescriptor[] theDevices = new DeviceDescriptor[1];
				theDevices[0] = deviceEntry;
				deviceRepository.save(theDevices);
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
	        if(descriptor == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("Could not find, id: " + request.params(":id") + " ");
	        }
	        else
	        	response.status(HttpStatus.SC_OK);
	        return descriptor;
	    }, new JsonTransformer());

    	delete (API_CONTEXT + "/:id", "application/json", (request, response) -> {
    		String anId = request.params(":id");
	    	log.debug("Delete a device: " + anId);
	        DeviceDescriptor deleted = deviceRepository.findOne(anId);
	        if(deleted == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("Could not delete, id: " + anId + " not found. ");
	        }
	        else
	        {
	        	deviceRepository.delete(deleted);
				response.status(HttpStatus.SC_OK);
	        }
	        return null;
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/vera/devices", "application/json", (request, response) -> {
	    	log.debug("Get vera devices");
	        if(veraHome == null){
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("A Vera is not available.");
	        }
	        List<Device> theDevices = veraHome.getDevices();
	        if(theDevices == null) {
	        	response.status(HttpStatus.SC_SERVICE_UNAVAILABLE);
	        	return new ErrorMessage("A Vera request failed to get devices. Check your Vera IP addresses.");
	        }
	        else
	        	response.status(HttpStatus.SC_OK);
	        return theDevices;
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/vera/scenes", "application/json", (request, response) -> {
	    	log.debug("Get vera scenes");
	        if(veraHome == null){
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("A Vera is not available.");
	        }
	        List<Scene> theScenes = veraHome.getScenes();
	        if(theScenes == null) {
	        	response.status(HttpStatus.SC_SERVICE_UNAVAILABLE);
	        	return new ErrorMessage("A Vera is not available and failed to get scenes. Check your Vera IP addresses.");
	        }
	        else
	        	response.status(HttpStatus.SC_OK);
	        return theScenes;
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/harmony/activities", "application/json", (request, response) -> {
	    	log.debug("Get harmony activities");
	      	if(myHarmonyHome == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("A Harmony is not available.");	      		
	      	}
	      	response.status(HttpStatus.SC_OK);
	      	return myHarmonyHome.getActivities();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/harmony/show", "application/json", (request, response) -> {
	    	log.debug("Get harmony current activity");
	      	if(myHarmonyHome == null) {
	      		response.status(HttpStatus.SC_NOT_FOUND);
	      		return new ErrorMessage("A Harmony is not available.");	
	      	}
	      	response.status(HttpStatus.SC_OK);
      		return myHarmonyHome.getCurrentActivities();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/harmony/devices", "application/json", (request, response) -> {
	    	log.debug("Get harmony devices");
	      	if(myHarmonyHome == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("A Harmony is not available.");		      		
	      	}
	      	response.status(HttpStatus.SC_OK);
	      	return myHarmonyHome.getDevices();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/nest/items", "application/json", (request, response) -> {
	    	log.debug("Get nest items");
	      	if(nestHome == null) {
				response.status(HttpStatus.SC_NOT_FOUND);
				return new ErrorMessage("A Nest is not available.");
	      	}
	      	response.status(HttpStatus.SC_OK);
	      	return nestHome.getItems();
	    }, new JsonTransformer());

    	get (API_CONTEXT + "/backup/available", "application/json", (request, response) -> {
        	log.debug("Get backup filenames");
          	response.status(HttpStatus.SC_OK);
          	return deviceRepository.getBackups();
        }, new JsonTransformer());

	    // http://ip_address:port/api/devices/backup/create CORS request
	    options(API_CONTEXT + "/backup/create", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	put (API_CONTEXT + "/backup/create", "application/json", (request, response) -> {
	    	log.debug("Create backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	BackupFilename returnFilename = new BackupFilename();
        	returnFilename.setFilename(deviceRepository.backup(aFilename.getFilename()));
	        return returnFilename;
    	}, new JsonTransformer());

	    // http://ip_address:port/api/devices/backup/delete CORS request
	    options(API_CONTEXT + "/backup/delete", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "POST");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post (API_CONTEXT + "/backup/delete", "application/json", (request, response) -> {
	    	log.debug("Delete backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	if(aFilename != null)
        		deviceRepository.deleteBackup(aFilename.getFilename());
        	else
        		log.warn("No filename given for delete backup.");
	        return null;
	    }, new JsonTransformer());

	    // http://ip_address:port/api/devices/backup/restore CORS request
	    options(API_CONTEXT + "/backup/restore", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "POST");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
    	post (API_CONTEXT + "/backup/restore", "application/json", (request, response) -> {
	    	log.debug("Restore backup: " + request.body());
        	BackupFilename aFilename = new Gson().fromJson(request.body(), BackupFilename.class);
        	if(aFilename != null) {
        		deviceRepository.restoreBackup(aFilename.getFilename());
        		deviceRepository.loadRepository();
        	}
        	else
        		log.warn("No filename given for restore backup.");
	        return null;
	    }, new JsonTransformer());
    }
}