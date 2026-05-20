package com.example.speculator;

import android.graphics.Color;
import android.util.Log;

import engine.components.DrawInstruction;
import engine.components.InstructedDrawer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class MPDrawer extends InstructedDrawer {
    private LineChart chart;
    private DTFormatter formatter;
    private double leftMost= Double.MAX_VALUE;
//    private List<LineDataSet> allLines;
    public MPDrawer () {
//        allLines =  Collections.synchronizedList(new ArrayList<>());
    }
    public void setChart(LineChart chart) {
        chart.clear();
        formatter = new DTFormatter();
        chart.getXAxis().setValueFormatter(formatter);
        this.chart = chart;
    }

    public void doDraw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label) {
        Log.d("debug_plot", "start");
        int colorHex;
        switch (color) {
            case RED:
                colorHex = Color.RED; break;
            case BLUE:
                colorHex = Color.BLUE; break;
            case GREEN:
                colorHex = Color.GREEN; break;
            default:
                throw new UnsupportedOperationException();
        };
        Stack<DrawInstruction.Point> descending = new Stack<>();
        LinkedList<DrawInstruction.Point> ascending = new LinkedList<>();
        ArrayList<LineDataSet> lines = new ArrayList<>();
        DrawInstruction.Point prevPoint = points.get(0);
        ascending.add(prevPoint);
        leftMost = Math.min(leftMost, points.stream().min(Comparator.comparing(DrawInstruction.Point::getX)).get().getX());
        formatter.setLeftMost(leftMost);
        for (int i = 1; i < points.size(); i++) {
            DrawInstruction.Point point = points.get(i);
            if (point.getX() >= prevPoint.getX()) { // sorted correctly
                if (!descending.isEmpty()) {
                    LineDataSet line = new LineDataSet(new ArrayList<>(),label);
                    while (!descending.isEmpty()) {
                        DrawInstruction.Point p = descending.pop();
                        line.addEntry(new Entry((float)(p.getX()-leftMost),(float) p.getY()));
                    }
                    lines.add(line);
                    ascending.add(prevPoint);
                }
                ascending.add(point);
            } else { // sorted reverse
                if (!ascending.isEmpty()) {
                    LineDataSet line = new LineDataSet(new ArrayList<>(),label);
                    while (!ascending.isEmpty()) {
                        DrawInstruction.Point p = ascending.poll();
                        line.addEntry(new Entry((float)(p.getX()-leftMost),(float) p.getY()));
                    }
                    lines.add(line);
                    descending.add(prevPoint);
                }
                descending.add(point);
            }
            prevPoint = point;
        }
        if (!descending.isEmpty()) {
            LineDataSet line = new LineDataSet(new ArrayList<>(),label);
            while (!descending.isEmpty()) {
                DrawInstruction.Point p = descending.pop();
                line.addEntry(new Entry((float)(p.getX()-leftMost),(float) p.getY()));
            }
            lines.add(line);
        }
        if (!ascending.isEmpty()) {
            LineDataSet line = new LineDataSet(new ArrayList<>(),label);
            while (!ascending.isEmpty()) {
                DrawInstruction.Point p = ascending.poll();
                line.addEntry(new Entry((float)(p.getX()-leftMost),(float) p.getY()));
            }
            lines.add(line);
        }
        LineData lineData = Optional.ofNullable(chart.getLineData()).orElseGet(()->{chart.setData(new LineData()); return chart.getLineData();});
        lines.forEach(ds -> ds.setColor(colorHex));
        switch (style) {
            case SOLID:
                lines.forEach(ds -> ds.disableDashedLine()); break;
            case DASHED:
                lines.forEach(ds -> ds.enableDashedLine(20, 6, 0)); break;
            case DOTTED:
                lines.forEach(ds -> ds.enableDashedLine(3, 8, 0)); break;
            case NONE:
            default:
                throw new UnsupportedOperationException();
        };
        lines.forEach(ds -> ds.setDrawCircleHole(false));
        lines.forEach(ds -> ds.setDrawCircles(false));
        lines.forEach(ds -> ds.setDrawValues(false));
        lines.forEach(lineData::addDataSet);
        Log.d("debug_draw", "" + points.size());
        Log.d("debug_draw", "" + lines.get(0).getEntryCount());
        lineData.notifyDataChanged();
//        this.chart.setData(lineData);
        this.chart.notifyDataSetChanged();
        this.chart.invalidate();
//        this.allLines.addAll(lines);
    }

    @Override
    public void legend(DrawInstruction.DrawMapping mapping) {

    }

    public void doUndraw() {
//        chart.clear();
//        this.allLines =  Collections.synchronizedList(new ArrayList<>());
        leftMost = Double.MAX_VALUE;

        Optional.ofNullable(chart)
//                .map(LineChart::getLineData)
                .ifPresent(chart -> {

//                    this.allLines.forEach(lineData::removeDataSet);

                    chart.setData(new LineData());

                    chart.notifyDataSetChanged();

                    chart.invalidate();

//                    this.allLines =  Collections.synchronizedList(new ArrayList<>());
                });
    }

    @Override
    public void draw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label) {
        chart.post(()-> doDraw(points,color,style,label));
    }

    @Override
    public void undraw() {
        System.out.println("debug_pred: start2");
        chart.post(()-> doUndraw());


    }
    private static class DTFormatter extends ValueFormatter {
        private double leftMost;
        public void setLeftMost(double leftMost) {
            this.leftMost = leftMost;
        }
        @Override
        public String getFormattedValue(float offset) {
            return ZonedDateTime
                    .ofInstant(Instant.ofEpochSecond((long)(leftMost+offset)), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
