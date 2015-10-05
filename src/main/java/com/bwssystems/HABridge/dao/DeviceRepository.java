package com.bwssystems.HABridge.dao;


import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystem;
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

import com.bwssystems.HABridge.JsonTransformer;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.google.gson.stream.JsonReader;

import java.util.List;
import java.util.ListIterator;
/*
 * This is an in memory list to manage the configured devices and saves the list as a JSON string to a file for later  
 * loading.
 */
public class DeviceRepository {
	Map<String, DeviceDescriptor> devices;
    Path repositoryPath;
    final Random random = new Random();
    final Logger log = LoggerFactory.getLogger(DeviceRepository.class);
	
    public DeviceRepository(String deviceDb) {
		super();
		repositoryPath = Paths.get(deviceDb);
		String jsonContent = repositoryReader(repositoryPath);
		devices = new HashMap<String, DeviceDescriptor>();
		
		if(jsonContent != null)
		{
			List<DeviceDescriptor> list = readJsonStream(jsonContent);
			ListIterator<DeviceDescriptor> theIterator = list.listIterator();
			DeviceDescriptor theDevice = null;
			while (theIterator.hasNext()) {
				theDevice = theIterator.next();
				put(theDevice.getId(), theDevice);
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
    
	public void save(DeviceDescriptor aDescriptor) {
        if(aDescriptor.getId() != null)
        	devices.remove(aDescriptor.getId());
        else
        	aDescriptor.setId(String.valueOf(random.nextInt(Integer.MAX_VALUE)));
        put(aDescriptor.getId(), aDescriptor);
    	JsonTransformer aRenderer = new JsonTransformer();
    	String  jsonValue = aRenderer.render(findAll());
        repositoryWriter(jsonValue, repositoryPath);
        log.debug("Save device: " + aDescriptor.getName());
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
			Path target = FileSystems.getDefault().getPath("data", "device.db.old");
			Files.move(filePath, target);
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
			Files.delete(target);
		} catch (IOException e) {
			log.error("Error writing the file: " + filePath + " message: " + e.getMessage(), e);
		}
	}
	
	private String repositoryReader(Path filePath) {

		String content = null;
		if(Files.notExists(filePath) || !Files.isReadable(filePath)){
			log.error("Error reading the file: " + filePath + " - Does not exist or is not readable. ");
			return null;
		}

		
		try {
			content = new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			log.error("Error reading the file: " + filePath + " message: " + e.getMessage(), e);
		}
		
		return content;
	}

	private List<DeviceDescriptor> readJsonStream(String context) {
		JsonReader reader = new JsonReader(new StringReader(context));
		List<DeviceDescriptor> theDescriptors = null;
		try {
			theDescriptors = readDescriptorArray(reader);
		} catch (IOException e) {
			log.error("Error reading json array: " + context + " message: " + e.getMessage(), e);
		} finally {
			try {
				reader.close();
			} catch  (IOException e) {
				log.error("Error closing json reader: " + context + " message: " + e.getMessage(), e);
			}
		}
		return theDescriptors;
	}

	public List<DeviceDescriptor> readDescriptorArray(JsonReader reader) throws IOException {
		List<DeviceDescriptor> descriptors = new ArrayList<DeviceDescriptor>();

		reader.beginArray();
		while (reader.hasNext()) {
			descriptors.add(readDescriptor(reader));
		}
		reader.endArray();
		return descriptors;
	}

	public DeviceDescriptor readDescriptor(JsonReader reader) throws IOException {
        DeviceDescriptor deviceEntry = new DeviceDescriptor();

		reader.beginObject();
		while (reader.hasNext()) {
		       String name = reader.nextName();
		       if (name.equals("id")) {
			        deviceEntry.setId(reader.nextString());
			    	log.debug("Read a Device - device json id: " + deviceEntry.getId());
		       } else if (name.equals("name")) {
			        deviceEntry.setName(reader.nextString());
			    	log.debug("Read a Device - device json name: " + deviceEntry.getName());
		       } else if (name.equals("deviceType")) {
			        deviceEntry.setDeviceType(reader.nextString());
			    	log.debug("Read a Device - device json type:" + deviceEntry.getDeviceType());
		       } else if (name.equals("offUrl")) {
			        deviceEntry.setOffUrl(reader.nextString());
			    	log.debug("Read a Device - device json off URL:" + deviceEntry.getOffUrl());
		       } else if (name.equals("onUrl")) {
			        deviceEntry.setOnUrl(reader.nextString());
			    	log.debug("Read a Device - device json on URL:" + deviceEntry.getOnUrl());
		       } else if (name.equals("httpVerb")) {
			        deviceEntry.setHttpVerb(reader.nextString());
			    	log.debug("Read a Device - device json httpVerb:" + deviceEntry.getHttpVerb());
		       } else if (name.equals("contentType")) {
			        deviceEntry.setContentType(reader.nextString());
			    	log.debug("Read a Device - device json contentType:" + deviceEntry.getContentType());
		       } else if (name.equals("contentBody")) {
			        deviceEntry.setContentBody(reader.nextString());
			    	log.debug("Read a Device - device json contentBody:" + deviceEntry.getContentBody());
			   } else if (name.equals("contentBodyOff")) {
			        deviceEntry.setContentBodyOff(reader.nextString());
			    	log.debug("Read a Device - device json contentBodyOff:" + deviceEntry.getContentBodyOff());
		       } else {
		         reader.skipValue();
		       }
		}
		reader.endObject();
		return deviceEntry;
	}
}
