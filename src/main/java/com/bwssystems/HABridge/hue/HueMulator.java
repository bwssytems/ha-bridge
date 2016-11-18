package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.GroupResponse;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorDetails;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.api.hue.HuePublicConfig;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.api.hue.WhitelistEntry;
import com.bwssystems.HABridge.dao.*;
import com.bwssystems.NestBridge.NestInstruction;
import com.bwssystems.NestBridge.NestHome;
import com.bwssystems.harmony.ButtonPress;
import com.bwssystems.harmony.HarmonyHandler;
import com.bwssystems.harmony.HarmonyHome;
import com.bwssystems.harmony.RunActivity;
import com.bwssystems.hue.HueDeviceIdentifier;
import com.bwssystems.hue.HueErrorStringSet;
import com.bwssystems.hue.HueHome;
import com.bwssystems.hue.HueUtil;
import com.bwssystems.mqtt.MQTTHandler;
import com.bwssystems.mqtt.MQTTHome;
import com.bwssystems.mqtt.MQTTMessage;
import com.bwssystems.nest.controller.Nest;
import com.bwssystems.util.JsonTransformer;
import com.bwssystems.util.UDPDatagramSender;
import com.google.gson.Gson;

import net.java.dev.eval.Expression;

import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

/**
 * Based on Armzilla's HueMulator - a Philips Hue emulator using sparkjava rest server
 */

public class HueMulator implements HueErrorStringSet {
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
    private HueHome myHueHome;
    private MQTTHome mqttHome;
    private HttpClient httpClient;
    private CloseableHttpClient httpclientSSL;
    private SSLContext sslcontext;
    private SSLConnectionSocketFactory sslsf;
    private RequestConfig globalConfig;
    private BridgeSettingsDescriptor bridgeSettings;
    private UDPDatagramSender theUDPDatagramSender;
    private byte[] sendData;
    private String hueUser;
    private String errorString;


