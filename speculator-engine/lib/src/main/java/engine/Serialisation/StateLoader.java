package engine.Serialisation;

import java.io.Serializable;
import java.util.Map;
@FunctionalInterface
public interface StateLoader <T extends StateMachine<T>> extends Serializable {
    abstract T load(Map<String, String> state);
//    abstract String toString(Map<String, String> state);
}
