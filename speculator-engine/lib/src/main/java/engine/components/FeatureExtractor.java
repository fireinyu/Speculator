package engine.components;

import engine.PriceData.Series;

import java.util.List;

public abstract class FeatureExtractor{

    private static FeatureExtractor identityInstance = null;

    public static  FeatureExtractor identity() {
        if (identityInstance == null) {
            identityInstance = new FeatureExtractor.Identity();
        }
        return (FeatureExtractor) identityInstance;
    }

    public FeatureExtractor() {
        
    }

    public abstract List<Float> extract (List<? extends Series> input, float baseline);
    //Series end at point of prediction

    private static class Identity  extends FeatureExtractor {
        @Override
        public List<Float> extract(List<? extends Series> input, float baseline) {
            return input.get(0).get();
        }
    }
}
