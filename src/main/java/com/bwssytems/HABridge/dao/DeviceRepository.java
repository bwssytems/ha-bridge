package com.bwssytems.HABridge.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.bwssytems.HABridge.dao.DeviceDescriptor;

import java.util.List;
/*
 * This is an in memory list to manage the configured devices. 
 * 
 */
public class DeviceRepository {
	Map<String, DeviceDescriptor> devices;
    final Random random = new Random();
	
    public DeviceRepository() {
		super();
		devices = new HashMap<String, DeviceDescriptor>();
	}
    
	public List<DeviceDescriptor> findAll() {
		List<DeviceDescriptor> list = new ArrayList<DeviceDescriptor>(devices.values());
		return list;
	}

	public List<DeviceDescriptor> findByDeviceType(String aType) {
		List<DeviceDescriptor> list = new ArrayList<DeviceDescriptor>(devices.values());
		return list;
	}
	
	public DeviceDescriptor findOne(String id) {
    	return devices.get(id);
    	
    }
    
	public void save(DeviceDescriptor aDescriptor) {
        int id = random.nextInt(Integer.MAX_VALUE);
        aDescriptor.setId(String.valueOf(id));
        devices.put(String.valueOf(id),aDescriptor);
    }
    
	public String delete(DeviceDescriptor aDescriptor) {
        if (aDescriptor != null) {
        	devices.remove(aDescriptor.getId());
            return "Device with id '" + aDescriptor.getId() + "' deleted";
        } else {
            return "Device not found";
        }

    }
}
