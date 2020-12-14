package com.bwssystems.HABridge.api.hue;

import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.dao.GroupDescriptor;

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
    private String swconfigid;
    private String productid;
    private String productname;

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

    public String getSwconfigid() {
        return swconfigid;
    }

    public void setSwconfigid(String swconfigid) {
        this.swconfigid = swconfigid;
    }
    
    public String getProductid() {
        return productid;
    }

	public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getProductName() {
        return productname;
    }

	public void setProductName(String productname) {
        this.productname = productname;
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
            
        if (device.isColorDevice()) {
            response.setType("Extended color light");
            response.setModelid("LCT015");
            response.setSwversion("1.46.13_r26312");
            response.setSwconfigid("52E3234B");
            response.setProductid("Philips-LCT015-1-A19ECLv5");
            response.setProductName("Hue color lamp");
        } else {
            response.setType("Dimmable light");
            response.setModelid("LWB007");
            response.setSwversion("66012040");
        }
        
        response.setLuminaireuniqueid(null);

        return response;
    }

    public static DeviceResponse createResponseForVirtualLight(GroupDescriptor group){
        DeviceResponse response = new DeviceResponse();
        response.setState(group.getAction());

        response.setName(group.getName());
        response.setUniqueid("00:11:22:33:44:55:66:77-" + String.format("%02X", Integer.parseInt(group.getId())));
        response.setManufacturername("Philips");
        response.setType("Extended color light");
        response.setModelid("LCT015");
        response.setSwversion("1.46.13_r26312");
        response.setSwconfigid("52E3234B");
        response.setProductid("Philips-LCT015-1-A19ECLv5");
        response.setProductName("Hue color lamp");
        
        response.setLuminaireuniqueid(null);

        return response;
    }


}
