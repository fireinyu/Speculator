package engine.components;

import java.util.Collection;
import java.util.List;

import engine.PriceData.Position;
import engine.Serialisation.CoreStateMachine;

public abstract class Executor extends CoreStateMachine<Executor> {
    public static enum ExecutionResult {
        UNKNOWN,
        SUCCESS,
        FAIL
    }

    public Executor(int index, ExecutionReporter reporter) {
        super(index);
    }

    public abstract ExecutionResult execute(List<Position> actions);

}
