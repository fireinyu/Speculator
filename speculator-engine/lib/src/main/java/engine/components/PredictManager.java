package engine.components;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import engine.PriceData.Candle;
import engine.PriceData.State;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;
import engine.Util;

public class PredictManager<T extends Number, V extends Number>{
    public static <T extends Number, V extends Number> ScheduledExecutorService predictLoop(
            Supplier<PredictManager<T, V>> getManager,
            Supplier<? extends Collection<Ticker<T>> > getTickers,
            Duration interval,
            Consumer<? super Set<PredictResult<T, V>>> callback) {
        ScheduledExecutorService clock = Executors.newScheduledThreadPool(Thread.activeCount());
        clock.scheduleWithFixedDelay(() -> {
//            System.out.println("debug pm: start");
            callback.accept(getManager.get().predict(getTickers.get()));
        },0, interval.toMillis(), TimeUnit.MILLISECONDS);
        return clock;
    }
    private List<ModelPredictor<T, V>> predictors;

    private Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> dependencies;

    public PredictManager(List<ModelPredictor<T, V>> predictors) {
        this.predictors = predictors;
        Map<Duration, Integer> leftDependencies = UpstreamRequest.unionCommon(predictors.stream()
                .map(ModelPredictor::getLeftDependencies)
                .collect(Collectors.toSet())
        );

        Map<Duration, Integer> rightDependencies = UpstreamRequest.unionCommon(predictors.stream()
                .map(ModelPredictor::getLeftDependencies)
                .collect(Collectors.toSet())
        );
        this.dependencies = Util.Pair.create(leftDependencies, rightDependencies);
    }

    private Map<Upstream<T>, HashSet<Ticker<T>>> groupByUpstream(Collection<? extends Ticker<T>> tickers) {
        HashMap<Upstream<T>, Util.Pair<Integer, Integer>> upstreams = new HashMap<>();
        for (Ticker<T> ticker : tickers) {
            List<Upstream<T>> tickerUpstreams = ticker.preferredUpstreams();
            for (int i = 0; i < tickerUpstreams.size() ; i++) {
                Upstream<T> upstream = tickerUpstreams.get(i);
                if (upstreams.containsKey(upstream)) {
                    upstreams.put(upstream, Util.Pair.create(upstreams.get(upstream).first + 1, upstreams.get(upstream).second - i));
                } else {
                    upstreams.put(upstream, Util.Pair.create(1, -i));
                }
            }
        }
        PriorityQueue<Upstream<T>> upstreamPQ = new PriorityQueue<>(Comparator
                .comparing(up -> upstreams.get(up).first)
                .thenComparing(up -> upstreams.get(up).second)
        );
        upstreamPQ.addAll(upstreams.keySet());
        HashSet<Ticker<T>> tickerSet = new HashSet<>(tickers);
        HashMap<Upstream<T>, HashSet<Ticker<T>>> groups = new HashMap<>();
        while (!tickerSet.isEmpty()) {
            Upstream<T> upstream = upstreamPQ.poll();
            HashSet<Ticker<T>> group = new HashSet<>();
            for (Ticker<T> ticker : tickerSet) {
                if (ticker.canRequestFrom(upstream)) {
                    tickerSet.remove(ticker);
                    group.add(ticker);
                }
            }
            groups.put(upstream, group);
        }
        return groups;
    }
    private Map<Ticker<T>, PredictResult<T,V>> predictFromState(Collection<? extends Ticker<T>> tickers, State<T> state) {
        Map<Ticker<T>, PredictResult<T,V>> results =  new HashMap<>();
        for (Ticker<T> ticker : tickers) {
            TickerState<T> tickerState = state.getTickerState(ticker);
            TimeSeries<T> features = dependencies.first.keySet().stream()
                    .map(tickerState::getPriceData)
                    .reduce(
                            TimeSeries.empty(),
                            TimeSeries::merge
                    );
            Map<ModelPredictor<T, V>, TimeSeries<V>> predictions = new HashMap<>();
            for (ModelPredictor<T, V> model : this.predictors) {
                TimeSeries<V> prediction = model.predict(state.getTickerState(ticker)).stream()
                        .reduce(
                                TimeSeries.empty(),
                                TimeSeries::merge
                        );
                predictions.put(model, prediction);
            }
            results.put(ticker, new PredictResult<>(ticker, features, predictions));
        }
        return results;
    }
    private Collection<PredictResult<T,V>> predictUsingUpstream(Upstream<T> upstream, Collection<Ticker<T>> tickers) {
        UpstreamRequest<T> request = new UpstreamRequest<>(tickers, this.dependencies);
        State<T> leftState = upstream.update(request);
        return this.predictFromState(tickers, leftState).values();
    }

