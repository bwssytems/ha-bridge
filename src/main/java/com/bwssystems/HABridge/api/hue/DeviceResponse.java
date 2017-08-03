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
            response.setModelid("LCT010");
            response.setSwversion("1.15.2_r19181");
            response.setSwconfigid("F921C859");
            response.setProductid("Philips-LCT010-1-A19ECLv4");    
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
        response.setUniqueid("00:17:88:5E:D3:FF-" + String.format("%02X", Integer.parseInt(group.getId())));
        response.setManufacturername("Philips");
        response.setType("Extended color light");
        response.setModelid("LCT010");
        response.setSwversion("1.15.2_r19181");
        response.setSwconfigid("F921C859");
        response.setProductid("Philips-LCT010-1-A19ECLv4");    
        
        response.setLuminaireuniqueid(null);

        return response;
    }


}
