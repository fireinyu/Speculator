package engine.components;

import java.util.List;

public abstract class Plotter <T extends  Number, V extends Number> {

    public abstract void unplot ();

    public abstract void plotAllBackTest(
            List<PredictManager.BacktestResult<T, V>> results
    );

    public abstract void plotAllPredict(
            List<PredictManager.PredictResult<T, V>> results
    );

    public void plotPredict(
            PredictManager.PredictResult<T, V> result
    ) {
        this.plotAllPredict(List.of(result));
    }

    public void plotBackTest(
            PredictManager.BacktestResult<T, V> result
    ) {
        this.plotAllBackTest(List.of(result));
    }



}
