
package com.bwssystems.HABridge.plugins.domoticz;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SceneResult {

    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Favorite")
    @Expose
    private Integer favorite;
    @SerializedName("LastUpdate")
    @Expose
    private String lastUpdate;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("OffAction")
    @Expose
    private String offAction;
    @SerializedName("OnAction")
    @Expose
    private String onAction;
    @SerializedName("Protected")
    @Expose
    private Boolean _protected;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("Timers")
    @Expose
    private String timers;
    @SerializedName("Type")
    @Expose
    private String type;
    @SerializedName("UsedByCamera")
    @Expose
    private Boolean usedByCamera;
    @SerializedName("idx")
    @Expose
    private String idx;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFavorite() {
        return favorite;
    }

    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOffAction() {
        return offAction;
    }

    public void setOffAction(String offAction) {
        this.offAction = offAction;
    }

    public String getOnAction() {
        return onAction;
    }

    public void setOnAction(String onAction) {
        this.onAction = onAction;
    }

    public Boolean getProtected() {
        return _protected;
    }

    public void setProtected(Boolean _protected) {
        this._protected = _protected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimers() {
        return timers;
    }

    public void setTimers(String timers) {
        this.timers = timers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getUsedByCamera() {
        return usedByCamera;
    }

    public void setUsedByCamera(Boolean usedByCamera) {
        this.usedByCamera = usedByCamera;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

}
