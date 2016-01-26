package com.bwssystems.HABridge.dao;


import java.io.IOException;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
		_loadRepository(deviceDb);
	}
    
    private void _loadRepository(String aFilePath){
		repositoryPath = Paths.get(aFilePath);
		_loadRepository(repositoryPath);
    }
    
	private void _loadRepository(Path aPath){
		String jsonContent = repositoryReader(aPath);
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
    
	public String backup(String aFilename) {
        if(aFilename == null || aFilename.equalsIgnoreCase("")) {
        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        	aFilename = "devicedb-" + dateFormat.format(Calendar.getInstance().getTime()) + ".bk"; 
        }
        else
        	aFilename = aFilename + ".bk";
    	try {
			Files.copy(repositoryPath, FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename), StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			log.error("Could not backup to file: " + aFilename + " message: " + e.getMessage(), e);
		}
        log.debug("Backup repository: " + aFilename);
        return aFilename;
    }

	public String deleteBackup(String aFilename) {
        log.debug("Delete backup repository: " + aFilename);
        try {
			Files.delete(FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename));
		} catch (IOException e) {
			log.error("Could not delete file: " + aFilename + " message: " + e.getMessage(), e);
		}
        return aFilename;
    }

	public String restoreBackup(String aFilename) {
        log.debug("Restore backup repository: " + aFilename);
		try {
			Path target = null;
			if(Files.exists(repositoryPath)) {
	        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				target = FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), "devicedb-" + dateFormat.format(Calendar.getInstance().getTime()) + ".bk");
				Files.move(repositoryPath, target);
			}
			Files.copy(FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename), repositoryPath, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			log.error("Error restoring the file: " + aFilename + " message: " + e.getMessage(), e);
			return null;
		}
		_loadRepository(repositoryPath);
        return aFilename;
    }

	public List<String> getBackups() {
		List<String> theFilenames = new ArrayList<String>();
		Path dir = repositoryPath.getParent();
		try (DirectoryStream<Path> stream =
		     Files.newDirectoryStream(dir, "*.{bk}")) {
		    for (Path entry: stream) {
		        theFilenames.add(entry.getFileName().toString());
		    }
		} catch (IOException x) {
		    // IOException can never be thrown by the iteration.
		    // In this snippet, it can // only be thrown by newDirectoryStream.
			log.error("Issue getting direcotyr listing for backups - " + x.getMessage());
		}
		return theFilenames;
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
		       } else if (name.equals("mapType")) {
			        deviceEntry.setMapType(reader.nextString());
			    	log.debug("Read a Device - device json name: " + deviceEntry.getMapType());
		       } else if (name.equals("mapId")) {
			        deviceEntry.setMapId(reader.nextString());
			    	log.debug("Read a Device - device json name: " + deviceEntry.getMapId());
		       } else if (name.equals("deviceType")) {
			        deviceEntry.setDeviceType(reader.nextString());
			    	log.debug("Read a Device - device json type:" + deviceEntry.getDeviceType());
		       } else if (name.equals("targetDevice")) {
			        deviceEntry.setTargetDevice(reader.nextString());
			    	log.debug("Read a Device - device json type:" + deviceEntry.getTargetDevice());
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
