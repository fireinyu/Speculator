package com.example.speculator.uiComponents;

import static java.lang.Double.min;

import android.content.Context;

import com.example.speculator.MPDrawer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import engine.Util;
import engine.components.DrawInstruction;

public class DateTimeChart extends LineChart {
    private DTFormatter formatter;
    public DateTimeChart(Context context) {
        super(context);
        formatter = new DTFormatter();
        getXAxis().setValueFormatter(formatter);
        setData(new LineData());
        clearValues();
    }

    @Override
    public void clearValues() {
        formatter.leftMost = -1;
        super.clearValues();
    }


    public void addLine(List<Util.Pair<Double, Double>> points, LineDataSet dataSet) {
        double left = points.stream()
                .map(pair -> pair.first)
                .min(Double::compare)
                .orElse(Double.MAX_VALUE);
        if (left < formatter.leftMost || formatter.leftMost == -1) {
            double shift = formatter.leftMost - left;
            formatter.setLeftMost(left);
            for (ILineDataSet ids : getLineData().getDataSets()) {
                LineDataSet ds = (LineDataSet) ids;
                ds.getValues().forEach(entry -> entry.setX((float)(entry.getX()+shift)));
            }
        }
        List<Entry> line = points.stream()
                .map(pair -> new Entry((float)(pair.first-formatter.leftMost),(float)(double)pair.second))
                .collect(Collectors.toList());
        dataSet.setValues(line);
        LineData lines = getLineData();
        lines.addDataSet(dataSet);
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
