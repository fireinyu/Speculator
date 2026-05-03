package com.example.speculator;

import engine.control.PredictManager;

import java.util.List;

public class StringPlotter <X extends Number, Y extends Number> extends Plotter<X, Y> {

    @Override
    public void unplot() {

    }

    @Override
    public void plotAllBackTest(List<PredictManager.BacktestResult<X, Y>> backtestResults) {

    }

    @Override
    public void plotAllPredict(List<PredictManager.PredictResult<X, Y>> predictResults) {

    }
}
