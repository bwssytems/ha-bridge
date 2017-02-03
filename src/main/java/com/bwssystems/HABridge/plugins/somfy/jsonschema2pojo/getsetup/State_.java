
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class State_ {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private Long type;
    @SerializedName("value")
    @Expose
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
