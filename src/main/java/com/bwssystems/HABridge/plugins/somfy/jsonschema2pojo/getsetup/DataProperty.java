
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataProperty {

    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("qualifiedName")
    @Expose
    private String qualifiedName;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

}
