package com.bwssystems.HABridge.plugins.mqtt;

import com.bwssystems.HABridge.NamedIP;

public class MQTTBroker {
	private String clientId;
	private String ip;

	public MQTTBroker(NamedIP brokerConfig) {
		super();
		this.setIp(brokerConfig.getIp());
		this.setClientId(brokerConfig.getName());
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
