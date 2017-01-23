
package com.bwssystems.HABridge.plugins.domoticz;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceResult {

    @SerializedName("AddjMulti")
    @Expose
    private Integer addjMulti;
    @SerializedName("AddjMulti2")
    @Expose
    private Integer addjMulti2;
    @SerializedName("AddjValue")
    @Expose
    private Integer addjValue;
    @SerializedName("AddjValue2")
    @Expose
    private Integer addjValue2;
    @SerializedName("BatteryLevel")
    @Expose
    private Integer batteryLevel;
    @SerializedName("CustomImage")
    @Expose
    private Integer customImage;
    @SerializedName("Data")
    @Expose
    private String data;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Favorite")
    @Expose
    private Integer favorite;
    @SerializedName("HardwareID")
    @Expose
    private Integer hardwareID;
    @SerializedName("HardwareName")
    @Expose
    private String hardwareName;
    @SerializedName("HardwareType")
    @Expose
    private String hardwareType;
    @SerializedName("HardwareTypeVal")
    @Expose
    private Integer hardwareTypeVal;
    @SerializedName("HaveDimmer")
    @Expose
    private Boolean haveDimmer;
    @SerializedName("HaveGroupCmd")
    @Expose
    private Boolean haveGroupCmd;
    @SerializedName("HaveTimeout")
    @Expose
    private Boolean haveTimeout;
    @SerializedName("ID")
    @Expose
    private String iD;
    @SerializedName("Image")
    @Expose
    private String image;
    @SerializedName("IsSubDevice")
    @Expose
    private Boolean isSubDevice;
    @SerializedName("LastUpdate")
    @Expose
    private String lastUpdate;
    @SerializedName("Level")
    @Expose
    private Integer level;
    @SerializedName("LevelInt")
    @Expose
    private Integer levelInt;
    @SerializedName("MaxDimLevel")
    @Expose
    private Integer maxDimLevel;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Notifications")
    @Expose
    private String notifications;
    @SerializedName("PlanID")
    @Expose
    private String planID;
    @SerializedName("PlanIDs")
    @Expose
    private List<Integer> planIDs = null;
    @SerializedName("Protected")
    @Expose
    private Boolean _protected;
    @SerializedName("ShowNotifications")
    @Expose
    private Boolean showNotifications;
    @SerializedName("SignalLevel")
    @Expose
    private String signalLevel;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("StrParam1")
    @Expose
    private String strParam1;
    @SerializedName("StrParam2")
    @Expose
    private String strParam2;
    @SerializedName("SubType")
    @Expose
    private String subType;
    @SerializedName("SwitchType")
    @Expose
    private String switchType;
    @SerializedName("SwitchTypeVal")
    @Expose
    private Integer switchTypeVal;
    @SerializedName("Timers")
    @Expose
    private String timers;
    @SerializedName("Type")
    @Expose
    private String type;
    @SerializedName("TypeImg")
    @Expose
    private String typeImg;
    @SerializedName("Unit")
    @Expose
    private Integer unit;
    @SerializedName("Used")
    @Expose
    private Integer used;
    @SerializedName("UsedByCamera")
    @Expose
    private Boolean usedByCamera;
    @SerializedName("XOffset")
    @Expose
    private String xOffset;
    @SerializedName("YOffset")
    @Expose
    private String yOffset;
    @SerializedName("idx")
    @Expose
    private String idx;

    public Integer getAddjMulti() {
        return addjMulti;
    }

    public void setAddjMulti(Integer addjMulti) {
        this.addjMulti = addjMulti;
    }

    public Integer getAddjMulti2() {
        return addjMulti2;
    }

    public void setAddjMulti2(Integer addjMulti2) {
        this.addjMulti2 = addjMulti2;
    }

    public Integer getAddjValue() {
        return addjValue;
    }

    public void setAddjValue(Integer addjValue) {
        this.addjValue = addjValue;
    }

    public Integer getAddjValue2() {
        return addjValue2;
    }

    public void setAddjValue2(Integer addjValue2) {
        this.addjValue2 = addjValue2;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getCustomImage() {
        return customImage;
    }

    public void setCustomImage(Integer customImage) {
        this.customImage = customImage;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFavorite() {
        return favorite;
    }

    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    public Integer getHardwareID() {
        return hardwareID;
    }

    public void setHardwareID(Integer hardwareID) {
        this.hardwareID = hardwareID;
    }

    public String getHardwareName() {
        return hardwareName;
    }

    public void setHardwareName(String hardwareName) {
        this.hardwareName = hardwareName;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(String hardwareType) {
        this.hardwareType = hardwareType;
    }

    public Integer getHardwareTypeVal() {
        return hardwareTypeVal;
    }

    public void setHardwareTypeVal(Integer hardwareTypeVal) {
        this.hardwareTypeVal = hardwareTypeVal;
    }

    public Boolean getHaveDimmer() {
        return haveDimmer;
    }

    public void setHaveDimmer(Boolean haveDimmer) {
        this.haveDimmer = haveDimmer;
    }

    public Boolean getHaveGroupCmd() {
        return haveGroupCmd;
    }

    public void setHaveGroupCmd(Boolean haveGroupCmd) {
        this.haveGroupCmd = haveGroupCmd;
    }

    public Boolean getHaveTimeout() {
        return haveTimeout;
    }

    public void setHaveTimeout(Boolean haveTimeout) {
        this.haveTimeout = haveTimeout;
    }

    public String getID() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getIsSubDevice() {
        return isSubDevice;
    }

    public void setIsSubDevice(Boolean isSubDevice) {
        this.isSubDevice = isSubDevice;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getLevelInt() {
        return levelInt;
    }

    public void setLevelInt(Integer levelInt) {
        this.levelInt = levelInt;
    }

    public Integer getMaxDimLevel() {
        return maxDimLevel;
    }

    public void setMaxDimLevel(Integer maxDimLevel) {
        this.maxDimLevel = maxDimLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public String getPlanID() {
        return planID;
    }

    public void setPlanID(String planID) {
        this.planID = planID;
    }

    public List<Integer> getPlanIDs() {
        return planIDs;
    }

    public void setPlanIDs(List<Integer> planIDs) {
        this.planIDs = planIDs;
    }

    public Boolean getProtected() {
        return _protected;
    }

    public void setProtected(Boolean _protected) {
        this._protected = _protected;
    }

    public Boolean getShowNotifications() {
        return showNotifications;
    }

    public void setShowNotifications(Boolean showNotifications) {
        this.showNotifications = showNotifications;
    }

    public String getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(String signalLevel) {
        this.signalLevel = signalLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public String getStrParam2() {
        return strParam2;
    }

    public void setStrParam2(String strParam2) {
        this.strParam2 = strParam2;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSwitchType() {
        return switchType;
    }

    public void setSwitchType(String switchType) {
        this.switchType = switchType;
    }

    public Integer getSwitchTypeVal() {
        return switchTypeVal;
    }

    public void setSwitchTypeVal(Integer switchTypeVal) {
        this.switchTypeVal = switchTypeVal;
    }

    public String getTimers() {
        return timers;
    }

    public void setTimers(String timers) {
        this.timers = timers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeImg() {
        return typeImg;
    }

    public void setTypeImg(String typeImg) {
        this.typeImg = typeImg;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public Boolean getUsedByCamera() {
        return usedByCamera;
    }

    public void setUsedByCamera(Boolean usedByCamera) {
        this.usedByCamera = usedByCamera;
    }

    public String getXOffset() {
        return xOffset;
    }

    public void setXOffset(String xOffset) {
        this.xOffset = xOffset;
    }

    public String getYOffset() {
        return yOffset;
    }

    public void setYOffset(String yOffset) {
        this.yOffset = yOffset;
    }

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

}
