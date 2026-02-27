package engine.sugar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import engine.Serialisation.SavedStateMachine;
import engine.components.DrawInstructor;
import engine.components.ModelPredictor;

public class Preset <T extends Number, V extends Number> implements Serializable {
    private ArrayList<SavedStateMachine<ModelPredictor<T, V>>> modelStates;
    private ArrayList<String> tickers;
    private ArrayList<SavedStateMachine<DrawInstructor<T, V>>> instructorStates;
    private String name;

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Preset)) {
            return false;
        }
        Preset<?, ?> other = (Preset<?, ?>) obj;
        return tickers.equals(other.tickers) &&
                modelStates.equals(other.modelStates) &&
                instructorStates.equals(other.instructorStates);
    }

    public Preset(
            String name,
            List<SavedStateMachine<ModelPredictor<T, V>>>  modelStates,
            List<String> tickers,
            List<SavedStateMachine<DrawInstructor<T, V>>> instructorStates
        ) {
        this.name = name;
        this.modelStates = new ArrayList<>(modelStates);
        this.tickers = new ArrayList<>(tickers);
        this.instructorStates = new ArrayList<>(instructorStates);

    }
    public ArrayList<SavedStateMachine<DrawInstructor<T, V>>> getInstructorStates() {
        return instructorStates;
    }

    public ArrayList<SavedStateMachine<ModelPredictor<T, V>>> getModelStates() {
        return modelStates;
    }

    public ArrayList<String> getTickers() {
        return tickers;
    }

    @Override
    public String toString() {
        return name;
    }
}
