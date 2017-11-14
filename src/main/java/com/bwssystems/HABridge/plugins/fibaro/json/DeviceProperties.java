package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class DeviceProperties {
    @SerializedName("batteryLevel")
    private String batteryLevel;

    @SerializedName("UIMessageSendTime")
    private String UIMessageSendTime;

    @SerializedName("autoConfig")
    private String autoConfig;

    @SerializedName("color")
    private String color;

    @SerializedName("date")
    private String date;

    @SerializedName("dead")
    private String dead;

    @SerializedName("deviceControlType")
    private String deviceControlType;

    @SerializedName("deviceIcon")
    private String deviceIcon;

    @SerializedName("disabled")
    private String disabled;

    @SerializedName("emailNotificationID")
    private String emailNotificationID;

    @SerializedName("emailNotificationType")
    private String emailNotificationType;

    @SerializedName("endPoint")
    private String endPoint;

    @SerializedName("energy")
    private String energy;

    @SerializedName("liliOffCommand")
    private String liliOffCommand;

    @SerializedName("liliOnCommand")
    private String liliOnCommand;

    @SerializedName("log")
    private String log;

    @SerializedName("logTemp")
    private String logTemp;

    @SerializedName("manufacturer")
    private String manufacturer;

    @SerializedName("markAsDead")
    private String markAsDead;

    @SerializedName("mode")
    private String mode;

    @SerializedName("model")
    private String model;

    @SerializedName("nodeID")
    private String nodeID;

    @SerializedName("pollingDeadDevice")
    private String pollingDeadDevice;

    @SerializedName("pollingTime")
    private String pollingTime;

    @SerializedName("pollingTimeNext")
    private String pollingTimeNext;

    @SerializedName("pollingTimeSec")
    private int pollingTimeSec;

    @SerializedName("power")
    private String power;

    @SerializedName("productInfo")
    private String productInfo;

    @SerializedName("pushNotificationID")
    private String pushNotificationID;

    @SerializedName("pushNotificationType")
    private String pushNotificationType;

    @SerializedName("remoteGatewayId")
    private String remoteGatewayId;

    @SerializedName("requestNodeNeighborStat")
    private String requestNodeNeighborStat;

    @SerializedName("requestNodeNeighborStatTimeStemp")
    private String requestNodeNeighborStatTimeStemp;

    @SerializedName("requestNodeNeighborState")
    private String requestNodeNeighborState;

    @SerializedName("requestNodeNeighborStateTimeStemp")
    private String requestNodeNeighborStateTimeStemp;

    @SerializedName("saveLogs")
    private String saveLogs;

		@SerializedName("showChildren")
    private String showChildren;

    @SerializedName("smsNotificationID")
    private String smsNotificationID;

    @SerializedName("smsNotificationType")
    private String smsNotificationType;

    @SerializedName("supportedModes")
    private String supportedModes;

    @SerializedName("targetLevel")
    private String targetLevel;

    @SerializedName("unit")
    private String unit;

    @SerializedName("useTemplate")
    private String useTemplate;

    @SerializedName("status")
    private String status;

    @SerializedName("sunriseHour")
    private String sunriseHour;

    @SerializedName("sunsetHour")
    private String sunsetHour;

    @SerializedName("userDescription")
    private String userDescription;

    @SerializedName("value")
    private String value;

    @SerializedName("zwaveBuildVersion")
    private String zwaveBuildVersion;

    @SerializedName("zwaveCompany")
    private String zwaveCompany;

    @SerializedName("zwaveInfo")
    private String zwaveInfo;

    @SerializedName("zwaveRegion")
    private String zwaveRegion;

    @SerializedName("zwaveVersion")
    private double zwaveVersion;

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public String getColor() {
        return color;
    }

    public String getDeviceControlType() {
        return deviceControlType;
    }

    public String getEnergy() {
        return energy;
    }

    public String getPower() {
        return power;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public String getValue() {
        return value;
    }

    // --- begin yrWeather plugin ---
    @SerializedName("Humidity")
    private String Humidity;

    @SerializedName("Pressure")
    private String Pressure;

    @SerializedName("Temperature")
    private String Temperature;

    @SerializedName("WeatherCondition")
    private String WeatherCondition;

    @SerializedName("Wind")
    private String Wind;

    public String getHumidity() {
        return Humidity;
    }

    public String getPressure() {
        return Pressure;
    }
    
    public String getSaveLogs()
		{
			return saveLogs;
		}

    public String getTemperature() {
        return Temperature;
    }

    public String getWeatherCondition() {
        return WeatherCondition;
    }

    public String getWind() {
        return Wind;
    }
    // --- end yrWeather plugin ---

		public String getUserDescription()
		{
			return userDescription;
		}

		public void setUserDescription(String userDescription)
		{
			this.userDescription = userDescription;
		}
}
