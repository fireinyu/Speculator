package engine.control;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import engine.PriceData.State;
import engine.PriceData.TimeSeries;
import engine.PriceData.Upstream;
import engine.Util;
import engine.components.Ticker;

public class UpstreamManager {
//    private Menu<Ticker> tickers;
//    private Menu<Upstream> upstreams;
//    private Map<Upstream, ArrayList<Ticker>> cachedGroups;

//    public UpstreamManager(Menu<Ticker> tickers, Menu<Upstream> upstreams) {
//        this.tickers = tickers;
//        this.upstreams = upstreams;
//    }

//    public List<Ticker> availableTickers(List<Upstream> upstreams) {
//        Set<Upstream> ref = new HashSet<>(upstreams);
//        return Tickers.list.stream()
//                .filter(tk -> tk.preferredUpstreams().stream().anyMatch(ref::contains))
//                .collect(Collectors.toList());
//    }

    private Map<Upstream, List<Ticker>> groupByUpstream(List<Upstream> upstreams, List<Ticker> tickers, ZonedDateTime at) {
        HashMap<Upstream, Util.Pair<Integer, Integer>> grouping = new HashMap<>();
        List<Ticker> groupedTickers = new ArrayList<>();
        Set<Upstream> selectedUpstreams = new HashSet<>(upstreams);
        for (Ticker ticker : tickers) {
            List<Upstream> tickerUpstreams = ticker.preferredUpstreams(at)
                    .stream()
                    .filter(selectedUpstreams::contains)
                    .collect(Collectors.toList());
            if (!tickerUpstreams.isEmpty()) {
                groupedTickers.add(ticker);
            }
            for (int i = 0; i < tickerUpstreams.size() ; i++) {
                Upstream upstream = tickerUpstreams.get(i);
                if (grouping.containsKey(upstream)) {
                    grouping.put(upstream, Util.Pair.create(grouping.get(upstream).first - 1, grouping.get(upstream).second + i));
                } else {
                    grouping.put(upstream, Util.Pair.create(-1, i));
                }
            }
        }
        PriorityQueue<Upstream> upstreamPQ = new PriorityQueue<>(Comparator
                .comparing(up -> grouping.get(up).first)
                .thenComparing(up -> grouping.get(up).second)
        );
        upstreamPQ.addAll(grouping.keySet());
        HashMap<Upstream, List<Ticker>> groups = new HashMap<>();
        while (!groupedTickers.isEmpty()) {
            Upstream upstream = upstreamPQ.poll();
            List<Ticker> group = new ArrayList<>();
            for (int i = groupedTickers.size()-1; i > -1; i--) {
                Ticker ticker = groupedTickers.get(i);
                if (ticker.canRequestFrom(upstream)) {
                    groupedTickers.remove(i);
                    group.add(ticker);
                }
            }
            groups.put(upstream, group);
        }
        return groups;
    }

    public State update(Map<Duration, Integer> ld, List<Upstream> upstreams, List<Ticker> tickers) {
        Map<Upstream, List<Ticker>> groups = this.groupByUpstream(upstreams, tickers, ZonedDateTime.now());
        return groups.keySet().stream()
                .parallel()
                .map(up -> update(up, groups.get(up), ld))
                .reduce(
                        new State(),
                        (s1, s2) -> s1.merge(s2),
                        (s1, s2) -> s1.merge(s2)
                );
    }

    public Util.Pair<State, State> snapshot(Map<Duration, Integer> ld, Map<Duration, Integer> rd, ZonedDateTime at, List<Upstream> upstreams, List<Ticker> tickers) {
        Map<Upstream, List<Ticker>> groups = this.groupByUpstream(upstreams, tickers, at);
        return groups.keySet().stream()
                .parallel()
                .map(up -> snapshot(up, groups.get(up), ld, rd, at))
                .reduce(
                        Util.Pair.create(new State(), new State()),
                        (p1, p2) -> Util.Pair.create(p1.first.merge(p2.first), p1.second.merge(p2.second)),
                        (p1, p2) -> Util.Pair.create(p1.first.merge(p2.first), p1.second.merge(p2.second))
                );
    }
    private State.MutableState cache = new State.MutableState();

