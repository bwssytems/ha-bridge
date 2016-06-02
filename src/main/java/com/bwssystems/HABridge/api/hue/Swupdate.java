package com.bwssystems.HABridge.api.hue;


public class Swupdate
{
	private Integer updatestate;
	private Boolean checkforupdate;
	private DeviceTypes devicetypes;
	private String text;
	private Boolean notify;
	private String url;

	public static Swupdate createSwupdate() {
		Swupdate aSwupdate = new Swupdate();
		aSwupdate.setUpdatestate(0);
		aSwupdate.setCheckforupdate(false);
		aSwupdate.setDevicetypes(new DeviceTypes());
		aSwupdate.setNotify(false);
		aSwupdate.setText("");
		aSwupdate.setUrl("");
		return aSwupdate;
	}
	
	public Boolean getCheckforupdate() {
		return checkforupdate;
	}
	public void setCheckforupdate(Boolean checkforupdate) {
		this.checkforupdate = checkforupdate;
	}
	public DeviceTypes getDevicetypes() {
		return devicetypes;
	}
	public void setDevicetypes(DeviceTypes devicetypes) {
		this.devicetypes = devicetypes;
	}
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Boolean getNotify() {
		return notify;
	}

	public void setNotify(Boolean notify) {
		this.notify = notify;
	}

	public Integer getUpdatestate() {
		return updatestate;
	}

	public void setUpdatestate(Integer updatestate) {
		this.updatestate = updatestate;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
