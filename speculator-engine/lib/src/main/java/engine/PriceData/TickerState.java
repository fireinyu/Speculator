package engine.PriceData;

public abstract class TickerState <V extends Number> {

    public static <V extends Number> TickerState<V> of (
            TimeSeries<V> priceData,
            Position<V> position
    ) {
        return new BasicTS<>(priceData, position);
    }

    public abstract TimeSeries<V> getPriceData ();

    public abstract Candle<V> getLatest();

    public abstract Position<V> getPosition ();

    private static class BasicTS<V extends Number> extends TickerState<V> {

        private final TimeSeries<V> priceData;
        private final Position<V> position;

        private BasicTS (TimeSeries<V> priceData, Position<V> position) {
            this.priceData = priceData;
            this.position = position;
        }

        @Override
        public TimeSeries<V> getPriceData() {
            return this.priceData;
        }

        @Override
        public Candle<V> getLatest() {
            return this.priceData.getLast();
        }

        @Override
        public Position<V> getPosition() {
            return this.position;
        }
    }

}
