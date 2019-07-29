
package com.bwssystems.HABridge.plugins.homegenie;

import java.util.List;

// import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Module {
    private static final Logger log = LoggerFactory.getLogger(HomeGenieInstance.class);

    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("DeviceType")
    @Expose
    private String deviceType;
    @SerializedName("Domain")
    @Expose
    private String domain;
    @SerializedName("Address")
    @Expose
    private String address;
    /*
     * @SerializedName("Properties")
     * 
     * @Expose private List<Property> properties = null;
     */
    @SerializedName("RoutingNode")
    @Expose
    private String routingNode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /*
     * public List<Property> getProperties() { return properties; }
     * 
     * public void setProperties(List<Property> properties) { this.properties =
     * properties; }
     */
    public String getRoutingNode() {
        return routingNode;
    }

    public void setRoutingNode(String routingNode) {
        this.routingNode = routingNode;
    }

    public boolean isSwitch() {
        return isDeviceType("Switch");
    }

    public boolean isDimmer() {
        return isDeviceType("Dimmer");
    }

    public boolean isLight() {
        return isDeviceType("Light");
    }

    private boolean isDeviceType(String theType) {
        if (deviceType.equals(theType)) {
            return true;
        }
        return false;
    }

    public boolean isModuleValid(JsonObject theExtensions) {
        ModuleTypes moduleTypes = null;
        if (this.name == null || this.name.trim().isEmpty())
            return false;

        if (isSwitch())
            return true;

        if (isDimmer())
            return true;

        if (isLight())
            return true;

        if (theExtensions != null && theExtensions.isJsonObject() && theExtensions.get("moduleTypes").isJsonArray()) {
            try {
                moduleTypes = new Gson().fromJson(theExtensions, ModuleTypes.class);
            } catch (Exception e) {
                log.warn("Could not parse extensions for {} with {}", this.name, theExtensions);
                return false;
            }

            if (moduleTypes == null)
                return false;

            for (ModuleType moduleType : moduleTypes.getModuleTypes()) {
                if (isDeviceType(moduleType.getModuleType()))
                    return true;
            }
        }

        return false;
    }
}
