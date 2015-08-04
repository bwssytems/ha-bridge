package com.bwssytems.HABridge.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssytems.HABridge.JsonTransformer;
import com.bwssytems.HABridge.dao.DeviceDescriptor;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.util.List;
import java.util.ListIterator;
/*
 * This is an in memory list to manage the configured devices and saves the list as a JSON string to a file for later  
 * loading.
 */
public class DeviceRepository {
	Map<String, DeviceDescriptor> devices;
    final Random random = new Random();
    final String repositoryPath = "device.db";
    final Logger log = LoggerFactory.getLogger(DeviceRepository.class);
	
    public DeviceRepository() {
		super();
		String jsonContent = repositoryReader(repositoryPath);
		devices = new HashMap<String, DeviceDescriptor>();
		if(jsonContent != null)
		{
			List<DeviceDescriptor> list = readJsonStream(jsonContent);
			ListIterator<DeviceDescriptor> theIterator = list.listIterator();
			DeviceDescriptor theDevice = null;
			while (theIterator.hasNext()) {
				theDevice = theIterator.next();
				put(Integer.parseInt(theDevice.getId()), theDevice);
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
    
	private void put(int id, DeviceDescriptor aDescriptor) {
        devices.put(String.valueOf(id),aDescriptor);
    }
    
	public void save(DeviceDescriptor aDescriptor) {
        int id = random.nextInt(Integer.MAX_VALUE);
        aDescriptor.setId(String.valueOf(id));
        put(id, aDescriptor);
    	JsonTransformer aRenderer = new JsonTransformer();
    	String  jsonValue = aRenderer.render(findAll());
        repositoryWriter(jsonValue, repositoryPath);
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
	
	private void repositoryWriter(String content, String filePath) {
		FileWriter writer = null;

		try {
			writer = new FileWriter(filePath, false);
			writer.write(content);
		} catch (IOException e) {
			log.error("Error writing the file: " + filePath + " message: " + e.getMessage(), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					log.error("Error closing the file (w): " + filePath + " message: " + e.getMessage(), e);
				}
			}
		}
	}
	
	private String repositoryReader(String filePath) {
		FileReader reader = null;
		BufferedReader br = null;
		String content = null;
		try {
			reader = new FileReader(filePath);
			br = new BufferedReader(reader);
			content = br.readLine();
		} catch (IOException e) {
			log.error("Error reading the file: " + filePath + " message: " + e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					br.close();
					reader.close();
				} catch (IOException e) {
					log.error("Error closing the file (r): " + filePath + " message: " + e.getMessage(), e);
				}
			}
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
		       } else {
		         reader.skipValue();
		       }
		}
		reader.endObject();
		return deviceEntry;
	}
}
