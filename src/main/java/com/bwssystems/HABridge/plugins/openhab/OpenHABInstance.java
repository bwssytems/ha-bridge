package com.bwssystems.HABridge.plugins.openhab;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class OpenHABInstance {
	private static final Logger log = LoggerFactory.getLogger(OpenHABInstance.class);
	private NamedIP theOpenHAB;

	public OpenHABInstance(NamedIP openhabLocation) {
		super();
		theOpenHAB = openhabLocation;
	}

	public NamedIP getOpenHABAddress() {
		return theOpenHAB;
	}

	public void setOpenHABAddress(NamedIP openhabAddress) {
		this.theOpenHAB = openhabAddress;
	}

	public Boolean callCommand(String aCommand, String commandData, HTTPHandler httpClient) {
		log.debug("calling OpenHAB: " + theOpenHAB.getIp() + ":" + theOpenHAB.getPort() + aCommand);
		String aUrl = null;
		NameValue[] headers = null;
		if(theOpenHAB.getSecure() != null && theOpenHAB.getSecure())
			aUrl = "https://";
		else
			aUrl = "http://";
		if(theOpenHAB.getUsername() != null && !theOpenHAB.getUsername().isEmpty() && theOpenHAB.getPassword() != null && !theOpenHAB.getPassword().isEmpty()) {
			aUrl = aUrl + theOpenHAB.getUsername() + ":" + theOpenHAB.getPassword() + "@";
		}
		aUrl = aUrl + theOpenHAB.getIp() + ":" + theOpenHAB.getPort() + "/" + aCommand;
		String theData = httpClient.doHttpRequest(aUrl, HttpPost.METHOD_NAME, "text/plain", commandData, headers);
   		log.debug("call Command return is: <" + theData + ">");
		return true;
	}

	public List<OpenHABDevice> getDevices(HTTPHandler httpClient) {
		List<OpenHABDevice> deviceList = null;
		OpenHABItem[] theOpenhabStates;
		String theUrl = null;
    	String theData;
		NameValue[] headers = null;
		if(theOpenHAB.getSecure() != null && theOpenHAB.getSecure())
			theUrl = "https://";
		else
			theUrl = "http://";
		if(theOpenHAB.getUsername() != null && !theOpenHAB.getUsername().isEmpty() && theOpenHAB.getPassword() != null && !theOpenHAB.getPassword().isEmpty()) {
			theUrl = theUrl + theOpenHAB.getUsername() + ":" + theOpenHAB.getPassword() + "@";
		}
   		theUrl = theUrl + theOpenHAB.getIp() + ":" + theOpenHAB.getPort() + "/rest/items?recursive=false";
   		theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
    	if(theData != null) {
    		log.debug("GET OpenHAB States - data: " + theData);
    		theOpenhabStates = new Gson().fromJson(theData, OpenHABItem[].class);
	    	if(theOpenhabStates == null) {
	    		log.warn("Cannot get an devices for OpenHAB " + theOpenHAB.getName() + " as response is not parsable.");
	    	}
	    	else {
		    	deviceList = new ArrayList<OpenHABDevice>();
		    	
		    	for (int i = 0; i < theOpenhabStates.length; i++) {
		    		OpenHABDevice aNewOpenHABDeviceDevice = new OpenHABDevice();
		    		aNewOpenHABDeviceDevice.setItem(theOpenhabStates[i]);
		    		aNewOpenHABDeviceDevice.setAddress(theOpenHAB.getIp() + ":" + theOpenHAB.getPort());
		    		aNewOpenHABDeviceDevice.setName(theOpenHAB.getName());
					deviceList.add(aNewOpenHABDeviceDevice);
		    		
		    	}
	    	}
    	}
    	else
    		log.warn("Cannot get an devices for OpenHAB " + theOpenHAB.getName() + " http call failed.");
		return deviceList;
	}

	
	protected void closeClient() {
	}
}
