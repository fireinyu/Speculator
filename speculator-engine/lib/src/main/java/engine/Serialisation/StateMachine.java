package engine.Serialisation;


import java.util.Map;

public interface StateMachine<R> {
    /*
        StateMachine obj;
        \ASSERT obj.base().load(obj.save()) == obj

     */
    StateLoader<? extends StateMachine<R>> getLoader();
    /*identity state
     */
    Map<String, String> save();

}
