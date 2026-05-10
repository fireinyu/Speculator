package org.example;

import java.util.ArrayList;
import java.util.List;
import java.awt.BasicStroke;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import engine.components.InstructedDrawer;
import engine.components.DrawInstruction.Color;
import engine.components.DrawInstruction.DrawMapping;
import engine.components.DrawInstruction.Point;
import engine.components.DrawInstruction.Style;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class TestDrawer extends InstructedDrawer{
    private transient XYChart chart;
    private transient SwingWrapper<XYChart> wrapper;
    private transient JFrame frame;
    private transient boolean frameOpening;
    private int lineIdx;

    /// Point API:
    ///     float getX(): returns x-value of point
    ///     float getY(): returns y-value of point 
    public TestDrawer() {
        lineIdx = 0;
        this.chart = new XYChartBuilder()
                .width(900)
                .height(600)
                .title("Speculator")
                .xAxisTitle("x")
                .yAxisTitle("y")
                .build();
        this.chart.getStyler().setLegendVisible(true);
        this.chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        this.chart.getStyler().setMarkerSize(0);
    }

    private XYChart chart() {
        if (this.chart == null) {
            this.chart = new XYChartBuilder()
                    .width(900)
                    .height(600)
                    .title("Speculator")
                    .xAxisTitle("x")
                    .yAxisTitle("y")
                    .build();
            this.chart.getStyler().setLegendVisible(true);
            this.chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            this.chart.getStyler().setMarkerSize(0);
        }
        return this.chart;
    }

    private static java.awt.Color toAwtColor(Color color) {
        if (color == null) {
            return java.awt.Color.GRAY;
        }
        switch (color) {
            case RED:
                return java.awt.Color.RED;
            case GREEN:
                return java.awt.Color.GREEN;
            case BLUE:
                return java.awt.Color.BLUE;
            case YELLOW:
                return java.awt.Color.YELLOW;
            case PINK:
                return java.awt.Color.PINK;
            case ANY:
                return java.awt.Color.GRAY;
            case NONE:
            default:
                return java.awt.Color.GRAY;
        }
    }

    private static BasicStroke toStroke(Style style) {
        switch (style) {
            case DASHED:
                return new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[] {10.0f, 8.0f}, 0.0f);
            case DOTTED:
                return new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[] {2.0f, 6.0f}, 0.0f);
            case SOLID:
            case ANY:
            case NONE:
            default:
                return new BasicStroke(2.0f);
        }
    }

    private void showChart() {
        if (this.frame != null) {
            this.frame.setVisible(true);
            this.frame.toFront();
            this.frame.repaint();
        }
    }

    private static void runOnEdtAndWait(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void ensureChartShown(XYChart chart) {
        if (this.wrapper == null) {
            this.wrapper = new SwingWrapper<>(chart);
        }
        if (this.frame != null && this.frame.isDisplayable()) {
            this.wrapper.repaintChart();
            showChart();
            return;
        }
        if (this.frameOpening) {
            return;
        }
        this.frameOpening = true;
        Runnable openFrame = () -> {
            try {
                this.frame = this.wrapper.displayChart();
                showChart();
            } finally {
                this.frameOpening = false;
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            Thread thread = new Thread(openFrame, "speculator-chart-open");
            thread.setDaemon(true);
            thread.start();
        } else {
            openFrame.run();
        }
    }

    @Override
    public void undraw() {
        lineIdx = 0;
        if (this.chart != null) {
            runOnEdtAndWait(() -> {
                for (String seriesName : new ArrayList<>(this.chart.getSeriesMap().keySet())) {
                    this.chart.removeSeries(seriesName);
                }
            });
        }
        // if (this.frame != null) {
        //     this.frame.dispose();
        //     this.frame = null;
        // }
        // this.wrapper = null;
    }

    @Override
    public void draw(List<Point> points, Color color, Style style, String label) {
        if (points == null || points.isEmpty() || style == Style.NONE || color == Color.NONE) {
            return;
        }
        final String seriesLabel = label + lineIdx++;
        XYChart chart = chart();

        List<Double> xData = new ArrayList<>(points.size());
        List<Number> yData = new ArrayList<>(points.size());
        for (Point point : points) {
            xData.add((double) point.getX());
            yData.add((double) point.getY());
        }

        runOnEdtAndWait(() -> {
            XYSeries series = chart.addSeries(seriesLabel, xData, yData);
            series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            series.setMarker(SeriesMarkers.NONE);
            series.setLineColor(toAwtColor(color));
            series.setLineStyle(toStroke(style));
        });

        ensureChartShown(chart);
    }

    @Override
    public void legend(DrawMapping mapping) {
        /// do nothing for now
    }
}
