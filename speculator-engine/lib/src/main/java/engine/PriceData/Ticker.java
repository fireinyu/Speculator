package engine.PriceData;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import engine.Util.Pair;
import engine.components.UpstreamAdapter;

public abstract class Ticker {

    public static Ticker of(String name, Map<? extends UpstreamAdapter, String> aliases) {
        return new MapTicker(name, aliases);
    }
    public static Ticker of(String name, List<Pair<? extends UpstreamAdapter, String>> aliases) {
        return new MapTicker(
                name,
                aliases.stream().map(pair -> pair.first).collect(Collectors.toList()),
                aliases.stream().map(pair -> pair.second).collect(Collectors.toList())
        );
    }
    public static Ticker of(String name, List<? extends UpstreamAdapter> upstreams, List<String> aliases) {
        return new MapTicker(name, upstreams, aliases);
    }


    private String name;

    public Ticker(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getAliasFor(UpstreamAdapter adapter) {
        return this.getAliasFor(adapter.getClass());
    }

    public abstract String getAliasFor(Class<? extends UpstreamAdapter> upstream);

    public abstract List<? extends UpstreamAdapter> preferredUAs();

    @Override
    public String toString() {
        return this.name;
    }
    private static class MapTicker extends Ticker{

        private Map<Class<? extends UpstreamAdapter>, String> aliases;
        private List<UpstreamAdapter> preferred;

        private MapTicker(String name, Map<? extends UpstreamAdapter, String> aliases) {
            super(name);
            this.aliases = new HashMap<>();
            for (UpstreamAdapter key : aliases.keySet()) {
                this.aliases.put(key.getClass(), aliases.get(key));
            }
            this.preferred = List.copyOf(aliases.keySet());
        }

        private MapTicker(String name, List<? extends UpstreamAdapter> upstreams, List<String> aliases) {
            super(name);
            this.aliases = new HashMap<>();
            for (int i = 0; i < upstreams.size(); i++) {
                this.aliases.put(upstreams.get(i).getClass(), aliases.get(i));
            }
            this.preferred = List.copyOf(upstreams);
        }

        @Override
        public String getAliasFor(Class<? extends UpstreamAdapter> adapterClass) {
            Class<?> cls = adapterClass;
            String alias = null;
            while (!cls.equals(Object.class)) {
                alias = this.aliases.get(cls);
                if (alias != null) {
                    break;
                }
                cls = cls.getSuperclass();
            }
            return alias;
        }

        public List<? extends UpstreamAdapter> preferredUAs() {
            return this.preferred;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
    }
}