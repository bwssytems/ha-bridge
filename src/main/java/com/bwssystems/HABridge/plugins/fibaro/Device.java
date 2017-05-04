package com.bwssystems.HABridge.plugins.fibaro;

public class Device
{
	public int id;
	public String name;
	public int roomID;
	public String type;
	public String baseType;
	public boolean enabled;
	public boolean visible;
	public boolean isPlugin;
	public int parentId;
	public int remoteGatewayId;
	public boolean viewXml;
	public boolean configXml;
	public Object interfaces;
	public Properties properties;
	public Object actions;
	public int created;
	public int modified;
	public int sortOrder;

	public class Properties {
		public String UIMessageSendTime;
		public String autoConfig;
		public String date;
		public String dead;
		public String deviceControlType;
		public String deviceIcon;
		public String disabled;
		public String emailNotificationID;
		public String emailNotificationType;
		public String endPoint;
		public String liliOffCommand;
		public String liliOnCommand;
		public String log;
		public String logTemp;
		public String manufacturer;
		public String markAsDead;
		public String model;
		public String nodeID;
		public String pollingDeadDevice;
		public String pollingTime;
		public String pollingTimeNext;
		public int pollingTimeSec;
		public String productInfo;
		public String pushNotificationID;
		public String pushNotificationType;
		public String remoteGatewayId;
		public String requestNodeNeighborStat;
		public String requestNodeNeighborStatTimeStemp;
		public String requestNodeNeighborState;
		public String requestNodeNeighborStateTimeStemp;
		public String saveLogs;
		public String showChildren;
		public String smsNotificationID;
		public String smsNotificationType;
		public String status;
		public String sunriseHour;
		public String sunsetHour;
		public String userDescription;
		public String value;
		public String zwaveBuildVersion;
		public String zwaveCompany;
		public String zwaveInfo;
		public String zwaveRegion;
		public double zwaveVersion;
	}
	
	public String room;
	public String category;
	public String fibaroaddress;
	public String fibaroname;
}
