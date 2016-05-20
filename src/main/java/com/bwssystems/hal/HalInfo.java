package com.bwssystems.hal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
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

public class HalInfo {
    private static final Logger log = LoggerFactory.getLogger(HalInfo.class);
    private static final String LIGHTS_REQUEST = "/DeviceData!DeviceCmd=GetNames!DeviceType=Light?Token=";
    private HttpClient httpClient;
    private NamedIP halAddress;
	private String theToken;

    public HalInfo(NamedIP addressName, String aGivenToken) {
		super();
        httpClient = HttpClients.createDefault();
        halAddress = addressName;
        theToken = aGivenToken;
	}

	public List<HalDevice> getLights() {
		DeviceElements theHalApiResponse = null;
		List<HalDevice> deviceList = null;

		String theUrl = null;
    	String theData;
   		theUrl = "http://" + halAddress.getIp() + LIGHTS_REQUEST + theToken;
   		theData = doHttpGETRequest(theUrl);
    	if(theData != null) {
    		log.debug("GET HalApiResponse - data: " + theData);
	    	theHalApiResponse = new Gson().fromJson(theData, DeviceElements.class);
	    	deviceList = new ArrayList<HalDevice>();
	    	
	    	Iterator<DeviceName> theDeviceNames = theHalApiResponse.getDeviceElements().iterator();
	    	while(theDeviceNames.hasNext()) {
	    		DeviceName theDevice = theDeviceNames.next();
				HalDevice aNewHalDevice = new HalDevice();
				aNewHalDevice.setHaldevicetype("lights");
				aNewHalDevice.setHaldevicename(theDevice.getDeviceName());
				deviceList.add(aNewHalDevice);
	    		
	    	}
    	}
    	else {
    		log.warn("GET HalApiResponse for " + halAddress.getName() + " - returned null, no data.");
    	}
    	return deviceList;
    }

	//	This function executes the url against the hal
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

	public NamedIP getHalAddress() {
		return halAddress;
	}

	public void setHalAddress(NamedIP halAddress) {
		this.halAddress = halAddress;
	}

}
