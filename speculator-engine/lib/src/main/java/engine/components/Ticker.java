package engine.components;


import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.menus.Tickers;
import engine.PriceData.Upstream;
import engine.Serialisation.CoreStateMachine;

public abstract class Ticker extends CoreStateMachine<Ticker> {

//    public static Ticker of(String name, Map<Upstream, String> aliases, int index) {
//        return new MapTicker(name, aliases,  index);
//    }
//    public static Ticker of(String name, List<Pair<Upstream, String>> aliases, int index) {
//        return new MapTicker(
//                name,
//                aliases.stream().map(pair -> pair.first).collect(Collectors.toList()),
//                aliases.stream().map(pair -> pair.second).collect(Collectors.toList()),
//                index
//        );
//    }
//    public static Ticker of(String name, List<Upstream> upstreams, List<String> aliases, int index) {
//        return new MapTicker(name, upstreams, aliases, index);
//    }
//
    public static Ticker of(String name, List<TickerSource> sources, int index) {
        return new MapTicker(name, sources, index);
    }


    private String name;

    public Ticker(String name, int index) {
        super(index);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract String getAliasFor(Upstream upstream);
    public abstract List<Upstream> preferredUpstreams(ZonedDateTime at);
    public abstract boolean canRequestFrom(Upstream upstream);

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Ticker)) {
            return false;
        }
        Ticker other = (Ticker) obj;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    private static class MapTicker extends Ticker{

        private LinkedHashMap<Upstream, TickerSource> sources;

        private MapTicker(String name, List<TickerSource> sources, int index) {
            super(name, index);
            this.sources = new LinkedHashMap<>();
            sources.forEach(src -> this.sources.put(src.getUpstream(), src));

        }

        @Override
        public String getAliasFor(Upstream upstream) {
            return this.sources.get(upstream).getAlias();
//            Class<?> cls = adapterClass;
//            String alias = null;
//            while (!cls.equals(Object.class)) {
//                alias = this.aliases.get(cls);
//                if (alias != null) {
//                    break;
//                }
//                cls = cls.getSuperclass();
//            }
//            return alias;
        }

        @Override
        public List<Upstream> preferredUpstreams(ZonedDateTime at) {
            return this.sources.values().stream()
                    .filter(src -> src.getTradingSchedule().isTrading(at))
                    .map(TickerSource::getUpstream)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean canRequestFrom(Upstream upstream) {
            return this.sources.containsKey(upstream);
        }

        @Override
        public CoreStateLoader<Ticker> getLoader() {
            return new TickerLoader();
        }

        private static class TickerLoader extends CoreStateLoader<Ticker> {
            @Override
            public List<Ticker> getSource() {
                return Tickers.list;
            }
        }
    }

    public static class TickerSource {
        private Upstream upstream;
        private String alias;
        private TradingSchedule tradingSchedule;

        public TickerSource(String alias, TradingSchedule tradingSchedule, Upstream upstream) {
            this.alias = alias;
            this.tradingSchedule = tradingSchedule;
            this.upstream = upstream;
        }

        public String getAlias() {
            return alias;
        }

        public TradingSchedule getTradingSchedule() {
            return tradingSchedule;
        }

        public Upstream getUpstream() {
            return upstream;
        }
    }

    public static class TradingSchedule {
        private TradingSession normal;
        private HashMap<DayOfWeek, TradingSession> weekend;
        private HashMap<Month, HashMap<Integer, TradingSession>> holidays;

        public TradingSchedule(TradingSession normal, Map<DayOfWeek, TradingSession> weekend, Map<Month, Map<Integer, TradingSession>> holidays) {
            this.normal = normal;
            this.weekend = new HashMap<>(weekend);
            this.holidays = new HashMap<>(holidays.keySet().stream().collect(Collectors.toMap(month -> month, month -> new HashMap<>(holidays.get(month)))));
        }

        public boolean isTrading(ZonedDateTime at) {
            if (holidays.containsKey(at.getMonth()) && holidays.get(at.getMonth()).containsKey(at.getDayOfMonth())) {
                return holidays.get(at.getMonth()).get(at.getDayOfMonth()).isTrading(at);
            } else if (weekend.containsKey(at.getDayOfWeek())) {
                return weekend.get(at.getDayOfWeek()).isTrading(at);
            } else {
                return normal.isTrading(at);
            }
        }
    }

    public static class TradingSession {
        public static TradingSession empty() {
            return new TradingSession(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT);
        }

        private LocalTime start;
        private LocalTime end;

        public TradingSession(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public boolean isTrading(ZonedDateTime at) {
            LocalTime time = at.toLocalTime();
            if (start.equals(end)) {
                return false;
            } else if (start.isBefore(end)) {
                return !(time.isBefore(start) || time.isAfter(end));
            } else {
                return !(time.isBefore(start) && time.isAfter(end));
            }
        }
    }
}