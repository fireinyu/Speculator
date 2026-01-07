package engine.Instances;



import engine.Serialisation.StateLoader;
import engine.Serialisation.StateMachine;
import engine.components.ModelPredictor;
import engine.modelPredictors.NN16842;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelLoaders {
    public static List<StateLoader<ModelPredictor<Float, Float>>> list = Stream.<ModelPredictor<Float, Float>>of(
            // CONFIG
            ModelPredictor.offset(new NN16842(), Duration.ZERO)
    )
            .map(StateMachine::getLoader)
            .map(loader -> (StateLoader<ModelPredictor<Float, Float>>)loader)
            .collect(Collectors.toList());

}
