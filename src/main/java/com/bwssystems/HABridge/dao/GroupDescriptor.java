package com.bwssystems.HABridge.dao;

import com.bwssystems.HABridge.api.hue.DeviceState;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.bwssystems.HABridge.api.hue.GroupState;

/*
 * Object to handle the device configuration
 */
public class GroupDescriptor{
	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("name")
	@Expose
    private String name;
    @SerializedName("groupType")
	@Expose
    private String groupType;
    @SerializedName("groupClass")
	@Expose
    private String groupClass;
    @SerializedName("requesterAddress")
    @Expose
    private String requesterAddress;
    @SerializedName("inactive")
    @Expose
    private boolean inactive;
	@SerializedName("description")
    @Expose
    private String description;
    @SerializedName("comments")
    @Expose
    private String comments;
    
    private DeviceState action;
    private GroupState groupState;
    
    @SerializedName("lights")
    @Expose
    private String[] lights;
    @SerializedName("exposeAsLight")
    @Expose
    private String exposeAsLight;
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGroupClass() {
        return groupClass;
    }

    public void setGroupClass(String groupClass) {
        this.groupClass = groupClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public GroupState getGroupState() {
		if(groupState == null)
			groupState = new GroupState(false,false);
		return groupState;
	}

	public void setGroupState(GroupState groupState) {
		this.groupState = groupState;
	}

	public DeviceState getAction() {
		if(action == null)
			action = DeviceState.createDeviceState(true);
		return action;
	}

	public void setAction(DeviceState action) {
		this.action = action;
	}

	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	public String getRequesterAddress() {
		return requesterAddress;
	}

	public void setRequesterAddress(String requesterAddress) {
		this.requesterAddress = requesterAddress;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String[] getLights() {
		return lights;
	}

	public void setLights(String[] lights) {
		this.lights = lights;
	}

	public void setExposeAsLight(String exposeFor) {
		this.exposeAsLight = exposeFor;
	}

	public String getExposeAsLight() {
		return exposeAsLight;
	}
}