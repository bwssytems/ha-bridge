package com.bwssystems.HABridge.plugins.homewizard;

/**
 * Control HomeWizard devices over HomeWizard Cloud
 * 
 * @author Björn Rennfanz (bjoern@fam-rennfanz.de)
 *
 */
public class HomeWizardSmartPlugDevice {

	private String name;
	private String gateway;
	private String id;
	private String typeName;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}
	
    public String getId() {
		return id;
	}
    
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTypeName() {
		return this.typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}
