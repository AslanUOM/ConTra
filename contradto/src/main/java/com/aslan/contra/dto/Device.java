package com.aslan.contra.dto;

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
     */
    @Min(1)
    private int api;

    /**
     * Last known battery level.
     */
    @Min(0)
    @Max(100)
    private float batteryLevel;

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


    @Override
    public String toString() {
        return getDeviceID();
    }

    /**
     * Indicates whether the device is with the user or not.
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * API level of the device.
     */
    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }

    /**
     * Last known battery level.
     */
    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    /**
     * Bluetooth MAC address.
     */
    public String getBluetoothMAC() {
        return bluetoothMAC;
    }

    public void setBluetoothMAC(String bluetoothMAC) {
        this.bluetoothMAC = bluetoothMAC;
    }

    /**
     * Unique deviceID number to identify the device.
     */
    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * The lat seen time of this device.
     */
    public Time getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Time lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Manufacturer name of the device.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

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

    /**
     * Temperature of the environment.
     */
    public float getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(float ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    /**
     * Humidity of the environment.
     */
    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    /**
     * Illuminance of the environment.
     */
    public float getIlluminance() {
        return illuminance;
    }

    public void setIlluminance(float illuminance) {
        this.illuminance = illuminance;
    }

    /**
     * Pressure in the environment.
     */
    public float getAmbientPressure() {
        return ambientPressure;
    }

    public void setAmbientPressure(float ambientPressure) {
        this.ambientPressure = ambientPressure;
    }

    /**
     * Proximity of the device with any objects.
     */
    public float getProximity() {
        return proximity;
    }

    public void setProximity(float proximity) {
        this.proximity = proximity;
    }

    /**
     * Google push notification token.
     */
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * WiFi MAC address.
     */
    public String getWifiMAC() {
        return wifiMAC;
    }

    public void setWifiMAC(String wifiMAC) {
        this.wifiMAC = wifiMAC;
    }
}
