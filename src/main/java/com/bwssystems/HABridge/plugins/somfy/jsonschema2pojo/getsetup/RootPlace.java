
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RootPlace {

    @SerializedName("creationTime")
    @Expose
    private Long creationTime;
    @SerializedName("lastUpdateTime")
    @Expose
    private Long lastUpdateTime;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("type")
    @Expose
    private Long type;
    @SerializedName("oid")
    @Expose
    private String oid;
    @SerializedName("subPlaces")
    @Expose
    private List<SubPlace> subPlaces = null;

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public List<SubPlace> getSubPlaces() {
        return subPlaces;
    }

    public void setSubPlaces(List<SubPlace> subPlaces) {
        this.subPlaces = subPlaces;
    }

}
