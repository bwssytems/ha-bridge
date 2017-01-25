package com.bwssystems.HABridge.plugins.domoticz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;

public class DomoticzHandler {
    private static final Logger log = LoggerFactory.getLogger(DomoticzHandler.class);
    private static final String GET_REQUEST = "/json.htm?type=";
    private static final String DEVICES_TYPE = "devices";
    private static final String SCENES_TYPE = "scenes";
    private static final String FILTER_USED = "&used=";
    private HTTPHandler httpClient;
    private NamedIP domoticzAddress;

    public DomoticzHandler(NamedIP addressName) {
		super();
        httpClient = new HTTPHandler();
        domoticzAddress = addressName;
	}

	public List<DomoticzDevice> getDevices() {
    	return getDomoticzDevices(GET_REQUEST, DEVICES_TYPE, FILTER_USED);
    }

	public List<DomoticzDevice> getScenes() {
    	return getDomoticzDevices(GET_REQUEST, SCENES_TYPE, null);
    }

	private List<DomoticzDevice> getDomoticzDevices(String rootRequest, String type, String postpend) {
		Devices theDomoticzApiResponse = null;
		List<DomoticzDevice> deviceList = null;

		String theUrl = null;
    	String theData;
   		theUrl = "http://" + domoticzAddress.getIp() + ":" + domoticzAddress.getPort() + rootRequest + type;
   		theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
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

	public NamedIP getDomoticzAddress() {
		return domoticzAddress;
	}

	public void setDomoticzAddress(NamedIP DomoticzAddress) {
		this.domoticzAddress = DomoticzAddress;
	}

}
