package com.aslan.contra.dto;

import java.io.Serializable;

/**
 * Device entity represents the Android device of the user.
 * <p>
 * Created by gobinath on 12/24/15.
 */
public class Device implements Serializable {
    /**
     * Indicates whether the device is with the user or not.
     */
    private boolean active;

    /**
     * API level of the device.
     */
    private int api;

    /**
     * Last known battery level.
     */
    private float batteryLevel;

    /**
     * Bluetooth MAC address.
     */
    private String bluetoothMAC;

    /**
     * Unique deviceID number to identify the device.
     */
    private String deviceID;

    /**
     * The lat seen time of this device.
     */
    private Time lastSeen;

    /**
     * Manufacturer name of the device.
     */
    private String manufacturer;

    /**
     * List of available hardware sensors.
     */
    private String[] sensors;

    /**
     * Temperature of the environment.
     */
    private float ambientTemperature;

    /**
     * Humidity of the environment.
     */
    private float humidity;

    /**
     * Illuminance of the environment.
     */
    private float illuminance;

    /**
     * Pressure in the environment.
     */
    private float ambientPressure;

    /**
     * Proximity of the device with any objects.
     */
    private float proximity;

    /**
     * Google push notification token.
     */
    private String token;

    /**
     * WiFi MAC address.
     */
    private String wifiMAC;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getBluetoothMAC() {
        return bluetoothMAC;
    }

    public void setBluetoothMAC(String bluetoothMAC) {
        this.bluetoothMAC = bluetoothMAC;
    }

    public Time getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Time lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String[] getSensors() {
        return sensors;
    }

    public void setSensors(String[] sensors) {
        this.sensors = sensors;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWifiMAC() {
        return wifiMAC;
    }

    public void setWifiMAC(String wifiMAC) {
        this.wifiMAC = wifiMAC;
    }

    @Override
    public String toString() {
        return deviceID;
    }
}
