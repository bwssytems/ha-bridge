package com.bwssystems.HABridge.plugins.hass;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class State {

@SerializedName("attributes")
@Expose
private Map<String, JsonElement>  attributes;
@SerializedName("entity_id")
@Expose
private String entityId;
@SerializedName("last_changed")
@Expose
private String lastChanged;
@SerializedName("last_updated")
@Expose
private String lastUpdated;
@SerializedName("state")
@Expose
private String state;

/**
* 
* @return
* The attributes
*/
public Map<String, JsonElement>  getAttributes() {
return attributes;
}

/**
* 
* @param attributes
* The attributes
*/
public void setAttributes(Map<String, JsonElement>  attributes) {
this.attributes = attributes;
}

/**
* 
* @return
* The entityId
*/
public String getEntityId() {
return entityId;
}

/**
* 
* @param entityId
* The entity_id
*/
public void setEntityId(String entityId) {
this.entityId = entityId;
}

/**
* 
* @return
* The lastChanged
*/
public String getLastChanged() {
return lastChanged;
}

/**
* 
* @param lastChanged
* The last_changed
*/
public void setLastChanged(String lastChanged) {
this.lastChanged = lastChanged;
}

/**
* 
* @return
* The lastUpdated
*/
public String getLastUpdated() {
return lastUpdated;
}

/**
* 
* @param lastUpdated
* The last_updated
*/
public void setLastUpdated(String lastUpdated) {
this.lastUpdated = lastUpdated;
}

/**
* 
* @return
* The state
*/
public String getState() {
return state;
}

/**
* 
* @param state
* The state
*/
public void setState(String state) {
this.state = state;
}

}