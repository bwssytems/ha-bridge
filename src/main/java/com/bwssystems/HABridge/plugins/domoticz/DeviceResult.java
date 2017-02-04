
package com.bwssystems.HABridge.plugins.domoticz;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceResult {
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Type")
    @Expose
    private String type;
    @SerializedName("idx")
    @Expose
    private String idx;



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

}
