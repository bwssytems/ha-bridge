package com.bwssystems.HABridge.api.hue;


/**
 * Created by arm on 4/14/15.
 */
public class DeviceState {
    private boolean on;
    private int bri = 255;
    private String effect;
    private String alert;
    private boolean reachable;

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getBri() {
        return bri;
    }

    public void setBri(int bri) {
        this.bri = bri;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    @Override
    public String toString() {
        return "DeviceState{" +
                "on=" + on +
                ", bri=" + bri +
                '}';
    }
}
