package engine.Serialisation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.control.App;

public class
Preset extends UserStateMachine<Preset> {
    private LinkedHashSet<Integer> agents;
    private LinkedHashSet<Integer> plotters;
    private LinkedHashSet<Integer> executors;
    private LinkedHashSet<Integer> models;
    private LinkedHashSet<Integer> tickers;
    private LinkedHashSet<Integer> upstreams;
    private String name;

//    @Override
//    public boolean equals(Object obj) {
//        if (! (obj instanceof Preset)) {
//            return false;
//        }
//        Preset<?, ?> other = (Preset<?, ?>) obj;
//        return tickers.equals(other.tickers) &&
//                modelStates.equals(other.modelStates) &&
//                instructorStates.equals(other.instructorStates);
//    }

    public Preset(
            String name,
            App app
        ) {
        super(Map.of(
                "name", name,
                "selected", new JSONObject(Map.of(
                        "agents", new JSONArray(app.agents.getSelectedIndices()),
                        "plotters", new JSONArray(app.plotters.getSelectedIndices()),
                        "executors", new JSONArray(app.executors.getSelectedIndices()),
                        "models", new JSONArray(app.models.getSelectedIndices()),
                        "upstreams", new JSONArray(app.upstreams.getSelectedIndices()),
                        "tickers", new JSONArray(app.tickers.getSelectedIndices())
                )).toString()
        ));
        this.name = name;
        this.agents = app.agents.getSelectedIndices();
        this.plotters =  app.plotters.getSelectedIndices();
        this.executors =  app.executors.getSelectedIndices();
        this.models = app.models.getSelectedIndices();
        this.upstreams = app.upstreams.getSelectedIndices();
        this.tickers = app.tickers.getSelectedIndices();
    }
    private Preset(
            String name,
            List<Integer> agents,
            List<Integer> plotters,
            List<Integer> executors,
            List<Integer> models,
            List<Integer> upstreams,
            List<Integer> tickers
    ){
        super(Map.of(
                "name", name,
                "selected", new JSONObject(Map.of(
                        "agents", new JSONArray(agents),
                        "plotters", new JSONArray(plotters),
                        "executors", new JSONArray(executors),
                        "models", new JSONArray(models),
                        "upstreams", new JSONArray(upstreams),
                        "tickers", new JSONArray(tickers)
                )).toString()
        ));
        this.name = name;
        this.agents = new LinkedHashSet<>(agents);
        this.executors = new LinkedHashSet<>(executors);
        this.plotters = new LinkedHashSet<>(plotters);
        this.models = new LinkedHashSet<>(models);
        this.upstreams = new LinkedHashSet<>(upstreams);
        this.tickers = new LinkedHashSet<>(tickers);

    }

    public void apply(App app) {
        app.agents.unselectAll();
        app.agents.selectAll(this.agents);
        app.plotters.unselectAll();
        app.plotters.selectAll(this.plotters);
        app.tickers.unselectAll();
        app.tickers.selectAll(this.tickers);
        app.upstreams.unselectAll();
        app.upstreams.selectAll(this.upstreams);
        app.executors.unselectAll();
        app.executors.selectAll(this.executors);
        app.models.unselectAll();
        app.models.selectAll(this.models);

    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public UserStateLoader<? extends StateMachine<Preset>> getLoader() {
        return new PresetLoader();
    }

    public static class PresetLoader extends UserStateLoader<Preset> {
        public PresetLoader() {
            super(List.of("name"));
        }

        @Override
        public Preset load(Map<String, String> state) {
            String name = state.get("name");
            JSONObject selected = new JSONObject(state.get("selected"));
            return new Preset(
                    name,
                    selected.getJSONArray("agents").toList().stream()
                            .map(x -> (Integer)x)
                            .collect(Collectors.toList()),
                    selected.getJSONArray("plotters").toList().stream()
                            .map(x -> (Integer)x)
                            .collect(Collectors.toList()),
                    selected.getJSONArray("executors").toList().stream()
                            .map(x -> (Integer)x)
                            .collect(Collectors.toList()),
                    selected.getJSONArray("models").toList().stream()
                            .map(x -> (Integer)x)
                            .collect(Collectors.toList()),
                    selected.getJSONArray("upstreams").toList().stream()
                            .map(x -> (Integer)x)
                            .collect(Collectors.toList()),
                    selected.getJSONArray("tickers").toList().stream()
                            .map(x -> (Integer)x)
                            .collect(Collectors.toList())
            );
        }
    }
}
