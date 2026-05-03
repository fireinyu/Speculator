package engine.components;

import java.io.Serializable;
import java.util.List;

import engine.PriceData.Position;

public abstract class ExecutionReporter implements Serializable {
    public abstract void report(List<Position> actions, Executor.ExecutionResult result);
}
