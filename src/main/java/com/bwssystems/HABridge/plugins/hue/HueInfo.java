package com.bwssystems.HABridge.plugins.hue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
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
    private HTTPHandler httpClient;
    private NamedIP hueAddress;
	private String theUser;
	public static final String HUE_REQUEST = "/api";

    public HueInfo(NamedIP addressName) {
		super();
        httpClient = new HTTPHandler();
        hueAddress = addressName;
        theUser = null;
	}
    
	public HueApiResponse getHueApiResponse() {
		HueApiResponse theHueApiResponse = null;

		if(theUser == null) {
			registerWithHue();
			if(theUser == null) {
				log.warn("Could not register with hue: " + hueAddress.getName());
			}
		}
		
		String theUrl = "http://" + hueAddress.getIp() + HUE_REQUEST + "/" + theUser;
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
    		theUrl = "http://" + hueAddress.getIp() + HueUtil.HUE_REQUEST + "/" + theUser;
    		theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
	    	if(theData != null) {
	    		log.debug("GET HueApiResponse - data: " + theData);
	    		if(theData.contains("[{\"error\":")) {
	    			if(theData.contains("unauthorized user")) {
	    				theUser = registerWithHue();
	        			if(theUser == null) {
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
        HttpPost postRequest = new HttpPost("http://" + hueAddress.getIp() + HUE_REQUEST);
        ContentType parsedContentType = ContentType.parse("application/json");
        StringEntity requestBody = new StringEntity(new Gson().toJson(theLogin), parsedContentType);
        HttpResponse response = null;
        postRequest.setEntity(requestBody);
        HttpClient anHttpClient = httpClient.getHttpClient();
        try {
            response = anHttpClient.execute(postRequest);
            log.debug("POST execute on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() >= 200  && response.getStatusLine().getStatusCode() < 300){
            	String theBody = EntityUtils.toString(response.getEntity());
                log.debug("registerWithHue response data: " + theBody);
                if(theBody.contains("[{\"error\":")) {
                	if(theBody.contains("link button not")) {
                		log.warn("registerWithHue needs link button pressed on HUE bridge: " + hueAddress.getName());
                	}
                	else
                		log.warn("registerWithHue returned an unexpected error: " + theBody);
                }
                else {
	            	SuccessUserResponse[] theResponses = new Gson().fromJson(theBody, SuccessUserResponse[].class); //read content for data, SuccessUserResponse[].class);
	            	theUser = theResponses[0].getSuccess().getUsername();
                }
            }
            EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
        } catch (IOException e) {
        	log.warn("Error logging into HUE: IOException in log", e);
        }
        return theUser;
    }

	public DeviceResponse getHueDeviceInfo(String hueDeviceId, DeviceDescriptor device) {
		String responseString = null;
		DeviceResponse deviceResponse = null;
		if(theUser == null)
			registerWithHue();
		if (theUser != null) {
			// make call
			responseString = httpClient.doHttpRequest(
					"http://" + hueAddress.getIp() + "/api/" + theUser
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
		String responseString = null;
		if(theUser == null)
			registerWithHue();
		if (theUser != null) {
			responseString = httpClient.doHttpRequest(
					"http://" + deviceId.getIpAddress() + "/api/" + theUser
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
		httpClient.closeHandler();
		httpClient = null;
	}
	
	public NamedIP getHueAddress() {
		return hueAddress;
	}

	public void setHueAddress(NamedIP hueAddress) {
		this.hueAddress = hueAddress;
	}
}
