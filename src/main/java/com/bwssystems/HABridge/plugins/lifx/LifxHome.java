package com.bwssystems.HABridge.plugins.lifx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.github.besherman.lifx.LFXClient;
import com.github.besherman.lifx.LFXGroup;
import com.github.besherman.lifx.LFXGroupCollection;
import com.github.besherman.lifx.LFXLight;
import com.github.besherman.lifx.LFXLightCollection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LifxHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(LifxHome.class);
	private Map<String, LifxDevice> lifxMap;
	private LFXClient client = new LFXClient();        
	private Boolean validLifx;
	private Gson aGsonHandler;
	
	public LifxHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		lifxMap = null;
		aGsonHandler = null;
		validLifx = bridgeSettings.isValidLifx();
		log.info("LifxDevice Home created." + (validLifx ? "" : " No LifxDevices configured."));
		if(validLifx) {
	    	try {
	    		log.info("Open Lifx client....");
				client.open(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			aGsonHandler =
					new GsonBuilder()
					.create();
			lifxMap = new HashMap<String, LifxDevice>();
			this.addLifxLights(client.getLights());
			this.addLifxGroups(client.getGroups());
        }
		return this;
	}

	public LifxDevice getLifxDevice(String aName) {
		if(!validLifx)
			return null;
		LifxDevice aLifxDevice = null;
		if(aName == null || aName.equals("")) {
			log.debug("Cannot get LifxDevice for name as it is empty.");
		}
		else {
			aLifxDevice = lifxMap.get(aName);
			log.debug("Retrieved a LifxDevice for name: " + aName);
		}
		return aLifxDevice;
	}
	
	@Override
	public Object getItems(String type) {
		log.debug("consolidating devices for lifx");
		if(!validLifx)
			return null;
		LifxEntry theResponse = null;
		Iterator<String> keys = lifxMap.keySet().iterator();
		List<LifxEntry> deviceList = new ArrayList<LifxEntry>();
		while(keys.hasNext()) {
			String key = keys.next();
			theResponse = lifxMap.get(key).toEntry();
			if(theResponse != null)
				deviceList.add(theResponse);
			else {
				log.warn("Cannot get LifxDevice with name: " + key + ", skipping this Lifx.");
				continue;
			}
		}
		return deviceList;
	}

	private Boolean addLifxLights(LFXLightCollection theDeviceList) {
		if(!validLifx)
			return false;
		Iterator<LFXLight> devices = theDeviceList.iterator();;
		while(devices.hasNext()) {
			LFXLight theDevice = devices.next();
			LifxDevice aNewLifxDevice = new LifxDevice(theDevice, LifxDevice.LIGHT_TYPE);
			lifxMap.put(aNewLifxDevice.toEntry().getName(), aNewLifxDevice);
		}
		return true;
	}
	
	private Boolean addLifxGroups(LFXGroupCollection theDeviceList) {
		if(!validLifx)
			return false;
		Iterator<LFXGroup> devices = theDeviceList.iterator();;
		while(devices.hasNext()) {
			LFXGroup theDevice = devices.next();
			LifxDevice aNewLifxDevice = new LifxDevice(theDevice, LifxDevice.GROUP_TYPE);
			lifxMap.put(aNewLifxDevice.toEntry().getName(), aNewLifxDevice);
		}
		return true;
	}
	
	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, DeviceDescriptor device, String body) {
		String theReturn = null;
		log.debug("executing HUE api request to send message to LifxDevice: " + anItem.getItem().toString());
		if(!validLifx) {
			log.warn("Should not get here, no LifxDevice clients configured");
			theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no LifxDevices configured\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";

		} else {
			LifxEntry lifxCommand = null;
			if(anItem.getItem().isJsonObject())
				lifxCommand = aGsonHandler.fromJson(anItem.getItem(), LifxEntry.class);
			else
				lifxCommand = aGsonHandler.fromJson(anItem.getItem().getAsString(), LifxEntry.class);
			int aBriValue = BrightnessDecode.calculateIntensity(intensity, targetBri, targetBriInc);
			LifxDevice theDevice = getLifxDevice(lifxCommand.getName());
			if (theDevice == null) {
				log.warn("Should not get here, no LifxDevices available");
				theReturn = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Should not get here, no Lifx clients available\", \"parameter\": \"/lights/"
						+ lightId + "state\"}}]";
			} else {
					log.debug("calling LifxDevice: " + lifxCommand.getName());
					if(theDevice.getType().equals(LifxDevice.LIGHT_TYPE)) {
						LFXLight theLight = (LFXLight)theDevice.getLifxObject();
						if(body.contains("true"))
							theLight.setPower(true);
						if(body.contains("false"))
							theLight.setPower(false);
						if(targetBri != null || targetBriInc != null)
							theLight.setBrightness((float)(aBriValue/254));
					} else if (theDevice.getType().equals(LifxDevice.GROUP_TYPE)) {
						LFXGroup theGroup = (LFXGroup)theDevice.getLifxObject();
						if(body.contains("true"))
							theGroup.setPower(true);
						if(body.contains("false"))
							theGroup.setPower(false);
					}
			}
		}
		return theReturn;
	}

	@Override
	public void closeHome() {
		if(!validLifx)
			return;
		client.close();
	}
}
