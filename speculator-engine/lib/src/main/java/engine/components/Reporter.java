package engine.components;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import engine.PriceData.Position;

public abstract class Reporter {
    public abstract void report(Executor.ExecutionResult result);
    public abstract void report(List<Simulator.SimResult> result);
}