    public State update(
            Upstream upstream,
            Collection<Ticker> tickers,
            Map<Duration, Integer> leftDependencies
    ) {
        return this.snapshot(upstream, tickers, leftDependencies, Map.of(), ZonedDateTime.now()).first;
    }
    public Util.Pair<State, State> snapshot(
            Upstream upstream,
            Collection<Ticker> tickers,
            Map<Duration, Integer> leftDependencies,
            Map<Duration, Integer> rightDependencies,
            ZonedDateTime at
    ) {
        try {
            Map<Duration, Util.Pair<Integer, Duration>> dependencies = new ConcurrentHashMap<>(); //overall dependency
            // combine left and right dependencies
            Collection<Duration> intervals = new HashSet<>();
            intervals.addAll(leftDependencies.keySet());
            intervals.addAll(rightDependencies.keySet());
            for (Duration interval : intervals) {
                int ld = leftDependencies.getOrDefault(interval, 0);
                Duration rd = interval.multipliedBy(rightDependencies.getOrDefault(interval, 0));
                dependencies.put(interval, Util.Pair.create(ld, rd));
            }
            for (Ticker ticker : tickers) { // iterate over tickers
                for (Duration interval : dependencies.keySet()) { // iterate over intervals
                    int ld = dependencies.get(interval).first;
                    Duration rd = dependencies.get(interval).second;
                    ZonedDateTime rightDT = at.plus(rd);
                    Duration minLeftDuration = interval.multipliedBy(ld - 1); // based on minimum left duration
                    if (!cache.contains(ticker, interval)) { // not in cache {6}
                        // basic fetch left by count
                        TimeSeries deltaLeft = upstream.fetchCountUntil(ticker, interval, ld, at);
                        // basic fetch right by duration
                        TimeSeries deltaRight = upstream.fetchBetween(ticker, interval, at, rightDT);
                        // combine by right extend
                        TimeSeries delta = deltaLeft.extendRight(deltaRight);
                        // put cache entry
                        cache.put(ticker, interval, delta);
                        continue;
                    }
                    cache.markHit(ticker, interval);
                    ZonedDateTime cacheFirst = cache.from(ticker, interval);
                    ZonedDateTime cacheLast = cache.until(ticker, interval);
                    if (at.plus(rd).isAfter(cacheLast)) { // rightmost is after cache last {1, 2, 8, 9, 10, 11}
                        if (at.minus(minLeftDuration).isAfter(cacheLast)){// no guaranteed overlap {8}
                            // basic fetch left by count
                            TimeSeries deltaLeft = upstream.fetchCountUntil(ticker, interval, ld, at);
                            // basic fetch right by duration
                            TimeSeries deltaRight = upstream.fetchBetween(ticker, interval, at, rightDT);
                            // combine by right extend
                            TimeSeries delta = deltaLeft.extendRight(deltaRight);
                            // replace cache entry
                            cache.put(ticker, interval, delta);
                        }
                        else{ // guaranteed overlap {1, 2, 9, 10, 11}
                            // fetch by duration from cache last until rightmost
                            TimeSeries deltaRight = upstream.fetchBetween(ticker, interval, cacheLast, rightDT);
                            // right extend cache
                            cache.extendRight(ticker, interval, deltaRight);
                            // make sure all RD candles are cached
                            if (at.isBefore(cacheFirst)) {
                                cache.extendLeft(
                                        ticker,
                                        interval,
                                        upstream.fetchBetween(ticker,interval, at, cacheFirst.minusNanos(1000))
                                );
                            }
                            // find number of cached candles not after at
                            int numCached = cache.pointsNotAfter(ticker, interval, at);
                            if (numCached < ld) { // too few cached candles {9, 10, 11}
                                // fetch by count ending at cache first
                                TimeSeries deltaLeft = upstream.fetchCountUntil(ticker, interval, ld-numCached, cacheFirst.minusNanos(1000));
                                // left extend cache
                                cache.extendLeft(ticker, interval, deltaLeft);
                            } else { // at > cache last or <= cache last {1, 2}
                                // left slice cache by index
                                cache.dropLeft(ticker, interval, numCached-ld);
                            }
                        }
                    } else { // rightmost is before cache last {3, 4, 5, 7}
                        if (rightDT.isBefore(cacheFirst)) { // rightmost is before cache first {7}
                            // basic fetch left by count
                            TimeSeries deltaLeft = upstream.fetchCountUntil(ticker, interval, ld, at);
                            // basic fetch right by duration
                            TimeSeries deltaRight = upstream.fetchBetween(ticker, interval, at, rightDT);
                            // combine by right extend
                            TimeSeries delta = deltaLeft.extendRight(deltaRight);
                            // replace cache entry
                            cache.put(ticker, interval, delta);
                        } else { // guaranteed overlap {3, 4, 5}
                            if (at.isBefore(cacheFirst)) { // at is before cache first {3}
                                // fetch left by count
                                TimeSeries deltaLeft = upstream.fetchCountUntil(ticker, interval, ld, at);
                                // fetch right by missing duration
                                TimeSeries deltaRight = upstream.fetchBetween(ticker, interval, at, cacheFirst.minusNanos(1000));
                                // right extend delta
                                TimeSeries delta = deltaLeft.extendRight(deltaRight);
                                // left extend cache
                                cache.extendLeft(ticker, interval, delta);
                                // right slice cache by DateTime
                                cache.dropAfter(ticker, interval, rightDT);
                            } else { // at is >= cache first {4, 6}
                                // find number of cached candles not after at
                                int numCached = cache.pointsNotAfter(ticker, interval, at);
                                if (numCached < ld) { // too few cached candles {4}
                                    // fetch left by missing count
                                    TimeSeries delta = upstream.fetchCountUntil(ticker, interval, ld-numCached, cacheFirst.minusNanos(1000));
                                    // left extend cache
                                    cache.extendLeft(ticker, interval, delta);
                                    // right slice cache by DateTime
                                    cache.dropAfter(ticker, interval, rightDT);
                                } else { // completely enclosed by cached candles {5}
                                    // right slice cache by DateTime
                                    cache.dropAfter(ticker, interval, rightDT);
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
        upstream.fetchPositionsNow(Set.copyOf(tickers)).forEach(
                cache::put
        );
        Util.Pair<State, State> states = cache.partition(at);
        return Util.Pair.create(states.first.asView(tickers), states.second.asView(tickers));
    }
}
