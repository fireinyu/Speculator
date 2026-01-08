package engine.components;

import java.sql.Time;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Timer;

import ai.djl.Model;
import engine.Instances.UpstreamAdapters;
import engine.PriceData.Candle;
import engine.PriceData.State;
import engine.PriceData.Ticker;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;
import jdk.jshell.spi.SPIResolutionException;

public class PredictManager<T extends Number, V extends Number>{
    public static <T extends Number, V extends Number> ScheduledExecutorService predictLoop(
            Supplier<? extends PredictManager<T, V>> getManager,
            Supplier<? extends List<Ticker>> getTickers,
            Duration interval,
            Consumer<List<PredictResult<T, V>>> callback) {
        ScheduledExecutorService clock = Executors.newSingleThreadScheduledExecutor();
        clock.scheduleWithFixedDelay(() -> {
            List<CompletableFuture<PredictResult<T, V>>> predCF =  getTickers.get().stream()
                    .map(getManager.get()::predictAsync)
                    .collect(Collectors.toList());
            CompletableFuture.allOf(predCF.toArray(new CompletableFuture<?>[]{})).thenRunAsync(() -> {
                callback.accept(predCF.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            });
        },0, interval.toMillis(), TimeUnit.MILLISECONDS);
        return clock;
    }
    private List<ModelPredictor<T, V>> predictors;
    private Map<Duration, Integer> leftDependencies; //include latest / now
    private Map<Duration, Integer> rightDependencies; //exclude latest / now

    public PredictManager(List<ModelPredictor<T, V>> predictors) {
        this.predictors = predictors;
        leftDependencies = new HashMap<>();
        predictors.forEach(model -> {
            Map<Duration, Integer> leftDeps = model.getLeftDependencies();
            leftDeps.forEach((interval, ld) -> {
                if (leftDependencies.containsKey(interval)) {
                    leftDependencies.put(interval, Math.max(leftDependencies.get(interval), ld));
                } else {
                    leftDependencies.put(interval, ld);
                }
            }) ;
        });
        rightDependencies = new HashMap<>();
        predictors.forEach(model -> {
            Map<Duration, Integer> rightDeps = model.getRightDependencies();
            rightDeps.forEach((interval, rd) -> {
                if (rightDependencies.containsKey(interval)) {
                    rightDependencies.put(interval, Math.max(rightDependencies.get(interval), rd));
                } else {
                    rightDependencies.put(interval, rd);
                }
            }) ;
        });

    }
    public BacktestResult<T, V> backtest(Ticker ticker, ZonedDateTime anchor) {
        System.out.println("start");
        UpstreamAdapter adapter = UpstreamAdapters.getAdapterFor(ticker);
        HashMap<Duration, TickerState<T>> states = new HashMap<>();
        leftDependencies.forEach((interval, ld) -> states.put(interval, adapter.makeLeftFor(interval, ld).<T>snapshot(anchor).getTickerState(ticker)));
        PredictResult<T, V> predictResult = this.predictFromStates(ticker, states);
        ArrayList<TickerState<T>> rightStates = new ArrayList<>();
        System.out.println("p1");
        this.rightDependencies.forEach((interval, rd) -> rightStates.add(adapter.makeRightFor(interval, rd).<T>verify(anchor).getTickerState(ticker)));
        TimeSeries<T> targets = rightStates.stream()
                .map(TickerState::getPriceData)
                .reduce(
                        new TimeSeries<>(List.of()),
                        (accum, nxt) -> accum.merge(nxt)
                );
        System.out.println("p2");
        return new BacktestResult<>(predictResult, targets);
    }

    public PredictResult<T, V> predict(Ticker ticker) {
        UpstreamAdapter adapter = UpstreamAdapters.getAdapterFor(ticker);
        HashMap<Duration, TickerState<T>> states = new HashMap<>();
        leftDependencies.forEach((interval, ld) -> states.put(interval, adapter.makeLeftFor(interval, ld).<T>update().getTickerState(ticker)));
        return this.predictFromStates(ticker, states);
    }

    private PredictResult<T, V> predictFromStates(Ticker ticker, HashMap<Duration, TickerState<T>> states) {
        Candle<T> latest = states.values().stream()
                .map(TickerState::getLatest)
                .max(Comparator.comparing(Candle::getTime))
                .get();
        HashMap<Duration,TimeSeries<T>> maxFeatures = new HashMap<>();
        states.forEach((interval, state) -> {
            maxFeatures.put(interval, state.getPriceData());
        });
        HashMap<ModelPredictor<T, V>, List<TimeSeries<T>>> features = new HashMap<>();
        predictors
                .forEach(model -> {
                    Map<Duration, Integer> map = model.getLeftDependencies();
                    ArrayList<TimeSeries<T>> fs = new ArrayList<>();
                    map.forEach((interval, ld) -> {
                        TimeSeries<T> maxF = maxFeatures.get(interval);
//                        fs.add(maxF);
                        System.out.println("q0");
                        System.out.println(ld);
                        System.out.println(ld);
                        System.out.println(maxF.slice(maxF.size() - ld, maxF.size()).size());
                        fs.add(maxF.slice(maxF.size() - ld, maxF.size()));
                    });
                    features.put(model, fs);
                });
        HashMap<ModelPredictor<T, V>, List<TimeSeries<V>>> predictions = new HashMap<>();
        predictors.forEach(model -> {
            List<TimeSeries<V>> prediction = model.predict(features.get(model), latest);
            predictions.put(model, prediction);
        });
        TimeSeries<T> resF = maxFeatures.values().stream()
                .reduce(
                        new TimeSeries<>(List.of()),
                        (accum, nxt) -> accum.merge(nxt)
                );
        Map<ModelPredictor<T, V>, TimeSeries<V>> resP = new HashMap<>();
        predictions.forEach((model, preds) -> {
            TimeSeries<V> resPred = preds.stream()
                    .reduce(
                            new TimeSeries<>(List.of()),
                            (accum, nxt) -> accum.merge(nxt)
                    );
            resP.put(model, resPred);
        });
        return new PredictResult<>(ticker, resF, resP);
    }

    public CompletableFuture<PredictResult<T, V>> predictAsync(Ticker ticker) {
        return CompletableFuture.supplyAsync(() -> this.predict(ticker));
    }

    public ScheduledExecutorService predictLoop(List<Ticker> tickers, Duration interval , Consumer<? super List<? super PredictResult<T, V>>> callback) {
        ScheduledExecutorService clock = Executors.newSingleThreadScheduledExecutor();
        clock.scheduleWithFixedDelay(() -> {
            List<CompletableFuture<PredictResult<T, V>>> predCF =  tickers.stream()
                    .map(this::predictAsync)
                    .collect(Collectors.toList());
            CompletableFuture.allOf(predCF.toArray(new CompletableFuture<?>[]{})).thenRunAsync(() -> {
                callback.accept(predCF.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            });
        },0, interval.toMillis(), TimeUnit.MILLISECONDS);
        return clock;
    }

    public CompletableFuture<BacktestResult<T, V>> backTestAsync(Ticker ticker, ZonedDateTime anchor) {
        return CompletableFuture.supplyAsync(() -> this.backtest(ticker, anchor));
    }
    public static class PredictResult<T extends Number, V extends Number> {
        private Ticker ticker;
        private TimeSeries<T> features;
        private Map<ModelPredictor<T, V>, TimeSeries<V>> predictions;

        public PredictResult(Ticker ticker, TimeSeries<T> features, Map<ModelPredictor<T, V>, TimeSeries<V>> predictions) {
            this.ticker = ticker;
            this.features = features;
            this.predictions = predictions;
        }

        public Map<ModelPredictor<T, V>, TimeSeries<V>> getPrediction() {
            return predictions;
        }

        public Ticker getTicker() {
            return ticker;
        }

        public TimeSeries<T> getFeatures() {
            return features;
        }
    }

    public static class BacktestResult<T extends Number, V extends Number> extends PredictResult<T, V> {
        private TimeSeries<T> targets;

        public BacktestResult(Ticker ticker, TimeSeries<T> features, Map<ModelPredictor<T, V>, TimeSeries<V>> predictions, TimeSeries<T> targets) {
            super(ticker, features, predictions);
            this.targets = targets;
        }

        public BacktestResult(PredictResult<T, V> predictResult ,TimeSeries<T> targets) {
            this(predictResult.ticker, predictResult.features, predictResult.predictions, targets);
        }

        public TimeSeries<T> getTargets() {
            return targets;
        }
    }
}
