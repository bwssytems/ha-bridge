package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class Device {
    private String roomName;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("roomID")
    private int roomID;

    @SerializedName("type")
    private String type;

    @SerializedName("properties")
    private DeviceProperties properties;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRoomID() {
        return roomID;
    }

    public String getType() {
        return type;
    }

    public DeviceProperties getProperties() {
        return properties;
    }

    public boolean isThermostat() {
        return type.equals("com.fibaro.setPoint") || type.equals("com.fibaro.thermostatDanfoss")
                || type.equals("com.fibaro.thermostatHorstmann");
    }

    @Override
    public String toString() {
        return "{" + id + ", " + name + "}";
    }

		public String fibaroaddress;
		public String fibaroport;
		public String fibaroAuth;
		public String fibaroname;
}
