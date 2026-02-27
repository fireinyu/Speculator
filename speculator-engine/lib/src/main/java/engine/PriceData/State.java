package engine.PriceData;

import java.util.Map;
import java.util.function.Function;

import engine.components.Ticker;

public abstract class State<V extends Number> {

    public static <V extends Number> State<V> empty() {
        return State.of(Map.of());
    }

    public abstract TickerState<V> getTickerState(Ticker<V> ticker);

    public static <V extends Number> MapState<V> of(
            Map<? extends Ticker<V>, ? extends TickerState<V>> map
    ) {
        return new MapState<>(map);
    }
    public static <V extends Number> LazyState<V> of(
            Function<? super Ticker<V>, ? extends TickerState<V>> handler
    ) {
        return new LazyState<>(handler);
    }

    private static class MapState <V extends Number> extends State<V> {

        private final Map<? extends Ticker<V>,? extends TickerState<V>> map;

        private MapState(Map<? extends Ticker<V>, ? extends TickerState<V>> map) {
            this.map = map;
        }
        @Override
        public TickerState<V> getTickerState(Ticker<V> ticker) {
            return this.map.get(ticker);
        }
    }

    private static class LazyState <V extends Number> extends State<V> {
        private final Function<? super Ticker<V>, ? extends TickerState<V>> handler;

        private LazyState(Function<? super Ticker<V>, ? extends TickerState<V>> handler) {
            this.handler = handler;
        }
        @Override
        public TickerState<V> getTickerState(Ticker<V> ticker) {
            return handler.apply(ticker);
        }
    }

}
