
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Definition {

    @SerializedName("commands")
    @Expose
    private List<Command> commands = null;
    @SerializedName("events")
    @Expose
    private List<Object> events = null;
    @SerializedName("states")
    @Expose
    private List<State> states = null;
    @SerializedName("dataProperties")
    @Expose
    private List<DataProperty> dataProperties = null;
    @SerializedName("widgetName")
    @Expose
    private String widgetName;
    @SerializedName("uiClass")
    @Expose
    private String uiClass;
    @SerializedName("qualifiedName")
    @Expose
    private String qualifiedName;
    @SerializedName("type")
    @Expose
    private String type;

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    public List<Object> getEvents() {
        return events;
    }

    public void setEvents(List<Object> events) {
        this.events = events;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public List<DataProperty> getDataProperties() {
        return dataProperties;
    }

    public void setDataProperties(List<DataProperty> dataProperties) {
        this.dataProperties = dataProperties;
    }

    public String getWidgetName() {
        return widgetName;
    }

    public void setWidgetName(String widgetName) {
        this.widgetName = widgetName;
    }

    public String getUiClass() {
        return uiClass;
    }

    public void setUiClass(String uiClass) {
        this.uiClass = uiClass;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
