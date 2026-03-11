package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Set;

import engine.Util;
import engine.components.Ticker;

public class State<V extends Number> {

    HashMap<Ticker<V>, TickerState<V>> tickerData;

    public State(
            HashMap<Ticker<V>, TickerState<V>> tickerData
    ) {
        // constructor for delta
        this.tickerData = tickerData;
    }

    public TickerState<V> getTickerState(Ticker<V> ticker) {
//        System.out.println("State::getTS");
//        System.out.println(this.tickerData.get(ticker));
        return Optional.ofNullable(this.tickerData.get(ticker))
                .map(TickerState::asView)
                .orElse(null);
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

    public static class MutableState<V extends Number> extends State<V> {
        Hashtable<Ticker<V>, Integer> nonHits;
        HashSet<Ticker<V>> hits;
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

        public State<V> asView() {
            // makes shallow copy view (for abstraction)
            return new State<>(this.tickerData);
        }

        public int pointsNotAfter(Ticker<V> ticker, Duration interval, ZonedDateTime at) {
            return this.tickerData.get(ticker).asMutable().pointsNotAfter(interval, at);
        }

        public void extendLeft(Ticker<V> ticker, Duration interval, TimeSeries<V> deltaLeft) {
            this.tickerData.get(ticker).asMutable().extendLeft(interval, deltaLeft);
            this.markHit(ticker, interval);
        }

        public void extendRight(Ticker<V> ticker, Duration interval, TimeSeries<V> deltaRight) {
            this.tickerData.get(ticker).asMutable().extendRight(interval, deltaRight);
            this.markHit(ticker, interval);

        }

        public void dropLeft(Ticker<V> ticker, Duration interval, int count) {
            TickerState<V> tickerState = this.tickerData.get(ticker);
            tickerState.asMutable().dropLeft(interval, count);
            if (tickerState.isEmpty()) {
                this.tickerData.remove(ticker);
            } else {
                this.markHit(ticker, interval);
            }
        }

        public void dropAfter(Ticker<V> ticker, Duration interval, ZonedDateTime after) {
            TickerState<V> tickerState = this.tickerData.get(ticker);
            tickerState.asMutable().dropAfter(interval, after);
            if (tickerState.isEmpty()) {
                this.tickerData.remove(ticker);
            } else {
                this.markHit(ticker, interval);
            }
        }

        public void put(Ticker<V> ticker, Duration interval, TimeSeries<V> timeSeries) {
            if (timeSeries.isEmpty()) {
                return;
            }
            if (!this.tickerData.containsKey(ticker)) {
                this.tickerData.put(ticker, new TickerState.MutableTickerState<>(nonHitsBeforeClear));
            }
            this.tickerData.get(ticker).put(interval, timeSeries);
            this.markHit(ticker, interval);
        }

        public boolean contains(Ticker<V> ticker, Duration interval) {
            return this.tickerData.containsKey(ticker) && this.tickerData.get(ticker).contains(interval);
        }

        public ZonedDateTime from(Ticker<V> ticker, Duration interval) {
            return this.tickerData.get(ticker).from(interval);
        }

        public ZonedDateTime until(Ticker<V> ticker, Duration interval) {
            return this.tickerData.get(ticker).until(interval);
        }

        public void markHit(Ticker<V> ticker, Duration interval) {
            if (this.tickerData.containsKey(ticker)) {
                this.hits.add(ticker);
                this.tickerData.get(ticker).asMutable().markHit(interval);
            }
        }

        @Override
        public TickerState<V> getTickerState(Ticker<V> ticker) {
            this.hits.add(ticker);
            return super.getTickerState(ticker);
        }

        public void cleanUp() {
            Hashtable<Ticker<V>, Integer> nonHits = new Hashtable<>();
            for (Ticker<V> ticker : this.tickerData.keySet()) {
                if (this.hits.contains(ticker)) {
                    nonHits.put(ticker, this.nonHitsBeforeClear);
                    TickerState.MutableTickerState<V> ts = this.tickerData.get(ticker).asMutable();
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

        public Util.Pair<State<V>, State<V>> partition(ZonedDateTime at) {
            HashMap<Ticker<V>, TickerState<V>> leftMap = new HashMap<>();
            HashMap<Ticker<V>, TickerState<V>> rightMap = new HashMap<>();
            for (Ticker<V> ticker : this.tickerData.keySet()) {
                Util.Pair<TickerState<V>, TickerState<V>> pair = this.tickerData.get(ticker).asMutable().partition(at);
                leftMap.put(ticker, pair.first);
                rightMap.put(ticker, pair.second);
            }
            return Util.Pair.create(new State<>(leftMap), new State<>(rightMap));
        }
    }

}
