package engine.components;



import engine.PriceData.Candle;
import engine.PriceData.OffsetSeries;
import engine.PriceData.Series;
import engine.PriceData.TimeSeries;
import engine.Serialisation.StateLoader;
import engine.Util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import engine.Serialisation.StateMachine;
import kotlin.collections.builders.MapBuilder;

public abstract class ModelPredictor<V extends Number, R extends Number> implements StateMachine<ModelPredictor<V, R>> {

    public static <V extends Number, R extends Number> ModelPredictor<V, R> identity(List<Duration> intervals, List<Integer> leftDependencies) {
        return new Identity<>(intervals, leftDependencies);
    }

    public static <V extends Number, R extends Number> ModelPredictor<V, R> offset(ModelPredictor<V, R> model, Duration offset) {
        return new OffsetModel<>(model, offset);
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

    public Map<Duration, Integer> getRightDependencies() {
        HashMap<Duration, Integer> map = new HashMap<>();
        IntStream.range(0, this.intervals.size())
                .forEach(i -> map.put(intervals.get(i), rightDependencies.get(i)));
        return map;
    }

    public Map<Duration, Integer> getLeftDependencies() {
        HashMap<Duration, Integer> map = new HashMap<>();
        IntStream.range(0, this.intervals.size())
                .forEach(i -> map.put(intervals.get(i), leftDependencies.get(i)));
        return map;
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

    private static class OffsetModel<V extends Number, R extends Number> extends ModelPredictor<V, R> {

        private ModelPredictor<V, R> model;
        private Duration offset;
        private static <V extends Number, R extends Number> List<Integer> makeLeft(ModelPredictor<V, R> model, Duration offset) {
            List<Integer> leftDependencies = new ArrayList<>();
            IntStream.range(0, model.intervals.size())
                    .forEach(i -> {
                        Duration interval = model.intervals.get(i);
                        double ratio = offset.toMillis()/interval.toMillis();
                        leftDependencies.add(model.leftDependencies.get(i) + (int)Math.ceil(ratio));
                    });
            return leftDependencies;
        }
        private static <V extends Number, R extends Number> List<Integer> makeRight(ModelPredictor<V, R> model, Duration offset) {
            List<Integer> rightDependencies = new ArrayList<>();
            IntStream.range(0, model.intervals.size())
                    .forEach(i -> {
                        Duration interval = model.intervals.get(i);
                        double ratio = offset.toMillis()/interval.toMillis();
                        rightDependencies.add(Math.max(0, model.rightDependencies.get(i) - (int)Math.floor(ratio)));
                    });
            return rightDependencies;
        }
        public OffsetModel(ModelPredictor<V, R> model, Duration offset) {
            super(
                    null, null, model.intervals, OffsetModel.makeLeft(model, offset), OffsetModel.makeRight(model, offset));
            this.model = model;
            this.offset = offset;
        }

        @Override
        public StateLoader<? extends StateMachine<ModelPredictor<V, R>>> getLoader() {
            return new OffSetLoader(model);
        }

        @Override
        public Map<String, String> save() {
            Map<String, String> state = new HashMap<>(model.save());
            state.put("_offset", String.valueOf(offset.toMillis()));
            return state;
        }

        @Override
        public List<TimeSeries<R>> predict(List<? extends TimeSeries<V>> input, Candle<V> latest) {
            ZonedDateTime anchor = latest.getTime().minus(offset);
            List<Integer> anchorIndices = input.stream()
                    .map(ts -> ts.indexAt(anchor))
                    .collect(Collectors.toList());
            Candle<V> offsetLatest = Util.combine(
                    input.stream(),
                    anchorIndices.stream(),
                    TimeSeries::get
            ).max(Comparator.comparing(Candle::getTime)).get();
            List<TimeSeries<V>> offsetInput = new ArrayList<>();
            IntStream.range(0, model.intervals.size())
                    .forEach(i -> {
                        int ld = model.leftDependencies.get(i);
                        TimeSeries<V> ts = input.get(i);
                        int anchorIndex = anchorIndices.get(i);
                        offsetInput.add(ts.slice(anchorIndex + 1 - ld, anchorIndex + 1));

                    });

            return this.model.predict(offsetInput, offsetLatest);
        }


    }

    private static class OffSetLoader <V extends Number, R extends Number> implements StateLoader<ModelPredictor<V, R>> {
        private StateLoader<ModelPredictor<V, R>> loader;
        public OffSetLoader(ModelPredictor<V, R> template) {
            super();
            this.loader = (StateLoader<ModelPredictor<V, R>>)template.getLoader();
        }

        @Override
        public ModelPredictor<V, R> load(Map<String, String> state) {
            return new OffsetModel<>(
                    this.loader.load(state),
                    Duration.ofMillis(Long.parseLong(state.get("_offset")))
                    );
        }

        @Override
        public String toString(Map<String, String> state) {
            return this.loader.toString(state) + "|os:" + state.get("_offset") +"ms";
        }

        @Override
        public String toString() {
            return this.loader.toString() + "|offset";
        }
    }

}
