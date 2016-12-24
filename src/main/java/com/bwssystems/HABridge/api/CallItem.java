package com.bwssystems.HABridge.api;

import com.google.gson.JsonElement;

public class CallItem {
	private JsonElement item;
	private Integer count;
	private Integer delay;
	private String type;
	private String filterIPs;
	private String httpVerb;
	private String httpBody;
	private String httpHeaders;
	private String contentType;

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

	public String getHttpVerb() {
		return httpVerb;
	}

	public void setHttpVerb(String httpVerb) {
		this.httpVerb = httpVerb;
	}

	public String getHttpBody() {
		return httpBody;
	}

	public void setHttpBody(String httpBody) {
		this.httpBody = httpBody;
	}

	public String getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(String httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
