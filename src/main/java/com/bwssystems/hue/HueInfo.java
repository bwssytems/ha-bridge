package com.bwssystems.hue;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.bwssystems.http.HTTPHandler;
import com.google.gson.Gson;


public class HueInfo {
    private static final Logger log = LoggerFactory.getLogger(HueInfo.class);
    private HTTPHandler httpClient;
    private NamedIP hueAddress;
	private String theUser;
	private HueHome theHueHome;
	private String errorString = null;

    public HueInfo(NamedIP addressName, HueHome aHueHome) {
		super();
        httpClient = new HTTPHandler();
        hueAddress = addressName;
        theUser = "habridge";
        theHueHome = aHueHome;
	}
    
	public HueApiResponse getHueApiResponse() {
		HueApiResponse theHueApiResponse = null;

		String theUrl = "http://" + hueAddress.getIp() + HueUtil.HUE_REQUEST + "/" + theUser;
    	String theData;
    	boolean loopControl = true;
    	int retryCount = 0;
    	while(loopControl) {
    		if(retryCount > 3) {
	    		log.warn("Max Retry reached to get Hue data from " + hueAddress.getName());
	    		loopControl = false;
    			break;
    		}
    		theUrl = "http://" + hueAddress.getIp() + HueUtil.HUE_REQUEST + "/" + theUser;
    		theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
	    	if(theData != null) {
	    		log.debug("GET HueApiResponse - data: " + theData);
	    		if(theData.contains("[{\"error\":")) {
	    			if(theData.contains("unauthorized user")) {
	    				theUser = HueUtil.registerWithHue(httpClient, hueAddress.getIp(), hueAddress.getName(), theHueHome.getTheHUERegisteredUser());
	        			if(theUser == null) {
	        				log.warn("Register to Hue for " + hueAddress.getName() + " returned error: " + errorString);
	        				return null;
	        			}
	        			else
	        				theHueHome.setTheHUERegisteredUser(theUser);
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

	public NamedIP getHueAddress() {
		return hueAddress;
	}

	public void setHueAddress(NamedIP hueAddress) {
		this.hueAddress = hueAddress;
	}
}
