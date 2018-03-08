
package com.bwssystems.HABridge.plugins.fhem;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FHEMItem {

    @SerializedName("Arg")
    @Expose
    private String arg;
    @SerializedName("Results")
    @Expose
    private List<Result> results = null;
    @SerializedName("totalResultsReturned")
    @Expose
    private Integer totalResultsReturned;

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Integer getTotalResultsReturned() {
        return totalResultsReturned;
    }

    public void setTotalResultsReturned(Integer totalResultsReturned) {
        this.totalResultsReturned = totalResultsReturned;
    }

}
