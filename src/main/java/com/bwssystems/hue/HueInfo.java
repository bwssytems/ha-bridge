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
    private static final String HUE_REQUEST = "/api/habridge";
    private NamedIP hueAddress;

    public HueInfo(NamedIP addressName) {
		super();
        httpClient = HttpClients.createDefault();
        hueAddress = addressName;
	}
    
	public HueApiResponse getHueApiResponse() {
		HueApiResponse theHueApiResponse = null;

		String theUrl = "http://" + hueAddress.getIp() + HUE_REQUEST;
    	String theData;
    	
    	theData = doHttpGETRequest(theUrl);
    	if(theData != null) {
	    	theHueApiResponse = new Gson().fromJson(theData, HueApiResponse.class);
	        log.debug("GET HueApiResponse - name: " + theHueApiResponse.getConfig().getName() + ", mac addr: " + theHueApiResponse.getConfig().getMac());
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
