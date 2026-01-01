package engine.components;

import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.util.List;

public abstract class DrawInstructor<V extends Number> extends Plotter<V> {

    private InstructedDrawer drawer;

    public DrawInstructor(InstructedDrawer drawer) {
        this.drawer = drawer;
    }

    protected abstract DrawInstruction singleDraw(TimeSeries<V> input, String label);
    protected abstract List<DrawInstruction> drawAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs, List<? extends TimeSeries<V>> predsLs, List<? extends TimeSeries<V>> targetsLs);

    @Override
    public void plot(TimeSeries<V> input, String label) {
        this.singleDraw(input, label).drawBy(this.drawer);
    }

    @Override
    public void unplot() {
        this.drawer.undraw();
    }

    @Override
    public void plotAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs, List<? extends TimeSeries<V>> predsLs, List<? extends TimeSeries<V>> targetsLs) {
        this.drawAll(tickers, featuresLs, predsLs, targetsLs).forEach(instruction -> instruction.drawBy(this.drawer));
    }
}
