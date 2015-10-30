package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.JsonTransformer;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.dao.*;
import com.bwssystems.harmony.ButtonPress;
import com.bwssystems.harmony.HarmonyHandler;
import com.bwssystems.harmony.RunActivity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import net.java.dev.eval.Expression;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import java.math.BigDecimal;
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
    private static final String INTENSITY_MATH = "${intensity.math(";
    private static final String INTENSITY_MATH_VALUE = "X";
    private static final String INTENSITY_MATH_CLOSE = ")}";
    private static final String HUE_CONTEXT = "/api";

    private DeviceRepository repository;
    private HarmonyHandler myHarmony;
    private HttpClient httpClient;
    private ObjectMapper mapper;


    public HueMulator(DeviceRepository aDeviceRepository, HarmonyHandler theHandler){
        httpClient = HttpClients.createDefault();
        mapper = new ObjectMapper(); //armzilla: work around Echo incorrect content type and breaking mapping. Map manually
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        repository = aDeviceRepository;
        myHarmony = theHandler;
    }

//	This function sets up the sparkjava rest calls for the hue api
    public void setupServer() {
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
	        response.status(HttpStatus.SC_OK);
	        return deviceResponseMap;
	    } , new JsonTransformer());

//		http://ip_address:port/api with body of user request returns json object for a success of user add
        post(HUE_CONTEXT, "application/json", (request, response) -> {
        	UserCreateRequest aNewUser = null;
        	String newUser = null;
        	String aDeviceType = null;
        	
        	log.debug("hue api user create requested: " + request.body() + " from " + request.ip());
	        
	        if(request.body() != null && !request.body().isEmpty()) {
	        	aNewUser = new Gson().fromJson(request.body(), UserCreateRequest.class);
	        	newUser = aNewUser.getUsername();
	        	aDeviceType = aNewUser.getDevicetype();
	        }
    		if(newUser == null)
    			newUser = "lightssystem";
    		
    		if(aDeviceType == null)
    			aDeviceType = "<not given>";
    		log.debug("hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);

			response.type("application/json; charset=utf-8"); 
    		response.status(HttpStatus.SC_OK);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
	    } );

        //		http://ip_address:port/api/* with body of user request returns json object for a success of user add - This method is for Harmony Hub
        post(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
        	UserCreateRequest aNewUser = null;
        	String newUser = null;
        	String aDeviceType = null;
        	
        	log.info("HH trace: hue api user create requested: " + request.body() + " from " + request.ip());
	        
	        if(request.body() != null && !request.body().isEmpty()) {
	        	aNewUser = new Gson().fromJson(request.body(), UserCreateRequest.class);
	        	newUser = aNewUser.getUsername();
	        	aDeviceType = aNewUser.getDevicetype();
	        }
    		if(newUser == null)
    			newUser = "lightssystem";
    		
    		if(aDeviceType == null)
    			aDeviceType = "<not given>";
    		log.debug("HH trace: hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);

			response.type("application/json; charset=utf-8"); 
    		response.status(HttpStatus.SC_OK);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";        
        } );

//		http://ip_address:port/api/{userId} returns json objects for the full state
	    get(HUE_CONTEXT + "/:userid", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue api full state requested: " + userId + " from " + request.ip());
	        List<DeviceDescriptor> descriptorList = repository.findAll();
	        if (descriptorList == null) {
	        	response.status(HttpStatus.SC_NOT_FOUND);
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
	        response.status(HttpStatus.SC_OK);
	        return apiResponse;
	    }, new JsonTransformer());

//		http://ip_address:port/api/{userId}/lights/{lightId} returns json object for a given light
	    get(HUE_CONTEXT + "/:userid/lights/:id", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        log.debug("hue light requested: " + lightId + " for user: " + userId + " from " + request.ip());
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(HttpStatus.SC_NOT_FOUND);
	            return null;
	        } else {
	            log.debug("found device named: " + device.getName());
	        }
	        DeviceResponse lightResponse = DeviceResponse.createResponse(device.getName(), device.getId());
	
			response.type("application/json; charset=utf-8"); 
	        response.status(HttpStatus.SC_OK);
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
	        	response.status(HttpStatus.SC_BAD_REQUEST);
	            return null;
	        }
	
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(HttpStatus.SC_NOT_FOUND);
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

	        if(device.getDeviceType().contains("activity"))
	        {
	        	log.debug("executing activity to Harmony: " + url);
	        	RunActivity anActivity = new Gson().fromJson(url, RunActivity.class);
	        	myHarmony.startActivity(anActivity);
	        }
	        else if(device.getDeviceType().contains("button"))
	        {
	        	log.debug("executing button press to Harmony: " + url);
	        	ButtonPress aDeviceButton = new Gson().fromJson(url, ButtonPress.class);
	        	myHarmony.pressButton(aDeviceButton);
	        }
	        else
	        {
	        	log.debug("executing activity to Http: " + url);
				// quick template
				String body;
				url = replaceIntensityValue(url, state.getBri());
				if (state.isOn())
					body = replaceIntensityValue(device.getContentBody(), state.getBri());
				else
					body = replaceIntensityValue(device.getContentBodyOff(), state.getBri());
				// make call
				if (!doHttpRequest(url, device.getHttpVerb(), device.getContentType(), body)) {
					response.status(HttpStatus.SC_SERVICE_UNAVAILABLE);
					log.error("Error on calling url to change device state: " + url);
					return null;
				}
	        }
	
			response.type("application/json; charset=utf-8"); 
	        response.status(HttpStatus.SC_OK);
	        return responseString;
	    });
    }

    /* light weight templating here, was going to use free marker but it was a bit too
    *  heavy for what we were trying to do.
    *
    *  currently provides:
    *  intensity.byte : 0-255 brightness.  this is raw from the echo
    *  intensity.percent : 0-100, adjusted for the vera
    *  intensity.math(X*1) : where X is the value from the interface call and can use net.java.dev.eval math
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
        } else if(request.contains(INTENSITY_MATH)){
        	Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        	String mathDescriptor = request.substring(request.indexOf(INTENSITY_MATH) + INTENSITY_MATH.length(),request.indexOf(INTENSITY_MATH_CLOSE));
        	variables.put(INTENSITY_MATH_VALUE, new BigDecimal(intensity));
        	 
        	try {
        		log.debug("Math eval is: " + mathDescriptor + ", Where " + INTENSITY_MATH_VALUE + " is: " + String.valueOf(intensity));
            	Expression exp = new Expression(mathDescriptor);
            	BigDecimal result = exp.eval(variables);
				Integer endResult = Math.round(result.floatValue());
	            request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE, endResult.toString());
			} catch (Exception e) {
				log.error("Could not execute Math: " + mathDescriptor, e);
			}        }
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
