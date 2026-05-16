package com.example.speculator;

import java.util.List;
import java.util.Map;

import engine.PriceData.Position;
import engine.components.Reporter;
import engine.components.Executor;
import engine.components.Simulator;
import engine.components.Ticker;

public class AndroidReporter extends Reporter {
    @Override
    public void report(Executor.ExecutionResult result) {
        System.out.println(result);
    }

    @Override
    public void report(List<Simulator.SimResult> result) {
        System.out.println(result);

    }
}
