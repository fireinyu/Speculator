package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import engine.Util.Pair;
import engine.components.Ticker;

public abstract class Upstream<V extends Number> {

    // ticker -> interval
    private State.MutableState<V> cache;

    public Upstream(int cacheNonHitsBeforeClear) {
        this.cache = new State.MutableState<>();
    }

    public Upstream() {
        this(4);
    }

    public State<V> update(
            Collection<Ticker<V>> tickers,
            Map<Duration, Integer> leftDependencies
    ) {
        return this.snapshot(tickers, leftDependencies, Map.of(), ZonedDateTime.now()).first;
    }
    public Pair<State<V>, State<V>> snapshot(
            Collection<Ticker<V>> tickers,
            Map<Duration, Integer> leftDependencies,
            Map<Duration, Integer> rightDependencies,
            ZonedDateTime at
    ) {
        try {
            Map<Duration, Pair<Integer, Duration>> dependencies = new HashMap<>(); //overall dependency
            // combine left and right dependencies
            Collection<Duration> intervals = new HashSet<>();
            intervals.addAll(leftDependencies.keySet());
            intervals.addAll(rightDependencies.keySet());
            for (Duration interval : intervals) {
                int ld = leftDependencies.getOrDefault(interval, 0);
                Duration rd = interval.multipliedBy(rightDependencies.getOrDefault(interval, 0));
                dependencies.put(interval, Pair.create(ld, rd));
            }
            for (Ticker<V> ticker : tickers) { // iterate over tickers
                for (Duration interval : dependencies.keySet()) { // iterate over intervals
                    int ld = dependencies.get(interval).first;
                    Duration rd = dependencies.get(interval).second;
                    ZonedDateTime rightDT = at.plus(rd);
                    Duration minLeftDuration = interval.multipliedBy(ld - 1); // based on minimum left duration
                    if (!cache.contains(ticker, interval)) { // not in cache {9}
                        // basic fetch left by count
                        TimeSeries<V> deltaLeft = this.fetchCountUntil(ticker, interval, ld, at);
                        // basic fetch right by duration
                        TimeSeries<V> deltaRight = this.fetchBetween(ticker, interval, at, rightDT);
                        // combine by right extend
                        TimeSeries<V> delta = deltaLeft.extendRight(deltaRight);
                        // put cache entry
                        cache.put(ticker, interval, delta);
                        continue;
                    }
                    cache.markHit(ticker, interval);
                    ZonedDateTime cacheFirst = cache.from(ticker, interval);
                    ZonedDateTime cacheLast = cache.until(ticker, interval);
                    if (at.plus(rd).isAfter(cacheFirst)) { // rightmost is after cache last {2,3,7,8}
                        if (at.minus(minLeftDuration).isAfter(cacheLast)){// no guaranteed overlap {3}
                            // basic fetch left by count
                            TimeSeries<V> deltaLeft = this.fetchCountUntil(ticker, interval, ld, at);
                            // basic fetch right by duration
                            TimeSeries<V> deltaRight = this.fetchBetween(ticker, interval, at, rightDT);
                            // combine by right extend
                            TimeSeries<V> delta = deltaLeft.extendRight(deltaRight);
                            // replace cache entry
                            cache.put(ticker, interval, delta);
                        }
                        else{ // guaranteed overlap {2, 7, 8}
                            // fetch by duration from cache last until rightmost
                            TimeSeries<V> deltaRight = this.fetchBetween(ticker, interval, cacheLast, rightDT);
                            // right extend cache
                            cache.extendRight(ticker, interval, deltaRight);
                            // find number of cached candles not after at
                            int numCached = cache.pointsNotAfter(ticker, interval, at);
                            if (numCached < ld) { // too few cached candles {2}
                                // fetch by count ending at cache first
                                TimeSeries<V> deltaLeft = this.fetchCountUntil(ticker, interval, ld-numCached, cacheFirst.minus(interval));
                                // left extend cache
                                cache.extendLeft(ticker, interval, deltaLeft);
                            } else { // at > cache last or <= cache last {7, 8}
                                // left slice cache by index
                                cache.dropLeft(ticker, interval, numCached-ld);
                            }
                        }
                    } else { // rightmost is before cache last {1, 4, 5, 6}
                        if (rightDT.isBefore(cacheFirst)) { // rightmost is before cache first {1}
                            // basic fetch left by count
                            TimeSeries<V> deltaLeft = this.fetchCountUntil(ticker, interval, ld, at);
                            // basic fetch right by duration
                            TimeSeries<V> deltaRight = this.fetchBetween(ticker, interval, at, rightDT);
                            // combine by right extend
                            TimeSeries<V> delta = deltaLeft.extendRight(deltaRight);
                            // replace cache entry
                            cache.put(ticker, interval, delta);
                        } else { // guaranteed overlap {4, 5, 6}
                            if (at.isBefore(cacheFirst)) { // at is before cache first {5}
                                // fetch left by count
                                TimeSeries<V> deltaLeft = this.fetchCountUntil(ticker, interval, ld, at);
                                // fetch right by missing duration
                                TimeSeries<V> deltaRight = this.fetchBetween(ticker, interval, at, cacheFirst);
                                // right extend delta
                                TimeSeries<V> delta = deltaLeft.extendRight(deltaRight);
                                // left extend cache
                                cache.extendLeft(ticker, interval, delta);
                                // right slice cache by DateTime
                                cache.dropAfter(ticker, interval, rightDT);
                            } else { // at is >= cache first {4, 6}
                                // find number of cached candles not after at
                                int numCached = cache.pointsNotAfter(ticker, interval, at);
                                if (numCached < ld) { // too few cached candles {4}
                                    // fetch left by missing count
                                    TimeSeries<V> delta = this.fetchCountUntil(ticker, interval, ld-numCached, cacheFirst.minus(interval));
                                    // left extend cache
                                    cache.extendLeft(ticker, interval, delta);
                                    // right slice cache by DateTime
                                    cache.dropAfter(ticker, interval, rightDT);
                                } else { // completely enclosed by cached candles {6}
                                    // do nothing
                                }
                            }
                        }

                    }

                }
            }
            // remove nonHits
            // partition and return state
            cache.cleanUp();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return cache.partition(at);
    }

    public TimeSeries<V> fetchCountUntil(Ticker<V> ticker, Duration interval, int leftDependency, ZonedDateTime until) {
        if (leftDependency == 0) {
            return TimeSeries.empty();
        }
        TimeSeries<V> big = this.fetchCountUntilAtLeast(ticker, interval, leftDependency, until);
        TimeSeries<V> res = big.slice(big.size() - leftDependency, big.size());
        res.original = true;
        return res;
    }

    public TimeSeries<V> fetchBetween(Ticker<V> ticker, Duration interval, ZonedDateTime from, ZonedDateTime to) {
        if (!to.isAfter(from)) {
            return TimeSeries.empty();
        }
        TimeSeries<V> big = this.fetchBetweenAtLeast(ticker, interval, from, to);
        TimeSeries<V> res = big.slice(big.pointsNotAfter(from), big.pointsNotAfter(to));
        res.original = true;
        return res;
    }


    public abstract HashMap<Ticker<V>, Position<V>> fetchPositionsNow(Set<Ticker<V>> tickers);
    protected abstract TimeSeries<V> fetchCountUntilAtLeast(Ticker<V> ticker, Duration interval, int leftDependency, ZonedDateTime until);
    // include until
    // at least leftDependency
    protected abstract TimeSeries<V> fetchBetweenAtLeast(Ticker<V> ticker, Duration interval ,ZonedDateTime from, ZonedDateTime to);
    // fetch minimal range: exclude from, include until


}
