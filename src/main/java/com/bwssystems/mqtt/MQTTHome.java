package com.bwssystems.mqtt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettingsDescriptor;
import com.bwssystems.HABridge.NamedIP;

public class MQTTHome {
    private static final Logger log = LoggerFactory.getLogger(MQTTHome.class);
	private Map<String, MQTTHandler> handlers;

	public MQTTHome(BridgeSettingsDescriptor bridgeSettings) {
		super();
		if(!bridgeSettings.isValidMQTT())
			return;

		handlers = new HashMap<String, MQTTHandler>();
		Iterator<NamedIP> theList = bridgeSettings.getMqttaddress().getDevices().iterator();
		while(theList.hasNext()) {
			NamedIP aClientConfig = theList.next();
			MQTTHandler aHandler = new MQTTHandler(aClientConfig);
			if(aHandler != null)
				handlers.put(aClientConfig.getName(), aHandler);
		}
	}

	public void shutdownMQTTClients() {
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
	
}
