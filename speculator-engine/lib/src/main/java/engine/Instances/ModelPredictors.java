package engine.Instances;



import engine.components.ModelPredictor;
import engine.modelPredictors.NN16842;

import java.util.Map;

public class ModelPredictors {
    public static Map<String, ModelPredictor<Float, Float>> bases = Map.of(
            //CONFIG
            "NN16842-S5-M1", new NN16842()
    );

}
