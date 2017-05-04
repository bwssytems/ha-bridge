package com.bwssystems.HABridge.plugins.fibaro;

public class Room
{
	public int id;
	public String name;
	public int sectionID;
	public String icon;
	public Sensor defaultSensors;
	public int defaultThermostat;
	public int sortOrder;

	public class Sensor
	{
		public int temperature;
		public int humidity;
		public int light;
	}
}