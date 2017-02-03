
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("creationTime")
    @Expose
    private Long creationTime;
    @SerializedName("lastUpdateTime")
    @Expose
    private Long lastUpdateTime;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("deviceURL")
    @Expose
    private String deviceURL;
    @SerializedName("shortcut")
    @Expose
    private Boolean shortcut;
    @SerializedName("controllableName")
    @Expose
    private String controllableName;
    @SerializedName("metadata")
    @Expose
    private String metadata;
    @SerializedName("definition")
    @Expose
    private Definition definition;
    @SerializedName("states")
    @Expose
    private List<State_> states = null;
    @SerializedName("attributes")
    @Expose
    private List<Object> attributes = null;
    @SerializedName("available")
    @Expose
    private Boolean available;
    @SerializedName("enabled")
    @Expose
    private Boolean enabled;
    @SerializedName("placeOID")
    @Expose
    private String placeOID;
    @SerializedName("widget")
    @Expose
    private String widget;
    @SerializedName("type")
    @Expose
    private Long type;
    @SerializedName("oid")
    @Expose
    private String oid;
    @SerializedName("uiClass")
    @Expose
    private String uiClass;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDeviceURL() {
        return deviceURL;
    }

    public void setDeviceURL(String deviceURL) {
        this.deviceURL = deviceURL;
    }

    public Boolean getShortcut() {
        return shortcut;
    }

    public void setShortcut(Boolean shortcut) {
        this.shortcut = shortcut;
    }

    public String getControllableName() {
        return controllableName;
    }

    public void setControllableName(String controllableName) {
        this.controllableName = controllableName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public List<State_> getStates() {
        return states;
    }

    public void setStates(List<State_> states) {
        this.states = states;
    }

    public List<Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Object> attributes) {
        this.attributes = attributes;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPlaceOID() {
        return placeOID;
    }

    public void setPlaceOID(String placeOID) {
        this.placeOID = placeOID;
    }

    public String getWidget() {
        return widget;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getUiClass() {
        return uiClass;
    }

    public void setUiClass(String uiClass) {
        this.uiClass = uiClass;
    }

}
