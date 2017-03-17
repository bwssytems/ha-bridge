package com.bwssystems.HABridge.hue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.dao.DeviceDescriptor;

public class DeviceDataDecode {
	private static final Logger log = LoggerFactory.getLogger(DeviceDataDecode.class);
	private static final String DEVICE_ID = "${device.id}";
	private static final String DEVICE_UNIQUEID = "${device.uniqueid}";
	private static final String DEVICE_NAME = "${device.name}";
	private static final String DEVICE_MAPID = "${device.mapId}";
	private static final String DEVICE_MAPTYPE = "${device.mapType}";
	private static final String DEVICE_DEVICETYPE = "${device.deviceType}";
	private static final String DEVICE_TARGETDEVICE = "${device.targetDevice}";
	
	public static String replaceDeviceData(String request, DeviceDescriptor device) {
		if (request == null) {
			return null;
		}
		boolean notDone = true;
		
		while(notDone) {
			notDone = false;
			if (request.contains(DEVICE_ID)) {
				request = request.replace(DEVICE_ID, device.getId());
				notDone = true;
			}

			if (request.contains(DEVICE_UNIQUEID)) {
				request = request.replace(DEVICE_UNIQUEID, device.getUniqueid());
				notDone = true;
			}

			if (request.contains(DEVICE_NAME)) {
				request = request.replace(DEVICE_NAME, device.getName());
				notDone = true;
			}

			if (request.contains(DEVICE_MAPID)) {
				request = request.replace(DEVICE_MAPID, device.getMapId());
				notDone = true;
			}

			if (request.contains(DEVICE_MAPTYPE)) {
				request = request.replace(DEVICE_MAPTYPE, device.getMapType());
				notDone = true;
			}

			if (request.contains(DEVICE_DEVICETYPE)) {
				request = request.replace(DEVICE_DEVICETYPE, device.getDeviceType());
				notDone = true;
			}

			if (request.contains(DEVICE_TARGETDEVICE)) {
				request = request.replace(DEVICE_TARGETDEVICE, device.getTargetDevice());
				notDone = true;
			}
			
			log.debug("Request <<" + request + ">>, not done: " + notDone);
		}
		return request;
	}

}
