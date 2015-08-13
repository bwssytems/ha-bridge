package com.bwssystems.vera;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.luupRequests.Sdata;
import com.google.gson.Gson;


public class VeraInfo {
    private static final Logger log = LoggerFactory.getLogger(VeraInfo.class);
    private HttpClient httpClient;
    private static final String SDATA_REQUEST = ":3480/data_request?id=sdata&output_format=json";
    private String veraAddressString;

    public VeraInfo(String addressString) {
		super();
        httpClient = HttpClients.createMinimal();
        veraAddressString = addressString;
	}
    
	public Sdata getSdata() {
		String theUrl = "http://" + veraAddressString + SDATA_REQUEST;
    	String theData;
    	
    	theData = doHttpGETRequest(theUrl);
    	Sdata theSdata = new Gson().fromJson(theData, Sdata.class);
        log.debug("GET sdata - full: " + theSdata.getFull() + ", version: " + theSdata.getVersion());
    	return theSdata;
    }
	
//	This function executes the url against the vera
    protected String doHttpGETRequest(String url) {
        log.info("calling GET on URL: " + url);
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            String theContent = EntityUtils.toString(response.getEntity()); //read content for data
            EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
            log.info("GET on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                return theContent;
            }
        } catch (IOException e) {
            log.error("Error calling out to HA gateway", e);
        }
        return null;
    }
}
