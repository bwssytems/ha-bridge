package com.bwssystems.HABridge.plugins.homegenie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.ColorDecode;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.bwssystems.HABridge.plugins.http.HTTPHome;
import com.google.gson.Gson;

public class HomeGenieHome implements Home {
	private static final Logger log = LoggerFactory.getLogger(HomeGenieHome.class);
	private Map<String, HomeGenieInstance> homegenieMap;
	private Boolean validHomeGenie;
	private HTTPHandler httpClient;
	private boolean closed;

	public HomeGenieHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {

		String theUrl = anItem.getItem().getAsString().replaceAll("^\"|\"$", "");
		String responseString = null;

		if (theUrl != null && !theUrl.isEmpty()) {
			String anUrl = BrightnessDecode.calculateReplaceIntensityValue(theUrl, intensity, targetBri, targetBriInc,
					false);
			if (colorData != null) {
				anUrl = ColorDecode.replaceColorData(anUrl, colorData,
						BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), true);
			}
			anUrl = DeviceDataDecode.replaceDeviceData(anUrl, device);
			anUrl = TimeDecode.replaceTimeValue(anUrl);

			anUrl = BrightnessDecode.calculateReplaceIntensityValue(anUrl, intensity, targetBri, targetBriInc, false);
			if (colorData != null) {
				anUrl = ColorDecode.replaceColorData(anUrl, colorData,
						BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);
			}
			anUrl = DeviceDataDecode.replaceDeviceData(anUrl, device);
			anUrl = TimeDecode.replaceTimeValue(anUrl);

			HomeGenieCommand theCommand = null;
			try {
				theCommand = new Gson().fromJson(anUrl, HomeGenieCommand.class);
			} catch (Exception e) {
				log.warn("Cannot parse command to HomeGenie <<<" + theUrl + ">>>", e);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/" + lightId + "/state", null, null)
						.getTheErrors(), HueError[].class);
				return responseString;
			}

			HomeGenieInstance theHandler = homegenieMap.get(device.getTargetDevice());
			if (theHandler != null) {
				try {
					boolean success = theHandler.callCommand(theCommand.getDeviceId(), theCommand.getModuleType(), theCommand.getCommand(), httpClient);
					if (!success) {
						log.warn("Comand had error to HomeGenie");
						responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
								"Error on calling url to change device state", "/lights/" + lightId + "/state", null,
								null).getTheErrors(), HueError[].class);
					}
				} catch (Exception e) {
					log.warn("Cannot send comand to HomeGenie", e);
					responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
							"Error on calling url to change device state", "/lights/" + lightId + "/state", null, null)
							.getTheErrors(), HueError[].class);
				}
			} else {
				log.warn("HomeGenie Call could not complete, no address found: " + theUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/" + lightId + "/state", null, null)
						.getTheErrors(), HueError[].class);
			}
		} else {
			log.warn(
					"HomeGenie Call to be presented as http(s)://<ip_address>(:<port>)/payload, format of request unknown: "
							+ theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"Error on calling url to change device state", "/lights/" + lightId + "/state", null, null)
					.getTheErrors(), HueError[].class);
		}
		return responseString;
	}

	@Override
	public Object getItems(String type) {

		if (!validHomeGenie)
			return null;
		log.debug("consolidating devices for HomeGenie");
		List<Module> theResponse = null;
		Iterator<String> keys = homegenieMap.keySet().iterator();
		List<HomeGenieDevice> deviceList = new ArrayList<HomeGenieDevice>();
		while (keys.hasNext()) {
			String key = keys.next();
			theResponse = homegenieMap.get(key).getDevices(httpClient);
			if (theResponse != null)
				addHomeGenieDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get devices for HomeGenie with name: " + key + ", skipping this HomeGenie.");
				continue;
			}
		}
		return deviceList;
	}

	private Boolean addHomeGenieDevices(List<HomeGenieDevice> theDeviceList, List<Module> theSourceList,
			String theKey) {
		Iterator<Module> hgModules = theSourceList.iterator();
		while (hgModules.hasNext()) {
			Module aModule = hgModules.next();
			HomeGenieDevice theDevice = new HomeGenieDevice();
			theDevice.setDeviceDetail(aModule);
			theDevice.setGatewayName(theKey);
			theDeviceList.add(theDevice);
		}
		return true;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		homegenieMap = null;
		validHomeGenie = bridgeSettings.getBridgeSettingsDescriptor().isValidHomeGenie();
		log.info("HomeGenie Home created." + (validHomeGenie ? "" : " No HomeGenies configured."));
		if (validHomeGenie) {
			homegenieMap = new HashMap<String, HomeGenieInstance>();
			httpClient = HTTPHome.getHandler();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getHomegenieaddress().getDevices()
					.iterator();
			while (theList.hasNext() && validHomeGenie) {
				NamedIP aHomeGenie = theList.next();
				try {
					homegenieMap.put(aHomeGenie.getName(), new HomeGenieInstance(aHomeGenie, httpClient));
				} catch (Exception e) {
					log.error("Cannot get HomeGenie (" + aHomeGenie.getName() + ") setup, Exiting with message: "
							+ e.getMessage(), e);
					validHomeGenie = false;
				}
			}
		}
		return this;
	}

	@Override
	public void closeHome() {
		log.debug("Closing Home.");
		if (!closed && validHomeGenie) {
			log.debug("Home is already closed....");
			return;
		}

		if (httpClient != null)
			httpClient.closeHandler();

		homegenieMap = null;
		closed = true;
	}

	@Override
	public void refresh() {
		// noop
	}
}
