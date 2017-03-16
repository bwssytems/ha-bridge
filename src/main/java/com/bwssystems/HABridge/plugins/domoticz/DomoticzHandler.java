package com.bwssystems.HABridge.plugins.domoticz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.NameValue;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class DomoticzHandler {
    private static final Logger log = LoggerFactory.getLogger(DomoticzHandler.class);
    private static final String GET_REQUEST = "/json.htm?type=";
    private static final String DEVICES_TYPE = "devices";
    private static final String SCENES_TYPE = "scenes";
    private static final String FILTER_USED = "&used=true";
    private NamedIP domoticzAddress;

    public DomoticzHandler(NamedIP addressName) {
		super();
        domoticzAddress = addressName;
	}

	public List<DomoticzDevice> getDevices(HTTPHandler httpClient) {
    	return getDomoticzDevices(GET_REQUEST, DEVICES_TYPE, FILTER_USED, httpClient);
    }

	public List<DomoticzDevice> getScenes(HTTPHandler httpClient) {
    	return getDomoticzDevices(GET_REQUEST, SCENES_TYPE, null, httpClient);
    }

	private List<DomoticzDevice> getDomoticzDevices(String rootRequest, String type, String postpend, HTTPHandler httpClient) {
		Devices theDomoticzApiResponse = null;
		List<DomoticzDevice> deviceList = null;

		String theUrl = null;
    	String theData;
    	if(postpend != null && !postpend.isEmpty())
    		theUrl = buildUrl(rootRequest + type + postpend);
    	else
    		theUrl = buildUrl(rootRequest + type);
   		theData = httpClient.doHttpRequest(theUrl, null, null, null, buildHeaders());
    	if(theData != null) {
    		log.debug("GET " + type + " DomoticzApiResponse - data: " + theData);
	    	theDomoticzApiResponse = new Gson().fromJson(theData, Devices.class);
	    	if(theDomoticzApiResponse.getResult() == null) {
    			log.warn("Cannot get any devices for type " + type + " for Domoticz " + domoticzAddress.getName() + " as response is not parsable.");
	        	return deviceList;
	    	}
	    	deviceList = new ArrayList<DomoticzDevice>();
	    	
	    	Iterator<DeviceResult> theDeviceNames = theDomoticzApiResponse.getResult().iterator();
	    	while(theDeviceNames.hasNext()) {
	    		DeviceResult theDevice = theDeviceNames.next();
				DomoticzDevice aNewDomoticzDevice = new DomoticzDevice();
				aNewDomoticzDevice.setDevicetype(theDevice.getType());
				aNewDomoticzDevice.setDevicename(theDevice.getName());
				aNewDomoticzDevice.setIdx(theDevice.getIdx());
				aNewDomoticzDevice.setDomoticzaddress(domoticzAddress.getIp() + ":" + domoticzAddress.getPort());
				aNewDomoticzDevice.setDomoticzname(domoticzAddress.getName());
				deviceList.add(aNewDomoticzDevice);
	    		
	    	}
    	}
    	else {
    		log.warn("Get Domoticz device types " + type + " for " + domoticzAddress.getName() + " - returned null, no data.");
    	}
    	return deviceList;
    }

	public String buildUrl(String thePayload) {
		String newUrl = null;
		
		if(thePayload != null && !thePayload.isEmpty()) {
			if(domoticzAddress.getSecure() != null && domoticzAddress.getSecure())
				newUrl = "https://";
			else
				newUrl = "http://";
			
			newUrl = newUrl + domoticzAddress.getIp();
			
			if(domoticzAddress.getPort() != null && !domoticzAddress.getPort().isEmpty())
				newUrl = newUrl + ":" + domoticzAddress.getPort();

			if(thePayload.startsWith("/"))
				newUrl = newUrl + thePayload;
			else
				newUrl = newUrl + "/" + thePayload;
		}
		
		return newUrl;
	}
	
	public NameValue[] buildHeaders() {
		NameValue[] headers = null;
		
		if(domoticzAddress.getUsername() != null && !domoticzAddress.getUsername().isEmpty()
				&& domoticzAddress.getPassword() != null && !domoticzAddress.getPassword().isEmpty()) {
			NameValue theAuth = new NameValue();
			theAuth.setName("Authorization");
			String encoding = Base64.getEncoder().encodeToString((domoticzAddress.getUsername() + ":" + domoticzAddress.getPassword()).getBytes());
			theAuth.setValue("Basic " + encoding);
			headers = new NameValue[1];
			headers[0] = theAuth;
		}
		
		return headers;
	}
	public NamedIP getDomoticzAddress() {
		return domoticzAddress;
	}

	public void setDomoticzAddress(NamedIP DomoticzAddress) {
		this.domoticzAddress = DomoticzAddress;
	}

}
