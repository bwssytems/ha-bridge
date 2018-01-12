package com.bwssystems.fhem.test;

import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;

public class HttpTestHandler extends HTTPHandler {
	private String theData;

	public String getTheData() {
		return theData;
	}

	public void setTheData(String theData) {
		this.theData = theData;
	}

	public String doHttpRequest(String url, String httpVerb, String contentType, String body, NameValue[] headers) {
		return theData;
	}
}
