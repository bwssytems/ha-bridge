package com.bwssystems.hass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.NamedIP;

public class HassHome {
    private static final Logger log = LoggerFactory.getLogger(HassHome.class);
	private Map<String, HomeAssistant> hassMap;
	
	public HassHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		hassMap = new HashMap<String,HomeAssistant>();
		if(!bridgeSettings.isValidHass())
			return;
		Iterator<NamedIP> theList = bridgeSettings.getHassaddress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHass = theList.next();
	      	try {
	      		hassMap.put(aHass.getName(), new HomeAssistant(aHass));
			} catch (Exception e) {
		        log.error("Cannot get hass (" + aHass.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
		        return;
			}
		}
	}

	public List<HassDevice> getDevices() {
		log.debug("consolidating devices for hues");
		List<State> theResponse = null;
		Iterator<String> keys = hassMap.keySet().iterator();
		List<HassDevice> deviceList = new ArrayList<HassDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = hassMap.get(key).getDevices();
			if(theResponse != null)
				addHassDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get devices for HomeAssistant with name: " + key + ", skipping this Hass.");
				continue;
			}
		}
		return deviceList;
	}

	private Boolean addHassDevices(List<HassDevice> theDeviceList, List<State> theSourceList, String theKey) {
		Iterator<State> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			State theDevice = devices.next();
			HassDevice aNewHassDevice = new HassDevice();
			aNewHassDevice.setDeviceState(theDevice);
			aNewHassDevice.setHassaddress(hassMap.get(theKey).getHassAddress().getIp());
			aNewHassDevice.setHassname(theKey);
			aNewHassDevice.setDeviceName(theDevice.getAttributes().get("friendly_name").getAsString());
			aNewHassDevice.setDomain(theDevice.getEntityId().substring(0, theDevice.getEntityId().indexOf(".")));
			theDeviceList.add(aNewHassDevice);
		}
		return true;
	}
}
