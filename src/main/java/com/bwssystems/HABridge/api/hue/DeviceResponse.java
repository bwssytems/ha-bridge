package com.bwssystems.HABridge.api.hue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arm on 4/14/15.
 */
public class DeviceResponse {
    private DeviceState state;
    private String type;
    private String name;
    private String modelid;
    private String manufacturername;
    private String uniqueid;
    private String swversion;
    private Map<String, String> pointsymbol;

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelid() {
        return modelid;
    }

    public void setModelid(String modelid) {
        this.modelid = modelid;
    }

    public String getManufacturername() {
        return manufacturername;
    }

    public void setManufacturername(String manufacturername) {
        this.manufacturername = manufacturername;
    }

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getSwversion() {
        return swversion;
    }

    public void setSwversion(String swversion) {
        this.swversion = swversion;
    }

    public Map<String, String> getPointsymbol() {
    	if(pointsymbol == null)
    	{
    		pointsymbol = new HashMap<>();
	        pointsymbol.put("1", "none");
	        pointsymbol.put("2", "none");
	        pointsymbol.put("3", "none");
	        pointsymbol.put("4", "none");
	        pointsymbol.put("5", "none");
	        pointsymbol.put("6", "none");
	        pointsymbol.put("7", "none");
	        pointsymbol.put("8", "none");
    	}

        return pointsymbol;
    }

    public void setPointsymbol(Map<String, String> pointsymbol) {
        this.pointsymbol = pointsymbol;
    }

    public static DeviceResponse createResponse(String name, String id){
        DeviceState deviceState = new DeviceState();
        DeviceResponse response = new DeviceResponse();
        response.setState(deviceState);
        deviceState.setOn(false);
        deviceState.setReachable(true);
        deviceState.setEffect("none");
        deviceState.setAlert("none");
        deviceState.setBri(254);
        deviceState.setSat(254);

        response.setName(name);
        response.setUniqueid(id);
        response.setManufacturername("Philips");
        response.setType("Dimmable light");
        response.setModelid("LWB004");
        response.setSwversion("65003148");

        return response;
    }
}
