package com.example.speculator;

import android.content.Context;

import engine.control.App;

import java.nio.file.Path;

public class GlobalState {
    /* TODO LIST
     * 2. benchmark upstream cache data
     * 6. (deploy & simulate) prototypical agent and interface
     * 8. djl -> Executorch
     * 9. limit datetime range based on upstream/ticker & left-dep
     * 10. fix "exceeded rate limit"
     * 13. update & push pyWorkflow template
     * 22. refactor upstream and state <DONE>
     * 24. LinePlotter labelling / legend
     * 25. fix line artifact issue (obvious for usdsgd)
     */
    static Path appStorageRoot;
    public static App app;
    public static MPDrawer drawer;

//    static List<Ticker> tickers = List.of(
//            // CONFIG
//            /// supported tickers
//            Tickers.XNG,
//            Tickers.SGD
//    );

    public static void init (Context context) {
        appStorageRoot = context.getFilesDir().toPath();
        drawer = new MPDrawer();
        app = App.start(GlobalState.appStorageRoot, new AndroidReporter(), drawer);

//        Authentication.init(context);
//        Predict.init(context);
//        Presets.init(context);

    }

//    public static class Authentication {
//        public static Path storageRoot = GlobalState.appStorageRoot.resolve("auth");
//        public static void init(Context context) {
//            engine.upstreams.Oanda.authenticate(GlobalState.Authentication.Oanda.accNo, GlobalState.Authentication.Oanda.apiKey);
//        }
//        public static class Oanda {
//            public static Path storageRoot = Authentication.storageRoot.resolve("oanda");
//            public static LocalObject<String> apiKey = new LocalObject.Encrypted<>(storageRoot, "apiKey");
//            public static LocalObject<String> accNo = new LocalObject.Encrypted<>(storageRoot,  "accNo");
//        }
//    }
//
//    public static class Loop {
//        public static Duration interval = Duration.ofSeconds(1); // wait time after callback complete
//    }
//
//    public static class Presets {
//        public static Path storageRoot;
//        public static LocalList<Preset<Float, Float>> presets;
//        public static ObjectMenu<Preset<Float, Float>> presetMenu;
//        public static LocalObject<Preset<Float, Float>> defaultPreset;
//        public static void init(Context context) {
//            Presets.storageRoot = GlobalState.appStorageRoot.resolve("presets");
//            Presets.presets = new LocalList<>(Presets.storageRoot, "list");
//            Presets.defaultPreset = new LocalObject<>(Presets.storageRoot, "default");
//            Presets.presetMenu = ObjectMenu.of(
//                    context,
//                    Presets.presets.get(),
//                    1,
//                    presetList -> {
//                        if (presetList.isEmpty()) {
//                            return;
//                        }
//                        Preset<Float, Float> selected = presetList.get(0);
//                        Predict.tickerMenu.preset(selected.getTickers().stream().map(Tickers.map::get).collect(Collectors.toList()));
//                        Predict.predictorMenu.preset(selected.getModelStates());
//                        Predict.instructorMenu.preset(selected.getInstructorStates().stream().map(SavedStateMachine::get).collect(Collectors.toList()));
//                    }
//            );
//            defaultPreset.get().ifPresent(preset -> Presets.presetMenu.preset(List.of(preset)));
//        }
//    }
//
//    public static class Predict {
//        public static Path storageRoot;
//        public static LocalList<SavedStateMachine<ModelPredictor<Float, Float>>> predictors;
//        public static List<SavedStateMachine<ModelPredictor<Float, Float>>> selectedPredictors;
//        public static PredictManager<Float, Float> predictManager;
//        public static PredictManager<Float, Float> pullManager;
//        public static ObjectMenu<SavedStateMachine<ModelPredictor<Float, Float>>> predictorMenu;
//        public static ObjectMenu<Ticker> tickerMenu;
//        public static ObjectMenu<DrawInstructor<Float, Float>> instructorMenu;
//        public static void init(Context context) {
//            Predict.storageRoot = GlobalState.appStorageRoot.resolve("models");
//            Predict.predictors = new LocalList<>(Predict.storageRoot);
//            Predict.pullManager = new PredictManager<>(List.of(ModelPredictor.identity(
//                    List.of(Duration.ofSeconds(60)),
//                    List.of(100)
//            )));
//            Predict.selectedPredictors = new ArrayList<>();
//            Predict.predictorMenu = ObjectMenu.of(
//                    context,
//                    GlobalState.Predict.predictors.get(),
//                    3,
//                    smList -> {
//                        GlobalState.Predict.selectedPredictors = new ArrayList<>(smList);
//                        Predict.predictManager = new PredictManager<>(smList.stream().map(SavedStateMachine::get).collect(Collectors.toList()));
//                    }
//            );
//            if (predictors.size() > 0) {
//                predictorMenu.check(0);
//            }
//            Predict.tickerMenu = ObjectMenu.of(context, tickers, 3, x->{});
//            Predict.instructorMenu = ObjectMenu.of(context, DrawInstructors.list, DrawInstructors.list.subList(0, 1), 1, x->{});
//        }
//    }

}
