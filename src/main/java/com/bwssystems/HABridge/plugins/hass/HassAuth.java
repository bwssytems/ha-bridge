package com.bwssystems.HABridge.plugins.hass;

public class HassAuth {
    boolean legacyauth;

    public boolean isLegacyauth() {
        return legacyauth;
    }

    public void setLegacyauth(boolean legacyauth) {
        this.legacyauth = legacyauth;
    }
}