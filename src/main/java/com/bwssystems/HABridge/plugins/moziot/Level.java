
package com.bwssystems.HABridge.plugins.moziot;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Level {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("@type")
    @Expose
    private String attype;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("minimum")
    @Expose
    private Integer minimum;
    @SerializedName("maximum")
    @Expose
    private Integer maximum;
    @SerializedName("links")
    @Expose
    private List<Link> links = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttype() {
        return attype;
    }

    public void setAttype(String attype) {
        this.attype = attype;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getMinimum() {
        return minimum;
    }

    public void setMinimum(Integer minimum) {
        this.minimum = minimum;
    }

    public Integer getMaximum() {
        return maximum;
    }

    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
