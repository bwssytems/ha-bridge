
package com.bwssystems.HABridge.plugins.somfy.jsonschema2pojo.getsetup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("creationTime")
    @Expose
    private Long creationTime;
    @SerializedName("lastUpdateTime")
    @Expose
    private Long lastUpdateTime;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("postalCode")
    @Expose
    private String postalCode;
    @SerializedName("addressLine1")
    @Expose
    private String addressLine1;
    @SerializedName("addressLine2")
    @Expose
    private String addressLine2;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("twilightMode")
    @Expose
    private Long twilightMode;
    @SerializedName("twilightAngle")
    @Expose
    private String twilightAngle;
    @SerializedName("twilightCity")
    @Expose
    private String twilightCity;
    @SerializedName("summerSolsticeDuskMinutes")
    @Expose
    private Long summerSolsticeDuskMinutes;
    @SerializedName("winterSolsticeDuskMinutes")
    @Expose
    private Long winterSolsticeDuskMinutes;
    @SerializedName("twilightOffsetEnabled")
    @Expose
    private Boolean twilightOffsetEnabled;
    @SerializedName("dawnOffset")
    @Expose
    private Long dawnOffset;
    @SerializedName("duskOffset")
    @Expose
    private Long duskOffset;

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Long getTwilightMode() {
        return twilightMode;
    }

    public void setTwilightMode(Long twilightMode) {
        this.twilightMode = twilightMode;
    }

    public String getTwilightAngle() {
        return twilightAngle;
    }

    public void setTwilightAngle(String twilightAngle) {
        this.twilightAngle = twilightAngle;
    }

    public String getTwilightCity() {
        return twilightCity;
    }

    public void setTwilightCity(String twilightCity) {
        this.twilightCity = twilightCity;
    }

    public Long getSummerSolsticeDuskMinutes() {
        return summerSolsticeDuskMinutes;
    }

    public void setSummerSolsticeDuskMinutes(Long summerSolsticeDuskMinutes) {
        this.summerSolsticeDuskMinutes = summerSolsticeDuskMinutes;
    }

    public Long getWinterSolsticeDuskMinutes() {
        return winterSolsticeDuskMinutes;
    }

    public void setWinterSolsticeDuskMinutes(Long winterSolsticeDuskMinutes) {
        this.winterSolsticeDuskMinutes = winterSolsticeDuskMinutes;
    }

    public Boolean getTwilightOffsetEnabled() {
        return twilightOffsetEnabled;
    }

    public void setTwilightOffsetEnabled(Boolean twilightOffsetEnabled) {
        this.twilightOffsetEnabled = twilightOffsetEnabled;
    }

    public Long getDawnOffset() {
        return dawnOffset;
    }

    public void setDawnOffset(Long dawnOffset) {
        this.dawnOffset = dawnOffset;
    }

    public Long getDuskOffset() {
        return duskOffset;
    }

    public void setDuskOffset(Long duskOffset) {
        this.duskOffset = duskOffset;
    }

}
