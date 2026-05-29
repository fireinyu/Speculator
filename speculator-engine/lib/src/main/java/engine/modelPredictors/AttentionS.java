package engine.modelPredictors;


import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import engine.Serialisation.StateMachine;
import engine.Util;
import engine.components.ModelPredictor;

public class AttentionS extends LogNN {
    public AttentionS(Map<String, String> settings) {
        super("attention_s", "input", "output",
                Util.Pair.create(
                        new LinkedHashMap<>(Map.of(
                                Duration.ofMinutes(15), 64,
                                Duration.ofHours(1), 64,
                                Duration.ofDays(1), 64
                        )),
                        new LinkedHashMap<>(Map.of(
                                Duration.ofHours(1), 4,
                                Duration.ofDays(1), 4,
                                Duration.ofDays(7), 2
                        ))
                )
                , settings);
    }

    @Override
    public UserStateLoader<? extends StateMachine<ModelPredictor>> getLoader() {
        return new Loader();
    }
    public static class Loader extends UserStateLoader<ModelPredictor> {
        public Loader() {
            super(List.of());
        }

        @Override
        public ModelPredictor load(Map<String, String> state) {
            return new AttentionS(state);
        }
    }
}
