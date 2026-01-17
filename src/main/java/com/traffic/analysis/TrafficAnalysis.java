package com.traffic.analysis;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TrafficAnalysis {

    // Average traffic volume by hour (0–23)
    public static Map<Integer, Double> averageVolumeByHour(List<TrafficRecord> rows) {
        Map<Integer, DoubleSummaryStatistics> stats = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> r.dateTime().getHour(),
                        TreeMap::new,
                        Collectors.summarizingDouble(TrafficRecord::trafficVolume)
                ));

        Map<Integer, Double> out = new TreeMap<>();
        for (var e : stats.entrySet()) {
            out.put(e.getKey(), e.getValue().getAverage());
        }
        return out;
    }

    // Weekday vs weekend overall averages
    public static Map<String, Double> averageWeekdayVsWeekend(List<TrafficRecord> rows) {
        double weekdaySum = 0;
        int weekdayCount = 0;

        double weekendSum = 0;
        int weekendCount = 0;

        for (TrafficRecord r : rows) {
            DayOfWeek d = r.dateTime().getDayOfWeek();
            boolean weekend = (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY);

            if (weekend) {
                weekendSum += r.trafficVolume();
                weekendCount++;
            } else {
                weekdaySum += r.trafficVolume();
                weekdayCount++;
            }
        }

        return Map.of(
                "Weekday", weekdaySum / weekdayCount,
                "Weekend", weekendSum / weekendCount
        );
    }

    // Hourly curves (weekday vs weekend): hour -> average volume
    public static Map<String, Map<Integer, Double>> averageVolumeByHourWeekdayVsWeekend(List<TrafficRecord> rows) {
        double[] weekdaySum = new double[24];
        int[] weekdayCount = new int[24];
        double[] weekendSum = new double[24];
        int[] weekendCount = new int[24];

        for (TrafficRecord r : rows) {
            int h = r.dateTime().getHour();
            DayOfWeek d = r.dateTime().getDayOfWeek();
            boolean weekend = (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY);

            if (weekend) {
                weekendSum[h] += r.trafficVolume();
                weekendCount[h]++;
            } else {
                weekdaySum[h] += r.trafficVolume();
                weekdayCount[h]++;
            }
        }

        Map<Integer, Double> weekdayAvg = new TreeMap<>();
        Map<Integer, Double> weekendAvg = new TreeMap<>();

        for (int h = 0; h < 24; h++) {
            weekdayAvg.put(h, weekdayCount[h] == 0 ? 0.0 : weekdaySum[h] / weekdayCount[h]);
            weekendAvg.put(h, weekendCount[h] == 0 ? 0.0 : weekendSum[h] / weekendCount[h]);
        }

        return Map.of(
                "Weekday", weekdayAvg,
                "Weekend", weekendAvg
        );
    }

    // Peak hour record
    public static record PeakHour(int hour, double averageVolume) {}

    // Peak hour by average volume from a list of records
    public static PeakHour peakHourByAverage(List<TrafficRecord> rows) {
        var avgByHour = averageVolumeByHour(rows);

        return avgByHour.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(e -> new PeakHour(e.getKey(), e.getValue()))
                .orElseThrow(() -> new IllegalArgumentException("No data provided"));
    }

    public static PeakHour peakHourWeekday(List<TrafficRecord> rows) {
        List<TrafficRecord> weekdayRows = rows.stream()
                .filter(r -> {
                    DayOfWeek d = r.dateTime().getDayOfWeek();
                    return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
                })
                .toList();

        return peakHourByAverage(weekdayRows);
    }

    public static PeakHour peakHourWeekend(List<TrafficRecord> rows) {
        List<TrafficRecord> weekendRows = rows.stream()
                .filter(r -> {
                    DayOfWeek d = r.dateTime().getDayOfWeek();
                    return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
                })
                .toList();

        return peakHourByAverage(weekendRows);
    }
    
    public static Map<String, Double> averageVolumeByWeatherMain(List<TrafficRecord> rows) {
        // weather_main -> average traffic volume
        return rows.stream()
                .filter(r -> r.weatherMain() != null && !r.weatherMain().isBlank())
                .collect(Collectors.groupingBy(
                        r -> r.weatherMain().trim(),
                        TreeMap::new,
                        Collectors.averagingDouble(TrafficRecord::trafficVolume)
                ));
    }
}

