package com.bwssystems.vera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.luupRequests.Device;
import com.bwssystems.luupRequests.Scene;

public class VeraHome {
    private static final Logger log = LoggerFactory.getLogger(VeraHome.class);
	private Map<String, VeraInfo> veras;
	
	public VeraHome(BridgeSettings bridgeSettings) {
		veras = new HashMap<String, VeraInfo>();
		if(!bridgeSettings.isValidVera())
			return;
		Iterator<NamedIP> theList = bridgeSettings.getVeraAddress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aVera = theList.next();
      		veras.put(aVera.getName(), new VeraInfo(aVera, bridgeSettings.isValidVera()));
		}
	}

	public List<Device> getDevices() {
		log.debug("consolidating devices for veras");
		Iterator<String> keys = veras.keySet().iterator();
		ArrayList<Device> deviceList = new ArrayList<Device>();
		while(keys.hasNext()) {
			String key = keys.next();
			Iterator<Device> devices = veras.get(key).getSdata().getDevices().iterator();
			while(devices.hasNext()) {
				deviceList.add(devices.next());
			}
		}
		return deviceList;
	}
	public List<Scene> getScenes() {
		log.debug("consolidating scenes for veras");
		Iterator<String> keys = veras.keySet().iterator();
		ArrayList<Scene> sceneList = new ArrayList<Scene>();
		while(keys.hasNext()) {
			String key = keys.next();
			Iterator<Scene> scenes = veras.get(key).getSdata().getScenes().iterator();
			while(scenes.hasNext()) {
				sceneList.add(scenes.next());
			}
		}
		return sceneList;
	}
}
