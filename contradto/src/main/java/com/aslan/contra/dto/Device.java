package com.aslan.contra.dto;

import java.io.Serializable;

/**
 * Device entity represents the Android device of the user.
 * <p>
 * Created by gobinath on 12/24/15.
 */
public class Device implements Serializable {
    /**
     * Google push notification token.
     */
    private String token;

    /**
     * Unique serial number to identify the device.
     */
    private String serial;

    /**
     * Manufacturer name of the device.
     */
    private String manufacturer;

    /**
     * WiFi MAC address.
     */
    private String wifiMAC;

    /**
     * Bluetooth MAC address.
     */
    private String btMAC;

    /**
     * API level of the device.
     */
    private int api;

    /**
     * List of available hardware sensors.
     */
    private String[] sensors;

    /**
     * Last knwon battery level.
     */
    private float batteryLevel;

    /**
     * The lat seen time of this device.
     */
    private Time lastSeen;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getWifiMAC() {
        return wifiMAC;
    }

    public void setWifiMAC(String wifiMAC) {
        this.wifiMAC = wifiMAC;
    }

    public String getBtMAC() {
        return btMAC;
    }

    public void setBtMAC(String btMAC) {
        this.btMAC = btMAC;
    }

    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }

    public String[] getSensors() {
        return sensors;
    }

    public void setSensors(String[] sensors) {
        this.sensors = sensors;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Time getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Time lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return serial;
    }
}
