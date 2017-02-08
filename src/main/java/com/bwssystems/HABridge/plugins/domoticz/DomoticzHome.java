package com.bwssystems.HABridge.plugins.domoticz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.MultiCommandUtil;

public class DomoticzHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(DomoticzHome.class);
	private Map<String, DomoticzHandler> domoticzs;
	private Boolean validDomoticz;

	public DomoticzHome(BridgeSettingsDescriptor bridgeSettings) {
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
			theResponse = domoticzs.get(key).getDevices();
			if(theResponse != null)
				addDomoticzDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get lights for Domoticz with name: " + key + ", skipping this Domoticz.");
				continue;
			}
			theResponse = domoticzs.get(key).getScenes();
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
			Integer targetBri,Integer targetBriInc, DeviceDescriptor device, String body) {
		// Not a device handler
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		validDomoticz = bridgeSettings.isValidDomoticz();
		log.info("Domoticz Home created." + (validDomoticz ? "" : " No Domoticz devices configured."));
		if(!validDomoticz)
			return null;
		domoticzs = new HashMap<String, DomoticzHandler>();
		Iterator<NamedIP> theList = bridgeSettings.getDomoticzaddress().getDevices().iterator();
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

	@Override
	public void closeHome() {
		// noop
		
	}
}
