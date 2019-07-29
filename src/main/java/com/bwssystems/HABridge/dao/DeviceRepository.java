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

import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.api.hue.DeviceResponse;
import com.bwssystems.HABridge.api.hue.DeviceState;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.plugins.hue.HueHome;
import com.bwssystems.HABridge.util.BackupHandler;
import com.bwssystems.HABridge.util.JsonTransformer;
import com.bwssystems.HABridge.util.HexLibrary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Collection;
import java.util.List;
import java.util.Arrays;

/*
 * This is an in memory list to manage the configured devices and saves the list as a JSON string to a file for later  
 * loading.
 */
public class DeviceRepository extends BackupHandler {
	private Map<String, DeviceDescriptor> devices;
	private Path repositoryPath;
	private Gson gson;
	private Integer nextId;
	private Integer seedId;
	private Logger log = LoggerFactory.getLogger(DeviceRepository.class);

	public DeviceRepository(String deviceDb, Integer seedid) {
		super();
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		repositoryPath = null;
		repositoryPath = Paths.get(deviceDb);
		setupParams(repositoryPath, ".bk", "device.db-");
		nextId = seedid;
		seedId = seedid;
		_loadRepository(repositoryPath);
	}

	public void loadRepository() {
		if (repositoryPath != null)
			_loadRepository(repositoryPath);
	}

	private void _loadRepository(Path aPath) {
		String jsonContent = repositoryReader(aPath);
		devices = new HashMap<String, DeviceDescriptor>();

		if (jsonContent != null) {
			DeviceDescriptor list[] = gson.fromJson(jsonContent, DeviceDescriptor[].class);
			for (int i = 0; i < list.length; i++) {
				if (list[i].getColorUrl() == null || list[i].getColorUrl().isEmpty())
					list[i].setDeviceState(DeviceState.createDeviceState(false));
				else
					list[i].setDeviceState(DeviceState.createDeviceState(true));
				put(list[i].getId(), list[i]);
				if (Integer.decode(list[i].getId()) > nextId) {
					nextId = Integer.decode(list[i].getId());
				}
			}
		}
	}

	public List<DeviceDescriptor> findAll() {
		List<DeviceDescriptor> list = new ArrayList<DeviceDescriptor>(devices.values());
		return list;
	}

	public List<DeviceDescriptor> findActive() {
		List<DeviceDescriptor> list = new ArrayList<DeviceDescriptor>();
		for (DeviceDescriptor aDevice : new ArrayList<DeviceDescriptor>(devices.values())) {
			if (!aDevice.isInactive())
				list.add(aDevice);
		}
		return list;
	}

	public List<DeviceDescriptor> findAllByRequester(String anAddress) {
		List<DeviceDescriptor> list = new ArrayList<DeviceDescriptor>(devices.values());
		return findAllByRequester(anAddress, list);
	}

	private List<DeviceDescriptor> findAllByRequester(String anAddress, Collection<DeviceDescriptor> list) {
		List<DeviceDescriptor> theReturnList = new ArrayList<DeviceDescriptor>();
		Iterator<DeviceDescriptor> anIterator = list.iterator();
		DeviceDescriptor theDevice;
		String theRequesterAddress;

		HashMap<String, String> addressMap;
		while (anIterator.hasNext()) {
			theDevice = anIterator.next();
			theRequesterAddress = theDevice.getRequesterAddress();
			addressMap = new HashMap<String, String>();
			if (theRequesterAddress != null) {
				if (theRequesterAddress.contains(",")) {
					String[] theArray = theRequesterAddress.split(",");
					for (String v : theArray) {
						addressMap.put(v.trim(), v.trim());
					}
				} else
					addressMap.put(theRequesterAddress, theRequesterAddress);
			}
			if (theRequesterAddress == null || theRequesterAddress.length() == 0 || addressMap.containsKey(anAddress))
				theReturnList.add(theDevice);
		}
		return theReturnList;
	}

	public Map<String, DeviceResponse> findAllByGroupWithState(String[] lightsInGroup, String anAddress,
			HueHome myHueHome, Gson aGsonBuilder) {
		return findAllByGroupWithState(lightsInGroup, anAddress, myHueHome, aGsonBuilder, false);
	}