    private Set<BacktestResult<T,V>> backtestUsingUpstream(Upstream<T> upstream, Collection<Ticker<T>> tickers, ZonedDateTime anchor) {
        UpstreamRequest<T> request = new UpstreamRequest<>(tickers, this.dependencies);
        State<T> leftState = upstream.snapshot(anchor, request);
        Map<Ticker<T>, PredictResult<T, V>> predictions = this.predictFromState(tickers, leftState);
        State<T> rightState = upstream.verify(anchor, request);
        Map<Ticker<T>, TimeSeries<T>> targets = tickers.stream().collect(Collectors.toMap(
                ticker -> ticker,
                ticker -> {
                    TickerState<T> tickerState = rightState.getTickerState(ticker);
                    return dependencies.second.keySet().stream()
                            .map(tickerState::getPriceData)
                            .reduce(
                                    TimeSeries.empty(),
                                    TimeSeries::merge
                            );
                }
        ));
        return tickers.stream()
                .map(ticker -> new BacktestResult<>(predictions.get(ticker), targets.get(ticker)))
                .collect(Collectors.toSet());
    }

    public Set<BacktestResult<T,V>> backtest(Collection<? extends Ticker<T>> tickers, ZonedDateTime anchor) {
//        System.out.println("start");
        Map<Upstream<T>, HashSet<Ticker<T>>> groups = groupByUpstream(tickers);
        Set<BacktestResult<T, V>> results = new HashSet<>();
        for (Upstream<T> upstream : groups.keySet()) {
            results.addAll(backtestUsingUpstream(upstream, groups.get(upstream), anchor));
        }
        return results;
    }


    public Set<PredictResult<T, V>> predict(Collection<? extends Ticker<T>> tickers) {
        Map<Upstream<T>, HashSet<Ticker<T>>> groups = groupByUpstream(tickers);
        Set<PredictResult<T, V>> results = new HashSet<>();
        for (Upstream<T> upstream : groups.keySet()) {
            results.addAll(predictUsingUpstream(upstream, groups.get(upstream)));
        }
        return results;
    }

    public CompletableFuture<Set<PredictResult<T, V>>> predictAsync(Collection<? extends Ticker<T>> tickers) {
        return CompletableFuture.supplyAsync(() -> this.predict(tickers));
    }

    public CompletableFuture<Set<BacktestResult<T, V>>> backTestAsync(Collection<? extends Ticker<T>> tickers, ZonedDateTime anchor) {
        return CompletableFuture.supplyAsync(() -> this.backtest(tickers, anchor));
    }
    public static class PredictResult<T extends Number, V extends Number> {
        private Ticker<T> ticker;
        private TimeSeries<T> features;
        private Map<ModelPredictor<T, V>, TimeSeries<V>> predictions;

        public PredictResult(Ticker<T> ticker, TimeSeries<T> features, Map<ModelPredictor<T, V>, TimeSeries<V>> predictions) {
            this.ticker = ticker;
            this.features = features;
            this.predictions = predictions;
        }

        public Map<ModelPredictor<T, V>, TimeSeries<V>> getPrediction() {
            return predictions;
        }

        public Ticker<T> getTicker() {
            return ticker;
        }

        public TimeSeries<T> getFeatures() {
            return features;
        }
    }

    public static class BacktestResult<T extends Number, V extends Number> extends PredictResult<T, V> {
        private TimeSeries<T> targets;

        public BacktestResult(Ticker<T> ticker, TimeSeries<T> features, Map<ModelPredictor<T, V>, TimeSeries<V>> predictions, TimeSeries<T> targets) {
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
