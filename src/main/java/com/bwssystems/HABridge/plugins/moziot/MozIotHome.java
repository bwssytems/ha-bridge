package com.bwssystems.HABridge.plugins.moziot;

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

public class MozIotHome implements Home  {
    private static final Logger log = LoggerFactory.getLogger(MozIotHome.class);
	private Map<String, MozIotInstance> moziotMap;
	private Boolean validMoziot;
    private HTTPHandler httpClient;
	private boolean closed;
	
	public MozIotHome(BridgeSettings bridgeSettings) {
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
			MozIotCommand theCommand = null;
			try {
				theUrl = theUrl.replaceAll("^\"|\"$", "");
				theCommand = new Gson().fromJson(theUrl, MozIotCommand.class);
			} catch(Exception e) {
    			log.warn("Cannot parse command to Mozilla IOT <<<" + theUrl + ">>>", e);
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
			MozIotInstance theHandler = findHandlerByAddress(hostAddr);
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
		    			log.warn("Comand had error to Mozilla IOT");
						responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
								"Error on calling url to change device state", "/lights/"
								+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		   			}
		   		} catch (Exception e) {
	    			log.warn("Cannot send comand to Mozilla IOT", e);
					responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
							"Error on calling url to change device state", "/lights/"
							+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		    	}
			} else {
				log.warn("Mozilla IOT Call could not complete, no address found: " + theUrl);
				responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
						"Error on calling url to change device state", "/lights/"
						+ lightId + "state", null, null).getTheErrors(), HueError[].class);
			}
		} else {
			log.warn("Mozilla IOT Call to be presented as http(s)://<ip_address>(:<port>)/payload, format of request unknown: " + theUrl);
			responseString = new Gson().toJson(HueErrorResponse.createResponse("6", "/lights/" + lightId,
					"Error on calling url to change device state", "/lights/"
					+ lightId + "state", null, null).getTheErrors(), HueError[].class);
		}
		return responseString;
	}

	@Override
	public Object getItems(String type) {

		if(!validMoziot)
			return null;
		log.debug("consolidating devices for Mozilla IOT");
		List<MozillaThing> theResponse = null;
		Iterator<String> keys = moziotMap.keySet().iterator();
		List<MozillaThing> deviceList = new ArrayList<MozillaThing>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = moziotMap.get(key).getDevices(httpClient);
			if(theResponse != null)
				addMozIotDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get devices for Mozilla IOT with name: " + key + ", skipping this Mozilla IOT.");
				continue;
			}
		}
		return deviceList;
	}
	
	private Boolean addMozIotDevices(List<MozillaThing> theDeviceList, List<MozillaThing> theSourceList, String theKey) {
		Iterator<MozillaThing> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			MozillaThing theDevice = devices.next();
			theDeviceList.add(theDevice);
		}
		return true;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		moziotMap = null;
		validMoziot = bridgeSettings.getBridgeSettingsDescriptor().isValidMozIot();
		log.info("Mozilla IOT Home created." + (validMoziot ? "" : " No Mozilla IOTs configured."));
		if(validMoziot) {
			moziotMap = new HashMap<String,MozIotInstance>();
	        httpClient = HTTPHome.getHandler();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getMoziotaddress().getDevices().iterator();
			while(theList.hasNext() && validMoziot) {
				NamedIP aMoziot = theList.next();
		      	try {
		      		moziotMap.put(aMoziot.getName(), new MozIotInstance(aMoziot, httpClient));
				} catch (Exception e) {
			        log.error("Cannot get Mozilla IOT (" + aMoziot.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
			        validMoziot = false;
				}
			}
        }
		return this;
	}

	private MozIotInstance findHandlerByAddress(String hostAddress) {
		MozIotInstance aHandler = null;
		boolean found = false;
		Iterator<String> keys = moziotMap.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			aHandler = moziotMap.get(key);
			if(aHandler != null && aHandler.getMozIotIP().getIp().equals(hostAddress)) {
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
		if(!closed && validMoziot) {
			log.debug("Home is already closed....");
			return;
		}

		if(httpClient != null)
			httpClient.closeHandler();

		moziotMap = null;
		closed = true;		
	}
	
	@Override
	public void refresh() {
		// noop		
	}
}
