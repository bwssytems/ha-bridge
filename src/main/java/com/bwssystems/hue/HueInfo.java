package com.bwssystems.hue;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.SuccessUserResponse;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.bwssystems.HABridge.api.hue.HueApiResponse;
import com.google.gson.Gson;


public class HueInfo {
    private static final Logger log = LoggerFactory.getLogger(HueInfo.class);
    private HttpClient httpClient;
    private static final String HUE_REQUEST = "/api";
    private NamedIP hueAddress;
	private UserCreateRequest theLogin;
	private String theUser;

    public HueInfo(NamedIP addressName) {
		super();
        httpClient = HttpClients.createDefault();
        hueAddress = addressName;
        theLogin = new UserCreateRequest();
        theLogin.setDevicetype("HA Bridge");
        theLogin.setUsername("habridge");
        theUser = theLogin.getUsername();
	}
    
	public HueApiResponse getHueApiResponse() {
		HueApiResponse theHueApiResponse = null;

		String theUrl = "http://" + hueAddress.getIp() + HUE_REQUEST + "/" + theLogin.getUsername();
    	String theData;
    	boolean loopControl = true;
    	int retryCount = 0;
    	while(loopControl) {
    		if(retryCount > 3) {
	    		log.warn("Max Retry reached to get Hue data from " + hueAddress.getName());
	    		loopControl = false;
    			break;
    		}
    		theUrl = "http://" + hueAddress.getIp() + HUE_REQUEST + "/" + theUser;
    		theData = doHttpGETRequest(theUrl);
	    	if(theData != null) {
	    		log.debug("GET HueApiResponse - data: " + theData);
	    		if(theData.contains("[{\"error\":")) {
	    			if(theData.contains("unauthorized user")) {
	        			if(!registerWithHue()) {
	        				log.warn("Register to Hue for " + hueAddress.getName() + " - returned error.");
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

    private boolean registerWithHue() {
    	boolean responseValue = false;
        HttpPost postRequest = new HttpPost("http://" + hueAddress.getIp() + HUE_REQUEST);
        ContentType parsedContentType = ContentType.parse("application/json");
        StringEntity requestBody = new StringEntity(new Gson().toJson(theLogin), parsedContentType);
        HttpResponse response = null;
        postRequest.setEntity(requestBody);
        try {
            response = httpClient.execute(postRequest);
            log.debug("POST execute on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() >= 200  && response.getStatusLine().getStatusCode() < 300){
            	String theBody = EntityUtils.toString(response.getEntity());
                log.debug("registerWithHue response data: " + theBody);
            	SuccessUserResponse[] theResponses = new Gson().fromJson(theBody, SuccessUserResponse[].class); //read content for data, SuccessUserResponse[].class);
            	theUser = theResponses[0].getSuccess().getUsername();
                responseValue = true;
            }
            EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
        } catch (IOException e) {
        	log.warn("Error loggin into HUE: IOException in log", e);
        }
        return responseValue;
    }
	public NamedIP getHueAddress() {
		return hueAddress;
	}

	public void setHueAddress(NamedIP hueAddress) {
		this.hueAddress = hueAddress;
	}
}
