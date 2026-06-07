package engine.components;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.PriceData.CostPosition;
import engine.PriceData.Position;
import engine.PriceData.State;

public class Simulator {
    private ZonedDateTime at;
    private Duration interval;
    HashMap<Ticker, CostPosition> last;

    public Simulator(ZonedDateTime from, Duration interval) {
        at = from.minus(interval);
        this.interval = interval;
        last = new HashMap<>();
    }
    public ZonedDateTime step() {
        at = at.plus(interval);
        return this.at;
    }
    public SimResult act(State state, Map<Ticker, Position> actions) {
        for (Ticker ticker : actions.keySet()) {
            Position position = actions.get(ticker);
            float price = state.getTickerState(ticker).getAbsoluteLatest().get();
            last.put(ticker, last.getOrDefault(ticker, CostPosition.makeEmpty()).apply(CostPosition.from(position, price)));
        }
        return new SimResult(at, last, state);
    }

    public static class nowSimulator extends Simulator {
        public nowSimulator() {
            super(ZonedDateTime.now(), Duration.ZERO);
        }

        @Override
        public ZonedDateTime step() {
            super.at = ZonedDateTime.now();
            return super.at;
        }
    }
    public static class SimResult {
        private ZonedDateTime at;
        private double nav;
        private Map<Ticker, Double> navMap;
        private Map<Ticker, CostPosition> positions;
        private Map<Ticker, Float> prices;
        public SimResult(ZonedDateTime at, Map<Ticker, CostPosition> positions, State state) {
            this.at = at;
            this.positions = new HashMap<>(positions);
            prices = positions.keySet().stream()
                    .collect(Collectors.toMap(
                            ticker -> ticker,
                            ticker -> state.getTickerState(ticker).getAbsoluteLatest().get()
                    ));
            navMap = positions.keySet().stream()
                    .collect(Collectors.toMap(
                            ticker -> ticker,
                            ticker -> positions.get(ticker).evaluate(at, prices.get(ticker)).getNetValue()
                    ));
            nav = navMap.values().stream().parallel().reduce(0.0, Double::sum, Double::sum);

        }

        public double nav() {
            return nav;

        }
        public double nav(Ticker ticker) {
            return navMap.get(ticker);
        }

        public double price(Ticker ticker) {
            return prices.get(ticker);
        }

        public ZonedDateTime when() {
            return at;
        }
        public List<Ticker> tickers() {
            return new ArrayList<>(navMap.keySet());
        }
        public CostPosition position(Ticker ticker) {
            return positions.get(ticker);
        }
    }
//    private UpstreamManager<T> source;
//    public void execute(List<Position> actions) {
//        // actions: in order of recommendation
//        ...
//    }
}
