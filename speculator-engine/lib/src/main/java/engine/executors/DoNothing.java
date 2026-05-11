package engine.executors;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import engine.PriceData.NAVPosition;
import engine.PriceData.Position;
import engine.components.Executor;
import engine.components.Ticker;

public class DoNothing extends Executor {
    public DoNothing(int index) {
        super(index);
    }

    @Override
    public ExecutionResult executeLimitOrder(Ticker ticker, NAVPosition action) {
        return new ExecutionResult(ticker, CompletionStatus.SUCCESS, action);
    }

    @Override
    public ExecutionResult executeMarketOrder(Ticker ticker, Position action) {
        return new ExecutionResult(ticker, CompletionStatus.SUCCESS, NAVPosition.from(action, Double.NaN));
    }
}
