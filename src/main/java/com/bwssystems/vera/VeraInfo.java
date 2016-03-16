package com.bwssystems.vera;

import java.io.IOException;
import java.nio.charset.Charset;
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

import com.bwssystems.HABridge.NamedIP;
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
    private NamedIP veraAddress;
    private Boolean validVera;

    public VeraInfo(NamedIP addressName, Boolean isValidVera) {
		super();
        httpClient = HttpClients.createDefault();
        veraAddress = addressName;
        validVera = isValidVera;
	}
    
	public Sdata getSdata() {
    	Sdata theSdata = null;
		if(!validVera)
			return theSdata;

		String theUrl = "http://" + veraAddress.getIp() + SDATA_REQUEST;
    	String theData;
    	
    	theData = doHttpGETRequest(theUrl);
    	if(theData != null) {
	    	theSdata = new Gson().fromJson(theData, Sdata.class);
	        log.debug("GET sdata - full: " + theSdata.getFull() + ", version: " + theSdata.getVersion());
	        denormalizeSdata(theSdata);
    	}
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
			theDevice.setVeraaddress(veraAddress.getIp());
			theDevice.setVeraname(veraAddress.getName());
		}

		ListIterator<Scene> theSecneIter = theSdata.getScenes().listIterator();
		Scene theScene = null;
		while (theSecneIter.hasNext()) {
			theScene = theSecneIter.next();
			if(theScene.getRoom() != null && roomMap.get(theScene.getRoom()) != null)
				theScene.setRoom(roomMap.get(theScene.getRoom()).getName());
			else
				theScene.setRoom("no room");
			theScene.setVeraaddress(veraAddress.getIp());
			theScene.setVeraname(veraAddress.getName());
		}
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
}
