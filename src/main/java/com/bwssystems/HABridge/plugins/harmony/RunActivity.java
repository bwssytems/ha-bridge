package com.bwssystems.HABridge.plugins.harmony;

public class RunActivity {
	private String name;
	private String hub;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public Boolean isValid() {
		if (name != null && !name.isEmpty())
			return true;
		return false;
	}

	public String getHub() {
		return hub;
	}

	public void setHub(String hub) {
		this.hub = hub;
	}
}
