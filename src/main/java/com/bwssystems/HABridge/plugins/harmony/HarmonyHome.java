package com.bwssystems.HABridge.plugins.harmony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.IpList;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;

public class HarmonyHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(HarmonyHome.class);
	private Map<String, HarmonyServer> hubs;
	private Boolean isDevMode;
	private Boolean validHarmony;
	private Gson aGsonHandler;

	public HarmonyHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public void closeHome() {
		if(!validHarmony)
			return;
		if(isDevMode || hubs == null)
			return;
		Iterator<String> keys = hubs.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			hubs.get(key).getMyHarmony().shutdown();
		}
		
		hubs = null;
	}

	public HarmonyHandler getHarmonyHandler(String aName) {
		if(!validHarmony)
			return null;
		HarmonyHandler aHandler = null;
		if(aName == null || aName.equals("")) {
			aName = "default";
		}

		if(hubs.get(aName) == null) {
			Set<String> keys = hubs.keySet();
			if(!keys.isEmpty()) {
				aHandler = hubs.get(keys.toArray()[0]).getMyHarmony();
			}
			else
				aHandler = null;
		}
		else
			aHandler = hubs.get(aName).getMyHarmony();
		return aHandler;
	}
	
	public List<HarmonyActivity> getActivities() {
		Iterator<String> keys = hubs.keySet().iterator();
		ArrayList<HarmonyActivity> activityList = new ArrayList<HarmonyActivity>();
		if(!validHarmony)
			return null;
		while(keys.hasNext()) {
			String key = keys.next();
			Iterator<Activity> activities = hubs.get(key).getMyHarmony().getActivities().iterator();
			while(activities.hasNext()) {
				HarmonyActivity anActivity = new HarmonyActivity();
				anActivity.setActivity(activities.next());
				anActivity.setHub(key);
				activityList.add(anActivity);
			}
		}
		return activityList;
	}
	public List<HarmonyActivity> getCurrentActivities() {
		Iterator<String> keys = hubs.keySet().iterator();
		ArrayList<HarmonyActivity> activityList = new ArrayList<HarmonyActivity>();
		if(!validHarmony)
			return null;
		while(keys.hasNext()) {
			String key = keys.next();
			Activity theActivity = hubs.get(key).getMyHarmony().getCurrentActivity();
			HarmonyActivity anActivity = new HarmonyActivity();
			anActivity.setActivity(theActivity);
			anActivity.setHub(key);
			activityList.add(anActivity);
		}
		return activityList;
	}
	public List<HarmonyDevice> getDevices() {
		Iterator<String> keys = hubs.keySet().iterator();
		ArrayList<HarmonyDevice> deviceList = new ArrayList<HarmonyDevice>();
		if(!validHarmony)
			return null;
		while(keys.hasNext()) {
			String key = keys.next();
			Iterator<Device> devices = hubs.get(key).getMyHarmony().getDevices().iterator();
			while(devices.hasNext()) {
				HarmonyDevice aDevice = new HarmonyDevice();
				aDevice.setDevice(devices.next());
				aDevice.setHub(key);
				deviceList.add(aDevice);
			}
		}
		return deviceList;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String responseString = null;
		log.debug("executing HUE api request to change " + anItem.getType() + " to Harmony: " + device.getName());
		if(!validHarmony) {
			log.warn("Should not get here, no harmony configured");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no harmony configured\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";			
		} else {
			if(anItem.getType().trim().equalsIgnoreCase(DeviceMapTypes.HARMONY_ACTIVITY[DeviceMapTypes.typeIndex]))
			{
				RunActivity anActivity = null;
				if(anItem.getItem().isJsonObject())
					anActivity = aGsonHandler.fromJson(anItem.getItem(), RunActivity.class);
				else
					anActivity = aGsonHandler.fromJson(anItem.getItem().getAsString(), RunActivity.class);
				if(anActivity.getHub() == null || anActivity.getHub().isEmpty())
					anActivity.setHub(device.getTargetDevice());
				HarmonyHandler myHarmony = getHarmonyHandler(anActivity.getHub());
				if (myHarmony == null) {
					log.warn("Should not get here, no harmony hub available");
					responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
							+ "\",\"description\": \"Should not get here, no harmony hub available\", \"parameter\": \"/lights/"
							+ lightId + "state\"}}]";
				} else {
					myHarmony.startActivity(anActivity);
				}
			} else if(anItem.getType().trim().equalsIgnoreCase(DeviceMapTypes.HARMONY_BUTTON[DeviceMapTypes.typeIndex])) {
				String url = null;
				if(anItem.getItem().isJsonObject() || anItem.getItem().isJsonArray()) {
					url = aGsonHandler.toJson(anItem.getItem());
				} else
					url = anItem.getItem().getAsString();

				if (url.substring(0, 1).equalsIgnoreCase("{")) {
					url = "[" + url + "]";
				}
				
				url = BrightnessDecode.calculateReplaceIntensityValue(url, intensity, targetBri, targetBriInc, false);
				ButtonPress[] deviceButtons = aGsonHandler.fromJson(url, ButtonPress[].class);
	        	Integer theCount = 1;
        		for(int z = 0; z < deviceButtons.length; z++) {
	        		if(deviceButtons[z].getCount() != null && deviceButtons[z].getCount() > 0)
	        			theCount = deviceButtons[z].getCount();
	        		for(int y = 0; y < theCount; y++) {
	        			if( y > 0 || z > 0) {
								try {
									Thread.sleep(aMultiUtil.getTheDelay());
								} catch (InterruptedException e) {
									// ignore
								}
							}
							if (anItem.getDelay() != null && anItem.getDelay() > 0)
								aMultiUtil.setTheDelay(anItem.getDelay());
							else
								aMultiUtil.setTheDelay(aMultiUtil.getDelayDefault());
	    	        	log.debug("pressing button: " + deviceButtons[z].getDevice() + " - " + deviceButtons[z].getButton() + " with pressTime of: " + deviceButtons[z].getPressTime() + " - iteration: " + String.valueOf(z) + " - count: " + String.valueOf(y));
	    				if(deviceButtons[z].getHub() == null || deviceButtons[z].getHub().isEmpty())
	    					deviceButtons[z].setHub(device.getTargetDevice());
	    				HarmonyHandler myHarmony = getHarmonyHandler(deviceButtons[z].getHub());
	    				if (myHarmony == null)
	    					log.warn("Button Press - Should not get here, no harmony hub available");
	    				else
	    					myHarmony.pressButton(deviceButtons[z]);
	        		}
        		}
			}
		}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
        isDevMode = Boolean.parseBoolean(System.getProperty("dev.mode", "false"));
        validHarmony = bridgeSettings.getBridgeSettingsDescriptor().isValidHarmony();
		log.info("Harmony Home created." + (validHarmony ? "" : " No Harmony devices configured.") + (isDevMode ? " DevMode is set." : ""));
		if(validHarmony || isDevMode) {
			hubs = new HashMap<String, HarmonyServer>();
			aGsonHandler =
					new GsonBuilder()
					.create();
			if(isDevMode) {
				NamedIP devModeIp = new NamedIP();
				devModeIp.setIp("10.10.10.10");
				devModeIp.setName("devMode");
				List<NamedIP> theList = new ArrayList<NamedIP>();
				theList.add(devModeIp);
				IpList thedevList = new IpList();
				thedevList.setDevices(theList);
				bridgeSettings.getBridgeSettingsDescriptor().setHarmonyAddress(thedevList);
			}
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getHarmonyAddress().getDevices().iterator();
			while(theList.hasNext() && validHarmony) {
				NamedIP aHub = theList.next();
				boolean loopControl = true;
				int retryCount = 0;
				while(loopControl) {
					try {
			      		hubs.put(aHub.getName(), HarmonyServer.setup(bridgeSettings.getBridgeSettingsDescriptor(), isDevMode, aHub));
			      		loopControl = false;
					} catch (Exception e) {
						if(retryCount > 3) {
					        log.error("Cannot get harmony client (" + aHub.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
					        loopControl = false;
						} else {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								// ignore
							}
						}
							
						retryCount++;
					}
				}
			}
			if(hubs.isEmpty())
				validHarmony = false;
		}
		return this;
	}

	@Override
	public Object getItems(String type) {
		if(validHarmony) {
			if(type.equalsIgnoreCase(DeviceMapTypes.HARMONY_ACTIVITY[DeviceMapTypes.typeIndex]))
				return getActivities();
			if(type.equalsIgnoreCase(DeviceMapTypes.HARMONY_BUTTON[DeviceMapTypes.typeIndex]))
				return getDevices();
			if(type.equalsIgnoreCase("current_activity"))
				return getCurrentActivities();
		}
		return null;
	}
}
