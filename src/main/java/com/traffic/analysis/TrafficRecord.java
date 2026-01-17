package com.traffic.analysis;

import java.time.LocalDateTime;

public record TrafficRecord(
        String holiday,
        double tempK,
        double rain1hMm,
        double snow1hMm,
        int cloudsAllPct,
        String weatherMain,
        String weatherDescription,
        LocalDateTime dateTime,
        int trafficVolume
) {}

