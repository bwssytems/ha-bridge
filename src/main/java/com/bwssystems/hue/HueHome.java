package com.bwssystems.hue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.hue.DeviceResponse;

public class HueHome {
    private static final Logger log = LoggerFactory.getLogger(HueHome.class);
	private Map<String, HueInfo> hues;
	
	public HueHome(BridgeSettingsDescriptor bridgeSettings) {
		hues = new HashMap<String, HueInfo>();
		if(!bridgeSettings.isValidHue())
			return;
		Iterator<NamedIP> theList = bridgeSettings.getVeraAddress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHue = theList.next();
      		hues.put(aHue.getName(), new HueInfo(aHue));
		}
	}

	public List<HueDevice> getDevices() {
		log.debug("consolidating devices for hues");
		Iterator<String> keys = hues.keySet().iterator();
		ArrayList<HueDevice> deviceList = new ArrayList<HueDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			Map<String, DeviceResponse> theDevices = hues.get(key).getHueApiResponse().getLights();
			if(theDevices != null) {
				Iterator<String> deviceKeys = theDevices.keySet().iterator();
				while(deviceKeys.hasNext()) {
					HueDevice aNewHueDevice = new HueDevice();
					aNewHueDevice.setDevice(theDevices.get(deviceKeys.next()));
					aNewHueDevice.setHubaddress("");
					deviceList.add(aNewHueDevice);
				}
			}
			else {
				deviceList = null;
				break;
			}
		}
		return deviceList;
	}
}
