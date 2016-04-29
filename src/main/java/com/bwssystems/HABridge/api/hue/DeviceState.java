package com.bwssystems.HABridge.api.hue;

// import java.util.ArrayList;
import java.util.List;

/**
 * Created by arm on 4/14/15.
 */
public class DeviceState {
    private boolean on;
    private int bri;
    private int hue;
    private int sat;
    private String effect;
    private int ct;
    private String alert;
    private String colormode;
    private boolean reachable;
    private List<Double> xy;

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
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public int getCt() {
        return ct;
    }

    public void setCt(int ct) {
        this.ct = ct;
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
    }
    public static DeviceState createDeviceState() {
    	DeviceState newDeviceState = new DeviceState();
    	newDeviceState.fillIn();
//    	newDeviceState.setColormode("none");
//    	ArrayList<Double> doubleArray = new ArrayList<Double>();
//    	doubleArray.add(new Double(0));
//    	doubleArray.add(new Double(0));
//    	newDeviceState.setXy(doubleArray);
    	
    	return newDeviceState;
    }
    public void fillIn() {
    	if(this.getAlert() == null)
    		this.setAlert("none");
    	if(this.getEffect() == null)
    		this.setEffect("none");
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
