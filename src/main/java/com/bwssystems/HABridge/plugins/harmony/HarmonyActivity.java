package com.bwssystems.HABridge.plugins.harmony;

import java.io.UnsupportedEncodingException;

import net.whistlingfish.harmony.config.Activity;

public class HarmonyActivity {
	private String hub;
	private Activity activity;
	public String getHub() {
		return hub;
	}
	public void setHub(String hub) {
		this.hub = hub;
	}
	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		byte ptext[];
		String theLabel = activity.getLabel();
		try {
			ptext = theLabel.getBytes("ISO-8859-1");
			activity.setLabel(new String(ptext, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			activity.setLabel(theLabel);
		} 
		this.activity = activity;
	}

}
