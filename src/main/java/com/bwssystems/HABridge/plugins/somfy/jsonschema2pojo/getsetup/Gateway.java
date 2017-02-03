
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Gateway {

    @SerializedName("gatewayId")
    @Expose
    private String gatewayId;
    @SerializedName("type")
    @Expose
    private Long type;
    @SerializedName("subType")
    @Expose
    private Long subType;
    @SerializedName("placeOID")
    @Expose
    private String placeOID;
    @SerializedName("alive")
    @Expose
    private Boolean alive;
    @SerializedName("timeReliable")
    @Expose
    private Boolean timeReliable;
    @SerializedName("connectivity")
    @Expose
    private Connectivity connectivity;
    @SerializedName("upToDate")
    @Expose
    private Boolean upToDate;
    @SerializedName("mode")
    @Expose
    private String mode;
    @SerializedName("functions")
    @Expose
    private String functions;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public Long getSubType() {
        return subType;
    }

    public void setSubType(Long subType) {
        this.subType = subType;
    }

    public String getPlaceOID() {
        return placeOID;
    }

    public void setPlaceOID(String placeOID) {
        this.placeOID = placeOID;
    }

    public Boolean getAlive() {
        return alive;
    }

    public void setAlive(Boolean alive) {
        this.alive = alive;
    }

    public Boolean getTimeReliable() {
        return timeReliable;
    }

    public void setTimeReliable(Boolean timeReliable) {
        this.timeReliable = timeReliable;
    }

    public Connectivity getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(Connectivity connectivity) {
        this.connectivity = connectivity;
    }

    public Boolean getUpToDate() {
        return upToDate;
    }

    public void setUpToDate(Boolean upToDate) {
        this.upToDate = upToDate;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFunctions() {
        return functions;
    }

    public void setFunctions(String functions) {
        this.functions = functions;
    }

}
