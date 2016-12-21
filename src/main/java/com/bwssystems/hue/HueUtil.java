package com.bwssystems.hue;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.api.SuccessUserResponse;
import com.bwssystems.HABridge.api.UserCreateRequest;
import com.google.gson.Gson;

public class HueUtil {
    private static final Logger log = LoggerFactory.getLogger(HueUtil.class);
	public static final String HUE_REQUEST = "/api";

	public static final String registerWithHue(HttpClient anHttpClient, String ipAddress, String aName, String theUser) {
    	UserCreateRequest theLogin = new UserCreateRequest();
        theLogin.setDevicetype("HABridge#MyMachine");
        HttpPost postRequest = new HttpPost("http://" + ipAddress + HUE_REQUEST);
        ContentType parsedContentType = ContentType.parse("application/json");
        StringEntity requestBody = new StringEntity(new Gson().toJson(theLogin), parsedContentType);
        HttpResponse response = null;
        postRequest.setEntity(requestBody);
        try {
            response = anHttpClient.execute(postRequest);
            log.debug("POST execute on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() >= 200  && response.getStatusLine().getStatusCode() < 300){
            	String theBody = EntityUtils.toString(response.getEntity());
                log.debug("registerWithHue response data: " + theBody);
                if(theBody.contains("[{\"error\":")) {
                	if(theBody.contains("link button not")) {
                		log.warn("registerWithHue needs link button pressed on HUE bridge: " + aName);
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
}
