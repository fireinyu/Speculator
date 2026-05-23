package com.example.speculator;

import android.graphics.Color;
import android.util.Log;
import android.view.ViewGroup;

import engine.Util;
import engine.components.DrawInstruction;
import engine.components.InstructedDrawer;

import com.example.speculator.uiComponents.DateTimeChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
    private DateTimeChart chart;
//    private List<LineDataSet> allLines;
    public MPDrawer () {
//        allLines =  Collections.synchronizedList(new ArrayList<>());
    }
    public void setViews(ViewGroup box) {
        box.removeAllViews();
        chart = new DateTimeChart(box.getContext());
        box.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LineDataSet makeEmpty(DrawInstruction.Color color, DrawInstruction.Style style, String label) {
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
        LineDataSet ds = new LineDataSet(List.of(), label);
        ds.setColor(colorHex);
        switch (style) {
            case SOLID:
                ds.disableDashedLine(); break;
            case DASHED:
                ds.enableDashedLine(20, 6, 0); break;
            case DOTTED:
                ds.enableDashedLine(3, 8, 0); break;
            case NONE:
            default:
                throw new UnsupportedOperationException();
        };

        ds.setDrawCircleHole(false);
        ds.setDrawCircles(false);
        ds.setDrawValues(false);
        return ds;
    }
    public void doDraw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label) {
        Log.d("debug_plot", "start");

        Stack<DrawInstruction.Point> descending = new Stack<>();
        LinkedList<DrawInstruction.Point> ascending = new LinkedList<>();
        DrawInstruction.Point prevPoint = points.get(0);
        ascending.add(prevPoint);

        for (int i = 1; i < points.size(); i++) {
            DrawInstruction.Point point = points.get(i);
            if (point.getX() >= prevPoint.getX()) { // sorted correctly
                if (!descending.isEmpty()) {
                    List<Util.Pair<Double, Double>> line = new ArrayList<>();
                    while (!descending.isEmpty()) {
                        DrawInstruction.Point p = descending.pop();
                        line.add(Util.Pair.create(p.getX(), p.getY()));
                    }
                    chart.addLine(line, makeEmpty(color, style, label));
                    ascending.add(prevPoint);
                }
                ascending.add(point);
            } else { // sorted reverse
                if (!ascending.isEmpty()) {
                    List<Util.Pair<Double, Double>> line = new ArrayList<>();
                    while (!ascending.isEmpty()) {
                        DrawInstruction.Point p = ascending.poll();
                        line.add(Util.Pair.create(p.getX(), p.getY()));
                    }
                    chart.addLine(line, makeEmpty(color, style, label));
                    descending.add(prevPoint);
                }
                descending.add(point);
            }
            prevPoint = point;
        }
        if (!descending.isEmpty()) {
            List<Util.Pair<Double, Double>> line = new ArrayList<>();
            while (!descending.isEmpty()) {
                DrawInstruction.Point p = descending.pop();
                line.add(Util.Pair.create(p.getX(), p.getY()));
            }
            chart.addLine(line, makeEmpty(color, style, label));
        }
        if (!ascending.isEmpty()) {
            List<Util.Pair<Double, Double>> line = new ArrayList<>();
            while (!ascending.isEmpty()) {
                DrawInstruction.Point p = ascending.poll();
                line.add(Util.Pair.create(p.getX(), p.getY()));
            }
            chart.addLine(line, makeEmpty(color, style, label));
        }

//        lines.forEach(lineData::addDataSet);
        Log.d("debug_draw", "" + points.size());
        chart.getLineData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
//        this.allLines.addAll(lines);
    }

    @Override
    public void legend(DrawInstruction.DrawMapping mapping) {

    }

    public void doUndraw() {
//        chart.clear();
//        this.allLines =  Collections.synchronizedList(new ArrayList<>());

        Optional.ofNullable(chart)
//                .map(LineChart::getLineData)
                .ifPresent(chart -> {

//                    this.allLines.forEach(lineData::removeDataSet);
                    chart.clearValues();

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

}
