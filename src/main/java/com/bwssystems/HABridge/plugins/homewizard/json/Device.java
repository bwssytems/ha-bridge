package com.bwssystems.HABridge.plugins.homewizard.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Control HomeWizard devices over HomeWizard Cloud
 * 
 * @author Björn Rennfanz (bjoern@fam-rennfanz.de)
 *
 */
public class Device {

    @SerializedName("id")
    @Expose
	private String id;
    
	@SerializedName("name")
    @Expose
    private String name;
    
	@SerializedName("typeName")
    @Expose
    private String typeName;
	
    public String getId() {
		return id;
	}
    
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTypeName() {
		return this.typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}
