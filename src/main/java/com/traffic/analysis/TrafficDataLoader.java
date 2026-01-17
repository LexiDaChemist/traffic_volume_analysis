package com.traffic.analysis;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrafficDataLoader {

    
	private static final DateTimeFormatter DT_FMT =
	        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    public static List<TrafficRecord> load(Path csvPath) throws IOException {
        if (!Files.exists(csvPath)) {
            throw new IOException("CSV not found at: " + csvPath.toAbsolutePath());
        }

        List<TrafficRecord> out = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord r : parser) {
                String holiday = safeString(r, "holiday");
                if (holiday.isBlank()) holiday = "None";

                double tempK = parseDouble(r, "temp");
                double rain1hMm = parseDouble(r, "rain_1h");
                double snow1hMm = parseDouble(r, "snow_1h");
                int cloudsAllPct = (int) Math.round(parseDouble(r, "clouds_all"));

                String weatherMain = safeString(r, "weather_main");
                String weatherDescription = safeString(r, "weather_description");

                LocalDateTime dateTime = LocalDateTime.parse(
                        safeString(r, "date_time"),
                        DT_FMT
                );

                int trafficVolume = (int) Math.round(parseDouble(r, "traffic_volume"));

                out.add(new TrafficRecord(
                        holiday,
                        tempK,
                        rain1hMm,
                        snow1hMm,
                        cloudsAllPct,
                        weatherMain,
                        weatherDescription,
                        dateTime,
                        trafficVolume
                ));
            }
        }

        return out;
    }

    private static String safeString(CSVRecord r, String col) {
        String v = r.isMapped(col) ? r.get(col) : "";
        return v == null ? "" : v.trim();
    }

    private static double parseDouble(CSVRecord r, String col) {
        String s = safeString(r, col);
        if (s.isEmpty()) return 0.0;
        return Double.parseDouble(s);
    }
}
