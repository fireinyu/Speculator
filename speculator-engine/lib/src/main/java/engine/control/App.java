package engine.control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import engine.PriceData.Position;
import engine.PriceData.State;
import engine.PriceData.TimeSeries;
import engine.PriceData.Upstream;
import engine.Serialisation.EditMenu;
import engine.Serialisation.LocalObject;
import engine.Serialisation.Menu;
import engine.Serialisation.Preset;
import engine.Util;
import engine.components.Agent;
import engine.components.DrawInstructor;
import engine.components.ExecutionReporter;
import engine.components.Executor;
import engine.components.InstructedDrawer;
import engine.components.ModelPredictor;
import engine.Serialisation.PresetMenu;
import engine.components.Simulator;
import engine.components.Ticker;
import engine.menus.Agents;
import engine.menus.DrawInstructors;
import engine.menus.Executors;
import engine.menus.ModelLoaders;
import engine.menus.Tickers;
import engine.menus.Upstreams;

public class App implements Serializable {
    public static App start(Path root, ExecutionReporter reporter, InstructedDrawer drawer) {
        LocalObject<App> res = new LocalObject<>(root, "_App");
        return res.get().map(app -> {
            app.AM.init(
                    Agents.list,
                    DrawInstructors.list,
                    Executors.list,
                    ModelLoaders.list,
                    Tickers.list,
                    Upstreams.list);
            app.root = root;
            return app;
        }).orElseGet(() -> new App(root, reporter, drawer));
    }

    public Menu<Ticker> tickers = Tickers.menu;
    public Menu<Upstream> upstreams = Upstreams.menu;
    public Menu<DrawInstructor> plotters = DrawInstructors.menu;
    public Menu<Executor> executors = Executors.menu;
    public EditMenu<ModelPredictor> models = ModelLoaders.menu;
    public EditMenu<Agent> agents = Agents.menu;
    private PresetMenu presets;
    public ExecutionReporter reporter;
    public InstructedDrawer drawer;
    private AuthManager AM;
    private transient PredictManager PM;
    private transient UpstreamManager UM;
    private transient DrawManager DM;
    private transient ScheduledExecutorService cycleService;
    private transient Map<String, Future<?>> running;
    private transient Simulator simulator;
    public transient Path root;


    /// meta
    private App(Path root, ExecutionReporter reporter, InstructedDrawer drawer) {
        this.root = root;
        this.reporter = reporter;
        this.drawer = drawer;
        this.presets = new PresetMenu(this);
        this.AM = new AuthManager(
                Agents.list,
                DrawInstructors.list,
                Executors.list,
                ModelLoaders.list,
                Tickers.list,
                Upstreams.list
        );
        this._init();
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // 1. Perform default restoration for non-transient fields
        in.defaultReadObject();
        this._init();
    }

    private void _init() {
        this.cycleService = new ScheduledThreadPoolExecutor(8);
        this.running = new HashMap<>();
        this.UM = new UpstreamManager(tickers, upstreams);
        this.PM = new PredictManager(models);
        this.DM = new DrawManager(plotters, tickers, models, drawer);
        this.simulator = new Simulator();
    }

    public void save() {
        LocalObject<App> local = new LocalObject<>(this.root, "_App");
        local.put(this);
    }

    /// Auth
    public List<String> getAuthTargets() {
        return this.AM.getTargets();
    }
    public List<String> getAuthFields(String target) {
        return this.AM.getFields(target);
    }
    public List<String> getAuthFields() {
        return this.AM.getFields();
    }
    public boolean isAuthFilled(String field) {
        return this.AM.isFilled(field);
    }
    public void authenticate(Map<String, String> creds) {
        this.AM.auth(creds);
    }


    /// menus
    public PresetMenu getPresets() {
        return this.presets;
    }
    public Menu<Ticker> getTickers() {
        return this.tickers;
    }
    public Menu<Upstream> getUpstreams() {
        return this.upstreams;
    }
    public Menu<DrawInstructor> getPlotters() {
        return plotters;
    }
    public EditMenu<ModelPredictor> getModels() {
        return models;
    }
    public Menu<Executor> getExecutors() {
        return executors;
    }
    public EditMenu<Agent> getAgents() {
        return agents;
    }
//    public List<String> getPresets() {
//        return this.presets.stream()
//                .map(Objects::toString)
//                .collect(Collectors.toList());
//    }
//    public void savePreset(String name) {
//        for (int i = 0 ;i < presets.size(); i++) {
//            if (presets.get(i).toString().equals(name)) {
//                presets.set(i, new Preset(name, this));
//                return;
//            }
//        }
//        this.presets.add(new Preset(name, this));
//    }
//    public void removePreset(String name) {
//        for (int i = presets.size()-1 ;i > -1; i--) {
//            if (presets.get(i).toString().equals(name)) {
//                presets.remove(i);
//                return;
//            }
//        }
//    }

//    public void usePreset(String name) {
//        this.presets.stream()
//                .filter(preset -> preset.toString().equals(name))
//                .findAny()
//                .ifPresent(preset -> preset.apply(this));
//    }

