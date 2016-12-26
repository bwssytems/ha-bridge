package com.bwssystems.hal;

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
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.MultiCommandUtil;

public class HalHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HalHome.class);
	private Map<String, HalInfo> hals;

	public HalHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for hues");
		List<HalDevice> theResponse = null;
		Iterator<String> keys = hals.keySet().iterator();
		List<HalDevice> deviceList = new ArrayList<HalDevice>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = hals.get(key).getLights();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else {
				log.warn("Cannot get lights for Hal with name: " + key + ", skipping this HAL.");
				continue;
			}
			theResponse = hals.get(key).getAppliances();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get appliances for Hal with name: " + key);
			theResponse = hals.get(key).getTheatre();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get theatre for Hal with name: " + key);
			theResponse = hals.get(key).getCustom();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get custom for Hal with name: " + key);
			theResponse = hals.get(key).getHVAC();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get HVAC for Hal with name: " + key);
			theResponse = hals.get(key).getHome(key);
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Homes for Hal with name: " + key);
			theResponse = hals.get(key).getGroups();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Groups for Hal with name: " + key);
			theResponse = hals.get(key).getMacros();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Macros for Hal with name: " + key);
			theResponse = hals.get(key).getScenes();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Scenes for Hal with name: " + key);
			theResponse = hals.get(key).getButtons();
			if(theResponse != null)
				addHalDevices(deviceList, theResponse, key);
			else
				log.warn("Cannot get Buttons for Hal with name: " + key);
		}
		return deviceList;
	}
	
	private Boolean addHalDevices(List<HalDevice> theDeviceList, List<HalDevice> theSourceList, String theKey) {
		Iterator<HalDevice> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			HalDevice theDevice = devices.next();
			HalDevice aNewHalDevice = new HalDevice();
			aNewHalDevice.setHaldevicetype(theDevice.getHaldevicetype());
			aNewHalDevice.setHaldevicename(theDevice.getHaldevicename());
			aNewHalDevice.setButtons(theDevice.getButtons());
			aNewHalDevice.setHaladdress(hals.get(theKey).getHalAddress().getIp());
			aNewHalDevice.setHalname(theKey);
			theDeviceList.add(aNewHalDevice);
		}
		return true;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc, DeviceDescriptor device, String body) {
		log.info("device handler not implemented");
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		hals = new HashMap<String, HalInfo>();
		if(!bridgeSettings.isValidHal())
			return null;
		Iterator<NamedIP> theList = bridgeSettings.getHaladdress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHal = theList.next();
	      	try {
	      		hals.put(aHal.getName(), new HalInfo(aHal, bridgeSettings.getHaltoken()));
			} catch (Exception e) {
		        log.error("Cannot get hal client (" + aHal.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
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
