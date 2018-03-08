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

    @SerializedName("roomID")
    private int roomID;

    @SerializedName("liliStartCommand")
    private String liliStartCommand;

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