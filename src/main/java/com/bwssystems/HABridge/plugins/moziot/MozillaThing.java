
package com.bwssystems.HABridge.plugins.moziot;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MozillaThing {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("@context")
    @Expose
    private String atcontext;
    @SerializedName("@type")
    @Expose
    private List<String> attype = null;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("href")
    @Expose
    private String href;
    @SerializedName("properties")
    @Expose
    private Properties properties;
    @SerializedName("actions")
    @Expose
    private Actions actions;
    @SerializedName("events")
    @Expose
    private Events events;
    @SerializedName("links")
    @Expose
    private List<Link> links = null;
    @SerializedName("layoutIndex")
    @Expose
    private Integer layoutIndex;
    @SerializedName("selectedCapability")
    @Expose
    private String selectedCapability;
    @SerializedName("iconHref")
    @Expose
    private Object iconHref;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAtcontext() {
        return atcontext;
    }

    public void setAtcontext(String atcontext) {
        this.atcontext = atcontext;
    }

    public List<String> getAttype() {
        return attype;
    }

    public void setAttype(List<String> attype) {
        this.attype = attype;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    public Events getEvents() {
        return events;
    }

    public void setEvents(Events events) {
        this.events = events;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public Integer getLayoutIndex() {
        return layoutIndex;
    }

    public void setLayoutIndex(Integer layoutIndex) {
        this.layoutIndex = layoutIndex;
    }

    public String getSelectedCapability() {
        return selectedCapability;
    }

    public void setSelectedCapability(String selectedCapability) {
        this.selectedCapability = selectedCapability;
    }

    public Object getIconHref() {
        return iconHref;
    }

    public void setIconHref(Object iconHref) {
        this.iconHref = iconHref;
    }

}
