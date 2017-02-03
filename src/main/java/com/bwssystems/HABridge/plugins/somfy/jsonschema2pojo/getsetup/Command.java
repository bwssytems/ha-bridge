
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Command {

    @SerializedName("commandName")
    @Expose
    private String commandName;
    @SerializedName("nparams")
    @Expose
    private Long nparams;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public Long getNparams() {
        return nparams;
    }

    public void setNparams(Long nparams) {
        this.nparams = nparams;
    }

}
