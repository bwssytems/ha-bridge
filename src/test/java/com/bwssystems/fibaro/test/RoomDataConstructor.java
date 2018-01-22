package com.bwssystems.fibaro.test;

import com.bwssystems.HABridge.plugins.fibaro.json.Room;
import com.google.gson.Gson;

public class RoomDataConstructor {
	public final static String TestData = "[{\"id\":4,\"name\":\"Dachboden\",\"sectionID\":4,\"icon\":\"room_bedroom\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":1},{\"id\":5,\"name\":\"Badezimmer\",\"sectionID\":5,\"icon\":\"room_wanna\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":2},{\"id\":6,\"name\":\"Büro\",\"sectionID\":5,\"icon\":\"room_office\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":3},{\"id\":7,\"name\":\"Flur\",\"sectionID\":5,\"icon\":\"room_schody2\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":4},{\"id\":8,\"name\":\"Schlafzimmer\",\"sectionID\":5,\"icon\":\"room_bedroom\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":5},{\"id\":9,\"name\":\"Emelie\",\"sectionID\":5,\"icon\":\"room_wardrobe\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":6},{\"id\":10,\"name\":\"Mino\",\"sectionID\":5,\"icon\":\"room_garage\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":7},{\"id\":11,\"name\":\"Flur\",\"sectionID\":6,\"icon\":\"room_drzwiwejsciowe\",\"defaultSensors\":{\"temperature\":181,\"humidity\":0,\"light\":182},\"defaultThermostat\":0,\"sortOrder\":8},{\"id\":12,\"name\":\"Wohnzimmer\",\"sectionID\":6,\"icon\":\"room_sofa\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":9},{\"id\":13,\"name\":\"Küche\",\"sectionID\":6,\"icon\":\"room_dining\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":10},{\"id\":14,\"name\":\"Esszimmer\",\"sectionID\":6,\"icon\":\"room_jadalnia\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":11},{\"id\":15,\"name\":\"HWR\",\"sectionID\":6,\"icon\":\"room_laundry\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":12},{\"id\":16,\"name\":\"Gäste WC\",\"sectionID\":6," + 
			"\"icon\":\"room_toilet\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":13},{\"id\":17,\"name\":\"Haus\",\"sectionID\":7,\"icon\":\"room_bedroom\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":14},{\"id\":18,\"name\":\"Terrasse\",\"sectionID\":7,\"icon\":\"room_bedroom\",\"defaultSensors\":{\"temperature\":0,\"humidity\":0,\"light\":0},\"defaultThermostat\":0,\"sortOrder\":15}]";
	public static void main(String[] args){
		RoomDataConstructor aTestService = new RoomDataConstructor();
		if(aTestService.validateStructure())
			System.out.println("Test Successful");
	}

	public Boolean validateStructure() {
		String theData = TestData;
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
