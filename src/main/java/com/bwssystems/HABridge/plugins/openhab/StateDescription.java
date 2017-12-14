
package com.bwssystems.HABridge.plugins.openhab;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StateDescription {

    @SerializedName("pattern")
    @Expose
    private String pattern;
    @SerializedName("readOnly")
    @Expose
    private Boolean readOnly;
    @SerializedName("options")
    @Expose
    private List<Option> options = null;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

}
