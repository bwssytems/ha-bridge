package com.bwssystems.HABridge.plugins.openhab;

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

public class OpenHABHome implements Home  {
    private static final Logger log = LoggerFactory.getLogger(OpenHABHome.class);
	private Map<String, OpenHABInstance> openhabMap;
	private Boolean validOpenhab;
    private HTTPHandler httpClient;
	private boolean closed;
	
	public OpenHABHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}


	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {

		String theUrl = anItem.getItem().getAsString();
		String responseString = null;

		if(theUrl != null && !theUrl.isEmpty()) {
			OpenHABCommand theCommand = null;
			try {
				if(anItem.getItem().isJsonObject())
					theCommand = new Gson().fromJson(anItem.getItem(), OpenHABCommand.class);
				else
					theCommand = new Gson().fromJson(anItem.getItem().getAsString().replaceAll("^\"|\"$", ""), OpenHABCommand.class);
			} catch(Exception e) {
    			log.warn("Cannot parse command to OpenHAB <<<" + theUrl + ">>>", e);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "/state", null, null).getTheErrors(), HueError[].class);
				return responseString;
			}
			String intermediate = theCommand.getUrl().substring(theCommand.getUrl().indexOf("://") + 3);
			String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
			String theUrlBody = intermediate.substring(intermediate.indexOf('/') + 1);
			String hostAddr = null;
			if (hostPortion.contains(":")) {
				hostAddr = hostPortion.substring(0, intermediate.indexOf(':'));
			} else
				hostAddr = hostPortion;
			OpenHABInstance theHandler = findHandlerByAddress(hostAddr);
			if(theHandler != null) {
				String anUrl = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody,
						intensity, targetBri, targetBriInc, false);
				if (colorData != null) {
					anUrl = ColorDecode.replaceColorData(anUrl, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);	
				}
				anUrl = DeviceDataDecode.replaceDeviceData(anUrl, device);
				anUrl = TimeDecode.replaceTimeValue(anUrl);
		
				String aCommand = null;
				if(theCommand.getCommand() != null && !theCommand.getCommand().isEmpty()) {
					aCommand = BrightnessDecode.calculateReplaceIntensityValue(theCommand.getCommand(),
							intensity, targetBri, targetBriInc, false);
					if (colorData != null) {
						aCommand = ColorDecode.replaceColorData(aCommand, colorData, BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc), false);	
					}
					aCommand = DeviceDataDecode.replaceDeviceData(aCommand, device);
					aCommand = TimeDecode.replaceTimeValue(aCommand);
				}
		   		try {
		   			boolean success = theHandler.callCommand(anUrl, aCommand, httpClient);
		   			if(!success) {
		    			log.warn("Comand had error to OpenHAB");
						responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
								"Error on calling url to change device state", "/lights/"
								+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		   			}
		   		} catch (Exception e) {
	    			log.warn("Cannot send comand to OpenHAB", e);
					responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
							"Error on calling url to change device state", "/lights/"
							+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		    	}
			} else {
				log.warn("OpenHAB Call could not complete, no address found: " + theUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "state", null, null).getTheErrors(), HueError[].class);
			}
		} else {
			log.warn("OpenHAB Call to be presented as http(s)://<ip_address>(:<port>)/payload, format of request unknown: " + theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"Error on calling url to change device state", "/lights/"
					+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		}
		return responseString;
	}

	@Override
	public Object getItems(String type) {

		if(!validOpenhab)
			return null;
		log.debug("consolidating devices for OpenHAB");
		List<OpenHABDevice> theResponse = null;
		Iterator<String> keys = openhabMap.keySet().iterator();
		List<OpenHABDevice> deviceList = new ArrayList<OpenHABDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = openhabMap.get(key).getDevices(httpClient);
			if(theResponse != null)
				addOpenhabDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get devices for OpenHAB with name: " + key + ", skipping this OpenHAB.");
				continue;
			}
		}
		return deviceList;
	}
	
	private Boolean addOpenhabDevices(List<OpenHABDevice> theDeviceList, List<OpenHABDevice> theSourceList, String theKey) {
		Iterator<OpenHABDevice> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			OpenHABDevice theDevice = devices.next();
			theDeviceList.add(theDevice);
		}
		return true;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		openhabMap = null;
		validOpenhab = bridgeSettings.getBridgeSettingsDescriptor().isValidOpenhab();
		log.info("OpenHAB Home created." + (validOpenhab ? "" : " No OpenHABs configured."));
		if(validOpenhab) {
			openhabMap = new HashMap<String,OpenHABInstance>();
	        httpClient = HTTPHome.getHandler();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getOpenhabaddress().getDevices().iterator();
			while(theList.hasNext() && validOpenhab) {
				NamedIP anOpenhab = theList.next();
		      	try {
		      		openhabMap.put(anOpenhab.getName(), new OpenHABInstance(anOpenhab));
				} catch (Exception e) {
			        log.error("Cannot get OpenHAB (" + anOpenhab.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
			        validOpenhab = false;
				}
			}
        }
		return this;
	}

	private OpenHABInstance findHandlerByAddress(String hostAddress) {
		OpenHABInstance aHandler = null;
		boolean found = false;
		Iterator<String> keys = openhabMap.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			aHandler = openhabMap.get(key);
			if(aHandler != null && aHandler.getOpenHABAddress().getIp().equals(hostAddress)) {
				found = true;
				break;
			}	
		}
		if(!found)
			aHandler = null;
		return aHandler;
	}

	@Override
	public void closeHome() {
		log.debug("Closing Home.");
		if(!closed && validOpenhab) {
			log.debug("Home is already closed....");
			return;
		}

		if(httpClient != null)
			httpClient.closeHandler();

		openhabMap = null;
		closed = true;		
	}
	
	@Override
	public void refresh() {
		// noop		
	}
}
