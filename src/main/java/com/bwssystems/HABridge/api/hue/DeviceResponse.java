package com.bwssystems.HABridge.api.hue;

import com.bwssystems.HABridge.dao.DeviceDescriptor;

/**
 * Created by arm on 4/14/15.
 */
public class DeviceResponse {
    private DeviceState state;
    private String type;
    private String name;
    private String modelid;
    private String manufacturername;
    private String luminaireuniqueid;
    private String uniqueid;
    private String swversion;

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

    public String getLuminaireuniqueid() {
		return luminaireuniqueid;
	}

	public void setLuminaireuniqueid(String luminaireuniqueid) {
		this.luminaireuniqueid = luminaireuniqueid;
	}

	public static DeviceResponse createResponse(DeviceDescriptor device){
        DeviceResponse response = new DeviceResponse();
        response.setState(device.getDeviceState());

        response.setName(device.getName());
        response.setUniqueid(device.getUniqueid());
        response.setManufacturername("Philips");
        response.setType("Dimmable light");
        response.setModelid("LWB004");
        response.setSwversion("66012040");
        response.setLuminaireuniqueid(null);

        return response;
    }
}
