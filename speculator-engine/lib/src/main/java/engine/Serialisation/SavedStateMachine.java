package engine.Serialisation;

import java.awt.SecondaryLoop;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import engine.PriceData.State;

public class SavedStateMachine <T extends StateMachine<T>> implements Serializable {
    private StateLoader<T> loader;
    private HashMap<String, String> state;
    private transient T object;

    public SavedStateMachine(StateLoader<T> loader, Map<String, String> state) {
        this.loader = loader;
        this.state = new HashMap<>(state);
    }

    @SuppressWarnings("unchecked")
    public SavedStateMachine(T stateMachine) {
        this((StateLoader<T>)stateMachine.getLoader(), stateMachine.save());
    }

    public T get() {
        if (this.object == null) {
            this.object = this.loader.load(this.state);

        }
        return this.object;
    }

    @Override
    public String toString() {
        return this.loader.toString(this.state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SavedStateMachine) {
            SavedStateMachine other = (SavedStateMachine) obj;
            return
                    this.loader.getClass().equals(other.loader.getClass())
                    && this.state.equals(other.state)
                    ;
        } else {
            return false;
        }
    }
}
