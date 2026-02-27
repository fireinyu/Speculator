package engine.components;

import engine.PriceData.Position;
import engine.PriceData.State;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import engine.Util.Pair;

public abstract class Upstream<V extends Number> {

    // ticker -> interval
    private Hashtable<Ticker<V>, Hashtable<Duration, TimeSeries<V>>> lCache;
    private Hashtable<Ticker<V>, Hashtable<Duration, TimeSeries<V>>> rCache;

    public State<V> update(UpstreamRequest<V> request) {
        UpstreamRequest.LeftRequest<V> left = request.getLeft();
        Map<Ticker<V>, TickerState<V>> stateMap = new HashMap<>();
        Map<Ticker<V>, Position<V>> positionMap = this.fetchPositionsNow(request.getTickers());
        left.getTickers().forEach(ticker -> {
            Map<Duration, TimeSeries<V>> priceData = new HashMap<>();
            left.getIntervals(ticker).forEach(interval -> {
                priceData.put(interval, this.fetchPricesNow(ticker, interval, left.getLD(ticker, interval)));
            });
            stateMap.put(ticker, TickerState.of(priceData, positionMap.get(ticker)));
        });
        return State.of(stateMap);
    }

    public State<V> snapshot(ZonedDateTime at, UpstreamRequest<V> request) {
        UpstreamRequest.LeftRequest<V> left = request.getLeft();
        Map<Ticker<V>, TickerState<V>> stateMap = new HashMap<>();
        Map<Ticker<V>, Position<V>> positionMap = this.fetchPositionsAt(request.getTickers(), at);
        left.getTickers().forEach(ticker -> {
            Map<Duration, TimeSeries<V>> priceData = new HashMap<>();
            left.getIntervals(ticker).forEach(interval -> {
                priceData.put(interval, this.fetchPricesLeft(ticker, interval, left.getLD(ticker, interval), at));
            });
            stateMap.put(ticker, TickerState.of(priceData, positionMap.get(ticker)));
        });
        return State.of(stateMap);
    }

    public State<V> verify(ZonedDateTime from, UpstreamRequest<V> request) {
        UpstreamRequest.RightRequest<V> right = request.getRight();
        Map<Ticker<V>, TickerState<V>> stateMap = new HashMap<>();
        Map<Ticker<V>, Position<V>> positionMap = this.fetchPositionsAt(request.getTickers(), from);
        right.getTickers().forEach(ticker -> {
            Map<Duration, TimeSeries<V>> priceData = new HashMap<>();
            right.getIntervals(ticker).forEach(interval -> {
                priceData.put(interval, this.fetchPricesRight(ticker, interval, right.getRD(ticker, interval), from));
            });
            stateMap.put(ticker, TickerState.of(priceData, positionMap.get(ticker)));
        });
        return State.of(stateMap);
    }

    public abstract Map<Ticker<V>, Position<V>> fetchPositionsNow(Set<Ticker<V>> tickers);
    public abstract Map<Ticker<V>, Position<V>> fetchPositionsAt(Set<Ticker<V>> tickers, ZonedDateTime at);
    public abstract TimeSeries<V> fetchPricesNow(Ticker<V> ticker, Duration interval, int leftDependency);
    // include latest / now
    public abstract TimeSeries<V> fetchPricesLeft(Ticker<V> ticker, Duration interval, int leftDependency, ZonedDateTime at);
    // include latest / now
    public abstract TimeSeries<V> fetchPricesRight(Ticker<V> ticker, Duration interval, int rightDependency, ZonedDateTime at);
    // exclude latest / now
    //    public <V extends Number> CompletableFuture<? extends State<V>> updateAsync () {
//        return CompletableFuture.supplyAsync(this::update);
//    }


}
