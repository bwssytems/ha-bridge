package com.bwssystems.fibaro.test;

import com.bwssystems.HABridge.plugins.fibaro.FibaroTestData;
import com.bwssystems.HABridge.plugins.fibaro.json.Scene;
import com.google.gson.Gson;

public class SceneDataConstructor {

	public static void main(String[] args){
		SceneDataConstructor aTestService = new SceneDataConstructor();
		if(aTestService.validateStructure())
			System.out.println("Test Successful");
	}

	public Boolean validateStructure() {
		String theData = FibaroTestData.SceneTestData;
		try {
			Scene[] all_scenes = new Gson().fromJson(theData, Scene[].class);
			for(int i = 0; i < all_scenes.length; i++) {
				System.out.println(all_scenes[i].getName() + " " + all_scenes[i].getId() + " " + all_scenes[i].getRoomName());
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
