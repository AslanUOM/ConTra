package com.aslan.contra.dto.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Device entity which represents an Android device of the user.
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
     * Even though minimum API is 1, 0 is given as minimum value to avoid errors during validation of default value.
     */
    @Min(0)
    private int api;

    /**
     * Last known battery level.
     */
    @Min(0)
    @Max(100)
    private double batteryLevel;

    /**
     * Bluetooth MAC address.
     */
    private String bluetoothMAC;

    /**
     * Unique deviceID number to identify the device.
     */
    @NotNull
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
     * Proximity of the device with any objects.
     */
    private double proximity;

    /**
     * Google push notification token.
     */
    private String token;

    /**
     * WiFi MAC address.
     */
    private String wifiMAC;

    /**
     * State of the device.
     */
    private String state;

    /**
     * List of available hardware sensors.
     */
    public String[] getSensors() {
        if (sensors == null) {
            return null;
        }
        return Arrays.copyOf(sensors, sensors.length);
    }

    public void setSensors(String[] sensors) {
        if (sensors == null) {
            return;
        }
        this.sensors = Arrays.copyOf(sensors, sensors.length);
    }

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

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getBluetoothMAC() {
        return bluetoothMAC;
    }

    public void setBluetoothMAC(String bluetoothMAC) {
        this.bluetoothMAC = bluetoothMAC;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
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

    public double getProximity() {
        return proximity;
    }

    public void setProximity(double proximity) {
        this.proximity = proximity;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return deviceID;
    }
}
