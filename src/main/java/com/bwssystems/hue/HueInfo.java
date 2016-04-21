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
import com.google.gson.Gson;


public class HueInfo {
    private static final Logger log = LoggerFactory.getLogger(HueInfo.class);
    private HttpClient httpClient;
    private NamedIP hueAddress;
	private String theUser;

    public HueInfo(NamedIP addressName) {
		super();
        httpClient = HttpClients.createDefault();
        hueAddress = addressName;
        theUser = "habridge";
	}
    
	public HueApiResponse getHueApiResponse() {
		HueApiResponse theHueApiResponse = null;
		String errorString = null;

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
    		theData = doHttpGETRequest(theUrl);
	    	if(theData != null) {
	    		log.debug("GET HueApiResponse - data: " + theData);
	    		if(theData.contains("[{\"error\":")) {
	    			if(theData.contains("unauthorized user")) {
	        			if((theUser = HueUtil.registerWithHue(httpClient, hueAddress.getIp(), hueAddress.getName(), errorString)) == null) {
	        				log.warn("Register to Hue for " + hueAddress.getName() + " returned error: " + errorString);
	        				return null;
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

	//	This function executes the url against the vera
    protected String doHttpGETRequest(String url) {
    	String theContent = null;
        log.debug("calling GET on URL: " + url);
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            log.debug("GET on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                theContent = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8")); //read content for data
                EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
            }
        } catch (IOException e) {
            log.error("doHttpGETRequest: Error calling out to HA gateway: " + e.getMessage());
        }
        return theContent;
    }

	public NamedIP getHueAddress() {
		return hueAddress;
	}

	public void setHueAddress(NamedIP hueAddress) {
		this.hueAddress = hueAddress;
	}
}
