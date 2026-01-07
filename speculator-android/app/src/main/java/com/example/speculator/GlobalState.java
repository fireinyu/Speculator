package com.example.speculator;

import android.content.Context;

import com.example.speculator.dynamicUI.ObjectMenu;

import engine.Instances.DrawInstructors;
import engine.Instances.UpstreamAdapters;
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
     */ 
    static Path appStorageRoot;

    public static void init (Context context) {
        GlobalState.appStorageRoot = context.getFilesDir().toPath();

        Predict.init(context);


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
            Predict.tickerMenu = ObjectMenu.of(context, UpstreamAdapters.getTickers(), 3, x->{});
            Predict.instructorMenu = ObjectMenu.of(context, DrawInstructors.list, DrawInstructors.list.subList(0, 1), 1, x->{});
        }
    }

}
