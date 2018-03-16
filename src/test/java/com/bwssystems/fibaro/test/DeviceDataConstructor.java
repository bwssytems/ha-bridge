package com.bwssystems.fibaro.test;

import com.bwssystems.HABridge.plugins.fibaro.FibaroTestData;
import com.bwssystems.HABridge.plugins.fibaro.json.Device;
import com.google.gson.Gson;

public class DeviceDataConstructor {

	public static void main(String[] args){
		DeviceDataConstructor aTestService = new DeviceDataConstructor();
		if(aTestService.validateStructure())
			System.out.println("Test Successful");
	}

	public Boolean validateStructure() {
		String theData = FibaroTestData.DeviceTestData;
		try {
			Device[] decodeTarget = new Gson().fromJson(theData, Device[].class);
			for(int i = 0; i < decodeTarget.length; i++) {
				System.out.println(decodeTarget[i].getName() + " " + decodeTarget[i].getId() + " " + decodeTarget[i].getRoomName());
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
