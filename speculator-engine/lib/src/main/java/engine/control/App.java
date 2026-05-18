package engine.control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import engine.PriceData.Position;
import engine.PriceData.State;
import engine.PriceData.Upstream;
import engine.Serialisation.EditMenu;
import engine.Serialisation.LocalObject;
import engine.Serialisation.Menu;
import engine.Util;
import engine.components.Agent;
import engine.components.DrawInstructor;
import engine.components.Reporter;
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
    public static App start(Path root, Reporter reporter, InstructedDrawer drawer) {
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
            app.drawer = drawer;
            app.reporter = reporter;
            app._init();
            return app;
        }).orElseGet(() -> new App(root, reporter, drawer));
    }

    public static App startNew(Path root, Reporter reporter, InstructedDrawer drawer) {
        return new App(root, reporter, drawer);
    }

    public Menu<Ticker> tickers = Tickers.menu;
    public Menu<Upstream> upstreams = Upstreams.menu;
    public Menu<DrawInstructor> plotters = DrawInstructors.menu;
    public Menu<Executor> executors = Executors.menu;
    public EditMenu<ModelPredictor> models = ModelLoaders.menu;
    public EditMenu<Agent> agents = Agents.menu;
    private PresetMenu presets;
    public transient Reporter reporter;
    public transient InstructedDrawer drawer;
    private AuthManager AM;
    private transient PredictManager PM;
    private transient UpstreamManager UM;
    private transient DrawManager DM;
    private transient ScheduledExecutorService cycleService;
    private transient Map<String, Future<?>> running;
    private transient Simulator simulator;
    public transient Path root;


    /// meta
    private App(Path root, Reporter reporter, InstructedDrawer drawer) {
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

    private void _init() {
        this.cycleService = new ScheduledThreadPoolExecutor(8);
        this.running = new HashMap<>();
        this.UM = new UpstreamManager();
        this.PM = new PredictManager();
        this.DM = new DrawManager(drawer);
//        this.simulator = new Simulator();
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

    /// info
//    public List<Ticker> availableTickers() {
//        return UM.availableTickers();
//    }

    /// app cycle
    public void pullPlot(Duration interval) {
        List<Ticker> selTickers = tickers.getSelection();
        List<Upstream> selUpstreams = upstreams.getSelection();
        List<DrawInstructor> selPlotters = plotters.getSelection();
        for (String taskLabel : new String[]{
//                "predictPlot",
//                "predictAct",
//                "predictPlotCycle",
//                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("predictPlot", CompletableFuture.runAsync(() -> {
            Map<Duration, Integer> ld = Map.of(interval, 100);
            State state = UM.update(ld, selUpstreams, selTickers);
            DM.draw(state, selPlotters);
        }));
    }

    public void pullPlotCycle(Duration interval) {
//        if (running.containsKey("pullPlotCycle") && !running.get("pullPlotCycle").isDone()) {
//            return;
//        }
        for (String taskLabel : new String[]{
//                "predictPlot",
//                "predictAct",
//                "predictPlotCycle",
//                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }

        Future<?> task = this.cycleService.scheduleWithFixedDelay(() -> pullPlot(interval), 0, 1000, TimeUnit.MILLISECONDS);
        running.put("predictPlotCycle", task);
    }


    public void predictPlot() {
        List<Ticker> selTickers = tickers.getSelection();
        List<Upstream> selUpstreams = upstreams.getSelection();
        List<ModelPredictor> selModels = models.getSelection();
        List<DrawInstructor> selPlotters = plotters.getSelection();
        for (String taskLabel : new String[]{
//                "predictPlot",
//                "predictAct",
//                "predictPlotCycle",
//                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("predictPlot", CompletableFuture.runAsync(() -> {
            Map<Duration, Integer> ld = PM.getDependencies(selModels).first;
            State state = UM.update(ld, selUpstreams, selTickers);
            List<PredictManager.PredictResult> predictions = PM.predict(state, state.getTickers(), selModels);
            DM.drawPredict(state, predictions, selPlotters);
        }));
    }

    public void predictPlotCycle() {
//        if (running.containsKey("predictPlotCycle") && !running.get("predictPlotCycle").isDone()) {
//            return;
//        }
        for (String taskLabel : new String[]{
//                "predictPlot",
//                "predictAct",
//                "predictPlotCycle",
//                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }

        Future<?> task = this.cycleService.scheduleWithFixedDelay(this::predictPlot, 0, 1000, TimeUnit.MILLISECONDS);
        running.put("predictPlotCycle", task);
    }

    public void predictAct() {
        List<Ticker> selTickers = tickers.getSelection();
        List<Upstream> selUpstreams = upstreams.getSelection();
        List<ModelPredictor> selModels = models.getSelection();
        List<DrawInstructor> selPlotters = plotters.getSelection();
        List<Agent> selAgents = agents.getSelection();
        Set<Executor> selExecutors = new HashSet<>(executors.getSelection());
        for (String taskLabel : new String[]{
//                "predictPlot",
//                "predictAct",
//                "predictPlotCycle",
//                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("predictAct", CompletableFuture.runAsync(() -> {
            Map<Duration, Integer> ld = PM.getDependencies(selModels).first;
            State state = UM.update(ld, selUpstreams, selTickers);
            List<PredictManager.PredictResult> predictions = PM.predict(state, state.getTickers(), selModels);
            DM.drawPredict(state, predictions, selPlotters);
            selAgents.stream()
                    .map(agent -> agent.suggest(state, predictions))
                    .forEach(actions -> actions.forEach((ticker, action) -> {
                        ticker.preferredExecutors(ZonedDateTime.now()).stream()
                                .filter(selExecutors::contains)
                                .findFirst()
                                .map(exe -> exe.executeMarketOrder(ticker, action))
                                .ifPresent(result -> reporter.report(result));
                    }));
        }));
    }
    public void predictActCycle(Duration step) {
//        if (running.containsKey("predictActCycle") && !running.get("predictActCycle").isDone()) {
//            return;
//        }
        for (String taskLabel : new String[]{
//                "predictPlot",
//                "predictAct",
//                "predictPlotCycle",
//                "predictActCycle"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        Future<?> task = this.cycleService.scheduleWithFixedDelay(this::predictAct, 0, step.toMillis(), TimeUnit.MILLISECONDS);
        running.put("predictActCycle", task);
    }



    public void backtestPredict(ZonedDateTime at) {
        List<Ticker> selTickers = tickers.getSelection();
        List<Upstream> selUpstreams = upstreams.getSelection();
        List<ModelPredictor> selModels = models.getSelection();
        List<DrawInstructor> selPlotters = plotters.getSelection();
        for (String taskLabel : new String[]{
//                "backtestPredict",
//                "backtestAct"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        this.running.put("backtestPredict", CompletableFuture.runAsync(() -> {
            Util.Pair<Map<Duration,Integer>,Map<Duration,Integer>> deps = PM.getDependencies(selModels);
            Util.Pair<State, State> states = UM.snapshot(deps.first, deps.second, at, selUpstreams, selTickers);
            List<PredictManager.PredictResult> results = PM.predict(states.first, states.first.getTickers(), selModels);
            DM.drawBacktest(states.first, states.second, results, selPlotters);
        }));

    }
    public void backtestAct(ZonedDateTime at) {

    }
    public void simulate(ZonedDateTime from, ZonedDateTime to, Duration step) {
        List<Ticker> selTickers = tickers.getSelection();
        List<Upstream> selUpstreams = upstreams.getSelection();
        List<ModelPredictor> selModels = models.getSelection();
        List<DrawInstructor> selPlotters = plotters.getSelection();
        List<Agent> selAgents = agents.getSelection();
        for (String taskLabel : new String[]{
//                "backtestPredict",
//                "backtestAct"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        running.put("simulate", CompletableFuture.runAsync(() -> {
            Simulator sim = new Simulator(from, step);
            List<Simulator.SimResult> results = new ArrayList<>();
            while (!sim.step().isAfter(to)) {
                Util.Pair<Map<Duration,Integer>,Map<Duration,Integer>> deps = PM.getDependencies(selModels);
                State state = UM.snapshot(deps.first,Map.of(),sim.step(), selUpstreams, selTickers).first;
                List<PredictManager.PredictResult> predictions = PM.predict(state, state.getTickers(), selModels);
                Map<Ticker, Position> action = selAgents.stream()
                        .map(agent -> agent.suggest(state, predictions))
                        .parallel()
                        .reduce(new HashMap<>(),
                                (m1, m2) -> {m1.putAll(m2); return m1;},
                                (m1, m2) -> {m1.putAll(m2); return m1;}
                        );
                results.add(sim.act(state, action));
            }
            reporter.report(results);
        }));
    }
    public void simulateCycle(Duration step) {
        List<Ticker> selTickers = tickers.getSelection();
        List<Upstream> selUpstreams = upstreams.getSelection();
        List<ModelPredictor> selModels = models.getSelection();
        List<DrawInstructor> selPlotters = plotters.getSelection();
        List<Agent> selAgents = agents.getSelection();
        for (String taskLabel : new String[]{
//                "backtestPredict",
//                "backtestAct"
        }) {
            if (running.containsKey(taskLabel)) {
                running.get(taskLabel).cancel(true);
                running.remove(taskLabel);
            }
        }
        Simulator sim = new Simulator.nowSimulator();
        List<Simulator.SimResult> results = new ArrayList<>();
        running.put("simulateCycle", cycleService.scheduleWithFixedDelay(() -> {
            Util.Pair<Map<Duration,Integer>,Map<Duration,Integer>> deps = PM.getDependencies(selModels);
            State state = UM.snapshot(deps.first,Map.of(),sim.step(), selUpstreams, selTickers).first;
            List<PredictManager.PredictResult> predictions = PM.predict(state, state.getTickers(), selModels);
            Map<Ticker, Position> action = selAgents.stream()
                    .map(agent -> agent.suggest(state, predictions))
                    .parallel()
                    .reduce(new HashMap<>(),
                            (m1, m2) -> {m1.putAll(m2); return m1;},
                            (m1, m2) -> {m1.putAll(m2); return m1;}
                    );
            results.add(sim.act(state, action));
            reporter.report(results);
        }, 0, step.toMillis(), TimeUnit.MILLISECONDS));
    }

    public void endTasks() {
        this.running.values().forEach(task -> task.cancel(true));
        this.running.values().forEach(task -> System.out.println(task.isDone()));
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
