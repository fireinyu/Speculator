package engine.components;


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.menus.Tickers;
import engine.PriceData.Upstream;
import engine.Serialisation.CoreStateMachine;
import engine.Util.Pair;
import engine.menus.Upstreams;

public abstract class Ticker extends CoreStateMachine<Ticker> {

    public static Ticker of(String name, Map<Upstream, String> aliases, int index) {
        return new MapTicker(name, aliases,  index);
    }
    public static Ticker of(String name, List<Pair<Upstream, String>> aliases, int index) {
        return new MapTicker(
                name,
                aliases.stream().map(pair -> pair.first).collect(Collectors.toList()),
                aliases.stream().map(pair -> pair.second).collect(Collectors.toList()),
                index
        );
    }
    public static Ticker of(String name, List<Upstream> upstreams, List<String> aliases, int index) {
        return new MapTicker(name, upstreams, aliases, index);
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
    public abstract List<Upstream> preferredUpstreams();
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

        private Map<Upstream, String> aliases;
        private LinkedHashSet<Upstream> preferred;

        private MapTicker(String name, Map<Upstream, String> aliases, int index) {
            super(name, index);
            this.aliases = new HashMap<>();
            for (Upstream key : aliases.keySet()) {
                this.aliases.put(key, aliases.get(key));
            }
            this.preferred = new LinkedHashSet<>(aliases.keySet());
        }

        private MapTicker(String name, List<Upstream> upstreams, List<String> aliases, int index) {
            super(name, index);
            this.aliases = new HashMap<>();
            for (int i = 0; i < upstreams.size(); i++) {
                this.aliases.put(upstreams.get(i), aliases.get(i));
            }
            this.preferred = new LinkedHashSet<>(upstreams);
        }

        @Override
        public String getAliasFor(Upstream upstream) {
            return this.aliases.get(upstream);
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
        public List<Upstream> preferredUpstreams() {
            return this.preferred.stream().collect(Collectors.toList());
        }

        @Override
        public boolean canRequestFrom(Upstream upstream) {
            return this.preferred.contains(upstream);
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
}