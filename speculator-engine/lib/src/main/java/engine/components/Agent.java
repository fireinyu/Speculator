package engine.components;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import engine.PriceData.Position;
import engine.PriceData.State;
import engine.Serialisation.UserStateMachine;
import engine.control.PredictManager;

public abstract class Agent extends UserStateMachine<Agent> {
    public Agent(Map<String, String> settings) {
        super(settings);
    }
    public abstract Map<Ticker, Position> suggest(State state, Collection<? extends PredictManager.PredictResult> predictions);

    // returns delta positions (in order of recommendation)
}
