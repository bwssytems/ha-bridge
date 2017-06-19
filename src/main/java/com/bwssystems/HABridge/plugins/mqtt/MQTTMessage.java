package com.bwssystems.HABridge.plugins.mqtt;

public class MQTTMessage {
	private String clientId;
	private String topic;
	private String message;
	private Integer delay;
	private Integer count;
	private Integer qos;
	private Boolean retain;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getDelay() {
		return delay;
	}
	public void setDelay(Integer delay) {
		this.delay = delay;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getQos() {
		return qos;
	}

	public void setQos(Integer qos) {
		this.qos = qos;
	}

	public Boolean getRetain() {
		return retain;
	}

	public void setRetain(Boolean retain) {
		this.retain = retain;
	}
}
