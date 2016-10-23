package com.bwssystems.harmony;

public class RunActivity {
	private String name;

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
}
