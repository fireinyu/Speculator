package engine.components;

import engine.PriceData.Series;

import java.util.List;

public abstract class FeatureExtractor<T extends Number>{

    private static FeatureExtractor<?> identityInstance = null;

    @SuppressWarnings("unchecked")
    public static <T extends Number> FeatureExtractor<T> identity() {
        if (identityInstance == null) {
            identityInstance = new FeatureExtractor.Identity<>();
        }
        return (FeatureExtractor<T>) identityInstance;
    }

    public FeatureExtractor() {
        
    }

    public abstract List<T> extract (List<? extends Series<T>> input, T baseline);
    //Series end at point of prediction

    private static class Identity <T extends Number> extends FeatureExtractor<T> {
        @Override
        public List<T> extract(List<? extends Series<T>> input, T baseline) {
            return input.get(0).get();
        }
    }
}
