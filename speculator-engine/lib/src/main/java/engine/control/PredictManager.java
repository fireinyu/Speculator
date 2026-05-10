package engine.control;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import engine.PriceData.State;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;
import engine.PriceData.Upstream;
import engine.Serialisation.Menu;
import engine.Util;
import engine.components.ModelPredictor;
import engine.components.Predictor;
import engine.components.Ticker;

public class PredictManager {
    private static Map<Duration, Integer> unionCommon (Collection<? extends Map<Duration, Integer>> commons) {
        Map<Duration, Integer> result = new HashMap<>();
        for (Map<Duration, Integer> common : commons) {
            for (Duration interval : common.keySet()) {
                if (result.containsKey(interval)) {
                    result.put(interval, Math.max(result.get(interval), common.get(interval)));
                } else {
                    result.put(interval, common.get(interval));
                }
            }
        }
        return result;
    }
//    public static <T extends Number, V extends Number> ScheduledExecutorService predictLoop(
//            Supplier<PredictManager> getManager,
//            Supplier<? extends Collection<Ticker> > getTickers,
//            Duration interval,
//            Consumer<? super List<PredictResult>> callback) {
//        ScheduledExecutorService clock = Executors.newScheduledThreadPool(Thread.activeCount());
//        clock.scheduleWithFixedDelay(() -> {
////            System.out.println("debug pm: start");
//            callback.accept(getManager.get().predict(getTickers.get()));
//        },0, interval.toMillis(), TimeUnit.MILLISECONDS);
//        return clock;
//    }
//    private Menu<ModelPredictor> predictors;

//    private Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> cachedDependencies;

//    public PredictManager(Menu<ModelPredictor> predictors) {
//        this.predictors = predictors;
//    }

    private Map<Duration, Integer> combineLD(List<ModelPredictor> models) {
        return PredictManager.unionCommon(models.stream()
                .map(ModelPredictor::getLeftDependencies)
                .collect(Collectors.toList())
        );
    }

    private Map<Duration, Integer> combineRD(List<ModelPredictor> models) {
        return PredictManager.unionCommon(models.stream()
                .map(ModelPredictor::getRightDependencies)
                .collect(Collectors.toList())
        );
    }

    public Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> getDependencies(List<ModelPredictor> models) {
        return Util.Pair.create(
                combineLD(models),
                combineRD(models)
        );
    }

    public List<PredictResult> predict(State state, List<Ticker> tickers, List<ModelPredictor> models) {
        List<PredictResult> results =  new ArrayList<>();
        for (Ticker ticker : tickers) {
            Map<ModelPredictor, TimeSeries> predictions = new HashMap<>();
            for (ModelPredictor model : models) {
//                System.out.println("PM::predictFS bug start");
                TimeSeries prediction = model.predict(state.getTickerState(ticker)).stream()
                        .reduce(
                                TimeSeries.empty(),
                                TimeSeries::merge
                        );
                predictions.put(model, prediction);
            }
            results.add(new PredictResult(ticker, predictions));
        }
//        System.out.println("PM::predictFS end");
        return results;
    }

    public static class PredictResult {
        private Ticker ticker;
        private Map<ModelPredictor, TimeSeries> predictions;

        public PredictResult(Ticker ticker, Map<ModelPredictor, TimeSeries> predictions) {
            this.ticker = ticker;
            this.predictions = predictions;
        }

        public Map<ModelPredictor, TimeSeries> getPrediction() {
            return predictions;
        }

        public Ticker getTicker() {
            return ticker;
        }

    }

//    public static class PredictResult<T extends Number, V extends Number> extends PredictResult {
//        private TimeSeries targets;
//
//        public PredictResult(Ticker ticker, Map<ModelPredictor, TimeSeries> predictions, TimeSeries targets) {
//            super(ticker, predictions);
//            this.targets = targets;
//        }
//
//        public PredictResult(PredictResult predictResult ,TimeSeries targets) {
//            this(predictResult.ticker, predictResult.features, predictResult.predictions, targets);
//        }
//
//        public TimeSeries getTargets() {
//            return targets;
//        }
//    }
}
