package engine.components;

import engine.PriceData.OffsetSeries;

import java.util.Collections;
import java.util.List;

public abstract class Predictor {

    private static Predictor identityInstance = null;
    public static Predictor identity() {
        if (identityInstance == null) {
            identityInstance = new Identity();
        }
        return (Predictor) identityInstance;
    }

    public Predictor () {

    }

    public abstract List<OffsetSeries> predict (List<Float> input, float baseline);
    // offset from point of prediction

    private static class Identity extends Predictor {
        @Override
        public List<OffsetSeries> predict(List<Float> input, float baseline) {
            return Collections.emptyList();
        }
    }

}
