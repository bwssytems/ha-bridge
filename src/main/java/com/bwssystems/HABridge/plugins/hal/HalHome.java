package com.bwssystems.HABridge.plugins.hal;

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
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.google.gson.Gson;

public class HalHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HalHome.class);
	private Map<String, HalInfo> hals;
	private Boolean validHal;

	public HalHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		if(!validHal)
			return null;
		log.debug("consolidating devices for HALs");
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
			theDeviceList.add(theDevice);
		}
		return true;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		boolean halFound = false;
		String responseString = null;
		String theUrl = anItem.getItem().getAsString();
		if(theUrl != null && !theUrl.isEmpty () && theUrl.contains("http")) {
			String intermediate = theUrl.substring(theUrl.indexOf("://") + 3);
			String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
//			String theUrlBody = intermediate.substring(intermediate.indexOf('/') + 1);
//			String hostAddr = null;
//			String port = null;
//			if (hostPortion.contains(":")) {
//				hostAddr = hostPortion.substring(0, intermediate.indexOf(':'));
//				port = hostPortion.substring(intermediate.indexOf(':') + 1);
//			} else
//				hostAddr = hostPortion;
			log.debug("executing HUE api request to Http "
					+ (anItem.getHttpVerb() == null ? "GET" : anItem.getHttpVerb()) + ": "
					+ anItem.getItem().getAsString());

			String anUrl = null;
			
			anUrl = BrightnessDecode.calculateReplaceIntensityValue(intermediate, intensity, targetBri, targetBriInc, false);
			anUrl = DeviceDataDecode.replaceDeviceData(anUrl, device);
			anUrl = TimeDecode.replaceTimeValue(anUrl);
			
			for (Map.Entry<String, HalInfo> entry : hals.entrySet())
			{
				if(entry.getValue().getHalAddress().getIp().equals(hostPortion)) {
					halFound = true;
			    	if(entry.getValue().getHalAddress().getSecure()!= null && entry.getValue().getHalAddress().getSecure())
			    		anUrl = "https://" + anUrl;
			    	else
			    		anUrl = "http://" + anUrl;

			    	if(!anUrl.contains("?Token="))
						anUrl = anUrl + "?Token=" + entry.getValue().getHalAddress().getPassword();
					
					log.debug("executing HUE api request to Http "
							+ (anItem.getHttpVerb() == null ? "GET" : anItem.getHttpVerb()) + ": "
							+ anUrl);

					if (entry.getValue().deviceCommand(anUrl) == null) {
						log.warn("Error on calling hal to change device state: " + anUrl);
						responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
								"Error on calling url to change device state", "/lights/"
								+ lightId + "state", null, null).getTheErrors(), HueError[].class);
					}
				}
			}
		}
		
		if(!halFound) {
			log.warn("No HAL found to call: " + theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"No HAL found.", "/lights/"
					+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		validHal = bridgeSettings.getBridgeSettingsDescriptor().isValidHal();
		log.info("HAL Home created." + (validHal ? "" : " No HAL devices configured."));
		if(!validHal)
			return null;
		hals = new HashMap<String, HalInfo>();
		Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getHaladdress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHal = theList.next();
	      	try {
	      		hals.put(aHal.getName(), new HalInfo(aHal, bridgeSettings.getBridgeSettingsDescriptor().getHaltoken()));
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
