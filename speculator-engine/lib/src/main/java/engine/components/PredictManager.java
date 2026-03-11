package engine.components;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import engine.PriceData.State;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;
import engine.PriceData.Upstream;
import engine.Util;

public class PredictManager<T extends Number, V extends Number>{
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
    public static <T extends Number, V extends Number> ScheduledExecutorService predictLoop(
            Supplier<PredictManager<T, V>> getManager,
            Supplier<? extends Collection<Ticker<T>> > getTickers,
            Duration interval,
            Consumer<? super List<PredictResult<T, V>>> callback) {
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
        Map<Duration, Integer> leftDependencies = unionCommon(predictors.stream()
                .map(ModelPredictor::getLeftDependencies)
                .collect(Collectors.toList())
        );

        Map<Duration, Integer> rightDependencies = unionCommon(predictors.stream()
                .map(ModelPredictor::getLeftDependencies)
                .collect(Collectors.toList())
        );
        this.dependencies = Util.Pair.create(leftDependencies, rightDependencies);
    }

    private Map<Upstream<T>, ArrayList<Ticker<T>>> groupByUpstream(Collection<? extends Ticker<T>> tickers) {
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
        ArrayList<Ticker<T>> tickerList = new ArrayList<>(tickers);
        HashMap<Upstream<T>, ArrayList<Ticker<T>>> groups = new HashMap<>();
        while (!tickerList.isEmpty()) {
            Upstream<T> upstream = upstreamPQ.poll();
            ArrayList<Ticker<T>> group = new ArrayList<>();
            for (int i = tickerList.size()-1; i > -1; i--) {
                Ticker<T> ticker = tickerList.get(i);
                if (ticker.canRequestFrom(upstream)) {
                    tickerList.remove(i);
                    group.add(ticker);
                }
            }
            groups.put(upstream, group);
        }
        return groups;
    }
    private Map<Ticker<T>, PredictResult<T,V>> predictFromState(Collection<? extends Ticker<T>> tickers, State<T> state) {
//        System.out.println("PM::predictFS");
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
//                System.out.println("PM::predictFS bug start");
                TimeSeries<V> prediction = model.predict(state.getTickerState(ticker)).stream()
                        .reduce(
                                TimeSeries.empty(),
                                TimeSeries::merge
                        );
                predictions.put(model, prediction);
            }
            results.put(ticker, new PredictResult<>(ticker, features, predictions));
        }
//        System.out.println("PM::predictFS end");
        return results;
    }
    private Collection<PredictResult<T,V>> predictUsingUpstream(Upstream<T> upstream, Collection<Ticker<T>> tickers) {
        State<T> leftState = upstream.update(tickers, this.dependencies.first);
        return this.predictFromState(tickers, leftState).values();
    }

    private List<BacktestResult<T,V>> backtestUsingUpstream(Upstream<T> upstream, Collection<Ticker<T>> tickers, ZonedDateTime anchor) {
//        System.out.println("PM::backtestUsing");
        Util.Pair<State<T>, State<T>> states = upstream.snapshot(tickers, this.dependencies.first, this.dependencies.second, anchor);
//        System.out.println("PM::backtestUsing bug start"); // BUG!
        Map<Ticker<T>, PredictResult<T, V>> predictions = this.predictFromState(tickers, states.first);
//        System.out.println("PM::backtestUsing bug end"); //BUG!
        Map<Ticker<T>, TimeSeries<T>> targets = tickers.stream().collect(Collectors.toMap(
                ticker -> ticker,
                ticker -> {
                    TickerState<T> tickerState = states.second.getTickerState(ticker);
                    return dependencies.second.keySet().stream()
                            .map(tickerState::getPriceData)
                            .reduce(
                                    TimeSeries.empty(),
                                    TimeSeries::merge
                            );
                }
        ));
//        System.out.println("PM::backtestUsing end");
        return tickers.stream()
                .map(ticker -> new BacktestResult<>(predictions.get(ticker), targets.get(ticker)))
                .collect(Collectors.toList());
    }

    public List<BacktestResult<T,V>> backtest(Collection<? extends Ticker<T>> tickers, ZonedDateTime anchor) {
//        System.out.println("PM::backtest");
        List<BacktestResult<T, V>> results = null;
        try {
            Map<Upstream<T>, ArrayList<Ticker<T>>> groups = groupByUpstream(tickers);
            results = new ArrayList<>();
            for (Upstream<T> upstream : groups.keySet()) {
    //            System.out.println("PM::backtest bug start");
                results.addAll(backtestUsingUpstream(upstream, groups.get(upstream), anchor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("PM::backtest end");
        return results;
    }


    public List<PredictResult<T, V>> predict(Collection<? extends Ticker<T>> tickers) {
        Map<Upstream<T>, ArrayList<Ticker<T>>> groups = groupByUpstream(tickers);
        List<PredictResult<T, V>> results = new ArrayList<>();
        for (Upstream<T> upstream : groups.keySet()) {
            results.addAll(predictUsingUpstream(upstream, groups.get(upstream)));
        }
        return results;
    }

    public CompletableFuture<List<PredictResult<T, V>>> predictAsync(Collection<? extends Ticker<T>> tickers) {
        return CompletableFuture.supplyAsync(() -> this.predict(tickers));
    }

    public CompletableFuture<List<BacktestResult<T, V>>> backTestAsync(Collection<? extends Ticker<T>> tickers, ZonedDateTime anchor) {
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
