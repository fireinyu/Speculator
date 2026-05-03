package engine.Serialisation;

import java.util.List;
import java.util.Map;

public abstract class CoreStateMachine<T extends StateMachine<T>> implements StateMachine<T> {
    public static class CoreStateLoader <V extends StateMachine<V>> implements StateLoader<V> {
        private List<V> source;
        public CoreStateLoader(List<V> source) {
            this.source = source;
        }
        @Override
        public V load(Map<String, String> state) {
            return source.get(Integer.parseInt(state.get("idx")));
        }
    }
//    private List<T> source;
    private int index;
    public CoreStateMachine(int index) {
        this.index = index;
    }

    @Override
    public abstract CoreStateLoader<? extends StateMachine<T>> getLoader();
    //    @Override
//    public StateLoader<? extends StateMachine<T>> getLoader() {
//        return (StateLoader<T>) state -> source.get(Integer.parseInt(state.get("idx")));
//    }

    @Override
    public Map<String, String> save() {
        return Map.of("idx", String.valueOf(index));
    }

}
