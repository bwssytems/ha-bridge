package com.bwssystems.HABridge.plugins.domoticz;

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
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class DomoticzHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(DomoticzHome.class);
	private Map<String, DomoticzHandler> domoticzs;
	private Boolean validDomoticz;
    private HTTPHandler httpClient;

	public DomoticzHome(BridgeSettings bridgeSettings) {
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
			theResponse = domoticzs.get(key).getDevices(httpClient);
			if(theResponse != null)
				addDomoticzDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get lights for Domoticz with name: " + key + ", skipping this Domoticz.");
				continue;
			}
			theResponse = domoticzs.get(key).getScenes(httpClient);
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
			theDeviceList.add(theDevice);
		}
		return true;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		Devices theDomoticzApiResponse = null;
		String responseString = null;
		
		String theUrl = anItem.getItem().getAsString();
		if(theUrl != null && !theUrl.isEmpty () && (theUrl.startsWith("http://") || theUrl.startsWith("https://"))) {
			String intermediate = theUrl.substring(theUrl.indexOf("://") + 3);
			String hostPortion = intermediate.substring(0, intermediate.indexOf('/'));
			String theUrlBody = intermediate.substring(intermediate.indexOf('/') + 1);
			String hostAddr = null;
			if (hostPortion.contains(":")) {
				hostAddr = hostPortion.substring(0, intermediate.indexOf(':'));
			} else
				hostAddr = hostPortion;
			DomoticzHandler theHandler = findHandlerByAddress(hostAddr);
			if(theHandler != null){
		    	String theData;
				String anUrl = BrightnessDecode.calculateReplaceIntensityValue(theUrlBody,
						intensity, targetBri, targetBriInc, false);
		   		theData = httpClient.doHttpRequest(theHandler.buildUrl(anUrl), null, null, null, theHandler.buildHeaders());
		   		try {
		   			theDomoticzApiResponse = new Gson().fromJson(theData, Devices.class);
		   			if(theDomoticzApiResponse.getStatus().equals("OK"))
		   				responseString = null;
		   			else {
		    			log.warn("Call failed for Domoticz " + theHandler.getDomoticzAddress().getName() + " with status " + theDomoticzApiResponse.getStatus() + " for item " + theDomoticzApiResponse.getTitle());
						responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
								"Error on calling url to change device state", "/lights/"
								+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		   			}
		   		} catch (Exception e) {
	    			log.warn("Cannot interrpret result from call for Domoticz " + theHandler.getDomoticzAddress().getName() + " as response is not parsable.");
					responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
							"Error on calling url to change device state", "/lights/"
							+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		    	}
			} else {
				log.warn("Domoticz Call could not complete, no address found: " + theUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "state", null, null).getTheErrors(), HueError[].class);
			}
		} else {
			log.warn("Domoticz Call to be presented as http(s)://<ip_address>(:<port>)/payload, format of request unknown: " + theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"Error on calling url to change device state", "/lights/"
					+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		validDomoticz = bridgeSettings.getBridgeSettingsDescriptor().isValidDomoticz();
		log.info("Domoticz Home created." + (validDomoticz ? "" : " No Domoticz devices configured."));
		if(!validDomoticz)
			return null;
        httpClient = new HTTPHandler();
		domoticzs = new HashMap<String, DomoticzHandler>();
		Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getDomoticzaddress().getDevices().iterator();
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

	private DomoticzHandler findHandlerByAddress(String hostAddress) {
		DomoticzHandler aHandler = null;
		boolean found = false;
		Iterator<String> keys = domoticzs.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			aHandler = domoticzs.get(key);
			if(aHandler != null && aHandler.getDomoticzAddress().getIp().equals(hostAddress)) {
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
		if(httpClient != null)
			httpClient.closeHandler();
		
	}
}
