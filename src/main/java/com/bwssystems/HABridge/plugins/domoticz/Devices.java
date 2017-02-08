
package com.bwssystems.HABridge.plugins.domoticz;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Devices {

    @SerializedName("ActTime")
    @Expose
    private Integer actTime;
    @SerializedName("AllowWidgetOrdering")
    @Expose
    private Boolean allowWidgetOrdering;
    @SerializedName("ServerTime")
    @Expose
    private String serverTime;
    @SerializedName("Sunrise")
    @Expose
    private String sunrise;
    @SerializedName("Sunset")
    @Expose
    private String sunset;
    @SerializedName("result")
    @Expose
    private List<DeviceResult> result = null;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("title")
    @Expose
    private String title;

    public Integer getActTime() {
        return actTime;
    }

    public void setActTime(Integer actTime) {
        this.actTime = actTime;
    }

    public Boolean getAllowWidgetOrdering() {
        return allowWidgetOrdering;
    }

    public void setAllowWidgetOrdering(Boolean allowWidgetOrdering) {
        this.allowWidgetOrdering = allowWidgetOrdering;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public List<DeviceResult> getResult() {
        return result;
    }

    public void setResult(List<DeviceResult> result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
