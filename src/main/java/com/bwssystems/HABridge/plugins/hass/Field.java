
package com.bwssystems.HABridge.plugins.hass;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Field {

    @SerializedName("fields")
    @Expose
    private Map<String, JsonElement> fields;

	public Field(Map<String, JsonElement> fields) {
		super();
		this.fields = fields;
	}

	public Map<String, JsonElement> getFields() {
		return fields;
	}

	public void setFields(Map<String, JsonElement> fields) {
		this.fields = fields;
	}

}
