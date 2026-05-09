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

//    private Map<Upstream, ArrayList<Ticker>> groupByUpstream(Collection<? extends Ticker> tickers) {
//        HashMap<Upstream, Util.Pair<Integer, Integer>> upstreams = new HashMap<>();
//        for (Ticker ticker : tickers) {
//            List<Upstream> tickerUpstreams = ticker.preferredUpstreams();
//            for (int i = 0; i < tickerUpstreams.size() ; i++) {
//                Upstream upstream = tickerUpstreams.get(i);
//                if (upstreams.containsKey(upstream)) {
//                    upstreams.put(upstream, Util.Pair.create(upstreams.get(upstream).first + 1, upstreams.get(upstream).second - i));
//                } else {
//                    upstreams.put(upstream, Util.Pair.create(1, -i));
//                }
//            }
//        }
//        PriorityQueue<Upstream> upstreamPQ = new PriorityQueue<>(Comparator
//                .comparing(up -> upstreams.get(up).first)
//                .thenComparing(up -> upstreams.get(up).second)
//        );
//        upstreamPQ.addAll(upstreams.keySet());
//        ArrayList<Ticker> tickerList = new ArrayList<>(tickers);
//        HashMap<Upstream, ArrayList<Ticker>> groups = new HashMap<>();
//        while (!tickerList.isEmpty()) {
//            Upstream upstream = upstreamPQ.poll();
//            ArrayList<Ticker> group = new ArrayList<>();
//            for (int i = tickerList.size()-1; i > -1; i--) {
//                Ticker ticker = tickerList.get(i);
//                if (ticker.canRequestFrom(upstream)) {
//                    tickerList.remove(i);
//                    group.add(ticker);
//                }
//            }
//            groups.put(upstream, group);
//        }
//        return groups;
//    }
//    private Map<Ticker, PredictResult> predictFromState(Collection<? extends Ticker> tickers, State state) {
////        System.out.println("PM::predictFS");
//        Map<Ticker, PredictResult> results =  new HashMap<>();
//        for (Ticker ticker : tickers) {
//            TickerState tickerState = state.getTickerState(ticker);
//            TimeSeries features = dependencies.first.keySet().stream()
//                    .map(tickerState::getPriceData)
//                    .reduce(
//                            TimeSeries.empty(),
//                            TimeSeries::merge
//                    );
//            Map<ModelPredictor, TimeSeries> predictions = new HashMap<>();
//            for (ModelPredictor model : this.predictors) {
////                System.out.println("PM::predictFS bug start");
//                TimeSeries prediction = model.predict(state.getTickerState(ticker)).stream()
//                        .reduce(
//                                TimeSeries.empty(),
//                                TimeSeries::merge
//                        );
//                predictions.put(model, prediction);
//            }
//            results.put(ticker, new PredictResult<>(ticker, features, predictions));
//        }
////        System.out.println("PM::predictFS end");
//        return results;
//    }
//    private Collection<PredictResult> predictUsingUpstream(Upstream upstream, Collection<Ticker> tickers) {
//        State leftState = upstream.update(tickers, this.dependencies.first);
//        return this.predictFromState(tickers, leftState).values();
//    }

//    private List<PredictResult> backtestUsingUpstream(Upstream upstream, Collection<Ticker> tickers, ZonedDateTime anchor) {
////        System.out.println("PM::backtestUsing");
//        Util.Pair<State, State> states = upstream.snapshot(tickers, this.dependencies.first, this.dependencies.second, anchor);
////        System.out.println("PM::backtestUsing bug start"); // BUG!
//        Map<Ticker, PredictResult> predictions = this.predictFromState(tickers, states.first);
////        System.out.println("PM::backtestUsing bug end"); //BUG!
//        Map<Ticker, TimeSeries> targets = tickers.stream().collect(Collectors.toMap(
//                ticker -> ticker,
//                ticker -> {
//                    TickerState tickerState = states.second.getTickerState(ticker);
//                    return dependencies.second.keySet().stream()
//                            .map(tickerState::getPriceData)
//                            .reduce(
//                                    TimeSeries.empty(),
//                                    TimeSeries::merge
//                            );
//                }
//        ));
////        System.out.println("PM::backtestUsing end");
//        return tickers.stream()
//                .map(ticker -> new PredictResult<>(predictions.get(ticker), targets.get(ticker)))
//                .collect(Collectors.toList());
//    }

//    public List<PredictResult> backtest(Collection<? extends Ticker> tickers, ZonedDateTime anchor) {
////        System.out.println("PM::backtest");
//        List<PredictResult> results = null;
//        try {
//            Map<Upstream, ArrayList<Ticker>> groups = groupByUpstream(tickers);
//            results = new ArrayList<>();
//            for (Upstream upstream : groups.keySet()) {
//    //            System.out.println("PM::backtest bug start");
//                results.addAll(backtestUsingUpstream(upstream, groups.get(upstream), anchor));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
////        System.out.println("PM::backtest end");
//        return results;
//    }

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
