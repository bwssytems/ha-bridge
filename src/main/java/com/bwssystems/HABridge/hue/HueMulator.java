package com.bwssystems.HABridge.hue;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.HomeManager;
import com.bwssystems.HABridge.User;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.GroupResponse;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.api.hue.HueConfig;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.api.hue.HuePublicConfig;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.dao.*;
import com.bwssystems.HABridge.plugins.hue.HueHome;
import com.bwssystems.HABridge.util.JsonTransformer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.post;
import static spark.Spark.put;

import org.apache.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on Armzilla's HueMulator - a Philips Hue emulator using sparkjava rest server
 */

public class HueMulator {
	private static final Logger log = LoggerFactory.getLogger(HueMulator.class);
	private static final String HUE_CONTEXT = "/api";

	private DeviceRepository repository;
	private HomeManager homeManager;
	private HueHome myHueHome;
	private BridgeSettingsDescriptor bridgeSettings;
	private BridgeSettings bridgeSettingMaster;
	private Gson aGsonHandler;
	private DeviceMapTypes validMapTypes;

	public HueMulator(BridgeSettings bridgeMaster, DeviceRepository aDeviceRepository, HomeManager aHomeManager) {
		repository = aDeviceRepository;
		validMapTypes = new DeviceMapTypes();
		bridgeSettingMaster = bridgeMaster;
		bridgeSettings = bridgeSettingMaster.getBridgeSettingsDescriptor();
		
		homeManager= aHomeManager;
		myHueHome = (HueHome) homeManager.findHome(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex]);
		aGsonHandler = new GsonBuilder().create();
	}

	// This function sets up the sparkjava rest calls for the hue api
	public void setupServer() {
		log.info("Hue emulator service started....");
		before(HUE_CONTEXT + "/*", (request, response) -> {
			log.debug("HueMulator GET called on api/* with request <" + request.pathInfo() + ">");
			if(bridgeSettingMaster.getBridgeSecurity().isSecure()) {
				String pathInfo = request.pathInfo();
				if(pathInfo != null && pathInfo.contains(HUE_CONTEXT + "/devices")) {
					User authUser = bridgeSettingMaster.getBridgeSecurity().getAuthenticatedUser(request);
					if(authUser == null) {
						halt(401, "{\"message\":\"User not authenticated\"}");
					}
				} else if (bridgeSettingMaster.getBridgeSecurity().isSecureHueApi()) {
					User authUser = bridgeSettingMaster.getBridgeSecurity().getAuthenticatedUser(request);
					if(authUser == null) {
						halt(401, "{\"message\":\"User not authenticated\"}");
					}
				}
			}
		});
		// http://ip_address:port/api/{userId}/groups returns json objects of
		// all groups configured
		get(HUE_CONTEXT + "/:userid/groups", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return groupsListHandler(request.params(":userid"),  request.ip());
		} , new JsonTransformer());
		// http://ip_address:port/api/{userId}/groups/{groupId} returns json
		// object for specified group. Only 0 is supported
		get(HUE_CONTEXT + "/:userid/groups/:groupid", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return  groupsIdHandler(request.params(":groupid"), request.params(":userid"),  request.ip());
		} , new JsonTransformer());
		// http://ip_address:port/:userid/groups CORS request
		options(HUE_CONTEXT + "/:userid/groups", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/groups
		// dummy handler
		post(HUE_CONTEXT + "/:userid/groups", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("group add requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/:userid/groups/<groupid>/action
		// Dummy handler
		// Error forces Logitech Pop to fall back to individual light control
		// instead of scene-based control.
		put(HUE_CONTEXT + "/:userid/groups/:groupid/action", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("put action to groups API from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"error\":{\"address\": \"/groups/0/action/scene\", \"type\":7, \"description\": \"invalid value, dummy for parameter, scene\"}}]";
		});		
		// http://ip_address:port/api/{userId}/scenes returns json objects of
		// all scenes configured
		get(HUE_CONTEXT + "/:userid/scenes", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("scenes", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/:userid/scenes CORS request
		options(HUE_CONTEXT + "/:userid/scenes", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/scenes
		// dummy handler
		post(HUE_CONTEXT + "/:userid/scenes", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("scene add requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/schedules returns json objects of
		// all schedules configured
		get(HUE_CONTEXT + "/:userid/schedules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("schedules", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/:userid/schedules CORS request
		options(HUE_CONTEXT + "/:userid/schedules", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/schedules
		// dummy handler
		post(HUE_CONTEXT + "/:userid/schedules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("schedules add requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/sensors returns json objects of
		// all sensors configured
		get(HUE_CONTEXT + "/:userid/sensors", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("sensors", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/:userid/sensors CORS request
		options(HUE_CONTEXT + "/:userid/sensors", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/sensors
		// dummy handler
		post(HUE_CONTEXT + "/:userid/sensors", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("sensors add requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/rules returns json objects of all
		// rules configured
		get(HUE_CONTEXT + "/:userid/rules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("rules", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/:userid/rules CORS request
		options(HUE_CONTEXT + "/:userid/rules", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/rules
		// dummy handler
		post(HUE_CONTEXT + "/:userid/rules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("rules add requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/resourcelinks returns json
		// objects of all resourcelinks configured
		get(HUE_CONTEXT + "/:userid/resourcelinks", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("resourcelinks", request.params(":userid"),  request.ip());
		});
		// http://ip_address:port/:userid/resourcelinks CORS request
		options(HUE_CONTEXT + "/:userid/resourcelinks", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/resourcelinks
		// dummy handler
		post(HUE_CONTEXT + "/:userid/resourcelinks", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("resourcelinks add requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/lights returns json objects of
		// all lights configured
		get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return lightsListHandler(request.params(":userid"), request.ip());
		} , new JsonTransformer());

		// http://ip_address:port/api/{userId}/lights/ returns json objects of
		// all lights configured
		get(HUE_CONTEXT + "/:userid/lights/", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return lightsListHandler(request.params(":userid"), request.ip());
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
		// http://ip_address:port/:userid/config CORS request
		options(HUE_CONTEXT + "/:userid/config", "application/json", (request, response) -> {
			response.status(HttpStatus.SC_OK);
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.header("Access-Control-Allow-Methods", "GET, POST, PUT");
			response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
			response.header("Content-Type", "text/html");
			return "";
		});
		// http://ip_address:port/:userid/config uses json
		// object to set the config. this is to handle swupdates
		put(HUE_CONTEXT + "/:userid/config", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			log.debug("Config change requested from " + request.ip() + " user " + request.params(":userid") + " with body " + request.body());
			HueConfig aConfig = aGsonHandler.fromJson(request.body(), HueConfig.class);
			if(aConfig.getPortalservices() != null) {
				return "[{\"success\":{\"/config/portalservices\":true}}]";
			}
			return "[{\"success\":{\"/config/name\":\"My bridge\"}}]";
		});

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
			return updateState(request.params(":userid"), request.params(":id"), request.body(), request.ip());
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
			return changeState(request.params(":userid"), request.params(":id"), request.body(), request.ip());
		});
	}
	
	private String formatSuccessHueResponse(StateChangeBody stateChanges, String body, String lightId,
			DeviceState deviceState, Integer targetBri, Integer targetBriInc, boolean offState) {

		String responseString = "[";
		boolean notFirstChange = false;
		if (body.contains("\"on\"")) {
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/on\":";
			if (stateChanges.isOn()) {
				responseString = responseString + "true}}";
			} else {
				responseString = responseString + "false}}";
			}
			if (deviceState != null) {
				deviceState.setOn(stateChanges.isOn());
				if(!deviceState.isOn() && deviceState.getBri() == 254)
					deviceState.setBri(0);
				if(!deviceState.isOn() && offState)
					deviceState.setBri(0);
			}
			notFirstChange = true;
		}

		if (body.contains("\"bri\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri\":" + stateChanges.getBri()
					+ "}}";
			if (deviceState != null)
				deviceState.setBri(stateChanges.getBri());
			notFirstChange = true;
		}

		if (body.contains("\"bri_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri_inc\":"
					+ stateChanges.getBri_inc() + "}}";
			// INFO: Bright inc check for deviceState needs to be outside of
			// this method
			if (deviceState != null)
				deviceState.setBri(BrightnessDecode.calculateIntensity(deviceState.getBri(), targetBri, targetBriInc));

			notFirstChange = true;
		}

		if (body.contains("\"ct\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct\":" + stateChanges.getCt()
					+ "}}";
			if (deviceState != null)
				deviceState.setCt(stateChanges.getCt());
			notFirstChange = true;
		}

		if (body.contains("\"xy\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy\":" + stateChanges.getXy()
					+ "}}";
			if (deviceState != null)
				deviceState.setXy(stateChanges.getXy());
			notFirstChange = true;
		}

		if (body.contains("\"hue\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue\":" + stateChanges.getHue()
					+ "}}";
			if (deviceState != null)
				deviceState.setHue(stateChanges.getHue());
			notFirstChange = true;
		}

		if (body.contains("\"sat\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat\":" + stateChanges.getSat()
					+ "}}";
			if (deviceState != null)
				deviceState.setSat(stateChanges.getSat());
			notFirstChange = true;
		}

		if (body.contains("\"ct_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct_inc\":"
					+ stateChanges.getCt_inc() + "}}";
			if (deviceState != null)
				deviceState.setCt(deviceState.getCt() + stateChanges.getCt_inc());
			notFirstChange = true;
		}

		if (body.contains("\"xy_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy_inc\":"
					+ stateChanges.getXy_inc() + "}}";
			if (deviceState != null)
				deviceState.setXy(stateChanges.getXy());
			notFirstChange = true;
		}

		if (body.contains("\"hue_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue_inc\":"
					+ stateChanges.getHue_inc() + "}}";
			if (deviceState != null)
				deviceState.setHue(deviceState.getHue() + stateChanges.getHue_inc());
			notFirstChange = true;
		}

		if (body.contains("\"sat_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat_inc\":"
					+ stateChanges.getSat_inc() + "}}";
			if (deviceState != null)
				deviceState.setSat(deviceState.getSat() + stateChanges.getSat_inc());
			notFirstChange = true;
		}

		if (body.contains("\"effect\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/effect\":"
					+ stateChanges.getEffect() + "}}";
			if (deviceState != null)
				deviceState.setEffect(stateChanges.getEffect());
			notFirstChange = true;
		}

		if (body.contains("\"transitiontime\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/transitiontime\":"
					+ stateChanges.getTransitiontime() + "}}";
			// if(deviceState != null)
			// deviceState.setTransitiontime(state.getTransitiontime());
			notFirstChange = true;
		}

		if (body.contains("\"alert\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/alert\":"
					+ stateChanges.getAlert() + "}}";
			if (deviceState != null)
				deviceState.setAlert(stateChanges.getAlert());
			notFirstChange = true;
		}

		if(deviceState.isOn() && deviceState.getBri() <= 0)
			deviceState.setBri(254);
		
		if(!deviceState.isOn() && (targetBri != null || targetBriInc != null))
			deviceState.setOn(true);

		responseString = responseString + "]";

		return responseString;
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

	private String basicListHandler(String type, String userId, String requestIp) {
		log.debug("hue " + type + " list requested: " + userId + " from " + requestIp);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null) {
			if(bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			return aGsonHandler.toJson(theErrors);
		}

		return "{}";
	}
	private Object groupsListHandler(String userId, String requestIp) {
		log.debug("hue group list requested: " + userId + " from " + requestIp);
		HueError[] theErrors = null;
		Map<String, GroupResponse> groupResponseMap = null;
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if(bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			groupResponseMap = new HashMap<String, GroupResponse>();
			groupResponseMap.put("1", (GroupResponse) this.groupsIdHandler("1", userId, requestIp));
			return groupResponseMap;
		}

		return theErrors;
	}


	private Object groupsIdHandler(String groupId, String userId, String requestIp) {
		log.debug("hue group id: <" + groupId + "> requested: " + userId + " from " + requestIp);
		HueError[] theErrors = null;
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if(bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			if (groupId.equalsIgnoreCase("0")) {
				GroupResponse theResponse = GroupResponse.createDefaultGroupResponse(repository.findActive());
				return theResponse;
			}
			if (!groupId.equalsIgnoreCase("0")) {
				GroupResponse theResponse = GroupResponse.createOtherGroupResponse(repository.findActive());
				return theResponse;
			}
			theErrors = HueErrorResponse.createResponse("3", userId + "/groups/" + groupId, "Object not found", null, null, null).getTheErrors();
		}

		return theErrors;
	}

	private Object lightsListHandler(String userId, String requestIp) {
		HueError[] theErrors = null;
		Map<String, DeviceResponse> deviceResponseMap = null;
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue lights list requested: " + userId + " from " + requestIp);
		log.debug("hue lights list requested: " + userId + " from " + requestIp);
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if(bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

	        List<DeviceDescriptor> deviceList = repository.findAllByRequester(requestIp);
//			List<DeviceDescriptor> deviceList = repository.findActive();
			deviceResponseMap = new HashMap<String, DeviceResponse>();
			for (DeviceDescriptor device : deviceList) {
				DeviceResponse deviceResponse = null;
				if(!device.isInactive()) {
					if (device.containsType(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
						CallItem[] callItems = null;
						try {
							if(device.getOnUrl() != null)
								callItems = aGsonHandler.fromJson(device.getOnUrl(), CallItem[].class);
						} catch(JsonSyntaxException e) {
							log.warn("Could not decode Json for url items to get Hue state for device: " + device.getName());
							callItems = null;
						}
						
						for (int i = 0; callItems != null && i < callItems.length; i++) {
							if((callItems[i].getType() != null && callItems[i].getType().equals(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) ||
										(callItems[i].getItem().getAsString().contains("hueName"))) {
								deviceResponse = myHueHome.getHueDeviceInfo(callItems[i], device);
								i = callItems.length;
							}
						}
					}
	
					if (deviceResponse == null)
						deviceResponse = DeviceResponse.createResponse(device);
					deviceResponseMap.put(device.getId(), deviceResponse);
				}
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
		boolean toContinue = false;

		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue api user create requested: " + body + " from " + ipAddress);
		
		if(bridgeSettingMaster.getBridgeSecurity().isUseLinkButton() && bridgeSettingMaster.getBridgeControl().isLinkButton())
			toContinue = true;
		else if(!bridgeSettingMaster.getBridgeSecurity().isUseLinkButton())
			toContinue = true;
		
		if(toContinue) {
			log.debug("hue api user create requested: " + body + " from " + ipAddress);
	
			if (body != null && !body.isEmpty()) {
				try {
					aNewUser = aGsonHandler.fromJson(body, UserCreateRequest.class);
				} catch (Exception e) {
					log.warn("Could not add user. Request garbled: " + body);
					return aGsonHandler.toJson(HueErrorResponse.createResponse("2", "/",
							"Could not add user.", null, null, null).getTheErrors(), HueError[].class);				
				}
				newUser = aNewUser.getUsername();
				aDeviceType = aNewUser.getDevicetype();
			}
	
			if (aDeviceType == null)
				aDeviceType = "<not given>";
	
			if (newUser == null) {
				newUser = bridgeSettingMaster.getBridgeSecurity().createWhitelistUser(aDeviceType);
			}
			else {
				bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(newUser, aDeviceType, false);
			}
	
			if(bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();
	
			if (bridgeSettings.isTraceupnp())
				log.info("Traceupnp: hue api user create requested for device type: " + aDeviceType + " and username: "
						+ newUser + (followingSlash ? " /api/ called" : ""));
			log.debug("hue api user create requested for device type: " + aDeviceType + " and username: " + newUser + (followingSlash ? " /api/ called" : ""));
			return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
		}
		return aGsonHandler.toJson(HueErrorResponse.createResponse("1", "/api/", "unauthorized user", null, null, null).getTheErrors());
	}
	
	private Object getConfig(String userId, String ipAddress) {
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue api/:userid/config config requested: " + userId + " from " + ipAddress);
		log.debug("hue api config requested: " + userId + " from " + ipAddress);
		if (bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton()) != null) {
			log.debug("hue api config requested, No User supplied, returning public config");
			HuePublicConfig apiResponse = HuePublicConfig.createConfig("Philips hue",
					bridgeSettings.getUpnpConfigAddress(), bridgeSettings.getHubversion());
			return apiResponse;
		}

		HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(),
				bridgeSettings.getWhitelist(), bridgeSettings.getHubversion(), bridgeSettingMaster.getBridgeControl().isLinkButton());
		log.debug("api response config <<<" + aGsonHandler.toJson(apiResponse.getConfig()) + ">>>");
		return apiResponse.getConfig();
	}
	
	@SuppressWarnings("unchecked")
	private Object getFullState(String userId, String ipAddress) {
		log.debug("hue api full state requested: " + userId + " from " + ipAddress);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null)
			return theErrors;

		HueApiResponse apiResponse = new HueApiResponse("Philips hue", bridgeSettings.getUpnpConfigAddress(),
				bridgeSettings.getWhitelist(), bridgeSettings.getHubversion(), bridgeSettingMaster.getBridgeControl().isLinkButton());
		apiResponse.setLights((Map<String, DeviceResponse>) this.lightsListHandler(userId, ipAddress));
		apiResponse.setGroups((Map<String, GroupResponse>) this.groupsListHandler(userId, ipAddress));

		return apiResponse;
	}
	
	private Object getLight(String userId, String lightId, String ipAddress) {
		log.debug("hue light requested: " + lightId + " for user: " + userId + " from " + ipAddress);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
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
		if (device.containsType(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
			CallItem[] callItems = null;
			try {
				if(device.getOnUrl() != null)
					callItems = aGsonHandler.fromJson(device.getOnUrl(), CallItem[].class);
			} catch(JsonSyntaxException e) {
				log.warn("Could not decode Json for url items to get Hue state for device: " + device.getName());
				callItems = null;
			}
			
			for (int i = 0; callItems != null && i < callItems.length; i++) {
				if((callItems[i].getType() != null && callItems[i].getType().equals(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) || callItems[i].getItem().getAsString().startsWith("{\"ipAddress\":\"")) {
					lightResponse = myHueHome.getHueDeviceInfo(callItems[i], device);
					i = callItems.length;
				}
			}
		}

		if (lightResponse == null)
			lightResponse = DeviceResponse.createResponse(device);

		return lightResponse;
		
	}

	private String updateState(String userId, String lightId, String body, String ipAddress) {
		String responseString = null;
		StateChangeBody theStateChanges = null;
		DeviceState state = null;
		Integer targetBri = null;
		Integer targetBriInc = null;
		log.debug("Update state requested: " + userId + " from " + ipAddress + " body: " + body);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null)
			return aGsonHandler.toJson(theErrors);
		try {
			theStateChanges = aGsonHandler.fromJson(body, StateChangeBody.class);
		} catch (Exception e) {
			theStateChanges = null;
		}
		if (theStateChanges == null) {
			log.warn("Could not parse state change body. Light state not changed.");
			return aGsonHandler.toJson(HueErrorResponse.createResponse("2", "/lights/" + lightId,
					"Could not parse state change body.", null, null, null).getTheErrors(), HueError[].class);
		}

		DeviceDescriptor device = repository.findOne(lightId);
		if (device == null) {
			log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from "
					+ ipAddress + " body: " + body);
			return aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
					"Could not find device.", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
		}

		if (body.contains("\"bri_inc\""))
			targetBriInc = new Integer(theStateChanges.getBri_inc());
		else if (body.contains("\"bri\"")) {
			targetBri = new Integer(theStateChanges.getBri());
		}

		state = device.getDeviceState();
		if (state == null)
			state = DeviceState.createDeviceState();

		responseString = this.formatSuccessHueResponse(theStateChanges, body, lightId, state, targetBri, targetBriInc, device.isOffState());
		device.setDeviceState(state);

		return responseString;
	}

	private String changeState(String userId, String lightId, String body, String ipAddress) {
		String responseString = null;
		String url = null;
		StateChangeBody theStateChanges = null;
		DeviceState state = null;
		Integer targetBri = null;
		Integer targetBriInc = null;
		MultiCommandUtil aMultiUtil = new MultiCommandUtil();
		aMultiUtil.setTheDelay(bridgeSettings.getButtonsleep());
		aMultiUtil.setDelayDefault(bridgeSettings.getButtonsleep());
		aMultiUtil.setSetCount(1);
		log.debug("hue state change requested: " + userId + " from " + ipAddress + " body: " + body);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null, bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null)
			return aGsonHandler.toJson(theErrors);
		try {
			theStateChanges = aGsonHandler.fromJson(body, StateChangeBody.class);
		} catch (Exception e) {
			theStateChanges = null;
		}
		if (theStateChanges == null) {
			log.warn("Could not parse state change body. Light state not changed.");
			return aGsonHandler.toJson(HueErrorResponse.createResponse("2", "/lights/" + lightId,
					"Could not parse state change body.", null, null, null).getTheErrors(), HueError[].class);
		}

		DeviceDescriptor device = repository.findOne(lightId);
		if (device == null) {
			log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from "
					+ ipAddress + " body: " + body);
			return aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
					"Could not find device.", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
		}

		if (body.contains("\"bri_inc\"")) {
			targetBriInc = new Integer(theStateChanges.getBri_inc());
		}
		else if (body.contains("\"bri\"")) {
			targetBri = new Integer(theStateChanges.getBri());
		}

		state = device.getDeviceState();
		if (state == null) {
			state = DeviceState.createDeviceState();
			device.setDeviceState(state);
		}

		if (targetBri != null || targetBriInc != null) {
			url = device.getDimUrl();

			if (url == null || url.length() == 0)
				url = device.getOnUrl();
		} else {
			if (theStateChanges.isOn()) {
				url = device.getOnUrl();
			} else if (!theStateChanges.isOn()) {
				url = device.getOffUrl();
			}
		}

		// code for backwards compatibility
		if(device.getMapType() != null && device.getMapType().equalsIgnoreCase(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
			if(url == null)
				url = device.getOnUrl();
		}
		if (url != null && !url.equals("")) {
			if (!url.startsWith("[")) {
				if (url.startsWith("{\"item"))
					url = "[" + url + "]";
				else {
					if(url.startsWith("{"))
						url = "[{\"item\":" + url + "}]";
					else
						url = "[{\"item\":\"" + url + "\"}]";
				}
			} else if(!url.startsWith("[{\"item\""))
				url = "[{\"item\":" + url + "}]";

			log.debug("Decode Json for url items: " + url);
			CallItem[] callItems = null;
			try {
				callItems = aGsonHandler.fromJson(url, CallItem[].class);
			} catch(JsonSyntaxException e) {
				log.warn("Could not decode Json for url items: " + lightId + " for hue state change request: " + userId + " from "
						+ ipAddress + " body: " + body + " url items: " + url);
				return aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
						"Could decode json in request", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
			}
			
			for (int i = 0; callItems != null && i < callItems.length; i++) {
				if(!filterByRequester(device.getRequesterAddress(), ipAddress) || !filterByRequester(callItems[i].getFilterIPs(), ipAddress)) {
					log.warn("filter for requester address not present in: (device)" + device.getRequesterAddress() + " OR then (item)" + callItems[i].getFilterIPs() + " with request ip of: " + ipAddress);
					continue;
				}
				if (callItems[i].getCount() != null && callItems[i].getCount() > 0)
					aMultiUtil.setSetCount(callItems[i].getCount());
				else
					aMultiUtil.setSetCount(1);
				// code for backwards compatibility
				if((callItems[i].getType() == null || callItems[i].getType().trim().isEmpty())) {
					if(validMapTypes.validateType(device.getMapType()))
						callItems[i].setType(device.getMapType().trim());
					else if(validMapTypes.validateType(device.getDeviceType()))
						callItems[i].setType(device.getDeviceType().trim());
					else
						callItems[i].setType(DeviceMapTypes.CUSTOM_DEVICE[DeviceMapTypes.typeIndex]);
				}
	
				if (callItems[i].getType() != null) {
					for (int x = 0; x < aMultiUtil.getSetCount(); x++) {
						if (x > 0 || i > 0) {
							try {
								Thread.sleep(aMultiUtil.getTheDelay());
							} catch (InterruptedException e) {
								// ignore
							}
						}
						if (callItems[i].getDelay() != null && callItems[i].getDelay() > 0)
							aMultiUtil.setTheDelay(callItems[i].getDelay());
						else
							aMultiUtil.setTheDelay(aMultiUtil.getDelayDefault());
						responseString = homeManager.findHome(callItems[i].getType().trim()).deviceHandler(callItems[i], aMultiUtil, lightId, state.getBri(), targetBri, targetBriInc, device, body);
						if(responseString != null && responseString.contains("{\"error\":")) {
							x = aMultiUtil.getSetCount();
						}
					}
				}
			}
		} else {
			log.warn("Could not find url: " + lightId + " for hue state change request: " + userId + " from "
					+ ipAddress + " body: " + body);
			responseString =  aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
					"Could not find url.", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
		}

		if (responseString == null || !responseString.contains("[{\"error\":")) {
			if(!device.isNoState()) {
				responseString = this.formatSuccessHueResponse(theStateChanges, body, lightId, state, targetBri, targetBriInc, device.isOffState());
				device.setDeviceState(state);
			} else {
				DeviceState dummyState = DeviceState.createDeviceState();
				responseString = this.formatSuccessHueResponse(theStateChanges, body, lightId, dummyState, targetBri, targetBriInc, device.isOffState());
			}
		}
		return responseString;
		
	}
}
