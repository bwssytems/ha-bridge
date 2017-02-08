package com.bwssystems.HABridge.plugins.vera;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.http.HTTPHandler;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Categorie;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Device;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Room;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Scene;
import com.bwssystems.HABridge.plugins.vera.luupRequests.Sdata;
import com.google.gson.Gson;


public class VeraInfo {
    private static final Logger log = LoggerFactory.getLogger(VeraInfo.class);
    private HTTPHandler httpClient;
    private static final String SDATA_REQUEST = ":3480/data_request?id=sdata&output_format=json";
    private NamedIP veraAddress;

    public VeraInfo(NamedIP addressName) {
		super();
        httpClient = new HTTPHandler();
        veraAddress = addressName;
	}
    
	public Sdata getSdata() {
    	Sdata theSdata = null;

		String theUrl = "http://" + veraAddress.getIp() + SDATA_REQUEST;
    	String theData;
    	
    	theData = httpClient.doHttpRequest(theUrl, null, null, null, null);
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
}
