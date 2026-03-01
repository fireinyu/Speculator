package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import engine.components.Ticker;
import engine.components.UpstreamRequest;

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
        // absolute latest across all intervals
        return this.getIntervals().stream()
                .map(this::getPriceData)
                .map(TimeSeries::getLast)
                .max(Comparator.comparing(Candle::getTime))
                .get();
    }
    public Candle<V> getCommonLatest() {
        return null;
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

    UpstreamRequest.LeftRequest<V> bootstrapRequestLeft(UpstreamRequest.LeftRequest<V> request, ZonedDateTime at, Ticker<V> ticker) {
        request.getIntervals(ticker).forEach(interval -> {
            ZonedDateTime cacheLast = priceData.get(interval).until();
            request.bootstrap(ticker, interval, at, cacheLast);
        });
        return request;
    }
    UpstreamRequest.RightRequest<V> bootstrapRequestRight(UpstreamRequest.RightRequest<V> request, ZonedDateTime at, Ticker<V> ticker) {
        priceData.keySet().forEach(interval -> {
            ZonedDateTime cacheLast = priceData.get(interval).until();
            request.bootstrap(ticker, interval, at, cacheLast);
        });
        return request;
    }

    public static class MutableTickerState <V extends Number> extends TickerState<V> {
        private Hashtable<Duration, Integer> nonHits; // value = remaining non-hits
        private int nonHitsBeforeClear;

        public MutableTickerState(int nonHitsBeforeClear) {
            // start out empty
            super(new HashMap<>(), Position.empty());
            this.nonHits = new Hashtable<>();
            this.nonHitsBeforeClear = nonHitsBeforeClear;
        }

        MutableTickerState<V> updateLeft(TickerState<V> delta, Ticker<V> ticker, UpstreamRequest.LeftRequest<V> request, ZonedDateTime at) {
            // modifies priceData and intervals
            // request: the left upstreamrequest that produced delta
            // returns self, for chaining
            for (Duration interval: priceData.keySet()) {
                if (delta.priceData.containsKey(interval)) {
                    // update using timeseries::update
                    priceData.put(interval,
                            priceData.get(interval).updateLength(
                                    delta.priceData.get(interval),
                                    at,
                                    request.getDependency(ticker, interval)
                                    )
                    );
                    this.nonHits.put(interval, nonHitsBeforeClear);
                } else {
                    // decrease nonhitsbeforeclear
                    // this.nonHits.putIfAbsent(interval, nonHitsBeforeClear); // need this if not starting empty
                    int nonHit = this.nonHits.get(interval);
                    if (nonHit == 0) {
                        this.nonHits.remove(interval);
                        this.priceData.remove(interval);
                    } else {
                        this.nonHits.put(interval, nonHit-1);
                    }
                }
            }
            for (Duration interval : delta.priceData.keySet()) {
                if (!this.nonHits.containsKey(interval)) {
                    priceData.put(interval, delta.priceData.get(interval));
                    this.nonHits.put(interval, nonHitsBeforeClear);
                }
            }
            return this;
        }

        MutableTickerState<V> updateRight(TickerState<V> delta, Ticker<V> ticker, UpstreamRequest.RightRequest<V> request, ZonedDateTime at) {
            // modifies priceData and intervals
            // request: the right upstreamrequest that produced delta
            // returns self, for chaining
            for (Duration interval: priceData.keySet()) {
                if (delta.priceData.containsKey(interval)) {
                    // update using timeseries::update
                    priceData.put(
                            interval,
                            priceData.get(interval).updateRange(
                                    delta.priceData.get(interval),
                                    at,
                                    at.plus(interval.multipliedBy(request.getDependency(ticker,interval)))
                            )
                    );
                    this.nonHits.put(interval, nonHitsBeforeClear);
                } else {
                    // decrease nonhitsbeforeclear
                    // this.nonHits.putIfAbsent(interval, nonHitsBeforeClear); // need this if not starting empty
                    int nonHit = this.nonHits.get(interval);
                    if (nonHit == 0) {
                        this.nonHits.remove(interval);
                        this.priceData.remove(interval);
                    } else {
                        this.nonHits.put(interval, nonHit-1);
                    }
                }
            }
            for (Duration interval : delta.priceData.keySet()) {
                if (!this.nonHits.containsKey(interval)) {
                    priceData.put(interval, delta.priceData.get(interval));
                    this.nonHits.put(interval, nonHitsBeforeClear);
                }
            }
            return this;
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

    }


}
