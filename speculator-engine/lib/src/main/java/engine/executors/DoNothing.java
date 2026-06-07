package engine.executors;

import engine.PriceData.CostPosition;
import engine.PriceData.Position;
import engine.components.Executor;
import engine.components.Ticker;

public class DoNothing extends Executor {
    public DoNothing(int index) {
        super(index);
    }

    @Override
    public ExecutionResult executeLimitOrder(Ticker ticker, CostPosition action) {
        return new ExecutionResult(ticker, CompletionStatus.SUCCESS, action);
    }

    @Override
    public ExecutionResult executeMarketOrder(Ticker ticker, Position action) {
        return new ExecutionResult(ticker, CompletionStatus.SUCCESS, CostPosition.from(action, Double.NaN));
    }
}
