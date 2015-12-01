package com.bwssystems.harmony;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.NamedIP;

import net.whistlingfish.harmony.config.Activity;

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
		if(aName == null || aName.equals("")) {
			HarmonyHandler aHandler = hubs.get("default").getMyHarmony();
			if(aHandler == null) {
				Set<String> keys = hubs.keySet();
				if(!keys.isEmpty()) {
					aHandler = hubs.get(keys.toArray()[0]).getMyHarmony();
				}
				else
					aHandler = null;
			}
			return aHandler;
		}
		return hubs.get(aName).getMyHarmony();
	}
	
	public List<HarmonyActivity> getActivities() {
		Iterator<String> keys = hubs.keySet().iterator();
		while(keys.hasNext()) {
			List<Activity> theActivities = hubs.get(keys.next()).getMyHarmony().getActivities();
			ListIterator<Activity> activities = theActivities.listIterator();
			while(activities.hasNext()) {
				
			}
		}
		return null;
	}
	public List<HarmonyCurrentActivity> getCurrentActivities() {
		return null;
	}
	public List<HarmonyDevice> getDevices() {
		return null;
	}
}
