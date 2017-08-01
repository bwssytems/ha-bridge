package com.bwssystems.HABridge.api.hue;

import java.util.ArrayList;

public class GroupClassTypes {

	public final static String BATHROOM = "Bathroom";
	public final static String BEDROOM = "Bedroom";
	public final static String CARPORT = "Carport";
	public final static String DINING = "Dining";
	public final static String DRIVEWAY = "Driveway";
	public final static String FRONT_DOOR = "Front door";
	public final static String GARAGE = "Garage";
	public final static String GARDEN = "Garden";
	public final static String GYM = "Gym";
	public final static String HALLWAY = "Hallway";
	public final static String BEDROOM_KIDS = "Kids bedroom";
	public final static String KITCHEN = "Kitchen";
	public final static String LIVING_ROOM = "Living room";
	public final static String NURSERY = "Nursery";
	public final static String OFFICE = "Office";
	public final static String OTHER = "Other";
	public final static String RECREATION = "Recreation";
	public final static String TERRACE = "Terrace";
	public final static String TOILET = "Toilet";
	
	ArrayList<String> groupClassTypes;

	public GroupClassTypes() {
		groupClassTypes = new ArrayList<String>();
		groupClassTypes.add(BATHROOM);
		groupClassTypes.add(BEDROOM);
		groupClassTypes.add(CARPORT);
		groupClassTypes.add(DINING);
		groupClassTypes.add(DRIVEWAY);
		groupClassTypes.add(FRONT_DOOR);
		groupClassTypes.add(GARAGE);
		groupClassTypes.add(GARDEN);
		groupClassTypes.add(GYM);
		groupClassTypes.add(HALLWAY);
		groupClassTypes.add(BEDROOM_KIDS);
		groupClassTypes.add(KITCHEN);
		groupClassTypes.add(LIVING_ROOM);
		groupClassTypes.add(NURSERY);
		groupClassTypes.add(OFFICE);
		groupClassTypes.add(OTHER);
		groupClassTypes.add(RECREATION);
		groupClassTypes.add(TERRACE);
		groupClassTypes.add(TOILET);
	}

	public Boolean validateType(String type) {
		if(type == null || type.trim().isEmpty())
			return false;
		for(String classType : groupClassTypes) {
			if(type.trim().contentEquals(classType))
				return true;
		}
		return false;
	}
}