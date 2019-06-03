package com.bwssystems.HABridge.plugins.moziot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MozIotDevice {
    @SerializedName("gatewayName")
    @Expose
    private String gatewayName;
    @SerializedName("deviceDetail")
    @Expose
    private MozillaThing deviceDetail;

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public MozillaThing getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(MozillaThing deviceDetail) {
        this.deviceDetail = deviceDetail;
    }
}