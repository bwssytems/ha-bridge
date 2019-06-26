package com.bwssystems.HABridge.plugins.homegenie;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModuleTypes {

    @SerializedName("moduleTypes")
    @Expose
    private List<ModuleType> moduleTypes = null;

    public List<ModuleType> getModuleTypes() {
        return moduleTypes;
    }

    public void setModuleTypes(List<ModuleType> moduleTypes) {
        this.moduleTypes = moduleTypes;
    }

}