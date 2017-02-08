
package com.bwssystems.HABridge.plugins.hass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServiceElement {

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("fields")
    @Expose
    private Field fields;

    /**
     * 
     * @return
     *     The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     *     The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @return
     *     The fields
     */
    public Field getFields() {
        return fields;
    }

    /**
     * 
     * @param fields
     *     The fields
     */
    public void setField(Field fields) {
        this.fields = fields;
    }

}
