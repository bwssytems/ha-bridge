
package com.bwssystems.HABridge.plugins.fhem;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PossibleSets")
    @Expose
    private String possibleSets;
    @SerializedName("PossibleAttrs")
    @Expose
    private String possibleAttrs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPossibleSets() {
        return possibleSets;
    }

    public void setPossibleSets(String possibleSets) {
        this.possibleSets = possibleSets;
    }

    public String getPossibleAttrs() {
        return possibleAttrs;
    }

    public void setPossibleAttrs(String possibleAttrs) {
        this.possibleAttrs = possibleAttrs;
    }

}
