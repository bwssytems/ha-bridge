package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.JsonTransformer;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.dao.*;
import com.bwssystems.NestBridge.NestInstruction;
import com.bwssystems.NestBridge.NestHome;
import com.bwssystems.harmony.ButtonPress;
import com.bwssystems.harmony.HarmonyHandler;
import com.bwssystems.harmony.HarmonyHome;
import com.bwssystems.harmony.RunActivity;
import com.bwssystems.nest.controller.Nest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import net.java.dev.eval.Expression;

import static spark.Spark.get;
import static spark.Spark.options;
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

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
    private HarmonyHome myHarmonyHome;
    private Nest theNest;
    private HttpClient httpClient;
    private ObjectMapper mapper;
    private BridgeSettings bridgeSettings;
    private byte[] sendData;


    public HueMulator(BridgeSettings theBridgeSettings, DeviceRepository aDeviceRepository, HarmonyHome theHarmonyHome, NestHome aNestHome){
        httpClient = HttpClients.createDefault();
        mapper = new ObjectMapper(); //armzilla: work around Echo incorrect content type and breaking mapping. Map manually
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        repository = aDeviceRepository;
		if(theBridgeSettings.isValidHarmony())
			this.myHarmonyHome = theHarmonyHome;
		else
			this.myHarmonyHome = null;
		if(theBridgeSettings.isValidNest())
			this.theNest = aNestHome.getTheNest();
		else
			this.theNest = null;
        bridgeSettings = theBridgeSettings;
    }

