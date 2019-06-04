
package com.bwssystems.HABridge.plugins.homegenie;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Property {

    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Value")
    @Expose
    private String value;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("FieldType")
    @Expose
    private String fieldType;
    @SerializedName("UpdateTime")
    @Expose
    private String updateTime;
    @SerializedName("NeedsUpdate")
    @Expose
    private Boolean needsUpdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(Boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

}
