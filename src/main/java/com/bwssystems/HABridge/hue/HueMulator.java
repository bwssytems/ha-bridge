package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.GroupResponse;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.api.hue.HueError;
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
import com.bwssystems.hass.HassCommand;
import com.bwssystems.hass.HassHome;
import com.bwssystems.hass.HomeAssistant;
import com.bwssystems.hue.HueDeviceIdentifier;
import com.bwssystems.hue.HueHome;
import com.bwssystems.hue.HueUtil;
import com.bwssystems.mqtt.MQTTHandler;
import com.bwssystems.mqtt.MQTTHome;
import com.bwssystems.mqtt.MQTTMessage;
import com.bwssystems.nest.controller.Nest;
import com.bwssystems.util.JsonTransformer;
import com.bwssystems.util.UDPDatagramSender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import java.net.URI;
import java.net.URISyntaxException;
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
	private HueHome myHueHome;
	private MQTTHome mqttHome;
	private HassHome hassHome;
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
	private Integer setCount;
	private Integer theDelay;
	private Gson aGsonHandler;

	public HueMulator(BridgeSettingsDescriptor theBridgeSettings, DeviceRepository aDeviceRepository,
			HarmonyHome theHarmonyHome, NestHome aNestHome, HueHome aHueHome, MQTTHome aMqttHome, HassHome aHassHome,
			UDPDatagramSender aUdpDatagramSender) {
		httpClient = HttpClients.createDefault();
		// Trust own CA and all self-signed certs
		sslcontext = SSLContexts.createDefault();
		// Allow TLSv1 protocol only
		sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		httpclientSSL = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(globalConfig).build();

		repository = aDeviceRepository;
		if (theBridgeSettings.isValidHarmony())
			this.myHarmonyHome = theHarmonyHome;
		else
			this.myHarmonyHome = null;
		if (theBridgeSettings.isValidNest())
			this.theNest = aNestHome.getTheNest();
		else
			this.theNest = null;
		if (theBridgeSettings.isValidHue())
			this.myHueHome = aHueHome;
		else
			this.myHueHome = null;
		if (theBridgeSettings.isValidMQTT())
			this.mqttHome = aMqttHome;
		else
			this.mqttHome = null;
		if (theBridgeSettings.isValidHass())
			this.hassHome = aHassHome;
		else
			this.hassHome = null;
		bridgeSettings = theBridgeSettings;
		theUDPDatagramSender = aUdpDatagramSender;
		hueUser = null;
		errorString = null;
		aGsonHandler =
				new GsonBuilder()
		//	.registerTypeAdapter(CallItem.class, new CallItemDeserializer())
		.create();

	}

	// This function sets up the sparkjava rest calls for the hue api
	public void setupServer() {
		log.info("Hue emulator service started....");
		// http://ip_address:port/api/{userId}/groups returns json objects of
		// all groups configured
		get(HUE_CONTEXT + "/:userid/groups", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("groups", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/api/{userId}/groups/{groupId} returns json
		// object for specified group. Only 0 is supported
		get(HUE_CONTEXT + "/:userid/groups/:groupid", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return  groupsListHandler(request.params(":groupid"), request.params(":userid"),  request.ip());
		} , new JsonTransformer());
		// http://ip_address:port/api/{userId}/scenes returns json objects of
		// all scenes configured
		get(HUE_CONTEXT + "/:userid/scenes", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("scenes", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/api/{userId}/schedules returns json objects of
		// all schedules configured
		get(HUE_CONTEXT + "/:userid/schedules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("schedules", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/api/{userId}/sensors returns json objects of
		// all sensors configured
		get(HUE_CONTEXT + "/:userid/sensors", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("sensors", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/api/{userId}/rules returns json objects of all
		// rules configured
		get(HUE_CONTEXT + "/:userid/rules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("rules", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/api/{userId}/resourcelinks returns json
		// objects of all resourcelinks configured
		get(HUE_CONTEXT + "/:userid/resourcelinks", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("resourcelinks", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/api/{userId}/lights returns json objects of
		// all lights configured
		get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return lightsListHandler("lights", request.params(":userid"), request.ip());
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
		// http://ip_address:port/api with body of user request returns json
		// object for a success of user add
		post(HUE_CONTEXT, "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return userAdd(request.body(), request.ip(), false);
		});

		// http://ip_address:port/api/* CORS request
		options(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/api/* with body of user request returns json
		// object for a success of user add - This method is for Harmony Hub
		post(HUE_CONTEXT + "/*", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return userAdd(request.body(), request.ip(), true);
		});

		// http://ip_address:port/api/config returns json objects for the public
		// config when no user is given
		get(HUE_CONTEXT + "/config", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getConfig(null, request.ip());
		} , new JsonTransformer());

		// http://ip_address:port/api/{userId}/config returns json objects for
		// the config
		get(HUE_CONTEXT + "/:userid/config", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getConfig(request.params(":userid"), request.ip());
		} , new JsonTransformer());

		// http://ip_address:port/api/{userId} returns json objects for the full
		// state
		get(HUE_CONTEXT + "/:userid", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getFullState(request.params(":userid"), request.ip());
		} , new JsonTransformer());

		// http://ip_address:port/api/{userId}/ returns json objects for the full
		// state
		get(HUE_CONTEXT + "/:userid/", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getFullState(request.params(":userid"), request.ip());
		} , new JsonTransformer());

		// http://ip_address:port/api/{userId}/lights/{lightId} returns json
		// object for a given light
		get(HUE_CONTEXT + "/:userid/lights/:id", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getLight(request.params(":userid"), request.params(":id"), request.ip());
		} , new JsonTransformer());

		// http://ip_address:port/api/:userid/lights/:id/bridgeupdatestate CORS
		// request
		options(HUE_CONTEXT + "/:userid/lights/:id/bridgeupdatestate", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/api/{userId}/lights/{lightId}/bridgeupdatestate
		// uses json object to update the internal bridge lights state.
		// THIS IS NOT A HUE API CALL... It is for state management if so
		// desired.
		put(HUE_CONTEXT + "/:userid/lights/:id/bridgeupdatestate", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			String userId = request.params(":userid");
			String lightId = request.params(":id");
			String responseString = null;
			StateChangeBody theStateChanges = null;
			DeviceState state = null;
			boolean stateHasBri = false;
			boolean stateHasBriInc = false;
			log.debug("Update state requested: " + userId + " from " + request.ip() + " body: " + request.body());
			HueError[] theErrors = validateWhitelistUser(userId, false);
			if (theErrors != null)
				return aGsonHandler.toJson(theErrors);
			theStateChanges = aGsonHandler.fromJson(request.body(), StateChangeBody.class);
			if (theStateChanges == null) {
				log.warn("Could not parse state change body. Light state not changed.");
				responseString = "[{\"error\":{\"type\": 2, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not parse state change body.\"}}]";
				return responseString;
			}

			if (request.body().contains("\"bri\"")) {
				if (theStateChanges.isOn() && theStateChanges.getBri() == 0)
					stateHasBri = false;
				else
					stateHasBri = true;
			}
			if (request.body().contains("\"bri_inc\""))
				stateHasBriInc = true;

			DeviceDescriptor device = repository.findOne(lightId);
			if (device == null) {
				log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from "
						+ request.ip() + " body: " + request.body());
				responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not find device\", \"resource\": \"/lights/" + lightId + "\"}}]";
				return responseString;
			}
			state = device.getDeviceState();
			if (state == null)
				state = DeviceState.createDeviceState();
			state.fillIn();
			if (stateHasBri) {
				if (theStateChanges.getBri() > 0 && !state.isOn())
					state.setOn(true);
			} else if (stateHasBriInc) {
				if ((state.getBri() + theStateChanges.getBri_inc()) > 0 && !state.isOn())
					state.setOn(true);
				else if ((state.getBri() + theStateChanges.getBri_inc()) <= 0 && state.isOn())
					state.setOn(false);
			} else {
				if (theStateChanges.isOn()) {
					state.setOn(true);
					if (state.getBri() <= 0)
						state.setBri(255);
				} else {
					state.setOn(false);
					state.setBri(0);
				}
			}
			responseString = this.formatSuccessHueResponse(theStateChanges, request.body(), lightId,
					device.getDeviceState());
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
		// http://ip_address:port/api/{userId}/lights/{lightId}/state uses json
		// object to set the lights state
		put(HUE_CONTEXT + "/:userid/lights/:id/state", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			/**
			 * strangely enough the Echo sends a content type of
			 * application/x-www-form-urlencoded even though it sends a json
			 * object
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
			this.setTheDelay(bridgeSettings.getButtonsleep());
			this.setSetCount(1);
			log.debug("hue state change requested: " + userId + " from " + request.ip() + " body: " + request.body());
			HueError[] theErrors = validateWhitelistUser(userId, false);
			if (theErrors != null)
				return aGsonHandler.toJson(theErrors);

			theStateChanges = aGsonHandler.fromJson(request.body(), StateChangeBody.class);
			if (theStateChanges == null) {
				log.warn("Could not parse state change body. Light state not changed.");
				responseString = "[{\"error\":{\"type\": 2, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not parse state change body.\"}}]";
				return responseString;
			}

			if (request.body().contains("\"bri\"")) {
				stateHasBri = true;
			}
			if (request.body().contains("\"bri_inc\""))
				stateHasBriInc = true;

			DeviceDescriptor device = repository.findOne(lightId);
			if (device == null) {
				log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from "
						+ request.ip() + " body: " + request.body());
				responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not find device\", \"resource\": \"/lights/" + lightId + "\"}}]";
				return responseString;
			}

			state = device.getDeviceState();
			if (state == null)
				state = DeviceState.createDeviceState();

			theHeaders = aGsonHandler.fromJson(device.getHeaders(), NameValue[].class);

			if (stateHasBri) {
				if(!state.isOn())
					state.setOn(true);

				url = device.getDimUrl();

				if (url == null || url.length() == 0)
					url = device.getOnUrl();
			} else if (stateHasBriInc) {
				if(!state.isOn())
					state.setOn(true);
				if ((state.getBri() + theStateChanges.getBri_inc()) <= 0)
					state.setBri(theStateChanges.getBri_inc());

				url = device.getDimUrl();

				if (url == null || url.length() == 0)
					url = device.getOnUrl();
			} else {
				if (theStateChanges.isOn()) {
					url = device.getOnUrl();
					state.setOn(true);
//					if (state.getBri() <= 0)
//						state.setBri(255);
				} else if (!theStateChanges.isOn()) {
					url = device.getOffUrl();
					state.setOn(false);
//					state.setBri(0);
				}
			}

			// code for backwards compatibility
			if(!(device.getMapType() != null && device.getMapType().equalsIgnoreCase(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex]))) {
				if(url == null)
					url = device.getOnUrl();
			}
			CallItem[] callItems = null;
			if (url == null) {
				log.warn("Could not find url: " + lightId + " for hue state change request: " + userId + " from "
						+ request.ip() + " body: " + request.body());
				responseString = "[{\"error\":{\"type\": 3, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Could not find url\", \"resource\": \"/lights/" + lightId + "\"}}]";
			}
			else {
				if (!url.startsWith("[")) {
					if (url.startsWith("{\"item"))
						url = "[" + url + "]";
					else
						url = "[{\"item\":\"" + url + "\"}]";
				}
	
				// CallItem[] callItems = callItemGson.fromJson(url,
				// CallItem[].class);
				callItems = aGsonHandler.fromJson(url, CallItem[].class);
			}

			for (int i = 0; callItems != null && i < callItems.length; i++) {
				if(!filterByRequester(callItems[i].getFilterIPs(), request.ip())) {
					log.debug("filter for requester address not present in list: " + callItems[i].getFilterIPs() + " with request ip of: " + request.ip());
					continue;
				}
				if (callItems[i].getCount() != null && callItems[i].getCount() > 0)
					setCount = callItems[i].getCount();
				else
					setCount = 1;
				// code for backwards compatibility
				if((callItems[i].getType() == null || callItems[i].getType().trim().length() == 0)) {
					if(device.getMapType() != null && device.getMapType().length() > 0)
						callItems[i].setType(device.getMapType());
					else if(device.getDeviceType() != null && device.getDeviceType().length() > 0)
						callItems[i].setType(device.getDeviceType());
					else
						callItems[i].setType(DeviceMapTypes.CUSTOM_DEVICE[DeviceMapTypes.typeIndex]);
				}

				if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
					if (myHueHome != null) {

						HueDeviceIdentifier deviceId = aGsonHandler.fromJson(callItems[i].getItem(), HueDeviceIdentifier.class);
						if (myHueHome.getTheHUERegisteredUser() == null) {
							hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(),
									myHueHome.getTheHUERegisteredUser());
							if (hueUser == null) {
								return errorString;
							}
							myHueHome.setTheHUERegisteredUser(hueUser);
						}

						// make call
						for (int x = 0; x < setCount; x++) {
							if (x > 0 || i > 0) {
								Thread.sleep(theDelay);
							}
							if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
								theDelay = callItems[i].getDelay();
							else
								theDelay = bridgeSettings.getButtonsleep();
							responseString = doHttpRequest(
									"http://" + deviceId.getIpAddress() + "/api/" + myHueHome.getTheHUERegisteredUser()
											+ "/lights/" + deviceId.getDeviceId() + "/state",
									HttpPut.METHOD_NAME, device.getContentType(), request.body(), null);
							if (responseString.contains("[{\"error\":"))
									x = setCount;
						}
						if (responseString == null) {
							log.warn("Error on calling url to change device state: " + url);
							responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"Error on calling HUE to change device state\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						} else if (responseString.contains("[{\"error\":")) {
							if(responseString.contains("unauthorized user")) {
								myHueHome.setTheHUERegisteredUser(null);
								hueUser = HueUtil.registerWithHue(httpClient, deviceId.getIpAddress(), device.getName(),
										myHueHome.getTheHUERegisteredUser());
								if (hueUser == null) {
									return errorString;
								}
								myHueHome.setTheHUERegisteredUser(hueUser);
							}
							else
								log.warn("Error occurred when calling Hue Passthru: " + responseString);
						}
					} else {
						log.warn("No HUE home configured for HUE device passthru call for deviceID: " + device.getId());
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"No HUE configured\", \"parameter\": \"/lights/" + lightId
								+ "state\"}}]";
					}
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.HARMONY_ACTIVITY[DeviceMapTypes.typeIndex])) {
					log.debug("executing HUE api request to change activity to Harmony: " + url);
					if (myHarmonyHome != null) {
						RunActivity anActivity = aGsonHandler.fromJson(url, RunActivity.class);
						HarmonyHandler myHarmony = myHarmonyHome.getHarmonyHandler(device.getTargetDevice());
						if (myHarmony == null) {
							log.warn("Should not get here, no harmony hub available");
							responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						} else {
							for (int x = 0; x < setCount; x++) {
								if (x > 0 || i > 0) {
									Thread.sleep(theDelay);
								}
								if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
									theDelay = callItems[i].getDelay();
								else
									theDelay = bridgeSettings.getButtonsleep();
								myHarmony.startActivity(anActivity);
							}
						}
					} else {
						log.warn("Should not get here, no harmony configured");
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Should not get here, no harmony configured\", \"parameter\": \"/lights/"
								+ lightId + "state\"}}]";
					}
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.HARMONY_BUTTON[DeviceMapTypes.typeIndex])) {
					log.debug("executing HUE api request to button press(es) to Harmony: " + url);
					if (myHarmonyHome != null) {
						if (url.substring(0, 1).equalsIgnoreCase("{")) {
							url = "[" + url + "]";
						}
						ButtonPress[] deviceButtons = aGsonHandler.fromJson(url, ButtonPress[].class);
						HarmonyHandler myHarmony = myHarmonyHome.getHarmonyHandler(device.getTargetDevice());
						if (myHarmony == null) {
							log.warn("Should not get here, no harmony hub available");
							responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						} else {
							if(deviceButtons.length > 1) {
					        	Integer theCount = 1;
				        		for(int z = 0; z < deviceButtons.length; z++) {
					        		if(deviceButtons[z].getCount() != null && deviceButtons[z].getCount() > 0)
					        			theCount = deviceButtons[z].getCount();
					        		else
					        			theCount = 1;
					        		for(int y = 0; y < theCount; y++) {
					        			if( y > 0 || z > 0) {
					        				Thread.sleep(theDelay);
					        			}
					        			if(deviceButtons[z].getDelay() != null && deviceButtons[z].getDelay() > 0)
					        				theDelay = deviceButtons[z].getDelay();
					        			else
					        				theDelay = bridgeSettings.getButtonsleep();
					    	        	log.debug("pressing button: " + deviceButtons[z].getDevice() + " - " + deviceButtons[z].getButton() + " - iteration: " + String.valueOf(z) + " - count: " + String.valueOf(y));
					        			myHarmony.pressButton(deviceButtons[z]);
					        		}
				        		}
							}
							else {
								for (int x = 0; x < setCount; x++) {
									if (x > 0 || i > 0) {
										Thread.sleep(theDelay);
									}
									if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0) {
										theDelay = callItems[i].getDelay();
									}
									else
										theDelay = bridgeSettings.getButtonsleep();
									log.debug("pressing button: " + deviceButtons[i].getDevice() + " - "
											+ deviceButtons[i].getButton() + " - iteration: " + String.valueOf(i)
											+ " - count: " + String.valueOf(x));
									myHarmony.pressButton(deviceButtons[i]);
								}
							}
						}
					} else {
						log.warn("Should not get here, no harmony configured");
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Should not get here, no harmony configured\", \"parameter\": \"/lights/"
								+ lightId + "state\"}}]";

					}
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.NEST_HOMEAWAY[DeviceMapTypes.typeIndex])) {
					log.debug("executing HUE api request to set away for nest home: " + url);
					if (theNest == null) {
						log.warn("Should not get here, no Nest available");
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Should not get here, no Nest available\", \"parameter\": \"/lights/"
								+ lightId + "state\"}}]";
					} else {
						NestInstruction homeAway = aGsonHandler.fromJson(url, NestInstruction.class);
						theNest.getHome(homeAway.getName()).setAway(homeAway.getAway());
					}
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.NEST_THERMO_SET[DeviceMapTypes.typeIndex])) {
					log.debug("executing HUE api request to set thermostat for nest: " + url);
					if (theNest == null) {
						log.warn("Should not get here, no Nest available");
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Should not get here, no Nest available\", \"parameter\": \"/lights/"
								+ lightId + "state\"}}]";
					} else {
						NestInstruction thermoSetting = aGsonHandler.fromJson(url, NestInstruction.class);
						if (thermoSetting.getControl().equalsIgnoreCase("temp")) {
							if (request.body().contains("bri")) {
								if (bridgeSettings.isFarenheit())
									thermoSetting
											.setTemp(
													String.valueOf((Double
															.parseDouble(replaceIntensityValue(thermoSetting.getTemp(),
																	calculateIntensity(state, theStateChanges,
																			stateHasBri, stateHasBriInc),
																	false))
															- 32.0) / 1.8));
								else
									thermoSetting
											.setTemp(
													String.valueOf(Double.parseDouble(replaceIntensityValue(
															thermoSetting.getTemp(), calculateIntensity(state,
																	theStateChanges, stateHasBri, stateHasBriInc),
													false))));
								log.debug("Setting thermostat: " + thermoSetting.getName() + " to "
										+ thermoSetting.getTemp() + "C");
								theNest.getThermostat(thermoSetting.getName())
										.setTargetTemperature(Float.parseFloat(thermoSetting.getTemp()));
							}
						} else if (thermoSetting.getControl().contains("range")
								|| thermoSetting.getControl().contains("heat")
								|| thermoSetting.getControl().contains("cool")
								|| thermoSetting.getControl().contains("off")) {
							log.debug("Setting thermostat target type: " + thermoSetting.getName() + " to "
									+ thermoSetting.getControl());
							theNest.getThermostat(thermoSetting.getName()).setTargetType(thermoSetting.getControl());
						} else if (thermoSetting.getControl().contains("fan")) {
							log.debug("Setting thermostat fan mode: " + thermoSetting.getName() + " to "
									+ thermoSetting.getControl().substring(4));
							theNest.getThermostat(thermoSetting.getName())
									.setFanMode(thermoSetting.getControl().substring(4));
						} else {
							log.warn("no valid Nest control info: " + thermoSetting.getControl());
							responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"no valid Nest control info\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						}
					}
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.MQTT_MESSAGE[DeviceMapTypes.typeIndex])) {
					log.debug("executing HUE api request to send message to MQTT broker: " + url);
					if (mqttHome != null) {
						MQTTMessage[] mqttMessages = aGsonHandler.fromJson(replaceIntensityValue(url,
								calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false), MQTTMessage[].class);
						MQTTHandler mqttHandler = mqttHome.getMQTTHandler(mqttMessages[i].getClientId());
						if (mqttHandler == null) {
							log.warn("Should not get here, no mqtt hanlder available");
							responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"Should not get here, no mqtt handler available\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
						}
						for (int x = 0; x < setCount; x++) {
							if (x > 0 || i > 0) {
								Thread.sleep(theDelay);
							}
							if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
								theDelay = callItems[i].getDelay();
							else
								theDelay = bridgeSettings.getButtonsleep();
							log.debug("publishing message: " + mqttMessages[i].getClientId() + " - "
									+ mqttMessages[i].getTopic() + " - " + mqttMessages[i].getMessage()
									+ " - iteration: " + String.valueOf(i) + " - count: " + String.valueOf(x));
							mqttHandler.publishMessage(mqttMessages[i].getTopic(), mqttMessages[i].getMessage());
						}
					} else {
						log.warn("Should not get here, no mqtt brokers configured");
						responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
								+ "\",\"description\": \"Should not get here, no mqtt brokers configured\", \"parameter\": \"/lights/"
								+ lightId + "state\"}}]";

					}
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.HASS_DEVICE[DeviceMapTypes.typeIndex])) {
					responseString = hassDeviceHandler(callItems[i], lightId, i, state, theStateChanges, stateHasBri, stateHasBriInc);
				} else if (callItems[i].getType() != null && callItems[i].getType().trim().equalsIgnoreCase(DeviceMapTypes.EXEC_DEVICE[DeviceMapTypes.typeIndex])) {
					log.debug("Exec Request called with url: " + url);
					String intermediate;
					if (callItems[i].getItem().getAsString().contains("exec://"))
						intermediate = callItems[i].getItem().getAsString().substring(callItems[i].getItem().getAsString().indexOf("://") + 3);
					else
						intermediate = callItems[i].getItem().getAsString();
					for (int x = 0; x < setCount; x++) {
						if (x > 0 || i > 0) {
							Thread.sleep(theDelay);
						}
						if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
							theDelay = callItems[i].getDelay();
						else
							theDelay = bridgeSettings.getButtonsleep();
						String anError = doExecRequest(intermediate,
								calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), lightId);
						if (anError != null) {
							responseString = anError;
							x = setCount;
						}
					}
				} else // This section allows the usage of http/tcp/udp/exec
						// calls in a given set of items
				{
					log.debug("executing HUE api request for network call: " + url);
					for (int x = 0; x < setCount; x++) {
						if (x > 0 || i > 0) {
							Thread.sleep(theDelay);
						}
						if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
							theDelay = callItems[i].getDelay();
						else
							theDelay = bridgeSettings.getButtonsleep();
						try {
							if (callItems[i].getItem().getAsString().contains("udp://")
									|| callItems[i].getItem().getAsString().contains("tcp://")) {
								String intermediate = callItems[i].getItem().getAsString()
										.substring(callItems[i].getItem().getAsString().indexOf("://") + 3);
								String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
								String theUrlBody = intermediate.substring(intermediate.indexOf('/') + 1);
								String hostAddr = null;
								String port = null;
								if (hostPortion.contains(":")) {
									hostAddr = hostPortion.substring(0, intermediate.indexOf(':'));
									port = hostPortion.substring(intermediate.indexOf(':') + 1);
								} else
									hostAddr = hostPortion;
								InetAddress IPAddress = InetAddress.getByName(hostAddr);

								if (theUrlBody.startsWith("0x")) {
									theUrlBody = replaceIntensityValue(theUrlBody,
											calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc),
											true);
									sendData = DatatypeConverter.parseHexBinary(theUrlBody.substring(2));
								} else {
									theUrlBody = replaceIntensityValue(theUrlBody,
											calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc),
											false);
									sendData = theUrlBody.getBytes();
								}
								if (callItems[i].getItem().getAsString().contains("udp://")) {
									log.debug("executing HUE api request to UDP: " + callItems[i].getItem().getAsString());
									theUDPDatagramSender.sendUDPResponse(new String(sendData), IPAddress,
											Integer.parseInt(port));
								} else if (callItems[i].getItem().getAsString().contains("tcp://")) {
									log.debug("executing HUE api request to TCP: " + callItems[i].getItem().getAsString());
									Socket dataSendSocket = new Socket(IPAddress, Integer.parseInt(port));
									DataOutputStream outToClient = new DataOutputStream(
											dataSendSocket.getOutputStream());
									outToClient.write(sendData);
									outToClient.flush();
									dataSendSocket.close();
								}
							} else if (callItems[i].getItem().getAsString().contains("exec://")) {
								String intermediate = callItems[i].getItem().getAsString()
										.substring(callItems[i].getItem().getAsString().indexOf("://") + 3);
								String anError = doExecRequest(intermediate,
										calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc),
										lightId);
								if (anError != null) {
									responseString = anError;
									x = setCount;
								}
							} else {
								log.debug("executing HUE api request to Http "
										+ (device.getHttpVerb() == null ? "GET" : device.getHttpVerb()) + ": "
										+ callItems[i].getItem().getAsString());

								String anUrl = replaceIntensityValue(callItems[i].getItem().getAsString(),
										calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false);
								String body;
								if (stateHasBri || stateHasBriInc)
									body = replaceIntensityValue(device.getContentBodyDim(),
											calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc),
											false);
								else if (state.isOn())
									body = replaceIntensityValue(device.getContentBody(),
											calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc),
											false);
								else
									body = replaceIntensityValue(device.getContentBodyOff(),
											calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc),
											false);
								// make call
								if (doHttpRequest(anUrl, device.getHttpVerb(), device.getContentType(), body,
										theHeaders) == null) {
									log.warn("Error on calling url to change device state: " + anUrl);
									responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
											+ "\",\"description\": \"Error on calling url to change device state\", \"parameter\": \"/lights/"
											+ lightId + "state\"}}]";
									x = setCount;
								}
							}
						} catch (Exception e) {
							log.warn("Change device state, Could not send data for network request: "
									+ callItems[i].getItem().getAsString() + " with Message: " + e.getMessage());
							responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
									+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/"
									+ lightId + "state\"}}]";
							x = setCount;
						}
					}
				}
			}

			if (responseString == null || !responseString.contains("[{\"error\":")) {
				responseString = this.formatSuccessHueResponse(theStateChanges, request.body(), lightId, state);
				state.setBri(calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc));
				device.setDeviceState(state);
			}
			return responseString;
		});
	}

	private int calculateIntensity(DeviceState state, StateChangeBody theChanges, boolean hasBri, boolean hasBriInc) {
		int setIntensity = state.getBri();
		if (hasBri) {
			setIntensity = theChanges.getBri();
		} else if (hasBriInc) {
			if ((setIntensity + theChanges.getBri_inc()) <= 0)
				setIntensity = theChanges.getBri_inc();
			else if ((setIntensity + theChanges.getBri_inc()) > 255)
				setIntensity = theChanges.getBri_inc();
			else
				setIntensity = setIntensity + theChanges.getBri_inc();
		}
		return setIntensity;
	}

	/*
	 * light weight templating here, was going to use free marker but it was a
	 * bit too heavy for what we were trying to do.
	 *
	 * currently provides: intensity.byte : 0-255 brightness. this is raw from
	 * the echo intensity.percent : 0-100, adjusted for the vera
	 * intensity.math(X*1) : where X is the value from the interface call and
	 * can use net.java.dev.eval math
	 */
	protected String replaceIntensityValue(String request, int intensity, boolean isHex) {
		if (request == null) {
			return null;
		}
		if (request.contains(INTENSITY_BYTE)) {
			if (isHex) {
				BigInteger bigInt = BigInteger.valueOf(intensity);
				byte[] theBytes = bigInt.toByteArray();
				String hexValue = DatatypeConverter.printHexBinary(theBytes);
				request = request.replace(INTENSITY_BYTE, hexValue);
			} else {
				String intensityByte = String.valueOf(intensity);
				request = request.replace(INTENSITY_BYTE, intensityByte);
			}
		} else if (request.contains(INTENSITY_PERCENT)) {
			int percentBrightness = (int) Math.round(intensity / 255.0 * 100);
			if (isHex) {
				BigInteger bigInt = BigInteger.valueOf(percentBrightness);
				byte[] theBytes = bigInt.toByteArray();
				String hexValue = DatatypeConverter.printHexBinary(theBytes);
				request = request.replace(INTENSITY_PERCENT, hexValue);
			} else {
				String intensityPercent = String.valueOf(percentBrightness);
				request = request.replace(INTENSITY_PERCENT, intensityPercent);
			}
		} else if (request.contains(INTENSITY_MATH)) {
			Map<String, BigDecimal> variables = new HashMap<String, BigDecimal>();
			String mathDescriptor = request.substring(request.indexOf(INTENSITY_MATH) + INTENSITY_MATH.length(),
					request.indexOf(INTENSITY_MATH_CLOSE));
			variables.put(INTENSITY_MATH_VALUE, new BigDecimal(intensity));

			try {
				log.debug("Math eval is: " + mathDescriptor + ", Where " + INTENSITY_MATH_VALUE + " is: "
						+ String.valueOf(intensity));
				Expression exp = new Expression(mathDescriptor);
				BigDecimal result = exp.eval(variables);
				Integer endResult = Math.round(result.floatValue());
				if (isHex) {
					BigInteger bigInt = BigInteger.valueOf(endResult);
					byte[] theBytes = bigInt.toByteArray();
					String hexValue = DatatypeConverter.printHexBinary(theBytes);
					request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE, hexValue);
				} else {
					request = request.replace(INTENSITY_MATH + mathDescriptor + INTENSITY_MATH_CLOSE,
							endResult.toString());
				}
			} catch (Exception e) {
				log.warn("Could not execute Math: " + mathDescriptor, e);
			}
		}
		return request;
	}

	// This function executes the url from the device repository against the
	// target as http or https as defined
	protected String doHttpRequest(String url, String httpVerb, String contentType, String body, NameValue[] headers) {
		HttpUriRequest request = null;
		String theContent = null;
		URI theURI = null;
		ContentType parsedContentType = null;
		StringEntity requestBody = null;
		if (contentType != null && contentType.length() > 0) {
			parsedContentType = ContentType.parse(contentType);
			if (body != null && body.length() > 0)
				requestBody = new StringEntity(body, parsedContentType);
		}
		try {
			theURI = new URI(url);
		} catch (URISyntaxException e1) {
			log.warn("Error creating URI http request: " + url + " with message: " + e1.getMessage());
			return null;
		}
		try {
			if (HttpGet.METHOD_NAME.equalsIgnoreCase(httpVerb) || httpVerb == null) {
				request = new HttpGet(theURI);
			} else if (HttpPost.METHOD_NAME.equalsIgnoreCase(httpVerb)) {
				HttpPost postRequest = new HttpPost(theURI);
				if (requestBody != null)
					postRequest.setEntity(requestBody);
				request = postRequest;
			} else if (HttpPut.METHOD_NAME.equalsIgnoreCase(httpVerb)) {
				HttpPut putRequest = new HttpPut(theURI);
				if (requestBody != null)
					putRequest.setEntity(requestBody);
				request = putRequest;
			}
		} catch (IllegalArgumentException e) {
			log.warn("Error creating outbound http request: IllegalArgumentException in log", e);
			return null;
		}
		log.debug("Making outbound call in doHttpRequest: " + request);
		if (headers != null && headers.length > 0) {
			for (int i = 0; i < headers.length; i++) {
				request.setHeader(headers[i].getName(), headers[i].getValue());
			}
		}
		try {
			HttpResponse response;
			if (url.startsWith("https"))
				response = httpclientSSL.execute(request);
			else
				response = httpClient.execute(request);
			log.debug((httpVerb == null ? "GET" : httpVerb) + " execute on URL responded: "
					+ response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300) {
				if (response.getEntity() != null) {
					try {
						theContent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")); // read
																											// content
																											// for
																											// data
						EntityUtils.consume(response.getEntity()); // close out
																	// inputstream
																	// ignore
																	// content
					} catch (Exception e) {
						log.debug(
								"Error ocurred in handling response entity after successful call, still responding success. "
										+ e.getMessage(),
								e);
					}
				}
				if (theContent == null)
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
		if (anItem != null && !anItem.equalsIgnoreCase("")) {
			try {
				Process p = Runtime.getRuntime().exec(replaceIntensityValue(anItem, intensity, false));
				log.debug("Process running: " + p.isAlive());
			} catch (IOException e) {
				log.warn("Could not execute request: " + anItem, e);
				responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
						+ "state\"}}]";
			}
		} else {
			log.warn("Could not execute request. Request is empty.");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Error on calling out to device\", \"parameter\": \"/lights/" + lightId
					+ "state\"}}]";
		}

		return responseString;
	}

	private String formatSuccessHueResponse(StateChangeBody state, String body, String lightId,
			DeviceState deviceState) {

		String responseString = "[";
		boolean notFirstChange = false;
		if (body.contains("\"on\"")) {
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/on\":";
			if (state.isOn()) {
				responseString = responseString + "true}}";
			} else {
				responseString = responseString + "false}}";
			}
			if (deviceState != null)
				deviceState.setOn(state.isOn());
			notFirstChange = true;
		}

		if (body.contains("\"bri\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri\":" + state.getBri()
					+ "}}";
			if (deviceState != null)
				deviceState.setBri(state.getBri());
			notFirstChange = true;
		}

		if (body.contains("\"bri_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri_inc\":"
					+ state.getBri_inc() + "}}";
			// INFO: Bright inc check for deviceState needs to be outside of
			// this method
			notFirstChange = true;
		}

		if (body.contains("\"ct\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct\":" + state.getCt()
					+ "}}";
			if (deviceState != null)
				deviceState.setCt(state.getCt());
			notFirstChange = true;
		}

		if (body.contains("\"xy\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy\":" + state.getXy()
					+ "}}";
			if (deviceState != null)
				deviceState.setXy(state.getXy());
			notFirstChange = true;
		}

		if (body.contains("\"hue\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue\":" + state.getHue()
					+ "}}";
			if (deviceState != null)
				deviceState.setHue(state.getHue());
			notFirstChange = true;
		}

		if (body.contains("\"sat\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat\":" + state.getSat()
					+ "}}";
			if (deviceState != null)
				deviceState.setSat(state.getSat());
			notFirstChange = true;
		}

		if (body.contains("\"ct_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct_inc\":"
					+ state.getCt_inc() + "}}";
			if (deviceState != null)
				deviceState.setCt(deviceState.getCt() + state.getCt_inc());
			notFirstChange = true;
		}

		if (body.contains("\"xy_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy_inc\":"
					+ state.getXy_inc() + "}}";
			if (deviceState != null)
				deviceState.setXy(state.getXy());
			notFirstChange = true;
		}

		if (body.contains("\"hue_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue_inc\":"
					+ state.getHue_inc() + "}}";
			if (deviceState != null)
				deviceState.setHue(deviceState.getHue() + state.getHue_inc());
			notFirstChange = true;
		}

		if (body.contains("\"sat_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat_inc\":"
					+ state.getSat_inc() + "}}";
			if (deviceState != null)
				deviceState.setSat(deviceState.getSat() + state.getSat_inc());
			notFirstChange = true;
		}

		if (body.contains("\"effect\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/effect\":"
					+ state.getEffect() + "}}";
			if (deviceState != null)
				deviceState.setEffect(state.getEffect());
			notFirstChange = true;
		}

		if (body.contains("\"transitiontime\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/transitiontime\":"
					+ state.getTransitiontime() + "}}";
			// if(deviceState != null)
			// deviceState.setTransitiontime(state.getTransitiontime());
			notFirstChange = true;
		}

		if (body.contains("\"alert\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/alert\":"
					+ state.getAlert() + "}}";
			if (deviceState != null)
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
		while (st.hasMoreTokens()) {
			newUser = newUser + st.nextToken();
		}

		return newUser;
	}
	private HueError[] validateWhitelistUser(String aUser, boolean strict) {
		String validUser = null;
		boolean found = false;
		if (aUser != null && aUser.equalsIgnoreCase("undefined") && aUser.equalsIgnoreCase("null")
				&& aUser.equalsIgnoreCase("")) {
			if (bridgeSettings.getWhitelist() != null) {
				Set<String> theUserIds = bridgeSettings.getWhitelist().keySet();
				Iterator<String> userIterator = theUserIds.iterator();
				while (userIterator.hasNext()) {
					validUser = userIterator.next();
					if (validUser.equals(aUser))
						found = true;
				}
			}
	
			if (!found && !strict) {
				if (bridgeSettings.getWhitelist() == null) {
					Map<String, WhitelistEntry> awhitelist = new HashMap<>();
					bridgeSettings.setWhitelist(awhitelist);
				}
				bridgeSettings.getWhitelist().put(aUser, WhitelistEntry.createEntry("auto insert user"));
				bridgeSettings.setSettingsChanged(true);
				found = true;
			}
		}

		if (!found) {
			log.debug("Valudate user, No User supplied");
			return HueErrorResponse.createResponse("1", "/api/" + aUser, "unauthorized user", null, null, null).getTheErrors();
		}
		
		return null;
	}

	private Boolean filterByRequester(String requesterFilterList, String anAddress) {
		if (requesterFilterList == null || requesterFilterList.length() == 0)
			return true;

		HashMap<String, String> addressMap;
		addressMap = new HashMap<String, String>();
		if (requesterFilterList.contains(",")) {
			String[] theArray = requesterFilterList.split(",");
			for (String v : theArray) {
				addressMap.put(v.trim(), v.trim());
			}
		} else
			addressMap.put(requesterFilterList.trim(), requesterFilterList.trim());
		if (addressMap.containsKey(anAddress))
			return true;
		return false;
	}

	private Integer getSetCount() {
		return setCount;
	}

	private void setSetCount(Integer setCount) {
		this.setCount = setCount;
	}

	private Integer getTheDelay() {
		return theDelay;
	}

	private void setTheDelay(Integer theDelay) {
		this.theDelay = theDelay;
	}
	private String basicListHandler(String type, String userId, String requestIp) {
		log.debug("hue " + type + " list requested: " + userId + " from " + requestIp);
		HueError[] theErrors = validateWhitelistUser(userId, false);
		if (theErrors != null) 
			return aGsonHandler.toJson(theErrors);

		return "{}";
	}

	private Object groupsListHandler(String groupId, String userId, String requestIp) {
		log.debug("hue group 0 list requested: " + userId + " from " + requestIp);
		HueError[] theErrors = null;
		theErrors = validateWhitelistUser(userId, false);
		if (theErrors == null) {
			if (groupId.equalsIgnoreCase("0")) {
				GroupResponse theResponse = GroupResponse
						.createGroupResponse(repository.findAll());
				return theResponse;
			}
			theErrors = HueErrorResponse.createResponse("3", userId + "/groups/" + groupId, "Object not found", null, null, null).getTheErrors();
		}

		return theErrors;
	}

	private Object lightsListHandler(String type, String userId, String requestIp) {
		HueError[] theErrors = null;
		Map<String, DeviceResponse> deviceResponseMap = null;
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue lights list requested: " + userId + " from " + requestIp);
		log.debug("hue lights list requested: " + userId + " from " + requestIp);
		theErrors = validateWhitelistUser(userId, false);
		if (theErrors == null) {
			List<DeviceDescriptor> deviceList = repository.findAll();
			deviceResponseMap = new HashMap<>();
			for (DeviceDescriptor device : deviceList) {
				DeviceResponse deviceResponse = null;
				String responseString;
				if ((device.getMapType() != null && device.getMapType().equalsIgnoreCase("hueDevice"))) {
					HueDeviceIdentifier deviceId = aGsonHandler.fromJson(device.getOnUrl(), HueDeviceIdentifier.class);
					theErrors = validateHueUser(userId, deviceId.getIpAddress(), device.getName());
					if (theErrors == null) {
						// make call
						responseString = doHttpRequest(
								"http://" + deviceId.getIpAddress() + "/api/" + myHueHome.getTheHUERegisteredUser()
										+ "/lights/" + deviceId.getDeviceId(),
								HttpGet.METHOD_NAME, device.getContentType(), null, null);
						if (responseString == null) {
							log.warn("Error on calling hue device to get state: " + device.getName());
							deviceResponse = DeviceResponse.createResponse(device);
						} else if (responseString.contains("[{\"error\":") && responseString.contains("unauthorized user")) {
								myHueHome.setTheHUERegisteredUser(null);
								theErrors = validateHueUser(userId, deviceId.getIpAddress(), device.getName());
								if (theErrors == null)
									deviceResponse = DeviceResponse.createResponse(device);
						} else {
							deviceResponse = aGsonHandler.fromJson(responseString, DeviceResponse.class);
							if (deviceResponse == null)
								deviceResponse = DeviceResponse.createResponse(device);
						}
					}
				} else
					deviceResponse = DeviceResponse.createResponse(device);
				deviceResponseMap.put(device.getId(), deviceResponse);
			}
		}
		
		if (theErrors != null)
			return theErrors;
		
		return deviceResponseMap;
	}

	private String userAdd(String body, String ipAddress, boolean followingSlash) {
		UserCreateRequest aNewUser = null;
		String newUser = null;
		String aDeviceType = null;

		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue api/ user create requested: " + body + " from " + ipAddress);
		log.debug("hue api user create requested: " + body + " from " + ipAddress);

		if (body != null && !body.isEmpty()) {
			aNewUser = aGsonHandler.fromJson(body, UserCreateRequest.class);
			newUser = aNewUser.getUsername();
			aDeviceType = aNewUser.getDevicetype();
		}
		if (newUser == null)
			newUser = getNewUserID();

		validateWhitelistUser(newUser, false);
		if (aDeviceType == null)
			aDeviceType = "<not given>";
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue api user create requested for device type: " + aDeviceType + " and username: "
					+ newUser + (followingSlash ? " /api/ called" : ""));
		log.debug("hue api user create requested for device type: " + aDeviceType + " and username: " + newUser);

		return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
		
	}
	
	private Object getConfig(String userId, String ipAddress) {
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue api/:userid/config config requested: " + userId + " from " + ipAddress);
		log.debug("hue api config requested: " + userId + " from " + ipAddress);
		if (validateWhitelistUser(userId, true) != null) {
			log.debug("Valudate user, No User supplied, returning public config");
			HuePublicConfig apiResponse = HuePublicConfig.createConfig("Philips hue",
					bridgeSettings.getUpnpConfigAddress(), bridgeSettings.getHubversion());
			return apiResponse;
		}

		HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(),
				bridgeSettings.getWhitelist(), bridgeSettings.getHubversion());

		return apiResponse.getConfig();
	}
	
	private Object getFullState(String userId, String ipAddress) {
		log.debug("hue api full state requested: " + userId + " from " + ipAddress);
		HueError[] theErrors = validateWhitelistUser(userId, false);
		if (theErrors != null)
			return theErrors;

		List<DeviceDescriptor> descriptorList = repository.findAll();
		HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(),
				bridgeSettings.getWhitelist(), bridgeSettings.getHubversion());
		Map<String, DeviceResponse> deviceList = new HashMap<>();
		if (descriptorList != null) {
			descriptorList.forEach(descriptor -> {
				DeviceResponse deviceResponse = DeviceResponse.createResponse(descriptor);
				deviceList.put(descriptor.getId(), deviceResponse);
			});
			apiResponse.setLights(deviceList);
		}

		return apiResponse;
	}
	
	private Object getLight(String userId, String lightId, String ipAddress) {
		log.debug("hue light requested: " + lightId + " for user: " + userId + " from " + ipAddress);
		HueError[] theErrors = validateWhitelistUser(userId, false);
		if (theErrors != null)
			return theErrors;

		DeviceDescriptor device = repository.findOne(lightId);
		if (device == null) {
//			response.status(HttpStatus.SC_NOT_FOUND);
			return HueErrorResponse.createResponse("3", "/api/" + userId + "/lights/" + lightId, "Object not found", null, null, null).getTheErrors();
		} else {
			log.debug("found device named: " + device.getName());
		}
		DeviceResponse lightResponse = null;
		String responseString;
		if ((device.getMapType() != null && device.getMapType().equalsIgnoreCase("hueDevice"))) {
			HueDeviceIdentifier deviceId = aGsonHandler.fromJson(device.getOnUrl(), HueDeviceIdentifier.class);
			theErrors = validateHueUser(userId, deviceId.getIpAddress(), device.getName());
			if (theErrors == null) {
			// make call
			responseString = doHttpRequest("http://" + deviceId.getIpAddress() + "/api/"
					+ myHueHome.getTheHUERegisteredUser() + "/lights/" + deviceId.getDeviceId(),
					HttpGet.METHOD_NAME, device.getContentType(), null, null);
			if (responseString == null) {
				log.warn("Error on calling hue device to get state: " + device.getName());
				lightResponse = DeviceResponse.createResponse(device);
			} else if (responseString.contains("[{\"error\":") && responseString.contains("unauthorized user")) {
				myHueHome.setTheHUERegisteredUser(null);
				theErrors = validateHueUser(userId, deviceId.getIpAddress(), device.getName());
				if (theErrors == null)
					lightResponse = DeviceResponse.createResponse(device);
			} else {
				lightResponse = aGsonHandler.fromJson(responseString, DeviceResponse.class);
				if (lightResponse == null)
					lightResponse = DeviceResponse.createResponse(device);
			}
			}
		} else
			lightResponse = DeviceResponse.createResponse(device);

		if(theErrors != null)
			return theErrors;
		return lightResponse;
		
	}
	
	private String hassDeviceHandler(CallItem anItem, String lightId, int iterationCount, DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc) {
		String theReturn = null;
		log.debug("executing HUE api request to send message to HomeAssistant: " + anItem.getItem().toString());
		if (hassHome != null) {
			HassCommand hassCommand = aGsonHandler.fromJson(anItem.getItem(), HassCommand.class);
			hassCommand.setBri(replaceIntensityValue(hassCommand.getBri(),
					calculateIntensity(state, theStateChanges, stateHasBri, stateHasBriInc), false));
			HomeAssistant homeAssistant = hassHome.getHomeAssistant(hassCommand.getHassName());
			if (homeAssistant == null) {
				log.warn("Should not get here, no HomeAssistants available");
				theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Should not get here, no HiomeAssistant clients available\", \"parameter\": \"/lights/"
						+ lightId + "state\"}}]";
			}
			for (int x = 0; x < this.getSetCount(); x++) {
				if (x > 0 || iterationCount > 0) {
					try {
						Thread.sleep(this.getTheDelay());
					} catch (InterruptedException e) {
						// ignore
					}
				}
				if (anItem.getDelay() != null && anItem.getDelay() > 0)
					this.setTheDelay(anItem.getDelay());
				else
					this.setTheDelay(bridgeSettings.getButtonsleep());
				log.debug("calling HomeAssistant: " + hassCommand.getHassName() + " - "
						+ hassCommand.getEntityId() + " - " + hassCommand.getState() + " - " + hassCommand.getBri()
						+ " - iteration: " + String.valueOf(iterationCount) + " - count: " + String.valueOf(x));
				homeAssistant.callCommand(hassCommand);
			}
		} else {
			log.warn("Should not get here, no HomeAssistant clients configured");
			theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no HomeAssistants configured\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";

		}
		return theReturn;
	}
	
	private HueError[] validateHueUser(String userId, String ipAddress, String aName) {
		String hueUser;
		HueErrorResponse theErrorResp = null;
		if (myHueHome.getTheHUERegisteredUser() == null) {
			hueUser = HueUtil.registerWithHue(httpClient, ipAddress, aName,
					myHueHome.getTheHUERegisteredUser());
			if (hueUser == null) {
				theErrorResp = HueErrorResponse.createResponse("901", "/api/" + userId, "Could not register proxy to other hue hub", null, null, null);
			} else
				myHueHome.setTheHUERegisteredUser(hueUser);
		}
		
		if(theErrorResp != null)
			return theErrorResp.getTheErrors();

		return null;
	}
}
