package com.bwssystems.HABridge.dao;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.util.BackupHandler;
import com.bwssystems.util.JsonTransformer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
/*
 * This is an in memory list to manage the configured devices and saves the list as a JSON string to a file for later  
 * loading.
 */
public class DeviceRepository extends BackupHandler {
	private Map<String, DeviceDescriptor> devices;
    private Path repositoryPath;
	private Gson gson;
    final private Random random = new Random();
    private Logger log = LoggerFactory.getLogger(DeviceRepository.class);
	
    public DeviceRepository(String deviceDb) {
		super();
		gson =
                new GsonBuilder()
                .create();
		repositoryPath = null;
		repositoryPath = Paths.get(deviceDb);
		setupParams(repositoryPath, ".bk", "device.db-");
		_loadRepository(repositoryPath);
	}
    
    public void loadRepository() {
    	if(repositoryPath != null)
    		_loadRepository(repositoryPath);
    }
	private void _loadRepository(Path aPath){
		String jsonContent = repositoryReader(aPath);
		devices = new HashMap<String, DeviceDescriptor>();
		
		if(jsonContent != null)
		{
			DeviceDescriptor list[] = gson.fromJson(jsonContent, DeviceDescriptor[].class);
			for(int i = 0; i < list.length; i++) {
				put(list[i].getId(), list[i]);
			}
		}    	
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
    
	private void put(String id, DeviceDescriptor aDescriptor) {
        devices.put(id, aDescriptor);
    }
    
	public void save(DeviceDescriptor[] descriptors) {
		String theNames = "";
		for(int i = 0; i < descriptors.length; i++) {
	        if(descriptors[i].getId() != null)
	        	devices.remove(descriptors[i].getId());
	        else
	        	descriptors[i].setId(String.valueOf(random.nextInt(Integer.MAX_VALUE)));
	        put(descriptors[i].getId(), descriptors[i]);
	        theNames = theNames + " " + descriptors[i].getName() + ", ";
		}
    	String  jsonValue = gson.toJson(findAll());
        repositoryWriter(jsonValue, repositoryPath);
        log.debug("Save device(s): " + theNames);
    }
    
	public String delete(DeviceDescriptor aDescriptor) {
        if (aDescriptor != null) {
        	devices.remove(aDescriptor.getId());
        	JsonTransformer aRenderer = new JsonTransformer();
        	String  jsonValue = aRenderer.render(findAll());
            repositoryWriter(jsonValue, repositoryPath);
            return "Device with id '" + aDescriptor.getId() + "' deleted";
        } else {
            return "Device not found";
        }

    }
	
	private void repositoryWriter(String content, Path filePath) {
		if(Files.exists(filePath) && !Files.isWritable(filePath)){
			log.error("Error file is not writable: " + filePath);
			return;
		}
		
		if(Files.notExists(filePath.getParent())) {
			try {
				Files.createDirectories(filePath.getParent());
			} catch (IOException e) {
				log.error("Error creating the directory: " + filePath + " message: " + e.getMessage(), e);
			}
		}

		try {
			Path target = null;
			if(Files.exists(filePath)) {
				target = FileSystems.getDefault().getPath(filePath.getParent().toString(), "device.db.old");
				Files.move(filePath, target);
			}
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
			if(target != null)
				Files.delete(target);
		} catch (IOException e) {
			log.error("Error writing the file: " + filePath + " message: " + e.getMessage(), e);
		}
	}
	
	private String repositoryReader(Path filePath) {

		String content = null;
		if(Files.notExists(filePath) || !Files.isReadable(filePath)){
			log.warn("Error reading the file: " + filePath + " - Does not exist or is not readable. continuing...");
			return null;
		}

		
		try {
			content = new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			log.error("Error reading the file: " + filePath + " message: " + e.getMessage(), e);
		}
		
		return content;
	}
}