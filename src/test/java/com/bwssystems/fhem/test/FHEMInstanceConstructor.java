package com.bwssystems.fhem.test;

import java.util.List;

import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.plugins.fhem.FHEMDevice;
import com.bwssystems.HABridge.plugins.fhem.FHEMInstance;
import com.bwssystems.HABridge.plugins.fhem.FHEMItem;
import com.bwssystems.HABridge.plugins.fhem.FHEMTestData;
import com.bwssystems.HABridge.plugins.fhem.Result;
import com.bwssystems.HABridge.plugins.http.HttpTestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FHEMInstanceConstructor {

	String csrfToken;

	public FHEMInstanceConstructor(){
		this(null);
	}

	public FHEMInstanceConstructor(String csrfToken){
		this.csrfToken = csrfToken;
	}

	public Boolean validateStructure() {
		Gson aGson;
		NamedIP anAddress = new NamedIP();
		anAddress.setName("TestData1");
		anAddress.setIp("10.0.0.1");
		anAddress.setCsrfToken(csrfToken);
		FHEMInstance anInstance = new FHEMInstance(anAddress);
		HttpTestHandler theHttpTestClient = new HttpTestHandler();
		List<Result> services = null;
		List<FHEMDevice> deviceList = null;
		String decodeData = null;
		String theTestData = null;
		
		for(int i = 0; i < 3; i++) {
			if(i == 0)
				theTestData = FHEMTestData.TestData;
			else if(i == 1) {
				theTestData = FHEMTestData.TestData2;
				anAddress.setName(anAddress.getName().replace("1", "2"));
				anInstance = new FHEMInstance(anAddress);
			}
			else {
				anAddress.setName(anAddress.getName().replace("2", "3"));
				theTestData = FHEMTestData.TestData3;
			}
			decodeData = anInstance.getJSONData(theTestData);
			try {
				aGson = new GsonBuilder()
		                .create();
				
				FHEMItem aService = aGson.fromJson(decodeData, FHEMItem.class);
				services = aService.getResults();
				for(Result aResult:services) {
					System.out.println(anAddress.getName() + " - Json Test:");
					System.out.println("    " +  aResult.getName());
					System.out.println("    	" + aResult.getPossibleSets());
				}
			} catch (Exception e) {
				return false;
			}
			System.out.println("----------------------------------");
			try {
				theHttpTestClient.updateTheData("jsonlist2", theTestData);
				deviceList = anInstance.getDevices(theHttpTestClient);
				if(deviceList == null)
					return false;
				for(FHEMDevice aDevice:deviceList) {
					System.out.println(aDevice.getName() + " - FHEMDevice Class Test:");
					System.out.println("    " + aDevice.getItem().getName());
					System.out.println("    " + aDevice.getCsrfToken());
					System.out.println("    	" + aDevice.getItem().getPossibleSets());
				}
			} catch (Exception e) {
				return false;
			}
			System.out.println("----------------------------------");
		}
		return true;
	}

}
