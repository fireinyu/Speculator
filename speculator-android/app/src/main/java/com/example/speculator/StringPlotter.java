package com.example.speculator;

import android.util.Log;

import engine.components.Plotter;
import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.util.Arrays;
import java.util.List;

public class StringPlotter <X extends Number, Y extends Number> extends Plotter<Y> {

    @Override
    public void unplot() {

    }

    @Override
    public void plotAll(List<? extends Ticker> tickers, List<? extends TimeSeries<Y>> featuresLs, List<? extends TimeSeries<Y>> predsLs, List<? extends TimeSeries<Y>> targetsLs) {
        return;
    }

    @Override
    public void plot(TimeSeries<Y> input, String label) {
        Log.d("Plot/Time",Arrays.toString(input.getTimes().toArray()));
        Log.d("Plot/Value",Arrays.toString(input.get().toArray()));
    }

}
