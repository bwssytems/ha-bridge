package com.bwssystems.HABridge.plugins.fhem;

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

public class FHEMInstance {
	private static final Logger log = LoggerFactory.getLogger(FHEMInstance.class);
	private NamedIP theFhem;

	public FHEMInstance(NamedIP fhemLocation) {
		super();
		theFhem = fhemLocation;
	}

	public NamedIP getFhemAddress() {
		return theFhem;
	}

	public void setFhemAddress(NamedIP fhemAddress) {
		this.theFhem = fhemAddress;
	}

	public Boolean callCommand(String aCommand, String commandData, HTTPHandler httpClient) {
		log.debug("calling FHEM: " + theFhem.getIp() + ":" + theFhem.getPort() + aCommand);
		String aUrl = null;
		NameValue[] headers = null;
		if(theFhem.getSecure() != null && theFhem.getSecure())
			aUrl = "https://";
		else
			aUrl = "http://";
		if(theFhem.getUsername() != null && !theFhem.getUsername().isEmpty() && theFhem.getPassword() != null && !theFhem.getPassword().isEmpty()) {
			aUrl = aUrl + theFhem.getUsername() + ":" + theFhem.getPassword() + "@";
		}
		aUrl = aUrl + theFhem.getIp() + ":" + theFhem.getPort() + "/" + aCommand;
		String theData = httpClient.doHttpRequest(aUrl, HttpPost.METHOD_NAME, "text/plain", commandData, headers);
   		log.debug("call Command return is: <" + theData + ">");
		return true;
	}

	public List<FHEMDevice> getDevices(HTTPHandler httpClient) {
		List<FHEMDevice> deviceList = null;
		FHEMItem theFhemStates;
		String theUrl = null;
    	String theData;
		NameValue[] headers = null;
		if(theFhem.getSecure() != null && theFhem.getSecure())
			theUrl = "https://";
		else
			theUrl = "http://";
		if(theFhem.getUsername() != null && !theFhem.getUsername().isEmpty() && theFhem.getPassword() != null && !theFhem.getPassword().isEmpty()) {
			theUrl = theUrl + theFhem.getUsername() + ":" + theFhem.getPassword() + "@";
		}
   		theUrl = theUrl + theFhem.getIp() + ":" + theFhem.getPort() + "/fhem?cmd=jsonlist2";
   		if(theFhem.getWebhook() != null && !theFhem.getWebhook().trim().isEmpty())
   			theUrl = theUrl + "%20room=" + theFhem.getWebhook().trim();
   		theData = httpClient.doHttpRequest(theUrl, HttpGet.METHOD_NAME, "application/json", null, headers);
    	if(theData != null) {
    		log.debug("GET FHEM States - data: " + theData);
    		theData = getJSONData(theData);
    		theFhemStates = new Gson().fromJson(theData, FHEMItem.class);
	    	if(theFhemStates == null) {
	    		log.warn("Cannot get any devices for FHEM " + theFhem.getName() + " as response is not parsable.");
	    	}
	    	else {
		    	deviceList = new ArrayList<FHEMDevice>();
		    	
		    	for (Result aResult:theFhemStates.getResults()) {
		    		String name = aResult.getName();
		    		if(name.contains("<a href=")) {
			    		name = name.substring(name.indexOf("<a href=") + name.indexOf(">"));
			    		name = name.substring(1, name.indexOf("</a"));
			    		aResult.setName(name);
		    		}
		    		FHEMDevice aNewFhemDeviceDevice = new FHEMDevice();
		    		aNewFhemDeviceDevice.setItem(aResult);
		    		aNewFhemDeviceDevice.setAddress(theFhem.getIp() + ":" + theFhem.getPort());
		    		aNewFhemDeviceDevice.setName(theFhem.getName());
					deviceList.add(aNewFhemDeviceDevice);
		    	}
	    	}
    	}
    	else
    		log.warn("Cannot get an devices for FHEM " + theFhem.getName() + " http call failed.");
		return deviceList;
	}

	public String getJSONData(String response) {
		String theData;
		theData = response.substring(response.indexOf("<pre>") + 4);
		theData = theData.substring(1, theData.indexOf("</pre>") - 1);
		theData = theData.replace("\n", "");
		theData = theData.replace("\r", "");
		theData = theData.replace("<a href=\"", "<a href=\\\"");
		theData = theData.replace("\">", "\\\">");
		return theData;
	}
	
	protected void closeClient() {
	}
}