	public Map<String, DeviceResponse> findAllByGroupWithState(String[] lightsInGroup, String anAddress,
			HueHome myHueHome, Gson aGsonBuilder, boolean ignoreAddress) {
		Map<String, DeviceResponse> deviceResponseMap = new HashMap<String, DeviceResponse>();
		Map<String, DeviceDescriptor> lights = new HashMap<String, DeviceDescriptor>(devices);
		lights.keySet().retainAll(Arrays.asList(lightsInGroup));
		for (DeviceDescriptor light : (ignoreAddress ? lights.values()
				: findAllByRequester(anAddress, lights.values()))) {
			DeviceResponse deviceResponse = null;
			if (!light.isInactive()) {
				if (light.containsType(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex])) {
					CallItem[] callItems = null;
					try {
						if (light.getOnUrl() != null)
							callItems = aGsonBuilder.fromJson(light.getOnUrl(), CallItem[].class);
					} catch (JsonSyntaxException e) {
						log.warn("Could not decode Json for url items to get Hue state for device: {}",
								light.getName());
						callItems = null;
					}

					for (int i = 0; callItems != null && i < callItems.length; i++) {
						if ((callItems[i].getType() != null
								&& callItems[i].getType().equals(DeviceMapTypes.HUE_DEVICE[DeviceMapTypes.typeIndex]))
								|| (callItems[i].getItem() != null && callItems[i].getItem().getAsString() != null
										&& callItems[i].getItem().getAsString().contains("hueName"))) {
							deviceResponse = myHueHome.getHueDeviceInfo(callItems[i], light);
							i = callItems.length;
						}
					}
				}

				if (deviceResponse == null) {
					deviceResponse = DeviceResponse.createResponse(light);
				}
				deviceResponseMap.put(light.getId(), deviceResponse);
			}
		}
		return (deviceResponseMap.size() == 0) ? null : deviceResponseMap;
	}

	public DeviceDescriptor findOne(String id) {
		return devices.get(id);
	}

	private void put(String id, DeviceDescriptor aDescriptor) {
		devices.put(id, aDescriptor);
	}

	public void save(DeviceDescriptor[] descriptors) {
		String theNames = "";
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getId() != null && descriptors[i].getId().length() > 0)
				devices.remove(descriptors[i].getId());
			else {
				descriptors[i].setId(String.valueOf(nextId));
				nextId++;
			}
			if (descriptors[i].getUniqueid() == null || descriptors[i].getUniqueid().length() == 0) {
				descriptors[i].setUniqueid("00:17:88:5E:D3:" + hueUniqueId(Integer.valueOf(descriptors[i].getId())));
			}
			put(descriptors[i].getId(), descriptors[i]);
			theNames = theNames + " " + descriptors[i].getName() + ", ";
		}
		String jsonValue = gson.toJson(findAll());
		repositoryWriter(jsonValue, repositoryPath);
		log.debug("Save device(s): {}", theNames);
	}

	public void renumber() {
		List<DeviceDescriptor> list = new ArrayList<DeviceDescriptor>(devices.values());
		Iterator<DeviceDescriptor> deviceIterator = list.iterator();
		Map<String, DeviceDescriptor> newdevices = new HashMap<String, DeviceDescriptor>();
		List<String> lockedIds = new ArrayList<String>();
		DeviceDescriptor theDevice;
		boolean findNext = true;

		
		nextId = seedId;
		while(deviceIterator.hasNext()) {
			theDevice = deviceIterator.next();
			if(theDevice.isLockDeviceId()) {
				lockedIds.add(theDevice.getId());
			}
		}
		log.debug("Renumber devices starting with: {}", nextId);
		deviceIterator = list.iterator();
		while (deviceIterator.hasNext()) {
			theDevice = deviceIterator.next();
			if (!theDevice.isLockDeviceId()) {
				findNext = true;
				while(findNext) {
					if(lockedIds.contains(String.valueOf(nextId))) {
						nextId++;
					} else {
						findNext = false;
					}
				}
				theDevice.setId(String.valueOf(nextId));
				theDevice.setUniqueid("00:17:88:5E:D3:" + hueUniqueId(nextId));
				nextId++;
			}
			newdevices.put(theDevice.getId(), theDevice);
		}
		devices = newdevices;
		String jsonValue = gson.toJson(findAll());
		repositoryWriter(jsonValue, repositoryPath);
	}

	public String delete(DeviceDescriptor aDescriptor) {
		if (aDescriptor != null) {
			devices.remove(aDescriptor.getId());
			JsonTransformer aRenderer = new JsonTransformer();
			String jsonValue = aRenderer.render(findAll());
			repositoryWriter(jsonValue, repositoryPath);
			return "Device with id '" + aDescriptor.getId() + "' deleted";
		} else {
			return "Device not found";
		}

	}

	private void repositoryWriter(String content, Path filePath) {
		if (Files.exists(filePath) && !Files.isWritable(filePath)) {
			log.error("Error file is not writable: {}", filePath);
			return;
		}

		if (Files.notExists(filePath.getParent())) {
			try {
				Files.createDirectories(filePath.getParent());
			} catch (IOException e) {
				log.error("Error creating the directory: {} message: {}", filePath, e.getMessage(), e);
			}
		}

		try {
			Path target = null;
			if (Files.exists(filePath)) {
				target = FileSystems.getDefault().getPath(filePath.getParent().toString(), "device.db.old");
				Files.move(filePath, target);
			}
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
			if (target != null)
				Files.delete(target);
		} catch (IOException e) {
			log.error("Error writing the file: {} message: {}", filePath, e.getMessage(), e);
		}
	}

	private String repositoryReader(Path filePath) {

		String content = null;
		if (Files.notExists(filePath) || !Files.isReadable(filePath)) {
			log.warn("Error reading the file: {} - Does not exist or is not readable. continuing...", filePath);
			return null;
		}

		try {
			content = new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			log.error("Error reading the file: {} message: {}", filePath, e.getMessage(), e);
		}

		return content;
	}

	private String hueUniqueId(Integer anId) {
		String theUniqueId;
		Integer newValue;
		String hexValueLeft;
		String hexValueRight;

		newValue = anId % 256;
		if (newValue <= 0)
			newValue = 1;
		else if (newValue > 255)
			newValue = 255;
		hexValueLeft = HexLibrary.byteToHex(newValue.byteValue());
		newValue = anId / 256;
		newValue = newValue % 256;
		if (newValue < 0)
			newValue = 0;
		else if (newValue > 255)
			newValue = 255;
		hexValueRight = HexLibrary.byteToHex(newValue.byteValue());

		theUniqueId = String.format("%s-%s", hexValueLeft, hexValueRight).toUpperCase();

		return theUniqueId;
	}
}