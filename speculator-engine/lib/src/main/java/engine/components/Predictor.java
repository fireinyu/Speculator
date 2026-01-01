package engine.components;

import engine.PriceData.OffsetSeries;

import java.util.Collections;
import java.util.List;

public abstract class Predictor <V extends Number, R extends Number> {

    private static Predictor<?, ?> identityInstance = null;
    @SuppressWarnings("unchecked")
    public static <V extends Number, R extends Number> Predictor<V, R> identity() {
        if (identityInstance == null) {
            identityInstance = new Identity<>();
        }
        return (Predictor<V, R>) identityInstance;
    }

    public Predictor () {

    }

    public abstract List<OffsetSeries<R>> predict (List<? extends V> input, V baseline);
    // offset from point of prediction

    private static class Identity <V extends Number, R extends Number> extends Predictor<V, R> {
        @Override
        public List<OffsetSeries<R>> predict(List<? extends V> input, V baseline) {
            return Collections.emptyList();
        }
    }

}
