package org.example;

import engine.components.ExecutionReporter;
import engine.components.Executor.ExecutionResult;
import java.util.List;
import engine.PriceData.Position;;

public class TestReporter extends ExecutionReporter{
    
    @Override
    public void report(List<Position> action, ExecutionResult result) {
    }
}
