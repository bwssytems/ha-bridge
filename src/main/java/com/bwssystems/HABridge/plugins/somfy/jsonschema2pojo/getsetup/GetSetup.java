
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetSetup {

    @SerializedName("setup")
    @Expose
    private Setup setup;
    @SerializedName("events")
    @Expose
    private List<Object> events = null;

    public Setup getSetup() {
        return setup;
    }

    public void setSetup(Setup setup) {
        this.setup = setup;
    }

    public List<Object> getEvents() {
        return events;
    }

    public void setEvents(List<Object> events) {
        this.events = events;
    }

}
