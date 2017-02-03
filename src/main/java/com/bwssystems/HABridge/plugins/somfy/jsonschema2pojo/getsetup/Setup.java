
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Setup {

    @SerializedName("creationTime")
    @Expose
    private Long creationTime;
    @SerializedName("lastUpdateTime")
    @Expose
    private Long lastUpdateTime;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("gateways")
    @Expose
    private List<Gateway> gateways = null;
    @SerializedName("devices")
    @Expose
    private List<Device> devices = null;
    @SerializedName("zones")
    @Expose
    private List<Object> zones = null;
    @SerializedName("resellerDelegationType")
    @Expose
    private String resellerDelegationType;
    @SerializedName("rootPlace")
    @Expose
    private RootPlace rootPlace;
    @SerializedName("features")
    @Expose
    private List<Feature> features = null;

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Gateway> getGateways() {
        return gateways;
    }

    public void setGateways(List<Gateway> gateways) {
        this.gateways = gateways;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<Object> getZones() {
        return zones;
    }

    public void setZones(List<Object> zones) {
        this.zones = zones;
    }

    public String getResellerDelegationType() {
        return resellerDelegationType;
    }

    public void setResellerDelegationType(String resellerDelegationType) {
        this.resellerDelegationType = resellerDelegationType;
    }

    public RootPlace getRootPlace() {
        return rootPlace;
    }

    public void setRootPlace(RootPlace rootPlace) {
        this.rootPlace = rootPlace;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

}
