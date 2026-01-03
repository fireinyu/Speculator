package engine.Serialisation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

public class LocalStateMachine <T extends StateMachine<T>> extends LocalObject<SavedStateMachine<T>>{
    //TODO

    public LocalStateMachine(Path root, String... tags) {
        super(root, tags);
    }

    public Optional<T> getObject() {
        return super.get().map(SavedStateMachine::get);
    }

    @SuppressWarnings("unchecked")
    public void putObject(T object) {
        super.put(new SavedStateMachine<>((StateLoader<T>) object.getLoader(), object.save()));
    }
}
