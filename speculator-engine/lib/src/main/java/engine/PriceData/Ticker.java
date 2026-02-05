package engine.PriceData;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import engine.Util.Pair;
import engine.components.Upstream;

public abstract class Ticker {

    public static Ticker of(String name, Map<? extends Upstream, String> aliases) {
        return new MapTicker(name, aliases);
    }
    public static Ticker of(String name, List<Pair<? extends Upstream, String>> aliases) {
        return new MapTicker(
                name,
                aliases.stream().map(pair -> pair.first).collect(Collectors.toList()),
                aliases.stream().map(pair -> pair.second).collect(Collectors.toList())
        );
    }
    public static Ticker of(String name, List<? extends Upstream> upstreams, List<String> aliases) {
        return new MapTicker(name, upstreams, aliases);
    }


    private String name;

    public Ticker(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract String getAliasFor(Upstream upstream);

    public abstract List<? extends Upstream> preferredUpstreams();

    @Override
    public String toString() {
        return this.name;
    }
    private static class MapTicker extends Ticker{

        private Map<Upstream, String> aliases;
        private List<Upstream> preferred;

        private MapTicker(String name, Map<? extends Upstream, String> aliases) {
            super(name);
            this.aliases = new HashMap<>();
            for (Upstream key : aliases.keySet()) {
                this.aliases.put(key, aliases.get(key));
            }
            this.preferred = List.copyOf(aliases.keySet());
        }

        private MapTicker(String name, List<? extends Upstream> upstreams, List<String> aliases) {
            super(name);
            this.aliases = new HashMap<>();
            for (int i = 0; i < upstreams.size(); i++) {
                this.aliases.put(upstreams.get(i), aliases.get(i));
            }
            this.preferred = List.copyOf(upstreams);
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
        public List<? extends Upstream> preferredUpstreams() {
            return this.preferred;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }
}