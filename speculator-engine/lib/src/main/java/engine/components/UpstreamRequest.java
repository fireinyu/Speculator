package engine.components;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import engine.Util;

public class UpstreamRequest<V extends Number> {

    public static Map<Duration, Integer> unionCommon (Collection<? extends Map<Duration, Integer>> commons) {
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

    public static <V extends Number> UpstreamRequest<V> union(Collection<UpstreamRequest<V>> requests) {
        return new UpstreamRequest<>(
                LeftRequest.leftUnion(requests.stream().map(UpstreamRequest::getLeft).collect(Collectors.toList())),
                RightRequest.rightUnion(requests.stream().map(UpstreamRequest::getRight).collect(Collectors.toList()))
        );
    }

    Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> common;
    private Set<Ticker<V>> tickers;
    Map<Ticker<V>, Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>>> special;

    public UpstreamRequest(
            Collection<Ticker<V>> tickers,
            Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> common,
            Map<Ticker<V>, Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>>> special
            ) {
        this.tickers = new HashSet<>(tickers);
        this.tickers.addAll(special.keySet());
        this.common = common;
        this.special = special;
    }
    public UpstreamRequest(
            Collection<Ticker<V>> tickers,
            Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> common
            ) {
        this(tickers, common, Map.of());
    }
    public UpstreamRequest(Map<Ticker<V>, Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>>> special) {
        this(Set.of(), null, special);
    }
    private UpstreamRequest(LeftRequest<V> left, RightRequest<V> right) {
        Set<Ticker<V>> tickers = new HashSet<>(left.getTickers());
        tickers.addAll(right.getTickers());
        Map<Ticker<V>, Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>>> special =  new HashMap<>();
        // non-intersecting common -> special
        for (Ticker<V> ticker : left.getTickers()) {
            if (left.special.containsKey(ticker)) { // special in left
                if (right.special.containsKey(ticker)) { // special in right
                    special.put(ticker, Util.Pair.create(left.special.get(ticker).first, right.special.get(ticker).first));
                } else if (right.getTickers().contains(ticker)) { // common in right
                    special.put(ticker, Util.Pair.create(left.special.get(ticker).first, right.common.second));
                } else { // not in right
                    special.put(ticker, left.special.get(ticker));
                }
            } else { // common in left
                if (right.special.containsKey(ticker)) { // special in right
                    special.put(ticker, Util.Pair.create(left.common.first, right.special.get(ticker).second));
                } else if (!right.getTickers().contains(ticker)) { // not in right
                    special.put(ticker, left.common);
                } // else: common in right
            }
        }
        for (Ticker<V> ticker : right.getTickers()) {
            if (left.getTickers().contains(ticker)) { // in left; double-counted
                continue;
            } else if (right.special.containsKey(ticker)){ // special in right
                special.put(ticker, right.special.get(ticker));
            } else { // common in right
                special.put(ticker, right.common);
            }
        }
        this.tickers = tickers;
        this.common = Util.Pair.create(left.common.first, right.common.second);
        this.special = special;
    }

    public Set<Ticker<V>> getTickers() {
        return tickers;
    }

    public boolean isEmpty() {
        if (
                tickers.isEmpty() ||
                common.first.values().stream().filter(ld -> ld > 0).count() +
                common.second.values().stream().filter(rd -> rd > 0).count() == 0
        ) {
            return special.values().stream()
                    .map(pair ->
                            pair.first.values().stream().filter(ld -> ld > 0).count() +
                                    pair.second.values().stream().filter(ld -> ld > 0).count()
                    ).anyMatch(sumDep -> sumDep > 0);
        }
        return false;

    }

    public LeftRequest<V> getLeft() {
        return new LeftRequest<>(
                tickers,
                common.first,
                special.keySet().stream()
                    .collect(Collectors.toMap(
                            ticker -> ticker,
                            ticker -> special.get(ticker).first
                    ))
        );
    }

    public RightRequest<V> getRight() {
        return new RightRequest<>(
                tickers,
                common.second,
                special.keySet().stream()
                        .collect(Collectors.toMap(
                                ticker -> ticker,
                                ticker -> special.get(ticker).second
                        ))
        );
    }

    Set<Duration> getLeftIntervals(Ticker<V> ticker) {
        Set<Duration> combined = new HashSet<>(common.first.keySet());
        combined.addAll(special.get(ticker).first.keySet());
        return combined;
    }

    Set<Duration> getRightIntervals(Ticker<V> ticker) {
        Set<Duration> combined = new HashSet<>(common.second.keySet());
        combined.addAll(special.get(ticker).second.keySet());
        return combined;
    }

    int getLD(Ticker<V> ticker, Duration interval) {
        if (special.containsKey(ticker)) {
            return special.get(ticker).first.get(interval);
        }
        return common.first.get(interval);
    }

    int getRD(Ticker<V> ticker, Duration interval) {
        if (special.containsKey(ticker)) {
            return special.get(ticker).first.get(interval);
        }
        return common.first.get(interval);
    }

//    public UpstreamRequest<V> intersect(UpstreamRequest<V> other) {
//        // merge common
//        Util.Pair<Map<Duration, Integer>, Map<Duration, Integer>> common = Util.Pair.create(
//                UpstreamRequest.intersect(this.common.first, other.common.first),
//                UpstreamRequest.intersect(this.common.second, other.common.second)
//        );
//        // merge special
//    }

    public static class LeftRequest <V extends Number> extends UpstreamRequest<V> {

        public static <V extends Number> LeftRequest<V> leftUnion (Collection<LeftRequest<V>> requests) {
            Collection<Ticker<V>> tickers = new HashSet<>();
            Map<Duration, Integer> common = new HashMap<>();
            Map<Ticker<V>, Map<Duration, Integer>> special =  new HashMap<>();
            for (LeftRequest<V> request : requests) {
                for (Ticker<V> ticker : request.getTickers()) {
                    if (request.special.containsKey(ticker)) { // special in new
                        if (special.containsKey(ticker)) { // special in existing
                            special.put(ticker, UpstreamRequest.unionCommon(Set.of(special.get(ticker), request.special.get(ticker).first)));
                        } else if (tickers.contains(ticker)){ // common in existing
                            special.put(ticker, UpstreamRequest.unionCommon(Set.of(common, request.special.get(ticker).first)));
                        } else { // not in existing
                            tickers.add(ticker);
                            special.put(ticker, request.special.get(ticker).first);
                        }
                    } else { // common in new
                        if (special.containsKey(ticker)) { // special in existing
                        } else if (tickers.contains(ticker)) { // common in existing
                        } else { // not in existing
                            tickers.add(ticker); // add to common
                        }
                    }
                }
                common = UpstreamRequest.unionCommon(Set.of(common, request.common.first));
            }
            return new LeftRequest<>(tickers, common, special);
        }

        public LeftRequest(
                Collection<Ticker<V>> tickers,
                Map<Duration, Integer> common,
                Map<Ticker<V>, Map<Duration, Integer>> special
        ) {
            super(
                    tickers,
                    Util.Pair.create(common, Map.of()),
                    special.keySet().stream().collect(Collectors.toMap(
                    ticker -> ticker,
                    ticker -> Util.Pair.create(special.get(ticker), Map.of())
                    )
                )
            );
        }

        public Set<Duration> getIntervals(Ticker<V> ticker) {
            return super.getLeftIntervals(ticker);
        }

        public int getDependency(Ticker<V> ticker, Duration interval) {
            return super.getLD(ticker, interval);
        }

    }

    public static class RightRequest <V extends Number> extends UpstreamRequest<V> {
        public static <V extends Number> RightRequest<V> rightUnion (Collection<RightRequest<V>> requests) {
            Collection<Ticker<V>> tickers = new HashSet<>();
            Map<Duration, Integer> common = new HashMap<>();
            Map<Ticker<V>, Map<Duration, Integer>> special =  new HashMap<>();
            for (RightRequest<V> request : requests) {
                for (Ticker<V> ticker : request.getTickers()) {
                    if (request.special.containsKey(ticker)) { // special in new
                        if (special.containsKey(ticker)) { // special in existing
                            special.put(ticker, UpstreamRequest.unionCommon(Set.of(special.get(ticker), request.special.get(ticker).second)));
                        } else if (tickers.contains(ticker)){ // common in existing
                            special.put(ticker, UpstreamRequest.unionCommon(Set.of(common, request.special.get(ticker).second)));
                        } else { // not in existing
                            tickers.add(ticker);
                            special.put(ticker, request.special.get(ticker).second);
                        }
                    } else { // common in new
                        if (special.containsKey(ticker)) { // special in existing
                        } else if (tickers.contains(ticker)) { // common in existing
                        } else { // not in existing
                            tickers.add(ticker); // add to common
                        }
                    }
                }
                common = UpstreamRequest.unionCommon(Set.of(common, request.common.second));
            }
            return new RightRequest<>(tickers, common, special);
        }
        
        public RightRequest(
                Collection<Ticker<V>> tickers,
                Map<Duration, Integer> common,
                Map<Ticker<V>, Map<Duration, Integer>> special
        ) {
            super(
                    tickers,
                    Util.Pair.create(Map.of(), common),
                    special.keySet().stream().collect(Collectors.toMap(
                                    ticker -> ticker,
                                    ticker -> Util.Pair.create(Map.of(), special.get(ticker))
                            )
                    )
            );
        }

        public Set<Duration> getIntervals(Ticker<V> ticker) {
            return super.getRightIntervals(ticker);
        }

        public int getDependency(Ticker<V> ticker, Duration interval) {
            return super.getRD(ticker, interval);
        }
    }

}
