package com.example.speculator;

import android.content.Context;

import com.example.speculator.dynamicUI.ObjectMenu;

import engine.Instances.DrawInstructors;
import engine.Instances.Tickers;
import engine.Serialisation.LocalList;
import engine.Serialisation.SavedStateMachine;
import engine.components.DrawInstructor;
import engine.components.ModelPredictor;
import engine.PriceData.Ticker;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import engine.Serialisation.LocalObject;
import engine.components.PredictManager;
import engine.sugar.Preset;

public class GlobalState {
    /* TODO LIST
     * 1. (backtest) overlay actual data <DONE>
     * 2. upstream to cache data
     * 3. (predict & backtest) multiple predictors <DONE>
     * 3a. (predict & backtest) prediction function given particular predictor and ticker <IGNORE>
     * 3b. (predict & backtest) to use first predictor if multiple tickers; other wise use all predictors <DONE>
     * 4. arbitrary ticker instead of hard-coding (do for backtest) <DONE>
     * 5. (predict & backtest) multiple tickers <DONE>
     * 6. (deploy & simulate) prototypical agent and interface
     * 7. (predict) live price stream and predictions; configure prediction interval <DONE>
     * 8. djl -> Executorch
     * 9. limit datetime range based on upstream/ticker & left-dep
     * 10. fix "exceeded rate limit"
     * 11. fix http3 connection leaking <DONE>
     * 12. make repo public (separate from python part) <DONE>
     * 13. update & push pyWorkflow template
     * 14. min-max bound plotter <DONE>
     * 15. change serialisation to be platform-agnostic <DONE>
     * 16. Select 1 of multiple plotters <DONE>
     * 17. isolate engine code <DONE>
     * 18. migrate LinePlotter to Instructor <DONE>
     * 19. ObjectMenu save and load state(refactoring) <DONE>
     * 20. migrate group selectors to ObjectMenu <DONE>
     * 21. refactor model storage into list / LocalObject <DONE>
     * 22. refactor upstream and state
     * 23. multiple predict per ticker w/ multiple ticker <DONE>
     * 24. LinePlotter labelling / legend
     * 25. Staggered ModelPredictor wrapper (use TimeSeries binary search) <DONE>
     * 26. (engine) presets <DONE>
     * 27. (android) ObjectMenu preset <DONE>
     * 28. fix poor performance of backtest (probably targets) <DONE>
     */ 
    static Path appStorageRoot;
    static List<Ticker> tickers = List.of(
            // CONFIG
            /// supported tickers
            Tickers.XNG,
            Tickers.SGD
    );

    public static void init (Context context) {
        GlobalState.appStorageRoot = context.getFilesDir().toPath();

        Predict.init(context);
        Presets.init(context);

    }

    public static class Authentication {
        public static Path storageRoot = GlobalState.appStorageRoot.resolve("auth");
        public static class Oanda {
            public static Path storageRoot = Authentication.storageRoot.resolve("oanda");
            public static LocalObject<String> apiKey = new LocalObject.Encrypted<>(storageRoot, "apiKey");
            public static LocalObject<String> accNo = new LocalObject.Encrypted<>(storageRoot,  "accNo");
        }
    }

    public static class Loop {
        public static Duration interval = Duration.ofSeconds(1); // wait time after callback complete
    }

    public static class Presets {
        public static Path storageRoot;
        public static LocalList<Preset<Float, Float>> presets;
        public static ObjectMenu<Preset<Float, Float>> presetMenu;
        public static LocalObject<Preset<Float, Float>> defaultPreset;
        public static void init(Context context) {
            Presets.storageRoot = GlobalState.appStorageRoot.resolve("presets");
            Presets.presets = new LocalList<>(Presets.storageRoot, "list");
            Presets.defaultPreset = new LocalObject<>(Presets.storageRoot, "default");
            Presets.presetMenu = ObjectMenu.of(
                    context,
                    Presets.presets.get(),
                    1,
                    presetList -> {
                        if (presetList.isEmpty()) {
                            return;
                        }
                        Preset<Float, Float> selected = presetList.get(0);
                        Predict.tickerMenu.preset(selected.getTickers().stream().map(Tickers.map::get).collect(Collectors.toList()));
                        Predict.predictorMenu.preset(selected.getModelStates());
                        Predict.instructorMenu.preset(selected.getInstructorStates().stream().map(SavedStateMachine::get).collect(Collectors.toList()));
                    }
            );
            defaultPreset.get().ifPresent(preset -> Presets.presetMenu.preset(List.of(preset)));
        }
    }

    public static class Predict {
        public static Path storageRoot;
        public static LocalList<SavedStateMachine<ModelPredictor<Float, Float>>> predictors;
        public static List<SavedStateMachine<ModelPredictor<Float, Float>>> selectedPredictors;
        public static PredictManager<Float, Float> predictManager;
        public static PredictManager<Float, Float> pullManager;
        public static ObjectMenu<SavedStateMachine<ModelPredictor<Float, Float>>> predictorMenu;
        public static ObjectMenu<Ticker> tickerMenu;
        public static ObjectMenu<DrawInstructor<Float, Float>> instructorMenu;
        public static void init(Context context) {
            Predict.storageRoot = GlobalState.appStorageRoot.resolve("models");
            Predict.predictors = new LocalList<>(Predict.storageRoot);
            Predict.pullManager = new PredictManager<>(List.of(ModelPredictor.identity(
                    List.of(Duration.ofSeconds(60)),
                    List.of(100)
            )));
            Predict.selectedPredictors = new ArrayList<>();
            Predict.predictorMenu = ObjectMenu.of(
                    context,
                    GlobalState.Predict.predictors.get(),
                    3,
                    smList -> {
                        GlobalState.Predict.selectedPredictors = new ArrayList<>(smList);
                        Predict.predictManager = new PredictManager<>(smList.stream().map(SavedStateMachine::get).collect(Collectors.toList()));
                    }
            );
            if (predictors.size() > 0) {
                predictorMenu.check(0);
            }
            Predict.tickerMenu = ObjectMenu.of(context, tickers, 3, x->{});
            Predict.instructorMenu = ObjectMenu.of(context, DrawInstructors.list, DrawInstructors.list.subList(0, 1), 1, x->{});
        }
    }

}
