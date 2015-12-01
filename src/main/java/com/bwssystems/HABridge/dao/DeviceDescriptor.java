package com.bwssystems.HABridge.dao;
/*
 * Object to handle the device configuration
 */
public class DeviceDescriptor{
    private String id;
    private String name;
    private String mapId;
    private String mapType;
    private String deviceType;
    private String targetDevice;
    private String offUrl;
    private String onUrl;
    private String httpVerb;
    private String contentType;
    private String contentBody;
    private String contentBodyOff;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapId() {
		return mapId;
	}

	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	public String getMapType() {
		return mapType;
	}

	public void setMapType(String mapType) {
		this.mapType = mapType;
	}

	public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getTargetDevice() {
		return targetDevice;
	}

	public void setTargetDevice(String targetDevice) {
		this.targetDevice = targetDevice;
	}

	public String getOffUrl() {
        return offUrl;
    }

    public void setOffUrl(String offUrl) {
        this.offUrl = offUrl;
    }

    public String getOnUrl() {
        return onUrl;
    }

    public void setOnUrl(String onUrl) {
        this.onUrl = onUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public String getHttpVerb() {
		return httpVerb;
	}

	public void setHttpVerb(String httpVerb) {
		this.httpVerb = httpVerb;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentBody() {
		return contentBody;
	}

	public void setContentBody(String contentBody) {
		this.contentBody = contentBody;
	}

	public String getContentBodyOff() {
		return contentBodyOff;
	}

	public void setContentBodyOff(String contentBodyOff) {
		this.contentBodyOff = contentBodyOff;
	}
    

}
