package com.bwssystems.HABridge.plugins.fibaro.json;

import com.google.gson.annotations.SerializedName;

public class SceneTriggers {
    @SerializedName("properties")
    private SceneProperties[] properties;

    @SerializedName("globals")
    private String[] globals;

    @SerializedName("events")
    private String[] events;
}
