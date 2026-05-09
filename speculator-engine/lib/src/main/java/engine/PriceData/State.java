package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import engine.Util;
import engine.components.Ticker;

public class State {

    HashMap<Ticker, TickerState> tickerData;
    Set<Ticker> mask;

    public State(
            HashMap<Ticker, TickerState> tickerData
    ) {
        this(tickerData, tickerData.keySet());
    }

    public State(
            HashMap<Ticker, TickerState> tickerData,
            Collection<Ticker> mask
    ) {
        // constructor for delta
        this.tickerData = tickerData;
        this.mask = mask.stream().filter(tickerData::containsKey).collect(Collectors.toSet());
    }

    public State() {
        this(new HashMap<>());
    }
    public ZonedDateTime getAnchor(){
        return mask.stream()
                .map(this::getTickerState)
                .map(TickerState::getAbsoluteLatest)
                .map(Candle::getTime)
                .min(ZonedDateTime::compareTo)
                .orElse(null);
    }
    public TickerState getTickerState(Ticker ticker) {
//        System.out.println("State::getTS");
//        System.out.println(this.tickerData.get(ticker));
        return Optional.of(ticker)
                .filter(mask::contains)
                .map(tickerData::get)
                .map(TickerState::asView)
                .orElse(null);
    }

    public Set<Ticker> getTickers() {
        return mask;
    }

    public State save() {
        // read-only deep copy
        HashMap<Ticker, TickerState> copyData = new HashMap<>();
        tickerData.keySet().forEach(ticker -> copyData.put(ticker, tickerData.get(ticker).save()));
        return new State(copyData, new HashSet<>(mask));
    }

    public State mergeWith(State s2) {
        this.tickerData.putAll(s2.tickerData);
        this.mask.addAll(s2.mask);
        return this;
    }

    public static class MutableState<V extends Number> extends State {
        Hashtable<Ticker, Integer> nonHits;
        HashSet<Ticker> hits;
        int nonHitsBeforeClear; // also applies until tickerState
        public MutableState(int nonHitsBeforeClear) {
            // start out empty
            super(new HashMap<>());
            this.nonHits = new Hashtable<>();
            this.hits = new HashSet<>();
            this.nonHitsBeforeClear = nonHitsBeforeClear;
        }

        public MutableState() {
            this(3);
        }

        public State asView() {
            // makes shallow copy view (for abstraction)
            return new State(this.tickerData);
        }

        public State asView(Collection<Ticker> mask) {
            return new State(this.tickerData, mask);
        }

        public int pointsNotAfter(Ticker ticker, Duration interval, ZonedDateTime at) {
            return this.tickerData.get(ticker).asMutable().pointsNotAfter(interval, at);
        }

        public void extendLeft(Ticker ticker, Duration interval, TimeSeries deltaLeft) {
            this.tickerData.get(ticker).asMutable().extendLeft(interval, deltaLeft);
            this.markHit(ticker, interval);
        }

        public void extendRight(Ticker ticker, Duration interval, TimeSeries deltaRight) {
            this.tickerData.get(ticker).asMutable().extendRight(interval, deltaRight);
            this.markHit(ticker, interval);

        }

        public void dropLeft(Ticker ticker, Duration interval, int count) {
            TickerState tickerState = this.tickerData.get(ticker);
            tickerState.asMutable().dropLeft(interval, count);
            if (tickerState.isEmpty()) {
                this.tickerData.remove(ticker);
            } else {
                this.markHit(ticker, interval);
            }
        }

        public void dropAfter(Ticker ticker, Duration interval, ZonedDateTime after) {
            TickerState tickerState = this.tickerData.get(ticker);
            tickerState.asMutable().dropAfter(interval, after);
            if (tickerState.isEmpty()) {
                this.tickerData.remove(ticker);
            } else {
                this.markHit(ticker, interval);
            }
        }

        public void put(Ticker ticker, Duration interval, TimeSeries timeSeries) {
            if (timeSeries.isEmpty()) {
                return;
            }
            if (!this.tickerData.containsKey(ticker)) {
                this.tickerData.put(ticker, new TickerState.MutableTickerState(nonHitsBeforeClear));
            }
            this.tickerData.get(ticker).put(interval, timeSeries);
            this.markHit(ticker, interval);
        }

        public boolean contains(Ticker ticker, Duration interval) {
            return this.tickerData.containsKey(ticker) && this.tickerData.get(ticker).contains(interval);
        }

        public ZonedDateTime from(Ticker ticker, Duration interval) {
            return this.tickerData.get(ticker).from(interval);
        }

        public ZonedDateTime until(Ticker ticker, Duration interval) {
            return this.tickerData.get(ticker).until(interval);
        }

        public void markHit(Ticker ticker, Duration interval) {
            if (this.tickerData.containsKey(ticker)) {
                this.hits.add(ticker);
                this.tickerData.get(ticker).asMutable().markHit(interval);
            }
        }

        @Override
        public TickerState getTickerState(Ticker ticker) {
            this.hits.add(ticker);
            return super.getTickerState(ticker);
        }

        public void cleanUp() {
            Hashtable<Ticker, Integer> nonHits = new Hashtable<>();
            for (Ticker ticker : this.tickerData.keySet()) {
                if (this.hits.contains(ticker)) {
                    nonHits.put(ticker, this.nonHitsBeforeClear);
                    TickerState.MutableTickerState ts = this.tickerData.get(ticker).asMutable();
                    ts.cleanUp();
                    if (ts.isEmpty()) {
                        this.tickerData.remove(ticker);
                    }
                } else {
                    int nh = this.nonHits.get(ticker);
                    if (nh == 0) {
                        this.tickerData.remove(ticker);
                    } else {
                        nonHits.put(ticker, nh-1);
                    }
                }
            }
            this.nonHits = nonHits;
            this.hits = new HashSet<>();
        }

        public Util.Pair<State, State> partition(ZonedDateTime at) {
            HashMap<Ticker, TickerState> leftMap = new HashMap<>();
            HashMap<Ticker, TickerState> rightMap = new HashMap<>();
            for (Ticker ticker : this.tickerData.keySet()) {
                Util.Pair<TickerState, TickerState> pair = this.tickerData.get(ticker).asMutable().partition(at);
                leftMap.put(ticker, pair.first);
                rightMap.put(ticker, pair.second);
            }
            return Util.Pair.create(new State(leftMap), new State(rightMap));
        }
    }

}
