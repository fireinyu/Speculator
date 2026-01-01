package engine.Serialisation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

public class LocalStateMachine <T extends StateMachine<T>> {
    //TODO
    private LocalObject<HashMap<String, String>> localState;
    private T base;

    public LocalStateMachine(T template, Path root, String... tags) {
        this.localState = new LocalObject<>(root, tags);
        this.base = template.base();
    }

    public Optional<T> get() {
        return this.localState.get().map(hMap -> this.base.load(hMap));
    }

    public void put(T item) {
        this.localState.put(new HashMap<>(item.save()));
    }

    public void delete() {
        this.localState.delete();
    }
}
