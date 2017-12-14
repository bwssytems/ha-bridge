package com.bwssystems.HABridge.plugins.openhab;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class OpenHABHome implements Home  {
    private static final Logger log = LoggerFactory.getLogger(OpenHABHome.class);
	private Map<String, OpenHABInstance> openhabMap;
	private Boolean validOpenhab;
	private Gson aGsonHandler;
	private boolean closed;
	
	public OpenHABHome(BridgeSettings bridgeSettings) {
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}


	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri, Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getItems(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		openhabMap = null;
		aGsonHandler = null;
		validOpenhab = bridgeSettings.getBridgeSettingsDescriptor().isValidOpenhab();
		log.info("OpenHAB Home created." + (validOpenhab ? "" : " No OpenHABs configured."));
		if(validOpenhab) {
			openhabMap = new HashMap<String,OpenHABInstance>();
			aGsonHandler =
					new GsonBuilder()
					.create();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getHassaddress().getDevices().iterator();
			while(theList.hasNext() && validOpenhab) {
				NamedIP anOpenhab = theList.next();
		      	try {
		      		openhabMap.put(anOpenhab.getName(), new OpenHABInstance(anOpenhab));
				} catch (Exception e) {
			        log.error("Cannot get hass (" + anOpenhab.getName() + ") setup, Exiting with message: " + e.getMessage(), e);
			        validOpenhab = false;
				}
			}
        }
		return this;
	}

	@Override
	public void closeHome() {
		if(!closed) {
			
		}
		
	}


}
