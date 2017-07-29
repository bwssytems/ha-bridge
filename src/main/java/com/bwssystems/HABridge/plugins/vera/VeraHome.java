package com.bwssystems.HABridge.plugins.vera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Device;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Scene;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Sdata;

public class VeraHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(VeraHome.class);
	private Map<String, VeraInfo> veras;
	private Boolean validVera;
	
	public VeraHome(BridgeSettings bridgeSettings) {
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
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
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
	public Home createHome(BridgeSettings bridgeSettings) {
		validVera = bridgeSettings.getBridgeSettingsDescriptor().isValidVera();
		log.info("Vera Home created." + (validVera ? "" : " No Veras configured."));
		if(validVera) {
			veras = new HashMap<String, VeraInfo>();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getVeraAddress().getDevices().iterator();
			while(theList.hasNext()) {
				NamedIP aVera = theList.next();
	      		veras.put(aVera.getName(), new VeraInfo(aVera));
			}
		}
		return this;
	}

	@Override
	public void closeHome() {
		veras = null;
	}
}
