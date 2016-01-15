package com.aslan.contra.dto.common;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A period is the time between a starting time and ending time.
 * It contains the start and end as the properties.
 * <p>
 * Created by gobinath on 1/15/16.
 */
public class Interval implements Serializable {
    @NotNull
    @Valid
    private Time startTime;

    @NotNull
    @Valid
    private Time endTime;

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return startTime + " - " + endTime;
    }
}
