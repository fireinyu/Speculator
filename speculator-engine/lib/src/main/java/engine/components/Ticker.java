package engine.components;


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.PriceData.Upstream;
import engine.Util.Pair;

public abstract class Ticker <V extends Number>{

    public static <V extends Number> Ticker<V> of(String name, Map<Upstream<V>, String> aliases) {
        return new MapTicker<>(name, aliases);
    }
    public static <V extends Number> Ticker<V> of(String name, List<Pair<Upstream<V>, String>> aliases) {
        return new MapTicker<>(
                name,
                aliases.stream().map(pair -> pair.first).collect(Collectors.toList()),
                aliases.stream().map(pair -> pair.second).collect(Collectors.toList())
        );
    }
    public static <V extends Number> Ticker<V> of(String name, List<Upstream<V>> upstreams, List<String> aliases) {
        return new MapTicker<>(name, upstreams, aliases);
    }


    private String name;

    public Ticker(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract String getAliasFor(Upstream<V> upstream);
    public abstract List<Upstream<V>> preferredUpstreams();
    public abstract boolean canRequestFrom(Upstream<V> upstream);

    @Override
    public String toString() {
        return this.name;
    }
    private static class MapTicker <V extends Number> extends Ticker<V>{

        private Map<Upstream<V>, String> aliases;
        private LinkedHashSet<Upstream<V>> preferred;

        private MapTicker(String name, Map<Upstream<V>, String> aliases) {
            super(name);
            this.aliases = new HashMap<>();
            for (Upstream<V> key : aliases.keySet()) {
                this.aliases.put(key, aliases.get(key));
            }
            this.preferred = new LinkedHashSet<>(aliases.keySet());
        }

        private MapTicker(String name, List<Upstream<V>> upstreams, List<String> aliases) {
            super(name);
            this.aliases = new HashMap<>();
            for (int i = 0; i < upstreams.size(); i++) {
                this.aliases.put(upstreams.get(i), aliases.get(i));
            }
            this.preferred = new LinkedHashSet<>(upstreams);
        }

        @Override
        public String getAliasFor(Upstream<V> upstream) {
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
        public List<Upstream<V>> preferredUpstreams() {
            return this.preferred.stream().collect(Collectors.toList());
        }

        @Override
        public boolean canRequestFrom(Upstream<V> upstream) {
            return this.preferred.contains(upstream);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }
}