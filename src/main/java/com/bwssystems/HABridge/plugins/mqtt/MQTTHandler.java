package com.bwssystems.HABridge.plugins.mqtt;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;

import java.util.Optional;

public class MQTTHandler {
    private static final Logger log = LoggerFactory.getLogger(MQTTHandler.class);
	private NamedIP myConfig;
	private MqttClient myClient;

	public MQTTHandler(NamedIP aConfig) {
		super();
		log.info("Setting up handler for name: " + aConfig.getName());
        MemoryPersistence persistence = new MemoryPersistence();
		myConfig = aConfig;
        try {
			myClient = new MqttClient("tcp://" + myConfig.getIp(), myConfig.getName(), persistence);
		} catch (MqttException e) {
			log.error("Could not create MQTT client for name: " + myConfig.getName() + " and ip: " + myConfig.getIp() + " with message: " + e.getMessage());
		}
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        if(aConfig.getUsername() != null && aConfig.getUsername().trim().length() > 0) {
        	if(aConfig.getPassword() != null && aConfig.getPassword().trim().length() > 0) {
        		connOpts.setUserName(aConfig.getUsername().trim());
        		connOpts.setPassword(aConfig.getPassword().trim().toCharArray());
        	}
        }
        try {
			myClient.connect(connOpts);
		} catch (MqttException e) {
			log.error("Could not connect MQTT client for name: " + myConfig.getName() + " and ip: " + myConfig.getIp() + " with message: " + e.getMessage());
		}
	}

	public void publishMessage(String topic, String content, Integer qos, Boolean retain) {
        MqttMessage message = new MqttMessage(StringEscapeUtils.unescapeJava(content).getBytes());

		message.setQos(Optional.ofNullable(qos).orElse(1));
        message.setRetained(Optional.ofNullable(retain).orElse(false));

        try {
			if(!myClient.isConnected()) {
				try {
					myClient.connect();
				} catch (MqttSecurityException e1) {
					log.error("Could not retry connect to MQTT client for name: " + myConfig.getName() + " and ip: " + myConfig.getIp() + " with message: " + e1.getMessage());
					return;
				} catch (MqttException e1) {
					log.error("Could not retry connect to MQTT client for name: " + myConfig.getName() + " and ip: " + myConfig.getIp() + " with message: " + e1.getMessage());
					return;
				}
			}
			myClient.publish(topic, message);
		} catch (MqttException e) {
			log.error("Could not publish to MQTT client for name: " + myConfig.getName() + " and ip: " + myConfig.getIp() + " with message: " + e.getMessage());
		}
	}
	
	public NamedIP getMyConfig() {
		return myConfig;
	}

	public void shutdown() {
		try {
			myClient.disconnect();
		} catch (MqttException e) {
			log.warn("Could not disconnect MQTT client for name: " + myConfig.getName() + " and ip: " + myConfig.getIp());
		}
		myClient = null;
	}
}
