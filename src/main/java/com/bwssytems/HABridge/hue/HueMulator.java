package com.bwssytems.HABridge.hue;

import com.bwssytems.HABridge.api.hue.DeviceResponse;
import com.bwssytems.HABridge.api.hue.DeviceState;
import com.bwssytems.HABridge.api.hue.HueApiResponse;
import com.bwssytems.HABridge.dao.*;
import com.bwssytems.HABridge.JsonTransformer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on Armzilla's HueMulator - a Philips Hue emulator using sparkjava rest server
 */

public class HueMulator {
    private static final Logger log = LoggerFactory.getLogger(HueMulator.class);
    private static final String INTENSITY_PERCENT = "${intensity.percent}";
    private static final String INTENSITY_BYTE = "${intensity.byte}";
    private static final String HUE_CONTEXT = "/api";

    private DeviceRepository repository;
    private HttpClient httpClient;
    private ObjectMapper mapper;


    public HueMulator(DeviceRepository aDeviceRepository){
        httpClient = HttpClients.createMinimal();
        mapper = new ObjectMapper(); //armzilla: work around Echo incorrect content type and breaking mapping. Map manually
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        repository = aDeviceRepository;
        setupEndpoints();
    }

//	This function sets up the sparkjava rest calls for the hue api
    private void setupEndpoints() {
//		http://ip_address:port/api/{userId}/lights  returns json objects of all lights configured
	    get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.info("hue lights list requested: " + userId + " from " + request.ip());
	        List<DeviceDescriptor> deviceList = repository.findByDeviceType("switch");
	    	JsonTransformer aRenderer = new JsonTransformer();
	    	String theStream = aRenderer.render(deviceList);
	    	log.debug("The Device List: " + theStream);
	        Map<String, String> deviceResponseMap = new HashMap<>();
	        for (DeviceDescriptor device : deviceList) {
	            deviceResponseMap.put(device.getId(), device.getName());
	        }
	        response.status(200);
	        return deviceResponseMap;
	    } , new JsonTransformer());

//		http://ip_address:port/api/* returns json object for a test call
        post(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
        	response.status(200);
	        return "[{\"success\":{\"username\":\"lights\"}}]";
	    } );

//		http://ip_address:port/api/{userId} returns json objects for the list of names of lights
	    get(HUE_CONTEXT + "/:userid", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.info("hue api root requested: " + userId + " from " + request.ip());
	        List<DeviceDescriptor> descriptorList = repository.findByDeviceType("switch");
	        if (descriptorList == null) {
	        	response.status(404);
	            return null;
	        }
	        Map<String, DeviceResponse> deviceList = new HashMap<>();
	
	        descriptorList.forEach(descriptor -> {
	                    DeviceResponse deviceResponse = DeviceResponse.createResponse(descriptor.getName(), descriptor.getId());
	                    deviceList.put(descriptor.getId(), deviceResponse);
	                }
	        );
	        HueApiResponse apiResponse = new HueApiResponse();
	        apiResponse.setLights(deviceList);
	
	        response.status(200);
	        return apiResponse;
	    }, new JsonTransformer());

//		http://ip_address:port/api/{userId}/lights/{lightId} returns json object for a given light
	    get(HUE_CONTEXT + "/:userid/lights/:id", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        log.info("hue light requested: " + lightId + "for user: " + userId + " from " + request.ip());
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(404);
	            return null;
	        } else {
	            log.info("found device named: " + device.getName());
	        }
	        DeviceResponse lightResponse = DeviceResponse.createResponse(device.getName(), device.getId());
	
	        response.status(200);
	        return lightResponse;
	    }, new JsonTransformer()); 

//		http://ip_address:port/api/{userId}/lights/{lightId}/state uses json object to set the lights state
	    put(HUE_CONTEXT + "/:userid/lights/:id/state", "application/json", (request, response) -> {
	        /**
	         * strangely enough the Echo sends a content type of application/x-www-form-urlencoded even though
	         * it sends a json object
	         */
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        log.info("hue state change requested: " + userId + " from " + request.ip());
	        log.info("hue stage change body: " + request.body() );
	
	        DeviceState state = null;
	        try {
	            state = mapper.readValue(request.body(), DeviceState.class);
	        } catch (IOException e) {
	            log.info("object mapper barfed on input", e);
	        	response.status(400);
	            return null;
	        }
	
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(404);
	            return null;
	        }
	
	        String responseString;
	        String url;
	        if (state.isOn()) {
	            responseString = "[{\"success\":{\"/lights/" + lightId + "/state/on\":true}}]";
	            url = device.getOnUrl();
	        } else {
	            responseString = "[{\"success\":{\"/lights/" + lightId + "/state/on\":false}}]";
	            url = device.getOffUrl();
	        }
	
	        /* light weight templating here, was going to use free marker but it was a bit too
	        *  heavy for what we were trying to do.
	        *
	        *  currently provides only two variables:
	        *  intensity.byte : 0-255 brightness.  this is raw from the echo
	        *  intensity.percent : 0-100, adjusted for the vera
	        */
	        if(url.contains(INTENSITY_BYTE)){
	            String intensityByte = String.valueOf(state.getBri());
	            url = url.replace(INTENSITY_BYTE, intensityByte);
	        }else if(url.contains(INTENSITY_PERCENT)){
	            int percentBrightness = (int) Math.round(state.getBri()/255.0*100);
	            String intensityPercent = String.valueOf(percentBrightness);
	            url = url.replace(INTENSITY_PERCENT, intensityPercent);
	        }
	
	        //make call
	        if(!doHttpGETRequest(url)){
	        	response.status(503);
	            return null;
	        }
	
	        response.status(200);
	        return responseString;
	    });
    }

//	This function executes the url from the device repository against the vera
    protected boolean doHttpGETRequest(String url) {
        log.info("calling GET on URL: " + url);
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
            log.info("GET on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                return true;
            }
        } catch (IOException e) {
            log.error("Error calling out to HA gateway", e);
        }
        return false;
    }
}
