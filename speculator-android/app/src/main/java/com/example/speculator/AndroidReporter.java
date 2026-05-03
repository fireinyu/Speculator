package com.example.speculator;

import java.util.List;

import engine.PriceData.Position;
import engine.components.ExecutionReporter;
import engine.components.Executor;

public class AndroidReporter extends ExecutionReporter {
    @Override
    public void report(List<Position> actions, Executor.ExecutionResult result) {

    }
}
