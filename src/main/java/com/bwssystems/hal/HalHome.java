package com.bwssystems.hal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.NamedIP;

public class HalHome {
    private static final Logger log = LoggerFactory.getLogger(HalHome.class);
	private Map<String, HalInfo> hals;

	public HalHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		hals = new HashMap<String, HalInfo>();
		if(!bridgeSettings.isValidHal())
			return;
		Iterator<NamedIP> theList = bridgeSettings.getHaladdress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHal = theList.next();
	      	try {
	      		hals.put(aHal.getName(), new HalInfo(aHal, bridgeSettings.getHaltoken()));
			} catch (Exception e) {
		        log.error("Cannot get harmony client (" + aHal.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
		        return;
			}
		}
	}

	public List<HalDevice> getDevices() {
		log.debug("consolidating devices for hues");
		Iterator<String> keys = hals.keySet().iterator();
		List<HalDevice> deviceList = new ArrayList<HalDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			List<HalDevice> theResponse = hals.get(key).getLights();
			if(theResponse != null) {
					Iterator<HalDevice> devices = theResponse.iterator();
					while(devices.hasNext()) {
						HalDevice theDevice = devices.next();
						HalDevice aNewHalDevice = new HalDevice();
						aNewHalDevice.setHaldevicetype(theDevice.getHaldevicetype());
						aNewHalDevice.setHaldevicename(theDevice.getHaldevicename());
						aNewHalDevice.setHaladdress(hals.get(key).getHalAddress().getIp());
						aNewHalDevice.setHalname(key);
						deviceList.add(aNewHalDevice);
					}
			}
			else
				log.warn("Cannot get lights for Hal with name: " + key);
		}
		return deviceList;
	}
}
