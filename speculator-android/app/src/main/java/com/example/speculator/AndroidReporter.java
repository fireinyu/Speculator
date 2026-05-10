package com.example.speculator;

import java.util.Map;

import engine.PriceData.Position;
import engine.components.Reporter;
import engine.components.Executor;
import engine.components.Ticker;

public class AndroidReporter extends Reporter {
    @Override
    public void report(Map<Ticker, Position> actions, Executor.ExecutionResult result) {

    }
}
