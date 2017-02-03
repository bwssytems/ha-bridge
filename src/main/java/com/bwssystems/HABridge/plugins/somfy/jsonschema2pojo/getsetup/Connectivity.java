
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Connectivity {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("protocolVersion")
    @Expose
    private String protocolVersion;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

}
