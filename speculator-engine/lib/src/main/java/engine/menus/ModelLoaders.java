package engine.menus;



import engine.Serialisation.EditMenu;
import engine.Serialisation.StateLoader;
import engine.Serialisation.UserStateMachine;
import engine.components.ModelPredictor;
import engine.modelPredictors.AttentionS;
import engine.modelPredictors.NN16842;

import java.util.List;

public class ModelLoaders {
    public static List<UserStateMachine.UserStateLoader<ModelPredictor>> list = List.of(
            /// CONFIG
            new ModelPredictor.OffSetLoader(new NN16842.Loader()),
            new ModelPredictor.OffSetLoader(new AttentionS.Loader())
    );
    public static EditMenu<ModelPredictor> menu = new EditMenu<>(list);

}
