package com.bwssystems.HABridge.plugins.somfy;

public class SomfyDevice {
    private String id;
    private String room;
    private String category;
    private String somfyname;
    private String name;
    private String deviceUrl;
    private String deviceType;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setSomfyname(String somfyname) {
        this.somfyname = somfyname;
    }

    public String getSomfyname() {
        return somfyname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDeviceUrl(String deviceUrl) {
        this.deviceUrl = deviceUrl;
    }

    public String getDeviceUrl() {
        return deviceUrl;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }
}
