package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import engine.Util;

public class TickerState {

    HashMap<Duration,TimeSeries> priceData;
    NAVPosition position;

    public TickerState(
            HashMap<Duration, TimeSeries> priceData,
            NAVPosition position
    ) {
        // constructor for delta
        this.priceData = priceData;
        this.position = position;
    }

    public Set<Duration> getIntervals() {
        return this.priceData.keySet();
    }
    public TimeSeries getPriceData() {
        return this.priceData.keySet().stream()
                .parallel()
                .map(this::getPriceData)
                .reduce(
                        TimeSeries.empty(),
                        TimeSeries::merge,
                        TimeSeries::merge
                );
    }

    public TimeSeries getPriceData(Duration interval) {
        return this.priceData.get(interval);
    }
    public Candle getAbsoluteLatest() {
//        System.out.println("TickerState:getAbsLate");
        // absolute latest across all intervals
        return this.getIntervals().stream()
                .map(this::getPriceData)
//                .peek(i -> System.out.println("TickerState:getAbsLate bug start"))
                .filter(ts -> !ts.isEmpty())
                .map(TimeSeries::getLast)
//                .peek(i -> System.out.println("TickerState:getAbsLate bug end"))
                .max(Comparator.comparing(Candle::getTime))
                .get();
    }

    public NAVPosition getPosition() {
        return this.position;
    }

    public TickerState save() {
        // makes deep copy view
        return new TickerState(
                new HashMap<>(this.priceData),
                this.position
        );
    }

    TickerState asView() {
        // makes shallow copy view (for abstraction)
        return this;
    }
    MutableTickerState asMutable() {
        throw new IllegalStateException("not mutable");
    }


    public void put(Duration interval, TimeSeries timeSeries) {
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

    public static class MutableTickerState extends TickerState {
        private Hashtable<Duration, Integer> nonHits; // value = remaining non-hits
        private HashSet<Duration> hits;
        private int nonHitsBeforeClear;

        public MutableTickerState(int nonHitsBeforeClear) {
            // start out empty
            super(new HashMap<>(), NAVPosition.makeEmpty());
            this.nonHits = new Hashtable<>();
            this.hits = new HashSet<>();
            this.nonHitsBeforeClear = nonHitsBeforeClear;
        }
        public int pointsNotAfter(Duration interval, ZonedDateTime at) {
            return this.getPriceData(interval).pointsNotAfter(at);
        }

        public void extendLeft(Duration interval, TimeSeries deltaLeft) {
            this.priceData.put(interval, this.getPriceData(interval).extendLeft(deltaLeft));
        }

        public void extendRight(Duration interval, TimeSeries deltaRight) {
            this.priceData.put(interval, this.getPriceData(interval).extendRight(deltaRight));
        }

        public void dropLeft(Duration interval, int count) {
            if (count == 0) {
                return;
            }
            TimeSeries ts = this.getPriceData(interval);
            TimeSeries newTs = ts.slice(count, ts.size());
            if (newTs.isEmpty()) {
                this.priceData.remove(interval);
            } else {
                newTs.original = true; // original timeseries deemed as derelict
                this.priceData.put(interval, newTs);
            }
        }

        public void dropAfter(Duration interval, ZonedDateTime after) {
            TimeSeries ts = this.getPriceData(interval);
            int keep = ts.pointsNotAfter(after);
            TimeSeries newTs = ts.slice(0, keep);
            if (newTs.isEmpty()) {
                this.priceData.remove(interval);
            } else {
                newTs.original = true; // original timeseries deemed as derelict
                this.priceData.put(interval, newTs);
            }
        }

        public TickerState asView() {
            // makes shallow copy view (for abstraction)
            return new TickerState(
                    this.priceData,
                    this.position
            );
        }

        @Override
        MutableTickerState asMutable() {
            return this;
        }

        void markHit(Duration interval) {
            if (this.priceData.containsKey(interval)) {
                hits.add(interval);
            }
        }

        void cleanUp() {
            Hashtable<Duration, Integer> nonHits = new Hashtable<>();
            for (Duration interval : new HashSet<>(this.priceData.keySet())) {
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

        Util.Pair<TickerState, TickerState> partition(ZonedDateTime at) {
            HashMap<Duration, TimeSeries> leftMap = new HashMap<>();
            HashMap<Duration, TimeSeries> rightMap = new HashMap<>();
            for (Duration interval : this.priceData.keySet()) {
                TimeSeries ts = this.priceData.get(interval);
                int mid = ts.pointsNotAfter(at);
                leftMap.put(interval, ts.slice(0, mid));
                rightMap.put(interval, ts.slice(mid, ts.size()));
            }
            return Util.Pair.create(
                    new TickerState(leftMap, this.position),
                    new TickerState(rightMap, this.position)
            );
        }
    }


}
