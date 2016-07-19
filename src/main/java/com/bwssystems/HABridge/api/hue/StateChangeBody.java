package com.bwssystems.HABridge.api.hue;

// import java.util.ArrayList;
import java.util.List;

/**
 * Created by arm on 4/14/15.
 */
public class StateChangeBody {
    private boolean on;
    private int bri;
    private int hue;
    private int sat;
    private String effect;
    private int ct;
    private String alert;
    private List<Double> xy;
    private int transitiontime;
    private int bri_inc;
    private int hue_inc;
    private int sat_inc;
    private List<Double> xy_inc;
    private int ct_inc;

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

    public List<Double> getXy() {
        return xy;
    }

    public void setXy(List<Double> xy) {
        this.xy = xy;
    }

	public int getTransitiontime() {
		return transitiontime;
	}

	public void setTransitiontime(int transitiontime) {
		this.transitiontime = transitiontime;
	}

	public int getBri_inc() {
		return bri_inc;
	}

	public void setBri_inc(int bri_inc) {
		this.bri_inc = bri_inc;
	}

	public int getHue_inc() {
		return hue_inc;
	}

	public void setHue_inc(int hue_inc) {
		this.hue_inc = hue_inc;
	}

	public int getSat_inc() {
		return sat_inc;
	}

	public void setSat_inc(int sat_inc) {
		this.sat_inc = sat_inc;
	}

	public List<Double> getXy_inc() {
		return xy_inc;
	}

	public void setXy_inc(List<Double> xy_inc) {
		this.xy_inc = xy_inc;
	}

	public int getCt_inc() {
		return ct_inc;
	}

	public void setCt_inc(int ct_inc) {
		this.ct_inc = ct_inc;
	}
}
