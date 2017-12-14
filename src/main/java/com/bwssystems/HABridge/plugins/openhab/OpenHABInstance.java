package com.bwssystems.HABridge.plugins.openhab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.bwssystems.HABridge.plugins.http.HTTPHome;
import com.google.gson.Gson;

public class OpenHABInstance {
	private static final Logger log = LoggerFactory.getLogger(OpenHABInstance.class);
	private NamedIP theOpenHAB;
	private HTTPHandler anHttpHandler;

	public OpenHABInstance(NamedIP openhabLocation) {
		super();
		anHttpHandler = HTTPHome.getHandler();
		theOpenHAB = openhabLocation;
	}

	public NamedIP getOpenHABAddress() {
		return theOpenHAB;
	}

	public void setOpenHABAddress(NamedIP openhabAddress) {
		this.theOpenHAB = openhabAddress;
	}

	public Boolean callCommand(String aCommand) {
		log.debug("calling HomeAssistant: " + aCommand);
		String aUrl = null;
		if(theOpenHAB.getSecure() != null && theOpenHAB.getSecure())
			aUrl = "https";
		else
			aUrl = "http";
/*		String domain = aCommand.getEntityId().substring(0, aCommand.getEntityId().indexOf("."));
		aUrl = aUrl + "://" + theOpenHAB.getIp() + ":" + theOpenHAB.getPort() + "/api/services/";
		if(domain.equals("group"))
			aUrl = aUrl + "homeassistant";
		else
			aUrl = aUrl + domain;
		String aBody = "{\"entity_id\":\"" + aCommand.getEntityId() + "\"";
		NameValue[] headers = null;
		if(theOpenHAB.getPassword() != null && !theOpenHAB.getPassword().isEmpty()) {
			NameValue password = new NameValue();
			password.setName("x-ha-access");
			password.setValue(theOpenHAB.getPassword());
			headers = new NameValue[1];
			headers[0] = password;
		}
		if(aCommand.getState().equalsIgnoreCase("on")) {
			aUrl = aUrl + "/turn_on";
			if(aCommand.getBri() != null)
				aBody = aBody + ",\"brightness\":" + aCommand.getBri() + "}";
			else
				aBody = aBody + "}";
		}
		else {
			aUrl = aUrl + "/turn_off";
			aBody = aBody + "}";			
		}
		log.debug("Calling HomeAssistant with url: " + aUrl);
   		String theData = anHttpHandler.doHttpRequest(aUrl, HttpPost.METHOD_NAME, "application/json", aBody, headers);
   		log.debug("call Command return is: <" + theData + ">");
   		*/
		return true;
	}

	public List<OpenHABItem> getDevices() {
		List<OpenHABItem> theDeviceStates = null;
		OpenHABItem[] theOpenhabStates;
		String theUrl = null;
    	String theData;
		NameValue[] headers = null;
		if(theOpenHAB.getPassword() != null && !theOpenHAB.getPassword().isEmpty()) {
			NameValue password = new NameValue();
			password.setName("x-ha-access");
			password.setValue(theOpenHAB.getPassword());
			headers = new NameValue[1];
			headers[0] = password;
		}
		if(theOpenHAB.getSecure() != null && theOpenHAB.getSecure())
			theUrl = "https";
		else
			theUrl = "http";
   		theUrl = theUrl + "://" + theOpenHAB.getIp() + ":" + theOpenHAB.getPort() + "/rest/items?recursive=false";
   		theData = anHttpHandler.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
    	if(theData != null) {
    		log.debug("GET OpenHAB States - data: " + theData);
    		theOpenhabStates = new Gson().fromJson(theData, OpenHABItem[].class);
	    	if(theOpenhabStates == null) {
	    		log.warn("Cannot get an devices for OpenHAB " + theOpenHAB.getName() + " as response is not parsable.");
	    	}
	    	else {
	    		theDeviceStates = new ArrayList<OpenHABItem>(Arrays.asList(theOpenhabStates));
	    	}
    	}
    	else
    		log.warn("Cannot get an devices for OpenHAB " + theOpenHAB.getName() + " http call failed.");
		return theDeviceStates;
	}

	
	protected void closeClient() {
		anHttpHandler.closeHandler();
		anHttpHandler = null;
	}
}
