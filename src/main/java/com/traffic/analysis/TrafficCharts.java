package com.traffic.analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.TextAnchor;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

public class TrafficCharts {

    private static final String REPORTS_DIR = "reports";
    private static int FIGURE_NO = 1;

    /* =======================
       Colors
       ======================= */

    // Line colors (lighter)
    private static final Color WEEKDAY_COLOR = new Color(220, 90, 90);
    private static final Color WEEKEND_COLOR = new Color(80, 110, 210);

    // Annotation colors (darker for contrast)
    private static final Color WEEKDAY_ANNO_COLOR = new Color(170, 40, 40);
    private static final Color WEEKEND_ANNO_COLOR = new Color(40, 70, 160);

    /* =======================
       Utilities
       ======================= */

    private static void saveChart(JFreeChart chart, String fileName, int width, int height) {
        String wd = System.getProperty("user.dir");
        System.out.println("\nWorking directory:");
        System.out.println(wd);

        File out = new File(wd + File.separator + REPORTS_DIR + File.separator + fileName);
        out.getParentFile().mkdirs();

        Runnable task = () -> {
            try {
                ChartUtils.saveChartAsJPEG(out, chart, width, height);
                System.out.println("Saved chart to:");
                System.out.println(out.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                task.run();
            } else {
                SwingUtilities.invokeAndWait(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showChart(String title, JFreeChart chart) {
        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);
    }

    private static void applyBarLabels(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        NumberFormat fmt = NumberFormat.getIntegerInstance();
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", fmt)
        );
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
    }

    private static int findPeakHour(Map<Integer, Double> avg) {
        int peakHour = -1;
        double peakValue = Double.NEGATIVE_INFINITY;

        for (var e : avg.entrySet()) {
            double v = e.getValue();
            if (!Double.isFinite(v)) continue;
            if (v > peakValue) {
                peakValue = v;
                peakHour = e.getKey();
            }
        }
        return peakHour;
    }

    private static String formatHour(int h) {
        return String.format("%02d:00", h);
    }

    private static String captionLine(String title, String body) {
        return "Figure " + (FIGURE_NO++) + ". " + title + " " + body;
    }

    private static void printLatexFigureBlock(String fileName, String caption, String label) {
        System.out.println("\nLaTeX figure:");
        System.out.println("\\begin{figure}[ht]");
        System.out.println("  \\centering");
        System.out.println("  \\includegraphics[width=0.95\\linewidth]{" + fileName + "}");
        System.out.println("  \\caption{" + caption.replace("%", "\\%") + "}");
        System.out.println("  \\label{" + label + "}");
        System.out.println("\\end{figure}");
    }

    /* =======================
       Charts
       ======================= */

    // 1) Average Traffic Volume by Hour
    public static void showAvgVolumeByHour(Map<Integer, Double> avgByHour,
                                           int peakHour,
                                           double peakAvgVolume) {

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (var e : avgByHour.entrySet()) {
            ds.addValue(e.getValue(), "Avg Volume",
                    String.format("%02d", e.getKey()));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Traffic Volume by Hour of Day",
                "Hour (0–23)",
                "Average Volume",
                ds
        );

        NumberFormat fmt = NumberFormat.getIntegerInstance();
        chart.addSubtitle(new TextTitle(
                String.format("Peak hour: %02d:00 (avg %s)",
                        peakHour, fmt.format(Math.round(peakAvgVolume))),
                new Font("SansSerif", Font.PLAIN, 12)
        ));

        applyBarLabels(chart);

        String fileName = "docs/images/traffic-volume.jpg";
        saveChart(chart, fileName, 900, 600);

        String caption = captionLine(
                "Average hourly traffic volume.",
                "Peak demand occurs at " + formatHour(peakHour) +
                        " (avg " + fmt.format(Math.round(peakAvgVolume)) + ")."
        );
        System.out.println("\nCaption:\n" + caption);
        printLatexFigureBlock(REPORTS_DIR + "/" + fileName, caption, "fig:traffic-volume-hour");

        showChart("Traffic Volume", chart);
    }

    // 2) Weekday vs Weekend
    public static void showWeekdayVsWeekend(Map<String, Double> avg) {

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(avg.get("Weekday"), "Avg Volume", "Weekday");
        ds.addValue(avg.get("Weekend"), "Avg Volume", "Weekend");

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Traffic Volume: Weekday vs Weekend",
                "Day Type",
                "Average Volume",
                ds
        );

        applyBarLabels(chart);

        String fileName = "docs/images/weekday-vs-weekend.jpg";
        saveChart(chart, fileName, 900, 600);

        NumberFormat fmt = NumberFormat.getIntegerInstance();
        String caption = captionLine(
                "Average traffic volume by day type.",
                "Weekday mean is " + fmt.format(Math.round(avg.get("Weekday"))) +
                        " vs weekend mean " + fmt.format(Math.round(avg.get("Weekend"))) + "."
        );
        System.out.println("\nCaption:\n" + caption);
        printLatexFigureBlock(REPORTS_DIR + "/" + fileName, caption, "fig:weekday-vs-weekend");

        showChart("Weekday vs Weekend", chart);
    }

    // 3) Hourly Curves with darker annotations
    public static void showHourlyCurvesWeekdayVsWeekend(
            Map<Integer, Double> weekdayAvg,
            Map<Integer, Double> weekendAvg) {

        XYSeries weekday = new XYSeries("Weekday");
        XYSeries weekend = new XYSeries("Weekend");

        for (int h = 0; h < 24; h++) {
            double w1 = weekdayAvg.getOrDefault(h, 0.0);
            double w2 = weekendAvg.getOrDefault(h, 0.0);

            if (!Double.isFinite(w1)) w1 = 0.0;
            if (!Double.isFinite(w2)) w2 = 0.0;

            weekday.add(h, w1);
            weekend.add(h, w2);
        }

        XYSeriesCollection ds = new XYSeriesCollection();
        ds.addSeries(weekday);
        ds.addSeries(weekend);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Hourly Traffic Volume Curves: Weekday vs Weekend",
                "Hour (0–23)",
                "Average Volume",
                ds
        );

        XYPlot plot = chart.getXYPlot();

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(true);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
        r.setSeriesPaint(0, WEEKDAY_COLOR);
        r.setSeriesPaint(1, WEEKEND_COLOR);
        r.setSeriesStroke(0, new BasicStroke(2.0f));
        r.setSeriesStroke(1, new BasicStroke(2.0f));
        r.setDefaultShapesVisible(false);

        int weekdayPeak = findPeakHour(weekdayAvg);
        int weekendPeak = findPeakHour(weekendAvg);

        double weekdayPeakVal = weekdayAvg.get(weekdayPeak);
        double weekendPeakVal = weekendAvg.get(weekendPeak);

        NumberFormat fmt = NumberFormat.getIntegerInstance();
        Font annoFont = new Font("SansSerif", Font.PLAIN, 11);
        BasicStroke arrowStroke = new BasicStroke(1.0f);

        XYPointerAnnotation wdAnno = new XYPointerAnnotation(
                "Weekday peak: " + formatHour(weekdayPeak) +
                        " (~" + fmt.format(Math.round(weekdayPeakVal)) + ")",
                weekdayPeak, weekdayPeakVal,
                Math.PI / 4
        );
        wdAnno.setTextAnchor(TextAnchor.BOTTOM_LEFT);
        wdAnno.setFont(annoFont);
        wdAnno.setPaint(WEEKDAY_ANNO_COLOR);
        wdAnno.setArrowPaint(WEEKDAY_ANNO_COLOR);
        wdAnno.setArrowStroke(arrowStroke);

        XYPointerAnnotation weAnno = new XYPointerAnnotation(
                "Weekend peak: " + formatHour(weekendPeak) +
                        " (~" + fmt.format(Math.round(weekendPeakVal)) + ")",
                weekendPeak, weekendPeakVal,
                -Math.PI / 4
        );
        weAnno.setTextAnchor(TextAnchor.TOP_RIGHT);
        weAnno.setFont(annoFont);
        weAnno.setPaint(WEEKEND_ANNO_COLOR);
        weAnno.setArrowPaint(WEEKEND_ANNO_COLOR);
        weAnno.setArrowStroke(arrowStroke);

        plot.addAnnotation(wdAnno);
        plot.addAnnotation(weAnno);

        String fileName = "docs/images/hourly-curves.jpg";
        saveChart(chart, fileName, 1000, 650);

        String caption = captionLine(
                "Hourly traffic volume curves for weekday vs weekend.",
                "The weekday peak occurs at " + formatHour(weekdayPeak) +
                        " (~" + fmt.format(Math.round(weekdayPeakVal)) +
                        "), while the weekend peak occurs at " + formatHour(weekendPeak) +
                        " (~" + fmt.format(Math.round(weekendPeakVal)) + ")."
        );
        System.out.println("\nCaption:\n" + caption);
        printLatexFigureBlock(REPORTS_DIR + "/" + fileName, caption, "fig:hourly-curves");

        showChart("Hourly Curves", chart);
    }

    // 4) Weather Impact
    public static void showAvgVolumeByWeatherMain(Map<String, Double> avgByWeather) {

        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (var e : avgByWeather.entrySet()) {
            ds.addValue(e.getValue(), "Avg Volume", e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Average Traffic Volume by Weather Condition",
                "Weather",
                "Average Volume",
                ds
        );

        applyBarLabels(chart);

        String fileName = "docs/images/weather-impact.jpg";
        saveChart(chart, fileName, 1000, 650);

        String bestWeather = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (var e : avgByWeather.entrySet()) {
            double v = e.getValue();
            if (!Double.isFinite(v)) continue;
            if (v > bestVal) {
                bestVal = v;
                bestWeather = e.getKey();
            }
        }

        NumberFormat fmt = NumberFormat.getIntegerInstance();
        String caption = captionLine(
                "Average traffic volume by weather condition.",
                "Highest mean volume occurs under " + bestWeather +
                        " (~" + fmt.format(Math.round(bestVal)) + ")."
        );
        System.out.println("\nCaption:\n" + caption);
        printLatexFigureBlock(REPORTS_DIR + "/" + fileName, caption, "fig:weather-impact");

        showChart("Weather Impact", chart);
    }
}








