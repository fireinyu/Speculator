package engine.Serialisation;

import java.awt.SecondaryLoop;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import engine.PriceData.State;

public class SavedStateMachine <T extends StateMachine<T>> implements Serializable {
    private StateLoader<T> loader;
    private HashMap<String, String> state;

    public SavedStateMachine(StateLoader<T> loader, Map<String, String> state) {
        this.loader = loader;
        this.state = new HashMap<>(state);
    }

    public T get() {
        return this.loader.load(this.state);
    }
}
