package com.bwssystems.fibaro.test;

import com.bwssystems.HABridge.plugins.fibaro.FibaroTestData;
import com.bwssystems.HABridge.plugins.fibaro.json.Room;
import com.google.gson.Gson;

public class RoomDataConstructor {

	public static void main(String[] args){
		RoomDataConstructor aTestService = new RoomDataConstructor();
		if(aTestService.validateStructure())
			System.out.println("Test Successful");
	}

	public Boolean validateStructure() {
		String theData = FibaroTestData.RoomTestData;
		try {
			Room[] decodeTarget = new Gson().fromJson(theData, Room[].class);
			for(int i = 0; i < decodeTarget.length; i++) {
				System.out.println(decodeTarget[i].getName() + " " + decodeTarget[i].getId());
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
