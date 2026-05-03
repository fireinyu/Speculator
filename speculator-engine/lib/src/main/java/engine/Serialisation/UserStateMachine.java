package engine.Serialisation;

import java.util.Collection;
import java.util.Map;

public abstract class UserStateMachine<T extends StateMachine<T>> implements StateMachine<T>{
    Map<String, String> settings;
    public UserStateMachine(Map<String, String> settings) {
        this.settings = settings;
    }

//    @Override
//    public StateLoader<? extends StateMachine<T>> getLoader() {
//        return (StateLoader<T>) state -> UserStateMachine.this.loadFromSettings(settings);
//    }

    @Override
    public Map<String, String> save() {
        return settings;
    }

    @Override
    public abstract UserStateLoader<? extends StateMachine<T>> getLoader();


//    protected abstract T loadFromSettings(Map<String, String> settings);
    public static abstract class UserStateLoader<T extends StateMachine<T>> implements StateLoader<T> {
        private Collection<String> options;
        public UserStateLoader(Collection<String> options) {
            this.options = options;
        }
        public Collection<String> getOptions() {
            return this.options;
        }
        protected void addOption(String name) {
            this.options.add(name);
        }
    }
}
