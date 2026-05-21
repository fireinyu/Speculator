package engine.agents;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.PriceData.Position;
import engine.PriceData.State;
import engine.Serialisation.StateMachine;
import engine.Util;
import engine.components.Agent;
import engine.components.Ticker;
import engine.control.PredictManager;

public class SimpleExpect extends Agent {
    /// Strategy description:
    /// consider the predicted best-performing ticker by simple mean -> buy it if it is positive enough
    /// consider the predicted worst-performing ticker by simple mean -> sell it if it is negative enough

    private float threshold;
    private double unitSize;
    public SimpleExpect(Map<String, String> settings) {
        super(settings);
        this.threshold = Float.parseFloat(settings.get("action threshold"));
        this.unitSize = Double.parseDouble(settings.get("unit size"));
    }

    @Override
    public Map<Ticker, Position> suggest(State state, Collection<? extends PredictManager.PredictResult> predictions) {
        Util.Pair<Ticker, Float> minSoFar = Util.Pair.create(null, Float.MAX_VALUE);
        Util.Pair<Ticker, Float> maxSoFar = Util.Pair.create(null, Float.MIN_VALUE);
        for (PredictManager.PredictResult result : predictions) {
            Ticker ticker = result.getTicker();
            float base = state.getTickerState(ticker).getAbsoluteLatest().get();
            List<Float> pts = result.getPrediction().values().stream()
                    .parallel()
                    .map(ts -> ts.extract((dt, px) -> px/base))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            float exp = pts.stream().parallel().reduce(0f, Float::sum, Float::sum) / pts.size();
            if (exp < minSoFar.second) {
                minSoFar = Util.Pair.create(ticker, exp);
            }
            if (exp > maxSoFar.second) {
                maxSoFar = Util.Pair.create(ticker, exp);
            }
        }
        Map<Ticker, Position> actions = new HashMap<>();
        if (minSoFar.second < 1 - threshold) {
            actions.put(minSoFar.first, Position.makeShort(unitSize));
        }
        if (maxSoFar.second > 1 + threshold) {
            actions.put(maxSoFar.first, Position.makeLong(unitSize));
        }
        return actions;
    }

    @Override
    public String toString() {
        return "simple expect";
    }

    @Override
    public UserStateLoader<? extends StateMachine<Agent>> getLoader() {
        return new SELoader();
    }

    public static class SELoader extends UserStateLoader<Agent> {
        public SELoader() {
            super(List.of("action threshold", "unit size"));
        }

        @Override
        public Agent load(Map<String, String> state) {
            return new SimpleExpect(state);
        }
    }
}