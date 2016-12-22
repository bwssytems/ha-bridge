package com.bwssystems.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.api.hue.StateChangeBody;
import com.bwssystems.HABridge.hue.MultiCommandUtil;

public class MQTTHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(MQTTHome.class);
	private Map<String, MQTTHandler> handlers;
	private Boolean validMqtt;

	public MQTTHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public void closeHome() {
		if(!validMqtt)
			return;
		log.debug("Shutting down MQTT handlers.");
		if(handlers != null && !handlers.isEmpty()) {
			Iterator<String> keys = handlers.keySet().iterator();
			while(keys.hasNext()) {
				String key = keys.next();
				handlers.get(key).shutdown();
			}
		}
	}

	public MQTTHandler getMQTTHandler(String aName) {
		if(!validMqtt)
			return null;
		MQTTHandler aHandler;
		if(aName == null || aName.equals("")) {
			aHandler = null;
			log.debug("Cannot get MQTT handler for name as it is empty.");
		}
		else {
			aHandler = handlers.get(aName);
			log.debug("Retrieved a MQTT hanlder for name: " + aName);
		}
		return aHandler;
	}
	
	@Override
	public Object getItems(String type) {
		if(!validMqtt)
			return null;
		Iterator<String> keys = handlers.keySet().iterator();
		ArrayList<MQTTBroker> deviceList = new ArrayList<MQTTBroker>();
		while(keys.hasNext()) {
			String key = keys.next();
			MQTTHandler aHandler = handlers.get(key);
			MQTTBroker aDevice = new MQTTBroker(aHandler.getMyConfig());
			deviceList.add(aDevice);
		}
		return deviceList;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int iterationCount,
			DeviceState state, StateChangeBody theStateChanges, boolean stateHasBri, boolean stateHasBriInc) {
		// TODO Auto-generated method stub
		log.info("device handler not implemented");
		return null;
	}

	@Override
	public Home createHome(BridgeSettingsDescriptor bridgeSettings) {
		validMqtt = bridgeSettings.isValidMQTT();
		if(!validMqtt) {
			log.debug("No MQTT configuration");
		} else {
			handlers = new HashMap<String, MQTTHandler>();
			Iterator<NamedIP> theList = bridgeSettings.getMqttaddress().getDevices().iterator();
			while(theList.hasNext()) {
				NamedIP aClientConfig = theList.next();
				MQTTHandler aHandler = new MQTTHandler(aClientConfig);
				if(aHandler != null)
					handlers.put(aClientConfig.getName(), aHandler);
			}
		}
		return this;
	}
}
