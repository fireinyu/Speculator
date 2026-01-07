package engine.components;

import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.util.List;

public abstract class DrawInstructor<T extends Number, V extends Number> {

    public InstructedPlotter<T, V> makePlotter(InstructedDrawer drawer) {
        return new InstructedPlotter<>(this, drawer);
    }
    protected abstract List<DrawInstruction> drawAllPredict(List<PredictManager.PredictResult<T, V>> results);

    protected abstract List<DrawInstruction> drawAllBacktest(List<PredictManager.BacktestResult<T, V>> results);


    private static class InstructedPlotter <T extends Number, V extends  Number> extends Plotter <T, V> {
        private DrawInstructor<T, V> instructor;
        private InstructedDrawer drawer;
        public InstructedPlotter(DrawInstructor<T, V> instructor, InstructedDrawer drawer) {
            this.instructor = instructor;
            this.drawer = drawer;

        }

        @Override
        public void unplot() {
            this.drawer.undraw();
        }

        @Override
        public void plotAllBackTest(List<PredictManager.BacktestResult<T, V>> backtestResults) {
            this.instructor.drawAllBacktest(backtestResults).forEach(drawInstruction -> drawInstruction.drawBy(this.drawer));
        }

        @Override
        public void plotAllPredict(List<PredictManager.PredictResult<T, V>> predictResults) {
            this.instructor.drawAllPredict(predictResults).forEach(drawInstruction -> drawInstruction.drawBy(this.drawer));

        }
    }
}
