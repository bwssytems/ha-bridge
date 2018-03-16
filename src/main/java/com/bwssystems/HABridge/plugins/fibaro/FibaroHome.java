package com.bwssystems.HABridge.plugins.fibaro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.BridgeSettings;
import com.bwssystems.HABridge.DeviceMapTypes;
import com.bwssystems.HABridge.Home;
import com.bwssystems.HABridge.NamedIP;
import com.bwssystems.HABridge.api.CallItem;
import com.bwssystems.HABridge.dao.DeviceDescriptor;
import com.bwssystems.HABridge.hue.ColorData;
import com.bwssystems.HABridge.hue.MultiCommandUtil;
import com.bwssystems.HABridge.plugins.fibaro.json.Device;
import com.bwssystems.HABridge.plugins.fibaro.json.Scene;

public class FibaroHome implements Home
{
	private static final Logger log = LoggerFactory.getLogger(FibaroHome.class);
	private Map<String, FibaroInfo> fibaros;
	private Boolean validFibaro;
	private boolean closed;

	public FibaroHome(BridgeSettings bridgeSettings)
	{
		super();
		closed = true;
		createHome(bridgeSettings);
		closed = false;
	}

	public List<Device> getDevices()
	{
		log.debug("consolidating devices for fibaros");
		Iterator<String> keys = fibaros.keySet().iterator();
		ArrayList<Device> deviceList = new ArrayList<>();
		while(keys.hasNext())
		{
			String key = keys.next();
			for(Device device : fibaros.get(key).getDevices())
				deviceList.add(device);
		}
		return deviceList;
	}

	public List<Scene> getScenes()
	{
		log.debug("consolidating scenes for fibaros");
		Iterator<String> keys = fibaros.keySet().iterator();
		ArrayList<Scene> sceneList = new ArrayList<>();
		while(keys.hasNext())
		{
			String key = keys.next();
			for(Scene scene : fibaros.get(key).getScenes())
				sceneList.add(scene);
		}
		return sceneList;
	}

	@Override
	public String deviceHandler(CallItem anItem, MultiCommandUtil aMultiUtil, String lightId, int intensity,
			Integer targetBri,Integer targetBriInc, ColorData colorData, DeviceDescriptor device, String body)
	{
		// Not a device handler
		return null;
	}

	@Override
	public Object getItems(String type)
	{
		if(validFibaro)
		{
			if(type.equalsIgnoreCase(DeviceMapTypes.FIBARO_DEVICE[DeviceMapTypes.typeIndex]))
				return getDevices();
			if(type.equalsIgnoreCase(DeviceMapTypes.FIBARO_SCENE[DeviceMapTypes.typeIndex]))
				return getScenes();
		}
		return null;
	}

	@Override
	public void refresh() {
		// noop		
	}
	
	@Override
	public Home createHome(BridgeSettings bridgeSettings)
	{
		validFibaro = bridgeSettings.getBridgeSettingsDescriptor().isValidFibaro();
		log.info("Fibaro Home created." + (validFibaro ? "" : " No Fibaros configured."));
		if(validFibaro)
		{
			fibaros = new HashMap<String, FibaroInfo>();
			Iterator<NamedIP> theList = bridgeSettings.getBridgeSettingsDescriptor().getFibaroAddress().getDevices().iterator();
			while(theList.hasNext())
			{
				NamedIP aFibaro = theList.next();
				fibaros.put(aFibaro.getName(), new FibaroInfo(aFibaro));
			}
		}
		return this;
	}

	@Override
	public void closeHome()
	{
		log.debug("Closing Home.");
		if(closed) {
			log.debug("Home is already closed....");
			return;
		}
		fibaros = null;
		closed = true;
	}
}