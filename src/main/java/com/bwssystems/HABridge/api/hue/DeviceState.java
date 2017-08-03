package com.bwssystems.HABridge.api.hue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arm on 4/14/15.
 */
public class DeviceState {
    private boolean on;
    private int bri;
    private Integer hue;
    private Integer sat;
    private String effect;
    private List<Double> xy;
    private Integer ct;
    private String alert;
    private String colormode;
    private boolean reachable;
    
//    private int transitiontime;

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

    public int getHue() {
        return hue != null ? hue.intValue() : 0;
    }

    public void setHue(int hue) {
        this.hue = hue;
        this.colormode = "hs";
    }

    public int getSat() {
        return sat != null ? sat.intValue() : 0;
    }

    public void setSat(int sat) {
        this.sat = sat;
        this.colormode = "hs";
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public int getCt() {
        return ct != null ? ct.intValue() : 0;
    }

    public void setCt(int ct) {
        this.ct = ct;
        this.colormode = "ct";
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getColormode() {
        return colormode;
    }

    public void setColormode(String colormode) {
        this.colormode = colormode;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public List<Double> getXy() {
        return xy;
    }

    public void setXy(List<Double> xy) {
        this.xy = xy;
        this.colormode = "xy";
    }
//    public int getTransitiontime() {
//		return transitiontime;
//	}

//	public void setTransitiontime(int transitiontime) {
//		this.transitiontime = transitiontime;
//	}

	public static DeviceState createDeviceState(boolean color) {
    	DeviceState newDeviceState = new DeviceState();
    	newDeviceState.fillIn(color);
        if (color) {
            newDeviceState.setColormode("xy");
            newDeviceState.setHue(0);
            newDeviceState.setSat(0);
            newDeviceState.setCt(153);
            ArrayList<Double> doubleArray = new ArrayList<Double>();
            doubleArray.add(0.3146);
            doubleArray.add(0.3303);
            newDeviceState.setXy(doubleArray);    
        }
    	return newDeviceState;
    }
    public void fillIn(boolean color) {
    	if(this.getAlert() == null)
    		this.setAlert("none");
        if (color) {
            if(this.getEffect() == null)
                this.setEffect("none");
        }
    	this.setReachable(true);
    }
    @Override
    public String toString() {
        return "DeviceState{" +
                "on=" + on +
                ", bri=" + bri +
                '}';
    }
}