//	This function sets up the sparkjava rest calls for the hue api
    public void setupServer() {
    	log.info("Hue emulator service started....");
    	// http://ip_address:port/api/{userId}/lights  returns json objects of all lights configured
	    get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
        	if(bridgeSettings.isTraceupnp())
        		log.info("Traceupnp: hue lights list requested: " + userId + " from " + request.ip());
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

	    // http://ip_address:port/api CORS request
	    options(HUE_CONTEXT, "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
	    // http://ip_address:port/api with body of user request returns json object for a success of user add
        post(HUE_CONTEXT, "application/json", (request, response) -> {
        	UserCreateRequest aNewUser = null;
        	String newUser = null;
        	String aDeviceType = null;
        	
        	if(bridgeSettings.isTraceupnp())
        		log.info("Traceupnp: hue api/ user create requested: " + request.body() + " from " + request.ip());
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
        	if(bridgeSettings.isTraceupnp())
        		log.info("Traceupnp: hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);
    		log.debug("hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);

	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json; charset=utf-8"); 
    		response.status(HttpStatus.SC_OK);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
	    } );

	    // http://ip_address:port/api/* CORS request
	    options(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
        //		http://ip_address:port/api/* with body of user request returns json object for a success of user add - This method is for Harmony Hub
        post(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
        	UserCreateRequest aNewUser = null;
        	String newUser = null;
        	String aDeviceType = null;
        	
        	if(bridgeSettings.isTraceupnp())
        		log.info("Traceupnp: hue api/* user create requested: " + request.body() + " from " + request.ip());
	        
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

	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json; charset=utf-8"); 
    		response.status(HttpStatus.SC_OK);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";        
        } );

        // http://ip_address:port/api/config returns json objects for the config when no user is given
	    get(HUE_CONTEXT + "/config", "application/json", (request, response) -> {
        	if(bridgeSettings.isTraceupnp())
    	        log.info("Traceupnp: hue api/config config requested: <no_user> from " + request.ip());
	        log.debug("hue api config requested: <no_user> from " + request.ip());
	        HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(), "My App", "none");
	
			response.type("application/json; charset=utf-8"); 
	        response.status(HttpStatus.SC_OK);
//	        String responseString = null;
//	        responseString = "[{\"swversion\":\"" + apiResponse.getConfig().getSwversion() + "\",\"apiversion\":\"" + apiResponse.getConfig().getApiversion() + "\",\"name\":\"" + apiResponse.getConfig().getName() + "\",\"mac\":\"" + apiResponse.getConfig().getMac() + "\"}]";
//	        return  responseString;
	        return apiResponse.getConfig();
	    }, new JsonTransformer());

        // http://ip_address:port/api/{userId}/config returns json objects for the config
	    get(HUE_CONTEXT + "/:userid/config", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
        	if(bridgeSettings.isTraceupnp())
    	        log.info("Traceupnp: hue api/:userid/config config requested: " + userId + " from " + request.ip());
	        log.debug("hue api config requested: " + userId + " from " + request.ip());
	        HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(), "My App", userId);
	
			response.type("application/json; charset=utf-8"); 
	        response.status(HttpStatus.SC_OK);
	        return apiResponse.getConfig();
	    }, new JsonTransformer());


        // http://ip_address:port/api/{userId} returns json objects for the full state
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
	        HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(), "My App", userId);
	        apiResponse.setLights(deviceList);
	
			response.type("application/json; charset=utf-8"); 
	        response.status(HttpStatus.SC_OK);
	        return apiResponse;
	    }, new JsonTransformer());

	    // http://ip_address:port/api/{userId}/lights/{lightId} returns json object for a given light
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

	    // http://ip_address:port/api/:userid/lights/:id/state CORS request
	    options(HUE_CONTEXT + "/:userid/lights/:id/state", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html; charset=utf-8");
	    	return "";
	    });
	    // http://ip_address:port/api/{userId}/lights/{lightId}/state uses json object to set the lights state
	    put(HUE_CONTEXT + "/:userid/lights/:id/state", "application/json", (request, response) -> {
	        /**
	         * strangely enough the Echo sends a content type of application/x-www-form-urlencoded even though
	         * it sends a json object
	         */
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        String responseString = null;
	        String url = null;
	        DeviceState state = null;
	        log.debug("hue state change requested: " + userId + " from " + request.ip() + " body: " + request.body());
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json; charset=utf-8"); 
	        response.status(HttpStatus.SC_OK);
	
	        try {
	            state = mapper.readValue(request.body(), DeviceState.class);
	        } catch (IOException e) {
	        	log.warn("Object mapper barfed on input of body.", e);
        		responseString = "[{\"error\":{\"type\": 2, \"address\": \"/lights/" + lightId + ",\"description\": \"Object mapper barfed on input of body.\"}}]";
    	        return responseString;
	        }
	
	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from " + request.ip() + " body: " + request.body());
        		responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId + ",\"description\": \"Could not find device\", \"resource\": \"/lights/" + lightId + "\"}}]";
    	        return responseString;
	        }
	
	        if (state.isOn()) {
	            responseString = "[{\"success\":{\"/lights/" + lightId + "/state/on\":true}}";
	            url = device.getOnUrl();
	        } else if (request.body().contains("false")) {
	            responseString = "[{\"success\":{\"/lights/" + lightId + "/state/on\":false}}";
	            url = device.getOffUrl();
	        }

	        if(request.body().contains("bri"))
	        {
	        	if(url == null)
	        	{
		            url = device.getOnUrl();
	        		responseString = "[";
	        	}
	        	else
	        		responseString = responseString + ",";

	        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri\":" + state.getBri() + "}}]";
	        }
	        else
	        	responseString = responseString + "]";

	        if(device.getDeviceType().toLowerCase().contains("activity") || (device.getMapType() != null && device.getMapType().equalsIgnoreCase("harmonyActivity")))
	        {
	        	log.debug("executing HUE api request to change activity to Harmony: " + url);
	        	RunActivity anActivity = new Gson().fromJson(url, RunActivity.class);
	        	HarmonyHandler myHarmony = myHarmonyHome.getHarmonyHandler(device.getTargetDevice());
	        	if(myHarmony == null)
	        	{
	        		log.warn("Should not get here, no harmony hub available");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        	else
	        		myHarmony.startActivity(anActivity);
	        }
	        else if(device.getDeviceType().toLowerCase().contains("button") || (device.getMapType() != null && device.getMapType().equalsIgnoreCase("harmonyButton")))
	        {
	        	log.debug("executing HUE api request to button press(es) to Harmony: " + url);
	        	if(url.substring(0, 1).equalsIgnoreCase("{")) {
	        		url = "[" + url +"]";
	        	}
	        	ButtonPress[] deviceButtons = new Gson().fromJson(url, ButtonPress[].class);
	        	HarmonyHandler myHarmony = myHarmonyHome.getHarmonyHandler(device.getTargetDevice());
	        	if(myHarmony == null)
	        	{
	        		log.warn("Should not get here, no harmony hub available");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        	else {
	        		for(int i = 0; i < deviceButtons.length; i++) {
	        			if( i > 0)
	        				Thread.sleep(100);
	    	        	log.debug("pressing button: " + deviceButtons[i].getDevice() + " - " + deviceButtons[i].getButton() + " - iteration: " + String.valueOf(i));
	        			myHarmony.pressButton(deviceButtons[i]);
	        		}
	        	}
	        }
	        else if(device.getDeviceType().toLowerCase().contains("home") || (device.getMapType() != null && device.getMapType().equalsIgnoreCase("nestHomeAway")))
	        {
	        	log.debug("executing HUE api request to set away for nest home: " + url);
	        	NestInstruction homeAway = new Gson().fromJson(url, NestInstruction.class);
	        	if(theNest == null)
	        	{
	        		log.warn("Should not get here, no Nest available");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"Should not get here, no Nest available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        	else
	        		theNest.getHome(homeAway.getName()).setAway(homeAway.getAway());
	        }
	        else if(device.getDeviceType().toLowerCase().contains("thermo") || (device.getMapType() != null && device.getMapType().equalsIgnoreCase("nestThermoSet")))
	        {
	        	log.debug("executing HUE api request to set thermostat for nest: " + url);
	        	NestInstruction thermoSetting = new Gson().fromJson(url, NestInstruction.class);
	        	if(theNest == null)
	        	{
	        		log.warn("Should not get here, no Nest available");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"Should not get here, no Nest available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        	else {
	        		if(thermoSetting.getControl().equalsIgnoreCase("temp")) {
	        			if(request.body().contains("bri")) {
	        				thermoSetting.setTemp(String.valueOf(Math.round((Integer.parseInt(replaceIntensityValue(thermoSetting.getTemp(), state.getBri())) - 32)/1.8)));
	        				log.debug("Setting thermostat: " + thermoSetting.getName() + " to " + thermoSetting.getTemp() + "C");
	        				theNest.getThermostat(thermoSetting.getName()).setTargetTemperature(Float.parseFloat(thermoSetting.getTemp()));
	        			}
	        		}
	        		else if (thermoSetting.getControl().contains("range") ||thermoSetting.getControl().contains("heat") ||thermoSetting.getControl().contains("cool") ||thermoSetting.getControl().contains("off")) {
        				log.debug("Setting thermostat target type: " + thermoSetting.getName() + " to " + thermoSetting.getControl());
        				theNest.getThermostat(thermoSetting.getName()).setTargetType(thermoSetting.getControl());
	        		}
	        		else if(thermoSetting.getControl().contains("fan")) {
        				log.debug("Setting thermostat fan mode: " + thermoSetting.getName() + " to " + thermoSetting.getControl().substring(4));
	        			theNest.getThermostat(thermoSetting.getName()).setFanMode(thermoSetting.getControl().substring(4));
	        		}
	        		else {
		        		log.warn("no valid Nest control info: " + thermoSetting.getControl());
		        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"no valid Nest control info\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        		}
	        	}
	        }
	        else if(url.startsWith("udp://"))
	        {
	        	log.debug("executing HUE api request to UDP: " + url);
	        	try {
	        		String intermediate = url.substring(6);
	        		String ipAddr = intermediate.substring(0, intermediate.indexOf(':'));
	        		String port = intermediate.substring(intermediate.indexOf(':') + 1, intermediate.indexOf('/'));
	        		String theBody = intermediate.substring(intermediate.indexOf('/')+1);
	        		DatagramSocket responseSocket = new DatagramSocket(Integer.parseInt(port));
	        		if(theBody.startsWith("0x")) {
	        			sendData = DatatypeConverter.parseHexBinary(theBody.substring(2));
	        		}
	        		else
	        			sendData = theBody.getBytes();
	        		InetAddress IPAddress = InetAddress.getByName(ipAddr);
	        		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(port));
	        		responseSocket.send(sendPacket);
	        		responseSocket.close();
	    		}  catch (IOException e) {
	    			log.warn("Could not send UDP Datagram packet for request.", e);
	    			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	    		}
	        }
	        else
	        {
	        	log.debug("executing HUE api request to Http " + (device.getHttpVerb() == null?"GET":device.getHttpVerb()) + ": " + url);
				// quick template
				String body;
				url = replaceIntensityValue(url, state.getBri());
				if (state.isOn())
					body = replaceIntensityValue(device.getContentBody(), state.getBri());
				else
					body = replaceIntensityValue(device.getContentBodyOff(), state.getBri());
				// make call
				if (!doHttpRequest(url, device.getHttpVerb(), device.getContentType(), body)) {
					log.warn("Error on calling url to change device state: " + url);
					responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + ",\"description\": \"Error on calling url to change device state\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
				}
	        }
	
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
				log.warn("Could not execute Math: " + mathDescriptor, e);
			}
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
            log.debug((httpVerb == null?"GET":httpVerb) + " execute on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                return true;
            }
        } catch (IOException e) {
        	log.warn("Error calling out to HA gateway", e);
        }
        return false;
    }
}
