package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class Room {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("sectionID")
    private int sectionID;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

		public int getSectionID()
		{
			return sectionID;
		}
}