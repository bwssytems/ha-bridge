package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class Scene {
    private String roomName;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("properties")
    private String properties;

    @SerializedName("roomID")
    private int roomID;

    @SerializedName("iconID")
    private int iconID;

    @SerializedName("runConfig")
    private String runConfig;

    @SerializedName("autostart")
    private boolean autostart;

    @SerializedName("protectedByPIN")
    private boolean protectedByPIN;

    @SerializedName("killable")
    private boolean killable;

    @SerializedName("maxRunningInstances")
    private int maxRunningInstances;

    @SerializedName("runningInstances")
    private int runningInstances;

    @SerializedName("visible")
    private boolean visible;

    @SerializedName("isLua")
    private boolean isLua;

    @SerializedName("triggers")
    private SceneTriggers triggers;

    @SerializedName("liliStartCommand")
    private String liliStartCommand;

    @SerializedName("liliStopCommand")
    private String liliStopCommand;

    @SerializedName("sortOrder")
    private int sortOrder;

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
    
		public String getLiliStartCommand()
		{
			return liliStartCommand;
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