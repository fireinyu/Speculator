package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import engine.Util;
import engine.components.Ticker;
import engine.components.UpstreamRequest;

public class State<V extends Number> {

    HashMap<Ticker<V>, TickerState<V>> tickerData;

    public State(
            HashMap<Ticker<V>, TickerState<V>> tickerData
    ) {
        // constructor for delta
        this.tickerData = tickerData;
    }

    public TickerState<V> getTickerState(Ticker<V> ticker) {
        return this.tickerData.get(ticker).asView();
    }

    public Set<Ticker<V>> getTickers() {
        return tickerData.keySet();
    }

    public State<V> save() {
        // read-only deep copy
        HashMap<Ticker<V>, TickerState<V>> copyData = new HashMap<>();
        tickerData.keySet().forEach(ticker -> copyData.put(ticker, tickerData.get(ticker).save()));
        return new State<>(copyData);
    }

    public static abstract class MutableState<V extends Number> extends State<V> {
        Hashtable<Ticker<V>, Integer> nonHits;
        int nonHitsBeforeClear; // also applies to tickerState
        public MutableState(int nonHitsBeforeClear) {
            // start out empty
            super(new HashMap<>());
            this.nonHits = new Hashtable<>();
            this.nonHitsBeforeClear = nonHitsBeforeClear;
        }

        public abstract UpstreamRequest<V> bootstrapRequest(UpstreamRequest<V> request, ZonedDateTime at);

        public State<V> asView() {
            // makes shallow copy view (for abstraction)
            return new State<>(this.tickerData);
        }
    }

    public static class LeftMutableState<V extends Number> extends MutableState<V>{
        public LeftMutableState(int nonHitsBeforeClear) {
            super(nonHitsBeforeClear);
        }

        @Override
        public UpstreamRequest.LeftRequest<V> bootstrapRequest(UpstreamRequest<V> request, ZonedDateTime at) {
            UpstreamRequest.LeftRequest<V> res = request.getLeft();
            res.getTickers().forEach(
                    ticker -> getTickerState(ticker).bootstrapRequestLeft(res ,at, ticker)
            );
            return res;
        }

        public MutableState<V> update(State<V> delta, UpstreamRequest<V> request, ZonedDateTime at) {
            // modifies tickerData and nonHits
            // returns self, for chaining
            UpstreamRequest.LeftRequest<V> leftRequest = request.getLeft();
            for (Ticker<V> ticker: tickerData.keySet()) {
                if (delta.tickerData.containsKey(ticker)) {
                    // update using timeseries::update
                    tickerData.get(ticker).asMutable().updateLeft(
                            delta.tickerData.get(ticker),
                            ticker,
                            leftRequest,
                            at
                    );
                    this.nonHits.put(ticker, nonHitsBeforeClear);
                } else {
                    // decrease nonhitsbeforeclear
                    // this.nonHits.putIfAbsent(ticker, nonHitsBeforeClear); // needed if not starting out empty
                    int nonHit = this.nonHits.get(ticker);
                    if (nonHit == 0) {
                        this.nonHits.remove(ticker);
                        this.tickerData.remove(ticker);
                    } else {
                        this.nonHits.put(ticker, nonHit-1);
                    }
                }
            }
            for (Ticker<V> ticker : delta.tickerData.keySet()) {
                if (!this.nonHits.containsKey(ticker)) {
                    tickerData.put(ticker, new TickerState.MutableTickerState<>(nonHitsBeforeClear));
                    tickerData.get(ticker).asMutable().updateLeft(
                            delta.tickerData.get(ticker),
                            ticker,
                            leftRequest,
                            at
                    );
                    this.nonHits.put(ticker, nonHitsBeforeClear);
                }
            }
            return this;
        }
    }

    public static class RightMutableState<V extends Number> extends MutableState<V> {
        public RightMutableState(int nonHitsBeforeClear) {
            super(nonHitsBeforeClear);
        }

        @Override
        public UpstreamRequest.RightRequest<V> bootstrapRequest(UpstreamRequest<V> request, ZonedDateTime at) {
            UpstreamRequest.RightRequest<V> res = request.getRight();
            res.getTickers().forEach(
                    ticker -> getTickerState(ticker).bootstrapRequestRight(res ,at, ticker)
            );
            return res;
        }

        public MutableState<V> update(State<V> delta, UpstreamRequest<V> request, ZonedDateTime at) {
            // modifies tickerData and nonHits
            // returns self, for chaining
            UpstreamRequest.RightRequest<V> rightRequest = request.getRight();
            for (Ticker<V> ticker: tickerData.keySet()) {
                if (delta.tickerData.containsKey(ticker)) {
                    // update using timeseries::update
                    tickerData.get(ticker).asMutable().updateRight(
                            delta.tickerData.get(ticker),
                            ticker,
                            rightRequest,
                            at
                    );
                    this.nonHits.put(ticker, nonHitsBeforeClear);
                } else {
                    // decrease nonhitsbeforeclear
                    // this.nonHits.putIfAbsent(ticker, nonHitsBeforeClear); // needed if not starting out empty
                    int nonHit = this.nonHits.get(ticker);
                    if (nonHit == 0) {
                        this.nonHits.remove(ticker);
                        this.tickerData.remove(ticker);
                    } else {
                        this.nonHits.put(ticker, nonHit-1);
                    }
                }
            }
            for (Ticker<V> ticker : delta.tickerData.keySet()) {
                if (!this.nonHits.containsKey(ticker)) {
                    tickerData.put(ticker, new TickerState.MutableTickerState<>(nonHitsBeforeClear));
                    tickerData.get(ticker).asMutable().updateRight(
                            delta.tickerData.get(ticker),
                            ticker,
                            rightRequest,
                            at
                    );
                    this.nonHits.put(ticker, nonHitsBeforeClear);
                }
            }
            return this;
        }
    }



}
