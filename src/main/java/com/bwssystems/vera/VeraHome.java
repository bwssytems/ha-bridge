package com.bwssystems.vera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.luupRequests.Device;
import com.bwssystems.luupRequests.Scene;
import com.bwssystems.luupRequests.Sdata;

public class VeraHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(VeraHome.class);
	private Map<String, VeraInfo> veras;
	private Boolean validVera;
	
	public VeraHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	public List<Device> getDevices() {
		log.debug("consolidating devices for veras");
		Iterator<String> keys = veras.keySet().iterator();
		ArrayList<Device> deviceList = new ArrayList<Device>();
		while(keys.hasNext()) {
			String key = keys.next();
			Sdata theSdata = veras.get(key).getSdata();
			if(theSdata != null) {
				Iterator<Device> devices = theSdata.getDevices().iterator();
				while(devices.hasNext()) {
					deviceList.add(devices.next());
				}
			}
			else {
				deviceList = null;
				break;
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
			Sdata theSdata = veras.get(key).getSdata();
			if(theSdata != null) {
				Iterator<Scene> scenes = theSdata.getScenes().iterator();
				while(scenes.hasNext()) {
					sceneList.add(scenes.next());
				}
			}
			else {
				sceneList = null;
				break;
			}
		}
		return sceneList;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc) {
		// Not a device handler
		return null;
	}

	@Override
	public Object getItems(String type) {
		if(validVera) {
			if(type.equalsIgnoreCase(DeviceMapTypes.VERA_DEVICE[DeviceMapTypes.typeIndex]))
				return getDevices();
			if(type.equalsIgnoreCase(DeviceMapTypes.VERA_SCENE[DeviceMapTypes.typeIndex]))
				return getScenes();
		}
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		validVera = bridgeSettings.isValidVera();
		if(!validVera) {
			log.debug("No valid veras");
		} else {
			veras = new HashMap<String, VeraInfo>();
			Iterator<NamedIP> theList = bridgeSettings.getVeraAddress().getDevices().iterator();
			while(theList.hasNext()) {
				NamedIP aVera = theList.next();
	      		veras.put(aVera.getName(), new VeraInfo(aVera));
			}
		}
		return this;
	}

	@Override
	public void closeHome() {
		// TODO Auto-generated method stub
		
	}
}
