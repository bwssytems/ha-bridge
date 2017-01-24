package com.bwssystems.HABridge.plugins.domoticz;

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

public class DomoticzHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(DomoticzHome.class);
	private Map<String, DomoticzHandler> domoticzs;
	private Boolean validDomoticz;
	private HTTPHandler anHttpHandler;

	public DomoticzHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		if(!validDomoticz)
			return null;
		log.debug("consolidating devices for hues");
		List<DomoticzDevice> theResponse = null;
		Iterator<String> keys = domoticzs.keySet().iterator();
		List<DomoticzDevice> deviceList = new ArrayList<DomoticzDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = domoticzs.get(key).getDevices();
			if(theResponse != null)
				addDomoticzDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get lights for Domoticz with name: " + key + ", skipping this Domoticz.");
				continue;
			}
			theResponse = domoticzs.get(key).getScenes();
			if(theResponse != null)
				addDomoticzDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Scenes for Domoticz with name: " + key);
		}
		return deviceList;
	}
	
	private Boolean addDomoticzDevices(List<DomoticzDevice> theDeviceList, List<DomoticzDevice> theSourceList, String theKey) {
		if(!validDomoticz)
			return null;
		Iterator<DomoticzDevice> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			DomoticzDevice theDevice = devices.next();
			DomoticzDevice aNewDomoticzDevice = new DomoticzDevice();
			aNewDomoticzDevice.setDevicetype(theDevice.getDevicetype());
			aNewDomoticzDevice.setDevicename(theDevice.getDevicename());
			aNewDomoticzDevice.setDomoticzaddress(domoticzs.get(theKey).getDomoticzAddress().getIp());
			aNewDomoticzDevice.setDomoticzname(theKey);
			theDeviceList.add(aNewDomoticzDevice);
		}
		anHttpHandler = new HTTPHandler();
		return true;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, DeviceDescriptor device, String body) {
		log.debug("executing HUE api request to Domoticz Http " + anItem.getItem().getAsString());
		String responseString = null;

			String anUrl = BrightnessDecode.calculateReplaceIntensityValue(anItem.getItem().getAsString(),
					intensity, targetBri, targetBriInc, false);
			String aBody;
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
		validDomoticz = bridgeSettings.isValidDomoticz();
		log.info("Domoticz Home created." + (validDomoticz ? "" : " No Domoticz devices configured."));
		if(!validDomoticz)
			return null;
		domoticzs = new HashMap<String, DomoticzHandler>();
		Iterator<NamedIP> theList = bridgeSettings.getDomoticzaddress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aDomoticz = theList.next();
	      	try {
	      		domoticzs.put(aDomoticz.getName(), new DomoticzHandler(aDomoticz));
			} catch (Exception e) {
		        log.error("Cannot get Domoticz client (" + aDomoticz.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
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
