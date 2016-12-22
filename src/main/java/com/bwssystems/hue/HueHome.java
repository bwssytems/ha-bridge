package com.bwssystems.hue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.hue.MultiCommandUtil;

public class HueHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HueHome.class);
	private Map<String, HueInfo> hues;
	private String theHUERegisteredUser;
	private Boolean validHue;
	
	public HueHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for hues");
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

	public String getTheHUERegisteredUser() {
		return theHUERegisteredUser;
	}

	public void setTheHUERegisteredUser(String theHUERegisteredUser) {
		this.theHUERegisteredUser = theHUERegisteredUser;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc) {
		// TODO Auto-generated method stub
		log.info("device handler not implemented");
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		validHue = bridgeSettings.isValidHue();
		if(!validHue) {
			log.debug("No Hue Configuration");
		} else {
			hues = new HashMap<String, HueInfo>();
			Iterator<NamedIP> theList = bridgeSettings.getHueaddress().getDevices().iterator();
			while(theList.hasNext()) {
				NamedIP aHue = theList.next();
	      		hues.put(aHue.getName(), new HueInfo(aHue, this));
			}
			theHUERegisteredUser = null;
		}
		return this;
	}

	@Override
	public void closeHome() {
		// TODO Auto-generated method stub
		
	}
}
