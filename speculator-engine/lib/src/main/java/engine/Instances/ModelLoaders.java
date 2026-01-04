package engine.Instances;



import engine.Serialisation.StateLoader;
import engine.Serialisation.StateMachine;
import engine.components.ModelPredictor;
import engine.modelPredictors.NN16842;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelLoaders {
    public static List<StateLoader<ModelPredictor<Float, Float>>> list = Stream.<ModelPredictor<Float, Float>>of(
            // CONFIG
            new NN16842(null)
    )
            .map(StateMachine::getLoader)
            .map(loader -> (StateLoader<ModelPredictor<Float, Float>>)loader)
            .collect(Collectors.toList());

}