    public HueMulator(BridgeSettingsDescriptor theBridgeSettings, DeviceRepository aDeviceRepository, HarmonyHome theHarmonyHome, NestHome aNestHome, HueHome aHueHome, MQTTHome aMqttHome, UDPDatagramSender aUdpDatagramSender) {
        httpClient = HttpClients.createDefault();
        // Trust own CA and all self-signed certs
        sslcontext = SSLContexts.createDefault();
        // Allow TLSv1 protocol only
        sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        httpclientSSL = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setDefaultRequestConfig(globalConfig)
                .build();

        repository = aDeviceRepository;
		if(theBridgeSettings.isValidHarmony())
			this.myHarmonyHome = theHarmonyHome;
		else
			this.myHarmonyHome = null;
		if(theBridgeSettings.isValidNest())
			this.theNest = aNestHome.getTheNest();
		else
			this.theNest = null;
		if(theBridgeSettings.isValidHue())
			this.myHueHome = aHueHome;
		else
			this.myHueHome = null;
		if(theBridgeSettings.isValidMQTT())
			this.mqttHome = aMqttHome;
		else
			this.mqttHome = null;
        bridgeSettings = theBridgeSettings;
        theUDPDatagramSender = aUdpDatagramSender;
        hueUser = null;
        errorString = null;
    }

//	This function sets up the sparkjava rest calls for the hue api
    public void setupServer() {
    	log.info("Hue emulator service started....");
    	// http://ip_address:port/api/{userId}/groups  returns json objects of all groups configured
	    get(HUE_CONTEXT + "/:userid/groups", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue groups list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}

	        return "{}";
	    });
    	// http://ip_address:port/api/{userId}/groups/{groupId}  returns json object for specified group. Only 0 is supported
	    get(HUE_CONTEXT + "/:userid/groups/:groupid", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	    	String groupId = request.params(":groupid");
	        log.debug("hue group 0 list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}
	    	
	    	if(groupId.equalsIgnoreCase("0")) {
	    		GroupResponse theResponse = GroupResponse.createGroupResponse(repository.findAllByRequester(request.ip()));
	    		return new Gson().toJson(theResponse, GroupResponse.class);
	    	}

    		return "[{\"error\":{\"type\":\"3\", \"address\": \"/api/" + userId + "/groups/" + "0" + "\",\"description\": \"Object not found\"}}]";
	    });
    	// http://ip_address:port/api/{userId}/scenes  returns json objects of all scenes configured
	    get(HUE_CONTEXT + "/:userid/scenes", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue scenes list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}

	        return "{}";
	    });
    	// http://ip_address:port/api/{userId}/schedules  returns json objects of all schedules configured
	    get(HUE_CONTEXT + "/:userid/schedules", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue schedules list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}

	        return "{}";
	    });
    	// http://ip_address:port/api/{userId}/sensors  returns json objects of all sensors configured
	    get(HUE_CONTEXT + "/:userid/sensors", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue sensors list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}

	        return "{}";
	    });
    	// http://ip_address:port/api/{userId}/rules  returns json objects of all rules configured
	    get(HUE_CONTEXT + "/:userid/rules", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue rules list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}

	        return "{}";
	    });
    	// http://ip_address:port/api/{userId}/resourcelinks  returns json objects of all resourcelinks configured
	    get(HUE_CONTEXT + "/:userid/resourcelinks", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        log.debug("hue resourcelinks list requested: " + userId + " from " + request.ip());
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}

	        return "{}";
	    });
    	// http://ip_address:port/api/{userId}/lights  returns json objects of all lights configured
	    get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
        	if(bridgeSettings.isTraceupnp())
        		log.info("Traceupnp: hue lights list requested: " + userId + " from " + request.ip());
	        log.debug("hue lights list requested: " + userId + " from " + request.ip());
			response.type("application/json"); 
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.status(HttpStatus.SC_OK);
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return theErrorResp.getTheErrors();
	    	}

	        List<DeviceDescriptor> deviceList = repository.findAllByRequester(request.ip());
	        Map<String, DeviceResponse> deviceResponseMap = new HashMap<>();
	        for (DeviceDescriptor device : deviceList) {
	        	DeviceResponse deviceResponse = null;
	        	String responseString;
	        	if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("hueDevice"))) {
		        	HueDeviceIdentifier deviceId = new Gson().fromJson(device.getOnUrl(), HueDeviceIdentifier.class);
		        	if(myHueHome.getTheHUERegisteredUser() == null) {
		        		hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(), myHueHome.getTheHUERegisteredUser(), this);
		        		if(hueUser == null) {
		        			return errorString;
		        		}
		        		myHueHome.setTheHUERegisteredUser(hueUser);
		        	}
					// make call
		        	responseString = doHttpRequest("http://"+deviceId.getIpAddress()+"/api/"+myHueHome.getTheHUERegisteredUser()+"/lights/"+deviceId.getDeviceId(), HttpGet.METHOD_NAME, device.getContentType(), null, null);
					if (responseString == null) {
						log.warn("Error on calling hue device to get state: " + device.getName());
		        		deviceResponse = DeviceResponse.createResponse(device);
					}
					else if(responseString.contains("[{\"error\":") && responseString.contains("unauthorized user")) {
						myHueHome.setTheHUERegisteredUser(null);
		        		hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(), myHueHome.getTheHUERegisteredUser(), this);
		        		if(hueUser == null) {
		        			return errorString;
		        		}
		        		myHueHome.setTheHUERegisteredUser(hueUser);
		        		deviceResponse = DeviceResponse.createResponse(device);
					}
					else {
						deviceResponse = new Gson().fromJson(responseString, DeviceResponse.class);
						if(deviceResponse == null)
							deviceResponse = DeviceResponse.createResponse(device);
					}
						
	        	}
	        	else
	        		deviceResponse = DeviceResponse.createResponse(device);
	            deviceResponseMap.put(device.getId(), deviceResponse);
	        }
	        return deviceResponseMap;
	    } , new JsonTransformer());

	    // http://ip_address:port/api CORS request
	    options(HUE_CONTEXT, "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html");
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
    			newUser = getNewUserID();
    		
    		validateWhitelistUser(newUser, false);
    		if(aDeviceType == null)
    			aDeviceType = "<not given>";
        	if(bridgeSettings.isTraceupnp())
        		log.info("Traceupnp: hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);
    		log.debug("hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);

	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json"); 
    		response.status(HttpStatus.SC_OK);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
	    } );

	    // http://ip_address:port/api/* CORS request
	    options(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html");
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
    			newUser = getNewUserID();
    		validateWhitelistUser(newUser, false);
    		if(aDeviceType == null)
    			aDeviceType = "<not given>";
    		log.debug("HH trace: hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);

			response.type("application/json"); 
    		response.status(HttpStatus.SC_OK);
	        return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";        
        } );

        // http://ip_address:port/api/config returns json objects for the public config when no user is given
	    get(HUE_CONTEXT + "/config", "application/json", (request, response) -> {
        	if(bridgeSettings.isTraceupnp())
    	        log.info("Traceupnp: hue api/config config requested: <no_user> from " + request.ip());
	        log.debug("hue api public config requested, from " + request.ip());
	        HuePublicConfig apiResponse = HuePublicConfig.createConfig("Philips hue", bridgeSettings.getUpnpConfigAddress());
	
			response.type("application/json"); 
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.status(HttpStatus.SC_OK);
	        return apiResponse;
	    }, new JsonTransformer());

        // http://ip_address:port/api/{userId}/config returns json objects for the config
	    get(HUE_CONTEXT + "/:userid/config", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
			response.type("application/json"); 
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.status(HttpStatus.SC_OK);
        	if(bridgeSettings.isTraceupnp())
    	        log.info("Traceupnp: hue api/:userid/config config requested: " + userId + " from " + request.ip());
    		log.debug("hue api config requested: " + userId + " from " + request.ip());
	    	if(validateWhitelistUser(userId, true) == null) {
	    		log.debug("Valudate user, No User supplied, returning public config");
		        HuePublicConfig apiResponse = HuePublicConfig.createConfig("Philips hue", bridgeSettings.getUpnpConfigAddress());
	    		return apiResponse;
	    	}

	        HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(), bridgeSettings.getWhitelist());
	
	        return apiResponse.getConfig();
	    }, new JsonTransformer());


        // http://ip_address:port/api/{userId} returns json objects for the full state
	    get(HUE_CONTEXT + "/:userid", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json"); 
	        response.status(HttpStatus.SC_OK);
	        log.debug("hue api full state requested: " + userId + " from " + request.ip());
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return theErrorResp.getTheErrors();
	    	}

	        List<DeviceDescriptor> descriptorList = repository.findAllByRequester(request.ip());
	    	HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(), bridgeSettings.getWhitelist());
	        Map<String, DeviceResponse> deviceList = new HashMap<>();
	        if (descriptorList != null) {
		        descriptorList.forEach(descriptor -> {
                    DeviceResponse deviceResponse = DeviceResponse.createResponse(descriptor);
                    deviceList.put(descriptor.getId(), deviceResponse);
                	}
		        );
		        apiResponse.setLights(deviceList);    	
	        }
	
	        return apiResponse;
	    }, new JsonTransformer());

	    // http://ip_address:port/api/{userId}/lights/{lightId} returns json object for a given light
	    get(HUE_CONTEXT + "/:userid/lights/:id", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json"); 
	        response.status(HttpStatus.SC_OK);
	        log.debug("hue light requested: " + lightId + " for user: " + userId + " from " + request.ip());
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return theErrorResp.getTheErrors();
	    	}

	    	DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	response.status(HttpStatus.SC_NOT_FOUND);
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("3", "/api/" + userId + "/lights/" + lightId, "Object not found", null, null, null)));
	            return theErrorResp.getTheErrors();
	        } else {
	            log.debug("found device named: " + device.getName());
	        }
	        DeviceResponse lightResponse = null;
        	String responseString;
        	if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("hueDevice"))) {
	        	HueDeviceIdentifier deviceId = new Gson().fromJson(device.getOnUrl(), HueDeviceIdentifier.class);
	        	if(myHueHome.getTheHUERegisteredUser() == null) {
	        		hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(), myHueHome.getTheHUERegisteredUser(), this);
	        		if(hueUser == null) {
	        			return errorString;
	        		}
	        		myHueHome.setTheHUERegisteredUser(hueUser);
	        	}
				// make call
	        	responseString = doHttpRequest("http://"+deviceId.getIpAddress()+"/api/"+myHueHome.getTheHUERegisteredUser()+"/lights/"+deviceId.getDeviceId(), HttpGet.METHOD_NAME, device.getContentType(), null, null);
				if (responseString == null) {
					log.warn("Error on calling hue device to get state: " + device.getName());
					lightResponse = DeviceResponse.createResponse(device);
				}
				else if(responseString.contains("[{\"error\":") && responseString.contains("unauthorized user")) {
					myHueHome.setTheHUERegisteredUser(null);
	        		hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(), myHueHome.getTheHUERegisteredUser(), this);
	        		if(hueUser == null) {
	        			return errorString;
	        		}
	        		myHueHome.setTheHUERegisteredUser(hueUser);
	        		lightResponse = DeviceResponse.createResponse(device);
				}
				else {
					lightResponse = new Gson().fromJson(responseString, DeviceResponse.class);
					if(lightResponse == null)
						lightResponse = DeviceResponse.createResponse(device);
				}
					
        	}
        	else
        		lightResponse = DeviceResponse.createResponse(device);
	
	        return lightResponse;
	    }, new JsonTransformer()); 

	    // http://ip_address:port/api/:userid/lights/:id/bridgeupdatestate CORS request
	    options(HUE_CONTEXT + "/:userid/lights/:id/bridgeupdatestate", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html");
	    	return "";
	    });
	    // http://ip_address:port/api/{userId}/lights/{lightId}/bridgeupdatestate uses json object to update the internal bridge lights state.
	    // THIS IS NOT A HUE API CALL... It is for state management if so desired.
	    put(HUE_CONTEXT + "/:userid/lights/:id/bridgeupdatestate", "application/json", (request, response) -> {
	    	String userId = request.params(":userid");
	    	String lightId = request.params(":id");
	        String responseString = null;
	        StateChangeBody theStateChanges = null;
	        DeviceState state = null;
	        boolean stateHasBri = false;
	        boolean stateHasBriInc = false;
	        log.debug("Update state requested: " + userId + " from " + request.ip() + " body: " + request.body());
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json"); 
	        response.status(HttpStatus.SC_OK);
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}
			theStateChanges = new Gson().fromJson(request.body(), StateChangeBody.class);
			if (theStateChanges == null) {
				log.warn("Could not parse state change body. Light state not changed.");
				responseString = "[{\"error\":{\"type\": 2, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not parse state change body.\"}}]";
				return responseString;
			}

			if (request.body().contains("\"bri\"")) {
				if(theStateChanges.isOn() && theStateChanges.getBri() == 0)
					stateHasBri = false;
				else
					stateHasBri = true;
			}
			if (request.body().contains("\"bri_inc\""))
				stateHasBriInc = true;

	        DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from " + request.ip() + " body: " + request.body());
        		responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId + "\",\"description\": \"Could not find device\", \"resource\": \"/lights/" + lightId + "\"}}]";
    	        return responseString;
	        }
	        state = device.getDeviceState();
	        if(state == null)
	        	state = DeviceState.createDeviceState();
	        state.fillIn();
	        if(stateHasBri)
	        {
	        	if(theStateChanges.getBri() > 0 && !state.isOn())
	        		state.setOn(true);
	        }
	        else if(stateHasBriInc) {
	        	if((state.getBri() + theStateChanges.getBri_inc()) > 0 && !state.isOn())
	        		state.setOn(true);
	        	else if((state.getBri() + theStateChanges.getBri_inc()) <= 0 && state.isOn())
	        		state.setOn(false);
	        }
	        else
	        {
		        if (theStateChanges.isOn()) {
		        	state.setOn(true);
		            if(state.getBri() <= 0)
		            	state.setBri(255);
		        } else {
		        	state.setOn(false);
		            state.setBri(0);
		        }
	        }
	        responseString = this.formatSuccessHueResponse(theStateChanges, request.body(), lightId, device.getDeviceState());
	        device.getDeviceState().setBri(calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc));
	    	
	        return responseString;
	    });
	    
	    // http://ip_address:port/api/:userid/lights/:id/state CORS request
	    options(HUE_CONTEXT + "/:userid/lights/:id/state", "application/json", (request, response) -> {
	        response.status(HttpStatus.SC_OK);
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
	        response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
	        response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
	        response.header("Content-Type", "text/html");
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
	        NameValue[] theHeaders = null;
	        StateChangeBody theStateChanges = null;
	        DeviceState state = null;
	        boolean stateHasBri = false;
	        boolean stateHasBriInc = false;
        	Integer theDelay = bridgeSettings.getButtonsleep();
	        log.debug("hue state change requested: " + userId + " from " + request.ip() + " body: " + request.body());
	        response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json"); 
	        response.status(HttpStatus.SC_OK);
	    	if(validateWhitelistUser(userId, false) == null) {
	    		log.debug("Valudate user, No User supplied");
	        	HueErrorResponse theErrorResp = new HueErrorResponse();
	        	theErrorResp.addError(new HueError(new HueErrorDetails("1", "/api/" + userId, "unauthorized user", null, null, null)));
	    		return new Gson().toJson(theErrorResp.getTheErrors());
	    	}
	
			theStateChanges = new Gson().fromJson(request.body(), StateChangeBody.class);
			if (theStateChanges == null) {
				log.warn("Could not parse state change body. Light state not changed.");
				responseString = "[{\"error\":{\"type\": 2, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not parse state change body.\"}}]";
				return responseString;
			}

			if (request.body().contains("\"bri\"")) {
				if(theStateChanges.isOn() && theStateChanges.getBri() == 0)
					stateHasBri = false;
				else
					stateHasBri = true;
			}
			if (request.body().contains("\"bri_inc\""))
				stateHasBriInc = true;

			DeviceDescriptor device = repository.findOne(lightId);
	        if (device == null) {
	        	log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from " + request.ip() + " body: " + request.body());
        		responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId + "\",\"description\": \"Could not find device\", \"resource\": \"/lights/" + lightId + "\"}}]";
    	        return responseString;
	        }
	        
	        state = device.getDeviceState();
	        if(state == null)
	        	state = DeviceState.createDeviceState();

	        theHeaders = new Gson().fromJson(device.getHeaders(), NameValue[].class);

	        if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("hueDevice")))
	        {
	        	if(myHueHome != null) {
		        	url = device.getOnUrl();
		        	HueDeviceIdentifier deviceId = new Gson().fromJson(url, HueDeviceIdentifier.class);
		        	if(myHueHome.getTheHUERegisteredUser() == null) {
		        		hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(), myHueHome.getTheHUERegisteredUser(), this);
		        		if(hueUser == null) {
		        			return errorString;
		        		}
		        		myHueHome.setTheHUERegisteredUser(hueUser);
		        	}
	
					// make call
		        	responseString = doHttpRequest("http://"+deviceId.getIpAddress()+"/api/"+myHueHome.getTheHUERegisteredUser()+"/lights/"+deviceId.getDeviceId()+"/state", HttpPut.METHOD_NAME, device.getContentType(), request.body(), null);
					if (responseString == null) {
						log.warn("Error on calling url to change device state: " + url);
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Error on calling HUE to change device state\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
					}
					else if(responseString.contains("[{\"error\":") && responseString.contains("unauthorized user")) {
						myHueHome.setTheHUERegisteredUser(null);
		        		hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(), myHueHome.getTheHUERegisteredUser(), this);
		        		if(hueUser == null) {
		        			return errorString;
		        		}
		        		myHueHome.setTheHUERegisteredUser(hueUser);
					}
	        	}
	        	else
					responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"No HUE configured\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        		
		        if(responseString == null || !responseString.contains("[{\"error\":")) {
			        responseString = this.formatSuccessHueResponse(theStateChanges, request.body(), lightId, state);
		        	state.setBri(calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc));
		        	device.setDeviceState(state);
		        }
				return responseString;
	        }

	        if(stateHasBri)
	        {
	        	if(theStateChanges.getBri() > 0 && !state.isOn())
	        		state.setOn(true);

        		url = device.getDimUrl();

	        	if(url == null || url.length() == 0)
		            url = device.getOnUrl();
	        }
	        else if(stateHasBriInc) {
	        	if((state.getBri() + theStateChanges.getBri_inc()) > 0 && !state.isOn())
	        		state.setOn(true);
	        	else if((state.getBri() + theStateChanges.getBri_inc()) <= 0 && state.isOn())
	        		state.setOn(false);

        		url = device.getDimUrl();

	        	if(url == null || url.length() == 0)
		            url = device.getOnUrl();	        	
	        }
	        else
	        {
		        if (theStateChanges.isOn()) {
		            url = device.getOnUrl();
		            state.setOn(true);
		            if(state.getBri() <= 0)
		            	state.setBri(255);
		        } else {
		            url = device.getOffUrl();
		            state.setOn(false);
		            state.setBri(0);
		        }
	        }
	
	        if (url == null) {
	        	log.warn("Could not find url: " + lightId + " for hue state change request: " + userId + " from " + request.ip() + " body: " + request.body());
        		responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId + "\",\"description\": \"Could not find url\", \"resource\": \"/lights/" + lightId + "\"}}]";
    	        return responseString;
	        }

	        
	        if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("harmonyActivity")))
	        {
	        	log.debug("executing HUE api request to change activity to Harmony: " + url);
	        	if(myHarmonyHome != null)
	        	{
		        	RunActivity anActivity = new Gson().fromJson(url, RunActivity.class);
		        	HarmonyHandler myHarmony = myHarmonyHome.getHarmonyHandler(device.getTargetDevice());
		        	if(myHarmony == null)
		        	{
		        		log.warn("Should not get here, no harmony hub available");
		        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
		        	}
		        	else
		        		myHarmony.startActivity(anActivity);
	        	}
	        	else {
	        		log.warn("Should not get here, no harmony configured");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no harmony configured\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        }
	        else if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("harmonyButton")))
	        {
	        	log.debug("executing HUE api request to button press(es) to Harmony: " + url);
	        	if(myHarmonyHome != null)
	        	{
		        	if(url.substring(0, 1).equalsIgnoreCase("{")) {
		        		url = "[" + url +"]";
		        	}
		        	ButtonPress[] deviceButtons = new Gson().fromJson(url, ButtonPress[].class);
		        	HarmonyHandler myHarmony = myHarmonyHome.getHarmonyHandler(device.getTargetDevice());
		        	if(myHarmony == null)
		        	{
		        		log.warn("Should not get here, no harmony hub available");
		        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
		        	}
		        	else {
			        	Integer setCount = 1;
		        		for(int i = 0; i < deviceButtons.length; i++) {
			        		if(deviceButtons[i].getCount() != null && deviceButtons[i].getCount() > 0)
			        			setCount = deviceButtons[i].getCount();
			        		else
			        			setCount = 1;
			        		for(int x = 0; x < setCount; x++) {
			        			if( x > 0 || i > 0) {
			        				Thread.sleep(theDelay);
			        			}
			        			if(deviceButtons[i].getDelay() != null && deviceButtons[i].getDelay() > 0)
			        				theDelay = deviceButtons[i].getDelay();
			        			else
			        				theDelay = bridgeSettings.getButtonsleep();
			    	        	log.debug("pressing button: " + deviceButtons[i].getDevice() + " - " + deviceButtons[i].getButton() + " - iteration: " + String.valueOf(i) + " - count: " + String.valueOf(x));
			        			myHarmony.pressButton(deviceButtons[i]);
			        		}
		        		}
		        	}
	        	}
	        	else {
	        		log.warn("Should not get here, no harmony configured");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no harmony configured\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        		
	        	}
	        }
	        else if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("nestHomeAway")))
	        {
	        	log.debug("executing HUE api request to set away for nest home: " + url);
	        	if(theNest == null)
	        	{
	        		log.warn("Should not get here, no Nest available");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no Nest available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        	else {
		        	NestInstruction homeAway = new Gson().fromJson(url, NestInstruction.class);
	        		theNest.getHome(homeAway.getName()).setAway(homeAway.getAway());
	        	}
	        }
	        else if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("nestThermoSet")))
	        {
	        	log.debug("executing HUE api request to set thermostat for nest: " + url);
	        	if(theNest == null)
	        	{
	        		log.warn("Should not get here, no Nest available");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no Nest available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        	}
	        	else {
		        	NestInstruction thermoSetting = new Gson().fromJson(url, NestInstruction.class);
	        		if(thermoSetting.getControl().equalsIgnoreCase("temp")) {
	        			if(request.body().contains("bri")) {
	        				if(bridgeSettings.isFarenheit())
	        					thermoSetting.setTemp(String.valueOf((Double.parseDouble(replaceIntensityValue(thermoSetting.getTemp(), calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false)) - 32.0)/1.8));
	        				else
	        					thermoSetting.setTemp(String.valueOf(Double.parseDouble(replaceIntensityValue(thermoSetting.getTemp(), calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false))));
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
		        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"no valid Nest control info\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        		}
	        	}
	        }
	        else if((device.getMapType() != null && device.getMapType().equalsIgnoreCase("mqttMessage")))
	        {
	        	log.debug("executing HUE api request to send message to MQTT broker: " + url);
	        	if(mqttHome != null)
	        	{
		        	if(url.substring(0, 1).equalsIgnoreCase("{")) {
		        		url = "[" + url +"]";
		        	}
		        	MQTTMessage[] mqttMessages = new Gson().fromJson(url, MQTTMessage[].class);
			        	Integer setCount = 1;
		        		for(int i = 0; i < mqttMessages.length; i++) {
				        	MQTTHandler mqttHandler = mqttHome.getMQTTHandler(mqttMessages[i].getClientId());
				        	if(mqttHandler == null)
				        	{
				        		log.warn("Should not get here, no mqtt hanlder available");
				        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no mqtt handler available\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
				        	}
			        		if(mqttMessages[i].getCount() != null && mqttMessages[i].getCount() > 0)
			        			setCount = mqttMessages[i].getCount();
			        		else
			        			setCount = 1;
			        		for(int x = 0; x < setCount; x++) {
			        			if( x > 0 || i > 0) {
			        				Thread.sleep(theDelay);
			        			}
			        			if(mqttMessages[i].getDelay() != null &&mqttMessages[i].getDelay() > 0)
			        				theDelay = mqttMessages[i].getDelay();
			        			else
			        				theDelay = bridgeSettings.getButtonsleep();
			    	        	log.debug("publishing message: " + mqttMessages[i].getClientId() + " - " + mqttMessages[i].getTopic() + " - " + mqttMessages[i].getMessage() + " - iteration: " + String.valueOf(i) + " - count: " + String.valueOf(x));
			    	        	mqttHandler.publishMessage(mqttMessages[i].getTopic(), mqttMessages[i].getMessage());
			        		}
		        		}
	        	}
	        	else {
	        		log.warn("Should not get here, no mqtt brokers configured");
	        		responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Should not get here, no mqtt brokers configured\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
	        		
	        	}
	        }
	        else if(device.getDeviceType().startsWith("exec")) {
	        	log.debug("Exec Request called with url: " + url);
	        	if(!url.startsWith("[")) {
	        		if(url.startsWith("{\"item"))
	        			url = "[" + url + "]";
	        		else
	        			url = "[{\"item\":\"" + url +"\"}]";
	        	}
	        	CallItem[] callItems = new Gson().fromJson(url, CallItem[].class);
	        	Integer setCount = 1;
	        	for(int i = 0; i < callItems.length; i++) {
	        		if(callItems[i].getCount() != null && callItems[i].getCount() > 0)
	        			setCount = callItems[i].getCount();
	        		else
	        			setCount = 1;
	        		for(int x = 0; x < setCount; x++) {
	        			if( x > 0 || i > 0) {
	        				Thread.sleep(theDelay);
	        			}
	        			if(callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
	        				theDelay = callItems[i].getDelay();
	        			else
	        				theDelay = bridgeSettings.getButtonsleep();
	        			String intermediate;
	        			if(callItems[i].getItem().contains("exec://"))
			        		intermediate = callItems[i].getItem().substring(callItems[i].getItem().indexOf("://") + 3);
			        	else
			        		intermediate = callItems[i].getItem();
	        			String anError = doExecRequest(intermediate, calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), lightId);
	        			if(anError != null) {
	        				responseString = anError;
							i = callItems.length+1;
	        			}
		        	}
	        	}
	        }
	        else // This section allows the usage of http/tcp/udp/exec calls in a given set of items
	        {
	        	log.debug("executing HUE api request for network call: " + url);
	        	if(!url.startsWith("[")) {
	        		if(url.startsWith("{\"item"))
	        			url = "[" + url + "]";
	        		else
	        			url = "[{\"item\":\"" + url +"\"}]";
	        	}
	        	CallItem[] callItems = new Gson().fromJson(url, CallItem[].class);
	        	Integer setCount = 1;
        		for(int i = 0; i < callItems.length; i++) {
	        		if(callItems[i].getCount() != null && callItems[i].getCount() > 0)
	        			setCount = callItems[i].getCount();
	        		else
	        			setCount = 1;
	        		for(int x = 0; x < setCount; x++) {
	        			if( x > 0 || i > 0) {
	        				Thread.sleep(theDelay);
	        			}
	        			if(callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
	        				theDelay = callItems[i].getDelay();
	        			else
	        				theDelay = bridgeSettings.getButtonsleep();
			        	try {
			        		if(callItems[i].getItem().contains("udp://") || callItems[i].getItem().contains("tcp://")) {
				        		String intermediate = callItems[i].getItem().substring(callItems[i].getItem().indexOf("://") + 3);
				        		String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
				        		String theUrlBody = intermediate.substring(intermediate.indexOf('/')+1);
				        		String hostAddr = null;
				        		String port = null;
				        		if(hostPortion.contains(":")) {
				        			hostAddr = hostPortion.substring(0, intermediate.indexOf(':'));
				        			port = hostPortion.substring(intermediate.indexOf(':') + 1);
				        		}
				        		else
				        			hostAddr = hostPortion;
				        		InetAddress IPAddress = InetAddress.getByName(hostAddr);;
				        		if(theUrlBody.startsWith("0x")) {
				        			theUrlBody = replaceIntensityValue(theUrlBody, calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), true);
				        			sendData = DatatypeConverter.parseHexBinary(theUrlBody.substring(2));
				        		}
				        		else {
				        			theUrlBody = replaceIntensityValue(theUrlBody, calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false);
				        			sendData = theUrlBody.getBytes();
				        		}
				        		if(callItems[i].getItem().contains("udp://")) {
				    	        	log.debug("executing HUE api request to UDP: " + callItems[i].getItem());
					        		theUDPDatagramSender.sendUDPResponse(new String(sendData), IPAddress, Integer.parseInt(port));
				        		}
				        		else if(callItems[i].getItem().contains("tcp://"))
				        		{
				    	        	log.debug("executing HUE api request to TCP: " + callItems[i].getItem());
					        		Socket dataSendSocket = new Socket(IPAddress, Integer.parseInt(port));
					        		DataOutputStream outToClient = new DataOutputStream(dataSendSocket.getOutputStream());
					        		outToClient.write(sendData);
					        		outToClient.flush();
					        		dataSendSocket.close();
				        		}
			        		}
			        		else if(callItems[i].getItem().contains("exec://")) {
				        		String intermediate = callItems[i].getItem().substring(callItems[i].getItem().indexOf("://") + 3);
			        			String anError = doExecRequest(intermediate, calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), lightId);
			        			if(anError != null) {
			        				responseString = anError;
									i = callItems.length+1;
			        			}
			        		}
			        		else {
			    	        	log.debug("executing HUE api request to Http " + (device.getHttpVerb() == null?"GET":device.getHttpVerb()) + ": " + callItems[i].getItem());
			        			
								String anUrl = replaceIntensityValue(callItems[i].getItem(), calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false);
								String body;
								if(stateHasBri || stateHasBriInc)
									body = replaceIntensityValue(device.getContentBodyDim(), calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false);
								else if (state.isOn())
									body = replaceIntensityValue(device.getContentBody(), calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false);
								else
									body = replaceIntensityValue(device.getContentBodyOff(), calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false);
								// make call
								if (doHttpRequest(anUrl, device.getHttpVerb(), device.getContentType(), body, theHeaders) == null) {
									log.warn("Error on calling url to change device state: " + anUrl);
									responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Error on calling url to change device state\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
									i = callItems.length+1;
								}
			        		}
			    		}  catch (Exception e) {
			    			log.warn("Change device state, Could not send data for network request: " + callItems[i].getItem() + " with Message: " + e.getMessage());
			    			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId + "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId + "state\"}}]";
							i = callItems.length+1;
			    		}
	        		}
        		}
	        }
	        
	        if(responseString == null || !responseString.contains("[{\"error\":")) {
		        responseString = this.formatSuccessHueResponse(theStateChanges, request.body(), lightId, state);
	        	state.setBri(calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc));
	        	device.setDeviceState(state);
	        }
	        return responseString;
	    });
    }

    private int calculateIntensity(DeviceState state, StateChangeBody theChanges, boolean hasBri, boolean hasBriInc) {
    	int setIntensity = state.getBri();
		if(hasBri) {
			setIntensity = theChanges.getBri();
		}
		else if(hasBriInc) {
			if((setIntensity + theChanges.getBri_inc()) < 0)
				setIntensity = 0;
			else if((setIntensity + theChanges.getBri_inc()) > 255)
				setIntensity = 255;
			else
				setIntensity = setIntensity + theChanges.getBri_inc();
		}
    	return setIntensity;
    }
 
    /* light weight templating here, was going to use free marker but it was a bit too
    *  heavy for what we were trying to do.
    *
    *  currently provides:
    *  intensity.byte : 0-255 brightness.  this is raw from the echo
    *  intensity.percent : 0-100, adjusted for the vera
    *  intensity.math(X*1) : where X is the value from the interface call and can use net.java.dev.eval math
    */
    protected String replaceIntensityValue(String request, int intensity, boolean isHex){
        if(request == null){
            return "";
        }
        if(request.contains(INTENSITY_BYTE)) {
            if(isHex) {
            	BigInteger bigInt = BigInteger.valueOf(intensity);
            	byte[] theBytes = bigInt.toByteArray();
            	String hexValue = DatatypeConverter.printHexBinary(theBytes);
            	request = request.replace(INTENSITY_BYTE, hexValue);
            }
            else {
                String intensityByte = String.valueOf(intensity);
            	request = request.replace(INTENSITY_BYTE, intensityByte);
            }
        } else if(request.contains(INTENSITY_PERCENT)) {
            int percentBrightness = (int) Math.round(intensity/255.0*100);
            if(isHex) {
            	BigInteger bigInt = BigInteger.valueOf(percentBrightness);
            	byte[] theBytes = bigInt.toByteArray();
            	String hexValue = DatatypeConverter.printHexBinary(theBytes);
            	request = request.replace(INTENSITY_PERCENT, hexValue);
            }
            else {
                String intensityPercent = String.valueOf(percentBrightness);
                request = request.replace(INTENSITY_PERCENT, intensityPercent);
            }
        } else if(request.contains(INTENSITY_MATH)) {
        	Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
        	String mathDescriptor = request.substring(request.indexOf(INTENSITY_MATH) + INTENSITY_MATH.length(),request.indexOf(INTENSITY_MATH_CLOSE));
        	variables.put(INTENSITY_MATH_VALUE, new BigDecimal(intensity));
        	 
        	try {
        		log.debug("Math eval is: " + mathDescriptor + ", Where " + INTENSITY_MATH_VALUE + " is: " + String.valueOf(intensity));
            	Expression exp = new Expression(mathDescriptor);
            	BigDecimal result = exp.eval(variables);
				Integer endResult = Math.round(result.floatValue());
	            if(isHex) {
	            	BigInteger bigInt = BigInteger.valueOf(endResult);
	            	byte[] theBytes = bigInt.toByteArray();
	            	String hexValue = DatatypeConverter.printHexBinary(theBytes);
	            	request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE, hexValue);
	            }
	            else {
	            	request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE, endResult.toString());
	            }
			} catch (Exception e) {
				log.warn("Could not execute Math: " + mathDescriptor, e);
			}
        }
        return request;
    }


