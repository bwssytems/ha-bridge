package com.bwssystems.hass;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.google.gson.Gson;

public class HomeAssistant {
    private static final Logger log = LoggerFactory.getLogger(HomeAssistant.class);
    private NamedIP hassAddress;
    private HttpClient httpClient;

	public HomeAssistant(NamedIP addressName) {
		super();
        httpClient = HttpClients.createDefault();
        hassAddress = addressName;
	}

	public NamedIP getHassAddress() {
		return hassAddress;
	}

	public void setHassAddress(NamedIP hassAddress) {
		this.hassAddress = hassAddress;
	}

	public List<State> getDevices() {
		List<State> theDeviceStates = null;
		State[] theHassStates;
		String theUrl = null;
    	String theData;
   		theUrl = "http://" + hassAddress.getIp() + ":" + hassAddress.getPort() + "/api/states";
   		theData = doHttpGETRequest(theUrl);
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

	//	This function executes the url against the hass
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
}
