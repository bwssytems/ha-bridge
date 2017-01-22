package com.bwssystems.HABridge.plugins.vera.luupRequests;

import java.util.List;

public class Sdata {
	private String full;
	private String version;
	private String model;
	private String zwave_heal;
	private String temperature;
	private String serial_number;
	private String fwd1;
	private String fwd2;
	private String ir;
	private String irtx;
	private String loadtime;
	private String dataversion;
	private String state;
	private String comment;
	private List<Section> sections;
	private List<Room> rooms;
	private List<Scene> scenes;
	private List<Device> devices;
	private List<Categorie> categories;

	public String getFull() {
		return full;
	}
	public void setFull(String full) {
		this.full = full;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getZwave_heal() {
		return zwave_heal;
	}
	public void setZwave_heal(String zwave_heal) {
		this.zwave_heal = zwave_heal;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}
	public String getFwd1() {
		return fwd1;
	}
	public void setFwd1(String fwd1) {
		this.fwd1 = fwd1;
	}
	public String getFwd2() {
		return fwd2;
	}
	public void setFwd2(String fwd2) {
		this.fwd2 = fwd2;
	}
	public String getIr() {
		return ir;
	}
	public void setIr(String ir) {
		this.ir = ir;
	}
	public String getIrtx() {
		return irtx;
	}
	public void setIrtx(String irtx) {
		this.irtx = irtx;
	}
	public String getLoadtime() {
		return loadtime;
	}
	public void setLoadtime(String loadtime) {
		this.loadtime = loadtime;
	}
	public String getDataversion() {
		return dataversion;
	}
	public void setDataversion(String dataversion) {
		this.dataversion = dataversion;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public List<Section> getSections() {
		return sections;
	}
	public void setSections(List<Section> sections) {
		this.sections = sections;
	}
	public List<Room> getRooms() {
		return rooms;
	}
	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}
	public List<Scene> getScenes() {
		return scenes;
	}
	public void setScenes(List<Scene> scenes) {
		this.scenes = scenes;
	}
	public List<Device> getDevices() {
		return devices;
	}
	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
	public List<Categorie> getCategoriess() {
		return categories;
	}
	public void setCategoriess(List<Categorie> categories) {
		this.categories = categories;
	}
}