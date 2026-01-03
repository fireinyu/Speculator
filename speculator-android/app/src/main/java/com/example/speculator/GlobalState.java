package com.example.speculator;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import engine.Instances.DrawInstructors;
import engine.Instances.ModelPredictors;
import engine.Instances.UpstreamAdapters;
import engine.components.DrawInstructor;
import engine.components.ModelPredictor;
import engine.PriceData.Ticker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.Serialisation.LocalObject;
import engine.Serialisation.LocalStateMap;

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
     * 7. (predict) live price stream and predictions; configure prediction interval
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
     * 19. ObjectMenu save and load state(refactoring)
     * 20. migrate group selectors to ObjectMenu
     * 20a. tickers
     * 20b. plotters
     * 20c. modelBuilders
     * 20d. models
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

    public static class Predict {
        public static Path storageRoot;
        public static Map<String, LocalStateMap<ModelPredictor<Float, Float>>> predictors;
        public static List<Pair<Pair<String, String>, ModelPredictor<Float, Float>>> selectedPredictors;
        public static List<Ticker> tickers;
        public static List<Ticker> selectedTickers;
        public static List<DrawInstructor<Float>> instructors;
        public static DrawInstructor<Float> selectedInstructor;
        public static void init(Context context) {
            Predict.storageRoot = GlobalState.appStorageRoot.resolve("models");
            Map<String, ModelPredictor<Float, Float>> bases = ModelPredictors.bases;
            Predict.predictors = new HashMap<>();
            Predict.selectedPredictors = new ArrayList<>();
            for (String baseName : bases.keySet()) {
                Predict.predictors.put(
                        baseName,
                        new LocalStateMap<>(bases.get(baseName), Predict.storageRoot.resolve(baseName))
                );
            }
            Predict.tickers = UpstreamAdapters.getTickers();
            Predict.selectedTickers = new ArrayList<>();

            Predict.instructors = DrawInstructors.list;
            Predict.selectedInstructor = Predict.instructors.get(0);
            Log.d("debug_plot", "" + GlobalState.Predict.selectedInstructor);
            // remove before flight
        }
    }

}
