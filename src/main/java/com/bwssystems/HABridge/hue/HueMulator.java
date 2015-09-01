package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.JsonTransformer;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.dao.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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
        httpClient = HttpClients.createDefault();
        mapper = new ObjectMapper(); //armzilla: work around Echo incorrect content type and breaking mapping. Map manually
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        repository = aDeviceRepository;
        setupEndpoints();
    }

//	This function sets up the sparkjava rest calls for the hue api
    private void setupEndpoints() {
    	log.info("Hue emulator service started....");
//		http://ip_address:port/api/{userId}/lights  returns json objects of all lights configured
	    get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue lights list requested: " + userId + " from " + request.ip());
	        List<DeviceDescriptor> deviceList = repository.findAll();
	        Map<String, DeviceResponse> deviceResponseMap = new HashMap<>();
	        for (DeviceDescriptor device : deviceList) {
                DeviceResponse deviceResponse = DeviceResponse.createResponse(device.getName(), device.getId());
	            deviceResponseMap.put(device.getId(), deviceResponse);
	        }
			response.type("application/json; charset=utf-8"); 
	        response.status(200);
	        return deviceResponseMap;
	    } , new JsonTransformer());

//		http://ip_address:port/api with body of user request returns json object for a success of user add
        post(HUE_CONTEXT, "application/json", (request, response) -> {
	        log.debug("hue api user create requested: " + request.body() + " from " + request.ip());
    		UserCreateRequest aNewUser = new Gson().fromJson(request.body(), UserCreateRequest.class);
    		String newUser = aNewUser.getUsername();
    		if(newUser == null)
    			newUser = "lightssystem";
    		log.debug("hue api user create requested for device type: " + aNewUser.getDevicetype() + " and username: " + newUser);

			response.type("application/json; charset=utf-8"); 
    		response.status(200);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
	    } );

//		http://ip_address:port/api/{userId} returns json objects for the full state
	    get(HUE_CONTEXT + "/:userid", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue api full state requested: " + userId + " from " + request.ip());
	        List<DeviceDescriptor> descriptorList = repository.findAll();
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
	        HueApiResponse apiResponse = new HueApiResponse("Philips hue", request.ip(), "My App", userId);
	        apiResponse.setLights(deviceList);
	
			response.type("application/json; charset=utf-8"); 
	        response.status(200);
	        return apiResponse;
	    }, new JsonTransformer());

//		http://ip_address:port/api/{userId}/lights/{lightId} returns json object for a given light
	    get(HUE_CONTEXT + "/:userid/lights/:id", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        log.debug("hue light requested: " + lightId + " for user: " + userId + " from " + request.ip());
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(404);
	            return null;
	        } else {
	            log.debug("found device named: " + device.getName());
	        }
	        DeviceResponse lightResponse = DeviceResponse.createResponse(device.getName(), device.getId());
	
			response.type("application/json; charset=utf-8"); 
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
	        log.debug("hue state change requested: " + userId + " from " + request.ip() + " body: " + request.body());
	
	        DeviceState state = null;
	        try {
	            state = mapper.readValue(request.body(), DeviceState.class);
	        } catch (IOException e) {
	            log.error("Object mapper barfed on input of body.", e);
	        	response.status(400);
	            return null;
	        }
	
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(404);
	            log.error("Could not find devcie: " + lightId + " for hue state change request: " + userId + " from " + request.ip() + " body: " + request.body());
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
	
	        //quick template
	        url = replaceIntensityValue(url, state.getBri());
	        String body = replaceIntensityValue(device.getContentBody(), state.getBri());
	        //make call
	        if(!doHttpRequest(url, device.getHttpVerb(), device.getContentType(), body)){
	        	response.status(503);
	            log.error("Error on calling url to change device state: " + url);
	            return null;
	        }
	
			response.type("application/json; charset=utf-8"); 
	        response.status(200);
	        return responseString;
	    });
    }

    /* light weight templating here, was going to use free marker but it was a bit too
    *  heavy for what we were trying to do.
    *
    *  currently provides only two variables:
    *  intensity.byte : 0-255 brightness.  this is raw from the echo
    *  intensity.percent : 0-100, adjusted for the vera
    */
    protected String replaceIntensityValue(String request, int intensity){
        if(request == null){
            return "";
        }
        if(request.contains(INTENSITY_BYTE)){
            String intensityByte = String.valueOf(intensity);
            request = request.replace(INTENSITY_BYTE, intensityByte);
        }else if(request.contains(INTENSITY_PERCENT)){
            int percentBrightness = (int) Math.round(intensity/255.0*100);
            String intensityPercent = String.valueOf(percentBrightness);
            request = request.replace(INTENSITY_PERCENT, intensityPercent);
        }
        return request;
    }


//	This function executes the url from the device repository against the vera
    protected boolean doHttpRequest(String url, String httpVerb, String contentType, String body) {
        HttpUriRequest request = null;
        if(HttpGet.METHOD_NAME.equalsIgnoreCase(httpVerb) || httpVerb == null) {
            request = new HttpGet(url);
        }else if(HttpPost.METHOD_NAME.equalsIgnoreCase(httpVerb)){
            HttpPost postRequest = new HttpPost(url);
            ContentType parsedContentType = ContentType.parse(contentType);
            StringEntity requestBody = new StringEntity(body, parsedContentType);
            postRequest.setEntity(requestBody);
            request = postRequest;
        }else if(HttpPut.METHOD_NAME.equalsIgnoreCase(httpVerb)){
            HttpPut putRequest = new HttpPut(url);
            ContentType parsedContentType = ContentType.parse(contentType);
            StringEntity requestBody = new StringEntity(body, parsedContentType);
            putRequest.setEntity(requestBody);
            request = putRequest;
        }
        log.debug("Making outbound call in doHttpRequest: " + request);
        try {
            HttpResponse response = httpClient.execute(request);
            EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
            log.debug("Execute on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                return true;
            }
        } catch (IOException e) {
            log.error("Error calling out to HA gateway", e);
        }
        return false;
    }
}
