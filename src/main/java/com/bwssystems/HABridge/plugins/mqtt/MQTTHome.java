package com.bwssystems.HABridge.plugins.mqtt;

import java.util.ArrayList;
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
import com.bwssystems.HABridge.hue.BrightnessDecode;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.DeviceDataDecode;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.hue.TimeDecode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MQTTHome implements Home {
    private static final Logger log = LoggerFactory.getLogger(MQTTHome.class);
	private Map<String, MQTTHandler> handlers;
	private Boolean validMqtt;
	private Gson aGsonHandler;

	public MQTTHome(BridgeSettings bridgeSettings) {
		super();
		createHome(bridgeSettings);
	}

	@Override
	public void closeHome() {
		if(!validMqtt)
			return;
		log.debug("Shutting down MQTT handlers.");
		if(handlers != null && !handlers.isEmpty()) {
			for (String key : handlers.keySet()) {
				handlers.get(key).shutdown();
			}
		}
		handlers = null;
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
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body) {
		String responseString = null;
		log.debug("executing HUE api request to send message to MQTT broker: " + anItem.getItem().toString());
		if (validMqtt) {
			String mqttObject = null;
			if(anItem.getItem().isJsonObject() || anItem.getItem().isJsonArray()) {
				mqttObject = aGsonHandler.toJson(anItem.getItem());
			}
			else
				mqttObject =anItem.getItem().getAsString();
			mqttObject = BrightnessDecode.calculateReplaceIntensityValue(mqttObject,
					intensity, targetBri, targetBriInc, false);
			mqttObject = DeviceDataDecode.replaceDeviceData(mqttObject, device);
			mqttObject = TimeDecode.replaceTimeValue(mqttObject);
			if (mqttObject.substring(0, 1).equalsIgnoreCase("{"))
				mqttObject = "[" + mqttObject + "]";
			MQTTMessage[] mqttMessages = aGsonHandler.fromJson(mqttObject, MQTTMessage[].class);
        	Integer theCount = 1;
       		for(int z = 0; z < mqttMessages.length; z++) {
        		if(mqttMessages[z].getCount() != null && mqttMessages[z].getCount() > 0)
        			theCount = mqttMessages[z].getCount();
        		for(int y = 0; y < theCount; y++) {
 					log.debug("publishing message: " + mqttMessages[y].getClientId() + " - "
							+ mqttMessages[y].getTopic() + " - " + mqttMessages[y].getMessage()
							+ " - count: " + String.valueOf(z));
						
					MQTTHandler mqttHandler = getMQTTHandler(mqttMessages[y].getClientId());
					if (mqttHandler == null) {
						log.warn("Should not get here, no mqtt hanlder available");
					} else {
						mqttHandler.publishMessage(mqttMessages[y].getTopic(), mqttMessages[y].getMessage(), mqttMessages[y].getQos(), mqttMessages[y].getRetain());
					}
        		}
 			}
		} else {
			log.warn("Should not get here, no mqtt brokers configured");
			responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
					+ "\",\"description\": \"Should not get here, no mqtt brokers configured\", \"parameter\": \"/lights/"
					+ lightId + "state\"}}]";

		}
		return responseString;
	}

	@Override
	public Home createHome(BridgeSettings bridgeSettings) {
		validMqtt = bridgeSettings.getBridgeSettingsDescriptor().isValidMQTT();
		log.info("MQTT Home created." + (validMqtt ? "" : " No MQTT Clients configured."));
		if(validMqtt) {
			aGsonHandler =
					new GsonBuilder()
					.create();
			handlers = new HashMap<>();
			for (NamedIP aClientConfig : bridgeSettings.getBridgeSettingsDescriptor().getMqttaddress().getDevices()) {
				MQTTHandler aHandler = new MQTTHandler(aClientConfig);
				handlers.put(aClientConfig.getName(), aHandler);
			}
		}
		return this;
	}
}
