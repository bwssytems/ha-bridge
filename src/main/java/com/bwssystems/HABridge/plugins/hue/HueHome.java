package com.bwssystems.HABridge.plugins.hue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HueHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HueHome.class);
	private Map<String, HueInfo> hues;
	private Boolean validHue;
	private Gson aGsonHandler;
	private BridgeSettings theBridgeSettings;
	
	public HueHome(BridgeSettings bridgeSettings) {
		super();
		theBridgeSettings = bridgeSettings;
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for hues");
		if(!validHue)
			return null;
		Iterator<String> keys = hues.keySet().iterator();
		ArrayList<HueDevice> deviceList = new ArrayList<HueDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			HueApiResponse theResponse = hues.get(key).getHueApiResponse();
			if(theResponse != null) {
				Map<String, DeviceResponse> theDevices = theResponse.getLights();
				if(theDevices != null) {
					Iterator<String> deviceKeys = theDevices.keySet().iterator();
					while(deviceKeys.hasNext()) {
						String theDeviceKey = deviceKeys.next();
						HueDevice aNewHueDevice = new HueDevice();
						aNewHueDevice.setDevice(theDevices.get(theDeviceKey));
						aNewHueDevice.setHuedeviceid(theDeviceKey);
						aNewHueDevice.setHueaddress(hues.get(key).getHueAddress().getIp());
						aNewHueDevice.setHuename(key);
						deviceList.add(aNewHueDevice);
					}
				}
				else {
					deviceList = null;
					break;
				}
			}
			else
				log.warn("Cannot get lights for Hue with name: " + key);
		}
		return deviceList;
	}

	public DeviceResponse getHueDeviceInfo(CallItem anItem, DeviceDescriptor device) {
		if(!validHue)
			return null;
		HueDeviceIdentifier deviceId = null;
		if(anItem.getItem().isJsonObject())
			deviceId = aGsonHandler.fromJson(anItem.getItem(), HueDeviceIdentifier.class);
		else
			deviceId = aGsonHandler.fromJson(anItem.getItem().getAsString(), HueDeviceIdentifier.class);
		if(deviceId.getHueName() == null || deviceId.getHueName().isEmpty())
			deviceId.setHueName(device.getTargetDevice());
		
		DeviceResponse deviceResponse = null;
		HueInfo aHueInfo = hues.get(device.getTargetDevice());
		deviceResponse = aHueInfo.getHueDeviceInfo(deviceId.getDeviceId(), device);
		return deviceResponse;
	}
	
	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		if(!validHue)
			return null;
		String responseString = null;
		HueDeviceIdentifier deviceId = null;
		if(anItem.getItem().isJsonObject())
			deviceId = aGsonHandler.fromJson(anItem.getItem(), HueDeviceIdentifier.class);
		else
			deviceId = aGsonHandler.fromJson(anItem.getItem().getAsString(), HueDeviceIdentifier.class);
		if(deviceId.getHueName() == null || deviceId.getHueName().isEmpty())
			deviceId.setHueName(device.getTargetDevice());
		
		HueInfo theHue = hues.get(deviceId.getHueName());

		// make call
		responseString = theHue.changeState(deviceId, lightId, body);

		return responseString;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		validHue = bridgeSettings.getBridgeSettingsDescriptor().isValidHue();
		log.info("Hue passthru Home created." + (validHue ? "" : " No Hue passtrhu systems configured."));
		if(validHue) {
			hues = new HashMap<String, HueInfo>();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getHueaddress().getDevices().iterator();
			while(theList.hasNext()) {
				NamedIP aHue = theList.next();
	      		hues.put(aHue.getName(), new HueInfo(aHue, this));
			}
			aGsonHandler = new GsonBuilder().create();
		}
		return this;
	}
	
	protected void updateHue(NamedIP aHue) {
		theBridgeSettings.getBridgeSettingsDescriptor().updateHue(aHue);
		if(theBridgeSettings.getBridgeSettingsDescriptor().isSettingsChanged()) {
			theBridgeSettings.updateConfigFile();
		}
	}

	@Override
	public void closeHome() {
		if(!validHue)
			return;
		if(hues == null)
			return;
		Iterator<String> keys = hues.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			hues.get(key).closeHue();;
		}
		hues = null;
	}
}
