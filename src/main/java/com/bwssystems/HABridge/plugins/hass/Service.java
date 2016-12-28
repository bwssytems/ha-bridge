
package com.bwssystems.HABridge.plugins.hass;

import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Service {

    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("services")
    @Expose
    private Map<String, ServiceElement> services;

    public Service(String domain, Map<String, ServiceElement> services) {
		super();
		this.domain = domain;
		this.services = services;
	}

	/**
     * 
     * @return
     *     The domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 
     * @param domain
     *     The domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * 
     * @return
     *     The services
     */
	public Map<String, ServiceElement> getServices() {
		return services;
	}

    /**
     * 
     * @param domain
     *     The services
     */
	public void setServices(Map<String, ServiceElement> services) {
		this.services = services;
	}

}
