package com.bwssystems.HABridge.plugins.hue;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.SuccessUserResponse;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.google.gson.Gson;


public class HueInfo {
    private static final Logger log = LoggerFactory.getLogger(HueInfo.class);
    private HTTPHandler httpHandler;
    private NamedIP hueAddress;
    private HueHome myHome;
	public static final String HUE_REQUEST = "/api";

    public HueInfo(NamedIP addressName, HueHome theHome) {
		super();
        httpHandler = new HTTPHandler();
        hueAddress = addressName;
        myHome = theHome;
 	}
    
	public HueApiResponse getHueApiResponse() {
		log.debug("Get hue info to hue " + hueAddress.getName());
		HueApiResponse theHueApiResponse = null;

		if(hueAddress.getUsername() == null) {
			registerWithHue();
			if(hueAddress.getUsername() == null) {
				log.warn("Could not register with hue: " + hueAddress.getName());
			}
		}
		
		String theUrl = "http://" + hueAddress.getIp() + HUE_REQUEST + "/" + hueAddress.getUsername();
    	String theData;
    	boolean loopControl = true;
    	int retryCount = 0;
    	while(loopControl) {
    		if(retryCount > 3) {
	    		log.warn("Max Retry reached to get Hue data from " + hueAddress.getName());
	    		loopControl = false;
    			break;
    		} else if (retryCount > 0) {
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
    		}
    		theUrl = "http://" + hueAddress.getIp() + HUE_REQUEST + "/" + hueAddress.getUsername();
    		theData = httpHandler.doHttpRequest(theUrl, null, null, null, null);
	    	if(theData != null) {
	    		log.debug("GET HueApiResponse - data: " + theData);
	    		if(theData.contains("[{\"error\":")) {
	    			if(theData.contains("unauthorized user")) {
	    				registerWithHue();
	        			if(hueAddress.getUsername() == null) {
	        				log.warn("Retry Register to Hue for " + hueAddress.getName() + " returned error: " + theData);
	        			}
	        			retryCount++;
	    			}
	    			else {
	    				log.warn("GET HueApiResponse for " + hueAddress.getName() + " - returned error: " + theData);
	    				return null;
	    			}
	    		}
	    		else {
			    	theHueApiResponse = new Gson().fromJson(theData, HueApiResponse.class);
			        log.debug("GET HueApiResponse for " + hueAddress.getName() + " - Gson parse - name: " + theHueApiResponse.getConfig().getName() + ", mac addr: " + theHueApiResponse.getConfig().getMac());
			        loopControl = false;
	    		}
	    	}
	    	else {
	    		log.warn("GET HueApiResponse for " + hueAddress.getName() + " - returned null, no data.");
	    		loopControl = false;
	    	}
    	}
    	return theHueApiResponse;
    }
	
	public String registerWithHue() {
    	UserCreateRequest theLogin = new UserCreateRequest();
        theLogin.setDevicetype("HABridge#MyMachine");
 
        	String aMessage = httpHandler.doHttpRequest("http://" + hueAddress.getIp() + HUE_REQUEST, HttpPost.METHOD_NAME, "application/json", new Gson().toJson(theLogin), null);

            log.debug("registerWithHue - POST execute on " + hueAddress.getName() + "URL responded: " + aMessage);
            if(!aMessage.isEmpty()){
                log.debug("registerWithHue response data: " + aMessage);
                if(aMessage.contains("[{\"error\":")) {
                	if(aMessage.contains("link button not")) {
                		log.warn("registerWithHue needs link button pressed on HUE bridge: " + hueAddress.getName());
                	}
                	else
                		log.warn("registerWithHue returned an unexpected error: " + aMessage);
                }
                else {
	            	SuccessUserResponse[] theResponses = new Gson().fromJson(aMessage, SuccessUserResponse[].class); //read content for data, SuccessUserResponse[].class);
	            	hueAddress.setUsername(theResponses[0].getSuccess().getUsername());
	            	myHome.updateHue(hueAddress);
                }
            }
        return hueAddress.getUsername();
    }

	public DeviceResponse getHueDeviceInfo(String hueDeviceId, DeviceDescriptor device) {
		log.debug("Get hue device info to "+ hueAddress.getName() + " passthru id " + hueDeviceId);
		String responseString = null;
		DeviceResponse deviceResponse = null;
		if(hueAddress.getUsername() == null)
			registerWithHue();
		if (hueAddress.getUsername() != null) {
			// make call
			responseString = httpHandler.doHttpRequest(
					"http://" + hueAddress.getIp() + "/api/" + hueAddress.getUsername()
							+ "/lights/" + hueDeviceId,
					HttpGet.METHOD_NAME, "application/json", null, null);
			if (responseString == null) {
				log.warn("Error on calling hue device to get state: " + device.getName());
				deviceResponse = DeviceResponse.createResponse(device);
			} else if (responseString.contains("[{\"error\":") && responseString.contains("unauthorized user")) {
				log.warn("Error on calling hue device to get state: " + device.getName() + " with errors: " + responseString);
				deviceResponse = DeviceResponse.createResponse(device);
			} else {
				deviceResponse = new Gson().fromJson(responseString, DeviceResponse.class);
				if (deviceResponse != null) 
					deviceResponse.setName(device.getName());
			}
		} else  {
			log.warn("Error on calling hue device to get state: " + device.getName() + " Could not register with hue: " + hueAddress.getName());
			deviceResponse = DeviceResponse.createResponse(device);
		}

		return deviceResponse;
	}

	public String changeState(HueDeviceIdentifier deviceId, String lightId, String body) {
		log.debug("Changing state for ha-bridge id " + lightId + " to " + deviceId.getHueName() + " passthru id " + deviceId.getDeviceId() + " with state " + body );
		String responseString = null;
		if(hueAddress.getUsername() == null)
			registerWithHue();
		if (hueAddress.getUsername() != null) {
			responseString = httpHandler.doHttpRequest(
					"http://" + deviceId.getIpAddress() + "/api/" + hueAddress.getUsername()
							+ "/lights/" + deviceId.getDeviceId() + "/state",
					HttpPut.METHOD_NAME, "application/json", body, null);
			if (responseString == null) {
				log.warn("Error on calling Hue passthru to change device state: " + deviceId.getHueName());
				responseString = "[{\"error\":{\"type\": 6, \"address\": \"/lights/" + lightId
						+ "\",\"description\": \"Error on calling HUE to change device state\", \"parameter\": \"/lights/"
						+ lightId + "state\"}}]";
			} else if (responseString.contains("[{\"error\":")) {
				if(responseString.contains("unauthorized user")) {
				}
				log.warn("Error occurred when calling Hue Passthru: " + responseString);
			}
		} else  {
			log.warn("Error on calling hue device to change state: " + deviceId.getHueName() + " Could not register with hue: " + hueAddress.getName());
		}
		return responseString;
	}

	public void closeHue() {
		httpHandler.closeHandler();
		httpHandler = null;
	}
	
	public NamedIP getHueAddress() {
		return hueAddress;
	}

	public void setHueAddress(NamedIP hueAddress) {
		this.hueAddress = hueAddress;
	}
}
