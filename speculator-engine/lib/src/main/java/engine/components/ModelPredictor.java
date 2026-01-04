package engine.components;



import engine.PriceData.Candle;
import engine.PriceData.OffsetSeries;
import engine.PriceData.TimeSeries;
import engine.Serialisation.StateLoader;
import engine.Util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import engine.Serialisation.StateMachine;

public abstract class ModelPredictor<V extends Number, R extends Number> implements StateMachine<ModelPredictor<V, R>> {

    public static <V extends Number, R extends Number> ModelPredictor<V, R> identity(List<Duration> intervals, List<Integer> leftDependencies) {
        return new Identity<>(intervals, leftDependencies);
    }

    protected FeatureExtractor<V> extractor;
    protected Predictor<V, R> model;

    private ArrayList<Integer> leftDependencies;
    private ArrayList<Integer> rightDependencies;
    private ArrayList<Duration> intervals;

    public ModelPredictor (FeatureExtractor<V> extractor,
                           Predictor<V, R> model,
                           List<Duration> intervals,
                           List<Integer> leftDependencies,
                           List<Integer> rightDependencies) {
        // leftDependencies: include latest/now
        // rightDependencies: exclude latest/now
        this.extractor = extractor;
        this.model = model;
        this.leftDependencies = new ArrayList<>();
        this.leftDependencies.addAll(leftDependencies);
        this.rightDependencies = new ArrayList<>();
        this.rightDependencies.addAll(rightDependencies);
        this.intervals = new ArrayList<>();
        this.intervals.addAll(intervals);
    }

    public List<TimeSeries<R>> predict (List<? extends TimeSeries<V>> input, Candle<V> latest) {
        List<V> features = this.extractor.extract(input, latest.get());
        List<OffsetSeries<R>> output = this.model.predict(features, latest.get());
        return output.stream().map(ts -> ts.at(latest.getTime())).collect(Collectors.toList());
    }

    public CompletableFuture<? extends List<TimeSeries<R>>> predictAsync (List<? extends TimeSeries<V>> input, Candle<V> latest) {
        return CompletableFuture.supplyAsync(() -> this.predict(input, latest));
    }

    public List<Duration> getRanges() {
        return Util.combine(this.intervals.stream(), this.rightDependencies.stream(), (interval, rDep) -> interval.multipliedBy(rDep.longValue()))
                .collect(Collectors.toList());
    }

    public <T extends Upstream & Snapshottable> List<T> requestLeftUpstreams (UpstreamAdapter adapter) {
        ArrayList<T> upstreams = new ArrayList<>();
        for (int i = 0; i < this.intervals.size(); i++){
            upstreams.add(adapter.makeLeftFor(
                    this.intervals.get(i),
                    this.leftDependencies.get(i)
            ));
        }
        return upstreams;
    }

    public List<? extends Snapshottable> requestRightUpstreams (UpstreamAdapter adapter) {
        ArrayList<Snapshottable> upstreams = new ArrayList<>();
        for (int i = 0; i < this.intervals.size(); i++){
            upstreams.add(adapter.makeRightFor(
                    this.intervals.get(i),
                    this.rightDependencies.get(i)
                    ));
        }
        return upstreams;
    }

    private static class Identity <V extends Number, R extends Number> extends ModelPredictor<V, R> {
        public Identity(List<Duration> intervals, List<Integer> leftDependencies) {
            super(
                    FeatureExtractor.identity(),
                    Predictor.identity(),
                    intervals,
                    leftDependencies,
                    List.of(0)
            );
        }

        @Override
        public StateLoader<? extends StateMachine<ModelPredictor<V, R>>> getLoader() {
            return new StateLoader<>() {
                @Override
                public Identity<V, R> load(Map<String, String> state) {
                    return new Identity<>(List.of(), List.of());
                }

                @Override
                public String toString(Map<String, String> state) {
                    return "identity";
                }
            };
        }

        @Override
        public Map<String, String> save() {
            return Collections.emptyMap();
        }


    }

}
