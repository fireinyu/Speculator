package engine.components;

import java.util.List;

import engine.PriceData.NAVPosition;
import engine.PriceData.Position;
import engine.Serialisation.CoreStateMachine;
import engine.menus.Executors;

public abstract class Executor extends CoreStateMachine<Executor> {
    public static enum CompletionStatus {
        SUCCESS,
        PARTIAL,
        FAIL,
        UNKNOWN
    }
    public static class ExecutionResult {
        private Ticker ticker;
        private NAVPosition filled;
        private CompletionStatus status;

        public NAVPosition getFilled() {
            return filled;
        }

        public CompletionStatus getStatus() {
            return status;
        }

        public Ticker getTicker() {
            return ticker;
        }

        public ExecutionResult(Ticker ticker, CompletionStatus status, NAVPosition filled) {
            this.ticker = ticker;
            this.status = status;
            this.filled = filled;
        }
    }

    @Override
    public CoreStateLoader<Executor> getLoader() {
        return new ExecutorLoader();
    }

    public Executor(int index) {
        super(index);
    }

    public ExecutionResult execute(Ticker ticker, Position action) {
        if (action instanceof NAVPosition) {
            return executeLimitOrder(ticker, (NAVPosition) action);
        } else {
            return executeMarketOrder(ticker, action);
        }
    }

    public abstract ExecutionResult executeMarketOrder(Ticker ticker, Position action);
    public abstract ExecutionResult executeLimitOrder(Ticker ticker, NAVPosition action);

    private static class ExecutorLoader extends CoreStateLoader<Executor> {
        @Override
        public List<Executor> getSource() {
            return Executors.list;
        }
    }
}
