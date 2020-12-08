package com.bwssystems.HABridge.plugins.hass;

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
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class HassHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HassHome.class);
	private Map<String, HomeAssistant> hassMap;
	private Boolean validHass;
	private Gson aGsonHandler;
	private boolean closed;
	
	public HassHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		hassMap = null;
		aGsonHandler = null;
		validHass = bridgeSettings.getBridgeSettingsDescriptor().isValidHass();
		log.info("HomeAssistant Home created." + (validHass ? "" : " No HomeAssistants configured."));
		if(validHass) {
			hassMap = new HashMap<String,HomeAssistant>();
			aGsonHandler =
					new GsonBuilder()
					.create();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getHassaddress().getDevices().iterator();
			while(theList.hasNext() && validHass) {
				NamedIP aHass = theList.next();
		      	try {
		      		hassMap.put(aHass.getName(), new HomeAssistant(aHass));
				} catch (Exception e) {
			        log.error("Cannot get hass (" + aHass.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
			        validHass = false;
				}
			}
        }
		return this;
	}

	public HomeAssistant getHomeAssistant(String aName) {
		if(!validHass)
			return null;
		HomeAssistant aHomeAssistant;
		if(aName == null || aName.equals("")) {
			aHomeAssistant = null;
			log.debug("Cannot get HomeAssistant for name as it is empty.");
		}
		else {
			aHomeAssistant = hassMap.get(aName);
			log.debug("Retrieved a HomeAssistant for name: " + aName);
		}
		return aHomeAssistant;
	}
	
	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for hass");
		if(!validHass)
			return null;
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
		if(!validHass)
			return false;
		Iterator<State> devices = theSourceList.iterator();
		while(devices.hasNext()) {
			State theDevice = devices.next();
			HassDevice aNewHassDevice = new HassDevice();
			aNewHassDevice.setDeviceState(theDevice);
			aNewHassDevice.setHassaddress(hassMap.get(theKey).getHassAddress().getIp());
			aNewHassDevice.setHassname(theKey);
			JsonElement friendlyName = theDevice.getAttributes().get("friendly_name");
			if(friendlyName == null)
				aNewHassDevice.setDeviceName(theDevice.getEntityId());
			else
				aNewHassDevice.setDeviceName(friendlyName.getAsString());
			aNewHassDevice.setDomain(theDevice.getEntityId().substring(0, theDevice.getEntityId().indexOf(".")));
			theDeviceList.add(aNewHassDevice);
		}
		return true;
	}
	
	@Override
	public void refresh() {
		// noop		
	}
	
	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String theReturn = null;
		log.debug("executing HUE api request to send message to HomeAssistant: " + anItem.getItem().toString());
		if(!validHass) {
			log.warn("Should not get here, no HomeAssistant clients configured");
			theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no HomeAssistants configured\", \"parameter\": \"/lights/"
					+ lightId + "/state\"}}]";

		} else {
			HassCommand hassCommand = null;
			if(anItem.getItem().isJsonObject())
				hassCommand = aGsonHandler.fromJson(anItem.getItem(), HassCommand.class);
			else
				hassCommand = aGsonHandler.fromJson(anItem.getItem().getAsString().replaceAll("^\"|\"$", ""), HassCommand.class);
			hassCommand.setBri(BrightnessDecode.calculateReplaceIntensityValue(hassCommand.getBri(), intensity, targetBri, targetBriInc, false));
			HomeAssistant homeAssistant = getHomeAssistant(hassCommand.getHassName());
			if (homeAssistant == null) {
				log.warn("Should not get here, no HomeAssistants available");
				theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Should not get here, no HiomeAssistant clients available\", \"parameter\": \"/lights/"
						+ lightId + "/state\"}}]";
			} else {
					log.debug("calling HomeAssistant: " + hassCommand.getHassName() + " - "
							+ hassCommand.getEntityId() + " - " + hassCommand.getState() + " - " + hassCommand.getBri());
					homeAssistant.callCommand(hassCommand);
			}
		}
		return theReturn;
	}

	@Override
	public void closeHome() {
		if(!validHass)
			return;
		log.debug("Closing Home.");
		if(closed) {
			log.debug("Home is already closed....");
			return;
		}
		if(hassMap == null)
			return;
		Iterator<String> keys = hassMap.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			hassMap.get(key).closeClient();
		}
		
		hassMap = null;
		closed = true;
	}
}
