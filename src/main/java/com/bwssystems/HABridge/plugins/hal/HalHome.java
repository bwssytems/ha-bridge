package com.bwssystems.HABridge.plugins.hal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class HalHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HalHome.class);
	private Map<String, HalInfo> hals;
	private Boolean validHal;
	private HTTPHandler anHttpHandler;

	public HalHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		if(!validHal)
			return null;
		log.debug("consolidating devices for hues");
		List<HalDevice> theResponse = null;
		Iterator<String> keys = hals.keySet().iterator();
		List<HalDevice> deviceList = new ArrayList<HalDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = hals.get(key).getLights();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get lights for Hal with name: " + key + ", skipping this HAL.");
				continue;
			}
			theResponse = hals.get(key).getAppliances();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get appliances for Hal with name: " + key);
			theResponse = hals.get(key).getTheatre();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get theatre for Hal with name: " + key);
			theResponse = hals.get(key).getCustom();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get custom for Hal with name: " + key);
			theResponse = hals.get(key).getHVAC();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get HVAC for Hal with name: " + key);
			theResponse = hals.get(key).getHome(key);
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Homes for Hal with name: " + key);
			theResponse = hals.get(key).getGroups();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Groups for Hal with name: " + key);
			theResponse = hals.get(key).getMacros();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Macros for Hal with name: " + key);
			theResponse = hals.get(key).getScenes();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Scenes for Hal with name: " + key);
			theResponse = hals.get(key).getButtons();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Buttons for Hal with name: " + key);
		}
		return deviceList;
	}
	
	private Boolean addHalDevices(List<HalDevice> theDeviceList, List<HalDevice> theSourceList, String theKey) {
		if(!validHal)
			return null;
		Iterator<HalDevice> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			HalDevice theDevice = devices.next();
			HalDevice aNewHalDevice = new HalDevice();
			aNewHalDevice.setHaldevicetype(theDevice.getHaldevicetype());
			aNewHalDevice.setHaldevicename(theDevice.getHaldevicename());
			aNewHalDevice.setButtons(theDevice.getButtons());
			aNewHalDevice.setHaladdress(hals.get(theKey).getHalAddress().getIp());
			aNewHalDevice.setHalname(theKey);
			theDeviceList.add(aNewHalDevice);
		}
		anHttpHandler = new HTTPHandler();
		return true;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, DeviceDescriptor device, String body) {
		log.debug("executing HUE api request to HAL Http " + anItem.getItem().getAsString());
		String responseString = null;

			String anUrl = BrightnessDecode.calculateReplaceIntensityValue(anItem.getItem().getAsString(),
					intensity, targetBri, targetBriInc, false);
			String aBody = null;
			if(anItem.getHttpBody()!= null && !anItem.getHttpBody().isEmpty())
				aBody = BrightnessDecode.calculateReplaceIntensityValue(anItem.getHttpBody(),
						intensity, targetBri, targetBriInc, false);
			// make call
			if (anHttpHandler.doHttpRequest(anUrl, anItem.getHttpVerb(), anItem.getContentType(), aBody,
					new Gson().fromJson(anItem.getHttpHeaders(), NameValue[].class)) == null) {
				log.warn("Error on calling url to change device state: " + anUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "state", null, null).getTheErrors(), HueError[].class);
			}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		validHal = bridgeSettings.isValidHal();
		log.info("HAL Home created." + (validHal ? "" : " No HAL devices configured."));
		if(!validHal)
			return null;
		hals = new HashMap<String, HalInfo>();
		Iterator<NamedIP> theList = bridgeSettings.getHaladdress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHal = theList.next();
	      	try {
	      		hals.put(aHal.getName(), new HalInfo(aHal, bridgeSettings.getHaltoken()));
			} catch (Exception e) {
		        log.error("Cannot get hal client (" + aHal.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
		        return null;
			}
		}
		return this;
	}

	@Override
	public void closeHome() {
		// noop
		
	}
}
