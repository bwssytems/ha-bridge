package com.bwssystems.HABridge.plugins.fhem;

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

public class FHEMHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(FHEMHome.class);
	private Map<String, FHEMInstance> fhemMap;
	private Boolean validFhem;
    private HTTPHandler httpClient;
	private boolean closed;

	public FHEMHome(BridgeSettings bridgeSettings) {
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
			FHEMCommand theCommand = null;
			try {
				theCommand = new Gson().fromJson(theUrl, FHEMCommand.class);
			} catch(Exception e) {
    			log.warn("Cannot parse command to FHEM <<<" + theUrl + ">>>", e);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "state", null, null).getTheErrors(), HueError[].class);
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
			FHEMInstance theHandler = findHandlerByAddress(hostAddr);
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
		   			theHandler.callCommand(anUrl, aCommand, httpClient);
		   		} catch (Exception e) {
	    			log.warn("Cannot send comand to FHEM", e);
					responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
							"Error on calling url to change device state", "/lights/"
							+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		    	}
			} else {
				log.warn("FHEM Call could not complete, no address found: " + theUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "state", null, null).getTheErrors(), HueError[].class);
			}
		} else {
			log.warn("FHEM Call to be presented as http(s)://<ip_address>(:<port>)/payload, format of request unknown: " + theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"Error on calling url to change device state", "/lights/"
					+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		}
		return responseString;
	}

	private FHEMInstance findHandlerByAddress(String hostAddress) {
		FHEMInstance aHandler = null;
		boolean found = false;
		Iterator<String> keys = fhemMap.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			aHandler = fhemMap.get(key);
			if(aHandler != null && aHandler.getFhemAddress().getIp().equals(hostAddress)) {
				found = true;
				break;
			}	
		}
		if(!found)
			aHandler = null;
		return aHandler;
	}

	@Override
	public Object getItems(String type) {

		if(!validFhem)
			return null;
		log.debug("consolidating devices for java.lang.String");
		List<FHEMDevice> theResponse = null;
		Iterator<String> keys = fhemMap.keySet().iterator();
		List<FHEMDevice> deviceList = new ArrayList<FHEMDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = fhemMap.get(key).getDevices(httpClient);
			if(theResponse != null)
				addFHEMDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get devices for FHEM: " + key + ", skipping this FHEM.");
				continue;
			}
		}
		return deviceList;
	}
	
	private Boolean addFHEMDevices(List<FHEMDevice> theDeviceList, List<FHEMDevice> theSourceList, String theKey) {
		Iterator<FHEMDevice> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			FHEMDevice theDevice = devices.next();
			theDeviceList.add(theDevice);
		}
		return true;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		fhemMap = null;
		validFhem = bridgeSettings.getBridgeSettingsDescriptor().isValidOpenhab();
		log.info("FHEM Home created." + (validFhem ? "" : " No FHEMs configured."));
		if(validFhem) {
			fhemMap = new HashMap<String,FHEMInstance>();
	        httpClient = HTTPHome.getHandler();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getOpenhabaddress().getDevices().iterator();
			while(theList.hasNext() && validFhem) {
				NamedIP aFhem = theList.next();
		      	try {
		      		fhemMap.put(aFhem.getName(), new FHEMInstance(aFhem));
				} catch (Exception e) {
			        log.error("Cannot get FHEM (" + aFhem.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
			        validFhem = false;
				}
			}
        }
		return this;
	}

	@Override
	public void closeHome() {
		log.debug("Closing Home.");
		if(!closed && validFhem) {
			log.debug("Home is already closed....");
			return;
		}

		if(httpClient != null)
			httpClient.closeHandler();

		fhemMap = null;
		closed = true;		
	}

}
