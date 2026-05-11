package engine.agents;

import java.util.Collection;
import java.util.Map;

import engine.PriceData.Position;
import engine.PriceData.State;
import engine.Serialisation.StateMachine;
import engine.components.Agent;
import engine.components.Ticker;
import engine.control.PredictManager;

public class SimpleExpect extends Agent {
    public SimpleExpect(Map<String, String> settings) {
        super(settings);
    }

    @Override
    public Map<Ticker, Position> suggest(State state, Collection<? extends PredictManager.PredictResult> predictions) {
        return Map.of();
    }

    @Override
    public UserStateLoader<? extends StateMachine<Agent>> getLoader() {
        return null;
    }
}