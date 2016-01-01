package com.aslan.contra.dto.common;

import java.io.Serializable;

/**
 * Created by gobinath on 12/31/15.
 */
public class Environment implements Serializable {
    /**
     * Temperature of the environment.
     */
    private double temperature;

    /**
     * Humidity of the environment.
     */
    private double humidity;

    /**
     * Illuminance of the environment.
     */
    private double illuminance;

    /**
     * Pressure in the environment.
     */
    private double pressure;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getIlluminance() {
        return illuminance;
    }

    public void setIlluminance(double illuminance) {
        this.illuminance = illuminance;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        return String.format("Environment T: %.2f, P: %.2f, H: %.2f, I: %.2f", temperature, pressure, humidity, illuminance);
    }
}
