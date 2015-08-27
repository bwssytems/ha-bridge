package com.bwssystems.vera;

import java.io.IOException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.luupRequests.Categorie;
import com.bwssystems.luupRequests.Device;
import com.bwssystems.luupRequests.Room;
import com.bwssystems.luupRequests.Scene;
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
        denormalizeSdata(theSdata);
    	return theSdata;
    }
	
	private void denormalizeSdata(Sdata theSdata) {
		Map<String,Room> roomMap = new HashMap<String,Room>();
		for (Room i : theSdata.getRooms()) roomMap.put(i.getId(),i);
		Map<String,Categorie> categoryMap = new HashMap<String,Categorie>();
		for (Categorie i : theSdata.getCategoriess()) categoryMap.put(i.getId(),i);
		Categorie controllerCat = new Categorie();
		controllerCat.setName("Controller");
		controllerCat.setId("0");
		categoryMap.put(controllerCat.getId(),controllerCat);
		ListIterator<Device> theIterator = theSdata.getDevices().listIterator();
		Device theDevice = null;
		while (theIterator.hasNext()) {
			theDevice = theIterator.next();
			if(theDevice.getRoom() != null && roomMap.get(theDevice.getRoom()) != null)
				theDevice.setRoom(roomMap.get(theDevice.getRoom()).getName());
			else
				theDevice.setRoom("no room");
			if(theDevice.getCategory() != null && categoryMap.get(theDevice.getCategory()) != null)
				theDevice.setCategory(categoryMap.get(theDevice.getCategory()).getName());
			else
				theDevice.setCategory("<unknown>");
		}

		ListIterator<Scene> theSecneIter = theSdata.getScenes().listIterator();
		Scene theScene = null;
		while (theSecneIter.hasNext()) {
			theScene = theSecneIter.next();
			theScene.setRoom(roomMap.get(theScene.getRoom()).getName());
		}
	}

	//	This function executes the url against the vera
    protected String doHttpGETRequest(String url) {
        log.debug("calling GET on URL: " + url);
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            String theContent = EntityUtils.toString(response.getEntity()); //read content for data
            EntityUtils.consume(response.getEntity()); //close out inputstream ignore content
            log.debug("GET on URL responded: " + response.getStatusLine().getStatusCode());
            if(response.getStatusLine().getStatusCode() == 200){
                return theContent;
            }
        } catch (IOException e) {
            log.error("Error calling out to HA gateway", e);
        }
        return null;
    }
}
