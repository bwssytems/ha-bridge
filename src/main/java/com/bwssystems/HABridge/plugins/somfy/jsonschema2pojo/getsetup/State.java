
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class State {

    @SerializedName("eventBased")
    @Expose
    private Boolean eventBased;
    @SerializedName("values")
    @Expose
    private List<String> values = null;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("qualifiedName")
    @Expose
    private String qualifiedName;

    public Boolean getEventBased() {
        return eventBased;
    }

    public void setEventBased(Boolean eventBased) {
        this.eventBased = eventBased;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

}
