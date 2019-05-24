
package com.bwssystems.HABridge.plugins.moziot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("on")
    @Expose
    private On on;
    @SerializedName("level")
    @Expose
    private Level level;

    public On getOn() {
        return on;
    }

    public void setOn(On on) {
        this.on = on;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

}
