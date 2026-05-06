package engine.components;

import java.util.Collection;
import java.util.List;

import engine.PriceData.Position;
import engine.Serialisation.CoreStateMachine;
import engine.Serialisation.StateMachine;
import engine.menus.Executors;

public abstract class Executor extends CoreStateMachine<Executor> {
    public static enum ExecutionResult {
        UNKNOWN,
        SUCCESS,
        FAIL
    }

    @Override
    public CoreStateLoader<Executor> getLoader() {
        return new ExecutorLoader();
    }

    public Executor(int index, ExecutionReporter reporter) {
        super(index);
    }

    public abstract ExecutionResult execute(List<Position> actions);

    private static class ExecutorLoader extends CoreStateLoader<Executor> {
        @Override
        public List<Executor> getSource() {
            return Executors.list;
        }
    }
}
