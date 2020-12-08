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
import com.bwssystems.HABridge.api.hue.GroupClassTypes;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.api.hue.HueConfig;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.api.hue.HuePublicConfig;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.dao.*;
import com.bwssystems.HABridge.hue.ColorData;
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
import static spark.Spark.delete;

import org.apache.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.util.Arrays;

/**
 * Based on Armzilla's HueMulator - a Philips Hue emulator using sparkjava rest
 * server
 */

public class HueMulator {
	private static final Logger log = LoggerFactory.getLogger(HueMulator.class);
	private static final String HUE_CONTEXT = "/api";

	private DeviceRepository repository;
	private GroupRepository groupRepository;
	private HomeManager homeManager;
	private HueHome myHueHome;
	private BridgeSettingsDescriptor bridgeSettings;
	private BridgeSettings bridgeSettingMaster;
	private Gson aGsonHandler;
	private DeviceMapTypes validMapTypes;

	public HueMulator(BridgeSettings bridgeMaster, DeviceRepository aDeviceRepository, GroupRepository aGroupRepository,
			HomeManager aHomeManager) {
		repository = aDeviceRepository;
		groupRepository = aGroupRepository;
		validMapTypes = new DeviceMapTypes();
		bridgeSettingMaster = bridgeMaster;
		bridgeSettings = bridgeSettingMaster.getBridgeSettingsDescriptor();

		homeManager = aHomeManager;
		myHueHome = (HueHome) homeManager.findHome(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex]);
		aGsonHandler = new GsonBuilder().create();
	}

	// This function sets up the sparkjava rest calls for the hue api
	public void setupServer() {
		log.info("Hue emulator service started....");
		startupDeviceCall();
		before(HUE_CONTEXT + "/*", (request, response) -> {
			// This currently causes an error with Spark replies
			// String path = request.pathInfo();
			// if (path.endsWith("/")) { // it should work with or without a trailing slash
			// response.redirect(path.substring(0, path.length() - 1));
			// }
			log.debug("HueMulator " + request.requestMethod() + " called on api/* with request <<<" + request.pathInfo()
					+ ">>>, and body <<<" + request.body() + ">>>");
			if (bridgeSettingMaster.getBridgeSecurity().isSecure()) {
				String pathInfo = request.pathInfo();
				if (pathInfo != null && pathInfo.contains(HUE_CONTEXT + "/devices")) {
					User authUser = bridgeSettingMaster.getBridgeSecurity().getAuthenticatedUser(request);
					if (authUser == null) {
						halt(401, "{\"message\":\"User not authenticated\"}");
					}
				} else if (bridgeSettingMaster.getBridgeSecurity().isSecureHueApi()) {
					User authUser = bridgeSettingMaster.getBridgeSecurity().getAuthenticatedUser(request);
					if (authUser == null) {
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
			return groupsListHandler(request.params(":userid"), request.ip());
		}, new JsonTransformer());
		// http://ip_address:port/api/{userId}/groups/{groupId} returns json
		// object for specified group.
		get(HUE_CONTEXT + "/:userid/groups/:groupid", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return groupsIdHandler(request.params(":groupid"), request.params(":userid"), request.ip());
		}, new JsonTransformer());
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
		// add a group
		post(HUE_CONTEXT + "/:userid/groups", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return addGroup(request.params(":userid"), request.ip(), request.body());
		});
		// http://ip_address:port/api/:userid/groups/<groupid>
		// delete a group
		delete(HUE_CONTEXT + "/:userid/groups/:groupid", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return deleteGroup(request.params(":userid"), request.params(":groupid"), request.ip());
		});
		// http://ip_address:port/api/:userid/groups/<groupid>
		// modify a single group
		put(HUE_CONTEXT + "/:userid/groups/:groupid", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return modifyGroup(request.params(":userid"), request.params(":groupid"), request.ip(), request.body());
		});
		// http://ip_address:port/api/:userid/groups/<groupid>/action
		// group acions
		put(HUE_CONTEXT + "/:userid/groups/:groupid/action", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return changeGroupState(request.params(":userid"), request.params(":groupid"), request.body(), request.ip(),
					false);
		});
		// http://ip_address:port/api/{userId}/scenes returns json objects of
		// all scenes configured
		get(HUE_CONTEXT + "/:userid/scenes", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("scenes", request.params(":userid"), request.ip());
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
			log.debug("scene add requested from " + request.ip() + " user " + request.params(":userid") + " with body "
					+ request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/schedules returns json objects of
		// all schedules configured
		get(HUE_CONTEXT + "/:userid/schedules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("schedules", request.params(":userid"), request.ip());
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
			log.debug("schedules add requested from " + request.ip() + " user " + request.params(":userid")
					+ " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/sensors returns json objects of
		// all sensors configured
		get(HUE_CONTEXT + "/:userid/sensors", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("sensors", request.params(":userid"), request.ip());
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
			log.debug("sensors add requested from " + request.ip() + " user " + request.params(":userid")
					+ " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/rules returns json objects of all
		// rules configured
		get(HUE_CONTEXT + "/:userid/rules", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("rules", request.params(":userid"), request.ip());
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
			log.debug("rules add requested from " + request.ip() + " user " + request.params(":userid") + " with body "
					+ request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/resourcelinks returns json
		// objects of all resourcelinks configured
		get(HUE_CONTEXT + "/:userid/resourcelinks", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return basicListHandler("resourcelinks", request.params(":userid"), request.ip());
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
			log.debug("resourcelinks add requested from " + request.ip() + " user " + request.params(":userid")
					+ " with body " + request.body());
			return "[{\"success\":{\"id\":\"1\"}}]";
		});
		// http://ip_address:port/api/{userId}/lights returns json objects of
		// all lights configured
		get(HUE_CONTEXT + "/:userid/lights", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return lightsListHandler(request.params(":userid"), request.ip());
		}, new JsonTransformer());

		// http://ip_address:port/api/{userId}/lights/ returns json objects of
		// all lights configured
		get(HUE_CONTEXT + "/:userid/lights/", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return lightsListHandler(request.params(":userid"), request.ip());
		}, new JsonTransformer());

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
		}, new JsonTransformer());

		// http://ip_address:port/api/{userId}/config returns json objects for
		// the config
		get(HUE_CONTEXT + "/:userid/config", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getConfig(request.params(":userid"), request.ip());
		}, new JsonTransformer());
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
			log.debug("Config change requested from " + request.ip() + " user " + request.params(":userid")
					+ " with body " + request.body());
			HueConfig aConfig = aGsonHandler.fromJson(request.body(), HueConfig.class);
			if (aConfig.getPortalservices() != null) {
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
		}, new JsonTransformer());

		// http://ip_address:port/api/{userId}/ returns json objects for the full
		// state
		get(HUE_CONTEXT + "/:userid/", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getFullState(request.params(":userid"), request.ip());
		}, new JsonTransformer());

		// http://ip_address:port/api/{userId}/lights/{lightId} returns json
		// object for a given light
		get(HUE_CONTEXT + "/:userid/lights/:id", "application/json", (request, response) -> {
			response.header("Access-Control-Allow-Origin", request.headers("Origin"));
			response.type("application/json");
			response.status(HttpStatus.SC_OK);
			return getLight(request.params(":userid"), request.params(":id"), request.ip());
		}, new JsonTransformer());

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
			return changeState(request.params(":userid"), request.params(":id"), request.body(), request.ip(), false);
		});
	}

	@SuppressWarnings("unchecked")
	private String formatSuccessHueResponse(StateChangeBody stateChanges, String body, String lightId,
			DeviceState deviceState, Integer targetBri, Integer targetBriInc, ColorData colorData, boolean offState) {

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
				if (!deviceState.isOn() && deviceState.getBri() == 254)
					deviceState.setBri(1);
				if (!deviceState.isOn() && offState)
					deviceState.setBri(1);
			}
			notFirstChange = true;
		}

		if (body.contains("\"bri\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/bri\":"
					+ stateChanges.getBri() + "}}";
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

		if (body.contains("\"xy\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy\":"
					+ stateChanges.getXy() + "}}";
			if (deviceState != null)
				deviceState.setXy(stateChanges.getXy());
			notFirstChange = true;
		} else if (body.contains("\"ct\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct\":"
					+ stateChanges.getCt() + "}}";
			if (deviceState != null)
				deviceState.setCt(stateChanges.getCt());
			notFirstChange = true;
		} else {
			if (body.contains("\"hue\"")) {
				if (notFirstChange)
					responseString = responseString + ",";
				responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/hue\":"
						+ stateChanges.getHue() + "}}";
				if (deviceState != null)
					deviceState.setHue(stateChanges.getHue());
				notFirstChange = true;
			}

			if (body.contains("\"sat\"")) {
				if (notFirstChange)
					responseString = responseString + ",";
				responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/sat\":"
						+ stateChanges.getSat() + "}}";
				if (deviceState != null)
					deviceState.setSat(stateChanges.getSat());
				notFirstChange = true;
			}
		}

		if (body.contains("\"xy_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/xy_inc\":"
					+ stateChanges.getXy_inc() + "}}";
			if (deviceState != null)
				deviceState.setXy((List<Double>) colorData.getData());
			notFirstChange = true;
		} else if (body.contains("\"ct_inc\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/ct_inc\":"
					+ stateChanges.getCt_inc() + "}}";
			if (deviceState != null)
				deviceState.setCt(deviceState.getCt() + stateChanges.getCt_inc());
			notFirstChange = true;
		} else {
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
		}

		if (body.contains("\"effect\"")) {
			if (notFirstChange)
				responseString = responseString + ",";
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/effect\":\""
					+ stateChanges.getEffect() + "\"}}";
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
			responseString = responseString + "{\"success\":{\"/lights/" + lightId + "/state/alert\":\""
					+ stateChanges.getAlert() + "\"}}";
			if (deviceState != null)
				deviceState.setAlert(stateChanges.getAlert());
			notFirstChange = true;
		}

		if ((deviceState != null) && deviceState.isOn() && deviceState.getBri() <= 1)
			deviceState.setBri(254);

		// if((deviceState != null) && !deviceState.isOn() && (targetBri != null ||
		// targetBriInc != null))
		// deviceState.setOn(true);

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
		log.debug("hue " + type + " list requested by user: " + userId + " from address: " + requestIp);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			return aGsonHandler.toJson(theErrors);
		}

		return "{}";
	}

	private Object addGroup(String userId, String ip, String body) {
		HueError[] theErrors = null;
		log.debug("group add requested from " + ip + " user " + userId + " with body " + body);
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			GroupResponse theGroup = null;
			try {
				theGroup = aGsonHandler.fromJson(body, GroupResponse.class);
			} catch (Exception e) {
				theGroup = null;
			}
			if (theGroup == null) {
				log.warn("Could not parse add group body. No group created.");
				return aGsonHandler.toJson(HueErrorResponse
						.createResponse("5", "/groups/lights", "invalid/missing parameters in body", null, null, null)
						.getTheErrors(), HueError[].class);
			}

			List<GroupDescriptor> groups = groupRepository.findAll();
			GroupDescriptor newGroup = new GroupDescriptor();

			String type = theGroup.getType();
			String groupClass = theGroup.getClass_name();

			// check type
			if (type == null || type.trim().equals("")) {
				type = (groupClass == null || groupClass.trim().equals("")) ? "LightGroup" : "Room";
			} else if (!type.equals("LightGroup") && !type.equals("Room")) {
				type = "LightGroup";
			}
			// Everything else than a room must contain lights
			if (!type.equals("Room")) {
				if (theGroup.getLights() == null || theGroup.getLights().length == 0) {
					return aGsonHandler.toJson(HueErrorResponse.createResponse("5", "/groups/lights",
							"invalid/missing parameters in body", null, null, null).getTheErrors(), HueError[].class);
				}
			} else { // check room class if it's a room
				if (groupClass == null || groupClass.trim().equals("")) {
					groupClass = GroupClassTypes.OTHER;
				} else if (!new GroupClassTypes().validateType(groupClass)) {
					return aGsonHandler.toJson(HueErrorResponse
							.createResponse("7", "/groups/class",
									"invalid value, " + groupClass + ", for parameter, class", null, null, null)
							.getTheErrors(), HueError[].class);
				}
			}
			String name = theGroup.getName();
			Integer newId = groupRepository.getNewId();
			if (name == null || name.trim().equals("")) {
				name = type + " " + newId;
			}
			newGroup.setGroupType(type);
			newGroup.setGroupClass(groupClass);
			newGroup.setName(name);
			newGroup.setLights(theGroup.getLights());
			groups.add(newGroup);
			groupRepository.save(groups.toArray(new GroupDescriptor[0]));

			return "[{\"success\":{\"id\":\"" + newId + "\"}}]";
		}

		return theErrors;
	}

	private Object deleteGroup(String userId, String groupId, String ip) {
		HueError[] theErrors = null;
		log.debug("group delete requested from " + ip + " user " + userId);
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			GroupDescriptor group = groupRepository.findOne(groupId);
			if (group == null || group.isInactive()) {
				return aGsonHandler
						.toJson(HueErrorResponse
								.createResponse("3", "/groups/" + groupId,
										"resource, /groups/" + groupId + ", not available", null, null, null)
								.getTheErrors(), HueError[].class);
			} else {
				groupRepository.delete(group);
				return "[{\"success\":\"/groups/" + groupId + " deleted\"}}]";
			}
		}
		return theErrors;
	}

	private Object modifyGroup(String userId, String groupId, String ip, String body) {
		HueError[] theErrors = null;
		log.debug("group modify requested from " + ip + " user " + userId + " with body " + body);
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			GroupDescriptor group = groupRepository.findOne(groupId);
			if (group == null || group.isInactive()) {
				return aGsonHandler
						.toJson(HueErrorResponse
								.createResponse("3", "/groups/" + groupId,
										"resource, /groups/" + groupId + ", not available", null, null, null)
								.getTheErrors(), HueError[].class);
			} else {
				String successString = "[";
				GroupResponse theGroup = null;
				try {
					theGroup = aGsonHandler.fromJson(body, GroupResponse.class);
				} catch (Exception e) {
					theGroup = null;
				}
				if (theGroup == null) {
					log.warn("Could not parse modify group body. Group unchanged.");
					return aGsonHandler.toJson(HueErrorResponse.createResponse("5", "/groups/lights",
							"invalid/missing parameters in body", null, null, null).getTheErrors(), HueError[].class);
				}

				String groupClass = theGroup.getClass_name();
				String name = theGroup.getName();
				if (!(name == null || name.trim().equals(""))) {
					group.setName(name);
					successString += "{\"success\":{\"/groups/" + groupId + "/name\":\"" + name + "\"}},";
				}
				if (!group.getGroupType().equals("Room")) {
					if (!(groupClass == null || groupClass.trim().equals(""))) {
						return aGsonHandler.toJson(HueErrorResponse
								.createResponse("6", "/groups/" + groupId + "/class",
										"parameter, /groups/" + groupId + "/class, not available", null, null, null)
								.getTheErrors(), HueError[].class);
					}
					if (theGroup.getLights() != null) {
						if (theGroup.getLights().length == 0) {
							return aGsonHandler
									.toJson(HueErrorResponse.createResponse("7", "/groups/" + groupId + "/lights",
											"invalid value, " + Arrays.toString(theGroup.getLights())
													+ ", for parameter, /groups" + groupId + "/lights",
											null, null, null).getTheErrors(), HueError[].class);
						} else {
							group.setLights(theGroup.getLights());
							successString += "{\"success\":{\"/groups/" + groupId + "/lights\":\""
									+ Arrays.toString(theGroup.getLights()) + "\"}},";
						}
					}
				} else { // check room class if it's a room
					if (!(groupClass == null || groupClass.trim().equals(""))) {
						if (!new GroupClassTypes().validateType(groupClass)) {
							return aGsonHandler.toJson(HueErrorResponse
									.createResponse("7", "/groups/class",
											"invalid value, " + groupClass + ", for parameter, class", null, null, null)
									.getTheErrors(), HueError[].class);
						} else {
							group.setGroupClass(groupClass);
							successString += "{\"success\":{\"/groups/" + groupId + "/class\":\"" + groupClass
									+ "\"}},";
						}
					}

					if (theGroup.getLights() != null) {
						group.setLights(theGroup.getLights());
						successString += "{\"success\":{\"/groups/" + groupId + "/lights\":\""
								+ Arrays.toString(theGroup.getLights()) + "\"}},";
					}
				}

				groupRepository.save();
				return (successString.length() == 1) ? "[]"
						: successString.substring(0, successString.length() - 1) + "]";
			}
		}
		return theErrors;
	}

	private Object groupsListHandler(String userId, String requestIp) {
		HueError[] theErrors = null;
		Map<String, GroupResponse> groupResponseMap = null;
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue group list requested: " + userId + " from " + requestIp);
		log.debug("hue group list requested: " + userId + " from " + requestIp);
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			List<GroupDescriptor> groupList = groupRepository.findAllByRequester(requestIp);
			groupResponseMap = new HashMap<String, GroupResponse>();
			for (GroupDescriptor group : groupList) {
				GroupResponse groupResponse = null;
				if (!group.isInactive()) {
					Map<String, DeviceResponse> lights = repository.findAllByGroupWithState(group.getLights(),
							requestIp, myHueHome, aGsonHandler);
					groupResponse = GroupResponse.createResponse(group, lights);
					groupResponseMap.put(group.getId(), groupResponse);
				}
			}
		}

		if (theErrors != null)
			return theErrors;

		return groupResponseMap;
	}

	private Object groupsIdHandler(String groupId, String userId, String requestIp) {
		log.debug("hue group id: <" + groupId + "> requested: " + userId + " from " + requestIp);
		HueError[] theErrors = null;
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			if (groupId.equalsIgnoreCase("0")) {
				@SuppressWarnings("unchecked")
				GroupResponse theResponse = GroupResponse
						.createDefaultGroupResponse((Map<String, DeviceResponse>) lightsListHandler(userId, requestIp));
				return theResponse;
			} else {
				GroupDescriptor group = groupRepository.findOne(groupId);
				if (group == null || group.isInactive()) {
					return aGsonHandler.toJson(HueErrorResponse
							.createResponse("3", "/groups/" + groupId,
									"resource, /groups/" + groupId + ", not available", null, null, null)
							.getTheErrors(), HueError[].class);
				} else {
					Map<String, DeviceResponse> lights = repository.findAllByGroupWithState(group.getLights(),
							requestIp, myHueHome, aGsonHandler);
					GroupResponse theResponse = GroupResponse.createResponse(group, lights);
					return theResponse;
				}

			}
		}

		return theErrors;
	}

	private Object lightsListHandler(String userId, String requestIp) {
		HueError[] theErrors = null;
		Map<String, DeviceResponse> deviceResponseMap = null;
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue lights list requested by user: " + userId + " from address: " + requestIp);
		log.debug("hue lights list requested: " + userId + " from " + requestIp);
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			List<DeviceDescriptor> deviceList = repository.findAllByRequester(requestIp);
			deviceResponseMap = new HashMap<String, DeviceResponse>();
			for (DeviceDescriptor device : deviceList) {
				DeviceResponse deviceResponse = null;
				if (!device.isInactive()) {
					if (device.containsType(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
						CallItem[] callItems = null;
						try {
							if (device.getOnUrl() != null)
								callItems = aGsonHandler.fromJson(device.getOnUrl(), CallItem[].class);
						} catch (JsonSyntaxException e) {
							log.warn("Could not decode Json for url items to get Hue state for device: "
									+ device.getName());
							callItems = null;
						}

						for (int i = 0; callItems != null && i < callItems.length; i++) {
							if ((callItems[i].getType() != null && callItems[i].getType()
									.equals(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex]))
									|| (callItems[i].getItem().getAsString().contains("hueName"))) {
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

			// handle groups which shall be exposed as fake lights to selected devices like
			// amazon echos
			List<GroupDescriptor> groups = groupRepository.findVirtualLights(requestIp);
			for (GroupDescriptor group : groups) {
				deviceResponseMap.put(String.valueOf(Integer.parseInt(group.getId()) + 10000),
						DeviceResponse.createResponseForVirtualLight(group));
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

		if (bridgeSettings.isTraceupnp() && !body.contains("test_ha_bridge"))
			log.info("Traceupnp: hue api user create requested: " + body + " from address: " + ipAddress);
		else
			log.debug("hue api user create requested: " + body + " from address: " + ipAddress);

		if (bridgeSettingMaster.getBridgeSecurity().isUseLinkButton()
				&& bridgeSettingMaster.getBridgeControl().isLinkButton())
			toContinue = true;
		else if (!bridgeSettingMaster.getBridgeSecurity().isUseLinkButton())
			toContinue = true;

		if (toContinue) {
			log.debug("user add toContinue was true, creating user.");

			if (body != null && !body.isEmpty()) {
				try {
					aNewUser = aGsonHandler.fromJson(body, UserCreateRequest.class);
				} catch (Exception e) {
					log.warn("Could not add user. Request garbled: " + body);
					return aGsonHandler.toJson(HueErrorResponse
							.createResponse("2", "/", "Could not add user.", null, null, null).getTheErrors(),
							HueError[].class);
				}
				newUser = aNewUser.getUsername();
				aDeviceType = aNewUser.getDevicetype();
			}

			if (aDeviceType == null)
				aDeviceType = "<not given>";
			else
				aDeviceType = aDeviceType + "#" + ipAddress;

			if (newUser == null) {
				newUser = bridgeSettingMaster.getBridgeSecurity().createWhitelistUser(aDeviceType);
			} else {
				bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(newUser, aDeviceType, false);
			}

			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			if (bridgeSettings.isTraceupnp() && !aDeviceType.equals("test_ha_bridge"))
				log.info("Traceupnp: hue api user create requested for device type: " + aDeviceType + " and username: "
						+ newUser + (followingSlash ? " /api/ called" : ""));
			log.debug("hue api user create requested for device type: " + aDeviceType + " and username: " + newUser
					+ (followingSlash ? " /api/ called" : ""));
			return "[{\"success\":{\"username\":\"" + newUser + "\"}}]";
		} else
			log.debug("user add toContinue was false, returning not authorized");
		return aGsonHandler.toJson(HueErrorResponse
				.createResponse("101", "/api/", "link button not pressed", null, null, null).getTheErrors());
	}

	private Object getConfig(String userId, String ipAddress) {
		if (bridgeSettings.isTraceupnp())
			log.info("Traceupnp: hue api/:userid/config config requested from user: " + userId + " from address: "
					+ ipAddress);
		log.debug("hue api config requested: " + userId + " from " + ipAddress);
		if (bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton()) != null) {
			log.debug("hue api config requested, User invalid, returning public config");
			HuePublicConfig apiResponse = HuePublicConfig.createConfig("HA-Bridge",
					bridgeSettings.getUpnpConfigAddress(), bridgeSettings.getHubversion(), bridgeSettings.getHubmac());
			return apiResponse;
		}

		HueApiResponse apiResponse = new HueApiResponse("HA-Bridge", bridgeSettings.getUpnpConfigAddress(),
				bridgeSettingMaster.getBridgeSecurity().getWhitelist(), bridgeSettings.getHubversion(),
				bridgeSettingMaster.getBridgeControl().isLinkButton(), bridgeSettings.getHubmac());
		log.debug("api response config <<<" + aGsonHandler.toJson(apiResponse.getConfig()) + ">>>");
		return apiResponse.getConfig();
	}

	@SuppressWarnings("unchecked")
	private Object getFullState(String userId, String ipAddress) {
		log.debug("hue api full state requested: " + userId + " from " + ipAddress);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null) {
			log.debug("full state error occurred <<<" + aGsonHandler.toJson(theErrors) + ">>>");
			return theErrors;
		}

		HueApiResponse apiResponse = new HueApiResponse("HA-Bridge", bridgeSettings.getUpnpConfigAddress(),
				bridgeSettingMaster.getBridgeSecurity().getWhitelist(), bridgeSettings.getHubversion(),
				bridgeSettingMaster.getBridgeControl().isLinkButton(), bridgeSettings.getHubmac());
		apiResponse.setLights((Map<String, DeviceResponse>) this.lightsListHandler(userId, ipAddress));
		apiResponse.setGroups((Map<String, GroupResponse>) this.groupsListHandler(userId, ipAddress));

		return apiResponse;
	}

	private Object getLight(String userId, String lightId, String ipAddress) {
		log.debug("hue light requested: " + lightId + " for user: " + userId + " from " + ipAddress);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null)
			return theErrors;

		if (bridgeSettings.isUserooms() && Integer.parseInt(lightId) >= 10000) {
			GroupDescriptor group = groupRepository.findOne(String.valueOf(Integer.parseInt(lightId) - 10000));
			return DeviceResponse.createResponseForVirtualLight(group);
		}

		DeviceDescriptor device = repository.findOne(lightId);
		if (device == null) {
			// response.status(HttpStatus.SC_NOT_FOUND);
			return HueErrorResponse
					.createResponse("3", "/api/" + userId + "/lights/" + lightId, "Object not found", null, null, null)
					.getTheErrors();
		} else {
			log.debug("found device named: " + device.getName());
		}
		DeviceResponse lightResponse = null;
		if (device.containsType(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
			CallItem[] callItems = null;
			try {
				if (device.getOnUrl() != null)
					callItems = aGsonHandler.fromJson(device.getOnUrl(), CallItem[].class);
			} catch (JsonSyntaxException e) {
				log.warn("Could not decode Json for url items to get Hue state for device: " + device.getName());
				callItems = null;
			}

			for (int i = 0; callItems != null && i < callItems.length; i++) {
				if ((callItems[i].getType() != null
						&& callItems[i].getType().equals(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex]))
						|| callItems[i].getItem().getAsString().startsWith("{\"ipAddress\":\"")) {
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
		ColorData colorData = null;

		if (bridgeSettings.isTracestate())
			log.info("Tracestate: Update state requested: " + userId + " from " + ipAddress + " body: " + body);

		log.debug("Update state requested: " + userId + " from " + ipAddress + " body: " + body);

		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null)
			return aGsonHandler.toJson(theErrors);

		try {
			theStateChanges = aGsonHandler.fromJson(body, StateChangeBody.class);
		} catch (Exception e) {
			log.warn("Could not parse state change body. Light state not changed.");
			return aGsonHandler.toJson(HueErrorResponse
					.createResponse("2", "/lights/" + lightId, "Could not parse state change body.", null, null, null)
					.getTheErrors(), HueError[].class);
		}

		DeviceDescriptor device = repository.findOne(lightId);
		if (device == null) {
			log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from "
					+ ipAddress + " body: " + body);
			return aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
					"Could not find device.", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
		}

		if (body.contains("\"bri_inc\""))
			targetBriInc = Integer.valueOf(theStateChanges.getBri_inc());
		else if (body.contains("\"bri\"")) {
			targetBri = Integer.valueOf(theStateChanges.getBri());
		}

		state = device.getDeviceState();
		if (state == null)
			state = DeviceState.createDeviceState(device.isColorDevice());

		colorData = parseColorInfo(body, theStateChanges, state, targetBri, targetBriInc);

		responseString = this.formatSuccessHueResponse(theStateChanges, body, lightId, state, targetBri, targetBriInc,
				colorData, device.isOffState());
		device.setDeviceState(state);

		return responseString;
	}

	private String changeState(String userId, String lightId, String body, String ipAddress, boolean ignoreRequester) {
		if (bridgeSettings.isUserooms() && Integer.parseInt(lightId) >= 10000) {
			return changeGroupState(userId, String.valueOf(Integer.parseInt(lightId) - 10000), body, ipAddress, true);
		}
		String responseString = null;
		String url = null;
		StateChangeBody theStateChanges = null;
		DeviceState state = null;
		Integer targetBri = null;
		Integer targetBriInc = null;
		boolean isColorRequest = false;
		boolean isDimRequest = false;
		boolean isOnRequest = false;
		ColorData colorData = null;
		if (bridgeSettings.isTracestate())
			log.info("Tracestate: hue state change requested: " + userId + " from " + ipAddress + " body: " + body);

		log.debug("hue state change requested: " + userId + " from " + ipAddress + " body: " + body);
		HueError[] theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors != null) {
			log.warn("Errors in security: <<<" + aGsonHandler.toJson(theErrors) + ">>>");
			return aGsonHandler.toJson(theErrors);
		}
		try {
			theStateChanges = aGsonHandler.fromJson(body, StateChangeBody.class);
		} catch (Exception e) {
			theStateChanges = null;
		}
		if (theStateChanges == null) {
			log.warn("Could not parse state change body. Light state not changed.");
			return aGsonHandler.toJson(HueErrorResponse
					.createResponse("2", "/lights/" + lightId, "Could not parse state change body.", null, null, null)
					.getTheErrors(), HueError[].class);
		}

		DeviceDescriptor device = repository.findOne(lightId);
		if (device == null) {
			log.warn("Could not find device: " + lightId + " for hue state change request: " + userId + " from "
					+ ipAddress + " body: " + body);
			return aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
					"Could not find device.", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
		}

		state = device.getDeviceState();
		if (state == null) {
			state = DeviceState.createDeviceState(device.isColorDevice());
			device.setDeviceState(state);
		}

		if (body.contains("\"bri_inc\"")) {
			targetBriInc = Integer.valueOf(theStateChanges.getBri_inc());
			isDimRequest = true;
		} else if (body.contains("\"bri\"")) {
			targetBri = Integer.valueOf(theStateChanges.getBri());
			isDimRequest = true;
		}

		colorData = parseColorInfo(body, theStateChanges, state, targetBri, targetBriInc);
		if (colorData != null)
			isColorRequest = true;

		if (body.contains("\"on\"")) {
			isOnRequest = true;
		}

		if(device.isOnFirstDim()) {
			if(isDimRequest && !device.getDeviceState().isOn()) {
				isOnRequest = true;
				theStateChanges.setOn(true);
				// isDimRequest = false;
				// isColorRequest = false;
			} else if (isDimRequest && device.getDeviceState().isOn()) {
				if (device.getDeviceState().getBri() == theStateChanges.getBri()) {
					isOnRequest = true;
					theStateChanges.setOn(true);
					// isDimRequest = false;
					// isColorRequest = false;
				} else {
					isOnRequest = false;
					// isDimRequest = true;
					// isColorRequest = false;
				}
			}
		} else if (device.isOnWhenDimPresent()) {
			if (isDimRequest) {
				isOnRequest = true;
				theStateChanges.setOn(true);
			}
		} else if (device.isDimNoOn()) {
			if (isDimRequest && isOnRequest) {
				isOnRequest = false;
			}
		}

		if(isColorRequest && isDimRequest && !device.isDimOnColor()) {
			isDimRequest = false;
		}

/* Old code supperceded by the above block
		if (!device.isOnFirstDim() && device.isOnWhenDimPresent() && isDimRequest && !isOnRequest) {
			isOnRequest = true;
			theStateChanges.setOn(true);
		} else 

		if (device.isOnFirstDim() && isDimRequest && !device.getDeviceState().isOn()) {
			isOnRequest = true;
			theStateChanges.setOn(true);
			isDimRequest = false;
			isColorRequest = false;
		} else if (device.isOnFirstDim() && isDimRequest && device.getDeviceState().isOn()) {
			if (device.getDeviceState().getBri() == theStateChanges.getBri()) {
				isOnRequest = true;
				theStateChanges.setOn(true);
				isDimRequest = false;
				isColorRequest = false;
			} else {
				isOnRequest = false;
				isDimRequest = true;
				isColorRequest = false;
			}
		}
*/

		if (isOnRequest) {
			if (bridgeSettings.isTracestate())
				log.info("Tracestate: Calling on-off as requested: " + theStateChanges.isOn());

			log.debug("Calling on-off as requested.");
			if (theStateChanges.isOn()) {
				url = device.getOnUrl();
			} else if (!theStateChanges.isOn()) {
				url = device.getOffUrl();
			}

			// code for backwards compatibility
			if (device.getMapType() != null
					&& device.getMapType().equalsIgnoreCase(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
				if (url == null)
					url = device.getOnUrl();
			}

			if (url != null && !url.equals("")) {
				responseString = callUrl(url, device, userId, lightId, body, ipAddress, ignoreRequester, targetBri,
						targetBriInc, colorData);
				if (responseString != null && responseString.contains("[{\"error\":")) {
					log.warn("On/Off Request failed with: " + responseString);
				}
			} else {
				log.info("On/off url not available for state change, lightId: " + lightId + ", userId: " + userId
						+ ", from IP: " + ipAddress + ", body: " + body);
			}
		}

		if (isDimRequest) {
			if (bridgeSettings.isTracestate())
				log.info("Tracestate: Calling dim as requested: " + targetBri + ", inc: " + targetBriInc);

			log.debug("Calling dim as requested.");
			url = device.getDimUrl();

			// code for backwards compatibility
			if (device.getMapType() != null
					&& device.getMapType().equalsIgnoreCase(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
				if (url == null)
					url = device.getOnUrl();
			}

			if (url != null && !url.equals("")) {
				if (isOnRequest) {
					try {
						Thread.sleep(bridgeSettings.getButtonsleep());
					} catch (InterruptedException e) {
						// ignore
					}
				}
				responseString = callUrl(url, device, userId, lightId, body, ipAddress, ignoreRequester, targetBri,
						targetBriInc, colorData);
				if (responseString != null && responseString.contains("[{\"error\":")) {
					log.warn("Dim Request failed with: " + responseString);
				}
			} else {
				log.info("Dim url not available for state change, lightId: " + lightId + ", userId: " + userId
						+ ", from IP: " + ipAddress + ", body: " + body);
			}
		}

		if (isColorRequest) {
			if (bridgeSettings.isTracestate())
				log.info("Tracestate: Calling color as requested. With " + colorData);

			log.debug("Calling color as requested.");
			url = device.getColorUrl();
			// code for backwards compatibility
			if (device.getMapType() != null
					&& device.getMapType().equalsIgnoreCase(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
				if (url == null)
					url = device.getOnUrl();
			}

			if (url != null && !url.equals("")) {
				if ((isOnRequest && !isDimRequest) || isDimRequest) {
					try {
						Thread.sleep(bridgeSettings.getButtonsleep());
					} catch (InterruptedException e) {
						// ignore
					}
				}
				responseString = callUrl(url, device, userId, lightId, body, ipAddress, ignoreRequester, targetBri,
						targetBriInc, colorData);
				if (responseString != null && responseString.contains("[{\"error\":")) {
					log.warn("Color Request failed with: " + responseString);
				}
			} else {
				log.info("Color url not available for state change, lightId: " + lightId + ", userId: " + userId
						+ ", from IP: " + ipAddress + ", body: " + body);
			}
		}

		if (responseString == null || !responseString.contains("[{\"error\":")) {
			if (!device.isNoState()) {
				responseString = this.formatSuccessHueResponse(theStateChanges, body, lightId, state, targetBri,
						targetBriInc, colorData, device.isOffState());
				device.setDeviceState(state);
			} else {
				DeviceState dummyState = DeviceState.createDeviceState(device.isColorDevice());
				responseString = this.formatSuccessHueResponse(theStateChanges, body, lightId, dummyState, targetBri,
						targetBriInc, colorData, device.isOffState());
			}
		}

		return responseString;

	}

	private ColorData parseColorInfo(String body, StateChangeBody theStateChanges, DeviceState state, Integer targetBri,
			Integer targetBriInc) {
		ColorData colorData = null;
		List<Double> xy = null;
		List<Double> xyInc = null;
		Integer ct = null;
		Integer ctInc = null;
		HueSatBri anHSL = null;
		Integer hue = null;
		Integer sat = null;
		Integer hueInc = null;
		Integer satInc = null;

		if (body.contains("\"xy\"")) {
			xy = theStateChanges.getXy();
		}

		if (body.contains("\"ct\"")) {
			ct = theStateChanges.getCt();
		}
		if (body.contains("\"hue\"")) {
			hue = theStateChanges.getHue();
		}
		if (body.contains("\"sat\"")) {
			sat = theStateChanges.getSat();
		}

		if (body.contains("\"xy_inc\"")) {
			xyInc = theStateChanges.getXy_inc();
		}

		if (body.contains("\"ct_inc\"")) {
			ctInc = theStateChanges.getCt_inc();
		}

		if (body.contains("\"hue_inc\"")) {
			hueInc = theStateChanges.getHue_inc();
		}

		if (body.contains("\"sat_inc\"")) {
			satInc = theStateChanges.getSat_inc();
		}

		if (xy != null && xy.size() == 2) {
			colorData = new ColorData(ColorData.ColorMode.XY, xy);
		} else if (xyInc != null && xyInc.size() == 2) {
			List<Double> current = state.getXy();
			current.set(0, current.get(0) + xyInc.get(0));
			current.set(1, current.get(1) + xyInc.get(1));
			colorData = new ColorData(ColorData.ColorMode.XY, current);
		} else if (ct != null) {
			colorData = new ColorData(ColorData.ColorMode.CT, ct);
		} else if (ctInc != null) {
			colorData = new ColorData(ColorData.ColorMode.CT, state.getCt() + ctInc);
		} else if (hue != null || sat != null) {
			anHSL = new HueSatBri();
			int bri = 0;
			if (targetBriInc != null) {
				bri = state.getBri() - targetBriInc;
				if (bri < 1)
					bri = 1;
			} else if (targetBri != null) {
				bri = targetBri;
			} else {
				bri = state.getBri();
			}
			anHSL.setBri(bri);
			if (hue != null)
				anHSL.setHue(hue);
			else
				anHSL.setHue(state.getHue());

			if (sat != null)
				anHSL.setSat(sat);
			else
				anHSL.setSat(state.getSat());
			log.debug("hue/sat request - " + anHSL);
			colorData = new ColorData(ColorData.ColorMode.HS, anHSL);
		} else if (hueInc != null || satInc != null) {
			anHSL = new HueSatBri();
			int bri = 0;
			if (targetBriInc != null) {
				bri = state.getBri() - targetBriInc;
				if (bri < 1)
					bri = 1;
			} else if (targetBri != null) {
				bri = targetBri;
			} else {
				bri = state.getBri();
			}
			anHSL.setBri(bri);
			if (hueInc != null)
				anHSL.setHue(state.getHue() - hueInc);
			else
				anHSL.setHue(state.getHue());

			if (satInc != null)
				anHSL.setSat(state.getSat() - satInc);
			else
				anHSL.setSat(state.getSat());
			log.info("hue/sat inc request - " + anHSL);
			colorData = new ColorData(ColorData.ColorMode.HS, anHSL);

		}
		return colorData;
	}

	@SuppressWarnings("unchecked")
	private String changeGroupState(String userId, String groupId, String body, String ipAddress,
			boolean fakeLightResponse) {
		ColorData colorData = null;
		log.debug("PUT action to group  " + groupId + " from " + ipAddress + " user " + userId + " with body " + body);
		HueError[] theErrors = null;
		theErrors = bridgeSettingMaster.getBridgeSecurity().validateWhitelistUser(userId, null,
				bridgeSettingMaster.getBridgeSecurity().isUseLinkButton());
		if (theErrors == null) {
			if (bridgeSettingMaster.getBridgeSecurity().isSettingsChanged())
				bridgeSettingMaster.updateConfigFile();

			GroupDescriptor group = null;
			Integer targetBriInc = null;
			Integer targetBri = null;
			DeviceState state = null;
			Map<String, DeviceResponse> lights = null;
			if (groupId.equalsIgnoreCase("0")) {
				lights = (Map<String, DeviceResponse>) lightsListHandler(userId, ipAddress);
			} else {
				group = groupRepository.findOne(groupId);
				if (group == null || group.isInactive()) {
					return aGsonHandler.toJson(HueErrorResponse
							.createResponse("3", "/groups/" + groupId,
									"resource, /groups/" + groupId + ", not available", null, null, null)
							.getTheErrors(), HueError[].class);
				} else {
					if (fakeLightResponse) {
						lights = repository.findAllByGroupWithState(group.getLights(), ipAddress, myHueHome,
								aGsonHandler, true);
					} else {
						lights = repository.findAllByGroupWithState(group.getLights(), ipAddress, myHueHome,
								aGsonHandler);
					}
				}
			}

			if (lights != null) {
				StateChangeBody theStateChanges = null;
				try {
					theStateChanges = aGsonHandler.fromJson(body, StateChangeBody.class);
				} catch (Exception e) {
					theStateChanges = null;
				}
				if (theStateChanges == null) {
					log.warn("Could not parse state change body. Light state not changed.");
					return aGsonHandler.toJson(
							HueErrorResponse.createResponse("2", "/groups/" + groupId + "/action",
									"Could not parse state change body.", null, null, null).getTheErrors(),
							HueError[].class);
				}

				if (group != null) {
					if (body.contains("\"bri_inc\"")) {
						targetBriInc = Integer.valueOf(theStateChanges.getBri_inc());
					} else if (body.contains("\"bri\"")) {
						targetBri = Integer.valueOf(theStateChanges.getBri());
					}

					state = group.getAction();
					if (state == null) {
						state = DeviceState.createDeviceState(true);
						group.setAction(state);
					}
				}

				boolean turnOn = false;
				boolean turnOff = false;
				if (!(body.contains("\"bri_inc\"") || body.contains("\"bri\""))) {
					if (!(body.contains("\"xy\"") || body.contains("\"ct\"") || body.contains("\"hue\""))) {
						if (theStateChanges.isOn()) {
							turnOn = true;
						} else if (!theStateChanges.isOn()) {
							turnOff = true;
						}
					}
				}
				for (Map.Entry<String, DeviceResponse> light : lights.entrySet()) {
					log.debug("Processing light" + light.getKey() + ": " + turnOn + " " + turnOff + " "
							+ light.getValue().getState().isOn());
					// ignore on/off for devices that are already on/off
					if (turnOff && !light.getValue().getState().isOn())
						continue;
					if (turnOn && light.getValue().getState().isOn())
						continue;
					changeState(userId, light.getKey(), body, ipAddress, fakeLightResponse);
				}
				// construct success response: one success message per changed property, but not
				// per light
				if (group != null) { // if not group 0
					String response = formatSuccessHueResponse(theStateChanges, body,
							String.valueOf(Integer.parseInt(groupId) + 10000), state, targetBri, targetBriInc,
							colorData, true);
					group.setAction(state);
					if (fakeLightResponse) {
						return response;
					}
				}

				String successString = "[";
				for (String pairStr : body.replaceAll("[{|}]", "").split(",\\s*\"")) {
					String[] pair = pairStr.split(":");
					if (fakeLightResponse) {
						successString += "{\"success\":{ \"/lights/" + String.valueOf(Integer.parseInt(groupId) + 10000)
								+ "/state/" + pair[0].replaceAll("\"", "").trim() + "\": " + pair[1].trim() + "}},";
					} else {
						successString += "{\"success\":{ \"address\": \"/groups/" + groupId + "/action/"
								+ pair[0].replaceAll("\"", "").trim() + "\", \"value\": " + pair[1].trim() + "}},";
					}

				}
				return (successString.length() == 1) ? "[]"
						: successString.substring(0, successString.length() - 1) + "]";
			}
		}

		return aGsonHandler.toJson(theErrors);
	}

	protected String callUrl(String url, DeviceDescriptor device, String userId, String lightId, String body,
			String ipAddress, boolean ignoreRequester, Integer targetBri, Integer targetBriInc, ColorData colorData) {
		String responseString = null;
		MultiCommandUtil aMultiUtil = new MultiCommandUtil();
		aMultiUtil.setTheDelay(bridgeSettings.getButtonsleep());
		aMultiUtil.setDelayDefault(bridgeSettings.getButtonsleep());
		aMultiUtil.setSetCount(1);

		if (!url.startsWith("[")) {
			if (url.startsWith("{\"item"))
				url = "[" + url + "]";
			else {
				if (url.startsWith("{"))
					url = "[{\"item\":" + url + "}]";
				else
					url = "[{\"item\":\"" + url + "\"}]";
			}
		} else if (!url.startsWith("[{\"item\""))
			url = "[{\"item\":" + url + "}]";

		if (bridgeSettings.isTracestate())
			log.info("Tracestate: Decode Json for url items: " + url);
		log.debug("Decode Json for url items: " + url);
		CallItem[] callItems = null;
		try {
			callItems = aGsonHandler.fromJson(url, CallItem[].class);
		} catch (JsonSyntaxException e) {
			log.warn("Could not decode Json for url items: " + lightId + " for hue state change request: " + userId
					+ " from " + ipAddress + " body: " + body + " url items: " + url);
			return aGsonHandler.toJson(HueErrorResponse.createResponse("3", "/lights/" + lightId,
					"Could decode json in request", "/lights/" + lightId, null, null).getTheErrors(), HueError[].class);
		}

		for (int i = 0; callItems != null && i < callItems.length; i++) {
			if (!ignoreRequester) {
				if (!filterByRequester(device.getRequesterAddress(), ipAddress)
						|| !filterByRequester(callItems[i].getFilterIPs(), ipAddress)) {
					log.warn("filter for requester address not present in: (device)" + device.getRequesterAddress()
							+ " OR then (item)" + callItems[i].getFilterIPs() + " with request ip of: " + ipAddress);
					continue;
				}
			}

			if (callItems[i].getCount() != null && callItems[i].getCount() > 0)
				aMultiUtil.setSetCount(callItems[i].getCount());
			else
				aMultiUtil.setSetCount(1);
			// code for backwards compatibility
			if ((callItems[i].getType() == null || callItems[i].getType().trim().isEmpty())) {
				if (validMapTypes.validateType(device.getMapType()))
					callItems[i].setType(device.getMapType().trim());
				else if (validMapTypes.validateType(device.getDeviceType()))
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
					if (bridgeSettings.isTracestate())
						log.info("Tracestate: Calling Home device handler for type : " + callItems[i].getType().trim());
					log.debug("Calling Home device handler for type : " + callItems[i].getType().trim());
					responseString = homeManager.findHome(callItems[i].getType().trim()).deviceHandler(callItems[i],
							aMultiUtil, lightId, device.getDeviceState().getBri(), targetBri, targetBriInc, colorData,
							device, body);
					if (responseString != null && responseString.contains("{\"error\":")) {
						x = aMultiUtil.getSetCount();
					}
				}
			} else
				log.warn("Call Items type is null <<<" + callItems[i] + ">>>");
		}

		if ((callItems == null) || (callItems.length == 0)) {
			log.warn("No call items were available: <<<" + url + ">>>");
		}

		return responseString;
	}

	private void startupDeviceCall() {
		String aUserId = bridgeSettingMaster.getBridgeSecurity().createWhitelistUser("test_ha_bridge");
		List<DeviceDescriptor> deviceList = repository.findAll();
		String aChangeBody;
		String[] components;
		boolean comma = false;

		for (DeviceDescriptor aDevice : deviceList) {
			if(aDevice.getStartupActions() != null && !aDevice.getStartupActions().isEmpty()) {
				log.info("Startup call for {} with startupActions {}", aDevice.getName(), aDevice.getStartupActions());
				aChangeBody = "{";
				components = aDevice.getStartupActions().split(":");
				if(components.length > 0  && components[0] != null && components[0].length() > 0) {
					if(components[0].equals("On")) {
						aChangeBody = aChangeBody + "\"on\":true";
					}
					else {
						aChangeBody = aChangeBody + "\"on\":false";
					}
					comma = true;
				}
				if(components.length > 1  && components[1] != null && components[1].length() > 0 && !(components.length > 2  && components[2] != null && components[2].length() > 0)) {
					if(comma)
						aChangeBody = aChangeBody + ",";
					aChangeBody = aChangeBody + "\"bri\":" + components[1];
					comma = true;
				}
				if(components.length > 2  && components[2] != null && components[2].length() > 0) {
					if(comma)
						aChangeBody = aChangeBody + ",";
						String theRGB = components[2].substring(components[2].indexOf('(') + 1, components[2].indexOf(')'));
						String[] RGB = theRGB.split(",");
						float[] hsb = new float[3];
						Color.RGBtoHSB(Integer.parseInt(RGB[0]), Integer.parseInt(RGB[1]), Integer.parseInt(RGB[2]), hsb);
						float hue = hsb[0] * (float) 360.0;
						float sat = hsb[1] * (float) 100.0;
						float bright = hsb[2] * (float) 100.0;
						aChangeBody = String.format("%s\"hue\":%.2f,\"sat\":%.2f,\"bri\":%d", aChangeBody, hue, sat, Math.round(bright));
				}
				aChangeBody = aChangeBody + "}";
				log.info("Startup call to set state for {} with body {}", aDevice.getName(), aChangeBody);
				changeState(aUserId, aDevice.getId(), aChangeBody, "localhost", true);
			}
		}
	}
}
