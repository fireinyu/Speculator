package engine.components;



import engine.PriceData.Candle;
import engine.PriceData.OffsetSeries;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;
import engine.Serialisation.StateLoader;
import engine.Serialisation.UserStateMachine;
import engine.Util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.rmi.ssl.SslRMIClientSocketFactory;

import engine.Serialisation.StateMachine;

public abstract class ModelPredictor extends UserStateMachine<ModelPredictor> {

    public static  ModelPredictor identity(List<Duration> intervals, List<Integer> leftDependencies) {
        return new Identity(intervals, leftDependencies);
    }

    public static  ModelPredictor offset(ModelPredictor model, Duration offset) {
        return new OffsetModel(model, offset);
    }

    protected FeatureExtractor extractor;
    protected Predictor model;
    Util.Pair<LinkedHashMap<Duration, Integer>, LinkedHashMap<Duration, Integer>> dependencies;

    public ModelPredictor (FeatureExtractor extractor,
                           Predictor model,
                           Util.Pair<LinkedHashMap<Duration, Integer>, LinkedHashMap<Duration, Integer>> dependencies,
                           Map<String, String> settings
    ) {
        // leftDependencies: include latest/now
        // rightDependencies: exclude latest/now
        super(settings);
        this.extractor = extractor;
        this.model = model;
        this.dependencies = dependencies;
    }

    public ModelPredictor (FeatureExtractor extractor,
                           Predictor model,
                           List<Duration> intervals,
                           List<Integer> leftDependencies,
                           List<Integer> rightDependencies,
                           Map<String, String> settings
    ) {
        // leftDependencies: include latest/now
        // rightDependencies: exclude latest/now
        super(settings);
        this.extractor = extractor;
        this.model = model;
        LinkedHashMap<Duration, Integer> left = new LinkedHashMap<>();
        LinkedHashMap<Duration, Integer> right = new LinkedHashMap<>();
        for (int i = 0; i < intervals.size(); i++) {
            left.put(intervals.get(i), leftDependencies.get(i));
            right.put(intervals.get(i), rightDependencies.get(i));
        }
        this.dependencies = Util.Pair.create(left, right);
    }

    public List<TimeSeries> predict(TickerState input) {
        System.out.println("Model::predict");
        System.out.println("Model::predict bug start");
        input.getIntervals().stream()
                .peek(System.out::println)
                .forEach(ts -> System.out.println(input.getPriceData(ts).size()));
        return this.predict(this.dependencies.first.keySet().stream()
                .map(dep -> Util.Pair.create(dep, input.getPriceData(dep)))
                .map(pair -> pair.second.slice(pair.second.size() - this.dependencies.first.get(pair.first), pair.second.size()))
                .collect(Collectors.toList()),
                input.getAbsoluteLatest()
        );
    }
    public List<TimeSeries> predict (List<? extends TimeSeries> input, Candle latest) {
        System.out.println("Model:predict");
        List<Float> features = this.extractor.extract(input, latest.get());
        List<OffsetSeries> output = this.model.predict(features, latest.get());
        System.out.println("Model:predict end");
        return output.stream().map(ts -> ts.at(latest.getTime())).collect(Collectors.toList());
    }

    public CompletableFuture<? extends List<TimeSeries>> predictAsync (List<? extends TimeSeries> input, Candle latest) {
        return CompletableFuture.supplyAsync(() -> this.predict(input, latest));
    }

    public Map<Duration, Integer> getRightDependencies() {
        return this.dependencies.second;
    }

    public Map<Duration, Integer> getLeftDependencies() {
        return this.dependencies.first;
    }

    private static class Identity  extends ModelPredictor {

        @Override
        public UserStateLoader<? extends StateMachine<ModelPredictor>> getLoader() {
            return new UserStateLoader<>(List.of()) {
                @Override
                public ModelPredictor load(Map<String, String> state) {
                    return null;
                }
            };
        }

        public Identity(List<Duration> intervals, List<Integer> leftDependencies) {
            super(
                    FeatureExtractor.identity(),
                    Predictor.identity(),
                    intervals,
                    leftDependencies,
                    List.of(0),
                    Map.of()
            );
        }


    }

    private static class OffsetModel extends ModelPredictor {

        private ModelPredictor model;

        private Duration offset;
        private static  LinkedHashMap<Duration, Integer> makeLeft(ModelPredictor model, Duration offset) {
            LinkedHashMap<Duration, Integer> original = model.dependencies.first;
            LinkedHashMap<Duration, Integer> updated  = new LinkedHashMap<>();
            for (Duration interval : original.keySet()) {
                double ratio = offset.toMillis()/(double)interval.toMillis();
                updated.put(interval, original.get(interval) + (int)Math.ceil(ratio));
            }
            return updated;
        }
        private static  LinkedHashMap<Duration, Integer> makeRight(ModelPredictor model, Duration offset) {
            LinkedHashMap<Duration, Integer> original = model.dependencies.second;
            LinkedHashMap<Duration, Integer> updated  = new LinkedHashMap<>();
            for (Duration interval : original.keySet()) {
                double ratio = offset.toMillis()/(double)interval.toMillis();
                updated.put(interval, Math.max(0, original.get(interval) - (int)Math.floor(ratio)));
            }
            return updated;
        }

        public OffsetModel(ModelPredictor model, Duration offset) {
            super(
                    null, null, Util.Pair.create(OffsetModel.makeLeft(model, offset), OffsetModel.makeRight(model, offset)), model.save());
            this.model = model;
            this.offset = offset;
        }


        @Override
        public List<TimeSeries> predict(List<? extends TimeSeries> input, Candle latest) {
            ZonedDateTime anchor = latest.getTime().minus(offset);
            List<Integer> anchorIndices = input.stream()
                    .map(ts -> ts.pointsNotAfter(anchor)-1)
                    .collect(Collectors.toList());
            Candle offsetLatest = Util.combine(
                    input.stream(),
                    anchorIndices.stream(),
                    TimeSeries::get
            ).max(Comparator.comparing(Candle::getTime)).get();
            System.out.println(anchor);
            List<TimeSeries> offsetInput = new ArrayList<>();
            int inputIdx = 0;
            for (Duration interval: model.dependencies.first.keySet()) {
                int ld = model.dependencies.first.get(interval);
                TimeSeries ts = input.get(inputIdx);
                int anchorIndex = anchorIndices.get(inputIdx);
                offsetInput.add(ts.slice(anchorIndex+1 - ld, anchorIndex+1));
                inputIdx++;
            }
            return this.model.predict(offsetInput, offsetLatest);
        }

        @Override
        public UserStateLoader<? extends StateMachine<ModelPredictor>> getLoader() {
            return new OffSetLoader((UserStateLoader<ModelPredictor>)model.getLoader());
        }

        @Override
        public Map<String, String> save() {
            Map<String, String> settings = super.save();
            settings.put("offset", String.valueOf(this.offset.toMillis()));
            return settings;
        }
    }

    public static class OffSetLoader extends UserStateLoader<ModelPredictor> {
        private UserStateLoader<ModelPredictor> loader;
        public OffSetLoader(UserStateLoader<ModelPredictor> loader) {
            super(loader.getOptions());
            super.addOption("offset");
            this.loader = (UserStateLoader<ModelPredictor>) loader;
        }

        @Override
        public ModelPredictor load(Map<String, String> state) {
            return new OffsetModel(
                    this.loader.load(state),
                    Duration.ofMillis(Long.parseLong(state.get("offset")))
                    );
        }


        @Override
        public String toString() {
            return this.loader.toString() + "|offset";
        }
    }

}
