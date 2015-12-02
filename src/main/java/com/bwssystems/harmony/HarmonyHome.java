package com.bwssystems.harmony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.NamedIP;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;

public class HarmonyHome {
    private static final Logger log = LoggerFactory.getLogger(HarmonyHome.class);
	private Map<String, HarmonyServer> hubs;

	public HarmonyHome(BridgeSettings bridgeSettings) {
		super();
		hubs = new HashMap<String, HarmonyServer>();
		Iterator<NamedIP> theList = bridgeSettings.getHarmonyAddress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aHub = theList.next();
	      	try {
	      		hubs.put(aHub.getName(), HarmonyServer.setup(bridgeSettings, aHub));
			} catch (Exception e) {
		        log.error("Cannot get harmony client (" + aHub.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
		        return;
			}
		}
	}

	public HarmonyHandler getHarmonyHandler(String aName) {
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
	public List<HarmonyDevice> getDevices() {
		Iterator<String> keys = hubs.keySet().iterator();
		ArrayList<HarmonyDevice> deviceList = new ArrayList<HarmonyDevice>();
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
}
