package com.bwssystems.HABridge.api;

import com.google.gson.JsonElement;

public class CallItem {
	private JsonElement item;
	private Integer count;
	private Integer delay;
	private String type;
	private String filterIPs;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFilterIPs() {
		return filterIPs;
	}

	public void setFilterIPs(String filterIPs) {
		this.filterIPs = filterIPs;
	}

	public JsonElement getItem() {
		return item;
	}

	public void setItem(JsonElement item) {
		this.item = item;
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
