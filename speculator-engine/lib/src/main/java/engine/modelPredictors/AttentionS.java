package engine.modelPredictors;


import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import engine.Serialisation.StateMachine;
import engine.Util;
import engine.components.ModelPredictor;

public class AttentionS extends LogNN {
    private static LinkedHashMap<Duration, Integer> leftDep =new LinkedHashMap<>();
    private static LinkedHashMap<Duration, Integer> rightDep =new LinkedHashMap<>();
    static {
        leftDep.put(Duration.ofMinutes(15), 64);
        leftDep.put(Duration.ofHours(1), 64);
        leftDep.put(Duration.ofDays(1), 64);
        rightDep.put(Duration.ofHours(1), 4);
        rightDep.put(Duration.ofDays(1), 4);
        rightDep.put(Duration.ofDays(7), 2);
    }
    public AttentionS(Map<String, String> settings) {
        super("attention_s", "input", "output",
                Util.Pair.create(leftDep, rightDep)
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
