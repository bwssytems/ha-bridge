package com.bwssystems.HABridge.plugins.homegenie;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModuleType {

    @SerializedName("moduleType")
    @Expose
    private String moduleType;

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

}