    /// app cycle

    public void pullPlot(Duration interval) {
        for (String taskLabel : new String[]{
                "predictPlot",
                "predictAct",
                "predictPlotCycle",
                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("predictPlot", CompletableFuture.runAsync(() -> {
            Map<Duration, Integer> ld = Map.of(interval, 100);
            State state = UM.update(ld);
            DM.draw(state);
        }));
    }

    public void pullPlotCycle(Duration interval) {
//        if (running.containsKey("pullPlotCycle") && !running.get("pullPlotCycle").isDone()) {
//            return;
//        }
        for (String taskLabel : new String[]{
                "predictPlot",
                "predictAct",
                "predictPlotCycle",
                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }

        Future<?> task = this.cycleService.scheduleWithFixedDelay(() -> pullPlot(interval), 300, 300, TimeUnit.MILLISECONDS);
        running.put("predictPlotCycle", task);
    }

    public void predictPlotCycle() {
//        if (running.containsKey("predictPlotCycle") && !running.get("predictPlotCycle").isDone()) {
//            return;
//        }
        for (String taskLabel : new String[]{
                "predictPlot",
                "predictAct",
                "predictPlotCycle",
                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }

        Future<?> task = this.cycleService.scheduleWithFixedDelay(this::predictPlot, 300, 300, TimeUnit.MILLISECONDS);
        running.put("predictPlotCycle", task);
    }

    public void predictActCycle() {
//        if (running.containsKey("predictActCycle") && !running.get("predictActCycle").isDone()) {
//            return;
//        }
        for (String taskLabel : new String[]{
                "predictPlot",
                "predictAct",
                "predictPlotCycle",
                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }

        Future<?> task = this.cycleService.scheduleWithFixedDelay(this::predictAct, 300, 300, TimeUnit.MILLISECONDS);
        running.put("predictActCycle", task);
    }
    public void predictPlot() {
        for (String taskLabel : new String[]{
                "predictPlot",
                "predictAct",
                "predictPlotCycle",
                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("predictPlot", CompletableFuture.runAsync(() -> {
            Map<Duration, Integer> ld = PM.getDependencies().first;
            State state = UM.update(ld);
            List<PredictManager.PredictResult> predictions = PM.predict(state);
            DM.draw(state, predictions);
        }));
    }

    public void predictAct() {
        for (String taskLabel : new String[]{
                "predictPlot",
                "predictAct",
                "predictPlotCycle",
                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("predictAct", CompletableFuture.runAsync(() -> {
            Agent agent = this.agents.getSelection().get(0);
            Executor executor = this.executors.getSelection().get(0);
            Map<Duration, Integer> ld = PM.getDependencies().first;
            State state = UM.update(ld);
            List<PredictManager.PredictResult> predictions = PM.predict(state);
            DM.draw(state, predictions);
            List<Position> actions = agent.suggest(state, predictions);
            reporter.report(actions, executor.execute(actions));
        }));
    }

    public void backtestPredict(ZonedDateTime at) {
        for (String taskLabel : new String[]{
                "backtestPredict",
                "backtestAct"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("backtestPredict", CompletableFuture.runAsync(() -> {
            Util.Pair<Map<Duration,Integer>,Map<Duration,Integer>> deps = PM.getDependencies();
            Util.Pair<State, State> states = UM.snapshot(deps.first, deps.second, at);
            List<PredictManager.PredictResult> results = PM.predict(states.first);
            DM.drawBacktest(states.first, states.second, results);
        }));

    }

    public void backtestPlotCycle(ZonedDateTime from, Duration simInterval, Duration actualInterval) {

    }

    public void endTasks() {
        this.running.values().forEach(task -> task.cancel(true));
        this.running = new HashMap<>();
    }

    public void completeTasks() {
        this.running.values().forEach(task -> {
            try {
                task.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    
}
