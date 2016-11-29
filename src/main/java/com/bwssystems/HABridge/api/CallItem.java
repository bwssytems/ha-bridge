package com.bwssystems.HABridge.api;

public class CallItem {
	private String item;
	private Integer count;
	private Integer delay;
	private String type;
	private String requesterAddress;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRequesterAddress() {
		return requesterAddress;
	}

	public void setRequesterAddress(String requesterAddress) {
		this.requesterAddress = requesterAddress;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String anitem) {
		item = anitem;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}
}
