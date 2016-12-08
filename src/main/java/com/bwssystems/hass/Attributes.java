package com.bwssystems.hass;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attributes {

@SerializedName("Vera Device Id")
@Expose
private Integer veraDeviceId;
@SerializedName("friendly_name")
@Expose
private String friendlyName;
@SerializedName("supported_features")
@Expose
private Integer supportedFeatures;
@SerializedName("attribution")
@Expose
private String attribution;
@SerializedName("entity_picture")
@Expose
private String entityPicture;
@SerializedName("azimuth")
@Expose
private Double azimuth;
@SerializedName("elevation")
@Expose
private Double elevation;
@SerializedName("next_rising")
@Expose
private String nextRising;
@SerializedName("next_setting")
@Expose
private String nextSetting;
@SerializedName("current_power_mwh")
@Expose
private Integer currentPowerMwh;
@SerializedName("auto")
@Expose
private Boolean auto;
@SerializedName("entity_id")
@Expose
private List<String> entityId = null;
@SerializedName("hidden")
@Expose
private Boolean hidden;
@SerializedName("order")
@Expose
private Integer order;

/**
* 
* @return
* The veraDeviceId
*/
public Integer getVeraDeviceId() {
return veraDeviceId;
}

/**
* 
* @param veraDeviceId
* The Vera Device Id
*/
public void setVeraDeviceId(Integer veraDeviceId) {
this.veraDeviceId = veraDeviceId;
}

/**
* 
* @return
* The friendlyName
*/
public String getFriendlyName() {
return friendlyName;
}

/**
* 
* @param friendlyName
* The friendly_name
*/
public void setFriendlyName(String friendlyName) {
this.friendlyName = friendlyName;
}

/**
* 
* @return
* The supportedFeatures
*/
public Integer getSupportedFeatures() {
return supportedFeatures;
}

/**
* 
* @param supportedFeatures
* The supported_features
*/
public void setSupportedFeatures(Integer supportedFeatures) {
this.supportedFeatures = supportedFeatures;
}

/**
* 
* @return
* The attribution
*/
public String getAttribution() {
return attribution;
}

/**
* 
* @param attribution
* The attribution
*/
public void setAttribution(String attribution) {
this.attribution = attribution;
}

/**
* 
* @return
* The entityPicture
*/
public String getEntityPicture() {
return entityPicture;
}

/**
* 
* @param entityPicture
* The entity_picture
*/
public void setEntityPicture(String entityPicture) {
this.entityPicture = entityPicture;
}

/**
* 
* @return
* The azimuth
*/
public Double getAzimuth() {
return azimuth;
}

/**
* 
* @param azimuth
* The azimuth
*/
public void setAzimuth(Double azimuth) {
this.azimuth = azimuth;
}

/**
* 
* @return
* The elevation
*/
public Double getElevation() {
return elevation;
}

/**
* 
* @param elevation
* The elevation
*/
public void setElevation(Double elevation) {
this.elevation = elevation;
}

/**
* 
* @return
* The nextRising
*/
public String getNextRising() {
return nextRising;
}

/**
* 
* @param nextRising
* The next_rising
*/
public void setNextRising(String nextRising) {
this.nextRising = nextRising;
}

/**
* 
* @return
* The nextSetting
*/
public String getNextSetting() {
return nextSetting;
}

/**
* 
* @param nextSetting
* The next_setting
*/
public void setNextSetting(String nextSetting) {
this.nextSetting = nextSetting;
}

/**
* 
* @return
* The currentPowerMwh
*/
public Integer getCurrentPowerMwh() {
return currentPowerMwh;
}

/**
* 
* @param currentPowerMwh
* The current_power_mwh
*/
public void setCurrentPowerMwh(Integer currentPowerMwh) {
this.currentPowerMwh = currentPowerMwh;
}

/**
* 
* @return
* The auto
*/
public Boolean getAuto() {
return auto;
}

/**
* 
* @param auto
* The auto
*/
public void setAuto(Boolean auto) {
this.auto = auto;
}

/**
* 
* @return
* The entityId
*/
public List<String> getEntityId() {
return entityId;
}

/**
* 
* @param entityId
* The entity_id
*/
public void setEntityId(List<String> entityId) {
this.entityId = entityId;
}

/**
* 
* @return
* The hidden
*/
public Boolean getHidden() {
return hidden;
}

/**
* 
* @param hidden
* The hidden
*/
public void setHidden(Boolean hidden) {
this.hidden = hidden;
}

/**
* 
* @return
* The order
*/
public Integer getOrder() {
return order;
}

/**
* 
* @param order
* The order
*/
public void setOrder(Integer order) {
this.order = order;
}

}
