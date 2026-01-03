package engine.components;

import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.util.List;

public abstract class DrawInstructor<V extends Number> {

    public InstructedPlotter<V> makePlotter(InstructedDrawer drawer) {
        return new InstructedPlotter<>(this, drawer);
    }
    protected abstract DrawInstruction singleDraw(TimeSeries<V> input, String label);
    protected abstract List<DrawInstruction> drawAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs, List<? extends TimeSeries<V>> predsLs, List<? extends TimeSeries<V>> targetsLs);



    private static class InstructedPlotter <T extends Number> extends Plotter <T> {
        private DrawInstructor<T> instructor;
        private InstructedDrawer drawer;
        public InstructedPlotter(DrawInstructor<T> instructor, InstructedDrawer drawer) {
            this.instructor = instructor;
            this.drawer = drawer;

        }@Override
        public void plot(TimeSeries<T> input, String label) {
            instructor.singleDraw(input, label).drawBy(this.drawer);
        }

        @Override
        public void unplot() {
            this.drawer.undraw();
        }

        @Override
        public void plotAll(List<? extends Ticker> tickers, List<? extends TimeSeries<T>> featuresLs, List<? extends TimeSeries<T>> predsLs, List<? extends TimeSeries<T>> targetsLs) {
            instructor.drawAll(tickers, featuresLs, predsLs, targetsLs).forEach(instruction -> instruction.drawBy(this.drawer));
        }

    }
}
