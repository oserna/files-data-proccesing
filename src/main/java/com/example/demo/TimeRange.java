package com.example.demo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class TimeRange{

    private final Instant eventTime;

    private final Integer UPPER_RANGE;
    private final Integer LOWER_RANGE;


    public TimeRange(Instant eventTime, Integer range) {
        this(eventTime, range, range);
    }

    public TimeRange(Instant eventTime, Integer lowerRange, Integer upperRange) {
        this.eventTime = eventTime;
        this.LOWER_RANGE = lowerRange;
        this.UPPER_RANGE = upperRange;
    }

    public  Boolean contains(Instant eventTime){
        return (!eventTime.isBefore(this.eventTime.minus(LOWER_RANGE, ChronoUnit.SECONDS))
                && eventTime.isBefore(this.eventTime.plus(UPPER_RANGE, ChronoUnit.SECONDS)));
    }

}