//	This function executes the url from the device repository against the target as http or https as defined
    protected String doHttpRequest(String url, String httpVerb, String contentType, String body, NameValue[] headers) {
        HttpUriRequest request = null;
    	String theContent = null;
    	ContentType parsedContentType = null;
    	StringEntity requestBody = null;
        if(contentType != null && contentType.length() > 0) {
        	parsedContentType = ContentType.parse(contentType);
	        if(body != null && body.length() > 0)
	        	requestBody = new StringEntity(body, parsedContentType);
        }
        
        try {
	        if(HttpGet.METHOD_NAME.equalsIgnoreCase(httpVerb) || httpVerb == null) {
	            request = new HttpGet(url);
	        }else if(HttpPost.METHOD_NAME.equalsIgnoreCase(httpVerb)){
	            HttpPost postRequest = new HttpPost(url);
	            if(requestBody != null)
	            	postRequest.setEntity(requestBody);
	            request = postRequest;
	        }else if(HttpPut.METHOD_NAME.equalsIgnoreCase(httpVerb)){
	            HttpPut putRequest = new HttpPut(url);
	            if(requestBody != null)
	            	putRequest.setEntity(requestBody);
	            request = putRequest;
	        }
        } catch(IllegalArgumentException e) {
        	log.warn("Error creating outbound http request: IllegalArgumentException in log", e);
            return null;        	
        }
        log.debug("Making outbound call in doHttpRequest: " + request);
    	if(headers != null && headers.length > 0) {
    		for(int i = 0; i < headers.length; i++) {
    			request.setHeader(headers[i].getName(), headers[i].getValue());
    		}
    	}
        try {
        	HttpResponse response;
        	if(url.startsWith("https"))
        		response = httpclientSSL.execute(request);
        	else
        		response = httpClient.execute(request);
            log.debug((httpVerb == null?"GET":httpVerb) + " execute on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() >= 200  && response.getStatusLine().getStatusCode() < 300){
            	if(response.getEntity() != null ) {
	            	try {
	            		theContent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")); //read content for data
	            		EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
	            	} catch(Exception e) {
	            		log.debug("Error ocurred in handling response entity after successful call, still responding success. "+ e.getMessage(), e);
	            	}
            	}
        		if(theContent == null)
        			theContent = "";
            }
        } catch (IOException e) {
        	log.warn("Error calling out to HA gateway: IOException in log", e);
        }
        return theContent;
    }

	private String doExecRequest(String anItem, int intensity, String lightId) {
		log.debug("Executing request: " + anItem);
		String responseString = null;
		if(anItem != null && !anItem.equalsIgnoreCase("")) {
			try {
				Process p = Runtime.getRuntime().exec(replaceIntensityValue(anItem, intensity, false));
				log.debug("Process running: " + p.isAlive());
			} catch (IOException e) {
				log.warn("Could not execute request: " + anItem, e);
				responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
						+ "state\"}}]";
			}
		}
		else {
			log.warn("Could not execute request. Request is empty.");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
			+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
			+ "state\"}}]";
		}
			
		return responseString;
	}

    private String formatSuccessHueResponse(StateChangeBody state, String body, String lightId, DeviceState deviceState) {
    	
        String responseString = "[";
        boolean notFirstChange = false;
        if(body.contains("\"on\""))
        {
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/on\":";
	        if (state.isOn()) {
	            responseString = responseString + "true}}";
	        } else {
	            responseString = responseString + "false}}";
	        }
            if(deviceState != null)
            	deviceState.setOn(state.isOn());
	        notFirstChange = true;
        }
        
        if(body.contains("\"bri\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri\":" + state.getBri() + "}}";
            if(deviceState != null)
            	deviceState.setBri(state.getBri());
	        notFirstChange = true;
        }
        
        if(body.contains("\"bri_inc\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri_inc\":" + state.getBri_inc() + "}}";
        	//INFO: Bright inc check for deviceState needs to be outside of this method
            notFirstChange = true;
        }
        
        if(body.contains("\"ct\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct\":" + state.getCt() + "}}";
            if(deviceState != null)
            	deviceState.setCt(state.getCt());
	        notFirstChange = true;
        }
        
        if(body.contains("\"xy\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy\":" + state.getXy() + "}}";
            if(deviceState != null)
            	deviceState.setXy(state.getXy());
	        notFirstChange = true;
        }
        
        if(body.contains("\"hue\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue\":" + state.getHue() + "}}";
            if(deviceState != null)
            	deviceState.setHue(state.getHue());
	        notFirstChange = true;
        }
        
        if(body.contains("\"sat\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat\":" + state.getSat() + "}}";
            if(deviceState != null)
            	deviceState.setSat(state.getSat());
	        notFirstChange = true;
        }
        
        if(body.contains("\"ct_inc\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct_inc\":" + state.getCt_inc() + "}}";
            if(deviceState != null)
            	deviceState.setCt(deviceState.getCt() + state.getCt_inc());
	        notFirstChange = true;
        }
        
        if(body.contains("\"xy_inc\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy_inc\":" + state.getXy_inc() + "}}";
            if(deviceState != null)
            	deviceState.setXy(state.getXy());
	        notFirstChange = true;
        }
        
        if(body.contains("\"hue_inc\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue_inc\":" + state.getHue_inc() + "}}";
            if(deviceState != null)
            	deviceState.setHue(deviceState.getHue() + state.getHue_inc());
	        notFirstChange = true;
        }
        
        if(body.contains("\"sat_inc\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat_inc\":" + state.getSat_inc() + "}}";
            if(deviceState != null)
            	deviceState.setSat(deviceState.getSat() + state.getSat_inc());
	        notFirstChange = true;
        }
        
        if(body.contains("\"effect\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/effect\":" + state.getEffect() + "}}";
            if(deviceState != null)
            	deviceState.setEffect(state.getEffect());
	        notFirstChange = true;
        }
        
        if(body.contains("\"transitiontime\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/transitiontime\":" + state.getTransitiontime() + "}}";
//            if(deviceState != null)
//            	deviceState.setTransitiontime(state.getTransitiontime());
	        notFirstChange = true;
        }

        if(body.contains("\"alert\""))
        {
        	if(notFirstChange)
        		responseString = responseString + ",";
        	responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/alert\":" + state.getAlert() + "}}";
            if(deviceState != null)
            	deviceState.setAlert(state.getAlert());
	        notFirstChange = true;
        }

        responseString = responseString + "]";
        
        return responseString;
    }

    private String getNewUserID() {
    	UUID uid = UUID.randomUUID();
    	StringTokenizer st = new StringTokenizer(uid.toString(), "-");
    	String newUser = "";
    	while(st.hasMoreTokens()) {
    		newUser = newUser + st.nextToken();
    	}
    	
    	return newUser;
    }
    private String validateWhitelistUser(String aUser, boolean strict) {
    	if(aUser == null ||aUser.equalsIgnoreCase("undefined") || aUser.equalsIgnoreCase("null") || aUser.equalsIgnoreCase(""))
    		return null;
  
    	String validUser = null;
    	boolean found = false;
		if(bridgeSettings.getWhitelist() != null) {
	    	Set<String> theUserIds = bridgeSettings.getWhitelist().keySet();
	    	Iterator<String> userIterator = theUserIds.iterator();
	    	while(userIterator.hasNext()) {
	    		validUser = userIterator.next();
	    		if(validUser.equals(aUser))
	    			found = true;
	    	}
		}
		
		if(!found && strict)
			return null;
		
    	if(!found) {
    		if(bridgeSettings.getWhitelist() == null) {
    			Map<String, WhitelistEntry> awhitelist = new HashMap<>();
    			bridgeSettings.setWhitelist(awhitelist);
    		}
    		bridgeSettings.getWhitelist().put(aUser, WhitelistEntry.createEntry("auto insert user"));
    		bridgeSettings.setSettingsChanged(true);
    		
    	}
    	return aUser;
    }
	@Override
	public void setErrorString(String anError) {
		errorString = anError;
	}
}
