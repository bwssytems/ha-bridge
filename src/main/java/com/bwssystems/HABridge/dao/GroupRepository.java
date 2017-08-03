package com.bwssystems.HABridge.dao;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.dao.GroupDescriptor;
import com.bwssystems.HABridge.util.BackupHandler;
import com.bwssystems.HABridge.util.JsonTransformer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
/*
 * This is an in memory list to manage the configured groups and saves the list as a JSON string to a file for later  
 * loading.
 */
public class GroupRepository extends BackupHandler {
	private Map<String, GroupDescriptor> groups;
    private Path repositoryPath;
	private Gson gson;
    private Integer nextId;
    private Logger log = LoggerFactory.getLogger(GroupRepository.class);
	
    public GroupRepository(String groupDb) {
		super();
		gson =
                new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        nextId = 0;
		try {
			repositoryPath = null;
			repositoryPath = Paths.get(groupDb);
			setupParams(repositoryPath, ".bk", "group.db-");
			_loadRepository(repositoryPath);
		} catch (Exception ex) {
			groups = new HashMap<String, GroupDescriptor>();
		}
	}
    
    public void loadRepository() {
    	if(repositoryPath != null)
    		_loadRepository(repositoryPath);
    }
	private void _loadRepository(Path aPath){
		String jsonContent = repositoryReader(aPath);
		groups = new HashMap<String, GroupDescriptor>();
		
		if(jsonContent != null)
		{
			GroupDescriptor list[] = gson.fromJson(jsonContent, GroupDescriptor[].class);
			for(int i = 0; i < list.length; i++) {
				list[i].setGroupState(null);
				put(list[i].getId(), list[i]);
				if(Integer.decode(list[i].getId()) > nextId) {
					nextId = Integer.decode(list[i].getId());
				}
			}
		}    	
    }
    
	public List<GroupDescriptor> findAll() {
		List<GroupDescriptor> list = new ArrayList<GroupDescriptor>(groups.values());
		return list;
	}

	public List<GroupDescriptor> findActive() {
		List<GroupDescriptor> list = new ArrayList<GroupDescriptor>();
		for(GroupDescriptor aGroup : new ArrayList<GroupDescriptor>(groups.values())) {
			if(!aGroup.isInactive())
				list.add(aGroup);
		}
		return list;
	}

	public List<GroupDescriptor> findAllByRequester(String anAddress) {
		List<GroupDescriptor> list = new ArrayList<GroupDescriptor>(groups.values());
		List<GroupDescriptor> theReturnList = new ArrayList<GroupDescriptor>();
		Iterator<GroupDescriptor> anIterator = list.iterator();
		GroupDescriptor theGroup;
		String theRequesterAddress;

		HashMap<String,String > addressMap;
		while (anIterator.hasNext()) {
			theGroup = anIterator.next();
			theRequesterAddress = theGroup.getRequesterAddress();
			addressMap = new HashMap<String, String>();
			if(theRequesterAddress != null) {
				if (theRequesterAddress.contains(",")) {
					String[] theArray = theRequesterAddress.split(",");
					for (String v : theArray) {
						addressMap.put(v.trim(), v.trim());
					}
				} else
					addressMap.put(theRequesterAddress, theRequesterAddress);
			}
			if (theRequesterAddress == null || theRequesterAddress.length() == 0 || addressMap.containsKey(anAddress))
				theReturnList.add(theGroup);
		}
		return theReturnList;
	}

	public List<GroupDescriptor> findVirtualLights(String anAddress) {
		List<GroupDescriptor> list = new ArrayList<GroupDescriptor>();
		for (GroupDescriptor group : groups.values()) {
			String expose = group.getExposeAsLight();
			if (expose != null && expose.contains(anAddress)) {
				list.add(group);
			}
		}
		return list;
	}

	public GroupDescriptor findOne(String id) {
    	return groups.get(id);	
    }
    
	private void put(String id, GroupDescriptor aDescriptor) {
        groups.put(id, aDescriptor);
    }
    
    public void save() {
    	save(groups.values().toArray(new GroupDescriptor[0]));
    }

	public void save(GroupDescriptor[] descriptors) {
		String theNames = "";
		for(int i = 0; i < descriptors.length; i++) {
	        if(descriptors[i].getId() != null && descriptors[i].getId().length() > 0)
	        	groups.remove(descriptors[i].getId());
	        else {
	        	nextId++;
	        	descriptors[i].setId(String.valueOf(nextId));
	        }
	        
	        put(descriptors[i].getId(), descriptors[i]);
	        theNames = theNames + " " + descriptors[i].getName() + ", ";
		}
    	String  jsonValue = gson.toJson(findAll());
        repositoryWriter(jsonValue, repositoryPath);
        log.debug("Save group(s): " + theNames);
    }

    public Integer getNewId() {
    	return nextId + 1;
    }

    public String delete(GroupDescriptor aDescriptor) {
        if (aDescriptor != null) {
        	groups.remove(aDescriptor.getId());
        	JsonTransformer aRenderer = new JsonTransformer();
        	String  jsonValue = aRenderer.render(findAll());
            repositoryWriter(jsonValue, repositoryPath);
            return "Group with id '" + aDescriptor.getId() + "' deleted";
        } else {
            return "Group not found";
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
				target = FileSystems.getDefault().getPath(filePath.getParent().toString(), "group.db.old");
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