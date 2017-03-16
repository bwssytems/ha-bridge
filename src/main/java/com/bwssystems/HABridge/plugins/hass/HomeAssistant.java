package com.bwssystems.HABridge.plugins.hass;

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
import com.google.gson.Gson;

public class HomeAssistant {
    private static final Logger log = LoggerFactory.getLogger(HomeAssistant.class);
    private NamedIP hassAddress;
    private HTTPHandler anHttpHandler;

	public HomeAssistant(NamedIP addressName) {
		super();
		anHttpHandler = new HTTPHandler();
        hassAddress = addressName;
	}

	public NamedIP getHassAddress() {
		return hassAddress;
	}

	public void setHassAddress(NamedIP hassAddress) {
		this.hassAddress = hassAddress;
	}

	public Boolean callCommand(HassCommand aCommand) {
		log.debug("calling HomeAssistant: " + aCommand.getHassName() + " - "
				+ aCommand.getEntityId() + " - " + aCommand.getState() + " - " + aCommand.getBri());
		String aUrl = null;
		if(hassAddress.getSecure() != null && hassAddress.getSecure())
			aUrl = "https";
		else
			aUrl = "http";
		String domain = aCommand.getEntityId().substring(0, aCommand.getEntityId().indexOf("."));
		aUrl = aUrl + "://" + hassAddress.getIp() + ":" + hassAddress.getPort() + "/api/services/";
		if(domain.equals("group"))
			aUrl = aUrl + "homeassistant";
		else
			aUrl = aUrl + domain;
		String aBody = "{\"entity_id\":\"" + aCommand.getEntityId() + "\"";
		NameValue[] headers = null;
		if(hassAddress.getPassword() != null && !hassAddress.getPassword().isEmpty()) {
			NameValue password = new NameValue();
			password.setName("x-ha-access");
			password.setValue(hassAddress.getPassword());
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
		return true;
	}

	public List<State> getDevices() {
		List<State> theDeviceStates = null;
		State[] theHassStates;
		String theUrl = null;
    	String theData;
		NameValue[] headers = null;
		if(hassAddress.getPassword() != null && !hassAddress.getPassword().isEmpty()) {
			NameValue password = new NameValue();
			password.setName("x-ha-access");
			password.setValue(hassAddress.getPassword());
			headers = new NameValue[1];
			headers[0] = password;
		}
		if(hassAddress.getSecure() != null && hassAddress.getSecure())
			theUrl = "https";
		else
			theUrl = "http";
   		theUrl = theUrl + "://" + hassAddress.getIp() + ":" + hassAddress.getPort() + "/api/states";
   		theData = anHttpHandler.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
    	if(theData != null) {
    		log.debug("GET Hass States - data: " + theData);
    		theHassStates = new Gson().fromJson(theData, State[].class);
	    	if(theHassStates == null) {
	    		log.warn("Cannot get an devices for HomeAssistant " + hassAddress.getName() + " as response is not parsable.");
	    	}
	    	else {
	    		theDeviceStates = new ArrayList<State>(Arrays.asList(theHassStates));
	    	}
    	}
    	else
    		log.warn("Cannot get an devices for HomeAssistant " + hassAddress.getName() + " http call failed.");
		return theDeviceStates;
	}

	
	protected void closeClient() {
		anHttpHandler.closeHandler();
		anHttpHandler = null;
	}
}
