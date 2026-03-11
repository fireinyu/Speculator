package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import engine.Util;

public class TickerState <V extends Number> {
    HashMap<Duration,TimeSeries<V>> priceData;
    Position<V> position;

    public TickerState(
            HashMap<Duration, TimeSeries<V>> priceData,
            Position<V> position
    ) {
        // constructor for delta
        this.priceData = priceData;
        this.position = position;
    }

    public Set<Duration> getIntervals() {
        return this.priceData.keySet();
    }

    public TimeSeries<V> getPriceData(Duration interval) {
        return this.priceData.get(interval);
    }
    public Candle<V> getAbsoluteLatest() {
//        System.out.println("TickerState:getAbsLate");
        // absolute latest across all intervals
        return this.getIntervals().stream()
                .map(this::getPriceData)
//                .peek(i -> System.out.println("TickerState:getAbsLate bug start"))
                .map(TimeSeries::getLast)
//                .peek(i -> System.out.println("TickerState:getAbsLate bug end"))
                .max(Comparator.comparing(Candle::getTime))
                .get();
    }

    public Position<V> getPosition() {
        return this.position;
    }

    public TickerState<V> save() {
        // makes deep copy view
        return new TickerState<>(
                new HashMap<>(this.priceData),
                this.position
        );
    }

    TickerState<V> asView() {
        // makes shallow copy view (for abstraction)
        return this;
    }
    MutableTickerState<V> asMutable() {
        throw new IllegalStateException("not mutable");
    }


    public void put(Duration interval, TimeSeries<V> timeSeries) {
        this.priceData.put(interval, timeSeries);
    }

    public boolean contains(Duration interval) {
        return this.priceData.containsKey(interval);
    }

    public ZonedDateTime from(Duration interval) {
        return this.getPriceData(interval).from();
    }

    public ZonedDateTime until(Duration interval) {
        return this.getPriceData(interval).until();
    }
    boolean isEmpty() {
        return this.priceData.isEmpty();
    }

    public static class MutableTickerState <V extends Number> extends TickerState<V> {
        private Hashtable<Duration, Integer> nonHits; // value = remaining non-hits
        private HashSet<Duration> hits;
        private int nonHitsBeforeClear;

        public MutableTickerState(int nonHitsBeforeClear) {
            // start out empty
            super(new HashMap<>(), Position.empty());
            this.nonHits = new Hashtable<>();
            this.hits = new HashSet<>();
            this.nonHitsBeforeClear = nonHitsBeforeClear;
        }
        public int pointsNotAfter(Duration interval, ZonedDateTime at) {
            return this.getPriceData(interval).pointsNotAfter(at);
        }

        public void extendLeft(Duration interval, TimeSeries<V> deltaLeft) {
            this.priceData.put(interval, this.getPriceData(interval).extendLeft(deltaLeft));
        }

        public void extendRight(Duration interval, TimeSeries<V> deltaRight) {
            this.priceData.put(interval, this.getPriceData(interval).extendRight(deltaRight));
        }

        public void dropLeft(Duration interval, int count) {
            if (count == 0) {
                return;
            }
            TimeSeries<V> ts = this.getPriceData(interval);
            TimeSeries<V> newTs = ts.slice(count, ts.size());
            if (newTs.isEmpty()) {
                this.priceData.remove(interval);
            } else {
                newTs.original = true; // original timeseries deemed as derelict
                this.priceData.put(interval, newTs);
            }
        }

        public void dropAfter(Duration interval, ZonedDateTime after) {
            TimeSeries<V> ts = this.getPriceData(interval);
            int keep = ts.pointsNotAfter(after);
            TimeSeries<V> newTs = ts.slice(0, keep);
            if (newTs.isEmpty()) {
                this.priceData.remove(interval);
            } else {
                newTs.original = true; // original timeseries deemed as derelict
                this.priceData.put(interval, newTs);
            }
        }

        public TickerState<V> asView() {
            // makes shallow copy view (for abstraction)
            return new TickerState<>(
                    this.priceData,
                    this.position
            );
        }

        @Override
        MutableTickerState<V> asMutable() {
            return this;
        }

        void markHit(Duration interval) {
            if (this.priceData.containsKey(interval)) {
                hits.add(interval);
            }
        }

        void cleanUp() {
            Hashtable<Duration, Integer> nonHits = new Hashtable<>();
            for (Duration interval : this.priceData.keySet()) {
                if (this.hits.contains(interval)) {
                    nonHits.put(interval, this.nonHitsBeforeClear);
                } else {
                    int nh = this.nonHits.get(interval);
                    if (nh == 0) {
                        this.priceData.remove(interval);
                    } else {
                        nonHits.put(interval, nh-1);
                    }
                }
            }
            this.nonHits = nonHits;
            this.hits = new HashSet<>();
        }

        Util.Pair<TickerState<V>, TickerState<V>> partition(ZonedDateTime at) {
            HashMap<Duration, TimeSeries<V>> leftMap = new HashMap<>();
            HashMap<Duration, TimeSeries<V>> rightMap = new HashMap<>();
            for (Duration interval : this.priceData.keySet()) {
                TimeSeries<V> ts = this.priceData.get(interval);
                int mid = ts.pointsNotAfter(at);
                leftMap.put(interval, ts.slice(0, mid));
                rightMap.put(interval, ts.slice(mid, ts.size()));
            }
            return Util.Pair.create(
                    new TickerState<>(leftMap, this.position),
                    new TickerState<>(rightMap, this.position)
            );
        }
    }


}
