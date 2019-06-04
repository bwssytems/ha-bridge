package com.bwssystems.HABridge.plugins.homegenie;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HomeGenieDevice {
    @SerializedName("gatewayName")
    @Expose
    private String gatewayName;
    @SerializedName("deviceDetail")
    @Expose
    private Module deviceDetail;

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Module getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(Module deviceDetail) {
        this.deviceDetail = deviceDetail;
    }
}