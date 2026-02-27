package engine.PriceData;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;

public abstract class TickerState <V extends Number> {

    public static <V extends Number> TickerState<V> of (
            Map<Duration,TimeSeries<V>> priceData,
            Position<V> position
    ) {
        return new MapTickerState<>(priceData, position);
    }

    public abstract TimeSeries<V> getPriceData (Duration interval);

    public abstract Candle<V> getAbsoluteLatest();
    // absolute latest across all intervals

    public abstract Candle<V> getCommonLatest();
    // latest in range of all intervals

    public abstract Position<V> getPosition ();

    private static class MapTickerState<V extends Number> extends TickerState<V> {

        private final Map<Duration,TimeSeries<V>> priceData;
        private final Position<V> position;

        private MapTickerState(Map<Duration,TimeSeries<V>> priceData, Position<V> position) {
            this.priceData = priceData;
            this.position = position;
        }

        @Override
        public TimeSeries<V> getPriceData(Duration interval) {
            return this.priceData.get(interval);
        }

        @Override
        public Candle<V> getCommonLatest() {
            return null;
        }

        @Override
        public Candle<V> getAbsoluteLatest() {
            return this.priceData.values().stream()
                    .map(TimeSeries::getLast)
                    .max(Comparator.comparing(Candle::getTime))
                    .get();
        }

        @Override
        public Position<V> getPosition() {
            return this.position;
        }
    }

}
