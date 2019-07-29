
package com.bwssystems.HABridge.plugins.moziot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JWT {

    @SerializedName("jwt")
    @Expose
    private String jwt;

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

}
