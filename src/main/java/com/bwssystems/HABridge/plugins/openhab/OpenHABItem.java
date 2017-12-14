
package com.bwssystems.HABridge.plugins.openhab;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OpenHABItem {

    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("tags")
    @Expose
    private List<Object> tags = null;
    @SerializedName("groupNames")
    @Expose
    private List<Object> groupNames = null;
    @SerializedName("stateDescription")
    @Expose
    private StateDescription stateDescription;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public List<Object> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<Object> groupNames) {
        this.groupNames = groupNames;
    }

    public StateDescription getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(StateDescription stateDescription) {
        this.stateDescription = stateDescription;
    }

}
