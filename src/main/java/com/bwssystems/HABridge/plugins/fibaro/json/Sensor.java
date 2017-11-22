package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class Sensor {
    @SerializedName("temperature")
    private int temperature;

    @SerializedName("humidity")
    private int humidity;

    @SerializedName("light")
    private int light;
}
