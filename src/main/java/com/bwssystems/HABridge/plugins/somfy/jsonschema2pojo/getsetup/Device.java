
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {
    
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("deviceURL")
    @Expose
    private String deviceURL;
    @SerializedName("widget")
    @Expose
    private String widget;
    @SerializedName("oid")
    @Expose
    private String oid;
    @SerializedName("uiClass")
    @Expose
    private String uiClass;


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
    
    
    public String getWidget() {
        return widget;
    }

    public void setWidget(String widget) {
        this.widget = widget;
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
