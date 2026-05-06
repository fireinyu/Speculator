package com.example.speculator;

import android.graphics.Color;
import android.util.Log;

import engine.components.DrawInstruction;
import engine.components.InstructedDrawer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class MPDrawer extends InstructedDrawer {
    private transient LineChart chart;
    private ArrayList<LineDataSet> allLines;
    public MPDrawer () {
        this.allLines = new ArrayList<>();
    }
    public void setChart(LineChart chart) {
        if (!(this.chart == null)) {
            this.undraw();
        }
        this.chart = chart;
    }

    @Override
    public void draw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label) {
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
        for (int i = 1; i < points.size(); i++) {
            DrawInstruction.Point point = points.get(i);
            if (point.getX().doubleValue() >= prevPoint.getX().doubleValue()) { // sorted correctly
                if (!descending.isEmpty()) {
                    LineDataSet line = new LineDataSet(new ArrayList<>(),label);
                    while (!descending.isEmpty()) {
                        DrawInstruction.Point p = descending.pop();
                        line.addEntry(new Entry(p.getX().floatValue(), p.getY().floatValue()));
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
                        line.addEntry(new Entry(p.getX().floatValue(), p.getY().floatValue()));
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
                line.addEntry(new Entry(p.getX().floatValue(), p.getY().floatValue()));
            }
            lines.add(line);
        }
        if (!ascending.isEmpty()) {
            LineDataSet line = new LineDataSet(new ArrayList<>(),label);
            while (!ascending.isEmpty()) {
                DrawInstruction.Point p = ascending.poll();
                line.addEntry(new Entry(p.getX().floatValue(), p.getY().floatValue()));
            }
            lines.add(line);
        }

        LineData lineData = Optional.ofNullable(chart.getLineData()).orElse(new LineData());
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
        this.chart.setData(lineData);
        this.chart.notifyDataSetChanged();
        this.chart.invalidate();
        this.allLines.addAll(lines);
    }

    @Override
    public void legend(DrawInstruction.DrawMapping mapping) {

    }

    @Override
    public void undraw() {
        Optional.ofNullable(chart.getLineData())
                .ifPresent(lineData -> {
                    this.allLines.forEach(lineData::removeDataSet);
                    chart.setData(lineData);
                    chart.notifyDataSetChanged();
                    chart.invalidate();
                    this.allLines =  new ArrayList<>();
                });
    }
}
