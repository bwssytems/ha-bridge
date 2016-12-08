
package com.bwssystems.hass;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FieldElement {

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("example")
    @Expose
    private String example;

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
     *     The example
     */
    public String getExample() {
        return example;
    }

    /**
     * 
     * @param example
     *     The example
     */
    public void setExample(String example) {
        this.example = example;
    }

}
