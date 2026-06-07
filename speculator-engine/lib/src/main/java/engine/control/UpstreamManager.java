package engine.control;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import engine.PriceData.State;
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
                .map(up -> up.update(groups.get(up), ld))
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
                .map(up -> up.snapshot(groups.get(up), ld, rd, at))
                .reduce(
                        Util.Pair.create(new State(), new State()),
                        (p1, p2) -> Util.Pair.create(p1.first.merge(p2.first), p1.second.merge(p2.second)),
                        (p1, p2) -> Util.Pair.create(p1.first.merge(p2.first), p1.second.merge(p2.second))
                );
    }
}
